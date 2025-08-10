package com.alphabetas.bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "group_names")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="chat_id")
    private CallerChat chat;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "users_group_names",
            joinColumns = { @JoinColumn(name = "group_name_id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id"),
                                   @JoinColumn(name = "chat_id") }
    )
    private Set<CallerUser> users;

    public GroupName(CallerChat chat, String name) {
        this.chat = chat;
        this.name = name;
        users = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupName groupName = (GroupName) o;
        return Objects.equals(chat, groupName.chat) && Objects.equals(name, groupName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat, name);
    }
}
