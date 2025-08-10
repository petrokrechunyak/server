package com.alphabetas.bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "message_count")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCount {

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

    private Long startTime;

    public MessageCount(Long userId, CallerChat chat, Integer count, Long startTime) {
        this.userId = userId;
        this.chat = chat;
        this.count = count;
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "MessageCount{" +
                "userId=" + userId +
                ", count=" + count +
                ", startTime=" + startTime +
                '}';
    }

}
