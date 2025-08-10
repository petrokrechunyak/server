package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.enums.CallerRoles;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.alphabetas.bot.callback.ChatConfigCallback.createCallbackMessage;
import static com.alphabetas.bot.model.enums.ConfigStateEnum.*;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.NOT_ENOUGH_RIGHTS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.utils.CallbackUtils.buttonByTextAndCallBack;
import static com.alphabetas.bot.utils.CallbackUtils.createMarkupByButton;

@Component
@Scope("prototype")
@Slf4j
public class ConfigCommand extends Command {

    public static final int DEFAULT_LIMIT = 8;
    public static final String MAIN_MENU_TEXT = "Налаштування для групи <b>%s</b>\n";

    public static SendMessage mainMenuMessage(CallerChat chat) {
        String msg = String.format(MAIN_MENU_TEXT,
                CommandUtils.deleteBadSymbols(chat.getTitle()));
        SendMessage sendMessage = new SendMessage(chat.getId().toString(), msg);
        sendMessage.enableHtml(true);

        sendMessage.setReplyMarkup(mainMenu(chat));

        return sendMessage;
    }

    @Override
    public int execute(Update update) {
        if(user.getRole().getRoleNumber() > CallerRoles.LOWER_ADMIN.getRoleNumber()) {
            SendMessage message = mainMenuMessage(chat);
            message.setReplyToMessageId(this.message.getMessageId());
            messageService.sendMessage(message);
            return SUCCESS.getReturnCode();
        }

        return NOT_ENOUGH_RIGHTS.getReturnCode();
    }

    public static InlineKeyboardMarkup mainMenu(CallerChat chat) {
        return createMarkupByButton(
                //limit names
                buttonByTextAndCallBack("Ліміт імен", createCallbackMessage(chat, LIMIT_MENU.toString())),
                //rp commands
                buttonByTextAndCallBack("Рольові команди", createCallbackMessage(chat, RP_MENU.toString())),
                // aggression
                buttonByTextAndCallBack("Агресія", createCallbackMessage(chat, AGGRESSION_MENU.toString())),
                // allow spaces
                buttonByTextAndCallBack("Пробіли в іменах", createCallbackMessage(chat, ALLOW_SPACES.toString())),
                buttonByTextAndCallBack("18+", createCallbackMessage(chat, ALLOW_ADULT.toString())),
                close(chat)
        );
    }

    public static InlineKeyboardButton close(CallerChat chat) {
        return buttonByTextAndCallBack("Закрити", createCallbackMessage(chat, CLOSE.toString()));
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/config", "конфіг"};
    }

}
