package com.alphabetas.bot.marriage;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.marriage.model.MarriageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.utils.CallbackUtils.buttonByTextAndCallBack;
import static com.alphabetas.bot.utils.CallbackUtils.createMarkupByButton;
import static com.alphabetas.bot.utils.ServiceUtils.marriageService;

@Slf4j
@Component
@Scope("prototype")
public class DivorceCommand extends Command {

    private static final String MARRIAGE_INVITE = "Ви впевнені що хочете розлучитися? ";
    @Override
    public int execute(Update update) {
        log.info("Entered in MarriageCommand with command {}", update.getMessage().getText());

        MarriageModel marriageModel = marriageService.findByUserIdAndChat(user.getUserId(), chat);
        if(marriageModel == null) {
            messageService.sendMessage("Ви не знаходитеся в шлюбі!");
        } else {
            sendMarriageDivorceMessage();
        }
        return SUCCESS.getReturnCode();
    }

    private void sendMarriageDivorceMessage() {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId());
        message.setText(String.format(MARRIAGE_INVITE));

        InlineKeyboardMarkup markup = createMarkupByButton(
                buttonByTextAndCallBack("Ні \uD83D\uDC9E", "DIVORCE.false." + user.getUserId()),
                buttonByTextAndCallBack("Так \uD83D\uDC94", "DIVORCE.true." + user.getUserId())
        );
        message.setReplyMarkup(markup);
        message.setReplyToMessageId(update.getMessage().getMessageId());
        messageService.sendMessage(message);
    }
    @Override
    public String[] getSpecialArgs() {
        return new String[]{"розлучення", "розлучитися"};
    }
}
