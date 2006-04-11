/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
