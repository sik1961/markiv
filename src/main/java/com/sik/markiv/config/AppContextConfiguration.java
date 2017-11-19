package com.sik.markiv.config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.sik.markiv.google.calendar.MarkIVCalendarFeed;

@Configuration
@PropertySource("file:${env.markiv.properties.location}/rabbitmq.properties")
@PropertySource("file:${env.markiv.properties.location}/tracking-staging-errors.properties")
@PropertySource("file:${env.markiv.properties.location}/application.properties")
public class AppContextConfiguration {
	private static final Logger LOG = LogManager.getLogger(AppContextConfiguration.class);
	
	@Bean
	MarkIVCalendarFeed getFeed() {
		LOG.info("Initialising Calendar feed....");
		return new MarkIVCalendarFeed();
	}

}
