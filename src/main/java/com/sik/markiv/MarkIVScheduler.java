package com.sik.markiv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarkIVScheduler {

	public static void main(String[] args) throws Exception {
<<<<<<< Updated upstream
		new SpringApplication(MarkIVScheduler.class).run(args);
=======
		//SpringApplication app = new SpringApplication(MarkIVScheduler.class);
		//app.setBanner(banner);
		//app.run(args);
		SpringApplication.run(MarkIVScheduler.class);
>>>>>>> Stashed changes
	}

}
