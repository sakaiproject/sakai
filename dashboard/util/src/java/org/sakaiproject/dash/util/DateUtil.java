package org.sakaiproject.dash.util;

import java.util.Date;

import org.sakaiproject.util.ResourceLoader;

public class DateUtil {

	private static final long ONE_MINUTE_IN_MILLIS = 60L * 1000L;
	private static final long ONE_HOUR_IN_MILLIS = 60L * ONE_MINUTE_IN_MILLIS;
	private static final long TWELVE_HOURS_IN_MILLIS = 12L * ONE_HOUR_IN_MILLIS;
	private static final long ONE_DAY_IN_MILLIS = 24L * ONE_HOUR_IN_MILLIS;

	protected String getCalendarTimeString(Date date) {
		
		ResourceLoader rl = new ResourceLoader("dash_entity");

		String timeStr = null;
		
/*
 * Any posting date that equals the current date will display "Today"; HH:MM PM.
 * 	Example: Today 5:00 PM
 * Any posting date that is equal to current date + 1 Day will be display "Tomorrow"; HH:MM PM
 * 	Example: Tomorrow 9:00 AM
 * Any posting date greater than current date + 1Day will be displayed with the Month abbreviation; Date ; HH:MM PM
 * 	Example: OCT 30 1:00PM
 */
		
		return timeStr;
	}
	
	protected String getNewsTimeString(Date date) {

		ResourceLoader rl = new ResourceLoader("dash_entity");

		String timeStr = null;
/*
 * Any posting date that is less than 1 hour before the current time will display "X minutes ago"
 * 	Example: 6 minutes ago
 * Any posting date that is 1 to 12 hours before the current time will display "X hours ago"
 * 	Example: 3 hours ago
 * Any posting date between 12 to 24 hours before the current time will display "Today"
 * Any posting date that is 1 Day before the current date will be display "Yesterday"
 * Any posting date that is 2 Days or more before the current date will be display Month abbreviation; date
 * 	Example: May 30
 */
		Date now = new Date();
		long millis_since_midnight = (now.getTime() % ONE_DAY_IN_MILLIS);
		Date midnightToday = new Date(now.getTime() - millis_since_midnight);
		Date midnightYesterday = new Date(midnightToday.getTime() - ONE_DAY_IN_MILLIS);
		Date twelveHoursAgo = new Date(System.currentTimeMillis() - TWELVE_HOURS_IN_MILLIS);
		Date oneHourAgo = new Date(System.currentTimeMillis() - ONE_HOUR_IN_MILLIS);
		
		if(date == null) {
			
		} else if(date.before(midnightYesterday)) {
			
		} else if(date.before(midnightToday)) {
			
		} else if(date.before(twelveHoursAgo )) {
			
		} else if(date.before(oneHourAgo)) {
			
			timeStr = rl.getFormattedMessage("", new String[]{  });
		} else {
			// Any posting date that is less than 1 hour before the current time will display "X minutes ago"
			// 	Example: 6 minutes ago
			long minutes = ((now.getTime() - date.getTime()) / ONE_MINUTE_IN_MILLIS) 
				+ (((now.getTime() - date.getTime()) % ONE_MINUTE_IN_MILLIS) > ONE_HOUR_IN_MILLIS/2 ? 1 : 0);
			
			timeStr = rl.getFormattedMessage("dash.news.minutes.ago", new Long[]{ minutes });

		}
		
		return timeStr;
	}
	


}
