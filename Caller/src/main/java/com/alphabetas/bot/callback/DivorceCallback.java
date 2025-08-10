package com.alphabetas.bot.callback;

import com.alphabetas.bot.marriage.AllMarriagesCommand;
import com.alphabetas.bot.marriage.model.MarriageModel;
import com.alphabetas.bot.model.CallerUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.alphabetas.bot.utils.ServiceUtils.marriageService;

@Component
@Scope("prototype")
@Slf4j
public class DivorceCallback extends CallBack {

    public static final String DIVORCE = "DIVORCE";

    @Override
    public void execute(Update update) {
        String[] params = data.split("\\.");
        // DIVORCE.answer.userId
        boolean answer = Boolean.parseBoolean(params[1]);
        CallerUser user = userService.getByUserIdAndCallerChat(Long.parseLong(params[2]), chat);
        if(!user.getUserId().equals(update.getCallbackQuery().getFrom().getId())) {
            messageService.sendAnswerCallback("Це повідомлення було адресоване не вам!", update);
            return;
        }
        MarriageModel marriageModel = marriageService.findByUserIdAndChat(user.getUserId(), chat);
        if(answer) {
            CallerUser user2 = marriageModel.getUser1().equals(user) ? marriageModel.getUser2() : marriageModel.getUser1();
            marriageService.delete(marriageModel);
            messageService.sendMessage(user2.getMentionedUser() + " співчуваємо, ваш шлюб проіснував "
                    + AllMarriagesCommand.getMarriageDuration(marriageModel.getStartDate()) + ".\uD83D\uDC94 Не сумуйте, життя довге, а людей багато.");
        }
        messageService.deleteMessage(update.getCallbackQuery().getMessage().getMessageId());
    }

    @Override
    public String getSpecialArg() {
        return DIVORCE;
    }
}
