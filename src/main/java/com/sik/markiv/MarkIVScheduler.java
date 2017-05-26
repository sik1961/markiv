package com.sik.markiv;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.sik.markiv.google.calendar.MarkIVCalendarFeed;

@SpringBootApplication
@EnableScheduling
public class MarkIVScheduler {

	private static final Logger LOG = Logger.getLogger(MarkIVScheduler.class);
	
    public static void main(String[] args) throws Exception {
        SpringApplication.run(MarkIVScheduler.class);
    }
    
    @Bean
	MarkIVCalendarFeed getFeed() {
		LOG.info("Initialising Calendar feed...");
		return new MarkIVCalendarFeed();
	}
    
}
