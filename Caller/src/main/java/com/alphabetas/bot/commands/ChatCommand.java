package com.alphabetas.bot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;

@Slf4j
@Component
@Scope("prototype")
public class ChatCommand extends Command {
    @Override
    public int execute(Update update) {
        // TODO chat info

        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/info", "/chat", "чат", "група"};
    }
}
