package com.sik.markiv.config;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
/**
 * @author sik
 *
 */
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.sik.markiv.google.calendar.MarkIVCalendarFeed;

@SpringBootApplication
public class AppContextConfiguration {
	private static final Logger LOG = LogManager.getLogger(AppContextConfiguration.class);
	
	@Bean
	MarkIVCalendarFeed getFeed() {
		LOG.info("Initialising Calendar feed....");
		return new MarkIVCalendarFeed();
	}

}
