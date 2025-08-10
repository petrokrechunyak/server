package com.alphabetas.bot.callback;

import com.alphabetas.bot.marriage.model.MarriageModel;
import com.alphabetas.bot.model.CallerUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.alphabetas.bot.utils.ServiceUtils.marriageService;

@Component
@Scope("prototype")
@Slf4j
public class MarriageCallback extends CallBack{

    public static final String MARRIAGE = "MARRIAGE";
    private static final String MARRIAGE_CREATED = "Шановні гості, дорога родино, вітаємо молодят %s та %s з шлюбом \uD83D\uDC8D";

    @Override
    public void execute(Update update) {
        String[] params = data.split("\\.");
        // MARRIAGE.repliedUser.answer.userId.messageId
        boolean answer = Boolean.parseBoolean(params[2]);
        Long repliedUserId = Long.parseLong(params[1]);
        CallerUser repliedUser = userService.getByUserIdAndCallerChat(repliedUserId, chat);
        CallerUser user = userService.getByUserIdAndCallerChat(Long.parseLong(params[3]), chat);

        if(!repliedUser.getUserId().equals(update.getCallbackQuery().getFrom().getId()) && !(!answer && user.getUserId().equals(update.getCallbackQuery().getFrom().getId()))) {
            messageService.sendAnswerCallback("Це повідомлення було адресоване не вам!", update);
            return;
        }
        if (answer) {
            MarriageModel current = marriageService.findByUserIdAndChat(user.getUserId(), chat);
            if(current != null) {
                messageService.deleteMessage(update.getCallbackQuery().getMessage().getMessageId());
                messageService.sendMessage(repliedUser.getMentionedUser() + " співчуваємо, ви запізнилися. Користувач " + user.getMentionedUser() + " вже знаходиться в шлюбі");
                return;
            }
            MarriageModel marriage = createMarriage(user.getUserId(), repliedUser.getUserId());
            marriageService.save(marriage);
            messageService.sendMessage(String.format(MARRIAGE_CREATED, user.getMentionedUser(), repliedUser.getMentionedUser()));
            messageService.deleteMessage(update.getCallbackQuery().getMessage().getMessageId());

        } else {
            if(user.getUserId().equals(update.getCallbackQuery().getFrom().getId())) {
                messageService.deleteMessage(update.getCallbackQuery().getMessage().getMessageId());
            } else {
                messageService.sendMessage(user.getMentionedUser() + " на жаль, вам відмовили, пощастить наступного разу, але не з цією людиною");
                messageService.deleteMessage(update.getCallbackQuery().getMessage().getMessageId());
            }
        }
    }

    private MarriageModel createMarriage(Long user, Long user2Id) {
        MarriageModel marriageModel = new MarriageModel(user, user2Id, chat);
        marriageModel.setStartDate(System.currentTimeMillis());
        userService.getByUserIdAndCallerChat(user2Id, chat);
        return marriageModel;
    }

    @Override
    public String getSpecialArg() {
        return MARRIAGE;
    }
}
