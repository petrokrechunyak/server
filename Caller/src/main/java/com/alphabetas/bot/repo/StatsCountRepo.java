package com.alphabetas.bot.repo;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.StatsCount;
import com.alphabetas.bot.model.enums.StatsCountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsCountRepo extends JpaRepository<StatsCount, Long> {

    StatsCount getByUserIdAndChatAndDayAndYearAndCountType(Long userId, CallerChat chat, int day, int year, StatsCountType countType);

    List<StatsCount> getAllByUserIdAndChatAndWeekAndYearAndCountType(Long userId, CallerChat chat, int week, int year, StatsCountType countType);

    List<StatsCount> getAllByUserIdAndChatAndMonthAndYearAndCountType(Long userId, CallerChat chat, int month, int year, StatsCountType countType);



}
