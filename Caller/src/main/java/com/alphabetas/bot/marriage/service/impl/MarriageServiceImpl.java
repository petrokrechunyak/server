package com.alphabetas.bot.marriage.service.impl;

import com.alphabetas.bot.marriage.model.MarriageModel;
import com.alphabetas.bot.marriage.repo.MarriageRepo;
import com.alphabetas.bot.marriage.service.MarriageService;
import com.alphabetas.bot.model.CallerChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarriageServiceImpl implements MarriageService {

    @Autowired
    private MarriageRepo repo;

    @Override
    public MarriageModel findByUserIdAndChat(Long userId, CallerChat chat) {
        MarriageModel marriage;
        marriage = repo.getByUser1IdAndChat(userId, chat);
        if(marriage == null) {
            return repo.getByUser2IdAndChat(userId, chat);
        }
        return marriage;
    }

    @Override
    public List<MarriageModel> findAll() {
        return repo.findAll();
    }

    @Override
    public List<MarriageModel> findAllByChat(CallerChat chat) {
        return repo.findAllByChat(chat);
    }

    @Override
    public MarriageModel save(MarriageModel marriageModel) {
        return repo.save(marriageModel);
    }

    @Override
    public void delete(MarriageModel marriageModel) {
        repo.delete(marriageModel);
    }

}
