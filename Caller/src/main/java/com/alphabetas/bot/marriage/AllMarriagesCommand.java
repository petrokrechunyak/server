package com.alphabetas.bot.marriage;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.marriage.model.MarriageModel;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.utils.ServiceUtils.marriageService;

@Slf4j
@Component
@Scope("prototype")
public class AllMarriagesCommand extends Command {

    private Map<Integer, String> placesMap = new HashMap<>(){{
        put(1, "\uD83E\uDD47");
        put(2, "\uD83E\uDD48");
        put(3, "\uD83E\uDD49");
    }};

    @Override
    public int execute(Update update) {
        StringBuilder builder = new StringBuilder("\uD83D\uDC8D Топ шлюбів <b>")
                .append(CommandUtils.deleteBadSymbols(chat.getTitle()))
                .append(" </b>\uD83D\uDC8D\n\n");
        List<MarriageModel> marriageModelList = marriageService.findAllByChat(chat).stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < marriageModelList.size(); i++) {
            MarriageModel current = marriageModelList.get(i);
            builder.append("<b>").append(placesMap.getOrDefault(i+1, "  " + (i+1) + ". ")).append("</b> ")
                    .append(current.getUser1().getMentionedUser())
                    .append(" та ")
                    .append(current.getUser2().getMentionedUser())
                    .append(": ")
                    .append(getMarriageDuration(current.getStartDate()))
                    .append(" ")
                    .append(getHeart(current.getStartDate()))
                    .append("\n");
        }

        builder.append("\n").append("Щоб вступити в шлюб напишіть \".шлюб\"");
        Message m = messageService.sendMessage("Шлюби");
        messageService.editMessage(m.getMessageId().longValue(), builder.toString());
        return SUCCESS.getReturnCode();
    }

    private String getHeart(Long startDate) {
        Long currentDate = System.currentTimeMillis();
        Long duration = currentDate - startDate;

        long allSeconds = duration / 1000;
        long allMinutes = allSeconds / 60;
        int allHours = (int) allMinutes / 60;
        int allDays = allHours / 24;
        int allMonths = allDays / 30;
        int allYears = allMonths / 12;

        if(allDays < 1) {
            return "\uD83E\uDD0D"; // 🤍
        } else if (allDays < 7) {
            return "\uD83E\uDE75"; // 🩵
        } else if (allDays < 14) {
            return "\uD83D\uDC99"; // 💙
        } else if (allMonths < 1) {
            return "\uD83D\uDC9C"; // 💜
        } else if (allMonths < 3) {
            return "\uD83E\uDDE1"; // 🧡
        } else if (allMonths < 6) {
            return "\uD83E\uDE77"; // 🩷
        } else if (allYears < 1) {
            return "❤\uFE0F"; // ❤️
        } else {
            return "❤\uFE0F\u200D\uD83D\uDD25"; // ❤️‍🔥
        }
    }

    public static String getMarriageDuration(Long startDate) {
        Long currentDate = System.currentTimeMillis();
        Long duration = currentDate - startDate;

        long allSeconds = duration / 1000;
        long allMinutes = allSeconds / 60;
        int allHours = (int) allMinutes / 60;
        int allDays = allHours / 24;
        int allMonths = allDays / 30;
        int allYears = allMonths / 12;

        int intSeconds = (int) allSeconds % 60;
        int intMinutes = (int) allMinutes % 60;
        int intHours = allHours % 24;
        int intDays = allDays % 30;
        int intMonths = allMonths % 12;

        int[] time = new int[]{allYears, intMonths, intDays, intHours, intMinutes, intSeconds};
        String[][] names = new String[][]{
                {" рік ", " роки ", " років "},
                {" місяць ", " місяці ", " місяців "},
                {" день ", " дня ", " днів "},
                {" година ", " години ", " годин "},
                {" хвилина ", " хвилини ", " хвилин "},
                {" секунда ", " секунди ", " секунд "}
        };

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < time.length; i++) {
            if (time[i] > 0) {
                String[] localNames = names[i];
                int localTime = time[i] % 10;
                if (localTime == 1) {
                    if (time[i] > 10 && time[i] < 15) {
                        builder.append(time[i]).append(localNames[2]);
                    } else {
                        builder.append(time[i]).append(localNames[0]);
                    }
                } else if (localTime > 1 && localTime < 5) {
                    if (time[i] > 10 && time[i] < 15) {
                        builder.append(time[i]).append(localNames[2]);
                    } else {
                        builder.append(time[i]).append(localNames[1]);
                    }
                } else {
                    builder.append(time[i]).append(localNames[2]);
                }
            }
            if (!first) {
                break;
            }
            if (time[i] > 0) {
                first = false;
            }
        }
        return builder.toString();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"шлюби"};
    }
}
