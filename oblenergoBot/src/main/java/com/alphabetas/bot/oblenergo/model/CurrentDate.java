package com.alphabetas.bot.oblenergo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Table(name = "current_data")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;

    public CurrentDate(String date) {
        this.date = date;
    }
}
