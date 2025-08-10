package com.alphabetas.bot.callback;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.service.ChatService;
import com.alphabetas.bot.service.MessageService;
import com.alphabetas.bot.service.UserService;
import com.alphabetas.bot.utils.CallbackUtils;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.HashMap;
import java.util.Map;

@Setter
public abstract class CallBack {

    public static final Map<String, CallBack> callbackMap = new HashMap<>();

    @Autowired
    protected UserService userService;

    @Autowired
    protected ChatService chatService;

    protected CallerChat chat;
    protected CallerUser user;
    protected Update update;
    protected Integer messageId;
    public MessageService messageService;
    public static final Long TEST_CHAT_ID = -1001907509347L;
    protected String data;

    public abstract void execute(Update update);

    public CallBack() {
        callbackMap.put(getSpecialArg(), this);
    }

    public abstract String getSpecialArg();

    public void updateCommand(Update update) {
        CallerChat chat = chatService.getById(update.getCallbackQuery().getMessage().getChatId(), update);
        CallerUser user = userService.getByUserIdAndCallerChat(update.getCallbackQuery().getFrom().getId(), chat);


        this.chat = chat;
        this.user = user;
        this.data = update.getCallbackQuery().getData();
        this.update = update;

    }

    public void edit(String text, InlineKeyboardMarkup markup) {
        CallbackUtils.edit(text, markup, update, chat);
    }
}
