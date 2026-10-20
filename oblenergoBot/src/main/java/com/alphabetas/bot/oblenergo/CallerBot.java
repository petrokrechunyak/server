package com.alphabetas.bot.oblenergo;

import com.alphabetas.bot.oblenergo.repo.CurrentDateRepo;
import com.alphabetas.bot.oblenergo.repo.GroupRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.service.impl.MessageServiceImpl;
import com.alphabetas.bot.oblenergo.service.impl.TelegramSender;
import com.alphabetas.bot.oblenergo.command.CommandContainer;
import com.alphabetas.bot.oblenergo.model.Group;
import com.alphabetas.bot.oblenergo.model.User;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.utils.MainUtil;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class CallerBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private CommandContainer container;

    // initialized before the constructor body, which hands this bot to MessageServiceImpl
    private final TelegramSender sender = new TelegramSender(this);

    private MessageService messageService;

    private UserRepo userRepo;
    private GroupRepo groupRepo;
    private CurrentDateRepo currentDateRepo;

    public static final int GROUPS_NUMBER = 12;

    public CallerBot(UserRepo userRepo, GroupRepo groupRepo, CurrentDateRepo currentDateRepo) throws Exception {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.currentDateRepo = currentDateRepo;
        this.messageService = new MessageServiceImpl(this);
        this.container = new CommandContainer(messageService, userRepo, groupRepo, currentDateRepo);

        prepareUser(userRepo);
        prepareGroups();
    }

    private void prepareGroups() {
        if(groupRepo.findAll().isEmpty()) {
            for(int i = 1; i <= GROUPS_NUMBER; i++) {
                groupRepo.save(new Group(i, "з".repeat(48)));
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()
                && update.getMessage().getChat().getType().equals("private")) {
            String message = update.getMessage().getText();
            container.retrieveCommand(message).execute(update);
        } else if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            answerCallback(update.getCallbackQuery().getId());

            org.telegram.telegrambots.meta.api.objects.User from = update.getCallbackQuery().getFrom();
            // the keyboard can outlive the user's DB row (e.g. after a DB reset), so register them like /start does
            User user = userRepo.findById(from.getId())
                    .orElseGet(() -> userRepo.save(new User(from.getId(), from.getUserName(), from.getFirstName(), true)));

            String data = update.getCallbackQuery().getData();
            Long userId = user.getUserId();

            if ("TOGGLE_MODE".equals(data)) {
                user.setCompact(!user.getCompact());
                userRepo.save(user);

                EditMessageText editMessage = new EditMessageText("Режим повідомлень змінено на: " +
                        (user.getCompact() ? "Компактний" : "Детальний") + "\n\n" +
                        MainUtil.GROUP_SELECTION_TEXT);
                editMessage.setChatId(userId);
                editMessage.enableHtml(true);
                editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                editMessage.setReplyMarkup(MainUtil.prepareKeyboard(user));

                messageService.sendMessage(editMessage);
                return;
            }

            Group group = groupRepo.findById(Integer.parseInt(update.getCallbackQuery().getData())).get();
            List<Group> groupList = user.getGroups();

            if(groupList.contains(group)) {
                groupList.remove(group);
                group.getUsers().remove(user);
            } else {
                if (ScheduleUtil.isEmergency()) {
                    // schedules don't apply during emergency outages, so the stored one would mislead
                    messageService.sendMessage(user.getUserId(), ScheduleUtil.EMERGENCY_TEXT);
                } else {
                    String longMessage = ScheduleUtil.prepareScheduleMessage(group, group.getShutdowns(), ScheduleUtil.getCurrentDate(), false);
                    String compactMessage = ScheduleUtil.prepareCompactScheduleMessage(group, group.getShutdowns(), ScheduleUtil.getCurrentDate(), false);
                    messageService.sendMessage(user.getUserId(), user.getCompact().equals(Boolean.TRUE) ? compactMessage : longMessage);
                }

                groupList.add(group);
                group.getUsers().add(user);
                ScheduleUtil.sendToMe(ScheduleUtil.getUserLink(user)
                        + " підписався на групу " + group.getGroupId());
            }
            groupRepo.save(group);
            userRepo.save(user);
            EditMessageText editMessage = new EditMessageText(MainUtil.GROUP_SELECTION_TEXT);

            editMessage.setChatId(user.getUserId());
            editMessage.enableHtml(true);
            editMessage.setReplyMarkup(MainUtil.prepareKeyboard(user));
            editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

            messageService.sendMessage(editMessage);
        }

    }

    private void answerCallback(String callbackQueryId) {
        try {
            // stops the loading spinner on the pressed button; failing here is harmless
            executeAsync(new AnswerCallbackQuery(callbackQueryId));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    void prepareUser(UserRepo userRepo) {
        try {
            User u = userRepo.findAll().get(0);
        } catch (Exception e) {
            User user = new User(-1L, "stub", "stub", false);
            userRepo.save(user);
        }

    }

    public TelegramSender getSender() {
        return sender;
    }

    @Bean
    public void fillData(){
        // one UPDATE instead of loading and merging every user on each startup
        userRepo.setCompactFalseWhereNull();
        // users from before priorities existed: the admin first, everyone else the default
        userRepo.setPriorityWhereNull(MainUtil.ADMIN_ID, 0);
        userRepo.setPriorityWhereNull(User.DEFAULT_PRIORITY);
    }

    @Override
    public void onRegister() {

    }
}
