package com.alphabetas.bot.marriage.repo;

import com.alphabetas.bot.marriage.model.MarriageModel;
import com.alphabetas.bot.model.CallerChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarriageRepo extends JpaRepository<MarriageModel, Long> {

    MarriageModel getByUser1IdAndChat(Long user1, CallerChat chat);

    MarriageModel getByUser2IdAndChat(Long user2, CallerChat chat);

    List<MarriageModel> findAllByChat(CallerChat chat);
}
