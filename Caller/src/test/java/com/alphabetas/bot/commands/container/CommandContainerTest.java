package com.alphabetas.bot.commands.container;


import com.alphabetas.bot.commands.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommandContainerTest extends CommandTest {

    @InjectMocks
    static CommandContainer container;
    @Mock
    private static ApplicationContext context;

    @BeforeAll
    public static void prepareContainer() {
        context = mock(ApplicationContext.class);
        container = new CommandContainer();
        container.setContext(context);
    }

    @Test
    public void iAmHereCommandTest() {
        // given
        Command command = new IAmHereCommand();
        decorateCommand(command);
        message.setText("Кликун");

        when(context.getBean((Class<Command>) any())).thenReturn(command);

        // when
        container.retrieveCommand(update);

        // then
        verify(context, Mockito.times(1)).getBean(command.getClass());
    }

    @Test
    public void noCommandTest() {

        Command command = new NoCommand();
        decorateCommand(command);
        message.setText("Some text");

        when(context.getBean((Class<Command>) any())).thenReturn(command);

        // when
        container.retrieveCommand(update);

        // then
        verify(context, Mockito.times(1)).getBean(command.getClass());
    }

    @Test
    public void shouldReturnAddCommand() {
        Command command = new AddNameCommand();
        decorateCommand(command);
        message.setText("!.Додати ім'я");

        when(context.getBean((Class<Command>) any())).thenReturn(command);

        // when
        container.retrieveCommand(update);

        // then
        verify(context, Mockito.times(1)).getBean(command.getClass());
    }

    @Test
    public void shouldReturnAddCommand_WithSpace() {
        Command command = new AddNameCommand();
        decorateCommand(command);
        message.setText(". Додати ім'я");

        when(context.getBean((Class<Command>) any())).thenReturn(command);

        // when
        container.retrieveCommand(update);

        // then
        verify(context, Mockito.times(1)).getBean(command.getClass());
    }


    @Test
    public void shouldReturnNoCommand_notBotUsername() {
        Command command = new NoCommand();
        decorateCommand(command);
        message.setText("/help@somebot");

        when(context.getBean((Class<Command>) any())).thenReturn(command);

        // when
        container.retrieveCommand(update);

        // then
        verify(context, Mockito.times(1)).getBean(command.getClass());
    }

    @Test
    public void shouldReturnHelpCommand_botUsername() {
        Command command = new HelpCommand();
        decorateCommand(command);
        message.setText("/help@caller_ua_bot");
        container.setBotUsername("caller_ua_bot");

        when(context.getBean((Class<Command>) any())).thenReturn(command);

        // when
        container.retrieveCommand(update);

        // then
        verify(context, Mockito.times(1)).getBean(command.getClass());
    }


}
