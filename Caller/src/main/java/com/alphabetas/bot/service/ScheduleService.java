package com.alphabetas.bot.service;

import com.alphabetas.bot.CallerBot;
import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.MessageCount;
import com.alphabetas.bot.service.impl.MessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.chatmember.*;

import java.util.List;

import static com.alphabetas.bot.utils.ServiceUtils.chatService;
import static com.alphabetas.bot.utils.ServiceUtils.userService;

@Component
@Slf4j
public class ScheduleService {


    @Autowired
    private MessageCountService messageCountService;

    private static MessageService messageService;

    private static final int SECOND = 1000;
    private static final int DAY = 86400 * SECOND;

    public ScheduleService(CallerBot bot) {
        messageService = new MessageServiceImpl();
    }

    // every 0.5 hour
    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void checkForDeletion() {
        new Thread(() -> {
            List<CallerChat> all = chatService.findAll();
            for(CallerChat chat: all) {
                if(chat.getTitle() == null) {
                    chatService.delete(chat);
                    return;
                }
                int counter = checkForDelete(chat);
                if(counter != 0) {
                    try {
                        messageService.sendMessage(chat.getId(), "Очищено імен: " + counter, false);
                    } catch (Exception e) {
                        messageService.sendErrorMessage(ExceptionUtils.getStackTrace(e));
                    }
                }
            }
        }).start();
    }

    // every 100 seconds
    @Scheduled(fixedDelay = 1000 * 100)
    public void checkForOldMessages() {
        List<MessageCount> all = messageCountService.getAll();
        long current = System.currentTimeMillis();
        current = current - (current % 100000);
        for (MessageCount messageCount: all) {
            if(current - DAY > messageCount.getStartTime()) {
                messageCountService.delete(messageCount);
            }
        }
    }

    public static int checkForDelete(CallerChat chat) {

        int counter = 0;
        log.info("searching users for deleting:");
        log.info(chat.getCallerUsers().toString());
        for(CallerUser u: chat.getCallerUsers()) {
            ChatMember chatMember = null;
            int currentSize = u.getNames().size();
            try {
                log.info("trying to find user {}", u);
                chatMember = messageService.getChatMember(chat.getId(), u.getUserId());
            } catch (Exception e) {
                String msg = e.getMessage();
                if(msg.contains("chat not found") || msg.contains("chat was deleted")
                        || msg.contains("bot was kicked")
                        || msg.contains("group chat was upgraded to a supergroup chat")) {
                    log.info("chat not found");
                    chatService.delete(chat);
                    break;
                } else if(!msg.contains("PARTICIPANT_ID_INVALID")) {
                    userService.delete(u);
                    counter += currentSize;
                    log.info("user to delete found:(Exception)" + u);
                    continue;
                }

            }
            if(chatMember instanceof ChatMemberLeft ||
                    chatMember instanceof ChatMemberBanned) {
                userService.delete(u);
                counter += currentSize;
                log.info("user to delete found: " + u);
            } else if(chatMember instanceof ChatMemberRestricted) {
                ChatMemberRestricted restricted = (ChatMemberRestricted) chatMember;
                if(!restricted.getIsMember()) {
                    userService.delete(u);
                    counter += currentSize;
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return counter;

    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void stats() {
        List<CallerChat> all = chatService.findAll();
        int callerCalls = 0;
        int simpleCalls = 0;
        for (CallerChat chat: all) {
            callerCalls += chat.getCallerCalls();
            simpleCalls += chat.getSimpleCalls();
            chatService.save(chat);

        }
        messageService.sendMessage(MessageService.MY_ID, "Загальна кількість чатів з Кликуном: <b>" + all.size() + "</b>\n" +
                "Кількість закликів через Кликуна: <b>" + callerCalls + "</b>\n" +
                "Кількість закликів через собачку: <b>" + simpleCalls + "</b>", false);
    }

}
