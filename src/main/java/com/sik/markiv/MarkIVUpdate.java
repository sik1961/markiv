package com.sik.markiv;

/**
 * @author sik
 */

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sik.markiv.api.UpdateRecord;
import com.sik.markiv.events.EventManager;
import com.sik.markiv.exception.MarkIVException;
import com.sik.markiv.google.calendar.MarkIVCalendarFeed;

@Component
public class MarkIVUpdate {

	DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
	private static final String DAYTIME = "0 0 7-23 * * *"; // hourly 7am-11pm
	private static final String END_OF_DAY = "0 30 23 * * *"; // daily @ 11:30pm
	private static final Logger LOG = LoggerFactory .getLogger(MarkIVUpdate.class);

	MarkIVHelper m4h;
	private EventManager em;
	private MarkIVCalendarFeed feed;
	private LocalDateTime calendarLastUpdate;

	public MarkIVUpdate() {
		LOG.info("Mark IV Mgt - Initialised");
		this.feed = new MarkIVCalendarFeed();
		this.em = new EventManager(this.feed.getFeed());
		this.m4h = new MarkIVHelper(em);
		this.calendarLastUpdate = new LocalDateTime(0);
	}

	@Scheduled(cron = DAYTIME)
	public void m4Update() throws MarkIVException {
		update(false);
	}

	@Scheduled(cron = END_OF_DAY)
	public void m4DailyUpdate() throws MarkIVException {
		update(true);
	}

	protected void update(final boolean forceUpdate) {
		LOG.info("Mark IV Mgt - Checking calendar....");

		em.updateFromFeed(new MarkIVCalendarFeed().getFeed());

		UpdateRecord lu = em.getLatestUpdate();

		LOG.info("Cal updated: " + lu.getLastUpdated().toString(dtf) + 
				" - Web updated: " + this.calendarLastUpdate.toString(dtf));

		if (forceUpdate || lu.getLastUpdated().isAfter(this.calendarLastUpdate)) {

			if (!forceUpdate) {
				LOG.info("Last update: " + lu.getLastUpdated().toString(dtf)
						+ " (" + lu.getEvent().getEventType() + ":"
						+ lu.getEvent().getSummary() + ")");
			}

			LOG.info("Mark IV Mgt - Building Gigs Page");
			m4h.buildGigsPage();

			LOG.info("Mark IV Mgt - Building Availability Page");
			m4h.buildAvailPage();
			
			LOG.info("Mark IV Mgt - Availablity Stats");
			m4h.doAvailabilityStats();
			
			LOG.info("Mark IV Mgt - Building News Page");
			m4h.buildNewsPage();

			LOG.info("Mark IV Mgt - Building Gallery Page");
			m4h.buildGalleryPage();

			LOG.info("Mark IV Mgt - Uploading");
			try {
				m4h.uploadFiles();
				LOG.info("Mark IV Mgt - Completed");
			} catch (MarkIVException e) {
				throw new MarkIVException("Caught Exception: ", e);
			}
			this.calendarLastUpdate = new LocalDateTime(
					System.currentTimeMillis());
			LOG.info("Mark IV Mgt - Last update time set to: "
					+ this.calendarLastUpdate.toString());
		} else {
			LOG.info("Mark IV Mgt - Skipped - no calendar updates");
		}
	}
}
