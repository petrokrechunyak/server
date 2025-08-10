package com.alphabetas.bot.repo;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.GroupName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupNameRepo extends JpaRepository<GroupName, Long> {

    GroupName findByNameIgnoreCaseAndChat(String name, CallerChat chat);

}
