package com.alphabetas.bot.service;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageId;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.List;

public interface MessageService {

    public static final long MY_ID = 731921794L;


    void execute (BotApiMethodMessage message);

    void sendMessage(BotApiMethod<MessageId> message);
    /**
     *
     * @return chat member with given id in given chat
     */
    ChatMember getChatMember(Long userId);
    ChatMember getChatMember(Long chatId, Long userId);

    /**
     * @return {@link Message} that has been already sent
     * <br>
     * {@link SendMessage} object creates automatically, with html parsing - true
     */
    Message sendMessage(String message);

    Message sendMessage(Long chatId, String message);

    Message sendMessageWithoutReply(String message);

    Message sendMessage(Long chatId, String message, boolean withReply);

    /**
     * @param sendMessage {@link SendMessage} object that need to be sent
     * @return {@link Message} that has been already sent
     */
    Message sendMessage(SendMessage sendMessage);

    /**
     * @param getFile contains basic information about file that need to be gotten
     * @return downloaded file
     */
    File getFile(GetFile getFile);

    /**
     * @param sendDocument {@link SendDocument} object, document that need to be sent
     * @return {@link Message} that has been already sent
     */
    Message sendDocument(SendDocument sendDocument);

    /**
     * @param messageId id of message, that need to be deleted
     */
    void deleteMessage(Integer messageId);

    /**
     * @param msgToUpdate id of message, that need to be edited
     * @param text        new text of edited message
     */
    void editMessage(Long msgToUpdate, String text);

    void editMessage(EditMessageText editMessageText);

    /**
     * @return List of admins of that with given chatId
     */
    List<ChatMember> getAdmins();

    List<ChatMember> getAdmins(Long chatId);

    void sendPhoto(SendPhoto photo);

    void sendVideo(SendVideo video);

    /**
     * @return count of members in given chatId
     */
    int getChatMemberCount();

    int getChatMemberCount(Long chatId);


    void sendErrorMessage(Throwable e, Update u);

    void sendErrorMessage(String stackTrace);

    void sendAnswerCallback(AnswerCallbackQuery answerCallbackQuery);
    void sendAnswerCallback(String text, Update update);

}
