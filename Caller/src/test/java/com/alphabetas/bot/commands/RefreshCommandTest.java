package com.alphabetas.bot.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RefreshCommandTest extends CommandTest {

    @Test
    public void successAdding_Test() {

        // given
        Command command = new RefreshCommand();
        command.setMsgText("/refresh");
        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString("");
    }

}
