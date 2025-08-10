package com.alphabetas.bot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.service.ScheduleService.checkForDelete;

@Slf4j
@Component
@Scope("prototype")
public class ClearCommand extends Command{

    @Override
    public int execute(Update update) {
        new Thread(() -> {
            int counter = checkForDelete(chat);
            messageService.sendMessage(chat.getId(), "Очищено імен: " + counter);
        }).start();
        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/clear", "очистити"};
    }
}
