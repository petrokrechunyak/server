package com.alphabetas.bot.utils;

import com.alphabetas.bot.marriage.service.MarriageService;
import com.alphabetas.bot.service.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter
@Component
public class ServiceUtils {

    public static GroupNameService groupNameService;
    public static NameService nameService;
    public static MessageCountService messageCountService;
    public static ChatService chatService;
    public static ChatConfigService configService;
    public static MarriageService marriageService;
    public static UserService userService;
    public static StatsCountService statsCountService;

    @Autowired
    public ServiceUtils(GroupNameService groupNameService1, NameService nameService1, MessageCountService messageCountService1,
                        ChatService chatService1, ChatConfigService configService1, MarriageService marriageService1,
                        UserService userService1, StatsCountService statsCountService1) {
        groupNameService = groupNameService1;
        nameService = nameService1;
        messageCountService = messageCountService1;
        chatService = chatService1;
        configService = configService1;
        marriageService = marriageService1;
        userService = userService1;
        statsCountService = statsCountService1;
    }


}
