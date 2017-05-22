package org.sakaiproject.gradebookng.business.util;

import org.apache.commons.lang.time.StopWatch;

import lombok.extern.slf4j.Slf4j;

/**
 * Stopwatch extension that times pieces of the logic to determine impact on any modifications.
 */
@Slf4j
public class GbStopWatch extends StopWatch {

	public void time(final String msg, final long time) {
		log.debug("Time for [" + msg + "] was: " + time + "ms");
	}

	public void timeWithContext(final String context, final String msg, final long time) {
		log.debug("Time for [" + context + "].[" + msg + "] was: " + time + "ms");
	}
}
