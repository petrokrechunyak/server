package com.alphabetas.bot.group;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.utils.CommandUtils;
import com.alphabetas.bot.utils.GroupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.USE_NAME_WITH_COMMAND;

@Slf4j
@Component
@Scope("prototype")
public class LeaveCommand extends Command {

    @Override
    public int execute(Update update) {

        String[] args = CommandUtils.getNamesFromMessage(CommandUtils.trimMessage(msgText, getSpecialArgs()));
        args = Arrays.stream(args).distinct().toArray(String[]::new);

        if(args.length == 1 && args[0].isEmpty()) {
            messageService.sendMessage("Напишіть групове ім'я, з якого хочете вийти разом із командою 'Кликун покинути {ім'я}'");
            return USE_NAME_WITH_COMMAND.getReturnCode();
        }

        String resultText = GroupUtils.leaveGroup(args, user, chat);
        messageService.sendMessage(resultText);
        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/leave", "покинути", "вийти", "залишити"};
    }
}
