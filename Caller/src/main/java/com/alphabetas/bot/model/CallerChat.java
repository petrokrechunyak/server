package com.alphabetas.bot.model;

import com.alphabetas.bot.marriage.model.MarriageModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "caller_chats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallerChat {

    @Id
    private Long id;

    private String title;

    @ColumnDefault("0")
    private Integer memberCount;

    @OneToMany(mappedBy = "callerChat", cascade = CascadeType.REMOVE)
    private Set<CallerUser> callerUsers;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private Set<Name> Names;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private Set<GroupName> groupNames;

    @OneToOne(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private ChatConfig config;

    @OneToOne(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private PremiumChat premium;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private Set<MessageCount> messageCounts;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private Set<StatsCount> statsCounts;

    @ColumnDefault("0")
    private int callerCalls;

    @ColumnDefault("0")
    private int simpleCalls;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private Set<MarriageModel> marriageModels;

    @ColumnDefault("0")
    private int allMessages;

    public CallerChat(Long id, String title) {
        this.id = id;
        this.title = title;
        Names = new HashSet<>();
        callerUsers = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallerChat that = (CallerChat) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CallerChat{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", memberCount=" + memberCount +
                ", allMessages=" + allMessages +
                '}';
    }

    public int messageCount() {
        AtomicInteger i = new AtomicInteger();
        this.getMessageCounts().forEach(x -> i.addAndGet(x.getCount()));
        return i.get();
    }

    public void incrementCallerCalls() {
        setCallerCalls(getCallerCalls()+1);
    }

    public void incrementSimpleCalls() {
        setSimpleCalls(getSimpleCalls()+1);
    }
}
