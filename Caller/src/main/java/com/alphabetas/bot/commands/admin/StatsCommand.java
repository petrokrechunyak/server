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
import static com.alphabetas.bot.service.MessageService.MY_ID;

@Component
@Scope("prototype")
@Slf4j
public class StatsCommand extends Command {

    @Override
    public int execute(Update update) {
        if(chat.getId().equals(MY_ID)) {
            List<CallerChat> all = chatService.findAll();
            int callerCalls = 0;
            int simpleCalls = 0;
            for (CallerChat chat: all) {
                callerCalls += chat.getCallerCalls();
                chat.setCallerCalls(0);
                simpleCalls += chat.getSimpleCalls();
                chat.setSimpleCalls(0);
                chatService.save(chat);

            }
            messageService.sendMessage("Загальна кількість чатів з Кликуном: <b>" + all.size() + "</b>\n" +
                    "Кількість закликів через Кликуна: <b>" + callerCalls + "</b>\n" +
                    "Кількість закликів через собачку: <b>" + simpleCalls + "</b>");
            return SUCCESS.getReturnCode();
        }

        return UNKNOWN_COMMAND.getReturnCode();


    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/stats"};
    }
}
