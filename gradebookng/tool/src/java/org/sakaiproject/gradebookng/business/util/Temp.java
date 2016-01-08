package org.sakaiproject.gradebookng.business.util;

public class Temp {

	public static void time(final String msg, final long time) {
		System.out.println("Time for [" + msg + "] was: " + time + "ms");
	}

	public static void timeWithContext(final String context, final String msg, final long time) {
		System.out.println("Time for [" + context + "].[" + msg + "] was: " + time + "ms");
	}
}
