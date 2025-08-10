package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.repo.GroupRepo;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class RefreshCommand implements Command {

    private MessageService service;
    private UserRepo userRepo;
    private GroupRepo groupRepo;

    public RefreshCommand(MessageService service, UserRepo userRepo, GroupRepo groupRepo) {
        this.service = service;
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
    }

    @Override
    public void execute(Update update) {
        ScheduleUtil.checkForSchedule();

    }
}
