package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.Name;
import com.alphabetas.bot.model.StatsCount;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.USE_NAME_WITH_COMMAND;
import static com.alphabetas.bot.utils.ServiceUtils.nameService;

@Component
@Slf4j
@Scope("prototype")
public class DeleteCommand extends Command {


    @Override
    public int execute(Update update) {
        // delete message if starts with /

        String[] args = CommandUtils.getNamesFromMessage(CommandUtils.trimMessage(msgText, getSpecialArgs()));
        args = Arrays.stream(args).distinct().toArray(String[]::new);

        if(args.length == 1 && args[0].isEmpty()) {
            messageService.sendMessage("Для видалення імені вкажіть ім'я разом із командою 'Кликун видали {ім'я}'");
            return USE_NAME_WITH_COMMAND.getReturnCode();
        }

        String localUser;
        if (msgText.startsWith("!")) {
            localUser = deleteNames(args, repliedUser);
        } else {
            localUser = deleteNames(args, user);
        }

        messageService.sendMessage(localUser);
        return SUCCESS.getReturnCode();
    }

    public String deleteNames(String[] args, CallerUser user) {

        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            arg = CommandUtils.encryptSpace(arg);
            Name name = nameService.getByCallerChatAndName(chat, arg);
            arg = CommandUtils.decryptSpace(arg);
            if (name != null) {
                builder.append("Ім'я <b><u>").append(arg).append("</u></b> ");
                if (name.getCallerUser().equals(user)) {
                    nameService.delete(name);
                    user.getNames().remove(name);
                    builder.append("успішно видалено!");
                } else {
                    if(msgText.startsWith("!")) {
                        nameService.delete(name);
                        user.getNames().remove(name);
                        builder.append("успішно видалено!");
                    } else {
                        builder.append("зайнято іншою людиною. Чужі імена видаляти не можна!");
                    }
                }
            } else {
                builder.append("У цьому чаті немає імені ").append("<b><u>").append(arg).append("</u></b>");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/delete_names", "/delete_name", "/delete", "видалити", "видали",};
    }

}
