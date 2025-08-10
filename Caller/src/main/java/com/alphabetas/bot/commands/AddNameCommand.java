package com.alphabetas.bot.commands;

import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.Name;
import com.alphabetas.bot.utils.CommandUtils;
import com.alphabetas.bot.utils.GroupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.Set;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.*;
import static com.alphabetas.bot.utils.CommandUtils.NAME_WITH_SPACES_REGEX;
import static com.alphabetas.bot.utils.CommandUtils.SINGLE_WORD_REGEX;
import static com.alphabetas.bot.utils.ServiceUtils.groupNameService;
import static com.alphabetas.bot.utils.ServiceUtils.nameService;

@Slf4j
@Component
@Scope("prototype")
public class AddNameCommand extends Command {

    public static final String LIMIT_ACHIEVED = "Досягнуто ліміту на імена: ";
    public static final String ALREADY_TAKEN = "вже зайнято! Власник - ";
    public static final String TOO_SMALL = "занадто мале! Мінімальна кількість символів: <u>3</u>";
    public static final String BLOCKED_NAME = "заблоковане! Ніхто не може його додати.";
    public static final String BLOCKED_SYMBOLS = "не може бути додано через через заборонені символи.";


    @Override
    public int execute(Update update) {

        if(repliedMessage != null && msgText.startsWith("!")) {
            if (repliedMessage.getFrom().getIsBot()) {
                messageService.sendMessage("У ботів не може бути імен :/");
                return IMPOSSIBLE_TO_USE_WITH_BOTS.getReturnCode();
            }
        }

        String[] args = CommandUtils.getNamesFromMessage(CommandUtils.trimMessage(msgText, getSpecialArgs()));
        args = Arrays.stream(args).distinct().toArray(String[]::new);

        if(args.length == 1 && args[0].isEmpty()) {
            messageService.sendMessage("Для додавання імені до списку закликів вкажіть ім'я разом із командою 'Кликун додай {ім'я}'");
            return USE_NAME_WITH_COMMAND.getReturnCode();
        }

        String localUser;
        if (msgText.startsWith("!") && repliedUser != null) {
            localUser = saveNames(args, repliedUser);
        } else {
            localUser = saveNames(args, user);
        }

        messageService.sendMessage(localUser);
        return SUCCESS.getReturnCode();
    }

    public String saveNames(String[] args, CallerUser user) {

        StringBuilder builder = new StringBuilder();

        for (String arg : args) {
            if(groupNameService.getByNameAndChat(CommandUtils.encryptSpace(arg), chat) != null) {
                GroupUtils.joinGroup(new String[]{arg}, user, chat);
                builder.append("Групове ім'я <b><u>").append(arg).append("</u></b> успішно додано!\n");
                continue;
            }
            Set<Name> nameSet = nameService.getAllByCallerUser(user);
            if (nameSet.size() >= chat.getConfig().getNameLimit()) {
                builder.append(LIMIT_ACHIEVED)
                        .append(chat.getConfig().getNameLimit())
                        .append(". Ім'я <b><u>")
                        .append(arg)
                        .append("</u></b> не додано!\n");
                continue;
            }
            builder.append("Ім'я <b><u>").append(arg).append("</u></b> ");
            String currentRegex = chat.getConfig().isAllowSpace()
                    ? NAME_WITH_SPACES_REGEX
                    : SINGLE_WORD_REGEX;
            if (arg.matches(currentRegex)) {
                arg = CommandUtils.encryptSpace(arg);
                Name name = nameService.getByCallerChatAndName(chat, arg);
                if (name != null) {
                    builder.append(ALREADY_TAKEN).append(name.getCallerUser().getMentionedUser());
                    builder.append("\n");
                    continue;
                }
                if (arg.length() < 3) {
                    builder.append(TOO_SMALL);
                    builder.append("\n");
                    continue;
                }
                String finalArg = arg;
                if (chat.getConfig().getBlockedNames().stream().anyMatch(x -> x.equalsIgnoreCase(finalArg))) {
                    builder.append(BLOCKED_NAME);
                    builder.append("\n");
                    continue;
                }
                arg = CommandUtils.encryptSpace(arg);
                name = new Name(user.getUserId(), chat, arg);
                user.getNames().add(name);
                nameService.save(name);
                userService.save(user);
                builder.append("успішно додано!");
            } else
                builder.append(BLOCKED_SYMBOLS);
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[] {"/add", "додати", "додай"};
    }
}
