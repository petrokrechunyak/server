package com.alphabetas.bot.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.alphabetas.bot.commands.HelpCommand.S;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HelpCommandTest extends CommandTest {

    @Test
    public void successAdding_Test() {
        // given
        Command command = new HelpCommand();
        decorateCommand(command);
        update.getMessage().setText("/help");
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString(S);
    }

}

