package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.GroupName;
import com.alphabetas.bot.repo.GroupNameRepo;
import com.alphabetas.bot.service.GroupNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupNameServiceImpl implements GroupNameService {

    @Autowired
    private GroupNameRepo groupNameRepo;

    @Override
    public GroupName save(GroupName groupName) {
        return groupNameRepo.save(groupName);
    }

    @Override
    public void delete(GroupName groupName) {
        groupNameRepo.delete(groupName);
    }

    @Override
    public GroupName getByNameAndChat(String name, CallerChat chat) {
        return groupNameRepo.findByNameIgnoreCaseAndChat(name, chat);
    }
}
