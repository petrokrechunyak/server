package com.alphabetas.bot.marriage;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.marriage.model.MarriageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.*;
import static com.alphabetas.bot.utils.CallbackUtils.buttonByTextAndCallBack;
import static com.alphabetas.bot.utils.CallbackUtils.createMarkupByButton;
import static com.alphabetas.bot.utils.ServiceUtils.marriageService;

@Slf4j
@Component
@Scope("prototype")
public class MarriageCommand extends Command {

    private static final String NO_REPLY_MESSAGE = "Для вступу у шлюб напишіть повідомлення у відповідь на повідомлення іншої людини";

    private static final String USER1_ALREADY_MARRIED = "Ви вже знаходитесь у шлюбі з %s. Два шлюба одночасно - це неприйнятно!";
    private static final String USER2_ALREADY_MARRIED = "Користувач %s вже знаходиться у шлюбі. Пошукайте собі когось іншого.";
    private static final String MARRIAGE_INVITE = "Користувач %s зробив пропозицію руки та серця %s";

    @Override
    public int execute(Update update) {
        log.info("Entered in MarriageCommand with command {}", update.getMessage().getText());

        if(repliedMessage != null) {

            if(repliedMessage.getFrom().getId().equals(user.getUserId())) {
                messageService.sendMessage("Вступати в шлюб з собою не можна!");

                return CANT_MARRY_YOURSELF.getReturnCode();
            }

            if(repliedMessage.getFrom().getIsBot()) {
                if(repliedMessage.getFrom().getUserName().equals("caller_ua_bot")) {
                    messageService.sendMessage("Мені не потрібна пара!");
                } else {
                    messageService.sendMessage("З ботами вступати в шлюб не можна!");
                }
                return IMPOSSIBLE_TO_USE_WITH_BOTS.getReturnCode();
            }

            if(checkIfMarried(user.getUserId(), true) || checkIfMarried(repliedUser.getUserId(), false)) {
                return ALREADY_MARRIED.getReturnCode();
            }
            sendMarriageInviteMessage();

        } else {
            messageService.sendMessage(NO_REPLY_MESSAGE);
        }
        return SUCCESS.getReturnCode();
    }

    private void sendMarriageInviteMessage() {
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId());
        message.setText(String.format(MARRIAGE_INVITE, user.getMentionedUser(), repliedUser.getMentionedUser()));
        message.setReplyToMessageId(repliedMessage.getMessageId());

        InlineKeyboardMarkup markup = createMarkupByButton(
                buttonByTextAndCallBack("Відхилити \uD83D\uDC94", "MARRIAGE." + repliedUser.getUserId() + ".false." + user.getUserId() + "." + update.getMessage().getMessageId()),
                buttonByTextAndCallBack("Прийняти \uD83D\uDC9E", "MARRIAGE." + repliedUser.getUserId() + ".true." + user.getUserId() + "." + update.getMessage().getMessageId())
        );
        message.setReplyMarkup(markup);
        messageService.sendMessage(message);

    }

    private boolean checkIfMarried(Long userId, boolean user1) {
        MarriageModel marriageModel = marriageService.findByUserIdAndChat(userId, chat);
        if(marriageModel != null) {
            String pattern = user1 ?
                    USER1_ALREADY_MARRIED : USER2_ALREADY_MARRIED;
            String userName = user1 ?
                    (userId.equals(marriageModel.getUser1().getUserId()) ?
                    marriageModel.getUser2().getMentionedUser() :
                    marriageModel.getUser1().getMentionedUser())
                    : (repliedUser.getMentionedUser());
            messageService.sendMessage(String.format(pattern, userName));
            return true;
        }
        return false;
    }


    @Override
    public String[] getSpecialArgs() {
        return new String[]{"шлюб"};
    }
}
