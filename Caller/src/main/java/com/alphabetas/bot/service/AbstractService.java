package com.alphabetas.bot.service;

public interface AbstractService<T> {

    T save(T t);

    void delete(T t);

}
