package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.CallerChat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IAmHereCommandTest extends CommandTest {

    @Test
    public void successAdding_Test() {
        // given
        IAmHereCommand command = new IAmHereCommand();
        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString("");
    }
}
