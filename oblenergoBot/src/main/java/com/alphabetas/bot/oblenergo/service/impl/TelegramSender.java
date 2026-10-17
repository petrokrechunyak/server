package com.alphabetas.bot.oblenergo.service.impl;

import com.alphabetas.bot.oblenergo.CallerBot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sends Telegram API calls for one bot at no more than {@link #MESSAGES_PER_SECOND} (Telegram's broadcast limit)
 * and retries the failures that can succeed later.
 * <p>
 * The library's own async sending runs on one thread over at most 2 HTTP connections, which caps a broadcast at
 * a few messages per second. Here one dispatcher thread starts requests at the allowed rate and up to
 * {@link #MAX_IN_FLIGHT} of them wait for Telegram's answer in parallel.
 * <p>
 * Non-bulk calls (replies, button edits) always go before queued bulk calls (schedule broadcasts, /say),
 * so the bot stays responsive while a broadcast is going out.
 * <p>
 * Retried, up to {@link #MAX_ATTEMPTS} attempts: 429 Too Many Requests after the retry_after Telegram asks for
 * (pausing every send, since the limit is per bot), and 5xx, unreadable responses and network errors with
 * exponential backoff. Other 4xx errors (403 blocked by user, 400 chat not found or bad HTML, ...) are not
 * retried: sending again would fail the same way.
 */
@Slf4j
public class TelegramSender {

    public static final int MESSAGES_PER_SECOND = 30;
    private static final int MAX_ATTEMPTS = 5;
    private static final long BASE_BACKOFF_MS = 1_000;
    // enough to keep sending at the full rate even when Telegram takes ~1 s to answer
    private static final int MAX_IN_FLIGHT = 30;

    private final CallerBot bot;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final PriorityBlockingQueue<Task<?>> queue = new PriorityBlockingQueue<>();
    private final AtomicLong sequence = new AtomicLong();
    private final RateLimiter rateLimiter = new RateLimiter(MESSAGES_PER_SECOND);
    // the dispatcher takes a permit before starting a request, so requests never queue up behind busy threads
    // (they would then all go out at once when Telegram speeds up again, breaking the rate limit)
    private final Semaphore inFlight = new Semaphore(MAX_IN_FLIGHT);
    private final ExecutorService requestThreads =
            Executors.newFixedThreadPool(MAX_IN_FLIGHT, daemonThreads("telegram-request"));
    private final ScheduledExecutorService retryScheduler =
            Executors.newSingleThreadScheduledExecutor(daemonThreads("telegram-retry"));

    public TelegramSender(CallerBot bot) {
        this.bot = bot;
        daemonThreads("telegram-dispatcher").newThread(this::dispatch).start();
    }

    /**
     * @param bulk true for broadcasts: they wait until no non-bulk call is queued
     * @return future of Telegram's answer; fails with the last error once retries are exhausted
     */
    public <T extends Serializable> CompletableFuture<T> send(BotApiMethod<T> method, boolean bulk) {
        CompletableFuture<T> future = new CompletableFuture<>();
        queue.put(new Task<>(method, bulk, sequence.getAndIncrement(), future));
        return future;
    }

    private void dispatch() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // permit first: the task is picked as late as possible, so a reply queued meanwhile still
                // goes before the pending bulk messages
                inFlight.acquire();
                try {
                    Task<?> task = queue.take();
                    rateLimiter.awaitSlot();
                    requestThreads.execute(task);
                } catch (InterruptedException | RuntimeException e) {
                    inFlight.release();
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                log.error("Telegram dispatcher error", e);
            }
        }
    }

    private <T extends Serializable> T call(BotApiMethod<T> method) throws TelegramApiException, InterruptedException {
        // same request the library builds in DefaultAbsSender.sendMethodRequest
        method.validate();
        String json;
        try {
            json = objectMapper.writeValueAsString(method);
        } catch (JsonProcessingException e) {
            throw new TelegramApiValidationException("Unable to serialize " + method.getMethod(), method);
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(bot.getBaseUrl() + method.getMethod()))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        String response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
        } catch (IOException e) {
            throw new TelegramApiException("Unable to execute " + method.getMethod() + " method", e);
        }
        return method.deserializeResponse(response);
    }

    /**
     * @return how long to wait before the next attempt, or -1 to give up
     */
    private static long retryDelayMs(TelegramApiException e, int attempt) {
        if (attempt >= MAX_ATTEMPTS || e instanceof TelegramApiValidationException) {
            return -1;
        }
        if (e instanceof TelegramApiRequestException) {
            TelegramApiRequestException requestException = (TelegramApiRequestException) e;
            // the library leaves the code at 0 when the response wasn't Telegram JSON (e.g. a proxy's 502 page)
            Integer code = requestException.getErrorCode();
            if (isTooManyRequests(e)) {
                Integer retryAfter = requestException.getParameters() == null
                        ? null : requestException.getParameters().getRetryAfter();
                return TimeUnit.SECONDS.toMillis(retryAfter == null ? 1 : retryAfter);
            }
            if (code != null && code >= 400 && code < 500) {
                return -1;
            }
        }
        // 5xx, unreadable response or network error: 1s, 2s, 4s, 8s
        return BASE_BACKOFF_MS << (attempt - 1);
    }

    private static boolean isTooManyRequests(TelegramApiException e) {
        return e instanceof TelegramApiRequestException
                && Integer.valueOf(429).equals(((TelegramApiRequestException) e).getErrorCode());
    }

    private final class Task<T extends Serializable> implements Runnable, Comparable<Task<?>> {
        private final BotApiMethod<T> method;
        private final boolean bulk;
        // a retried task keeps its number, so it goes ahead of everything queued after it
        private final long seq;
        private final CompletableFuture<T> future;
        private int attempt;

        Task(BotApiMethod<T> method, boolean bulk, long seq, CompletableFuture<T> future) {
            this.method = method;
            this.bulk = bulk;
            this.seq = seq;
            this.future = future;
        }

        @Override
        public void run() {
            attempt++;
            try {
                future.complete(call(method));
            } catch (TelegramApiException e) {
                retryOrFail(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            } catch (RuntimeException e) {
                future.completeExceptionally(e);
            } finally {
                inFlight.release();
            }
        }

        private void retryOrFail(TelegramApiException e) {
            long delayMs = retryDelayMs(e, attempt);
            if (delayMs < 0) {
                future.completeExceptionally(e);
                return;
            }
            log.warn("{} failed (attempt {}/{}), retrying in {} ms: {}",
                    method.getMethod(), attempt, MAX_ATTEMPTS, delayMs, e.getMessage());
            if (isTooManyRequests(e)) {
                // Telegram's limit is per bot, so every send has to wait, not just this one
                rateLimiter.pauseFor(delayMs);
                queue.put(this);
            } else {
                retryScheduler.schedule(() -> queue.put(this), delayMs, TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public int compareTo(Task<?> other) {
            if (bulk != other.bulk) {
                return bulk ? 1 : -1;
            }
            return Long.compare(seq, other.seq);
        }
    }

    /**
     * Spaces sends evenly and, as a hard limit, never lets more than permitsPerSecond start within
     * any 1-second window. awaitSlot is called only by the dispatcher thread; pauseFor by any thread.
     */
    static final class RateLimiter {
        private static final long WINDOW_NANOS = TimeUnit.SECONDS.toNanos(1);

        private final long intervalNanos;
        // start times of the last permitsPerSecond sends, oldest at index `oldest`
        private final long[] recent;
        private int oldest;
        private long last;
        private volatile long pausedUntil;

        RateLimiter(int permitsPerSecond) {
            long now = System.nanoTime();
            this.intervalNanos = WINDOW_NANOS / permitsPerSecond;
            this.recent = new long[permitsPerSecond];
            Arrays.fill(recent, now - WINDOW_NANOS);
            this.last = now - intervalNanos;
            this.pausedUntil = now;
        }

        void awaitSlot() throws InterruptedException {
            while (true) {
                long now = System.nanoTime();
                long allowedAt = later(later(last + intervalNanos, recent[oldest] + WINDOW_NANOS), pausedUntil);
                if (allowedAt - now <= 0) {
                    recent[oldest] = now;
                    oldest = (oldest + 1) % recent.length;
                    last = now;
                    return;
                }
                // re-checked in the loop: sleep may wake early, and a 429 may extend the pause meanwhile
                TimeUnit.NANOSECONDS.sleep(allowedAt - now);
            }
        }

        synchronized void pauseFor(long millis) {
            long until = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
            if (until - pausedUntil > 0) {
                pausedUntil = until;
            }
        }

        // nanoTime values may only be compared by subtraction
        private static long later(long a, long b) {
            return a - b > 0 ? a : b;
        }
    }

    private static ThreadFactory daemonThreads(String name) {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, name + "-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }
}
