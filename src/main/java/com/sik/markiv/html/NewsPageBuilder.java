/**
 * @author sik
 */
package com.sik.markiv.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sik.markiv.api.CalendarEvent;
import com.sik.markiv.api.EventType;
import com.sik.markiv.events.EventManager;
import com.sik.markiv.utils.M4DateUtils;

public class NewsPageBuilder {
	private static final Logger LOG = Logger.getLogger(NewsPageBuilder.class);

	private static final DateTimeFormatter NEWS_GIG_DF = DateTimeFormat
			.forPattern("EEE d MMMM ha");

	private static final String NEWLINE = "\n";
	private static final String NEWS_FORMAT = "<li>Our next gig is on <strong>%s</strong> at <strong>%s</strong> - Hope to see you there<br><br>";
	private static final String HEAD_FMT1 = "<tr><td><center>" + NEWLINE;
	private static final String HEAD_FMT2 = "<img src=\"images/m4front.jpg\" width=\"50%\" height=\"50%\"><br>\n" + NEWLINE;
	private static final String HEAD_FMT3 = " <n1>Photo courtesy of " + NEWLINE;
	private static final String HEAD_FMT4 = "   <a href=\"http://www.elementalcore.com\" target=\"_blank\">Ian Jukes Photography</a>" + NEWLINE;
	private static final String HEAD_FMT5 = " </td>" + NEWLINE;
	private static final String HEAD_FMT6 = "<tr><td><center><b2>News</b2></center></td></tr>";
	private static final String HEAD_FMT7 = "</tr>" + NEWLINE + "<tr><td><center>";
	private static final String TAIL_FMT8 = "</td></tr></table>" + NEWLINE;
	
	private EventManager em;
	private HtmlManager htmlMgr;
	private M4DateUtils du;
	

	public NewsPageBuilder(final EventManager em) {
		super();

		this.htmlMgr = new HtmlManager();
		this.du = new M4DateUtils();
		this.em = em;
	}

	public String buildNews(final String header, final String trailer)
			throws IOException {

		final StringBuilder out = new StringBuilder();

		if (header != null) {
			out.append(this.htmlMgr.readFileAsString(header));
		}
		out.append(this.newsBodyBuilder());
		if (trailer != null) {
			out.append(this.htmlMgr.readFileAsString(trailer));
		}

		return out.toString();
	}

	public String newsBodyBuilder() {
		List<CalendarEvent> gigs = em.getByType(EventType.GIG, System.currentTimeMillis(), true);

		final StringBuilder newsHtml = new StringBuilder();
		// set up title
		newsHtml.append(HEAD_FMT1).append(HEAD_FMT2).append(HEAD_FMT3).append(HEAD_FMT4).append(HEAD_FMT5).append(HEAD_FMT6).append(HEAD_FMT7);
		
		CalendarEvent nextPublicGig = this.getNextPublicGig(gigs);
		if (nextPublicGig != null) {
			LOG.info("Next gig: " + nextPublicGig.toString());
			newsHtml.append(String.format(NEWS_FORMAT, 
					NEWS_GIG_DF.print(this.du.adjustForDaylightSaving(nextPublicGig.getStartDate()).toDateTime()), 
					this.cleanForHtml(nextPublicGig.getLocation())));
		}
		
		for (String newsItem: this.getFixedNewsItems()) {
			LOG.info("News item: " + newsItem);
			newsHtml.append(this.cleanForHtml(newsItem + "<br><br>" + NEWLINE));
		}
				 
		newsHtml.append(String.format(
				"<tr><td><center><p>Page last updated at %s</p></center></td></tr>",
				new Date()));
		
		newsHtml.append(TAIL_FMT8 + NEWLINE);
		
		return newsHtml.toString();
	}

	/**
	 * @param gigs
	 * @return
	 */
	private CalendarEvent getNextPublicGig(List<CalendarEvent> gigs) {
		if (gigs.size() > 0) {
			for (final CalendarEvent e : gigs) {
				if (isGig(e) && !e.isEventPrivate()) { 
					return e;
				}
			}
		}
		return null;
	}

	private List<String> getFixedNewsItems() {
		List<String> fixedNewsItems = new ArrayList<String>();
		//fixedNewsItems.add("<li>Pictures from our gig at The Hillsborough Hotel are on the gallery page - click <a href=\"gallery.htm\">here</a>");
		fixedNewsItems.add("<li>To book <b2>Mark IV</b2> eMail us at <a href=\"mailto:markiv.band@gmail.com\">markiv.band@gmail.com</a>");
		fixedNewsItems.add("<li>We're now registered on	<b2><a href=\"http://www.gigz4u.co.uk/\">GIGZ4U</a></b2> - Use it to find our gigs and other live music in the Sheffield area.");
		fixedNewsItems.add("<li>Listen to our Demo CD \"Drivin' West\" on SoundCloud By clicking <a href=\"https://soundcloud.com/markiv-1/sets/drivin-west\" target=\"-blank\">here</a>");
		return fixedNewsItems; 
	}
	
	/**
	 * @param e
	 * @return
	 */
	private boolean isGig(CalendarEvent e) {
		return e.getSummary().toLowerCase().contains("gig");
	}

	private String cleanForHtml(final String s) {
		return s != null ? s.replace("\\n", ", ").replace("\\", "")
				.replace("GBP", "&pound;") : null;
	}
}