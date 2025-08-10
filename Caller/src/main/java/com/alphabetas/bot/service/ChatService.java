package com.alphabetas.bot.service;

import com.alphabetas.bot.model.CallerChat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface ChatService extends AbstractService<CallerChat> {

    CallerChat getByUpdate(Update update);

    List<CallerChat> findAll();

    CallerChat getById(Long id, Update update);

    void incrementMessages(CallerChat chat);


}
