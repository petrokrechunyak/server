package com.alphabetas.bot.group;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.model.GroupName;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;

@Slf4j
@Component
@Scope("prototype")
public class AllCommand extends Command {

    @Override
    public int execute(Update update) {
        log.info("Entered into AllCommand");

        StringBuilder builder = new StringBuilder("Всі групові імена чату <b>").append(CommandUtils.deleteBadSymbols(chat.getTitle())).append(":</b>\n");
        for(GroupName groupName: chat.getGroupNames()) {
            builder.append(CommandUtils.decryptSpace(groupName.getName())).append("\n");
        }

        messageService.sendMessage(builder.toString());
        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/all", "групові імена", "всі"};
    }

}
