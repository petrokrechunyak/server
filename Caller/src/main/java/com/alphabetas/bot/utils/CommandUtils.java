package com.alphabetas.bot.utils;

import com.alphabetas.bot.service.MessageService;
import com.alphabetas.bot.service.impl.MessageServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public final class CommandUtils {
    public static final String SPACE_SYMBOL = "ˁ";
    public static final String TEMPLATE_REGEX = "\\wа-яА-ЯіІїЇґҐєЄ\\-'`0-9" + SPACE_SYMBOL;
    public static final String SINGLE_WORD_REGEX = "[" + TEMPLATE_REGEX + "]*";
    public static final String CURRENT_REGEX = SINGLE_WORD_REGEX;
    public static final String NAME_WITH_SPACES_REGEX = "[" + TEMPLATE_REGEX + " ]*";

    public static final MessageService messageService = new MessageServiceImpl();

    private CommandUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param message start of message, which we will be edited
     * @param params  params, which will be cleared
     * @return edited string
     * <p>
     * Removes start of message such as "Кликун", ".", etc
     */
    public static String trimMessage(String message, String[] params) {
        message = message.replaceAll("@caller_ua_bot", "");
        message = message.replaceAll("@bunker_ua_bot", "");
        int max = message.length() > 7 ? 8 : message.length();
        if (StringUtils.containsIgnoreCase(message.substring(0, max), "кликун")) {
            message = StringUtils.replaceOnceIgnoreCase(message, "кликун", "");
        }
        message = message.replaceFirst("[.!]", "");
        for (String param : params) {
            message = StringUtils.replaceIgnoreCase(message, param, "");
        }
        return message.trim();
    }

    public static String makeLink(Update update) {
        return String.format("<a href='tg://user?id=%d'>%s</a>", update.getMessage().getFrom().getId(),
                update.getMessage().getFrom().getFirstName());
    }

    public static String makeLink(Long id, String firstName) {
        firstName = firstName.replaceAll("[<>]", "");
        return String.format("<a href='tg://user?id=%d'>%s</a>", id, firstName);
    }

    public static String encryptSpace(String text) {
        return text.replaceAll(" ", "ˁ");
    }

    public static String decryptSpace(String text) {
        return text.replaceAll("ˁ", " ");
    }

    public static boolean isCommand(String text) {
        return text.startsWith("!") || text.startsWith(".") || text.toLowerCase().startsWith("кликун") || text.startsWith("/");
    }

    public static String deleteBadSymbols(String text) {
        if(text == null) {
            return null;
        }
        return text.replaceAll("[<>]", "");
    }


    public static boolean isAdmin(Long userId, Long chatId) {
        List<ChatMember> admins = messageService.getAdmins(chatId);

        for (ChatMember member: admins) {
            if(userId.equals(member.getUser().getId())) {
                return member instanceof ChatMemberAdministrator;
            }
        }

        return false;
    }

    public static boolean isOwner(Long userId, Long chatId) {
        List<ChatMember> admins = messageService.getAdmins(chatId);

        for (ChatMember member: admins) {
            if(userId.equals(member.getUser().getId())) {
                return member instanceof ChatMemberOwner;
            }
        }

        return false;
    }

    public static String[] getNamesFromMessage(String message) {
        return Arrays.stream(message.split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }

    public static int returnDay(int day) {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(day);
    }

}
