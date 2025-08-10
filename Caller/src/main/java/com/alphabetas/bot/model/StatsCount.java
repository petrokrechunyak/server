package com.alphabetas.bot.model;

import com.alphabetas.bot.model.enums.StatsCountType;
import com.alphabetas.bot.model.enums.StatsRangeEnum;
import lombok.*;

import javax.persistence.*;
import java.util.Calendar;

@Entity
@Table(name = "stats_count")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caller_chat_id",
            insertable = false, updatable = false)
    @JoinColumn(name = "caller_user_id",
            insertable = false, updatable = false)
    private CallerUser callerUser;

    @Column(name = "caller_user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "caller_chat_id")
    private CallerChat chat;

    private Integer count;

    @Enumerated(EnumType.STRING)
    private StatsCountType countType;

    @Enumerated(EnumType.STRING)
    private StatsRangeEnum range;

    private int year;
    private int month;
    private int week;
    private int dayOfWeek;
    private int dayOfMonth;
    private int day;

    public StatsCount(CallerChat chat, Long userId, StatsCountType countType, StatsRangeEnum range) {
        this.chat = chat;
        this.userId = userId;
        this.countType = countType;
        this.range = range;
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        week = calendar.get(Calendar.WEEK_OF_YEAR);
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        day = calendar.get(Calendar.DAY_OF_YEAR);
        count = 0;

    }

    @Override
    public String toString() {
        return "StatsCount{" +
                "callerUser=" + callerUser +
                ", chat=" + chat +
                ", count=" + count +
                ", callingType=" + countType +
                ", range=" + range +
                '}';
    }


    public void incrementCounter() {
        count++;
    }
}
