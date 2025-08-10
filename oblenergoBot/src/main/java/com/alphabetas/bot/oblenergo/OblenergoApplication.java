package com.alphabetas.bot.oblenergo;

import com.alphabetas.bot.oblenergo.utils.MainUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableScheduling
@SpringBootApplication
public class OblenergoApplication {

	public static void main(String[] args) throws Exception {
		System.out.println("Getting current date");
		Document doc = Jsoup.connect("https://oblenergo.cv.ua/shutdowns/")
				.userAgent("Chrome/4.0.249.0 Safari/532.5")
				.referrer("http://www.google.com")
				.get();

		String date = doc.select("#gsv > ul > p").eachText().get(0);
		MainUtil.writeToFile(date, "date.txt");

		SpringApplication.run(OblenergoApplication.class, args);
	}

}
