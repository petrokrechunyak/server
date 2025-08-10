package com.alphabetas.bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "caller_names", uniqueConstraints =
@UniqueConstraint(columnNames = {"caller_chat_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Name {

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

    private String name;

    public Name(Long userId, CallerChat callerChat, String name) {
        this.userId = userId;
        this.chat = callerChat;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Name{" +
                "callerUser=" + callerUser +
                ", callerChat=" + chat +
                ", name='" + name + '\'' +
                '}';
    }
}
