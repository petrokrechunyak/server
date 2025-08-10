package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.model.User;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StopCommand implements Command{

    private MessageService service;
    private UserRepo userRepo;

    public StopCommand (MessageService service, UserRepo userRepo)
    {
        this.service = service;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        User u;
        try {
            u = userRepo.findById(userId).get();
            u.setSubscribed(false);
            userRepo.save(u);
        } catch (Exception e) {
            String username = update.getMessage().getFrom().getUserName();
            String firstname = update.getMessage().getFrom().getFirstName();
            u = new User(userId, username, firstname, false);
            userRepo.save(u);
        }
        service.sendMessage(u.getUserId(),
                "Вас успішно відписано від графіків відключення!\n" +
                        "Для відновлення підписки напишіть /start");
        service.sendMessage(-4592105386L, ScheduleUtil.getUserLink(u)
                + " - відписався(");
    }
}
