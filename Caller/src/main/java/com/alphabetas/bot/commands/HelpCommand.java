package com.alphabetas.bot.commands;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;

@Slf4j
@Component
@Scope("prototype")
public class HelpCommand extends Command {

    public static final String S = "Ось довідник по тому, як правильно користуватися Кликуном: ";

    @Value("${bot.caller.helpLink}")
    private String helpUrl;

    @Override
    public int execute(Update update) {
        messageService.sendMessage(String.format(S + "%s", helpUrl));
        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/help"};
    }
}
