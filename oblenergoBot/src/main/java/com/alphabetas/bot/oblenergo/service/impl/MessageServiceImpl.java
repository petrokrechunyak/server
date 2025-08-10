package com.alphabetas.bot.oblenergo.service.impl;

import com.alphabetas.bot.oblenergo.CallerBot;
import com.alphabetas.bot.oblenergo.service.MessageService;
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

import java.util.concurrent.CompletableFuture;

public class MessageServiceImpl implements MessageService {
    private CallerBot bot;

    public MessageServiceImpl(CallerBot bot) {
        this.bot = bot;
    }

    @Override
    public CompletableFuture<Message> sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendMessage.enableHtml(true);

        try {
            return bot.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            Message returnMessage = new Message();
            returnMessage.setText(e.getMessage());
            return CompletableFuture.completedFuture(returnMessage);
        }
    }

    @Override
    public void sendMessage(BotApiMethod sendMessage) {
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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
        DeleteMessage deleteMessage = new DeleteMessage(chatId, messageId);
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void editMessage(Long chatId, Long msgToUpdate, String text) {
        EditMessageText editMessage = new EditMessageText(text);
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(msgToUpdate.intValue());
        editMessage.enableHtml(true);
        try {
            bot.execute(editMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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
