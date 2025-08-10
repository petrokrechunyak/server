package com.alphabetas.bot.model.enums;

import lombok.Getter;

@Getter
public enum CallerRoles {

    CREATOR(6, "\uD83D\uDC51 Власник Кликуна \uD83D\uDC51"),
    MODERATOR(5, "Модератор Кликуна"),
    OWNER(4, "Власник групи"),
    TOP_ADMIN(3, "Старший адмін"),
    LOWER_ADMIN(2, "Молодший адмін"),
    MEMBER(1, "Учасник");

    private final int roleNumber;
    private final String ukrName;

    CallerRoles(int roleNumber, String ukrName) {
        this.roleNumber = roleNumber;
        this.ukrName = ukrName;
    }
}
