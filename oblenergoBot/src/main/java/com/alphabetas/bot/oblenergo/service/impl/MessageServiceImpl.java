package com.alphabetas.bot.oblenergo.service.impl;

import com.alphabetas.bot.oblenergo.CallerBot;
import com.alphabetas.bot.oblenergo.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MessageServiceImpl implements MessageService {
    private CallerBot bot;
    private TelegramSender sender;

    public MessageServiceImpl(CallerBot bot) {
        this.bot = bot;
        // one sender per bot, so every MessageServiceImpl shares the same rate limit
        this.sender = bot.getSender();
    }

    @Override
    public CompletableFuture<Message> sendMessage(Long chatId, String message) {
        return send(prepareMessage(chatId, message), false);
    }

    @Override
    public CompletableFuture<Message> sendBulkMessage(Long chatId, String message) {
        return send(prepareMessage(chatId, message), true);
    }

    private SendMessage prepareMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sendMessage(BotApiMethod sendMessage) {
        send((BotApiMethod<Serializable>) sendMessage, false);
    }

    private <T extends Serializable> CompletableFuture<T> send(BotApiMethod<T> method, boolean bulk) {
        // failures (after retries) arrive here as a failed future, never as an exception from this call
        return sender.send(method, bulk).whenComplete((result, e) -> {
            if (e != null) {
                log.warn("{} to {} failed: {}", method.getMethod(), chatIdOf(method), e.getMessage());
            }
        });
    }

    private static String chatIdOf(BotApiMethod<?> method) {
        if (method instanceof SendMessage) {
            return ((SendMessage) method).getChatId();
        }
        if (method instanceof EditMessageText) {
            return ((EditMessageText) method).getChatId();
        }
        return "?";
    }

    @Override
    public File getFile(GetFile getFile) {
        try {
            return bot.execute(getFile);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message sendDocument(SendDocument sendDocument) {
        return null;
    }

    @Override
    public void deleteMessage(String chatId, Integer messageId) {
        send(new DeleteMessage(chatId, messageId), false);
    }

    @Override
    public void editMessage(Long chatId, Long msgToUpdate, String text) {
        EditMessageText editMessage = new EditMessageText(text);
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(msgToUpdate.intValue());
        editMessage.enableHtml(true);
        send(editMessage, false);
    }

    @Override
    public void sendPhoto(SendPhoto photo) {
        try {
            bot.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.out.println(e.getMessage().contains("403"));
            throw new RuntimeException(e);
        }
    }
}
