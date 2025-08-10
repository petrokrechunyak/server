package com.alphabetas.bot.repo;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.MessageCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageCountRepo extends JpaRepository<MessageCount, Long> {

    List<MessageCount> getAllByCallerUser(CallerUser user);

    MessageCount getByCallerUserAndStartTime(CallerUser user, Long startTime);

    List<MessageCount> getAllByChat(CallerChat chat);

}
