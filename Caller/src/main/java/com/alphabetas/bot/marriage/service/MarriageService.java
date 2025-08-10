package com.alphabetas.bot.marriage.service;

import com.alphabetas.bot.marriage.model.MarriageModel;
import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.service.AbstractService;

import java.util.List;

public interface MarriageService extends AbstractService<MarriageModel> {

    MarriageModel findByUserIdAndChat(Long userId, CallerChat chat);

    List<MarriageModel> findAll();

    List<MarriageModel> findAllByChat(CallerChat chat);
}
