package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.RoleplayCommand;
import com.alphabetas.bot.repo.RoleplayRepo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;

@Slf4j
@Component
@Scope("prototype")
public class RPCommand extends Command {

    @Setter
    @Autowired
    private RoleplayRepo repo;
    @Override
    public int execute(Update update) {
        List<RoleplayCommand> rpCommands;
        if(chat.getConfig().isAllowAdult()) {
            rpCommands = repo.findAll();
        } else {
            rpCommands = repo.getAllByAdultOnlyIsFalse();
        }

        StringBuilder builder = new StringBuilder("<b>Рольові команди: </b>\n\n");
        for (RoleplayCommand command: rpCommands) {
            builder.append(command.getPhrase()).append("\n");
        }

        messageService.sendMessage(builder.toString());
        return SUCCESS.getReturnCode();
    }



    @Override
    public String[] getSpecialArgs() {
        return new String[]{"рп"};
    }
}
