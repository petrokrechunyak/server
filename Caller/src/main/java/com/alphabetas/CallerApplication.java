package com.alphabetas;

import com.alphabetas.bot.callback.ChatConfigCallback;
import com.alphabetas.bot.callback.DivorceCallback;
import com.alphabetas.bot.callback.MarriageCallback;
import com.alphabetas.bot.commands.*;
import com.alphabetas.bot.commands.admin.BackupCommand;
import com.alphabetas.bot.commands.admin.StatsCommand;
import com.alphabetas.bot.commands.admin.TellCommand;
import com.alphabetas.bot.group.*;
import com.alphabetas.bot.marriage.AllMarriagesCommand;
import com.alphabetas.bot.marriage.DivorceCommand;
import com.alphabetas.bot.marriage.MarriageCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Configuration
@EnableScheduling
@EnableAspectJAutoProxy
@SpringBootApplication
@ComponentScan(basePackages = "com.alphabetas.bot")
public class CallerApplication {

	public static void main(String[] args) {

		SpringApplication.run(CallerApplication.class, args);
	}

	@Bean
	public void initCommands() {
		new IAmHereCommand();
		new AddNameCommand();
		new DeleteCommand();
		new ShowCommand();
		new NoCommand();
		new TopCommand();
		new ConfigCommand();
		new StartCommand();
		new HelpCommand();
		new ClearCommand();
		new RefreshCommand();
		new TellCommand();
		new IdeaCommand();
		new BackupCommand();
		new StatsCommand();

		new MarriageCommand();
		new AllMarriagesCommand();
		new DivorceCommand();

		new AllCommand();
		new CreateCommand();
		new JoinCommand();
		new LeaveCommand();
		new RemoveCommand();

		new BlockNameCommand();
		new UnblockNameCommand();

		// callbacks
		new ChatConfigCallback();
		new MarriageCallback();
		new DivorceCallback();
		new RPCommand();
	}

}
