package com.sik.markiv.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author sik
 *
 */
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.sik.markiv.google.calendar.MarkIVCalendarFeed;

@SpringBootApplication
public class AppContextConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(AppContextConfiguration.class);
	
	@Bean
	MarkIVCalendarFeed getFeed() {
		LOG.info("Initialising Calendar feed....");
		return new MarkIVCalendarFeed();
	}

}
