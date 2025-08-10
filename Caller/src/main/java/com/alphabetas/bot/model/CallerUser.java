package com.alphabetas.bot.model;

import com.alphabetas.bot.model.enums.CallerRoles;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "caller_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CallerUser.UserId.class)
public class CallerUser {

    @Id
    @Column(name = "caller_user_id")
    private Long userId;

    private String firstname;

    private String username;

    private Date presentFrom;

    @Id
    @ManyToOne
    private CallerChat callerChat;

    @ManyToMany(mappedBy = "users")
    private Set<GroupName> groupNames;

    @OneToMany(mappedBy = "callerUser", cascade = CascadeType.REMOVE)
    private Set<Name> names;

    @OneToMany(mappedBy = "callerUser", cascade = CascadeType.REMOVE)
    private Set<MessageCount> messageCounts;

    @OneToMany(mappedBy = "callerUser", cascade = CascadeType.REMOVE)
    private Set<StatsCount> statsCounts;

    @Enumerated(EnumType.STRING)
    private CallerRoles role;

    @Transient
    private String mentionedUser;
    public CallerUser(Long userId, String firstname, String username, CallerChat callerChat) {
        this.userId = userId;
        this.firstname = firstname;
        this.username = username;
        this.callerChat = callerChat;
        names = new HashSet<>();
        groupNames = new HashSet<>();
        role = CallerRoles.MEMBER;
        presentFrom = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallerUser that = (CallerUser) o;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "CallerUser{" +
                "userId=" + userId +
                ", firstname='" + firstname + '\'' +
                ", username='" + username + '\'' +
                ", callerChat=" + callerChat +
                '}';
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class UserId implements Serializable {
        private Long userId;

        private Long callerChat;

    }

    public String getMentionedUser() {
        return CommandUtils.makeLink(getUserId(), getFirstname());
    }
}
