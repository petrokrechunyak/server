package com.alphabetas.bot.commands;


import com.alphabetas.bot.comparator.GroupNameStringLengthComparator;
import com.alphabetas.bot.comparator.NameStringLengthComparator;
import com.alphabetas.bot.model.*;
import com.alphabetas.bot.model.enums.StatsCountType;
import com.alphabetas.bot.repo.RoleplayRepo;
import com.alphabetas.bot.service.StatsCountService;
import com.alphabetas.bot.service.impl.StatsCountServiceImpl;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.utils.CommandUtils.makeLink;

@Slf4j
@Component
@Scope("prototype")
public class NoCommand extends Command {

    @Autowired
    private RoleplayRepo roleplayRepo;

    public static final Map<String, String> rpCommands = new HashMap<>() {{
        put("запросити на чай", "%s запросив(ла) на чай %s \uD83E\uDED6\uD83C\uDF6A");
        put("обійняти", "%s обійняв(ла) %s \uD83E\uDEC2");
        put("кохатися", "%s покохався(лася) з %s ❤\u200D\uD83D\uDD25");
        put("поцілувати", "%s поцілував(ла) \uD83D\uDE18 %s");
        put("посадити на гіляку", "%s посадив(ла) на гіляку %s \uD83D\uDE08\uD83E\uDEB5");
        put("вдарити", "%s вдарив(ла) \uD83E\uDD1C\uD83E\uDD15 %s");
        put("вкусити", "%s вкусив(ла) %s \uD83E\uDEE6");
        put("погодувати", "%s погодував(ла) %s \uD83C\uDF7D");
        put("потиснути руку", "%s потиснув(ла) руку \uD83E\uDD1D\uD83E\uDD1D %s");
        put("тягнути за вухо", "%s потягнув(ла) за вухо \uD83D\uDC42\uD83E\uDD0F %s");
        put("дати п'ять", "%s дав(ла) п'ять \uD83D\uDC4B %s");
        put("похвалити", "%s похвалив(ла) %s \uD83E\uDD17");
        put("погладити", "%s погладив(ла) %s ☺️");
        put("випити кров", "%s випив(ла) кров у %s \uD83E\uDE78");
        put("прочитати думки", "%s прочитав думки у %s \uD83E\uDEAC");
        put("чихнути", "%s чихнув(ла) на %s \uD83E\uDDA0");
        put("зненавидіти", "%s зненавидіти(ла) %s \uD83D\uDEA9");
        put("привітати", "%s привітав(ла) %s \uD83C\uDF82");
        put("записати в дез ноут", "%s записав(ла) в дез ноут %s \uD83D\uDCD3");
        put("звабити", "%s звабив(ла) %s \uD83D\uDE0F");
        put("пожаліти", "%s пожалів(ла) %s \uD83D\uDE22");
        put("знешкодити", "%s знешкодив(ла) %s \uD83D\uDC4E");
        put("вбити", "%s вбив(ла) %s ☠️");
        put("налякати", "%s налякав(ла) %s \uD83D\uDE40");
        put("обдурити", "%s обдурив(ла) %s \uD83E\uDD21");
        put("дмухнути", "%s дмухнув(ла) на %s \uD83D\uDE2E\u200D\uD83D\uDCA8");
        put("показати язик", "%s показав(ла) язик %s \uD83D\uDE1B");
        put("скуштувати", "%s скуштував(ла) %s \uD83E\uDD5F");
        put("допомогти", "%s допоміг(ла) %s \uD83E\uDD32");
        put("закінчити в", "%s закінчив(ла) у %s \uD83D\uDCA6");
        put("зґвалтувати", "%s зґвалтував(ла) %s \uD83D\uDC49\uD83D\uDC4C");
        put("віддатися", "%s віддався(лась) %s \uD83E\uDD2D");
        put("споїти", "%s споїв(ла) %s \uD83E\uDD74");
        put("напитись з", "%s напився(лась) з %s \uD83E\uDD74");
        put("заїбатися", "%s заїбався(лась) через %s \uD83E\uDD74");
        put("лизнути", "%s лизнув(ла) %s \uD83D\uDC45");
        put("трахнути", "%s трахнув(ла) %s \uD83D\uDC49\uD83D\uDC4C\uD83D\uDCA6");
        put("ляснути по попі", "%s ляснув(ла) по попі %s \uD83D\uDD90\uD83C\uDFFB \uD83C\uDF51");
        put("тріснути", "%s тріснув(ла) %s 😡");
        put("прошепотіти", "%s прошепотів(ла) на вухо %s 🤫");
        put("втопити", "%s втопив(ла) %s 🌊");
        put("тицьнути", "%s тицьнув(ла) %s 🤏");
    }};

