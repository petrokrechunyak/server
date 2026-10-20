package com.alphabetas.bot.oblenergo.utils;

import com.alphabetas.bot.oblenergo.service.MessageService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The admin log chat is a group, where Telegram lets a bot post only about 20 messages a minute.
 * A broadcast can produce hundreds of notifications (every user who blocked the bot), so lines are
 * collected and posted together, at most once every {@link #FLUSH_INTERVAL_SECONDS} seconds.
 */
@Slf4j
public class LogChat {

    public static final Long CHAT_ID = -4592105386L;
    private static final long FLUSH_INTERVAL_SECONDS = 5;

    private static final Queue<String> pending = new ConcurrentLinkedQueue<>();
    private static MessageService messageService;
    private static ScheduledExecutorService flusher;

    public static synchronized void init(MessageService service) {
        messageService = service;
        if (flusher == null) {
            flusher = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "log-chat");
                thread.setDaemon(true);
                return thread;
            });
            flusher.scheduleWithFixedDelay(LogChat::flush, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    public static void send(String line) {
        pending.add(line);
    }

    static void flush() {
        try {
            List<String> lines = new ArrayList<>();
            for (String line = pending.poll(); line != null; line = pending.poll()) {
                lines.add(line);
            }
            for (String message : MainUtil.joinWithinLimit("", lines, "\n")) {
                messageService.sendMessage(CHAT_ID, message);
            }
        } catch (RuntimeException e) {
            // an exception would cancel all later flushes
            log.error("Failed to post to the log chat", e);
        }
    }
}
