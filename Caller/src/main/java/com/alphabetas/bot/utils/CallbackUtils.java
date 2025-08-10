package com.alphabetas.bot.utils;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.enums.ConfigStateEnum;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.alphabetas.bot.utils.CommandUtils.messageService;

public class CallbackUtils {

    public static String buildCallbackText(Object... args) {
        StringBuilder builder = new StringBuilder(args[0].toString());
        for(int i = 1; i < args.length; i++) {
            builder.append(".").append(args[i]);
        }
        return builder.toString();
    }

    public static InlineKeyboardButton buttonByTextAndCallBack(String text, String callback) {
        return InlineKeyboardButton.builder()
                .callbackData(callback)
                .text(text)
                .build();
    }

    public static InlineKeyboardMarkup createMarkupByButton(InlineKeyboardButton... button) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> allRows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < button.length; i++) {
            if (i % 2 == 0) {
                row = new ArrayList<>();
                allRows.add(row);
                row.add(button[i]);
            } else {
                row.add(button[i]);
            }
        }

        markup.setKeyboard(allRows);
        return markup;
    }

    public static void edit(String text, InlineKeyboardMarkup markup, Update update, CallerChat chat) {
        if (update.getCallbackQuery().getMessage().getText().equals(text))
            return;
        EditMessageText editMessage = new EditMessageText(text);
        editMessage.setChatId(chat.getId());
        editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessage.setReplyMarkup(markup);
        editMessage.enableHtml(true);
        messageService.editMessage(editMessage);
    }

    public static ConfigStateEnum getByData(String data) {
        String[] arr = data.split("\\.");
        return ConfigStateEnum.valueOf(arr[2]);
    }

}
