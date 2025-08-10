package com.alphabetas.bot;

import com.alphabetas.bot.callback.CallBack;
import com.alphabetas.bot.commands.container.CommandContainer;
import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.MessageCount;
import com.alphabetas.bot.model.StatsCount;
import com.alphabetas.bot.model.enums.CallerRoles;
import com.alphabetas.bot.model.enums.StatsCountType;
import com.alphabetas.bot.repo.StatsCountRepo;
import com.alphabetas.bot.repo.StatsCountSpecification;
import com.alphabetas.bot.service.MessageService;
import com.alphabetas.bot.service.UserService;
import com.alphabetas.bot.service.impl.MessageServiceImpl;
import com.alphabetas.bot.utils.CommandUtils;
import com.alphabetas.bot.utils.SpaceUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.PromoteChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.alphabetas.bot.callback.CallBack.callbackMap;
import static com.alphabetas.bot.utils.ServiceUtils.*;

@Slf4j
@Component
@NoArgsConstructor
public class CallerBot extends TelegramLongPollingBot {

    public static void main(String[] args) {
    }


    @Value("${bot.caller.username}")
    private String botUsername;
    @Value("${bot.caller.helpLink}")
    private String helpUrl;
    private static UserService userService;
    @Autowired
    private CommandContainer container;
    @Autowired
    private ApplicationContext context;
    public static List<Long> creatorList = new ArrayList<>();
    public static List<Long> moderList = new ArrayList<>();
    private SpaceUtils spaceUtils;

    private MessageService messageService;

    @Autowired
    private StatsCountRepo statsCountRepo;

