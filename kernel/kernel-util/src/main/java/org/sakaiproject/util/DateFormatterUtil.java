/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
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
package org.sakaiproject.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

/**
 * Performs date validation respecting i18n.<br>
 * <b>Note:</b> This class does not support "hi_IN", "ja_JP_JP" and "th_TH" locales.
 */
@Slf4j
public final class DateFormatterUtil {

	private static DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

	private DateFormatterUtil() {
	}

	/**
	 * Performs date validation checking the ISO_ZONED_DATE_TIME format such as '2017-12-03T10:15:30+01:00[Europe/Paris]'.
	 *
	 * @param date
	 *            The candidate String date.
	 * @return TRUE - Conforms to a valid input date format string.
	 *         FALSE - Does not conform.
	 */
	public static boolean isValidISODate(final String date) {
		try {
		    isoFormatter.parse(date);
		    return true;
		  } catch (Exception e) { 
			  return false;
		  }
	}
	
	/**
	 * Parse the date string input using the ISO_ZONED_DATE_TIME format such as '2017-12-03T10:15:30+01:00[Europe/Paris]'.
	 * 
	 * @param inputDate
	 *            The string that needs to be parsed.
	 * 
	 * @throws Exception 
	 * 			If not a valid date compared to ISO_ZONED_DATE_TIME format
	 */
	public static Date parseISODate(final String inputDate) {
		Date convertedDate = null;

		try {
			LocalDateTime ldt = LocalDateTime.parse(inputDate, isoFormatter);
			convertedDate = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		} catch (Exception  e) {
			log.error(e.getMessage(), e);
		}

		return convertedDate;
	}
	/**
	 * Formats the date input to String using the format given.
	 * 
	 * @param inputDate
	 *            The date that needs to be formatted.
	 * @param format
	 *            The given date-time format.
	 * @param locale
	 *            The given locale.
	 * @throws ParseException
	 * 			If throws a parse exception then returns the SHORT format by default (MM/dd/yyyy hh:mm a)
	 */
	public static String format(Date inputDate, String format, Locale locale) {
		SimpleDateFormat formatter = null;

		if(inputDate == null){
			return null;
		}

		try {
			formatter = new SimpleDateFormat(format, locale);
			return formatter.format(inputDate);
		} catch(Exception ex) {
			formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
			return formatter.format(inputDate);
		}
	}
}
