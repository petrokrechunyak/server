package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.model.MessageCount;
import com.alphabetas.bot.repo.MessageCountRepo;
import com.alphabetas.bot.service.ChatService;
import com.alphabetas.bot.service.UserService;
import com.alphabetas.bot.service.MessageCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Service
public class MessageCountServiceImpl implements MessageCountService {

    @Autowired
    private MessageCountRepo messageCountRepo;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Override
    public List<MessageCount> getAllByCallerUser(CallerUser user) {
        return messageCountRepo.getAllByCallerUser(user);
    }

    @Override
    public MessageCount getByUserIdAndStartTime(Long userId, Long chatId, Long startTime, Update update) {
        CallerChat chat = chatService.getById(chatId, update);
        CallerUser user = userService.getByUserIdAndCallerChat(userId, chat);

        return messageCountRepo.getByCallerUserAndStartTime(user, startTime);
    }

    @Override
    public List<MessageCount> getAllByChat(CallerChat chat) {
        return messageCountRepo.getAllByChat(chat);
    }

    @Override
    public List<MessageCount> getAll() {
        return messageCountRepo.findAll();
    }

    @Override
    public void deleteAll(List<MessageCount> list) {
        messageCountRepo.deleteAll(list);
    }

    @Override
    public MessageCount save(MessageCount messageCount) {
        return messageCountRepo.save(messageCount);
    }

    @Override
    public void delete(MessageCount messageCount) {
        messageCountRepo.delete(messageCount);
    }

}
