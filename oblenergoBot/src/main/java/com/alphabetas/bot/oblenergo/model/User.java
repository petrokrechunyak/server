package com.alphabetas.bot.oblenergo.model;

import com.alphabetas.bot.oblenergo.utils.MainUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "oblenergo_users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public static final int DEFAULT_PRIORITY = 100;

    @Id
    private Long userId;

    private String username;

    private String firstname;

    private Boolean subscribed;

    private Boolean compact;

    // broadcasts go out in ascending order: lower gets schedules first
    private Integer priority;

    @ManyToMany(mappedBy = "users")
    private List<Group> groups;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId.equals(user.userId);
    }

    public User(Long userId, String username, String firstname, Boolean subscribed) {
        this.userId = userId;
        this.username = username;
        this.firstname = MainUtil.removeBadSymbols(firstname);
        this.subscribed = subscribed;
        this.compact = false;
        this.priority = DEFAULT_PRIORITY;
        groups = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
