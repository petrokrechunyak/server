package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.CallerBot;
import com.alphabetas.bot.oblenergo.repo.GroupRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.service.impl.MessageServiceImpl;
import com.alphabetas.bot.oblenergo.model.Group;
import com.alphabetas.bot.oblenergo.model.User;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.utils.MainUtil;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class StartCommand implements Command {

    private MessageService service;
    private UserRepo userRepo;
    private GroupRepo groupRepo;

    public StartCommand(MessageService service, UserRepo userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    @Autowired
    public StartCommand(CallerBot bot, UserRepo userRepo, GroupRepo groupRepo) {
        service = new MessageServiceImpl(bot);
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
    }

    @Override
    public void execute(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        User u;
        boolean wasSubscribed = false;
        String username = update.getMessage().getFrom().getUserName();
        String firstname = update.getMessage().getFrom().getFirstName();
        try {
            u = userRepo.findById(userId).get();
            u.setSubscribed(true);
            u.setFirstname(firstname);
            u.setUsername(username);
            wasSubscribed = true;
        } catch (Exception e) {
            u = new User(userId, username, firstname, true);
        }
        userRepo.save(u);

        service.sendMessage(prepareMessage(u));
        service.sendMessage(-4592105386L, ScheduleUtil.getUserLink(u) + " -" + (wasSubscribed ? " знову" : " <u><b>вперше</b></u>") + " підписався)");
        }

    SendMessage prepareMessage(User user) {
        SendMessage sendMessage = new SendMessage(user.getUserId().toString(),
                "Виберіть групу по якій хочете отримувати графіки(можна вибрати декілька)!\n" +
                        "Для відписки напишіть /stop\n" +
                        "Буду вдячний вашій підтримці: /support\n\n" +
                        "Щоб знайти свою групу нажміть <u><a href='https://oblenergo.cv.ua/shutdowns-search/'>сюди</a></u>");

        sendMessage.setReplyMarkup(MainUtil.prepareKeyboard(user));
        sendMessage.enableHtml(true);

        return sendMessage;
    }

    @Scheduled(fixedDelay = 60000 * 3)
    public void go() {
        ScheduleUtil.checkForSchedule();
    }
}
