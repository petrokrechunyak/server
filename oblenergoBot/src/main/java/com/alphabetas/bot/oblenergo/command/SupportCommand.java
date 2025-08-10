package com.alphabetas.bot.oblenergo.command;

import com.alphabetas.bot.oblenergo.service.MessageService;
import com.alphabetas.bot.oblenergo.utils.ScheduleUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SupportCommand implements Command {

    String text = "Бот працює завдяки вашій допомозі. Підтримати проєкт можна за посиланням: <a href='https://send.monobank.ua/jar/3xdNobZdy8'>https://send.monobank.ua/jar/3xdNobZdy8</a>";

    private MessageService service;

    public SupportCommand(MessageService service) {
        this.service = service;
    }

    @Override
    public void execute(Update update) {
        service.sendMessage(update.getMessage().getChatId(), text);
        ScheduleUtil.sendToMe("<b><u><i>Хтось нажав саппорт</i></u></b>");
    }

}
