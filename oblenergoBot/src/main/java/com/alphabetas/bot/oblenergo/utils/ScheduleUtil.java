package com.alphabetas.bot.oblenergo.utils;

import com.alphabetas.bot.oblenergo.CallerBot;
import com.alphabetas.bot.oblenergo.model.CurrentDate;
import com.alphabetas.bot.oblenergo.model.Group;
import com.alphabetas.bot.oblenergo.model.User;
import com.alphabetas.bot.oblenergo.repo.CurrentDateRepo;
import com.alphabetas.bot.oblenergo.repo.GroupRepo;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ScheduleUtil {

    private static GroupRepo groupRepo;
    private static UserRepo userRepo;
    private static MessageService messageService;
    private static CurrentDateRepo currentDateRepo;
    private static final String[] dates = {
            "00:00", "00:30", "01:00", "01:30", "02:00", "02:30",
            "03:00", "03:30", "04:00", "04:30", "05:00", "05:30",
            "06:00", "06:30", "07:00", "07:30", "08:00", "08:30",
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
            "15:00", "15:30", "16:00", "16:30", "17:00", "17:30",
            "18:00", "18:30", "19:00", "19:30", "20:00", "20:30",
            "21:00", "21:30", "22:00", "22:30", "23:00", "23:30",
            "00:00"
    };
    public static final String EMERGENCY_TEXT = "⚠️ <b>Запроваджено екстрені відключення по Чернівецькій області</b>\n\n" +
            "Графіки погодинних відключень тимчасово не діють. Відключення можуть бути без попередження.\n\n" +
            "Коли графіки знову почнуть діяти, вас буде повідомлено.";
    private static final String EMERGENCY_ENDED_TEXT = "✅ <b>Екстрені відключення скасовано, графіки знову діють.</b>\n\n";
    // one char per half-hour slot: з - powered, в - off, м - maybe powered
    private static final Pattern SCHEDULE_PATTERN = Pattern.compile("[взм]{" + (dates.length - 1) + "}");

    public ScheduleUtil(MessageService messageService, UserRepo userRepo, GroupRepo groupRepo, CurrentDateRepo currentDateRepo) {
        ScheduleUtil.messageService = messageService;
        ScheduleUtil.userRepo = userRepo;
        ScheduleUtil.groupRepo = groupRepo;
        ScheduleUtil.currentDateRepo = currentDateRepo;
        LogChat.init(messageService);
    }

    // synchronized: /refresh and the scheduler could otherwise both detect the same change and broadcast it twice
    public static synchronized void checkForSchedule() {
        log.info("checking for new schedule");
        Document doc;
        try {
            doc = Jsoup.connect("https://oblenergo.cv.ua/shutdowns/?next")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                    .referrer("https://www.google.com")
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        processPage(doc);

        log.info("Checking for new schedule ended");
    }

    private static void processPage(Document doc) {
        // during emergency outages the site replaces the schedule with this banner
        if (!doc.select("article.wrap_article_v2_emergency").isEmpty()) {
            startEmergency();
            return;
        }

        List<String> dateTexts = doc.select("#gsv > ul > p").eachText();
        if (dateTexts.isEmpty()) {
            log.warn("Schedule date not found on the page, skipping check");
            return;
        }
        String newDate = dateTexts.get(0);
        if(currentDateRepo.findAll().isEmpty()) {
            currentDateRepo.save(new CurrentDate(newDate));
        }

        updateAndSendSchedule(newDate, doc);
    }

    public static String getCurrentDate() {
        return currentDateRepo.findAll().get(0).getDate();
    }

    public static boolean isEmergency() {
        return currentDateRepo.findAll().stream().anyMatch(state -> Boolean.TRUE.equals(state.getEmergency()));
    }

    private static void startEmergency() {
        List<CurrentDate> rows = currentDateRepo.findAll();
        // no row yet if the bot is first started during emergency outages; the date is filled in once the schedule is back
        CurrentDate state = rows.isEmpty() ? new CurrentDate((String) null) : rows.get(0);
        if (Boolean.TRUE.equals(state.getEmergency())) {
            log.info("Emergency outages are still active");
            return;
        }
        state.setEmergency(true);
        currentDateRepo.save(state);

        log.info("Emergency outages started");
        long time = System.currentTimeMillis();
        CompletableFuture<?>[] sends = userRepo.getAllBySubscribedTrueAndGroupsNotEmptyOrderByPriorityAsc().stream()
                .map(user -> sendMessageToUser(user, EMERGENCY_TEXT))
                .toArray(CompletableFuture[]::new);
        reportWhenSent(sends, "з попередженням про екстрені відключення", time);
    }

    private static void updateAndSendSchedule(String newDate, Document doc) {
        // parse every group before saving anything: failing halfway would leave some groups
        // updated in the DB but never announced, and the next check would see no change
        String[] schedules = new String[CallerBot.GROUPS_NUMBER + 1];
        for (int i = 1; i <= CallerBot.GROUPS_NUMBER; i++) {
            List<String> texts = doc.select("div#inf" + i).eachText();
            String el = texts.isEmpty() ? "" : texts.get(0).replaceAll(" ", "").replace("мз", "м");
            if (!SCHEDULE_PATTERN.matcher(el).matches()) {
                log.warn("Unexpected schedule for group {}: '{}', skipping check", i, el);
                return;
            }
            schedules[i] = el;
        }

        CurrentDate state = currentDateRepo.findAll().get(0);
        boolean newDay = !newDate.equals(state.getDate());
        // the schedule is back after emergency outages: resend it to everyone, like on a new day
        boolean emergencyEnded = Boolean.TRUE.equals(state.getEmergency());
        boolean resendAll = newDay || emergencyEnded;
        List<Group> updated = new ArrayList<>();

        for (int i = 1; i <= CallerBot.GROUPS_NUMBER; i++) {
            String el = schedules[i];
            Group group = groupRepo.findById(i).get();
            if (resendAll || !el.equals(group.getShutdowns())) {
                log.info("Schedule updated in group {}", group.getGroupId());
                group.setOldShutdowns(group.getShutdowns());
                group.setShutdowns(el);
                groupRepo.save(group);
                updated.add(group);
            }
        }


        if (resendAll) {
            state.setDate(newDate);
            state.setEmergency(false);
            currentDateRepo.save(state);
        }
        if (emergencyEnded) {
            log.info("Emergency outages ended");
        }
        // sent as part of each schedule message, so it can't arrive after the schedule
        String prefix = emergencyEnded ? EMERGENCY_ENDED_TEXT : "";

        log.info("Schedule updated in number of groups: {}", updated.size());
        if (updated.isEmpty()) {
            return;
        }

        // built once per group; these Group instances are the only ones holding oldShutdowns for highlighting
        Map<Integer, String> longMessages = new HashMap<>();
        Map<Integer, String> compactMessages = new HashMap<>();
        for (Group group : updated) {
            longMessages.put(group.getGroupId(), prepareScheduleMessage(group, group.getShutdowns(), newDate, resendAll));
            compactMessages.put(group.getGroupId(), prepareCompactScheduleMessage(group, group.getShutdowns(), newDate, resendAll));
        }

        // per user, so someone subscribed to several updated groups gets them together instead of one message each
        long time = System.currentTimeMillis();
        List<CompletableFuture<Message>> sends = new ArrayList<>();
        for (User user : userRepo.findSubscribedWithGroups()) {
            Map<Integer, String> messages = Boolean.TRUE.equals(user.getCompact()) ? compactMessages : longMessages;
            List<String> parts = user.getGroups().stream()
                    .map(Group::getGroupId)
                    .sorted()
                    .map(messages::get)
                    .filter(Objects::nonNull)
                    .map(String::stripTrailing)
                    .collect(Collectors.toList());
            // worst case one group takes ~1200 chars detailed and ~1400 compact, so at least 3 detailed
            // or 2 compact groups always fit in one message; queued back to back, in group order
            for (String message : MainUtil.joinWithinLimit(prefix, parts, "\n\n")) {
                sends.add(sendMessageToUser(user, message));
            }
        }
        String groupIds = updated.stream().map(group -> group.getGroupId().toString()).collect(Collectors.joining(", "));
        reportWhenSent(sends.toArray(new CompletableFuture[0]), "з графіками груп " + groupIds, time);
    }

    // sends are async, so report only once they have actually finished
    private static void reportWhenSent(CompletableFuture<?>[] sends, String what, long startTime) {
        CompletableFuture.allOf(sends).whenComplete((ignored, e) -> {
            long failed = Arrays.stream(sends).filter(CompletableFuture::isCompletedExceptionally).count();
            sendToMe("Усі повідомлення \"" + sends.length + "\" надіслані " + what + " за " + (System.currentTimeMillis() - startTime) + " мілісекунд"
                    + (failed > 0 ? " (не доставлено: " + failed + ")" : ""));
        });
    }

    public static String prepareScheduleMessage(Group g, String newSchedule, String date, boolean newDay) {
        String[] args = newSchedule.split("");
        StringBuilder builder = new StringBuilder(newDay || g.getOldShutdowns() == null
                ? "Графік на <b>" + date + "</b>"
                : "Зміни в графіку на <b>" + date + "</b>").append(" в групі <u>")
                .append(g.getGroupId()).append("</u>\n\n");
        // don't write this back into g: prepareCompactScheduleMessage checks oldShutdowns == null for its header
        String old = g.getOldShutdowns() == null ? newSchedule : g.getOldShutdowns();
        for (int i = 0; i < args.length; i += 2) {
            builder.append(dates[i]).append(" - ").append(dates[i + 2])
                    .append(": ");
            // each line is one hour = two half-hour slots, so compare both
            if (newDay || !(args[i] + args[i + 1]).equals(old.substring(i, i + 2))) {
                if (args[i].equals(args[i + 1])) {
                    switch (args[i]) {
                        case "в":
                            builder.append("<u>❌❌Викл</u>\n");
                            break;
                        case "з":
                            builder.append("<b><u>\uD83D\uDCA1\uD83D\uDCA1Заживлено</u></b>\n");
                            break;
                        case "м":
                            builder.append("<u>❓❓Можливо заживлено</u>\n");
                            break;
                    }
                } else {
                    switch (args[i] + args[i + 1]) {
                        case "вз":
                            builder.append("<u>❌\uD83D\uDCA1Викл. | <b>заживлено</b></u>\n");
                            break;
                        case "вм":
                            builder.append("<u>❌\uD83D\uDCA1Викл. | можливо заживлено </u>\n");
                            break;
                        case "зв":
                            builder.append("<u>\uD83D\uDCA1❌<b>Заживлено</b> | відключено</u>\n");
                            break;
                        case "зм":
                            builder.append("<u>\uD83D\uDCA1❓<b>Заживлено</b> | можливо заживлено</u>\n");
                            break;
                        case "мз":
                            builder.append("<u>❓\uD83D\uDCA1Можливо заживлено | <b>заживлено</b> </u>\n");
                            break;
                        case "мв":
                            builder.append("<u>❓❌Можливо заживлено | відключено </u>\n");
                            break;
                    }
                }
            } else {
                if (args[i].equals(args[i + 1])) {
                    switch (args[i]) {
                        case "в":
                            builder.append("❌❌Викл\n");
                            break;
                        case "з":
                            builder.append("<b>\uD83D\uDCA1\uD83D\uDCA1Заживлено</b>\n");
                            break;
                        case "м":
                            builder.append("❓❓Можливо заживлено\n");
                            break;
                    }
                } else {
                    switch (args[i] + args[i + 1]) {
                        case "вз":
                            builder.append("❌\uD83D\uDCA1Викл. | <b>заживлено</b>\n");
                            break;
                        case "вм":
                            builder.append("❌\uD83D\uDCA1Викл. | можливо заживлено\n");
                            break;
                        case "зв":
                            builder.append("\uD83D\uDCA1❌<b>Заживлено</b> | відключено\n");
                            break;
                        case "зм":
                            builder.append("\uD83D\uDCA1❓<b>Заживлено</b> | можливо заживлено\n");
                            break;
                        case "мз":
                            builder.append("❓\uD83D\uDCA1Можливо заживлено | <b>заживлено</b>\n");
                            break;
                        case "мв":
                            builder.append("❓❌Можливо заживлено | відключено\n");
                            break;
                    }
                }
            }
        }
        return builder.toString();
    }

    public static String prepareCompactScheduleMessage(Group g, String newSchedule, String date, boolean newDay) {
        StringBuilder builder = new StringBuilder();

        // Якщо весь день світло — окреме повідомлення
        if (newSchedule.chars().allMatch(c -> c == 'з')) {
            return (newDay || g.getOldShutdowns() == null
                    ? "Графік на <b>" + date + "</b>"
                    : "Зміни в графіку на <b>" + date + "</b>")
                    + " в групі <u>" + g.getGroupId() + "</u>\n"
                    + "💡 Світло буде <b>весь день</b>";
        }
        builder.append(newDay || g.getOldShutdowns() == null
                ? "Графік на <b>" + date + "</b>"
                : "Зміни в графіку на <b>" + date + "</b>")
                .append(" в групі <u>")
                .append(g.getGroupId()).append("</u>\n");

        char prev = newSchedule.charAt(0);
        int startIndex = 0;

        for (int i = 1; i < newSchedule.length(); i++) {
            char current = newSchedule.charAt(i);
            if (current != prev) {
                builder.append(dates[startIndex])
                        .append("–")
                        .append(dates[i])
                        .append(" ")
                        .append(statusToText(prev))
                        .append("\n");
                startIndex = i;
                prev = current;
            }
        }

        builder.append(dates[startIndex])
                .append("–")
                .append(dates[48])
                .append(" ")
                .append(statusToText(prev))
                .append("\n");

        return builder.toString();
    }

    private static String statusToText(char c) {
        switch (c) {
            case 'в': return "❌ Відключено";
            case 'з': return "\uD83D\uDCA1 Заживлено";
            case 'м': return "❓ Можливо заживлено";
            default: return "Невідомо";
        }
    }

    public static CompletableFuture<Message> sendMessageToUser(User user, String message) {
        return messageService.sendBulkMessage(user.getUserId(), message).whenComplete((result, e) -> {
            if (e != null && MainUtil.isBlockedByUser(e)) {
                unsubscribeBlockedUser(user.getUserId());
            }
        });
    }

    private static void unsubscribeBlockedUser(Long userId) {
        // reload instead of saving the caller's copy, which may be stale (e.g. compact mode toggled since)
        userRepo.findById(userId).filter(User::getSubscribed).ifPresent(user -> {
            user.setSubscribed(false);
            userRepo.save(user);
            sendToMe(getUserLink(user) + " - кинув бота в чс((");
        });
    }

    public static String getUserLink(User user) {
        return MainUtil.makeLink(user.getUserId(), user.getFirstname())
                + ((user.getUsername() == null) ? "" : " - @" + user.getUsername());
    }

    // every notification for the admin log chat goes through here: it is batched, see LogChat
    public static void sendToMe(String s) {
        LogChat.send(s);
    }
}
