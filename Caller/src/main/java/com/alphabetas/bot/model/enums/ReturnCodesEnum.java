package com.alphabetas.bot.model.enums;

import lombok.Getter;

@Getter
public enum ReturnCodesEnum {

    UNKNOWN_COMMAND(-1),
    SUCCESS(0),
    NOT_ENOUGH_RIGHTS(1),
    USE_NAME_WITH_COMMAND(2),
    IMPOSSIBLE_TO_USE_WITH_BOTS(3),
    CANT_INTERACT_WITH_CALLER(10),
    CANT_MARRY_YOURSELF(21),
    ALREADY_MARRIED(22),
    PREMIUM_NEEDED(30),
    ;

    ReturnCodesEnum(int returnCode) {
        this.returnCode = returnCode;
    }

    private int returnCode;

}
