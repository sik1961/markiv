package com.sik.markiv.events;
/**
 * @author sik
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
//import org.springframework.beans.factory.annotation.Autowired;

import com.sik.markiv.api.CalendarEvent;
import com.sik.markiv.api.EventType;
import com.sik.markiv.api.M4Date;
import com.sik.markiv.api.RepeatType;
import com.sik.markiv.api.UpdateRecord;
import com.sik.markiv.exception.MarkIVException;
import com.sik.markiv.utils.M4DateUtils;

public class EventManager {
	private static final Logger LOG = Logger.getLogger(EventManager.class);

	private List<CalendarEvent> allEvents;
	private final M4DateUtils dateUtils = new M4DateUtils();

	public EventManager(InputStream feed) {
		this.updateFromFeed(feed);
	}

	public void updateFromFeed(InputStream feed) {
		this.allEvents = this.buildEvents(this.buildMapFromCalendarFeed(feed));
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
			
			if (isDateInEvent(dateTime, event)
					|| isEventInDate(dateTime, event)) {
				filteredEvents.add(event);
			}
		}
		return filteredEvents;
	}

	private boolean isDateInEvent(final M4Date date, final CalendarEvent event) {
		this.debug(">>>> " + event.getStartDate() + "-" + event.getEndDate() + " : " + date);
		return (this.dateUtils.isOnAfter(date.getStartTime(),
				event.getStartDate()) && this.dateUtils.isOnBefore(
				date.getEndTime(), event.getEndDate()));
	}

	private boolean isEventInDate(final M4Date date, final CalendarEvent event) {
		this.debug(">>>> " + event.getStartDate() + "-" + event.getEndDate() + " : " + date);
		return (this.dateUtils.isOnAfter(event.getStartDate(),
				date.getStartTime()) && this.dateUtils.isOnBefore(
				event.getEndDate(), date.getEndTime()));
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
		final LocalDateTime fromDate = new LocalDateTime(startTime);
		final List<CalendarEvent> filteredEvents = new ArrayList<CalendarEvent>();
		for (final CalendarEvent e : this.allEvents) {
			if (e.getStartDate().isAfter(fromDate)) {
				if (e.getEventType() == eventType) {
					if (!suppressDupes || !eventExists(e, filteredEvents)) {
						filteredEvents.add(e);
					}
				}
			}
		}
		return filteredEvents;
	}

	/**
	 * Check event exists with same Start Date (YYYYMMDD) & Type
	 * 
	 * @param e
	 * @param filteredEvents
	 * @return
	 */
	private boolean eventExists(CalendarEvent e,
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
	private boolean startDatesEqual(CalendarEvent e, CalendarEvent fe) {
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

	private void processLine(final String line,
			final Map<String, String> eventMap) {
		int sp = 0;
		String key = null;
		String value = null;
		if (line.startsWith("DTSTART;") || line.startsWith("DTEND;")
				|| line.startsWith("EXDATE;")) {
			sp = line.indexOf(";");
		} else if (line.startsWith("RRULE:")) {
			sp = line.lastIndexOf("=");
		} else {
			sp = line.indexOf(":");
		}
		if (sp > 0) {
			key = line.substring(0, sp);
			value = line.substring(sp + 1);
			if (key.equals("EXDATE")) {
				if (eventMap.get("EXDATE") == null) {
					eventMap.put(key, this.dateUtils.stdDate(value));
				} else {
					eventMap.put(
							key,
							eventMap.get(key) + ","
									+ this.dateUtils.stdDate(value));
				}
			} else {
				eventMap.put(key, value);
			}
		}
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

			if (rrulePresent(evt)) {
				doRepeats(evt, events);
			} else {
				LocalDateTime sDate = this.dateUtils.formatDate(this.dateUtils
						.stdDate(evt.get("DTSTART")));
				LocalDateTime eDate = this.dateUtils.formatDate(this.dateUtils
						.stdDate(evt.get("DTEND")));
				if (this.dateUtils.isRecent(eDate)) {
					this.debug("\tAdding single: " + evt.get("SUMMARY") + ":" + evt.get("DTSTART"));
					addEvent(events, evt, sDate, eDate);
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
		if (evt.get("RRULE:FREQ=DAILY;UNTIL") != null) {
			untilDate = this.dateUtils.setEndOf(new LocalDateTime(
					this.dateUtils.formatDate(this.dateUtils.stdDate(evt
							.get("RRULE:FREQ=DAILY;UNTIL")))));
			rType = RepeatType.DAILY;
			increment = 1;
		} else if (evt.get("RRULE:FREQ=WEEKLY;UNTIL") != null) {
			untilDate = this.dateUtils.setEndOf(new LocalDateTime(
					this.dateUtils.formatDate(this.dateUtils.stdDate(evt
							.get("RRULE:FREQ=WEEKLY;UNTIL")))));
			rType = RepeatType.WEEKLY;
			increment = 7;
		} else if (evt.get("RRULE:FREQ=MONTHLY;UNTIL") != null) {
			untilDate = this.dateUtils.setEndOf(new LocalDateTime(
					this.dateUtils.formatDate(this.dateUtils.stdDate(evt
							.get("RRULE:FREQ=MONTHLY;UNTIL")))));
			rType = RepeatType.MONTHLY;
			increment = 1;
		} else {
			throw new IllegalArgumentException("Invalid RRULE found");
		}

		LocalDateTime sDate = null;
		LocalDateTime eDate = null;

		if (evt.get("DTSTART") != null) {
			sDate = new LocalDateTime(this.dateUtils.formatDate(this.dateUtils
					.stdDate(evt.get("DTSTART"))));
		} else {
			throw new IllegalStateException("DTSTART is null for event: " + evt);
		}
		if (evt.get("DTEND") != null) {
			eDate = new LocalDateTime(
					this.dateUtils.subtractOneMillisecond(this.dateUtils.formatDate(this.dateUtils
							.stdDate(evt.get("DTEND")))));
		} else {
			eDate = this.dateUtils.setEndOf(sDate);
			this.debug("WARNING - End date missing on: " + evt + " - using: "
					+ eDate.toDateTime());
		}

		if (this.dateUtils.isOnBefore(untilDate, eDate)) {
			this.debug("WARNING - Calendar anomoly! untilDate earlier that endDate - setting untilDate to endDate");
			untilDate = eDate;
		}

		this.debug("About to add repeats for Repeat Rule: " + rType
				+ " - Increment: " + increment + " - Until: "
				+ untilDate.toDateTime());
		this.debug("Event: " + evt);
		List<LocalDateTime> excludedDates = getExcludedDates(evt);

		while (this.dateUtils.isOnBefore(eDate, untilDate)) {
			if (this.dateUtils.isRecent(eDate)) {
				if (!excludedDates.contains(sDate)) {
					this.debug("\tAdding rept'g evt:" + evt.get("SUMMARY") + " on " + sDate.toString());
					addEvent(events, evt, sDate, eDate);
				} else {
					this.debug("\tExcluded date: " + sDate + " not added");
					this.debug("Not added: " + sDate + " in " + excludedDates);
				}
			} 
			sDate = this.dateUtils.dateRoll(sDate, rType, increment);
			eDate = this.dateUtils.dateRoll(eDate, rType, increment);
		}
	}

	/**
	 * @param evt
	 * @return
	 */
	private List<LocalDateTime> getExcludedDates(Map<String, String> evt) {
		List<LocalDateTime> retVal = new ArrayList<LocalDateTime>();
		if (evt.get("EXDATE") != null) {
			String[] exDates = evt.get("EXDATE").split(",");
			for (String xd : exDates) {
				retVal.add(this.dateUtils.formatDate(xd));
			}
		}
		return retVal;
	}
	
	private void addEvent(List<CalendarEvent> events,
			final Map<String, String> evt, LocalDateTime sDate,
			LocalDateTime eDate) {
		events.add(new CalendarEvent(sDate, eDate, evt
				.get("SUMMARY"), evt.get("LOCATION"), evt
				.get("DESCRIPTION"), this.getLastUpdated(evt), evt
				.get("ORGANIZER;CN"), getEventType(evt), this
				.isEventPrivate(evt)));
	}

	private EventType getEventType(final Map<String, String> evt) {
		if (evt.get("SUMMARY").toLowerCase().contains("gig")) {
			return EventType.GIG;
		} else if (evt.get("SUMMARY").toLowerCase().contains("unavailable")
				|| evt.get("SUMMARY").toLowerCase().contains("not available")) {
			return EventType.UNAVAILABILITY;
		} else {
			return EventType.INFO;
		}
	}

	/**
	 * @return
	 */
	private List<HashMap<String, String>> buildMapFromCalendarFeed(
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
				if (line.startsWith("BEGIN:VEVENT")) {
					isEvent = true;
					eventMap = new HashMap<String, String>();
				} else if (line.startsWith("END:VEVENT")) {
					eventsList.add(eventMap);
					isEvent = false;
				} else if (line.startsWith("BEGIN:VALARM")) {
					isAlarm = true;
				} else if (line.startsWith("END:VALARM")) {
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

	private LocalDateTime getLastUpdated(final Map<String, String> evt) {
		return this.dateUtils.formatDate(this.dateUtils.stdDate(evt
				.get("LAST-MODIFIED")));
	}

	private boolean rrulePresent(Map<String, String> evt) {
		return !(evt.get("RRULE:FREQ=DAILY;UNTIL") == null
				&& evt.get("RRULE:FREQ=WEEKLY;UNTIL") == null && evt
					.get("RRULE:FREQ=MONTHLY;UNTIL") == null);
	}

	private Boolean isEventPrivate(final Map<String, String> e) {
		Boolean isPrivate;
		if (e.get("SUMMARY").toLowerCase().contains("private")
				|| e.get("SUMMARY").toLowerCase().contains("wedding")
				|| (e.get("CLASS") != null && e.get("CLASS").toLowerCase()
						.contains("private"))) {
			isPrivate = true;
		} else {
			isPrivate = false;
		}
		return isPrivate;
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

	private void debug(String msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(msg);
		}
	}
}
