package com.alphabetas.bot.repo;

import com.alphabetas.bot.model.PremiumChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PremiumChatRepo extends JpaRepository<PremiumChat, Long> {


}
