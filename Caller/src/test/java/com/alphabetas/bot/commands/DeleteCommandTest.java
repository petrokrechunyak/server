package com.alphabetas.bot.commands;

import com.alphabetas.bot.utils.CommandUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.USE_NAME_WITH_COMMAND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeleteCommandTest extends CommandTest {

    private Command addNameCommand = new DeleteCommand();

    @BeforeEach
    public void prepareCommand() {
        decorateCommand(addNameCommand);
        doNothing().when(userService).delete(any());
    }

    @Test
    public void shouldBotDelete_withoutNames_Test() {
        // given
        message.setText(".видалити");
        addNameCommand.updateCommand(update);

        // when
        int result = addNameCommand.execute(update);

        //then
        verify(messageService, Mockito.times(1)).sendMessage(anyString());
        verify(userService, times(0)).delete(any());
        assertEquals(USE_NAME_WITH_COMMAND.getReturnCode(), result);
    }

    @Test
    public void successDelete_Test() {
        // given
        String nameToDelete = "Ім'я";
        message.setText(".видалити " + nameToDelete);
        addNameCommand.updateCommand(update);

        when(nameService.getByCallerChatAndName(any(), any())).thenReturn(name);

        // when
        int result = addNameCommand.execute(update);

        //then
        verify(messageService, Mockito.times(1)).sendMessage(anyString());
        verify(nameService, times(1)).delete(any());
        assertEquals(SUCCESS.getReturnCode(), result);
    }
}
