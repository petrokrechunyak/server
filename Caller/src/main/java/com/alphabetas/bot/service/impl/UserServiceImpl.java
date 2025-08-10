package com.alphabetas.bot.service.impl;

import com.alphabetas.bot.CallerBot;
import com.alphabetas.bot.marriage.model.MarriageModel;
import com.alphabetas.bot.marriage.service.MarriageService;
import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.enums.CallerRoles;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.repo.UserRepo;
import com.alphabetas.bot.service.GroupNameService;
import com.alphabetas.bot.service.MessageService;
import com.alphabetas.bot.service.UserService;
import com.alphabetas.bot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MarriageService marriageService;
    @Autowired
    private GroupNameService groupNameService;
    private final MessageService messageService = new MessageServiceImpl();

    @Override
    public CallerUser save(CallerUser callerUser) {
        return userRepo.save(callerUser);
    }

    @Override
    public void delete(CallerUser callerUser) {
        MarriageModel marriage = marriageService.findByUserIdAndChat(callerUser.getUserId(), callerUser.getCallerChat());
        if(marriage != null) {
            marriageService.delete(marriage);
        }

        if(callerUser.getGroupNames() != null) {
            callerUser.getGroupNames().forEach(x -> {
                x.getUsers().remove(callerUser);
                groupNameService.save(x);
            });
        }

        userRepo.save(callerUser);
        userRepo.delete(callerUser);
    }

    @Override
    public CallerUser getByUserIdAndCallerChat(Long userId, CallerChat callerChat) {
        CallerUser user = userRepo.getByUserIdAndCallerChat(userId, callerChat);
        User fromUser = messageService.getChatMember(callerChat.getId(), userId).getUser();
        if (user == null) {
            user = new CallerUser(userId, fromUser.getFirstName(), fromUser.getUserName(), callerChat);
            try {
                if(CommandUtils.isOwner(userId, callerChat.getId())) {
                    user.setRole(CallerRoles.OWNER);
                } else if (CommandUtils.isAdmin(userId, callerChat.getId())) {
                    user.setRole(CallerRoles.LOWER_ADMIN);
                } else if (CallerBot.creatorList.contains(userId)) {
                    user.setRole(CallerRoles.CREATOR);
                } else if (CallerBot.moderList.contains(userId)) {
                    user.setRole(CallerRoles.MODERATOR);
                }
            } catch (Exception e) {

            }

            log.info("Creating new user...");
            save(user);
        } else {
            if(user.getPresentFrom() == null) {
                user.setPresentFrom(new Date());
                save(user);
            }
            if(user.getRole() == null) {
                user.setRole(CallerRoles.MEMBER);
                save(user);
            }

            if(!user.getFirstname().equals(fromUser.getFirstName())) {
                user.setFirstname(fromUser.getFirstName());
                save(user);
            }
            if(fromUser.getUserName()!= null && !fromUser.getUserName().equals(user.getUsername())) {
                user.setUsername(fromUser.getUserName());
                save(user);
            }
        }

        return user;
    }

    @Override
    public void removeByCallerChat(CallerChat chat) {

    }

    @Override
    public void removeByUserIdAndCallerChat(Long userId, CallerChat chat) {

    }

    @Override
    public List<CallerUser> getAllByUserId(Long userId) {
        return userRepo.getAllByUserId(userId);
    }

    @Override
    public CallerUser getByUsernameAndCallerChat(String username, CallerChat chat) {
        return userRepo.getByUsernameAndCallerChat(username, chat);
    }
}