    @Autowired
    private StatsCountService statsCountServiceImpl;

    private static boolean contains(String text, String toSearch) {
        text = text.toLowerCase();
        toSearch = toSearch.toLowerCase();
        String exceptWords = "[^" + CommandUtils.TEMPLATE_REGEX + "]";
        String pattern = exceptWords + toSearch + exceptWords;
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    private static String replace(String text, String toSearch, Name name) {
        toSearch = toSearch.toLowerCase();
        String exceptWords = "[^" + CommandUtils.TEMPLATE_REGEX + "]";
        Pattern pattern = Pattern.compile("(" + exceptWords + toSearch + exceptWords + ")", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text.toLowerCase());
        while (matcher.find()) {
            String old = matcher.group(1).toLowerCase();
            text = StringUtils.replaceIgnoreCase(text, old,
                    old.charAt(0) +
                            makeLink(
                                    name.getCallerUser().getUserId(),
                                    name.getName()) +
                            old.charAt(old.length() - 1));
        }
        return text;
    }


    @Override
    public int execute(Update update) {
        //check for rp commands
        if (chat.getConfig().isRpEnabled()) {
            checkForRp();
        }
        // check for aggression
        checkForMention();

        // call user, if his name is in the message
        callUser();
        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[] {"NoCommand"};
    }

    private void checkForMention() {
        if (update.getMessage().getFrom().getIsBot()) {
            return;
        }
        List<MessageEntity> entities = update.getMessage().getEntities();
        if (entities == null) {
            return;
        }
        for (MessageEntity entity : entities) {
            String type = entity.getType();
            boolean mention = type.equals("mention");
            boolean text_mention = type.equals("text_mention");
            if (!mention && !text_mention) {
                continue ;
            }

            CallerUser mentionedUser;
            if(mention) {
                mentionedUser = userService.getByUsernameAndCallerChat(entity.getText().replace("@", ""), chat);
                if(mentionedUser == null) {
                    continue;
                }
            } else {
                mentionedUser = userService.getByUserIdAndCallerChat(entity.getUser().getId(), chat);
            }

            chat.incrementSimpleCalls();
            statsCountServiceImpl.incrementStats(update.getMessage().getFrom().getId(), chat, StatsCountType.TG_MENTION);
            chatService.save(chat);
            if (mentionedUser.getNames().isEmpty()) {
                if (chat.getConfig().isAggressionEnabled()) {
                    messageService.sendMessage("Здається у користувача " + mentionedUser.getMentionedUser() + " немає імен!\n" +
                            "Але їх завжди можна додати командою Кликун додай {ім'я}");
                }
            } else {
                StringBuilder builder = new StringBuilder("<b>У користувача " + mentionedUser.getMentionedUser() + " є імена: \n</b>");
                for(Name names: mentionedUser.getNames()) {
                    builder.append(names.getName()).append("\n");
                }
                builder.append("\nСпробуйте вказати ім'я людини замість собачки.");
                messageService.sendMessage(builder.toString());
            }
            break;
        }
    }

    private void checkForRp() {
        String[] args = msgText.split("\n");
        RoleplayCommand roleplayCommand = roleplayRepo.getByPhrase(args[0].trim().toLowerCase());
        if (roleplayCommand == null || repliedMessage == null) {
            return;
        }

        String replyMessage = null;
        StringBuilder builder = new StringBuilder(roleplayCommand.getReplyPhrase());
        replyMessage = String.format(builder.toString(), user.getMentionedUser(), repliedUser.getMentionedUser());
        User to = repliedMessage.getFrom();
        if(roleplayCommand.isAdultOnly()) {
            if(!chat.getConfig().isAllowAdult()) {
                return;
            }
        }
        if ((to.getUserName() != null && to.getUserName().equals("caller_ua_bot")) && !roleplayCommand.isCallerLikesIt()) {
            builder.append("\nЗі словами: <b>\"Навіть не старайся!</b>\"");
            replyMessage = String.format(builder.toString(), repliedUser.getMentionedUser(), user.getMentionedUser());
        } else if (args.length > 1 && !to.getIsBot()) {
            builder.append("\nЗі словами: \"<b>")
                    .append(StringUtils.replaceIgnoreCase(update.getMessage().getText(),
                            args[0] + "\n", ""))
                    .append("</b>\"");
            replyMessage = String.format(builder.toString(), user.getMentionedUser(), repliedUser.getMentionedUser());
        }
        // if forever alone
        if (to.getId().equals(user.getUserId())) {
            messageService.sendMessage("Так не можна!");
            return;
        }
        // if user is bot
        if (to.getIsBot() && !to.getUserName().equals("caller_ua_bot")) {
            messageService.sendMessage("Рольові-комадни не можна виконувати з ботами");
            return;
        }

        roleplayCommand.setUses(roleplayCommand.getUses()+1);
        roleplayRepo.save(roleplayCommand);
        messageService.sendMessage(replyMessage);

    }

    public void callUser() {
        String msgText = " " + update.getMessage().getText() + " ";
        boolean send = false;
        chat.setNames(chat.getNames()
                .stream()
                .sorted(new NameStringLengthComparator())
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        chat.setGroupNames(chat.getGroupNames()
                .stream()
                .sorted(new GroupNameStringLengthComparator())
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        if(msgText.contains("$")) {
            for(GroupName groupName: chat.getGroupNames()) {
                String full = "$" + CommandUtils.decryptSpace(groupName.getName());
                if(StringUtils.containsIgnoreCase(msgText, full)) {
                    StringBuilder mentions = new StringBuilder("$" + CommandUtils.decryptSpace(groupName.getName()) + ": ");
                    for(CallerUser user1: groupName.getUsers()) {
                        mentions.append(makeLink(user1.getUserId(),
                                user1.getNames().isEmpty()
                                        ? user1.getFirstname()
                                        : CommandUtils.decryptSpace(user1.getNames().stream().findFirst().get().getName()))).append(", ");
                    }
                    mentions.delete(mentions.length() - 2, mentions.length());
                    msgText = StringUtils.replaceIgnoreCase(msgText, full, mentions.toString());
                    send = true;
                }
            }
        }

        for (Name name : chat.getNames()) {
            if (name.getCallerUser().getUserId().equals(update.getMessage().getFrom().getId())) {
                continue;
            }
            if (update.getMessage().getReplyToMessage() != null) {
                if (name.getCallerUser().getUserId().equals(update.getMessage().getReplyToMessage().getFrom().getId())) {
                    continue;
                }
            }
            String decrypted = CommandUtils.decryptSpace(name.getName());
            if (contains(msgText, decrypted)) {
                statsCountServiceImpl.incrementStats(name.getUserId(), chat, StatsCountType.MENTION);
                msgText = replace(msgText, decrypted, name);
                send = true;
            }
        }

        if (send) {
            statsCountServiceImpl.incrementStats(update.getMessage().getFrom().getId(), chat, StatsCountType.CALLING);
            msgText = CommandUtils.decryptSpace(msgText);
            SendMessage sendMessage = new SendMessage(chat.getId().toString(), msgText);
            sendMessage.enableHtml(true);
            sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
            sendMessage.setMessageThreadId(threadId);
            messageService.sendMessage(sendMessage);
            chat.incrementCallerCalls();
            chatService.save(chat);
        }
    }
}
