package com.alphabetas.bot.callback;


import com.alphabetas.bot.commands.ConfigCommand;
import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.ChatConfig;
import com.alphabetas.bot.model.enums.ConfigStateEnum;
import com.alphabetas.bot.utils.CallbackUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.alphabetas.bot.commands.ConfigCommand.DEFAULT_LIMIT;
import static com.alphabetas.bot.commands.ConfigCommand.mainMenu;
import static com.alphabetas.bot.model.enums.ConfigStateEnum.*;
import static com.alphabetas.bot.utils.CallbackUtils.*;
import static com.alphabetas.bot.utils.ServiceUtils.configService;

@Component
@Scope("prototype")
@Slf4j
public class ChatConfigCallback extends CallBack {

    private static final String CONFIG_DATA = "CONFIG";

    private final Map<ConfigStateEnum, MessageBuilder> actionsMap = new HashMap<>();

    @Override
    public void execute(Update update) {
        if(user.getRole().getRoleNumber() < 2) {
            messageService.sendAnswerCallback("Керувати налаштуванням Кликуна можуть тільки адміністратори", update);
            return;
        }
        initActions();
        actionsMap.get(getByData(data)).action();

    }

    @Override
    public String getSpecialArg() {
        return "CONFIG";
    }

    private void initActions() {
        ChatConfig config = chat.getConfig();
        String[] params = data.split("\\.");
        actionsMap.put(MAIN_MENU, () -> edit(String.format(ConfigCommand.MAIN_MENU_TEXT, chat.getTitle()), mainMenu(chat)));

        actionsMap.put(LIMIT_MENU, () -> limitMenu(chat.getConfig()));

        actionsMap.put(CLOSE, () -> {
            edit("Налаштування завершено!", null);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                messageService.sendErrorMessage(e, update);
            }
            messageService.deleteMessage(update.getCallbackQuery().getMessage().getMessageId());
        });

        actionsMap.put(CHANGE_NAME_LIMIT, () -> {
            if (params.length > 3) {
                int param = Integer.parseInt(params[3]);
                if (param > 0 && config.getNameLimit().equals(Integer.MAX_VALUE)) {
                    config.setNameLimit(0);
                } else if (config.getNameLimit().equals(Integer.MAX_VALUE)) {
                    return;
                }
                config.setNameLimit(config.getNameLimit() + param);
                configService.save(config);
            }
            String callback = createCallbackMessage(CHANGE_NAME_LIMIT) + ".";
            edit("Зміна ліміту\nЛіміт на імена: " +
                            (config.getNameLimit().equals(Integer.MAX_VALUE)
                                    ? 0 : config.getNameLimit()),
                    createMarkupByButton(
                            buttonByTextAndCallBack("-1", callback + "-1"),
                            buttonByTextAndCallBack("+1", callback + "+1"),
                            buttonByTextAndCallBack("-5", callback + "-5"),
                            buttonByTextAndCallBack("+5", callback + "+5"),
                            backButton()
                    ));
        });

        actionsMap.put(DISABLE_NAME_LIMIT, () -> {
            config.setNameLimit(Integer.MAX_VALUE);
            configService.save(config);
            limitMenu(config);
        });
        actionsMap.put(ENABLE_NAME_LIMIT, () -> {
            config.setNameLimit(DEFAULT_LIMIT);
            configService.save(config);
            limitMenu(config);
        });

        actionsMap.put(RP_MENU, () -> {
            if (params.length > 3) {
                boolean param = Boolean.parseBoolean(params[3]);
                config.setRpEnabled(param);
                configService.save(config);
            }
            booleanChanger(config.isRpEnabled(), ConfigStateEnum.RP_MENU,
                    "Рольові команди: ");
        });

        actionsMap.put(AGGRESSION_MENU, () -> {
            if (params.length > 3) {
                boolean param = Boolean.parseBoolean(params[3]);
                config.setAggressionEnabled(param);
                configService.save(config);
            }
        booleanChanger(config.isAggressionEnabled(), ConfigStateEnum.AGGRESSION_MENU,
                "Підказка при відмітці когось через @: ");
        });

        actionsMap.put(ALLOW_SPACES, () -> {
            if (params.length > 3) {
                boolean param = Boolean.parseBoolean(params[3]);
                config.setAllowSpace(param);
                configService.save(config);
            }

        booleanChanger(config.isAllowSpace(), ConfigStateEnum.ALLOW_SPACES,
                "Дозвіл на імена з пробілами: ");
        });

        actionsMap.put(ALLOW_ADULT, () -> {
            if (params.length > 3) {
                boolean param = Boolean.parseBoolean(params[3]);
                config.setAllowAdult(param);
                configService.save(config);
            }

            booleanChanger(config.isAllowAdult(), ALLOW_ADULT,
                    "Дозвіл на 18+ контент: ");
        });
    }

    public static String createCallbackMessage(CallerChat chat, String action) {
        return CallbackUtils.buildCallbackText("CONFIG", chat.getId(), action);
    }

    public String createCallbackMessage(ConfigStateEnum action) {
        return createCallbackMessage(chat, action.toString());
    }


    public void limitMenu(ChatConfig config) {
        CallerChat chat = config.getChat();
        String text = "Ліміт на імена: <b>" +
                (config.getNameLimit().equals(Integer.MAX_VALUE)
                        ? "без ліміту"
                        : config.getNameLimit().toString()) + "</b>";
        edit(text, createMarkupByButton(
                buttonByTextAndCallBack("Змінити ліміт",
                        createCallbackMessage(ConfigStateEnum.CHANGE_NAME_LIMIT)), // 1
                chat.getConfig().getNameLimit().equals(Integer.MAX_VALUE) // 2
                        ? enableLimitName()
                        : disableLimitName(),
                backButton() // 3
        ));
    }

    public InlineKeyboardButton enableLimitName() {
        return buttonByTextAndCallBack("Ввімкнути ліміт", createCallbackMessage(ENABLE_NAME_LIMIT));
    }

    public InlineKeyboardButton disableLimitName() {
        return buttonByTextAndCallBack("Вимкнути ліміт", createCallbackMessage(DISABLE_NAME_LIMIT));
    }

    public InlineKeyboardButton backButton() {
        return buttonByTextAndCallBack("Назад",
                createCallbackMessage(MAIN_MENU));
    }

    private void booleanChanger(boolean current, ConfigStateEnum state, String text) {
        InlineKeyboardButton button = buttonByTextAndCallBack(current ? "Вимкнути ❌" : "Ввімкнути ✅",
                createCallbackMessage(state) + "." +
                        (current ? "false" : "true"));
        edit(text + (current ? "Увімкнено ✅" : "Вимкнено ❌"),
                createMarkupByButton(button, backButton()));
    }

    @FunctionalInterface
    private interface MessageBuilder {
        void action();
    }
}
