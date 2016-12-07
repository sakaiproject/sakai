/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.chat2.tool;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.jsf.util.ConversionUtil;

import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.time.cover.TimeService;

/**
 * A utility to help deal with common tasks in JSF.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class JsfUtil {
	private static final Logger log = LoggerFactory.getLogger(JsfUtil.class);

	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components.  This pattern defines how times should
	 * be displayed in the Section Info UI.
	 */
	public static final String TIME_PATTERN_DISPLAY = "h:mm";


	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components.  This pattern defines how to parse and
	 * format complete times (with hours, minutes, and am/pm marker).
	 */
	public static final String TIME_PATTERN_LONG = "h:mm a";

	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components.  This pattern defines how to parse and
	 * format abberviated times (with only hours and am/pm marker).
	 */
	public static final String TIME_PATTERN_SHORT = "h a";

	/**
	 * This is ISO-8601 date validation
	 */
	public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	
	/**
	 * Gets a value from the request parameter map, as provided by the faces
	 * context.
	 *
	 * @param string
	 * @return
	 */
    public static String getStringFromParam(String string) {
    	Map map = FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap();
    	/*for(Object key: map.keySet()){
    		System.out.println(key+":"+map.get(key));
    	}*/
    	
		return (String)FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get(string);
	}

	/**
	 * Converts a string and a boolean (am) into a java.sql.Time object.
	 *
	 * @param str
	 * @param am
	 * @return
	 */
	public static Time convertStringToTime(String str, boolean am) {
		if(StringUtils.trimToNull(str) == null) {
			return null;
		}

		// Set the am/pm flag to ensure that the time is parsed properly
		if(am) {
			str = str + " " + new DateFormatSymbols(new ResourceLoader().getLocale()).getAmPmStrings()[0];
		} else {
			str = str + " " + new DateFormatSymbols(new ResourceLoader().getLocale()).getAmPmStrings()[1];
		}

		String pattern = (str.indexOf(':') != -1) ? JsfUtil.TIME_PATTERN_LONG : JsfUtil.TIME_PATTERN_SHORT;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, new ResourceLoader().getLocale());
		sdf.setTimeZone(TimeService.getLocalTimeZone());
		Date date;
		try {
			date = sdf.parse(str);
		} catch (ParseException pe) {
			throw new RuntimeException("A bad date made it through validation!  This should never happen!");
		}
		return ConversionUtil.convertDateToTime(date, am);
	}

	public static String convertTimeToString(Time time) {
		if(time == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(JsfUtil.TIME_PATTERN_DISPLAY, new ResourceLoader().getLocale());
		sdf.setTimeZone(TimeService.getLocalTimeZone());
		return sdf.format(time);
	}
	
	public static String convertTimeToDateString(Time time) {
		if(time == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(JsfUtil.TIME_PATTERN_DISPLAY, new ResourceLoader().getLocale());
		sdf.setTimeZone(TimeService.getLocalTimeZone());
		return sdf.format(time);
	}
	/**
	 * Converts an ISO-8601 formatted string into a Calendar object
	 *
	 * @param str
	 * @return Calendar
	 */
	public static Calendar convertISO8601StringToCalendar(String str) {
		if(StringUtils.trimToNull(str) == null) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(JsfUtil.ISO_8601_DATE_FORMAT);
		sdf.setTimeZone(TimeService.getLocalTimeZone());
		try {
			Date date = sdf.parse(str);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			return cal;
		} catch (Exception e) {
			log.warn("Bad ISO 8601 date in sections: " + str);
		}
		return null;
	}
	
	/**
	 * Converts a date object to an ISO-8601 formatted string 
	 *
	 * @param date
	 * @return String
	 */
	public static String convertCalendarToISO8601String(Date date) {
		if (date == null) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(JsfUtil.ISO_8601_DATE_FORMAT);
		sdf.setTimeZone(TimeService.getLocalTimeZone());
		String str = sdf.format(date);
		return str;

	}

	public static final  Comparator getSelectItemComparator() {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				SelectItem item1 = (SelectItem)o1;
				SelectItem item2 = (SelectItem)o2;
				return item1.getLabel().toString().compareTo(item2.getLabel().toString());
			}
		};
	}


}
