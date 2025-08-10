package com.alphabetas.bot.oblenergo.repo;

import com.alphabetas.bot.oblenergo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    List<User> getAllBySubscribedTrue();

    List<User> getAllBySubscribedTrueAndGroupsNotEmpty();

    List<User> getAllBySubscribedFalse();
}
