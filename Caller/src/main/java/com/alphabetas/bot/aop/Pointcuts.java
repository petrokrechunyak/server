package com.alphabetas.bot.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {

    @Pointcut("execution(public * *(..))")
    public void commandsExecution(){}


}
