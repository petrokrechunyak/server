package com.alphabetas.bot.aop;

import com.alphabetas.bot.model.CallerChat;
import com.alphabetas.bot.model.enums.CallerRoles;
import com.alphabetas.bot.model.CallerUser;
import com.alphabetas.bot.service.ChatService;
import com.alphabetas.bot.service.MessageService;
import com.alphabetas.bot.service.UserService;
import com.alphabetas.bot.service.impl.MessageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Aspect
@Component
public class CommandAspect {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    private CallerChat chat;
    private CallerUser user;
    private String msgText;

    private MessageService messageService;

    @Pointcut("execution(public void execute(..))")
    public void commandsExecution(){}

    @Around("commandsExecution()")
    public Object beforeCommandsExecution(ProceedingJoinPoint joinPoint) {
        Update update = (Update)joinPoint.getArgs()[0];
        setVars(update);

        if (msgText.startsWith("!")) {
            if (user.getRole().getRoleNumber() > CallerRoles.MEMBER.getRoleNumber()) {
                try {
                    joinPoint.proceed();
                } catch (Throwable e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                    messageService.sendErrorMessage(e, update);
                }
            } else {
                messageService.sendMessage("Команди, які починаються з \"!\", можуть використовувати лише адміністратори чату");
            }
        } else {
            try {
                Object result = joinPoint.proceed();
                System.out.println(result);
            } catch (Throwable e) {
                log.error(ExceptionUtils.getStackTrace(e));
                messageService.sendErrorMessage(e, update);
            }
        }

//        if (msgText.startsWith("/")) {
//            messageService.deleteMessage(update.getMessage().getMessageId());
//        }

        log.info(joinPoint.getTarget().getClass().getSimpleName() + " finished successfully");
        log.info("==========================");
        return null;
    }

    private void setVars(Update update) {
        this.chat = chatService.getByUpdate(update);
        this.user = userService.getByUserIdAndCallerChat(update.getMessage().getFrom().getId(), chat);
        this.msgText = update.getMessage().getText();
        this.messageService = new MessageServiceImpl(update);
    }

}
