package com.alphabetas.bot.oblenergo.repo;

import com.alphabetas.bot.oblenergo.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepo extends JpaRepository<Group, Integer> {
}
