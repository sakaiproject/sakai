/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.section;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSectionSchedule;

/**
 * Parses a meeting times string into its day of week and time of day components.
 * The input string must be formatted like:
 * 
 * <p>
 * 	<code>MON,WED,FRI|9:00am,11:00am</code>
 * </p>
 * 
 * The string to be parsed must follow these rules.
 * 
 * <ul>
 * <li>The days of the week are separated by a comma.</li>
 * <li>The start time and end time are separated by a comma</li>
 * <li>The start and end times must end in either 'am' or 'pm'.</li>
 * <li>The list of days and times are separated by a '|'</li>
 * </ul>
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseSectionScheduleImpl implements CourseSectionSchedule, Serializable {
	
	private static final long serialVersionUID = 1L;	

	private static Log log = LogFactory.getLog(CourseSectionScheduleImpl.class);
	
	protected static final String MONDAY = "MON";
	protected static final String TUESDAY = "TUE";
	protected static final String WEDNESDAY = "WED";
	protected static final String THURSDAY = "THU";
	protected static final String FRIDAY = "FRI";
	protected static final String SATURDAY = "SAT";
	protected static final String SUNDAY = "SUN";

	protected static final String LIST_SEPARATOR = ",";
	protected static final String DAY_TIME_SEPARATOR = "@";

	protected static final String AM = "am";
	protected static final String PM = "pm";

	
	protected boolean monday;
	protected boolean tuesday;
	protected boolean wednesday;
	protected boolean thursday;
	protected boolean friday;
	protected boolean saturday;
	protected boolean sunday;
	
	public String startTime;
	public boolean startTimeAm;

	public String endTime;
	public boolean endTimeAm;
	
	public CourseSectionScheduleImpl(String meetingTimes) {
		String[] daysAndTimes = meetingTimes.split(DAY_TIME_SEPARATOR);
		List days = Arrays.asList(daysAndTimes[0].split(LIST_SEPARATOR));
		if(days.contains(MONDAY)) {
			monday = true;
		}
		if(days.contains(TUESDAY)) {
			tuesday = true;
		}
		if(days.contains(WEDNESDAY)) {
			wednesday = true;
		}
		if(days.contains(THURSDAY)) {
			thursday = true;
		}
		if(days.contains(FRIDAY)) {
			friday = true;
		}
		if(days.contains(SATURDAY)) {
			saturday = true;
		}
		if(days.contains(SUNDAY)) {
			sunday = true;
		}
		
		String[] times = daysAndTimes[1].split(LIST_SEPARATOR);
		startTime = times[0].substring(0, times[0].length()-2);
		if(times[0].endsWith(AM)) {
			startTimeAm = true;
		}
		endTime = times[1].substring(0, times[1].length()-2);
		if(times[1].endsWith(AM)) {
			endTimeAm = true;
		}		
	}

	public String getEndTime() {
		return endTime;
	}

	public boolean isEndTimeAm() {
		return endTimeAm;
	}

	public boolean isFriday() {
		return friday;
	}

	public boolean isMonday() {
		return monday;
	}

	public boolean isSaturday() {
		return saturday;
	}

	public String getStartTime() {
		return startTime;
	}

	public boolean isStartTimeAm() {
		return startTimeAm;
	}

	public boolean isSunday() {
		return sunday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}
	

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
