package com.sik.markiv.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;

import com.sik.markiv.api.CalendarEvent;
import com.sik.markiv.api.EventType;
import com.sik.markiv.api.M4Date;
import com.sik.markiv.api.M4Fields;
import com.sik.markiv.exception.MarkIVException;
import com.sik.markiv.google.calendar.CalFields;
import com.sik.markiv.utils.M4DateUtils;

public class EventUtility {

	private static final Logger LOG = Logger.getLogger(EventManager.class);
	private final M4DateUtils dateUtils = new M4DateUtils();
	
	protected boolean isDateInEvent(final M4Date date, final CalendarEvent event) {
		this.debug(">>>> " + event.getStartDate() + "-" + event.getEndDate() + " : " + date);
		return (this.dateUtils.isOnAfter(date.getStartTime(),
				event.getStartDate()) && this.dateUtils.isOnBefore(
				date.getEndTime(), event.getEndDate()));
	}

	protected boolean isEventInDate(final M4Date date, final CalendarEvent event) {
		this.debug(">>>> " + event.getStartDate() + "-" + event.getEndDate() + " : " + date);
		return (this.dateUtils.isOnAfter(event.getStartDate(),
				date.getStartTime()) && this.dateUtils.isOnBefore(
				event.getEndDate(), date.getEndTime()));
	}
	
	protected LocalDateTime getLastUpdated(final Map<String, String> evt) {
		return this.dateUtils.formatDate(this.dateUtils.stdDate(evt
				.get(CalFields.LAST_MODIFIED)));
	}

	protected boolean rrulePresent(Map<String, String> evt) {
		return !(evt.get(CalFields.RRULE_DAILY) == null
				&& evt.get(CalFields.RRULE_WEEKLY) == null 
				&& evt.get(CalFields.RRULE_MONTHLY) == null);
	}

	protected Boolean isEventPrivate(final Map<String, String> e) {
		Boolean isPrivate;
		if (e.get(CalFields.SUMMARY).toLowerCase().contains(M4Fields.PRIVATE)
				|| e.get(CalFields.SUMMARY).toLowerCase().contains(M4Fields.WEDDING)
				|| e.get(CalFields.LOCATION).toLowerCase().contains(M4Fields.PRIVATE)
				|| (e.get(CalFields.CLASS) != null && e.get(CalFields.CLASS).toLowerCase().contains(M4Fields.PRIVATE))) {
			isPrivate = true;
		} else {
			isPrivate = false;
		}
		return isPrivate;
	}
	
	/**
	 * Is the event a gig?
	 * @param e
	 * @return
	 */
	public boolean isGig(CalendarEvent e) {
		return e.getSummary().toLowerCase().contains(M4Fields.GIG);
	}

	/**
	 * Is the event private?
	 * @param e
	 * @return
	 */
	public boolean isEventPrivate(CalendarEvent e) {
		return (e.isEventPrivate() || e.getLocation().toLowerCase().contains(M4Fields.PRIVATE));
	}
	
