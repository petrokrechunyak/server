package com.alphabetas.bot.repo;

import com.alphabetas.bot.model.CallerChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepo extends JpaRepository<CallerChat, Long> {

}
