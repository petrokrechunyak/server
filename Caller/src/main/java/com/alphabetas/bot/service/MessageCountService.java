package com.alphabetas.bot.service;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.MessageCount;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface MessageCountService extends AbstractService<MessageCount> {

    List<MessageCount> getAllByCallerUser(CallerUser user);

    MessageCount getByUserIdAndStartTime(Long userId, Long chatId, Long startTime, Update update);

    List<MessageCount> getAllByChat(CallerChat chat);

    List<MessageCount> getAll();

    void deleteAll(List<MessageCount> list);

}
