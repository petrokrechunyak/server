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
public class JoinCommand extends Command {

    private static final String[] createGroupMessages = new String[]{"Напишіть назву групового імені, до якого хочете приєднатися. Для відміни напишіть /cancel"};

    @Override
    public int execute(Update update) {

        String[] args = CommandUtils.getNamesFromMessage(CommandUtils.trimMessage(msgText, getSpecialArgs()));
        args = Arrays.stream(args).distinct().toArray(String[]::new);

        if(args.length == 1 && args[0].isEmpty()) {
            messageService.sendMessage("Для приєднання до групового імені вкажіть групове ім'я разом із командою 'Кликун приєднай {ім'я}'");

            return USE_NAME_WITH_COMMAND.getReturnCode();
        }

        String resultText = GroupUtils.joinGroup(args, user, chat);
        messageService.sendMessage(resultText);
        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/join", "приєднатися", "приєднай", "ввійти"};
    }
}
