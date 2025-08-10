package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.model.User;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class SayCommand implements Command{

    private MessageService service;
    private UserRepo userRepo;

    public SayCommand(MessageService messageService, UserRepo userRepo) {
        this.service = messageService;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(Update update) {
        List<User> removeList = new ArrayList<>();
        String msgText = update.getMessage().getText().replace("/say", "");
        try {
            if(update.getMessage().getFrom().getId().equals(731921794L)) {
                for(User u: userRepo.getAllBySubscribedTrue()) {
                    try {
                        service.sendMessage(u.getUserId(), msgText);
                    } catch (Exception e) {
                        if(e.getMessage().contains("[403]")) {
                            if(u.getSubscribed()) {
                                u.setSubscribed(false);
                                removeList.add(u);
                                service.sendMessage(731921794L, ScheduleUtil.getUserLink(u) + " - кинув бота в чс((");
                            }
                        } else {
                            e.printStackTrace();
                            service.sendMessage(731921794L, e.getMessage());
                        }
                    }
                }
                userRepo.saveAll(removeList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            userRepo.saveAll(removeList);
        }

    }
}
