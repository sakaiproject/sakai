/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
			Locale locale = rl.getLocale();
			
			Calendar midnightThisMorning = Calendar.getInstance(locale);
			midnightThisMorning.set(midnightThisMorning.get(Calendar.YEAR), midnightThisMorning.get(Calendar.MONTH), midnightThisMorning.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			
			Calendar midnightTonight = Calendar.getInstance(locale);
			midnightTonight.setTimeInMillis(midnightThisMorning.getTimeInMillis() + ONE_DAY_IN_MILLIS);
			
			Calendar yesterday = Calendar.getInstance(locale);
			yesterday.setTimeInMillis(midnightThisMorning.getTimeInMillis() - ONE_DAY_IN_MILLIS);
			
			Calendar beginningOfThisYear = Calendar.getInstance(locale);
			beginningOfThisYear.set(beginningOfThisYear.get(Calendar.YEAR), 1, 1, 0, 0, 0);
			
			Calendar midnightTomorrow = Calendar.getInstance(locale);
			midnightTomorrow.setTimeInMillis(midnightTonight.getTimeInMillis() + ONE_DAY_IN_MILLIS);
			
			Calendar beginningOfNextYear = Calendar.getInstance(locale);
			beginningOfNextYear.set(beginningOfNextYear.get(Calendar.YEAR) + 1,1,1,0,0,0);
			
			if(date.before(beginningOfThisYear.getTime())) {
				// Any posting date before the current year will be displayed with the Month abbreviation; Date Year ; HH:MM PM
				// 	Example: OCT 30, 2015 1:00PM
				DateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm a", locale);
				timeStr = df.format(date);
			} else if(date.before(yesterday.getTime())) {
				// Any posting date within the current year and before yesterday will be displayed with the Month abbreviation; Date ; HH:MM PM
				// 	Example: OCT 30 1:00PM
				DateFormat df = new SimpleDateFormat("MMM dd hh:mm a", locale);
				timeStr = df.format(date);
			} else if(date.before(midnightThisMorning.getTime())) {
				// Any posting date within yesterday will be displayed with Yesterday ; HH:MM PM
				// 	Example: Yesterday 1:00PM
				DateFormat df = new SimpleDateFormat("hh:mm a", locale);
				timeStr = rl.getFormattedMessage("dash.date.yesterday.time", new String[]{ df.format(date) });
			} else if(date.before(midnightTonight.getTime())) {
				// Any posting date that equals the current date will display "Today"; HH:MM PM.
				//	 	Example: Today 5:00 PM
				DateFormat df = SimpleDateFormat.getTimeInstance(DateFormat.SHORT, locale);
				timeStr = rl.getFormattedMessage("dash.date.today.time", new String[]{ df.format(date) });
			} else if(date.before(midnightTomorrow.getTime())) {
				// Any posting date that is equal to current date + 1 Day will be display "Tomorrow"; HH:MM PM
				//	 	Example: Tomorrow 9:00 AM
				DateFormat df = SimpleDateFormat.getTimeInstance(DateFormat.SHORT, locale);
				timeStr = rl.getFormattedMessage("dash.date.tomorrow.time", new String[]{ df.format(date) });
			} else if(date.before(beginningOfNextYear.getTime())) {
				// Any posting date greater than current date + 1Day will be displayed with the Month abbreviation; Date ; HH:MM PM
				// 	Example: OCT 30 1:00PM
				DateFormat df = new SimpleDateFormat("MMM dd hh:mm a", locale);
				timeStr = df.format(date);
			} else {
				// Any posting date greater than current date + 1Day will be displayed with the Month abbreviation; Date ; HH:MM PM
				// 	Example: OCT 30, 2015 1:00PM
				DateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm a", locale);
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
			Locale locale = rl.getLocale();
			
			Date now = new Date();
			long millis_since_midnight = now.getTime() % ONE_DAY_IN_MILLIS;
			Calendar new_year = Calendar.getInstance(locale);
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
				DateFormat df = new SimpleDateFormat("MMM dd, yyyy", locale);
				timeStr = df.format(date);
			} else if(date.before(midnightYesterday)) {
				// Any posting date that is 2 Days or more before the current date will be display Month abbreviation; date
				// 	Example: May 30
				// TODO: Is there a way to avoid including year and still 
				DateFormat df = new SimpleDateFormat("MMM dd", locale);
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
				if(hours <= 1) {
					timeStr = rl.getString("dash.date.hour.ago");
				} else {
					timeStr = rl.getFormattedMessage("dash.date.hours.ago", new Long[]{ hours });
				}
			} else {
				// Any posting date that is less than 1 hour before the current time will display "X minutes ago"
				// 	Example: 6 minutes ago
				long minutes = ((now.getTime() - date.getTime()) / ONE_MINUTE_IN_MILLIS) 
					+ (((now.getTime() - date.getTime()) % ONE_MINUTE_IN_MILLIS) > ONE_MINUTE_IN_MILLIS/2 ? 1 : 0);
				if(minutes <= 1) {
					timeStr = rl.getString("dash.date.minute.ago");
				} else {
					timeStr = rl.getFormattedMessage("dash.date.minutes.ago", new Long[]{ minutes });
				}
			}
		}
		
		return timeStr;
	}
	
	public static String getFullDateString(Date date) {
		ResourceLoader rl = new ResourceLoader("dash_entity");
		Locale locale = rl.getLocale();
		DateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm a", locale);
		return df.format(date);
	}

}
