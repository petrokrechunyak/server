package com.alphabetas.bot.commands.admin;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.model.CallerChat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.UNKNOWN_COMMAND;

@Slf4j
@Component
@Scope("prototype")
public class TellCommand extends Command {


    @Override
    public int execute(Update update) {
        log.info("Entered into TellCommand");

        Long myId = 731921794L;
        if(!user.getUserId().equals(myId) || !chat.getId().equals(myId)) {
            return UNKNOWN_COMMAND.getReturnCode();
        }
        msgText = msgText.replace("/tell ", "");
        List<CallerChat> all = chatService.findAll();
        for(CallerChat current: all) {
            try {
                messageService.sendMessage(current.getId(), msgText, false);
            } catch (Exception e) {
                messageService.sendErrorMessage(e, update);
            }
        }

        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/tell"};
    }
}

