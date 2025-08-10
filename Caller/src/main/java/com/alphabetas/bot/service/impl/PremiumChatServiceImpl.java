package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.model.PremiumChat;
import com.alphabetas.bot.repo.PremiumChatRepo;
import com.alphabetas.bot.service.PremiumChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PremiumChatServiceImpl implements PremiumChatService {

    @Autowired
    private PremiumChatRepo premiumChatRepo;

    @Override
    public void save(PremiumChat premiumChat) {
        premiumChatRepo.save(premiumChat);
    }
}