	/**
	 * Check event exists with same Start Date (YYYYMMDD) & Type
	 * 
	 * @param e
	 * @param filteredEvents
	 * @return
	 */
	protected boolean eventExists(CalendarEvent e,
			List<CalendarEvent> filteredEvents) {
		for (CalendarEvent fe : filteredEvents) {
			if (fe.getEventType() == e.getEventType() && startDatesEqual(e, fe)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param e
	 * @param fe
	 * @return
	 */
	protected boolean startDatesEqual(CalendarEvent e, CalendarEvent fe) {
		if (e.getStartDate().getYearOfCentury() != fe.getStartDate()
				.getYearOfCentury()) {
			return false;
		} else if (e.getStartDate().getMonthOfYear() != fe.getStartDate()
				.getMonthOfYear()) {
			return false;
		} else if (e.getStartDate().getDayOfMonth() != fe.getStartDate()
				.getDayOfMonth()) {
			return false;
		} else {
			return true;
		}
	}

	protected void processLine(final String line,
			final Map<String, String> eventMap) {
		int sp = 0;
		String key = null;
		String value = null;
		if (line.startsWith(CalFields.DTSTART + M4Fields.SEMICOLON) || line.startsWith(CalFields.DTEND + M4Fields.SEMICOLON)
				|| line.startsWith(CalFields.EXDATE + M4Fields.SEMICOLON)) {
			sp = line.indexOf(M4Fields.SEMICOLON);
		} else if (line.startsWith(CalFields.RRULE + M4Fields.COLON)) {
			sp = line.lastIndexOf(M4Fields.EQUALS);
		} else {
			sp = line.indexOf(M4Fields.COLON);
		}
		if (sp > 0) {
			key = line.substring(0, sp);
			value = line.substring(sp + 1);
			if (key.equals(CalFields.EXDATE)) {
				if (eventMap.get(CalFields.EXDATE) == null) {
					eventMap.put(key, this.dateUtils.stdDate(value));
				} else {
					eventMap.put(key, eventMap.get(key) + M4Fields.COMMA + this.dateUtils.stdDate(value));
				}
			} else {
				eventMap.put(key, value);
			}
		}
	}
	
	/**
	 * @param evt
	 * @return
	 */
	protected List<LocalDateTime> getExcludedDates(Map<String, String> evt) {
		List<LocalDateTime> retVal = new ArrayList<LocalDateTime>();
		if (evt.get(CalFields.EXDATE) != null) {
			String[] exDates = evt.get(CalFields.EXDATE).split(M4Fields.COMMA);
			for (String xd : exDates) {
				retVal.add(this.dateUtils.formatDate(xd));
			}
		}
		return retVal;
	}
	
	protected void addEvent(List<CalendarEvent> events,
			final Map<String, String> evt, 
			LocalDateTime sDate,
			LocalDateTime eDate) {
		events.add(new CalendarEvent()
				.withStartDate(sDate)
				.withEndDate(eDate)
				.withSummary(	evt.get(CalFields.SUMMARY))
				.withLocation(evt.get(CalFields.LOCATION))
				.withNotes(evt.get(CalFields.DESCRIPTION))
				.withLastUpdated(this.getLastUpdated(evt))
				.withLastUpdatedBy(evt.get(CalFields.ORGANIZER))
				.withEventType(getEventType(evt))
				.withEventPrivate(this.isEventPrivate(evt))
				.validate());
	}

	protected EventType getEventType(final Map<String, String> evt) {
		if (evt.get(CalFields.SUMMARY).toLowerCase().contains(M4Fields.GIG)) {
			return EventType.GIG;
		} else if (evt.get(CalFields.SUMMARY).toLowerCase().contains(M4Fields.UNAVAILABLE)
				|| evt.get(CalFields.SUMMARY).toLowerCase().contains(M4Fields.NOT_AVAILABLE)) {
			return EventType.UNAVAILABILITY;
		} else {
			return EventType.INFO;
		}
	}

	/**
	 * Builds a List of HashMap 'events' from the google calendar feed
	 * @return
	 */
	protected List<HashMap<String, String>> buildMapFromCalendarFeed(
			final InputStream feed) {
		boolean isEvent = false;
		boolean isAlarm = false;
		BufferedReader br;
		HashMap<String, String> eventMap = null;
		final List<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();

		int lineCount = 0;

		try {
			br = new BufferedReader(new InputStreamReader(feed));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(CalFields.BEGIN_EVENT)) {
					isEvent = true;
					eventMap = new HashMap<String, String>();
				} else if (line.startsWith(CalFields.END_EVENT)) {
					eventsList.add(eventMap);
					isEvent = false;
				} else if (line.startsWith(CalFields.BEGIN_ALARM)) {
					isAlarm = true;
				} else if (line.startsWith(CalFields.END_ALARM)) {
					isAlarm = false;
				} else if (isEvent && !isAlarm) {
					processLine(line, eventMap);
				}
				lineCount++;
			}
			br.close();
		} catch (final IOException e2) {
			throw new MarkIVException(e2.getMessage());
		}

		LOG.info(lineCount + " lines in calendar extract - "
				+ eventsList.size() + " raw events found");
		return eventsList;
	}
	
	
	private void debug(String msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(msg);
		}
	}
	
}
