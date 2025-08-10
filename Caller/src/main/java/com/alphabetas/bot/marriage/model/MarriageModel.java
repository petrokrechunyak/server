package com.alphabetas.bot.marriage.model;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "marriages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MarriageModel implements Comparable<MarriageModel> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chat_id",
            insertable = false, updatable = false)
    @JoinColumn(name = "user1_id",
            insertable = false, updatable = false)
    private CallerUser user1;

    @Column(name = "user1_id")
    private Long user1Id;

    @OneToOne
    @JoinColumn(name = "chat_id",
            insertable = false, updatable = false)
    @JoinColumn(name = "user2_id",
            insertable = false, updatable = false)
    private CallerUser user2;

    @Column(name = "user2_id")
    private Long user2Id;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private CallerChat chat;

    private Long startDate;

    public MarriageModel(Long user1Id, Long user2Id, CallerChat chat) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.chat = chat;
    }

    @Override
    public int compareTo(MarriageModel marriageModel) {
        return (this.startDate.compareTo(marriageModel.startDate));
    }
}
