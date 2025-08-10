package com.alphabetas.bot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.USE_NAME_WITH_COMMAND;

@Slf4j
@Component
@Scope("prototype")
public class IdeaCommand extends Command {

    @Override
    public int execute(Update update) {
        if (msgText.replace("/idea", "").isEmpty()) {
            messageService.sendMessage("Напишіть свою ідею в одному повідомленню з командою.");
            return USE_NAME_WITH_COMMAND.getReturnCode();
        }
        ForwardMessage forwardMessage = new ForwardMessage(Long.toString(messageService.MY_ID), Long.toString(chat.getId()), update.getMessage().getMessageId());
        messageService.execute(forwardMessage);
        messageService.sendMessage("Ваша ідея була відправлена розробнику. Дякую!");
        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/idea"};
    }
}
