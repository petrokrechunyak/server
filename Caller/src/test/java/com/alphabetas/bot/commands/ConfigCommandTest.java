package com.alphabetas.bot.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.NOT_ENOUGH_RIGHTS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConfigCommandTest extends CommandTest {

    @Test
    public void cannotUseNotEnoughRights_Test() {
        // given
        Command command = new ConfigCommand();
        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(NOT_ENOUGH_RIGHTS.getReturnCode(), result);
    }

}

