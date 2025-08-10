package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.RoleplayCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RPCommandTest extends CommandTest {

    private List<RoleplayCommand> rpCommands = new ArrayList<>();
    RoleplayCommand c1, c2, c3;

    @BeforeEach
    void initList() {
        c1 = new RoleplayCommand("команда 1", "відповідь 1");
        c1.setAdultOnly(false);
        c2 = new RoleplayCommand("команда 2", "відповідь 2");
        c1.setAdultOnly(true);
        c3 = new RoleplayCommand("команда 3", "відповідь 3");
        c1.setAdultOnly(false);

        rpCommands.add(c1);
        rpCommands.add(c2);
        rpCommands.add(c3);
    }

    @Test
    public void mustReturnNotAdultRp_Test() {

        // given
        RPCommand command = new RPCommand();
        command.setMsgText(".рп");
        command.setRepo(roleplayRepo);
        callerChat.getConfig().setAllowAdult(false);

        when(roleplayRepo.getAllByAdultOnlyIsFalse()).thenReturn(List.of(c1, c3));

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString("команда 1");
        verifyString("команда 2", 0);
        verifyString("команда 3");
    }

    @Test
    public void mustReturnAllRp_Test() {

        // given
        RPCommand command = new RPCommand();
        command.setMsgText(".рп");
        command.setRepo(roleplayRepo);
        callerChat.getConfig().setAllowAdult(true);

        when(roleplayRepo.findAll()).thenReturn(List.of(c1, c2, c3));

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString("команда 1");
        verifyString("команда 2");
        verifyString("команда 3");
    }

}
