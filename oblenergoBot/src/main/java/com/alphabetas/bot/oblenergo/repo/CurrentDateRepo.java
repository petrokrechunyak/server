package com.alphabetas.bot.oblenergo.repo;

import com.alphabetas.bot.oblenergo.model.CurrentDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentDateRepo extends JpaRepository<CurrentDate, Long> {

}
