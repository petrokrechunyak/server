package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.CallerBot;
import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.service.MessageService;
import com.alphabetas.bot.utils.CommandUtils;
import com.alphabetas.bot.utils.SpaceUtils;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageId;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Method;
import java.util.List;

import static com.alphabetas.bot.utils.CommandUtils.messageService;

@NoArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    public static CallerBot bot;

    private Long chatId;
    private Update update;
    private Message replyToMessage;


    public static void setBot(CallerBot bot) {
        MessageServiceImpl.bot = bot;
    }

    public void execute (BotApiMethodMessage message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(BotApiMethod<MessageId> message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public MessageServiceImpl(Update update) {
        if(update.hasCallbackQuery()) {
            update.setMessage(update.getCallbackQuery().getMessage());
        }
        this.update = update;
        this.chatId = update.getMessage().getChatId();
        this.replyToMessage = update.getMessage();
    }



    @Override
    public ChatMember getChatMember(Long userId) {
        return getChatMember(chatId, userId);
    }

    @Override
    public ChatMember getChatMember(Long chatId, Long userId) {
        GetChatMember getChatMember = new GetChatMember(chatId.toString(), userId);
        log.info("Getting chat member");
        try {
            return bot.execute(getChatMember);
        } catch (TelegramApiException e) {
//            messageService.sendErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message sendMessage(String message) {
        return sendMessage(chatId, message);
    }

    public Message sendMessage(Long chatId, String message, boolean withReply) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.enableHtml(true);
        if(withReply) {
            sendMessage.setReplyToMessageId(replyToMessage.getMessageId());
        }

        return sendMessage(sendMessage);
    }

    @Override
    public Message sendMessage(Long chatId, String message) {
        return sendMessage(chatId, message, true);

    }

    public Message sendMessageWithoutReply(String message) {
        return sendMessage(chatId, message, false);
    }

    @Override
    public Message sendMessage(SendMessage sendMessage) {
        sendMessage.enableHtml(true);
        log.info("Sending message...");
        try {
            if(update != null) {
                SpaceUtils.writeFromCaller(update, sendMessage.getText());
            }
            return (Message) bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public File getFile(GetFile getFile) {
        try {
            return bot.execute(getFile);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message sendDocument(SendDocument sendDocument) {
        try {
            return bot.execute(sendDocument);
        } catch (TelegramApiException e) { messageService.sendErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMessage(Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());

        }
    }

    @Override
    public void editMessage(Long msgToUpdate, String text) {
        EditMessageText editMessage = new EditMessageText(text);
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(msgToUpdate.intValue());
        editMessage(editMessage);
    }

    @Override
    public void editMessage(EditMessageText editMessage) {
        editMessage.enableHtml(true);
        try {
            bot.execute(editMessage);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ChatMember> getAdmins() {
        return getAdmins(chatId);
    }

    @Override
    public List<ChatMember> getAdmins(Long chatId) {
        GetChatAdministrators chatAdministrators = new GetChatAdministrators(chatId.toString());
        try {
            return bot.execute(chatAdministrators);
        } catch (TelegramApiException e) { messageService.sendErrorMessage(e.getMessage());
            return null; }
    }

    public void sendPhoto(SendPhoto photo) {
        try {
            bot.execute(photo);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
        }
    }

    @Override
    public void sendVideo(SendVideo video) {
        try {
            bot.execute(video);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
        }
    }

    @Override
    public int getChatMemberCount() {
        return getChatMemberCount(chatId);
    }

    @Override
    public int getChatMemberCount(Long chatId) {
        GetChatMemberCount count = new GetChatMemberCount(chatId.toString());
        try {
            return bot.execute(count);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
            return 0;
        }
    }

    @Override
    public void sendErrorMessage(Throwable e, Update u) {
        e.printStackTrace();
        String text = u.getMessage().getText();
        text = text + "\n\n" + ExceptionUtils.getStackTrace(e);
        text = CommandUtils.deleteBadSymbols(text);
        sendMessage(Command.TEST_CHAT_ID, text, false);
    }

    @Override
    public void sendErrorMessage(String stackTrace) {
        log.error(stackTrace);
        sendMessage(Command.TEST_CHAT_ID, stackTrace, false);
    }

    @Override
    public void sendAnswerCallback(AnswerCallbackQuery answerCallbackQuery) {
        try {
            bot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            messageService.sendErrorMessage(e.getMessage());
        }
    }

    @Override
    public void sendAnswerCallback(String text, Update update) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.getCallbackQuery().getId());
        answerCallbackQuery.setText(text);
        answerCallbackQuery.setShowAlert(true);
        messageService.sendAnswerCallback(answerCallbackQuery);
    }
}