    @Autowired
    public CallerBot(@Value("${bot.caller.token}") String botToken, @Value("#{${bot.roles.creator}}") List<String> creatorIds,
                     @Value("#{${bot.roles.moderator}}") List<String> moderatorIds, UserService userService) {
        super(botToken);
        CallerBot.userService = userService;
        MessageServiceImpl.setBot(this);
        this.messageService = new MessageServiceImpl();
        creatorList = creatorIds.stream().map(Long::parseLong).collect(Collectors.toList());
        moderList = moderatorIds.stream().map(Long::parseLong).collect(Collectors.toList());
        setCustomRoles();
        spaceUtils = new SpaceUtils(messageService, botToken);

//        ChatPermissions chatPermissions = new ChatPermissions();
//        chatPermissions.setCanSendMessages(true);
//        RestrictChatMember restrictChatMember = new RestrictChatMember("-1001847907970L", 731921794L, chatPermissions);
//        try {
//            execute(restrictChatMember);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//        }

        PromoteChatMember promoteChatMember = new PromoteChatMember("-1001847907970L", 731921794L, true, true, true, true, true, true, true, true, true, true, true, true);
        promoteChatMember.setIsAnonymous(true);
        try {
            execute(promoteChatMember);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    // ========================= MAIN METHOD =========================
    @Override
    public void onUpdateReceived(Update update) {
//        messageService.sendMessage(-4256380061L, "\uD83D\uFE49", false);
//        messageService.sendMessage(-4256380061L, "\uC83D\uFE49", false);
//        messageService.sendMessage(-4256380061L, "\uFFFF\uFF00", false);
//        System.out.println(statsCountRepo.findAll(StatsCountSpecification.byExample(
//                StatsCount.builder().week(18).userId(731921794L).countType(StatsCountType.MESSAGE).build()
//        )));
//
//        if(true) {
//            return;
//        }
//        if(update.hasMessage()) {
//            spaceUtils.trimSpaces(update);
//            if(update.getMessage().getForwardFrom() != null) {
//                return;
//            }
//        }
//        if(update.hasMessage() && (update.getMessage().hasText() || update.getMessage().getCaption() != null)) {
//            if (update.getMessage().getCaption() != null) {
//                update.getMessage().setText(update.getMessage().getCaption());
//            }
//            addMessageCount(update);
//            checkForSemeniv(update);
//            container.retrieveCommand(update).execute(update);
//        } else if (update.hasCallbackQuery()) {
//            String data = update.getCallbackQuery().getData();
//            CallBack callBack = context.getBean(callbackMap.get(data.split("\\.")[0]).getClass());
//            callBack.updateCommand(update);
//            callBack.setMessageService(new MessageServiceImpl(update));
//            callBack.execute(update);
//        } else if(update.hasMessage()) {
//            if(update.getMessage().getMigrateToChatId() != null) {
//                log.info(update.getMessage().toString());
//                CallerChat chat = chatService.getById(update.getMessage().getChatId(), update);
//                chat.setId(update.getMessage().getMigrateToChatId());
//                chatService.save(chat);
//
//                // FIXME CHAT DELETES AFTER MIGRATING TO SUPERGROUP
////                chat.getConfig().setChatId(chat.getId());
////                configService.save(chat.getConfig());
//
////                messageCountService.saveAll((List<MessageCount>) chat.getMessageCounts());
//                log.info("group was migrated to supergroup");
//            }
//            someOneLeft(update);
//            someOneEntered(update);
//        }
    }
    // ======================= MAIN METHOD end ========================

    private void checkForSemeniv(Update update) {
        if (update.getMessage().getText().equalsIgnoreCase("семенів")) {
            SendMessage message = new SendMessage(update.getMessage().getChatId().toString(), "Топ!");
            message.setReplyToMessageId(update.getMessage().getMessageId());
            messageService.sendMessage(message);
        }
     }

    private void addMessageCount(Update update) {
        try {
            long i = System.currentTimeMillis();
            i = i - (i % 100000);
            Long userId = update.getMessage().getFrom().getId();
            CallerChat chat = chatService.getById(update.getMessage().getChatId(), update);

            MessageCount mc = messageCountService.getByUserIdAndStartTime(userId, chat.getId(), i, update);
            if (mc == null) {
                mc = new MessageCount(userId, chat, 0, i);
            }

            StatsCount.builder()
                    .countType(StatsCountType.MESSAGE)
                            .userId(userId)
                                    .chat(chat)
                                            .day(124)
                                                    .build();

            statsCountService.incrementStats(userId, chat, StatsCountType.MESSAGE);

            mc.setCount(mc.getCount() + 1);
            chatService.incrementMessages(chat);
            messageCountService.save(mc);
        } catch (Exception e) {
            messageService.sendErrorMessage(e, update);
        }
    }

    public void someOneLeft(Update update) {

        User left = update.getMessage().getLeftChatMember();

        if (left != null) {
            CallerChat chat = chatService.getById(update.getMessage().getChatId(), update);
            try {
                messageService.sendMessage(update.getMessage().getChat().getId(), "Бувайте!\nНадіємося ви повернетеся.", false);
            } catch (Exception e) {
                if (e.getMessage().contains("bot was kicked")) {
                    chatService.delete(chat);
                } else {
                    update.getMessage().getFrom().setId(left.getId());
                    CallerUser user = userService.getByUserIdAndCallerChat(left.getId(), chat);
                    log.info("Into someOneLeft before deleting user wth user {}", user.toString());
                    userService.delete(user);
                }
            }
        }
    }

    private void someOneEntered(Update update) {
        List<User> users = update.getMessage().getNewChatMembers();
        for (User u : users) {
            SendMessage sendMessage = new SendMessage();
            if (u.getIsBot()) {
                if (u.getUserName().equals(botUsername)) {
                    sendMessage.setText("Привіт! ☀\uFE0F \n" +
                            "Дякую, що додали мене в групу :)\n" +
                            "\uD83D\uDD39Щоб додати ім'я, напишіть '<b><u>Кликун додай {ім'я}</u></b>'. \n" +
                            "\uD83D\uDD39Видалити - '<b><u>Кликун видали {ім'я}</u></b>'. \n" +
                            "\uD83D\uDD39Переглянути імена - '<b><u>Кликун покажи</u></b>'. \n" +
                            "\uD83D\uDD39‼\uFE0F<u>Щоб додати імена іншим - використовуйте \"!\"</u>‼\uFE0F\n" +
                            "\uD83D\uDD39Повна інструкція <b><u><a href='"+helpUrl+"'>тут.</a></u></b>\n" +
                            "\uD83D\uDD39У випадку питань чи пропозицій писати сюди: @caller_ua_bot_support\n\n" +
                            "Для роботи Кликуна надайте права адміністратора та можливість видаляти повідомлення. \n" +
                            "Приємного спілкування!\uD83E\uDEF6");
                    messageService.sendMessage(MessageService.MY_ID, "Мене додали в нову групу!!!!", false);
                } else {
                    continue;
                }

            } else {
                sendMessage.setText("<b>Привіт " + CommandUtils.deleteBadSymbols(u.getFirstName()) + "!</b>\n" +
                        "Я Кликун - бот який буде кликати вас щоразу, коли хтось буде писати ваше ім'я.\n" +
                        "\uD83D\uDD39Щоб додати ім'я, напишіть '<b><u>Кликун додай {ім'я}</u></b>'. \n" +
                        "\uD83D\uDD39Видалити - '<b><u>Кликун видали {ім'я}</u></b>'. \n" +
                        "\uD83D\uDD39Переглянути імена - '<b><u>Кликун покажи</u></b>'. \n\n" +
                        "<b>Приємного спілкування!</b>");
            }
            sendMessage.setParseMode("html");
            sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            messageService.sendMessage(sendMessage);
            return;
        }
    }
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public static void setCustomRoles() {
        for (Long id: creatorList) {
            for(CallerUser user: userService.getAllByUserId(id)) {
                user.setRole(CallerRoles.CREATOR);
                userService.save(user);
            }
        }

        for (Long id: moderList) {
            for(CallerUser user: userService.getAllByUserId(id)) {
                user.setRole(CallerRoles.MODERATOR);
                userService.save(user);
            }
        }
    }

}
