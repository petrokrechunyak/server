package com.alphabetas.bot.oblenergo.service;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.concurrent.CompletableFuture;

/**
 * Sends, edits and deletes messages by different ways
 */
public interface MessageService {


    /**
     * @param chatId chat, in which message need to be sent
     * @param message message text that need to be sent
     * @return {@link Message} that has been already sent
     * <br>
     * {@link SendMessage} object creates automatically, with html parsing - true
     *
     */
    CompletableFuture<Message> sendMessage(Long chatId, String message);

    /**
     * @param sendMessage {@link SendMessage} object that need to be sent
     * @return {@link Message} that has been already sent
     */
    void sendMessage(BotApiMethod sendMessage);

    /**
     * @param getFile contains basic information about file that need to be getted
     * @return downloaded file
     */
    File getFile(GetFile getFile);

    /**
     * @param sendDocument {@link SendDocument} object, document that need to be sent
     * @return {@link Message} that has been already sent
     */
    Message sendDocument(SendDocument sendDocument);

    /**
     * @param chatId chat, in which messages need to be deleted
     * @param messageId id of message, that need to be deleted
     */
    void deleteMessage(String chatId, Integer messageId);

    /**
     * @param chatId chat, in which messages need to be edited
     * @param msgToUpdate id of message, that need to be edited
     * @param text new text of edited message
     */
    void editMessage(Long chatId, Long msgToUpdate, String text);

    void sendPhoto(SendPhoto photo);

}
