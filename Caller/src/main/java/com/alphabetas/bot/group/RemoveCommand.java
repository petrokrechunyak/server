package com.alphabetas.bot.group;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.utils.CommandUtils;
import com.alphabetas.bot.utils.GroupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.*;

@Slf4j
@Component
@Scope("prototype")
public class RemoveCommand extends Command {

    @Override
    public int execute(Update update) {
        if (user.getRole().getRoleNumber() < 2) {
            messageService.sendMessage("Групові імена можуть видаляти лише адміністратори!");
            return NOT_ENOUGH_RIGHTS.getReturnCode();
        }

        String[] args = CommandUtils.getNamesFromMessage(CommandUtils.trimMessage(msgText, getSpecialArgs()));
        args = Arrays.stream(args).distinct().toArray(String[]::new);

        if(args.length == 1 && args[0].isEmpty()) {
            messageService.sendMessage("Для видалення групового імені напишіть ім'я разом із командою 'Кликун стри {ім'я}'");
            return USE_NAME_WITH_COMMAND.getReturnCode();
        }

        String resultText = GroupUtils.removeGroup(args, user, chat);
        messageService.sendMessage(resultText);
        return SUCCESS.getReturnCode();
    }


    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/remove", "стерти", "стри"};
    }
}
