/**
 * @author sik
 */
package com.sik.markiv.html;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sik.markiv.api.CalendarEvent;
import com.sik.markiv.api.EventType;
import com.sik.markiv.api.M4Date;
import com.sik.markiv.api.UpdateRecord;
import com.sik.markiv.events.EventManager;
import com.sik.markiv.utils.M4DateUtils;

public class GigsAvailabilityBuilder {
	private static final Logger LOG = Logger
			.getLogger(GigsAvailabilityBuilder.class);

	private static final DateTimeFormatter GIG_DF = DateTimeFormat.forPattern("EEE d MMMM ha");
	private static final DateTimeFormatter AVL_DF = DateTimeFormat.forPattern("EEE d MMM yyyy").withLocale(Locale.UK);

	private static final String GIG = "gig";
	private static final int GIG_DAYS_AHEAD = 183;
	private static final int AVL_DAYS_AHEAD = 400;
	private static final String PRIVATE_PARTY = "Private Party";
	private static final String SUN = "Sun";
	private static final String AVAILABLE = "Available";
	private static final String EMPTY_STRING = "";
	private static final String NEWLINE = "\n";
	private static final String RECENT_ASTER = " *";

	private EventManager eventMgr;
	private HtmlManager htmlMgr;
	private M4DateUtils du;

	public GigsAvailabilityBuilder(final EventManager eventMgr) {
		super();

		this.htmlMgr = new HtmlManager();
		this.du = new M4DateUtils();
		this.eventMgr = eventMgr;
	}

	public String buildGigs(final String header, final String trailer)
			throws IOException {

		final StringBuilder out = new StringBuilder();

		if (header != null) {
			out.append(this.htmlMgr.readFileAsString(header));
		}
		
		out.append(this.gigsBodyBuilder());
		
		if (trailer != null) {
			out.append(this.htmlMgr.readFileAsString(trailer));
		}

		return out.toString();
	}

	public String gigsBodyBuilder() {

		List<CalendarEvent> gigs = eventMgr.getByType(EventType.GIG,
				System.currentTimeMillis(), true);

		final StringBuilder gigsHtml = new StringBuilder();
		// set up title
		gigsHtml.append(HtmlSnips.GIG_HEAD_FMT1)
			.append(HtmlSnips.GIG_HEAD_FMT2)
			.append(HtmlSnips.GIG_HEAD_FMT3)
			.append(HtmlSnips.GIG_HEAD_FMT4)
			.append(HtmlSnips.GIG_HEAD_FMT5)
			.append(HtmlSnips.GIG_HEAD_FMT6);
		StringBuilder location = null;
		if (gigs.size() > 0) {
			for (final CalendarEvent e : gigs) {
				location = new StringBuilder();
				LOG.info("Gig:" + e.getStartDate().toString() + " : " + e.getLocation());
				if (e.getStartDate()
						.isBefore(this.getDateHence(GIG_DAYS_AHEAD))) {
					if (isGig(e)) {
						if (e.isEventPrivate()) {
							location.append(PRIVATE_PARTY);
						} else {
							if (e.getLocation() != null) {
								location.append(HtmlSnips.cleanForHtml(e.getLocation()));
							} else {
								location.append(HtmlSnips.cleanForHtml(e.getSummary()));
							}
							if (!e.getNotes().isEmpty()) {
								location.append(HtmlSnips.HTML_BREAK + HtmlSnips.OPN_BKT);
								location.append(HtmlSnips.cleanForHtml(e.getNotes()));
								location.append(HtmlSnips.CLS_BKT);
							}
						}
						if (location != null && location.length() > 0) {
							gigsHtml.append(String.format(HtmlSnips.GIG1_FORMAT,
											GIG_DF.print(this.du.adjustForDaylightSaving(e.getStartDate()).toDateTime()), 
											getLocName(location)));
							gigsHtml.append(NEWLINE);
							gigsHtml.append(String.format(HtmlSnips.GIG2_FORMAT,
											getLocAddr(location)));
							gigsHtml.append(NEWLINE);
						} else {
							LOG.warn("Location missing for event: " + e);
						}
					}
				}
			}
		} else {
			gigsHtml.append(HtmlSnips.NO_GIGS + NEWLINE);
		}
		gigsHtml.append(HtmlSnips.TR_END + HtmlSnips.TABLE_END + NEWLINE);
		gigsHtml.append(String.format(HtmlSnips.LAST_UPD_FMT, new Date()));
		LOG.info(gigs.size() + " Gig(s) found.");
		return gigsHtml.toString();
	}

	/**
	 * @param location
	 * @return
	 */
	private String getLocAddr(StringBuilder location) {
		return location.toString().indexOf(",") == -1 
			? ""
			: location.toString().substring(location.toString().indexOf(",") + 1);
	}

	/**
	 * @param location
	 * @return
	 */
	private String getLocName(StringBuilder location) {
		return location.toString().indexOf(",") == -1
			? location.toString()
			: location.toString().split(",")[0];
	}

