package com.alphabetas.bot.commands;


import com.alphabetas.bot.commands.container.CommandContainer;
import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.service.ChatService;
import com.alphabetas.bot.service.MessageService;
import com.alphabetas.bot.service.UserService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;

@Setter
public abstract class Command {

    @Autowired
    protected UserService userService;

    @Autowired
    protected ChatService chatService;

    protected String msgText;
    protected CallerChat chat;
    protected CallerUser user;
    protected Message message;
    protected Message repliedMessage;
    protected CallerUser repliedUser;
    protected Integer threadId;
    protected Update update;
    public MessageService messageService;
    public static final Long TEST_CHAT_ID = -1001907509347L;

    public Command() {
        Arrays.stream(getSpecialArgs()).forEach(x -> CommandContainer.classMap.put(x, this));
    }

    public abstract int execute(Update update);

    public abstract String[] getSpecialArgs();

    @Override
    public String toString() {
        return "Command{" +
                ", msgText='" + msgText + '\'' +
                ", chat=" + chat +
                ", user=" + user +
                '}';
    }

    public void updateCommand(Update update) {
        CallerChat chat = chatService.getByUpdate(update);
        CallerUser user = userService.getByUserIdAndCallerChat(update.getMessage().getFrom().getId(), chat);
        Message repliedMessage = update.getMessage().getReplyToMessage();
        Message message = update.getMessage();
        String msgText = update.getMessage().getText();
        Integer threadId = update.getMessage().getIsTopicMessage() != null ? update.getMessage().getMessageThreadId() : null;

        this.msgText = msgText;
        this.chat = chat;
        this.user = user;
        this.repliedMessage = repliedMessage;
        this.message = message;
        this.threadId = threadId;
        this.update = update;

        if (repliedMessage != null) {
            if(repliedMessage.getFrom().getIsBot()) {
                User from = repliedMessage.getFrom();
                this.repliedUser = new CallerUser(from.getId(), from.getFirstName(), from.getUserName(), chat);
            } else {
                this.repliedUser = userService.getByUserIdAndCallerChat(repliedMessage.getFrom().getId(), chat);
            }
        }
    }
}

