package com.alphabetas.bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Table(name = "premium_chats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PremiumChat {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @OneToOne
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private CallerChat chat;

    @ColumnDefault("true")
    private boolean isPremiumActive;

    private Long activeTo;

    public PremiumChat(CallerChat chat) {
        this.chatId = chat.getId();
        this.isPremiumActive = true;
    }

}
