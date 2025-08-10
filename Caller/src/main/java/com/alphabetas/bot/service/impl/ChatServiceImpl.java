package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.commands.ConfigCommand;
import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.ChatConfig;
import com.alphabetas.bot.model.PremiumChat;
import com.alphabetas.bot.repo.ChatRepo;
import com.alphabetas.bot.service.ChatConfigService;
import com.alphabetas.bot.service.ChatService;
import com.alphabetas.bot.service.PremiumChatService;
import com.alphabetas.bot.service.UserService;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepo chatRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatConfigService chatConfigService;

    @Autowired
    private PremiumChatService premiumChatService;

    @Override
    public CallerChat save(CallerChat callerChat) {
        return chatRepo.save(callerChat);
    }

    @Override
    public void delete(CallerChat callerChat) {
        callerChat.getCallerUsers().forEach(x -> userService.delete(x));
        callerChat.setCallerUsers(Collections.emptySet());
        chatRepo.delete(callerChat);
        log.info("Chat has been deleted");
    }

    @Override
    public CallerChat getById(Long id, Update update) {
        try {
            CallerChat chat = chatRepo.findById(id).get();
            if (chat.getConfig() == null || chat.getPremium() == null) {
                return saveWithMoreTables(chat);
            }
            if(update.hasCallbackQuery()) {
                update.setMessage(update.getCallbackQuery().getMessage());
            }
            if(update.getMessage().getChat().getTitle() != null && !chat.getTitle().equals(update.getMessage().getChat().getTitle())) {
                chat.setTitle(CommandUtils.deleteBadSymbols(update.getMessage().getChat().getTitle()));
                save(chat);
            }
            return chat;
        } catch (NoSuchElementException e) {
            String chatTitle = CommandUtils.deleteBadSymbols(update.getMessage().getChat().getTitle());
            CallerChat chat = new CallerChat(id, chatTitle);

            log.info("Creating new chat...");
            saveWithMoreTables(chat);
            return chat;
        }
    }

    @Override
    public void incrementMessages(CallerChat chat) {
        chat.setAllMessages(chat.getAllMessages()+1);
        save(chat);
    }

    @Override
    public CallerChat getByUpdate(Update update) {
        if(update.hasMessage())
            return getById(update.getMessage().getChatId(), update);
        else
            return getById(update.getCallbackQuery().getMessage().getChatId(), update);
    }

    @Override
    public List<CallerChat> findAll() {
        return chatRepo.findAll();
    }

    private CallerChat saveWithMoreTables(CallerChat chat) {
        ChatConfig config = new ChatConfig(chat.getId(), ConfigCommand.DEFAULT_LIMIT);
        chatConfigService.save(config);
        chat.setConfig(config);

        PremiumChat premiumChat = new PremiumChat(chat);
        premiumChatService.save(premiumChat);
        chat.setPremium(premiumChat);

        return save(chat);
    }
}
