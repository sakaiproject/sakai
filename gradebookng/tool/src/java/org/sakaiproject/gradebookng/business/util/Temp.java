package org.sakaiproject.gradebookng.business.util;

public class Temp {

	public static void time(String msg, long time) {
		System.out.println("Time for [" + msg + "] was: " + time + "ms");
	}
	
	public static void timeWithContext(String context, String msg, long time) {
		System.out.println("Time for [" + context + "].[" + msg + "] was: " + time + "ms");
	}
}
