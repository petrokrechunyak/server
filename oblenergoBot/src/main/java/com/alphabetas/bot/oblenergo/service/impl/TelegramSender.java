package com.alphabetas.bot.oblenergo.service.impl;

import com.alphabetas.bot.oblenergo.CallerBot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
import java.util.concurrent.ConcurrentHashMap;
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
 * Sends Telegram API calls for one bot and keeps them within Telegram's limits.
 * <p>
 * The library's own async sending runs on one thread over at most 2 HTTP connections, which caps a broadcast at
 * a few messages per second. Here one dispatcher thread starts requests at the allowed rate and up to
 * {@link #MAX_IN_FLIGHT} of them wait for Telegram's answer in parallel.
 * <p>
 * Limits: at most {@link #MESSAGES_PER_SECOND} requests start in any second (Telegram's broadcast limit), and
 * a group chat gets at most one message per {@link #GROUP_CHAT_SPACING_MS} ms (Telegram allows ~20 a minute
 * there). Non-bulk calls (replies, button edits) always go before queued bulk calls (schedule broadcasts, /say).
 * <p>
 * 429 Too Many Requests means "later", not "failed", so it gets up to {@link #MAX_RATE_LIMITED_ATTEMPTS} tries.
 * When it comes from one chat (a group over its own limit, a user flooded with messages), only that chat waits
 * retry_after and everything else keeps going. When {@link #FLOOD_CHATS} different chats get it within
 * {@link #FLOOD_WINDOW_MS} ms, the whole bot is over the limit: every send pauses for retry_after and the rate
 * is halved, then recovers gradually (see {@link RateLimiter}). Pausing the whole bot for one chat's 429 used to
 * stall entire broadcasts because of the admin log group.
 * <p>
 * Other failures get up to {@link #MAX_ATTEMPTS} attempts: 5xx, unreadable responses and network errors with
 * exponential backoff. Other 4xx errors (403 blocked by user, 400 chat not found or bad HTML, ...) fail at once.
 */
@Slf4j
public class TelegramSender {

    public static final int MESSAGES_PER_SECOND = 30;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_RATE_LIMITED_ATTEMPTS = 20;
    private static final long BASE_BACKOFF_MS = 1_000;
    // enough to keep sending at the full rate even when Telegram takes ~1 s to answer
    private static final int MAX_IN_FLIGHT = 30;
    private static final long GROUP_CHAT_SPACING_MS = 3_000;
    private static final int FLOOD_CHATS = 3;
    private static final long FLOOD_WINDOW_MS = 2_000;

    private final CallerBot bot;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final PriorityBlockingQueue<Task<?>> queue = new PriorityBlockingQueue<>();
    private final AtomicLong sequence = new AtomicLong();
    private final RateLimiter rateLimiter = new RateLimiter(MESSAGES_PER_SECOND);
    // chat id -> System.nanoTime() before which nothing may be sent to that chat
    private final ConcurrentHashMap<String, Long> chatReadyAt = new ConcurrentHashMap<>();
    // chat id -> System.nanoTime() of its last 429, to tell one chat's limit from the whole bot's
    private final ConcurrentHashMap<String, Long> recentRateLimits = new ConcurrentHashMap<>();
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

    static String chatIdOf(BotApiMethod<?> method) {
        if (method instanceof SendMessage) {
            return ((SendMessage) method).getChatId();
        }
        if (method instanceof EditMessageText) {
            return ((EditMessageText) method).getChatId();
        }
        if (method instanceof DeleteMessage) {
            return ((DeleteMessage) method).getChatId();
        }
        return null;
    }

    private void dispatch() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // permit first: the task is picked as late as possible, so a reply queued meanwhile still
                // goes before the pending bulk messages
                inFlight.acquire();
                try {
                    Task<?> task = queue.take();
                    long chatWaitNanos = chatWaitNanos(task.chatId);
                    if (chatWaitNanos > 0) {
                        // only this chat has to wait; it keeps its place (seq) for when it comes back
                        retryScheduler.schedule(() -> queue.put(task), chatWaitNanos, TimeUnit.NANOSECONDS);
                        inFlight.release();
                        continue;
                    }
                    rateLimiter.awaitSlot();
                    if (isGroupChat(task.chatId)) {
                        holdChat(task.chatId, GROUP_CHAT_SPACING_MS);
                    }
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

    private long chatWaitNanos(String chatId) {
        Long readyAt = chatId == null ? null : chatReadyAt.get(chatId);
        return readyAt == null ? 0 : readyAt - System.nanoTime();
    }

    private void holdChat(String chatId, long millis) {
        long until = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
        chatReadyAt.merge(chatId, until, (current, proposed) -> proposed - current > 0 ? proposed : current);
    }

    // group and channel ids are negative
    private static boolean isGroupChat(String chatId) {
        return chatId != null && chatId.startsWith("-");
    }

    private void onRateLimited(String chatId, long retryAfterMs) {
        long now = System.nanoTime();
        if (chatId != null) {
            holdChat(chatId, retryAfterMs);
        }
        recentRateLimits.put(chatId == null ? "" : chatId, now);
        recentRateLimits.values().removeIf(time -> now - time > TimeUnit.MILLISECONDS.toNanos(FLOOD_WINDOW_MS));
        if (chatId == null || recentRateLimits.size() >= FLOOD_CHATS) {
            int rate = rateLimiter.flood(retryAfterMs);
            if (rate > 0) {
                log.warn("Whole bot hit Telegram's rate limit: pausing all sends for {} ms, then {} messages/s",
                        retryAfterMs, rate);
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
    private static long backoffMs(TelegramApiException e, int failures) {
        if (failures >= MAX_ATTEMPTS || e instanceof TelegramApiValidationException) {
            return -1;
        }
        if (e instanceof TelegramApiRequestException) {
            // the library leaves the code at 0 when the response wasn't Telegram JSON (e.g. a proxy's 502 page)
            Integer code = ((TelegramApiRequestException) e).getErrorCode();
            if (code != null && code >= 400 && code < 500) {
                return -1;
            }
        }
        // 5xx, unreadable response or network error: 1s, 2s, 4s, 8s
        return BASE_BACKOFF_MS << (failures - 1);
    }

    private static boolean isTooManyRequests(TelegramApiException e) {
        return e instanceof TelegramApiRequestException
                && Integer.valueOf(429).equals(((TelegramApiRequestException) e).getErrorCode());
    }

    private static long retryAfterMs(TelegramApiRequestException e) {
        Integer retryAfter = e.getParameters() == null ? null : e.getParameters().getRetryAfter();
        return TimeUnit.SECONDS.toMillis(retryAfter == null ? 1 : retryAfter);
    }

    private final class Task<T extends Serializable> implements Runnable, Comparable<Task<?>> {
        private final BotApiMethod<T> method;
        private final String chatId;
        private final boolean bulk;
        // a retried task keeps its number, so it goes ahead of everything queued after it
        private final long seq;
        private final CompletableFuture<T> future;
        private int failures;
        private int rateLimited;

        Task(BotApiMethod<T> method, boolean bulk, long seq, CompletableFuture<T> future) {
            this.method = method;
            this.chatId = chatIdOf(method);
            this.bulk = bulk;
            this.seq = seq;
            this.future = future;
        }

        @Override
        public void run() {
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
            if (isTooManyRequests(e)) {
                if (++rateLimited >= MAX_RATE_LIMITED_ATTEMPTS) {
                    future.completeExceptionally(e);
                    return;
                }
                long retryAfterMs = retryAfterMs((TelegramApiRequestException) e);
                log.warn("{} to {} rate limited ({}/{}), retrying in {} ms",
                        method.getMethod(), chatId, rateLimited, MAX_RATE_LIMITED_ATTEMPTS, retryAfterMs);
                onRateLimited(chatId, retryAfterMs);
                // dispatch() holds it back until its chat (or the whole bot) may send again
                queue.put(this);
                return;
            }
            long delayMs = backoffMs(e, ++failures);
            if (delayMs < 0) {
                future.completeExceptionally(e);
                return;
            }
            log.warn("{} to {} failed (attempt {}/{}), retrying in {} ms: {}",
                    method.getMethod(), chatId, failures, MAX_ATTEMPTS, delayMs, e.getMessage());
            retryScheduler.schedule(() -> queue.put(this), delayMs, TimeUnit.MILLISECONDS);
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
     * Spaces sends evenly and, as a hard limit, never lets more than the current rate start within any
     * 1-second window.
     * <ul>
     * <li>Slow start: the first second after {@link #IDLE_NANOS} without sends runs at {@link #WARMUP_RATE}.
     * Fresh connections delay the first requests, which then reach Telegram bunched with the next ones,
     * so a full-rate start shows up there as more than the limit.</li>
     * <li>After a flood (the whole bot got 429) everything pauses for retry_after and the rate drops to
     * 80% of what triggered it. The lower ceiling holds until {@link #CEILING_RESET_NANOS} pass without
     * another flood, so a stricter-than-advertised Telegram limit is found in a few steps instead of
     * being hit over and over.</li>
     * </ul>
     * awaitSlot is called only by the dispatcher thread; the other methods by any thread.
     */
    static final class RateLimiter {
        private static final long WINDOW_NANOS = TimeUnit.SECONDS.toNanos(1);
        private static final long IDLE_NANOS = TimeUnit.SECONDS.toNanos(2);
        private static final int WARMUP_RATE = 15;
        private static final long CEILING_RESET_NANOS = TimeUnit.MINUTES.toNanos(10);
        private static final int MIN_RATE = 5;

        private final int maxRate;
        // start times of the last maxRate sends; `next` is the slot written next (the oldest)
        private final long[] recent;
        private int next;
        private long last;
        private volatile long pausedUntil;
        // guarded by this
        private int ceiling;
        private long lastFlood;
        private long warmupUntil;

        RateLimiter(int maxRate) {
            long now = System.nanoTime();
            this.maxRate = maxRate;
            this.recent = new long[maxRate];
            Arrays.fill(recent, now - WINDOW_NANOS);
            this.last = now - IDLE_NANOS - 1;
            this.pausedUntil = now;
            this.ceiling = maxRate;
            this.lastFlood = now - CEILING_RESET_NANOS;
            this.warmupUntil = now;
        }

        void awaitSlot() throws InterruptedException {
            while (true) {
                long now = System.nanoTime();
                if (now - last > IDLE_NANOS) {
                    startWarmup(now);
                }
                int currentRate = rate(now);
                // start time of the currentRate-th most recent send: the next one may start a second after it
                long windowStart = recent[Math.floorMod(next - currentRate, recent.length)];
                long allowedAt = later(later(last + WINDOW_NANOS / currentRate, windowStart + WINDOW_NANOS), pausedUntil);
                if (allowedAt - now <= 0) {
                    recent[next] = now;
                    next = (next + 1) % recent.length;
                    last = now;
                    return;
                }
                // re-checked in the loop: sleep may wake early, and a 429 may extend the pause meanwhile
                TimeUnit.NANOSECONDS.sleep(allowedAt - now);
            }
        }

        private synchronized void startWarmup(long now) {
            warmupUntil = now + WINDOW_NANOS;
        }

        synchronized int rate(long now) {
            if (ceiling < maxRate && now - lastFlood > CEILING_RESET_NANOS) {
                ceiling = maxRate;
            }
            return warmupUntil - now > 0 ? Math.min(ceiling, WARMUP_RATE) : ceiling;
        }

        /**
         * Pauses every send and lowers the rate.
         *
         * @return the new rate, or 0 if this flood was already handled
         */
        synchronized int flood(long pauseMillis) {
            long now = System.nanoTime();
            // one flood answers every request in flight with 429: lower the rate once per flood, not per answer
            boolean alreadyPaused = pausedUntil - now > 0;
            int current = rate(now);
            long until = now + TimeUnit.MILLISECONDS.toNanos(pauseMillis);
            if (until - pausedUntil > 0) {
                pausedUntil = until;
            }
            lastFlood = now;
            if (alreadyPaused) {
                return 0;
            }
            ceiling = Math.max(MIN_RATE, current * 4 / 5);
            return ceiling;
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