	/**
	 * @param e
	 * @return
	 */
	private boolean isGig(CalendarEvent e) {
		return e.getSummary().toLowerCase().contains(GIG);
	}

	public String buildAvail(final String header, final String trailer)
			throws IOException {
		final StringBuilder out = new StringBuilder();

		if (header != null) {
			out.append(this.htmlMgr.readFileAsString(header));
		}
		out.append(this.availBodyBuilder());
		if (trailer != null) {
			out.append(this.htmlMgr.readFileAsString(trailer));
		}

		return out.toString();
	}

	public String availBodyBuilder() {

		final StringBuilder availHtml = new StringBuilder();

		final UpdateRecord ur = this.eventMgr.getLatestUpdate();

		availHtml.append(HtmlSnips.TABLE);
		availHtml.append(HtmlSnips.TR + HtmlSnips.TD + NEWLINE);
		availHtml.append(HtmlSnips.AVAIL_HEAD);
		availHtml.append(HtmlSnips.TD_END + HtmlSnips.TR_END + NEWLINE);
		availHtml.append(HtmlSnips.TR + HtmlSnips.TD + NEWLINE);
		availHtml.append(String.format(HtmlSnips.N1 + HtmlSnips.CAL_LAST_UPD_FMT + HtmlSnips.N1_END + NEWLINE, ur
						.getLastUpdated().toLocalDate()));
		availHtml.append(String.format(HtmlSnips.LAST_UPD_FMT, new Date()));
		availHtml.append(HtmlSnips.TD_END + HtmlSnips.TR_END + NEWLINE);
		availHtml.append(HtmlSnips.TR + HtmlSnips.TD + NEWLINE);
		availHtml.append(HtmlSnips.N1 + NEWLINE);

		M4Date rolling = new M4Date();

		int availTotal = 0;
		boolean dateClear = true;
		StringBuilder remarks = null;
		for (int i = 0; i < AVL_DAYS_AHEAD; i++) {

			// LOG.info("Checking: " + rolling.getStartTime().toDate());

			final List<CalendarEvent> eventsOnDate = this.eventMgr
					.getEventsOn(rolling);
			remarks = new StringBuilder();
			dateClear = (eventsOnDate.size() == 0);

			if (dateClear) {
				availHtml.append(String.format(HtmlSnips.FONT_COLOR_FMT, HtmlSnips.GREEN));
				availTotal++;
			} else {
				this.debug(rolling.getStartTime().toLocalDate() + ": "
						+ eventsOnDate.size() + " event records found");
				for (CalendarEvent ce : eventsOnDate) {
					this.debug("\tce=" + ce);
				}
				boolean unavailable = false;
				for (final CalendarEvent e : eventsOnDate) {
					if (e.getEventType() == EventType.UNAVAILABILITY) {
						unavailable = true;
					}
					if (!remarks.toString().isEmpty()) {
						remarks.append(HtmlSnips.SLASH);
					}
					remarks.append(e.getSummary());
					if (e.getLocation() != null && !e.getLocation().isEmpty()
							&& !e.isEventPrivate()) {
						remarks.append(HtmlSnips.OPN_BKT);
						remarks.append(e.getLocation());
						remarks.append(HtmlSnips.CLS_BKT);
					}
					if (this.isRecentlyUpdated(e.getLastUpdated())) {
						remarks.append(RECENT_ASTER);
					}
				}
				if (unavailable) {
					availHtml.append(String.format(HtmlSnips.FONT_COLOR_FMT, HtmlSnips.RED));
				} else {
					availHtml.append(String
							.format(HtmlSnips.FONT_COLOR_FMT, HtmlSnips.AMBER));
				}

			}
			availHtml.append(String.format(HtmlSnips.AVAIL_FMT, 
					AVL_DF.print(rolling.getStartTime()), 
					dateClear ? AVAILABLE:EMPTY_STRING, 
					remarks.toString()));
			availHtml.append(HtmlSnips.FONT_END + NEWLINE);

			if (AVL_DF.print(rolling.getStartTime()).startsWith(SUN)) {
				availHtml.append(HtmlSnips.HTML_HR + NEWLINE);
			}
			rolling.rollDate(1);
		}
		availHtml.append(HtmlSnips.TD_END + HtmlSnips.TR_END + NEWLINE);
		availHtml.append(HtmlSnips.TABLE_END);
		LOG.info("Availability: " + availTotal + "/" + AVL_DAYS_AHEAD
				+ " days available.");
		return availHtml.toString();
	}

	private LocalDateTime getDateHence(final int daysAhead) {
		return new LocalDateTime().plusDays(daysAhead);
	}

	

	private Boolean isRecentlyUpdated(final LocalDateTime modDate) {
		return modDate.plusDays(7).isAfter(
				new LocalDateTime(System.currentTimeMillis()));
	}
	
	private void debug(String msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(msg);
		}
	}
}