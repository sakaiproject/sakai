/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.util;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Provides methods helpful in making object conversions not provided for by
 * the Sun or MyFaces distributions.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class ConversionUtil {

	/**
	 * The JSF DateTimeConverter can not convert into java.sql.Time (or into
	 * java.util.Calendar, for that matter!).  So we do the conversion manually.
	 * 
	 * @param date The date containing the time.
	 * @param am Whether this should be am (true) or pm (false)
	 * @return
	 */
	public static Time convertDateToTime(Date date, boolean am) {
		if(date == null) {
			return null;
		}

		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);

		if(am) {
			// Check to make sure that the hours are indeed am hours
			if( hourOfDay > 11) {
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay -12);
				date.setTime(cal.getTimeInMillis());
			}
		} else {
			// Check to make sure that the hours are indeed pm hours
			if(cal.get(Calendar.HOUR_OF_DAY) < 11) {
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay + 12);
				date.setTime(cal.getTimeInMillis());
			}
		}
		return new Time(date.getTime());
	}

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
