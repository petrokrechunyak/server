package com.alphabetas.bot.oblenergo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "oblenergo_groups")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    private Integer groupId;

    @ManyToMany
    @JoinTable(
            name = "group_users",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users;

    private String shutdowns;

    @Transient
    private String oldShutdowns;

    public Group(Integer groupId) {
        this.groupId = groupId;
        this.users = new ArrayList<>();
    }

    public Group(Integer groupId, String shutdowns) {
        this.shutdowns = shutdowns;
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return groupId.equals(group.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId=" + groupId +
                ", shutdowns='" + shutdowns + '\'' +
                '}';
    }
}
