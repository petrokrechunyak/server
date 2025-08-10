package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.GroupName;
import com.alphabetas.bot.model.Name;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.*;
import static com.alphabetas.bot.utils.CommandUtils.decryptSpace;

@Component
@Slf4j
@Scope("prototype")
public class ShowCommand extends Command {

    @Override
    public int execute(Update update) {
        // delete message if starts with /


        String sendMessage = null;
        if (repliedMessage != null) {
            if (repliedMessage.getFrom().getIsBot()) {
                if (repliedMessage.getFrom().getUserName().equals("caller_ua_bot")) {
                    messageService.sendMessage("У мене тільки одне ім'я - Кликун ;)");

                    return CANT_INTERACT_WITH_CALLER.getReturnCode();
                }
                messageService.sendMessage("У ботів не може бути імен :/");
                return IMPOSSIBLE_TO_USE_WITH_BOTS.getReturnCode();
            } else {
                sendMessage = getNames(repliedUser);
            }
        } else {
            sendMessage = getNames(user);
        }

        if (msgText.contains("@")) {
           msgText = msgText.split("@")[1];
           if (!msgText.equals("caller_ua_bot") && !msgText.equals("bunker_ua_bot")) {
               CallerUser callerUser = userService.getByUsernameAndCallerChat(msgText, chat);
               sendMessage = getNames(callerUser);
           }
        }

        messageService.sendMessage(sendMessage);
        return SUCCESS.getReturnCode();
    }

    private String getNames(CallerUser user) {
        StringBuilder builder = new StringBuilder();
        if (user.getNames().isEmpty() && user.getGroupNames().isEmpty()) {
            builder.append("У користувача ").append(user.getMentionedUser()).append(" ще немає імен, але їх завжди можна додати командою 'Кликун додай {ім'я}'");
        } else {
            builder.append("<b>Імена ")
                    .append(user.getMentionedUser())
                    .append("</b>\n");
            for (Name name : user.getNames()) {
                builder.append(decryptSpace(name.getName())).append("\n");
            }

            builder.append("\n<b>Групові імена: </b>\n");
            for (GroupName name : user.getGroupNames()) {
                builder.append(decryptSpace(name.getName())).append("\n");
            }

            builder.append("\n<b>Роль: </b>").append(user.getRole().getUkrName());
        }
        return builder.toString();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/show_names", "/show_name", "/show",
                "імена", "шов", "покажи"};
    }
}
