package com.alphabetas.bot.repo;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<CallerUser, Long> {
    CallerUser getByUserIdAndCallerChat(Long userId, CallerChat callerChat);

    @Transactional
    void removeByCallerChat(CallerChat chat);

    @Transactional
    void removeByUserIdAndCallerChat(Long userId, CallerChat chat);

    List<CallerUser> getAllByUserId(Long userId);

    CallerUser getByUsernameAndCallerChat(String username, CallerChat chat);
}
