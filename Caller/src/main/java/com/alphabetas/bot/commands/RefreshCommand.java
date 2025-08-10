package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.enums.CallerRoles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;

import java.util.List;

import static com.alphabetas.bot.CallerBot.setCustomRoles;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;

@Slf4j
@Component
@Scope("prototype")
public class RefreshCommand extends Command {

    @Override
    public int execute(Update update) {
        List<ChatMember> admins = messageService.getAdmins();
        chat.getCallerUsers().forEach(x -> {
            boolean changed = false;
            if(x.getRole() == null) {
                x.setRole(CallerRoles.MEMBER);
            }
            for (ChatMember admin: admins) {
                // if admin
                if(admin.getUser().getId().equals(x.getUserId())) {
                    if(admin instanceof ChatMemberAdministrator) {
                        if(!x.getRole().equals(CallerRoles.TOP_ADMIN)) {
                            x.setRole(CallerRoles.LOWER_ADMIN);
                        }
                    } else if (admin instanceof ChatMemberOwner) {
                        x.setRole(CallerRoles.OWNER);
                    }
                    changed = true;
                }
                if(changed) {
                    userService.save(x);
                    break;
                }
            }
            if(!changed) {
                x.setRole(CallerRoles.MEMBER);
            }
            userService.save(x);
            setCustomRoles();
        });
        messageService.sendMessage("Ролі оновлено!");
        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/refresh"};
    }
}
