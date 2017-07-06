package com.sik.markiv.events;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;

import com.sik.markiv.api.CalendarEvent;
import com.sik.markiv.api.EventType;
import com.sik.markiv.api.M4Date;
import com.sik.markiv.api.M4Fields;
import com.sik.markiv.api.RepeatType;
import com.sik.markiv.api.UpdateRecord;
import com.sik.markiv.exception.MarkIVException;
import com.sik.markiv.google.calendar.CalFields;
import com.sik.markiv.utils.M4DateUtils;

public class EventManager {
	private static final Logger LOG = Logger.getLogger(EventManager.class);

	

	private List<CalendarEvent> allEvents;
	private final M4DateUtils dateUtils = new M4DateUtils();
	private final EventUtility eu = new EventUtility();

	public EventManager(InputStream feed) {
		this.updateFromFeed(feed);
	}

	public void updateFromFeed(InputStream feed) {
		this.allEvents = this.buildEvents(eu.buildMapFromCalendarFeed(feed));
	}
	
	public List<CalendarEvent> getAllEvents() {
		return this.allEvents;
	}
	
	/**
	 * Get events after @param startTime
	 * 
	 * @param startTime
	 * @return
	 */
	public List<CalendarEvent> getEventsAfter(final long startTime) {
		final LocalDateTime fromDate = new LocalDateTime(startTime);
		final List<CalendarEvent> filteredEvents = new ArrayList<CalendarEvent>();
		for (final CalendarEvent e : this.allEvents) {
			if (e.getStartDate().isAfter(fromDate)) {
				filteredEvents.add(e);
			}
		}
		return filteredEvents;
	}

	/**
	 * Get events on @param dateTime
	 * 
	 * @param dateTime
	 * @return
	 */
	public List<CalendarEvent> getEventsOn(final M4Date dateTime) {
		final List<CalendarEvent> filteredEvents = new ArrayList<CalendarEvent>();
		for (final CalendarEvent event : this.allEvents) {
			this.debug(">>>>> event=" + event);
			
			if (eu.isDateInEvent(dateTime, event)
					|| eu.isEventInDate(dateTime, event)) {
				filteredEvents.add(event);
			}
		}
		return filteredEvents;
	}

	

	/**
	 * Get events by type
	 * 
	 * @param data
	 *            .calendarExportFilename
	 * @param eventType
	 * @param startTime
	 * @param suppressDupes
	 * @return
	 */
	public List<CalendarEvent> getByType(final EventType eventType,
			final long startTime, final boolean suppressDupes) {
		final List<CalendarEvent> filteredEvents = new ArrayList<CalendarEvent>();
		for (final CalendarEvent e : this.allEvents) {
			if (e.getStartDate().isAfter(new LocalDateTime(startTime))) {
				if (e.getEventType() == eventType) {
					if (!suppressDupes || !eu.eventExists(e, filteredEvents)) {
						filteredEvents.add(e);
					}
				}
			}
		}
		return filteredEvents;
	}

	/**
	 * Get latest update
	 * 
	 * @return
	 */
	public UpdateRecord getLatestUpdate() {
		final UpdateRecord ur = new UpdateRecord();
		for (final CalendarEvent e : this.allEvents) {
			if (e.getLastUpdated().isAfter(ur.getLastUpdated())) {
				ur.setLastUpdated(e.getLastUpdated());
				ur.setEvent(e);
			}
		}
		return ur;
	}

	

