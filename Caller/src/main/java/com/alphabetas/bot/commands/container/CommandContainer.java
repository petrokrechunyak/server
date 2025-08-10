package com.alphabetas.bot.commands.container;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.service.impl.MessageServiceImpl;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Setter
public class CommandContainer {
    public static final Map<String, Command> classMap = new HashMap<>();

    @Autowired
    private ApplicationContext context;

    @Value("${bot.caller.username}")
    private String botUsername;

    public CommandContainer() {
    }

    public Command retrieveCommand(Update update) {
        String msgText = update.getMessage().getText();
        Command command = null;
        if (msgText.equalsIgnoreCase("кликун")) {
            command = classMap.get("кликун");
        } else if (!CommandUtils.isCommand(msgText) || !isCallerCommand(msgText)) {
            command = classMap.get("NoCommand");
        } else {
            String temp = msgText.toLowerCase().replaceAll("[.!]|кликун ", "")
                    .trim().split("[ @]")[0];
            command = classMap.getOrDefault(temp, classMap.get("NoCommand"));
        }
        command = context.getBean(command.getClass());
        command.updateCommand(update);
        command.setMessageService(new MessageServiceImpl(update));
        log.info("Entering into {}", command.getClass().getSimpleName());
        return command;
    }

    private boolean isCallerCommand(String msgText) {
        String[] args = msgText.split("@");
        if(args.length == 1) {
            return true;
        }
        return args[1].equals(botUsername);
    }
}
