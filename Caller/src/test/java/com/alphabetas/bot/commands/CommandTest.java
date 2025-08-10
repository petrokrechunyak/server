package com.alphabetas.bot.commands;

import com.alphabetas.CallerApplication;
import com.alphabetas.bot.model.*;
import com.alphabetas.bot.repo.RoleplayRepo;
import com.alphabetas.bot.service.*;
import com.alphabetas.bot.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@Slf4j
public class CommandTest {

    protected static ChatConfigService chatConfigService;
    protected static ChatService chatService;
    protected static GroupNameService groupNameService;
    @Mock
    protected static MessageCountService messageCountService;
    protected static NameService nameService;
    protected static UserService userService;
    protected static MessageService messageService;
    protected static RoleplayRepo roleplayRepo;

    protected static Update update;
    protected static Chat chat;
    protected static Message message;
    protected static Message replyMessage;
    protected static User sender;
    protected static User replyUser;
    protected static User someBot;

    protected static CallerChat callerChat;
    protected static CallerUser callerSender;
    protected static CallerUser callerReplySender;

    protected static Name name;
    protected static Name name2;
    protected static Name someOneName;

    protected static GroupName groupName;

    @BeforeAll
    public static void firstPrepare() {
        new CallerApplication().initCommands();
        log.info("Prepared container commands!");
    }

    @BeforeEach
    void prepare() {
        log.info("Prepared chat and user data!");
        update = new Update();

        chat = new Chat(1L, "supergroup");
        chat.setTitle("Some chat title");

        someBot = new User(0L, "Some bot", true);
        someBot.setUserName("some_bot");
        sender = new User(1L, "Some firstname", false);
        sender.setUserName("some_username");
        replyUser = new User(2L, "Reply user", false);
        replyUser.setUserName("reply_username");

        replyMessage = new Message();
        replyMessage.setChat(chat);
        replyMessage.setText("some text");
        replyMessage.setFrom(replyUser);

        message = new Message();
        message.setChat(chat);
        message.setFrom(sender);

        update.setMessage(message);

        ChatConfig chatConfig = new ChatConfig(chat.getId(), 8);
        chatConfig.setBlockedNames(Collections.EMPTY_SET);
        chatConfig.setBlockedNames(Set.of("block"));

        callerChat = new CallerChat(chat.getId(), chat.getTitle());
        callerChat.setConfig(chatConfig);

        callerSender = new CallerUser(sender.getId(), sender.getFirstName(),
                sender.getUserName(), callerChat);
        callerReplySender = new CallerUser(replyUser.getId(), replyUser.getFirstName(),
                replyUser.getUserName(), callerChat);

        groupName = new GroupName(callerChat, "Групаˁ1");
        groupName.getUsers().add(callerSender);
        callerSender.getGroupNames().add(groupName);

        name = new Name(callerSender.getUserId(), callerChat, "Ім'яˁ1");
        name.setCallerUser(callerSender);

        name2 = new Name(callerSender.getUserId(), callerChat, "Ім'я");
        name2.setCallerUser(callerSender);

        someOneName = new Name(callerReplySender.getUserId(), callerChat, "Ім'я_2");
        someOneName.setCallerUser(callerReplySender);

        callerSender.getNames().add(name);
        callerSender.getNames().add(name2);
        callerReplySender.getNames().add(someOneName);


        prepareServices();
    }

    void verifyString(String message) {
        verifyString(message, 1);
    }

    void verifyString(String message, int times) {
        verify(messageService, times(times)).sendMessage(matches(".*" + message + ".*"));
    }


    public static void prepareServices() {

        mockChatService();
        mockUserService();
        mockMessageService();
        mockNameService();
        mockGroupNameService();
        mockChatConfigService();
        mockRolePlayRepo();
    }

    public static void mockUserService() {
        log.info("Prepared user service!");

        userService = mock(UserService.class);

        Answer<CallerUser> answer = arg -> {
            Object argument = arg.getArgument(0);
            if(argument.equals(callerSender.getUserId())) {
                return callerSender;
            } else if (argument.equals(callerReplySender)) {
                return callerReplySender;
            }
            return null;
        };

        when(userService.getByUserIdAndCallerChat(any(Long.class), any(CallerChat.class))).thenAnswer(answer);
    }

    public static void mockMessageService() {
        log.info("Prepared message service!");
        messageService = mock(MessageService.class);

        when(messageService.sendMessage(isA(String.class))).thenReturn(new Message());
    }

    public static void mockChatService() {
        log.info("Prepared chat service!");
        
        chatService = mock(ChatService.class);

//        when(chatService.findAll()).thenReturn(List.of(callerChat));
        when(chatService.getByUpdate(update)).thenReturn(callerChat);
    }

    public static void mockGroupNameService() {
        log.info("Prepared group name service!");

        groupNameService = mock(GroupNameService.class);
    }

    public static void mockNameService() {
        log.info("Prepared name service!");

        nameService = mock(NameService.class);

    }

    public static void mockChatConfigService() {
        log.info("Prepared chat config service!");

        chatConfigService = mock(ChatConfigService.class);

    }

    public static void mockRolePlayRepo() {
        log.info("Prepared RP repo");

        roleplayRepo = mock(RoleplayRepo.class);
    }

    protected void decorateCommand(Command command) {


        command.setMessageService(messageService);
        command.setChatService(chatService);
        command.setUserService(userService);
        ServiceUtils.groupNameService = groupNameService;
        ServiceUtils.nameService = nameService;
        ServiceUtils.configService = chatConfigService;
    }

}
