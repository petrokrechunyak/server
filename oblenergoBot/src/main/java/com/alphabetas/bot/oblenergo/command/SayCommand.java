package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.model.User;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.utils.MainUtil;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SayCommand implements Command{

    private MessageService service;
    private UserRepo userRepo;

    public SayCommand(MessageService messageService, UserRepo userRepo) {
        this.service = messageService;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(Update update) {
        if(!update.getMessage().getFrom().getId().equals(731921794L)) {
            return;
        }
        String msgText = update.getMessage().getText().replaceFirst("/say", "");
        // sends are async: blocked users are unsubscribed by sendMessageToUser, other errors are reported here
        for(User u: userRepo.getAllBySubscribedTrueOrderByPriorityAsc()) {
            ScheduleUtil.sendMessageToUser(u, msgText).whenComplete((result, e) -> {
                if(e != null && !MainUtil.isBlockedByUser(e)) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    service.sendMessage(731921794L, MainUtil.escapeHtml(cause.getMessage()));
                }
            });
        }
    }
}
