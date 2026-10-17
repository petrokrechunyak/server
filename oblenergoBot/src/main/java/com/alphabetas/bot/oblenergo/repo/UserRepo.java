package com.alphabetas.bot.oblenergo.repo;

import com.alphabetas.bot.oblenergo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    // broadcast recipients come ordered by priority: messages are queued, and so delivered, in this order

    List<User> getAllBySubscribedTrueOrderByPriorityAsc();

    List<User> getAllBySubscribedTrueAndGroupsNotEmptyOrderByPriorityAsc();

    // one query for everyone with their groups, instead of lazy-loading each user's groups separately
    @Query("select distinct u from User u join fetch u.groups where u.subscribed = true order by u.priority")
    List<User> findSubscribedWithGroups();

    long countBySubscribedTrue();

    long countBySubscribedTrueAndGroupsNotEmpty();

    long countBySubscribedFalse();

    @Transactional
    @Modifying
    @Query("update User u set u.compact = false where u.compact is null")
    int setCompactFalseWhereNull();

    // only fills users that have no priority yet, so values set with /priority survive restarts
    @Transactional
    @Modifying
    @Query("update User u set u.priority = :priority where u.priority is null and u.userId = :userId")
    int setPriorityWhereNull(@Param("userId") Long userId, @Param("priority") int priority);

    @Transactional
    @Modifying
    @Query("update User u set u.priority = :priority where u.priority is null")
    int setPriorityWhereNull(@Param("priority") int priority);
}
