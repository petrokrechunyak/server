package com.alphabetas.bot.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShowCommandTest extends CommandTest {

    @Test
    public void successShowingOwnNames_Test() {
        // given
        Command command = new ShowCommand();

        update.getMessage().setText("/show");
        update.getMessage().setReplyToMessage(null);

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString(callerSender.getFirstname());
        verifyString("Ім'я 1");
    }

    @Test
    public void successShowingOthersNames_Test() {
        // given
        Command command = new ShowCommand();
        update.getMessage().setText("/show");
        update.getMessage().setReplyToMessage(replyMessage);

        when(userService.getByUserIdAndCallerChat(replyUser.getId(), callerChat)).thenReturn(callerReplySender);

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString(callerReplySender.getFirstname());
        verifyString("Ім'я_2");
    }

}
