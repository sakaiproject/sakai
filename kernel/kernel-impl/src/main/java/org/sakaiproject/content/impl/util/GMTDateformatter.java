/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.util;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author ieb
 *
 */
public class GMTDateformatter
{
	public static final TimeZone gmt = TimeZone.getTimeZone("GMT");

	public static Date parse(String formattedDate) {
		//                 01234567890123456  
		//String format = "yyyyMMddHHmmssSSS";
		return parseGregorian(formattedDate).getTime();
	}

	/**
	 * @param time
	 * @return
	 */
	public static String format(Date time)
	{
		GregorianCalendar gc = new GregorianCalendar(gmt);
		gc.setTime(time);
		char[] c = new char[17];
		pad(gc.get(Calendar.YEAR),c,0,4);
		pad(gc.get(Calendar.MONTH)+1,c,4,6);
		pad(gc.get(Calendar.DATE),c,6,8);
		pad(gc.get(Calendar.HOUR_OF_DAY),c,8,10);
		pad(gc.get(Calendar.MINUTE),c,10,12);
		pad(gc.get(Calendar.SECOND),c,12,14);
		pad(gc.get(Calendar.MILLISECOND),c,14,17);
		return new String(c);
	}

	/**
	 * @param i
	 * @param j
	 * @param k
	 */
	private static void pad(int value, char[] ds,  int start, int end)
	{
		char[] v = String.valueOf(value).toCharArray();
		for ( int i = start; i < end; i++ ) {
			if ( i < end-v.length ) {
				ds[i] = '0';
			} else {
				ds[i] = v[i-(end-v.length)];
			}
		}
	}

	/**
	 * @param string
	 * @return
	 */
	public static GregorianCalendar parseGregorian(String formattedDate)
	{
		if ( formattedDate == null || formattedDate.length() != 17 ) {
			return null;
		}
		GregorianCalendar gc =  new GregorianCalendar(gmt);
		int year = Integer.parseInt(formattedDate.substring(0,4));
		int month = Integer.parseInt(formattedDate.substring(4,6));
		int day = Integer.parseInt(formattedDate.substring(6,8));
		int hour = Integer.parseInt(formattedDate.substring(8,10));
		int minute = Integer.parseInt(formattedDate.substring(10,12));
		int second = Integer.parseInt(formattedDate.substring(12,14));
		int millis = Integer.parseInt(formattedDate.substring(14));
		gc.set(Calendar.YEAR, year);
		gc.set(Calendar.MONTH, month-1);
		gc.set(Calendar.DATE, day);
		gc.set(Calendar.HOUR_OF_DAY, hour);
		gc.set(Calendar.MINUTE, minute);
		gc.set(Calendar.SECOND, second);
		gc.set(Calendar.MILLISECOND, millis);
		return gc;
	}

}
