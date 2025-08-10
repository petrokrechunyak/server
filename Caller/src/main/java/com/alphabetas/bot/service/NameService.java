package com.alphabetas.bot.service;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.Name;
import com.alphabetas.bot.model.CallerUser;

import java.util.Set;

public interface NameService extends AbstractService<Name> {

    Name getByCallerChatAndName(CallerChat chat, String name);

    Set<Name> getAllByCallerChat(CallerChat chat);

    void deleteAllByCallerUser(CallerUser user);

    Set<Name> getAllByCallerUser(CallerUser user);


}
