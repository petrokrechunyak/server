package com.alphabetas.bot.commands;


import com.alphabetas.bot.utils.CallbackUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.generic.RET;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.UNKNOWN_COMMAND;

@Slf4j
@Component
@Scope("prototype")
public class StartCommand extends Command {

    @Value("${bot.caller.helpLink}")
    private String helpUrl;
    String s = "\uD83D\uDC4B Привіт, я - бот Кликун, створений для заклику людей по імені\n" +
            "\n" +
            "Ось довідник по тому, як правильно користуватися Кликуном: %s";

    @Override
    public int execute(Update update) {
        if(!update.getMessage().getChat().getType().equals("private")) {
            return UNKNOWN_COMMAND.getReturnCode();
        }
        SendMessage message = new SendMessage(chat.getId().toString(), String.format(s, helpUrl));
        message.setReplyMarkup(CallbackUtils.createMarkupByButton(
                makeButton("\uD83D\uDCDD Довідка", helpUrl),
                makeButton("\uD83D\uDCE2 Хатина Кликуна", "https://t.me/callerHut"),
                makeButton("➕ Додати бота", "https://t.me/caller_ua_bot?startgroup=groupadded")
        ));
        messageService.sendMessage(message);
        return SUCCESS.getReturnCode();
    }

    public InlineKeyboardButton makeButton(String text, String link) {
        return InlineKeyboardButton.builder()
                .text(text)
                .url(link)
                .build();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[] {
                "/start"
        };
    }
}
