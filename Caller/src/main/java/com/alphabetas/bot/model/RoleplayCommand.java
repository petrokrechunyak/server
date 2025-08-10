package com.alphabetas.bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "roleplay_commands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleplayCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phrase;

    private String replyPhrase;

    private boolean callerLikesIt;

    private int uses;

    private boolean adultOnly;

    public RoleplayCommand(String phrase, String replyPhrase) {
        this.phrase = phrase;
        this.replyPhrase = replyPhrase;
        callerLikesIt = true;
        adultOnly = false;
    }
}
