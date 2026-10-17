package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.model.User;
import com.alphabetas.bot.oblenergo.repo.UserRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.utils.MainUtil;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

/**
 * Admin only. "/priority &lt;id&gt;" shows a user's priority, "/priority &lt;id&gt; &lt;priority&gt;" changes it.
 */
public class PriorityCommand implements Command {

    private static final String USAGE = "Використання: /priority &lt;id&gt; &lt;пріоритет&gt;\n" +
            "Менше число отримує графіки раніше. За замовчуванням " + User.DEFAULT_PRIORITY + ".\n" +
            "/priority &lt;id&gt; покаже поточний пріоритет.";

    private MessageService service;
    private UserRepo userRepo;

    public PriorityCommand(MessageService service, UserRepo userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    @Override
    public void execute(Update update) {
        if(!update.getMessage().getFrom().getId().equals(MainUtil.ADMIN_ID)) {
            return;
        }
        Long chatId = update.getMessage().getChatId();
        String[] args = update.getMessage().getText().trim().split("\\s+");
        if(args.length < 2 || args.length > 3) {
            service.sendMessage(chatId, USAGE);
            return;
        }

        long userId;
        Integer priority = null;
        try {
            userId = Long.parseLong(args[1]);
            if(args.length == 3) {
                priority = Integer.parseInt(args[2]);
            }
        } catch (NumberFormatException e) {
            service.sendMessage(chatId, USAGE);
            return;
        }

        Optional<User> found = userRepo.findById(userId);
        if(found.isEmpty()) {
            service.sendMessage(chatId, "Користувача з id " + userId + " не знайдено");
            return;
        }
        User user = found.get();
        if(priority == null) {
            service.sendMessage(chatId, "Пріоритет " + ScheduleUtil.getUserLink(user) + ": " + user.getPriority());
            return;
        }

        Integer old = user.getPriority();
        user.setPriority(priority);
        userRepo.save(user);
        service.sendMessage(chatId, "Пріоритет " + ScheduleUtil.getUserLink(user) + " змінено: " + old + " → " + priority);
    }
}
