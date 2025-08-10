package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.enums.CallerRoles;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.NOT_ENOUGH_RIGHTS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.utils.ServiceUtils.configService;

@Slf4j
@Component
@Scope("prototype")
public class UnblockNameCommand extends Command {
    @Override
    public int execute(Update update) {
        if (user.getRole().getRoleNumber() < CallerRoles.LOWER_ADMIN.getRoleNumber()) {
            messageService.sendMessage("Розблоковувати імена можуть лише адміністратори!");
            return NOT_ENOUGH_RIGHTS.getReturnCode();
        }
            String[] args = CommandUtils.getNamesFromMessage(CommandUtils.trimMessage(msgText, getSpecialArgs()));
            messageService.sendMessage(unblockNames(args));
            return SUCCESS.getReturnCode();
    }

    public String unblockNames(String[] args) {

        StringBuilder builder = new StringBuilder();

        Arrays.stream(args).distinct().forEach(x -> {
            builder.append("Ім'я <b><u>");
            if(chat.getConfig().getBlockedNames().stream().noneMatch(x::equalsIgnoreCase)) {
                builder.append(x).append("</u></b> не є заблокованим.\n");
            } else {
                builder.append(x).append("</u></b> успішно розблоковано!\n");
                chat.getConfig().getBlockedNames().removeIf(y -> y.equalsIgnoreCase(x));
            }
        });
        configService.save(chat.getConfig());
        return builder.toString();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"розблокуй", "розблокувати"};
    }
}
