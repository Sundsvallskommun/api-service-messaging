package se.sundsvall.messaging;

import org.springframework.scheduling.annotation.EnableAsync;
import se.sundsvall.dept44.ServiceApplication;

import static org.springframework.boot.SpringApplication.run;

@ServiceApplication
@EnableAsync
public class Application {

	public static void main(String[] args) {
		run(Application.class, args);
	}
}
