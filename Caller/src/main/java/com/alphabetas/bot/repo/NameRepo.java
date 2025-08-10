package com.alphabetas.bot.repo;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.Name;
import com.alphabetas.bot.model.CallerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface NameRepo extends JpaRepository<Name, Long> {
    Name getByChatAndNameIgnoreCase(CallerChat chat, String name);

    Set<Name> getAllByChat(CallerChat chat);

    boolean deleteAllByCallerUser(CallerUser callerUser);

    Set<Name> getAllByCallerUser(CallerUser user);

}
