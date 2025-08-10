package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.Name;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.repo.NameRepo;
import com.alphabetas.bot.service.NameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class NameServiceImpl implements NameService {
    @Autowired
    private NameRepo nameRepo;

    @Override
    public Name save(Name Name) {
        return nameRepo.saveAndFlush(Name);
    }

    @Override
    public void delete(Name name) {
        nameRepo.delete(name);
    }

    @Override
    public Name getByCallerChatAndName(CallerChat chat, String name) {
        return nameRepo.getByChatAndNameIgnoreCase(chat, name);
    }

    @Override
    public Set<Name> getAllByCallerChat(CallerChat chat) {
        return nameRepo.getAllByChat(chat);
    }

    @Override
    public void deleteAllByCallerUser(CallerUser user) {
        nameRepo.deleteAllByCallerUser(user);
    }

    @Override
    public Set<Name> getAllByCallerUser(CallerUser user) {
        return nameRepo.getAllByCallerUser(user);
    }
}
