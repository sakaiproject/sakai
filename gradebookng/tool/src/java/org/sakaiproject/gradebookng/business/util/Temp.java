package org.sakaiproject.gradebookng.business.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Temp {

	public static void time(final String msg, final long time) {
		log.info("Time for [" + msg + "] was: " + time + "ms");
	}

	public static void timeWithContext(final String context, final String msg, final long time) {
		log.info("Time for [" + context + "].[" + msg + "] was: " + time + "ms");
	}
}
