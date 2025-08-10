package com.alphabetas.bot.service;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.GroupName;

public interface GroupNameService extends AbstractService<GroupName> {

    GroupName getByNameAndChat(String name, CallerChat chat);

}
