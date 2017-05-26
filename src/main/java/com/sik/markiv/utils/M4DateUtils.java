package com.sik.markiv.utils;
/**
 * @author sik
 *
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sik.markiv.api.CalendarEvent;
import com.sik.markiv.api.RepeatType;

public class M4DateUtils {
	private static final DateTimeFormatter DFZ = DateTimeFormat.forPattern("YYYYMMDD'T'HHmmSS'Z'");
	private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("YYYYMMDD'T'HHmmSS");
	private static final DateTimeFormatter DFS = DateTimeFormat.forPattern("YYYYMMDD");
	private static final int DST_START_DAY = 85;
	private static final int DST_END_DAY = 300;
	private static final int VALID_DATE_16 = 16;
	private static final int VALID_DATE_15 = 15;
	private static final int VALID_DATE_8 = 8;
	private static final int DAY_END_HH = 23;
	private static final int DAY_END_MM = 59;
	private static final int DAY_END_SS = 59;
	private static final int DAY_END_MS = 999;
	
	private static final String YYYYMMDDHHMMSS = "yyyyMMddhhmmss";
	private static final String YYYYMMDD = "yyyyMMdd";
	public static final int DAY_LIMIT = 7;
	public static final int OBSOLETE_DAY_LIMIT = 2;
	public static final LocalDateTime RECENT_UPDATE_DATE = new LocalDateTime(new Date()).minusDays(DAY_LIMIT);
		
	/**
	 * Parse the input string to isolate the date string. 
	 * @param inDate
	 * @return
	 */
	public String stdDate(final String inDate) {
		String outDate = "";
		if (inDate.contains("VALUE=DATE:")) {
			outDate = inDate.replace("VALUE=DATE:", "");
		} else if (inDate.contains("TZID=Europe/London:")) {
			outDate = inDate.replace("TZID=Europe/London:", "");
		} else {
			outDate = inDate;
		}
		return outDate;
	}

	/**
	 * Roll the date by the supplied number of units.
	 * @param date
	 * @param rType
	 * @param increment
	 * @return
	 */
	public LocalDateTime dateRoll(final LocalDateTime date, final RepeatType rType,
			final int increment) {
		switch (rType) {
		case MONTHLY:
			return date.plusMonths(increment);
		case WEEKLY:
			return date.plusDays(increment * 7);
		case DAILY:
			return date.plusDays(increment);
		}

		return date.plusDays(increment);
	}

	/**
	 * Subtract 1 millisecond from the date.
	 * @param inDate
	 * @return
	 */
	public LocalDateTime subtractOneMillisecond(final LocalDateTime inDate) {
		return inDate.minusMillis(1);
	}

	/**
	 * Format the date.
	 * @param inDate
	 * @return
	 */
	public LocalDateTime formatDate(final String inDate) {
		LocalDateTime outDate = new LocalDateTime(DateTimeZone.UTC.getMillisKeepLocal(DateTimeZone.getDefault(),0));
		
		try {
			if (inDate.length() == VALID_DATE_16) {
				outDate = new LocalDateTime(
						new SimpleDateFormat(YYYYMMDDHHMMSS).parse(inDate
								.replace("T", "").replace("Z", "")));
			} else if (inDate.length() == VALID_DATE_15) {
				outDate = new LocalDateTime(
						new SimpleDateFormat(YYYYMMDDHHMMSS).parse(inDate
								.replace("T", "")));
			} else if (inDate.length() == VALID_DATE_8) {
				outDate = new LocalDateTime(
						new SimpleDateFormat(YYYYMMDD).parse(inDate));
			} else {
				throw new IllegalStateException("Unexpected date format:"
						+ inDate + " - len=" + inDate.length());
			}
		} catch (final ParseException e) {
			throw new IllegalStateException("Unexpected date format:" + inDate);
		}
		return outDate;
	}
	
	/**
	 * Adjust time for daylight saving time.
	 * @param date
	 * @return
	 */
	public LocalDateTime adjustForDaylightSaving(LocalDateTime date) {
		if (date.getDayOfYear() >= DST_START_DAY && date.getDayOfYear() <= DST_END_DAY) {
			return date.plusHours(1);
		} else {
			return date;
		}
	}

	/**
	 * Format the date.
	 * @param inDate
	 * @return
	 */
	public LocalDateTime formatLocalDate(final String inDate) {
		LocalDateTime outDate = new LocalDateTime(DateTimeZone.UTC.getMillisKeepLocal(DateTimeZone.getDefault(),0));
		
		if (inDate.length() == VALID_DATE_16) {
			outDate = new LocalDateTime(DFZ.parseDateTime(inDate));
		} else if (inDate.length() == VALID_DATE_15) {
			outDate = new LocalDateTime(DFT.parseDateTime(inDate));
		} else if (inDate.length() == VALID_DATE_8) {
			outDate = new LocalDateTime(DFS.parseDateTime(inDate));
		} else {
			throw new IllegalStateException("Unexpected date format:"
					+ inDate + " - len=" + inDate.length());
		}
		return outDate;
	}
	
	/**
	 * Is the CalendarEvent recently updated?
	 * @param e
	 * @return
	 */
	public boolean isRecentlyUpdated(final CalendarEvent e) {
		return e.getLastUpdated().isAfter(RECENT_UPDATE_DATE); 
	}
	
	/**
	 * Is date t1 equal or after date t2?
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean isOnAfter(final LocalDateTime t1, final LocalDateTime t2) {
		return t1.isEqual(t2) || t1.isAfter(t2);
	}

	/**
	 * Is date t1 equal or before date t2?
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean isOnBefore(final LocalDateTime t1, final LocalDateTime t2) {
		return t1.isEqual(t2) || t1.isBefore(t2);
	}
	
	/**
	 * Is the date recent?
	 * @param t1
	 * @param OBSOLETE_DAY_LIMIT
	 * @return
	 */
	public boolean isRecent(final LocalDateTime t1) {
		LocalDateTime recent = new LocalDateTime(new Date()).minusDays(OBSOLETE_DAY_LIMIT);
		return t1.isAfter(recent);
	}
	
	/**
	 * Set the date to the 'end' of the supplied date.
	 * @param dateTime
	 * @return
	 */
	public LocalDateTime setEndOf(LocalDateTime dateTime) {
		return new LocalDateTime(dateTime)
			.withHourOfDay(DAY_END_HH)
			.withMinuteOfHour(DAY_END_MM)
			.withSecondOfMinute(DAY_END_SS)
			.withMillisOfSecond(DAY_END_MS);
	}
}
