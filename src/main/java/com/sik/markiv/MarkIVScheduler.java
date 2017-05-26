package com.sik.markiv;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.sik.markiv.google.calendar.MarkIVCalendarFeed;

@SpringBootApplication
@EnableScheduling
public class MarkIVScheduler {

	private static final Logger LOG = Logger.getLogger(MarkIVScheduler.class);

	public static void main(String[] args) throws Exception {

		LOG.info("       ```````   ```````                          `..````               ````````````     ```````    \n"
				+ "      -:/oooo:- -:+oooo:-                        `::+ooo:-             `::oooo/+ooo:-   -:oooo/-`   \n"
				+ "      -/shhhh/-`:+hhhhh:-  .----------.`.---------:/hhhy--.----.       `/+hhhh-shhh:-  -/shhho-.    \n"
				+ "     -/sdddddo.-/hddddd:- ./yhhhhhhhhy/:/ohhhyyhhhooddd/.:yhhy+/.      -/ydddo`sddd-.`:+hddd+..     \n"
				+ "    .+smmdymmy.:dmmdmmm/- -+++++/+hmmm/:/ymmmyoooo-hmmh.odmms:-.      `+ommmm-.ymmm:-:odmmd/.`      \n"
				+ "   `+omNN++NNd-hNNosNNN/-/shhhhhyymNNd.-+mNNh..`.:oNNNdmNNd+/--       /odNNNy.-hNNN:-smNNd-.`       \n"
				+ "   +sdNNh./NNNhNNs.sNNN/-oNNNhyyydNNN+-:yNNN/-` :odNNmdddNNNN+/`     `osNNNN:-:hNNN:sNNNh--`        \n"
				+ "  /shddh--/dddddo.-sddd/:dddo..:/dddh-:+dddy-- `symddo.--hddh-:      /sdddds-:+hdddhdddo--          \n"
				+ " -yhNNN+-:+mNNNy--/hNNN/+NNNy+oshNNNo-/hNNN/-` :ydNNm-:/sNNNs-.     `yhNNNN::-odNNNNNm+-.           \n"
				+ " yhNNNy--:sdNNh--.shNNN//mNNNNmsmNNm::omNNd-:  shmNNs-/odNNm::`     /hmNNNy---sdNNNNd:-.            \n"
				+ " yysoo++:-yyso//``yyso++/+o+++/:++++/+osoo++/  sysoo++:ssso+++`     :hyso+++:.ysso++/:`             \n"
				+ "  ``````   ````    ````` `````` ````` ``````    `````   `````         ``````   ``````               \n"
				+ "                                                                                                    ");

		SpringApplication.run(MarkIVScheduler.class);
	}

	@Bean
	MarkIVCalendarFeed getFeed() {
		LOG.info("Initialising Calendar feed...");
		return new MarkIVCalendarFeed();
	}

}
