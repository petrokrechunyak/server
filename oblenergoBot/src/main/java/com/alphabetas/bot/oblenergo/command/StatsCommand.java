package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StatsCommand implements Command{

    private MessageService service;
    private UserRepo userRepo;

    private static Long MY_ID = 731921794L;

    public StatsCommand(MessageService service, UserRepo userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(Update update) {
        if(update.getMessage().getFrom().getId().equals(MY_ID)) {
            StringBuilder builder = new StringBuilder("Підписано користувачів: ")
                    .append(userRepo.getAllBySubscribedTrue().size())
                    .append("\nВідписано користувачів: ")
                    .append(userRepo.getAllBySubscribedFalse().size())
                    .append("\nОтримують графіки: ")
                    .append(userRepo.getAllBySubscribedTrueAndGroupsNotEmpty().size());
            SendMessage message = new SendMessage(MY_ID.toString(), builder.toString());
            service.sendMessage(message);
        }
    }
}
