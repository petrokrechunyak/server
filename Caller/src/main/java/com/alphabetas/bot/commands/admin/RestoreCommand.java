package com.alphabetas.bot.commands.admin;

import com.alphabetas.bot.CallerBot;
import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.service.DatabaseBackupService;
import com.alphabetas.bot.service.impl.MessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;
import static com.alphabetas.bot.model.enums.ReturnCodesEnum.UNKNOWN_COMMAND;
import static com.alphabetas.bot.service.MessageService.MY_ID;

@Slf4j
@Component
@Scope("prototype")
public class RestoreCommand extends Command {

    @Autowired
    private DatabaseBackupService databaseBackupService;

    @Override
    public int execute(Update update) {
        if (!user.getUserId().equals(MY_ID)) {
            messageService.sendMessage(chat.getId(), "❌ Ви не маєте прав для цієї команди", false);
            return UNKNOWN_COMMAND.getReturnCode();
        }

        // Check if this is a reply to a message with a document
        if (repliedMessage == null || !repliedMessage.hasDocument()) {
            messageService.sendMessage(chat.getId(),
                    "❌ Помилка: відповідь повинна містити файл резервної копії.\n\n" +
                    "Використання: відповідьте на повідомлення з файлом бекапу командою /restore",
                    false
            );
            return UNKNOWN_COMMAND.getReturnCode();
        }

        try {
            messageService.sendMessage(chat.getId(), "⏳ Обробляю файл резервної копії...", false);

            Document document = repliedMessage.getDocument();
            String fileId = document.getFileId();

            // Get file info
            GetFile getFile = new GetFile(fileId);
            File telegramFile = messageService.getFile(getFile);

            if (telegramFile == null) {
                messageService.sendMessage(chat.getId(), "❌ Помилка: неможливо завантажити файл", false);
                return UNKNOWN_COMMAND.getReturnCode();
            }

            // Download file from Telegram
            String downloadedFilePath = downloadFile(telegramFile);
            log.info("File downloaded to: {}", downloadedFilePath);

            // Restore database
            messageService.sendMessage(chat.getId(),
                    "⏳ Відновлюю базу даних (це може зайняти деякий час)...",
                    false
            );
            databaseBackupService.restoreBackup(downloadedFilePath);

            // Clean up downloaded file
            Files.delete(Paths.get(downloadedFilePath));

            messageService.sendMessage(chat.getId(),
                    "✅ Базу даних успішно відновлено з резервної копії!",
                    false
            );
            log.info("Database restored successfully from file: {}", downloadedFilePath);

            return SUCCESS.getReturnCode();

        } catch (Exception e) {
            log.error("Error restoring database: {}", ExceptionUtils.getStackTrace(e));
            messageService.sendMessage(chat.getId(),
                    "❌ Помилка при відновленні бази даних:\n" + e.getMessage(),
                    false
            );
            return UNKNOWN_COMMAND.getReturnCode();
        }
    }

    /**
     * Downloads a file from Telegram servers using the bot
     */
    private String downloadFile(File telegramFile) throws Exception {
        String downloadedFilePath = "./backups/backup_" + System.currentTimeMillis() + ".sql";

        // Get the bot instance from MessageServiceImpl
        CallerBot bot = MessageServiceImpl.bot;

        // Download file from Telegram - returns a java.io.File
        java.io.File downloadedTelegramFile = bot.downloadFile(telegramFile);

        // Copy the downloaded file to our backup location
        if (downloadedTelegramFile != null && downloadedTelegramFile.exists()) {
            java.nio.file.Files.copy(
                    downloadedTelegramFile.toPath(),
                    Paths.get(downloadedFilePath),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        } else {
            throw new IOException("Failed to download file from Telegram");
        }

        return downloadedFilePath;
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[]{"/restore"};
    }
}





