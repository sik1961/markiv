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

	private static final DateTimeFormatter GIG_DF = DateTimeFormat
			.forPattern("EEE d MMMM ha");
	private static final DateTimeFormatter AVL_DF = DateTimeFormat.forPattern(
			"EEE d MMM yyyy").withLocale(Locale.UK);
	private static final String GIG1_FORMAT = "<tr><td align=\"right\"><strong>%s - </strong></td><td><strong>%s</strong></td></tr>";
	private static final String GIG2_FORMAT = "<tr><td align=\"right\"></td><td>%s</td></tr>";
	private static final String HEAD_FMT1 = "<center><b2>Gigs</b2>";
	private static final String HEAD_FMT2 = "<table align=\"center\" border=\"0\">\n";
	private static final String HEAD_FMT3 = " <tr>\n";
	private static final String HEAD_FMT4 = "  <td align=\"right\"><n1>Date/Time - </n1></td>\n";
	private static final String HEAD_FMT5 = "  <td align=\"left\"><n1>Venue(Details)</n1></td><br>\n";
	private static final String HEAD_FMT6 = " </tr>\n";
	private static final String NEWLINE = "\n";
	private static final String RED = "#FF8888";
	private static final String AMBER = "#FFFF88";
	private static final String GREEN = "#88FF88";
	private static final int GIG_DAYS_AHEAD = 183;
	private static final int AVL_DAYS_AHEAD = 400;
	private EventManager em;
	private HtmlManager htmlMgr;
	private M4DateUtils du;
	

	public GigsAvailabilityBuilder(final EventManager em) {
		super();

		this.htmlMgr = new HtmlManager();
		this.du = new M4DateUtils();
		this.em = em;
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

		List<CalendarEvent> gigs = em.getByType(EventType.GIG,
				System.currentTimeMillis(), true);

		final StringBuilder gigsHtml = new StringBuilder();
		// set up title
		gigsHtml.append(HEAD_FMT1).append(HEAD_FMT2).append(HEAD_FMT3).append(HEAD_FMT4).append(HEAD_FMT5).append(HEAD_FMT6);
		StringBuilder location = null;
		if (gigs.size() > 0) {
			for (final CalendarEvent e : gigs) {
				location = new StringBuilder();
				LOG.info("Gig:" + e.getStartDate().toString() + " : " + e.getLocation());
				if (e.getStartDate()
						.isBefore(this.getDateHence(GIG_DAYS_AHEAD))) {
					if (isGig(e)) {
						if (e.isEventPrivate()) {
							location.append("Private Party");
						} else {
							if (e.getLocation() != null) {
								location.append(this.cleanForHtml(e
										.getLocation()));
							} else {
								location.append(this.cleanForHtml(e
										.getSummary()));
							}
							if (!e.getNotes().isEmpty()) {
								location.append("<br>(");
								location.append(this.cleanForHtml(e.getNotes()));
								location.append(")");
							}
						}
						if (location != null && location.length() > 0) {
							gigsHtml.append(String.format(GIG1_FORMAT,
											GIG_DF.print(this.du.adjustForDaylightSaving(e.getStartDate()).toDateTime()), 
											getLocName(location)));
							gigsHtml.append(NEWLINE);
							gigsHtml.append(String.format(GIG2_FORMAT,
											getLocAddr(location)));
							gigsHtml.append(NEWLINE);
						} else {
							LOG.info("Location missing for event: " + e);
						}
					}
				}
			}
		} else {
			gigsHtml.append(String.format("<tr><td colspan=\"2\">%s</td></tr>",
					"No Gigs"));
			gigsHtml.append(NEWLINE);
		}
		gigsHtml.append("</tr></table>" + NEWLINE);
		gigsHtml.append(String.format(
				"<center><br><p>Page last updated at %s</p></center>",
				new Date()));
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
		return e.getSummary().toLowerCase().contains("gig");
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

		final UpdateRecord ur = this.em.getLatestUpdate();

		availHtml.append("<table width=\"1000\">");
		availHtml.append("<tr><td>\n");
		availHtml
				.append("<b1>Mark IV Availability</b1>&nbsp;<n1>* Recently updated</n1>");
		availHtml.append("</td></tr>");
		availHtml.append("<tr><td>\n");
		availHtml.append(String.format(
				"<n1>Calendar Last Updated %s - Page Updated %s</n1>\n", ur
						.getLastUpdated().toLocalDate(), new Date()));
		availHtml.append("</td></tr>\n");
		availHtml.append("<tr><td>\n");
		availHtml.append("<n1>\n");

		M4Date rolling = new M4Date();

		int availTotal = 0;
		boolean dateClear = true;
		StringBuilder remarks = null;
		for (int i = 0; i < AVL_DAYS_AHEAD; i++) {

			// LOG.info("Checking: " + rolling.getStartTime().toDate());

			final List<CalendarEvent> eventsOnDate = this.em
					.getEventsOn(rolling);
			remarks = new StringBuilder("");
			dateClear = (eventsOnDate.size() == 0);

			if (dateClear) {
				availHtml.append(String.format("<font color=\"%s\">", GREEN));
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
						remarks.append("/");
					}
					remarks.append(e.getSummary());
					if (e.getLocation() != null && !e.getLocation().isEmpty()
							&& !e.isEventPrivate()) {
						remarks.append("(");
						remarks.append(e.getLocation());
						remarks.append(")");
					}
					if (this.isRecentlyUpdated(e.getLastUpdated())) {
						remarks.append(" *");
					}
				}
				if (unavailable) {
					availHtml.append(String.format("<font color=\"%s\">", RED));
				} else {
					availHtml.append(String
							.format("<font color=\"%s\">", AMBER));
				}

			}
			availHtml.append(String.format("<li>%s - %s %s", AVL_DF
					.print(rolling.getStartTime()), dateClear ? "Available"
					: "", remarks.toString()));
			availHtml.append("</font>\n");

			if (AVL_DF.print(rolling.getStartTime()).startsWith("Sun")) {
				availHtml.append("<hr>\n");
			}
			rolling.rollDate(1);
		}
		availHtml.append("</td></tr>\n");
		availHtml.append("</table>");
		LOG.info("Availability: " + availTotal + "/" + AVL_DAYS_AHEAD
				+ " days available.");
		return availHtml.toString();
	}

	private LocalDateTime getDateHence(final int daysAhead) {
		return new LocalDateTime().plusDays(daysAhead);
	}

	private String cleanForHtml(final String s) {
		return s != null ? s.replace("\\n", ", ").replace("\\", "")
				.replace("GBP", "&pound;") : null;
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