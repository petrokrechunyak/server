package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.model.ChatConfig;
import com.alphabetas.bot.repo.ChatConfigRepo;
import com.alphabetas.bot.service.ChatConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatConfigServiceImpl implements ChatConfigService {

    @Autowired
    private ChatConfigRepo configRepo;

    public void save(ChatConfig config) {
        configRepo.save(config);
    }

    @Override
    public ChatConfig findById(Long id) {
        return configRepo.findByChatId(id);
    }
}
