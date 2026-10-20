package net.java.trial_task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrialTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrialTaskApplication.class, args);
	}

}
