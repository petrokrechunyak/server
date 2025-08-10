package com.alphabetas.bot.service;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.StatsCount;
import com.alphabetas.bot.model.enums.StatsCountType;

import java.util.List;

public interface StatsCountService extends AbstractService<StatsCount> {

    StatsCount getByDayOfYear(Long userId, CallerChat chat, int day, int year, StatsCountType countType);

    List<StatsCount> getAllByWeek(Long userId, CallerChat chat, int week, int year, StatsCountType countType);

    List<StatsCount> getAllByMonth(Long userId, CallerChat chat, int month, int year, StatsCountType countType);

    StatsCount getMessageCountByDayOfYear(Long userId, CallerChat chat, int day, int year);

    List<StatsCount> getMessageCountByWeek(Long userId, CallerChat chat, int week, int year);

    List<StatsCount> getMessageCountByMonth(Long userId, CallerChat chat, int month, int year);

    StatsCount getCallingCountByDayOfYear(Long userId, CallerChat chat, int day, int year);

    List<StatsCount> getCallingCountByWeek(Long userId, CallerChat chat, int week, int year);

    List<StatsCount> getCallingCountByMonth(Long userId, CallerChat chat, int month, int year);

    void incrementStats(Long userId, CallerChat chat, StatsCountType type);

}
