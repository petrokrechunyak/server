package com.alphabetas.bot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Random;

import static com.alphabetas.bot.model.enums.ReturnCodesEnum.SUCCESS;

@Slf4j
@Component
@Scope("prototype")
public class IAmHereCommand extends Command {
    private final String[] commonPhrases = new String[]{"Я тут та готовий вас кликати", "Хтось мене кликав?",
            "Так?", "Завжди тут", "Завжди онлайн", "Слухаю?"};

    private final String[] rarePhrases = new String[]{"Я тут, привіт)", "Га?)", "Завжди до ваших послуг)",
            "Я тут, готовий до ваших команд)", "Ви сказали Кликун, і ось я! Як я можу допомогти?)",
    "Завжди у вашому розпорядженні)", "Це я. Що потрібно зробити?"};

    private final String[] epicPhrases = new String[]{"Я живий, здоровий і взагалі працюю :)",
            "Якщо потрібно когось покликати - звертайтеся :)", "Вгадайте, для чого я тут? Так, для заклику! :)",
    "Так, Кликун слухає вас. Кого потрібно покликати? :)"};

    private final String[] legendaryPhrases = new String[]{"Я тут та готовий вас кликат... Почекайте це ж легендарна фраза, якою я відповідаю з дуже маленьким шансом. Вітаю! В честь цього підпишіться на хатину Кликуна, будь ласка: \n\n" +
            "<a href='https://t.me/callerHut'>Моя хатина</a>)))"};

    public static String getPhrase(String[] arr) {
        return arr[new Random().nextInt(arr.length)];
    }

    @Override
    public int execute(Update update) {
        Random random = new Random();
        int num = random.nextInt(4001);
        log.info("Random number: {}", num);
        String phrase;
        if (num == 4000) {
            phrase = getPhrase(legendaryPhrases);
        } else if (num >= 3980) {
            phrase = getPhrase(epicPhrases);
        } else if (num >= 3800) {
            phrase = getPhrase(rarePhrases);
        } else {
            phrase = getPhrase(commonPhrases);
        }
        log.info("Random message: {}", phrase);
        messageService.sendMessage(phrase);

        return SUCCESS.getReturnCode();
    }

    @Override
    public String[] getSpecialArgs() {
        return new String[] {
                "кликун"
        };
    }
}
