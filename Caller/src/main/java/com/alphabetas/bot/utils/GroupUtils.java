package com.alphabetas.bot.utils;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.GroupName;
import com.alphabetas.bot.service.GroupNameService;
import lombok.Setter;

import java.util.Arrays;

import static com.alphabetas.bot.utils.ServiceUtils.groupNameService;

@Setter
public class GroupUtils {



    public static String createGroup(String[] args, CallerUser user, CallerChat chat) {

        args = Arrays.stream(args).distinct().toArray(String[]::new);

        StringBuilder builder = new StringBuilder();


        for(String arg: args) {
            String decrypted = arg;
            arg = CommandUtils.encryptSpace(arg);

            builder.append("Групове ім'я <b><u>").append(decrypted).append("</u></b> ");

            GroupName groupName = groupNameService.getByNameAndChat(arg, chat);
            if(groupName == null) {
                groupName = new GroupName(chat, arg);
                groupNameService.save(groupName);
                builder.append("успішно створено!\nПриєднатися до нього можна командою <b><u>Кликун приєднай ")
                        .append(decrypted).append("</u></b>");
            } else {
                builder.append("вже існує!\nПриєднатися до нього можна командою <u><b>Кликун приєднай ")
                        .append(decrypted).append("</b></u>");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String leaveGroup(String[] args, CallerUser user, CallerChat chat) {
        args = Arrays.stream(args).distinct().toArray(String[]::new);

        StringBuilder builder = new StringBuilder();

        for(String arg: args) {
            String decrypted = arg;
            arg = CommandUtils.encryptSpace(arg);

            GroupName groupName = groupNameService.getByNameAndChat(arg, chat);
            if(groupName != null) {
                if(groupName.getUsers().contains(user)) {
                    builder.append("Групове ім'я <b><u>").append(decrypted).append("</u></b> успішно покинуто");
                } else {
                    builder.append("Ви не входите до цього групового імені!");
                }
                groupName.getUsers().remove(user);
                groupNameService.save(groupName);
            } else {
                builder.append("Такого групового імені не існує!");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String joinGroup(String[] args, CallerUser user, CallerChat chat) {
        args = Arrays.stream(args).distinct().toArray(String[]::new);
        StringBuilder builder = new StringBuilder();

        for(String arg: args) {
            String decrypted = arg;
            String boldArg = "<b><u>" + arg + "</u></b>";
            arg = CommandUtils.encryptSpace(arg);

            GroupName groupName = groupNameService.getByNameAndChat(arg, chat);
            if(groupName != null) {
                if(groupName.getUsers().contains(user)) {
                    builder.append("Ви вже входите до групового імені ").append(boldArg);
                } else {
                    builder.append("Групове ім'я ").append(boldArg).append(" успішно додано");
                }
                groupName.getUsers().add(user);
                groupNameService.save(groupName);
            } else {
                builder.append("Групового імені ").append(boldArg).append(" не існує! ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String removeGroup(String[] args, CallerUser user, CallerChat chat) {
        args = Arrays.stream(args).distinct().toArray(String[]::new);

        StringBuilder builder = new StringBuilder();

        for(String arg: args) {
            String decrypted = arg;
            arg = CommandUtils.encryptSpace(arg);

            builder.append("Групове ім'я <b><u>").append(decrypted).append("</u></b> ");

            GroupName groupName = groupNameService.getByNameAndChat(arg, chat);
            if(groupName != null) {
                groupNameService.delete(groupName);
                builder.append("успішно видалено!");
            } else {
                builder.append("не існує!");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
