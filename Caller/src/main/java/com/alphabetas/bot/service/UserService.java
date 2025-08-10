package com.alphabetas.bot.service;

import com.alphabetas.bot.CallerBot;
import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface UserService extends AbstractService<CallerUser> {

    CallerUser getByUserIdAndCallerChat(Long userId, CallerChat callerChat);

    void removeByCallerChat(CallerChat chat);

    void removeByUserIdAndCallerChat(Long userId, CallerChat chat);

    List<CallerUser> getAllByUserId(Long userId);

    CallerUser getByUsernameAndCallerChat(String username, CallerChat chat);

}
