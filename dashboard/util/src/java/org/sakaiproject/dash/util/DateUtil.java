package org.sakaiproject.dash.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.sakaiproject.util.ResourceLoader;

public class DateUtil {

	private static final long ONE_MINUTE_IN_MILLIS = 60L * 1000L;
	private static final long ONE_HOUR_IN_MILLIS = 60L * ONE_MINUTE_IN_MILLIS;
	private static final long TWELVE_HOURS_IN_MILLIS = 12L * ONE_HOUR_IN_MILLIS;
	private static final long ONE_DAY_IN_MILLIS = 24L * ONE_HOUR_IN_MILLIS;

	public static String getCalendarTimeString(Date date) {
		
		ResourceLoader rl = new ResourceLoader("dash_entity");

		String timeStr = null;
		
		if(date == null) {
			timeStr = rl.getString("dash.date.unknown.time");
		} else {
		
			Calendar midnightTonight = Calendar.getInstance();
			midnightTonight.set(Calendar.HOUR_OF_DAY, 23);
			midnightTonight.set(Calendar.MINUTE, 59);
			midnightTonight.set(Calendar.SECOND, 59);
			midnightTonight.set(Calendar.MILLISECOND, 999);
			
			Calendar midnightTomorrow = Calendar.getInstance();
			midnightTomorrow.setTimeInMillis(midnightTonight.getTimeInMillis() + ONE_DAY_IN_MILLIS);
			
			Calendar newYearsEve = Calendar.getInstance();
			newYearsEve.set(midnightTonight.get(Calendar.YEAR),12,31,23,59,59);
			
			if(date.before(midnightTonight.getTime())) {
				// Any posting date that equals the current date will display "Today"; HH:MM PM.
				//	 	Example: Today 5:00 PM
				DateFormat df = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
				timeStr = rl.getFormattedMessage("dash.date.today.time", new String[]{ df.format(date) });
			} else if(date.before(midnightTomorrow.getTime())) {
				// Any posting date that is equal to current date + 1 Day will be display "Tomorrow"; HH:MM PM
				//	 	Example: Tomorrow 9:00 AM
				DateFormat df = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
				timeStr = rl.getFormattedMessage("dash.date.tomorrow.time", new String[]{ df.format(date) });
			} else if(date.before(newYearsEve.getTime())) {
				// Any posting date greater than current date + 1Day will be displayed with the Month abbreviation; Date ; HH:MM PM
				// 	Example: OCT 30 1:00PM
				DateFormat df = new SimpleDateFormat("MMM dd hh:mm a");
				timeStr = df.format(date);
			} else {
				// Any posting date greater than current date + 1Day will be displayed with the Month abbreviation; Date ; HH:MM PM
				// 	Example: OCT 30, 2015 1:00PM
				DateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
				timeStr = df.format(date);
			}

		}
		
		return timeStr;
	}
	
	public static String getNewsTimeString(Date date) {

		ResourceLoader rl = new ResourceLoader("dash_entity");

		String timeStr = null;
		if(date == null) {
			timeStr = rl.getString("dash.date.unknown.time");
		} else {
			Date now = new Date();
			long millis_since_midnight = now.getTime() % ONE_DAY_IN_MILLIS;
			Calendar new_year = Calendar.getInstance();
			new_year.set(Calendar.MILLISECOND,0);
			new_year.set(Calendar.SECOND,0);
			new_year.set(Calendar.MINUTE,0);
			new_year.set(Calendar.HOUR_OF_DAY,0);
			new_year.set(Calendar.DAY_OF_YEAR,0);
			Date midnightToday = new Date(now.getTime() - millis_since_midnight);
			Date midnightYesterday = new Date(midnightToday.getTime() - ONE_DAY_IN_MILLIS);
			Date twelveHoursAgo = new Date(System.currentTimeMillis() - TWELVE_HOURS_IN_MILLIS);
			Date oneHourAgo = new Date(System.currentTimeMillis() - ONE_HOUR_IN_MILLIS);
			
			if(date.before(new_year.getTime())) {
				// Any posting date that is for a date in a prior year will be display Month abbreviation; date year
				// 	Example: May 30, 2011
				// TODO: Is there a way to avoid including year and still 
				DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
				timeStr = df.format(date);
			} else if(date.before(midnightYesterday)) {
				// Any posting date that is 2 Days or more before the current date will be display Month abbreviation; date
				// 	Example: May 30
				// TODO: Is there a way to avoid including year and still 
				DateFormat df = new SimpleDateFormat("MMM dd");
				timeStr = df.format(date);
			} else if(date.before(midnightToday)) {
				// Any posting date that is 1 Day before the current date will be display "Yesterday"
				timeStr = rl.getString("dash.date.yesterday");
			} else if(date.before(twelveHoursAgo)) {
				// Any posting date between 12 to 24 hours before the current time will display "Today"
				timeStr = rl.getString("dash.date.today");
			} else if(date.before(oneHourAgo)) {
				// Any posting date that is 1 to 12 hours before the current time will display "X hours ago"
				// 	Example: 3 hours ago
				long hours = ((now.getTime() - date.getTime()) / ONE_HOUR_IN_MILLIS) 
					+ (((now.getTime() - date.getTime()) % ONE_HOUR_IN_MILLIS) > ONE_HOUR_IN_MILLIS/2 ? 1 : 0);
				timeStr = rl.getFormattedMessage("dash.date.hours.ago", new Long[]{ hours });
			} else {
				// Any posting date that is less than 1 hour before the current time will display "X minutes ago"
				// 	Example: 6 minutes ago
				long minutes = ((now.getTime() - date.getTime()) / ONE_MINUTE_IN_MILLIS) 
					+ (((now.getTime() - date.getTime()) % ONE_MINUTE_IN_MILLIS) > ONE_MINUTE_IN_MILLIS/2 ? 1 : 0);
				timeStr = rl.getFormattedMessage("dash.date.minutes.ago", new Long[]{ minutes });
			}
		}
		
		return timeStr;
	}
	


}
