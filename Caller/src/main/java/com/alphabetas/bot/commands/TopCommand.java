package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.MessageCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.stream.Collectors;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.utils.ServiceUtils.messageCountService;

@Slf4j
@Component
@Scope("prototype")
public class TopCommand extends Command {

    private Map<Integer, String> placesMap = new HashMap<>(){{
        put(1, "\uD83E\uDD47");
        put(2, "\uD83E\uDD48");
        put(3, "\uD83E\uDD49");
    }};

    @Override
    public int execute(Update update) {
        log.info("Entered into TopCommand");
        List<MessageCount> allInChat = messageCountService.getAllByChat(chat);
        Map<CallerUser, Integer> counter = new LinkedHashMap<>();
        for (MessageCount one : allInChat) {
            counter.put(one.getCallerUser(), counter.getOrDefault(one.getCallerUser(), 0) + one.getCount());
        }

        counter = counter.entrySet().stream().sorted(Map.Entry.comparingByValue((o1, o2) -> o2 - o1)).collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


        StringBuilder builder = new StringBuilder("Статистика по написаних повідомленнях за останню добу: \uD83D\uDCDD\n\n");
        int general = 0;
        int i = 1;

        Message message = messageService.sendMessage(builder.toString());

        for (Map.Entry<CallerUser, Integer> entries : counter.entrySet()) {
            builder.append(placesMap.getOrDefault(i, "  " + i + ". "))
                    .append(entries.getKey().getMentionedUser()).append(" — ")
                    .append(entries.getValue())
                    .append("\n");
            i++;
            general += entries.getValue();
        }

        builder.append("\nЗагальна кількість повідомлень: ").append(general);
        messageService.editMessage(message.getMessageId().longValue(), builder.toString());
        return SUCCESS.getReturnCode();
    }



    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/top", "топ"};
    }
}
