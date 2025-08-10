package com.alphabetas.bot.commands.admin;


import com.alphabetas.bot.commands.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.UNKNOWN_COMMAND;
import static com.alphabetas.bot.service.MessageService.MY_ID;

@Slf4j
@Component
@Scope("prototype")
public class BackupCommand extends Command {
    @Override
    public int execute(Update update) {
        if(!user.getUserId().equals(MY_ID)) {
            return UNKNOWN_COMMAND.getReturnCode();
        }
        try {
            Runtime.getRuntime().exec("sudo -iu postgres");
            Runtime.getRuntime().exec("/var/lib/postgresql/terrifficsprite/backup_to_file");
            Runtime.getRuntime().exec("cp /var/lib/postgresql/terrifficsprite/backup ./");

        } catch (IOException e) {
            e.printStackTrace();
        }

        backupChats();
        SendDocument document = new SendDocument(Command.TEST_CHAT_ID.toString(), new InputFile(new File("backup")));
        messageService.sendDocument(document);

        return SUCCESS.getReturnCode();
    }

    public void backupChats() {
        File dir = new File("./target/");
        FileFilter fileFilter = new WildcardFileFilter("chat*");
        File[] files = dir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
            SendDocument document = new SendDocument(Long.toString(MY_ID), new InputFile(files[i]));
            messageService.sendDocument(document);
            files[i].delete();
        }
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/backup"};
    }
}
