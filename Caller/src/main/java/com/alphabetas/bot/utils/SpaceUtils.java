package com.alphabetas.bot.utils;

import com.alphabetas.bot.commands.Command;
import com.alphabetas.bot.service.MessageService;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.File;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class SpaceUtils {

    private final String botToken;
    private final MessageService messageService;

    private final String testId = String.valueOf(Command.TEST_CHAT_ID);

    public SpaceUtils(MessageService messageService, String botToken) {
        this.messageService = messageService;
        this.botToken = botToken;
    }

    public static void write(Update update, String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("target/chat" + update.getMessage().getChatId().toString() + update.getMessage().getChat().getTitle(), true))) {
            if (text == null)
                text = update.getMessage().getText();
            writer.write(update.getMessage().getFrom().getFirstName() + "\t||\t" + text);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFromCaller(Update update, String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("target/chat" + update.getMessage().getChatId().toString() + update.getMessage().getChat().getTitle(), true))) {
            if (text == null)
                text = update.getMessage().getText();
            writer.write("\uD83D\uDC51Кликун\uD83D\uDC51" + "\t||\t" + text);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void saveImage(String imageUrl, String destinationFile) {
        try {
            URL url = new URL(imageUrl);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(destinationFile);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String trimSpaces(Update update) {
        try {
            if (update.getMessage().hasText()) {
                write(update, null);
            }
            if (update.getMessage().hasVideoNote() || update.getMessage().hasDocument()
            || update.getMessage().hasVideo() || update.getMessage().hasPhoto()) {
                int r = new Random().nextInt();
                write(update, "sent " + r);
                CopyMessage forwardMessage = new CopyMessage(Command.TEST_CHAT_ID.toString(),
                        update.getMessage().getChatId().toString(),
                        update.getMessage().getMessageId());
                forwardMessage.setCaption(Integer.toString(r));
                messageService.sendMessage(forwardMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return " text ".trim();
    }

}
