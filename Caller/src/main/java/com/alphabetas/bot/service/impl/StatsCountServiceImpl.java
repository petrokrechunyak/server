package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.StatsCount;
import com.alphabetas.bot.model.enums.StatsCountType;
import com.alphabetas.bot.model.enums.StatsRangeEnum;
import com.alphabetas.bot.repo.StatsCountRepo;
import com.alphabetas.bot.service.StatsCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Calendar;
import java.util.List;

@Service
public class StatsCountServiceImpl implements StatsCountService {

    @Autowired
    private StatsCountRepo countRepo;

    @Override
    public StatsCount save(StatsCount statsCount) {
        return countRepo.save(statsCount);
    }

    @Override
    public void delete(StatsCount statsCount) {
        countRepo.delete(statsCount);
    }

    @Override
    public StatsCount getByDayOfYear(Long userId, CallerChat chat, int day, int year, StatsCountType countType) {
        return countRepo.getByUserIdAndChatAndDayAndYearAndCountType(userId, chat, day, year, countType);
//        return null;
    }

    @Override
    public List<StatsCount> getAllByWeek(Long userId, CallerChat chat, int week, int year, StatsCountType countType) {
        return countRepo.getAllByUserIdAndChatAndWeekAndYearAndCountType(userId, chat, week, year, countType);
    }

    @Override
    public List<StatsCount> getAllByMonth(Long userId, CallerChat chat, int month, int year, StatsCountType countType) {
        return countRepo.getAllByUserIdAndChatAndMonthAndYearAndCountType(userId, chat, month, year, countType);
    }

    @Override
    public StatsCount getMessageCountByDayOfYear(Long userId, CallerChat chat, int day, int year) {
        return getByDayOfYear(userId, chat, day, year, StatsCountType.MESSAGE);
    }

    @Override
    public List<StatsCount> getMessageCountByWeek(Long userId, CallerChat chat, int week, int year) {
        return getAllByWeek(userId, chat, week, year, StatsCountType.MESSAGE);
    }

    @Override
    public List<StatsCount> getMessageCountByMonth(Long userId, CallerChat chat, int month, int year) {
        return getAllByMonth(userId, chat, month, year, StatsCountType.MESSAGE);
    }

    @Override
    public StatsCount getCallingCountByDayOfYear(Long userId, CallerChat chat, int day, int year) {
        return getByDayOfYear(userId, chat, day, year, StatsCountType.CALLING);
    }

    @Override
    public List<StatsCount> getCallingCountByWeek(Long userId, CallerChat chat, int week, int year) {
        return getAllByWeek(userId, chat, week, year, StatsCountType.CALLING);
    }

    @Override
    public List<StatsCount> getCallingCountByMonth(Long userId, CallerChat chat, int month, int year) {
        return getAllByMonth(userId, chat, month, year, StatsCountType.CALLING);
    }

    @Override
    public void incrementStats(Long userId, CallerChat chat, StatsCountType type) {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);

        StatsCount count = getByDayOfYear(userId, chat, dayOfYear, year, type);
        if(count == null) {
            count = new StatsCount(chat, userId, type, StatsRangeEnum.DAY);
        }
        count.incrementCounter();
        save(count);
    }
}