	private List<CalendarEvent> buildEvents(
			final List<HashMap<String, String>> rawEventsMapList) {
		final List<CalendarEvent> events = new ArrayList<CalendarEvent>();
		final Comparator<CalendarEvent> comparator = new Comparator<CalendarEvent>() {

			@Override
			public int compare(final CalendarEvent e1, final CalendarEvent e2) {
				int retVal = e1.getStartDate().compareTo(e2.getStartDate());
				if (retVal == 0) {
					retVal = e1.getEndDate().compareTo(e2.getEndDate());
				}
				return retVal;
			}
		};

		final Iterator<HashMap<String, String>> itr = rawEventsMapList
				.iterator();
		while (itr.hasNext()) {
			final Map<String, String> evt = itr.next();

			if (eu.rrulePresent(evt)) {
				doRepeats(evt, events);
			} else {
				LocalDateTime sDate = this.dateUtils.formatDate(this.dateUtils
						.stdDate(evt.get(CalFields.DTSTART)));
				LocalDateTime eDate = this.dateUtils.formatDate(this.dateUtils
						.stdDate(evt.get(CalFields.DTEND)));
				if (this.dateUtils.isRecent(eDate)) {
					this.debug("\tAdding single: " + evt.get(CalFields.SUMMARY) + M4Fields.COLON + evt.get(CalFields.DTSTART));
					eu.addEvent(events, evt, sDate, eDate);
				} 
			}
		}

		Collections.sort(events, comparator);

		LOG.info(events.size() + " CalendarEvent(s) generated");

		return events;
	}

	private void doRepeats(final Map<String, String> evt,
			List<CalendarEvent> events) {
		RepeatType rType = null;
		int increment = -1;
		LocalDateTime untilDate = null;
		if (evt.get(CalFields.RRULE_DAILY) != null) {
			untilDate = this.dateUtils.setEndOf(new LocalDateTime(
					this.dateUtils.formatDate(this.dateUtils.stdDate(evt.get(CalFields.RRULE_DAILY)))));
			rType = RepeatType.DAILY;
			increment = 1;
		} else if (evt.get(CalFields.RRULE_WEEKLY) != null) {
			untilDate = this.dateUtils.setEndOf(new LocalDateTime(
					this.dateUtils.formatDate(this.dateUtils.stdDate(evt.get(CalFields.RRULE_WEEKLY)))));
			rType = RepeatType.WEEKLY;
			increment = 7;
		} else if (evt.get(CalFields.RRULE_MONTHLY) != null) {
			untilDate = this.dateUtils.setEndOf(new LocalDateTime(
					this.dateUtils.formatDate(this.dateUtils.stdDate(evt.get(CalFields.RRULE_MONTHLY)))));
			rType = RepeatType.MONTHLY;
			increment = 1;
		} else {
			throw new MarkIVException("Invalid RRULE found");
		}

		LocalDateTime sDate = null;
		LocalDateTime eDate = null;

		if (evt.get(CalFields.DTSTART) != null) {
			sDate = new LocalDateTime(this.dateUtils.formatDate(this.dateUtils
					.stdDate(evt.get(CalFields.DTSTART))));
		} else {
			throw new MarkIVException("DTSTART is null for event: " + evt);
		}
		if (evt.get(CalFields.DTEND) != null) {
			eDate = new LocalDateTime(
					this.dateUtils.subtractOneMillisecond(this.dateUtils.formatDate(this.dateUtils
							.stdDate(evt.get(CalFields.DTEND)))));
		} else {
			eDate = this.dateUtils.setEndOf(sDate);
			LOG.warn("WARNING - End date missing on: " + evt + " - using: "
					+ eDate.toDateTime());
		}

		if (this.dateUtils.isOnBefore(untilDate, eDate)) {
			LOG.warn("WARNING - Calendar anomoly! untilDate earlier that endDate - setting untilDate to endDate");
			untilDate = eDate;
		}

		this.debug("About to add repeats for Repeat Rule: " + rType
				+ " - Increment: " + increment + " - Until: "
				+ untilDate.toDateTime());
		this.debug("Event: " + evt);
		List<LocalDateTime> excludedDates = eu.getExcludedDates(evt);

		while (this.dateUtils.isOnBefore(eDate, untilDate)) {
			if (this.dateUtils.isRecent(eDate)) {
				if (!excludedDates.contains(sDate)) {
					this.debug("\tAdding rept'g evt:" + evt.get(CalFields.SUMMARY) + " on " + sDate.toString());
					eu.addEvent(events, evt, sDate, eDate);
				} else {
					this.debug("\tExcluded date: " + sDate + " - not added because it's in " + excludedDates);
				}
			} 
			sDate = this.dateUtils.dateRoll(sDate, rType, increment);
			eDate = this.dateUtils.dateRoll(eDate, rType, increment);
		}
	}
	
	private void debug(String msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(msg);
		}
	}
}
