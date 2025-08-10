package com.alphabetas.bot.repo;

import com.alphabetas.bot.model.RoleplayCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.management.relation.Role;
import java.util.List;
import java.util.Set;

@Repository
public interface RoleplayRepo extends JpaRepository<RoleplayCommand, Long> {

    RoleplayCommand getByPhrase(String phrase);

    List<RoleplayCommand> getAllByAdultOnlyIsFalse();

}
