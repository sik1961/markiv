package com.sik.markiv.config;
/**
 * @author sik
 *
 */
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;

import com.sik.markiv.google.calendar.MarkIVCalendarFeed;

@SpringBootApplication
public class AppContextConfiguration {
	private static final Logger LOG = Logger.getLogger(AppContextConfiguration.class);
	
	@Bean
	MarkIVCalendarFeed getFeed() {
		LOG.info("Initialising Calendar feed....");
		return new MarkIVCalendarFeed();
	}

}
