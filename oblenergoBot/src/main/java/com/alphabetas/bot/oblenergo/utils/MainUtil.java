package com.alphabetas.bot.oblenergo.utils;

import com.alphabetas.bot.oblenergo.CallerBot;
import com.alphabetas.bot.oblenergo.model.Group;
import com.alphabetas.bot.oblenergo.model.User;
import com.alphabetas.bot.oblenergo.repo.GroupRepo;
import com.alphabetas.bot.oblenergo.service.MessageService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Slf4j
public class MainUtil {

    public static InlineKeyboardMarkup prepareKeyboard(User user) {
        List<List<InlineKeyboardButton>> allRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for(int i = 1; i <= CallerBot.GROUPS_NUMBER; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton(Integer.toString(i) +
                    (user.getGroups().contains(new Group(i)) ? " ✅" : ""));
            button.setCallbackData(Integer.toString(i));
            row.add(button);

            if(i % 3 == 0) {
                allRows.add(row);
                row = new ArrayList<>();
            }
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(allRows);

        InlineKeyboardButton modeButton = new InlineKeyboardButton();
        modeButton.setText("Режим: " + (user.getCompact() ? "Компактний" : "Детальний"));
        modeButton.setCallbackData("TOGGLE_MODE");

        allRows.add(List.of(modeButton));


        return keyboardMarkup;
    }

    public static void writeToFile(String hash, String path) {
        File file = new File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(hash);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String removeBadSymbols(String input) {
        return input.replaceAll("[<>]", "");
    }

    public String read() {
        File input = new File("image.png");

        try {
            BufferedImage buffImg = ImageIO.read(input);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(buffImg, "png", outputStream);
            byte[] data = outputStream.toByteArray();

            System.out.println("Checking for new photo");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            byte[] hash = md.digest();
            return returnHex(hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String readHash(String path) {
        File file = new File(path);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        }catch (FileNotFoundException e) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return "no date";
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveImage(String imageUrl, String destinationFile) throws IOException {
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
    }

    static String returnHex(byte[] inBytes) throws Exception {
        String hexString = null;
        for (int i = 0; i < inBytes.length; i++) { //for loop ID:1
            hexString +=
                    Integer.toString((inBytes[i] & 0xff) + 0x100, 16).substring(1);
        }                                   // Belongs to for loop ID:1
        return hexString;
    }

    public static void sleep(Long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String makeLink(Long id, String firstName) {
        return String.format("<a href='tg://user?id=%d'>%s</a>", id, firstName);
    }
}
