package com.alphabetas.bot.service;

import com.alphabetas.bot.model.ChatConfig;

public interface ChatConfigService {

    void save(ChatConfig config);

    ChatConfig findById(Long id);
}
