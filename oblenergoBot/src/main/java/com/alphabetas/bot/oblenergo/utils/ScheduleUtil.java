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
import java.util.List;

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

    public ScheduleUtil(MessageService messageService, UserRepo userRepo, GroupRepo groupRepo, CurrentDateRepo currentDateRepo) {
        ScheduleUtil.messageService = messageService;
        ScheduleUtil.userRepo = userRepo;
        ScheduleUtil.groupRepo = groupRepo;
        ScheduleUtil.currentDateRepo = currentDateRepo;
    }

    public static void checkForSchedule() {
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

        String newDate = doc.select("#gsv > ul > p").eachText().get(0);
        if(currentDateRepo.findAll().isEmpty()) {
            currentDateRepo.save(new CurrentDate(newDate));
        }

        updateAndSendSchedule(newDate, doc);

        log.info("Checking for new schedule ended");
    }

    private static String getCurrentDate() {
        return currentDateRepo.findAll().get(0).getDate();
    }

    private static void updateAndSendSchedule(String newDate, Document doc) {
        boolean newDay = !newDate.equals(getCurrentDate());
        List<Group> updated = new ArrayList<>();

        for (int i = 1; i <= CallerBot.GROUPS_NUMBER; i++) {
            String el = doc.select("div#inf" + i).eachText().get(0).replaceAll(" ", "").replace("мз", "м");
            Group group = groupRepo.findById(i).get();
            if (newDay || !el.equals(group.getShutdowns())) {
                log.info("Schedule updated in group {}", group.getGroupId());
                group.setOldShutdowns(group.getShutdowns());
                group.setShutdowns(el);
                groupRepo.save(group);
                updated.add(group);
            }
        }


        if (newDay) {
            CurrentDate currentDate = currentDateRepo.findAll().get(0);
            currentDate.setDate(newDate);
            currentDateRepo.save(currentDate);
        }

        log.info("Schedule updated in number of groups: {}", updated.size());
        updated.parallelStream().forEach(group -> {
            long time = System.currentTimeMillis();

            String longMessage = prepareScheduleMessage(group, group.getShutdowns(), newDate, newDay);
            String compactMessage = prepareCompactScheduleMessage(group, group.getShutdowns(), newDate, newDay);


            group.getUsers().parallelStream().filter(User::getSubscribed).forEach((user) -> {
                sendMessageToUser(user, user.getCompact().equals(Boolean.TRUE) ? compactMessage : longMessage);
            });

            userRepo.saveAll(group.getUsers());

            sendToMe("Усі повідомлення \"" + group.getUsers().size() + "\" надіслані для групи " + group.getGroupId() + " за " + (System.currentTimeMillis() - time) + " мілісекунд");

            log.info("Schedule to group " + group.getGroupId() + " sent");
        });
    }

    public static String prepareScheduleMessage(Group g, String newSchedule, String date, boolean newDay) {
        String[] args = newSchedule.split("");
        StringBuilder builder = new StringBuilder(newDay || g.getOldShutdowns() == null
                ? "Графік на <b>" + date + "</b>"
                : "Зміни в графіку на <b>" + date + "</b>").append(" в групі <u>")
                .append(g.getGroupId()).append("</u>\n\n");
        if (g.getOldShutdowns() == null) {
            g.setOldShutdowns(g.getShutdowns());
        }
        for (int i = 0; i < args.length; i += 2) {
            builder.append(dates[i]).append(" - ").append(dates[i + 2])
                    .append(": ");
            if (newDay || !args[i].equals(Character.toString(g.getOldShutdowns().charAt(i)))) {
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
                    + " в групі <u>" + g.getGroupId() + "</u>\n\n"
                    + "💡 Світло буде <b>весь день</b>";
        }
        builder.append(newDay || g.getOldShutdowns() == null
                ? "Графік на <b>" + date + "</b>"
                : "Зміни в графіку на <b>" + date + "</b>")
                .append(" в групі <u>")
                .append(g.getGroupId()).append("</u>\n\n");

        if (g.getOldShutdowns() == null) {
            g.setOldShutdowns(g.getShutdowns());
        }

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

    private static void sendMessageToUser(User user, String message) {
        messageService.sendMessage(user.getUserId(), message).thenAccept(result -> {
            if (result.getText().contains("[403]")) {
                if (user.getSubscribed()) {
                    user.setSubscribed(false);
                    messageService.sendMessage(-4592105386L, getUserLink(user) + " - кинув бота в чс((");
                }
            }
        });
    }

    public static String getUserLink(User user) {
        return MainUtil.makeLink(user.getUserId(), user.getFirstname())
                + ((user.getUsername() == null) ? "" : " - @" + user.getUsername());
    }

    public static void sendToMe(String s) {
        messageService.sendMessage(-4592105386L, s);
        MainUtil.sleep(1L);
    }
}
