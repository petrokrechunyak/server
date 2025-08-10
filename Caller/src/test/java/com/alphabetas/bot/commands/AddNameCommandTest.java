package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.GroupName;
import com.alphabetas.bot.model.Name;
import com.alphabetas.bot.utils.CommandUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;


import java.sql.SQLOutput;
import java.util.List;
import java.util.Set;

import static com.alphabetas.bot.commands.HelpCommand.S;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddNameCommandTest extends CommandTest {

    private Command addNameCommand = new AddNameCommand();

    @BeforeEach
    public void prepareCommand() {
        decorateCommand(addNameCommand);
    }

    @Test
    public void cannotAddNameToBot_Test() {
        // given
        message.setText("!Додати");
        message.setReplyToMessage(replyMessage);
        replyMessage.setFrom(someBot);
        addNameCommand.updateCommand(update);

        // when
        int result = addNameCommand.execute(update);

        //then
        verify(messageService, Mockito.times(1)).sendMessage(anyString());
        assertEquals(IMPOSSIBLE_TO_USE_WITH_BOTS.getReturnCode(), result);
    }

    @Test
    public void cantAddWithoutNames_Test() {
        // given
        message.setText(".додати");
        addNameCommand.updateCommand(update);


        // when
        int result = addNameCommand.execute(update);

        //then
        verify(messageService, Mockito.times(1)).sendMessage(anyString());
        assertEquals(USE_NAME_WITH_COMMAND.getReturnCode(), result);
    }

    @Test
    public void successAdding_Test() {
        // given
        message.setText(".додати Ім'я");
        addNameCommand.updateCommand(update);
        int namesSize = addNameCommand.user.getNames().size();


        // when
        int result = addNameCommand.execute(update);

        //then
        assertEquals(namesSize + 1, addNameCommand.user.getNames().size());
        verify(messageService, times(1)).sendMessage(Mockito.matches(".*успішно додано"));
        assertEquals(SUCCESS.getReturnCode(), result);
    }

    @Test
    public void successAddingGroupName_Test() {
        // given
        Command command = new AddNameCommand();
        message.setText(".додати Група 1");

        Answer<GroupName> answer = invocationOnMock -> {
            String name = invocationOnMock.getArgument(0);
            return name.equals(groupName.getName()) ? groupName : null;
        };

        when(groupNameService.getByNameAndChat(isA(String.class), isA(CallerChat.class)))
                .thenAnswer(answer);

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString("Групове ім'я ");
        verifyString(CommandUtils.decryptSpace(groupName.getName()));
    }

    @Test
    public void shouldNotSave_nameLimitAchieved_Test() {
        // given
        Command command = new AddNameCommand();
        update.getMessage().setText("Кликун додати ім'я_15");

        decorateCommand(command);
        command.updateCommand(update);

        int limit = 0;

        command.chat.getConfig().setNameLimit(limit);

        Answer<Set<Name>> answer = invocationOnMock -> {
            CallerUser user = invocationOnMock.getArgument(0);
            return user.getUserId().equals(callerSender.getUserId())
                    ? callerSender.getNames()
                    : callerReplySender.getNames();
        };

        when(nameService.getAllByCallerUser(isA(CallerUser.class))).thenAnswer(answer);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString(AddNameCommand.LIMIT_ACHIEVED + limit);
        verify(nameService, times(0)).save(any());
        verify(userService, times(0)).save(any());
    }

    @Test
    public void shouldNotSave_nameAlreadyTaken_Test() {
        // given
        Command command = new AddNameCommand();
        String name = "Ім'я 1";
        update.getMessage().setText("Кликун додати " + name);

        callerChat.getConfig().setAllowSpace(true);

        when(nameService.getByCallerChatAndName(callerChat, CommandUtils.encryptSpace(name))).thenReturn(CommandTest.name);

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString(AddNameCommand.ALREADY_TAKEN);
        verify(nameService, times(0)).save(any());
        verify(userService, times(0)).save(any());
    }

    @Test
    public void shouldNotSave_nameTooSmall_Test() {
        // given
        Command command = new AddNameCommand();
        String name = "hi";
        update.getMessage().setText("Кликун додати " + name);

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString(AddNameCommand.TOO_SMALL);
        verify(nameService, times(0)).save(any());
        verify(userService, times(0)).save(any());
    }

    @Test
    public void shouldNotSave_nameBlocked_Test() {
        // given
        Command command = new AddNameCommand();
        String name = "block";
        update.getMessage().setText("Кликун додати " + name);

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString(AddNameCommand.BLOCKED_NAME);
        verify(nameService, times(0)).save(any());
        verify(userService, times(0)).save(any());
    }

    @Test
    public void shouldNotSave_blockedSymbols_Test() {
        // given
        Command command = new AddNameCommand();
        String name = "some * name=";
        update.getMessage().setText("Кликун додати " + name);

        decorateCommand(command);
        command.updateCommand(update);

        // when
        int result = command.execute(update);

        //then
        assertEquals(SUCCESS.getReturnCode(), result);
        verifyString(AddNameCommand.BLOCKED_SYMBOLS);
        verify(nameService, times(0)).save(any());
        verify(userService, times(0)).save(any());
    }
}
