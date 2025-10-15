package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.repo.CurrentDateRepo;
import com.alphabetas.bot.oblenergo.repo.GroupRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;

import java.util.HashMap;
import java.util.Map;

public class CommandContainer {
    private final Map<String, Command> commands;
    private final MessageService messageService;

    // All Commands
    Command start, stop, unknown, stats, say, refresh, support;

    public CommandContainer(MessageService messageService, UserRepo userRepo, GroupRepo groupRepo, CurrentDateRepo currentDateRepo) {
        this.commands = new HashMap<>();
        this.messageService = messageService;

        start = new StartCommand(messageService, userRepo);
        stop = new StopCommand(messageService, userRepo);
        stats = new StatsCommand(messageService, userRepo);
        say = new SayCommand(messageService, userRepo);
        refresh = new RefreshCommand(messageService, userRepo, groupRepo);
        unknown = new UnknownCommand();
        support = new SupportCommand(messageService);

        new ScheduleUtil(messageService, userRepo, groupRepo,  currentDateRepo);

        commands.put("/start", start);
        commands.put("/stop", stop);
        commands.put("/stats", stats);
        commands.put("/say", say);
        commands.put("/refresh", refresh);
        commands.put("/support", support);
    }

    public Command retrieveCommand(String command){
        return commands.getOrDefault(command.split("[ @]")[0].toLowerCase(), unknown);
    }

}
