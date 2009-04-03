/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mock.domain;

import java.util.Calendar;
import java.util.Date;

import org.sakaiproject.time.api.TimeBreakdown;

public class Time implements org.sakaiproject.time.api.Time, TimeBreakdown {
	private static final long serialVersionUID = 1L;
	
	Calendar date;

	public Time(Date date) {
		this.date = Calendar.getInstance();
		this.date.setTime(date);
	}
	
	public Object clone() {
		return new Time(date.getTime());
	}
	
	public boolean after(org.sakaiproject.time.api.Time other) {
		return date.after( ((Time)other).date);
	}

	public boolean before(org.sakaiproject.time.api.Time other) {
		return date.before( ((Time)other).date);
	}

	public TimeBreakdown breakdownGmt() {
		// TODO What should this do?
		return this;
	}

	public TimeBreakdown breakdownLocal() {
		return this;
	}

	public String getDisplay() {
		return date.toString();
	}

	public long getTime() {
		return date.getTimeInMillis();
	}

	public void setTime(long value) {
		date.setTimeInMillis(value);
	}

	public String toStringFilePath() {
		return Long.toString(date.getTimeInMillis());
	}

	public String toStringGmtDate() {
		return date.toString();
	}

	public String toStringGmtFull() {
		return date.toString();
	}

	public String toStringGmtShort() {
		return date.toString();
	}

	public String toStringGmtTime() {
		return date.toString();
	}

	public String toStringLocal() {
		return date.toString();
	}

	public String toStringLocalDate() {
		return date.toString();
	}

	public String toStringLocalFull() {
		return date.toString();
	}

	public String toStringLocalFullZ() {
		return date.toString();
	}

	public String toStringLocalShort() {
		return date.toString();
	}

	public String toStringLocalShortDate() {
		return date.toString();
	}

	public String toStringLocalTime() {
		return date.toString();
	}

	public String toStringLocalTime24() {
		return date.toString();
	}

	public String toStringLocalTimeZ() {
		return date.toString();
	}

	public String toStringRFC822Local() {
		return date.toString();
	}

	public String toStringSql() {
		return date.toString();
	}

	public int compareTo(Object o) {
		return date.getTime().compareTo(((Calendar)o).getTime());
	}

	public int getDay() {
		return date.get(Calendar.DAY_OF_MONTH);
	}

	public int getHour() {
		return date.get(Calendar.HOUR_OF_DAY);
	}

	public int getMin() {
		return date.get(Calendar.MINUTE);
	}

	public int getMonth() {
		return date.get(Calendar.MONTH);
	}

	public int getMs() {
		return date.get(Calendar.MILLISECOND);
	}

	public int getSec() {
		return date.get(Calendar.SECOND);
	}

	public int getYear() {
		return date.get(Calendar.YEAR);
	}

	public void setDay(int day) {
		date.set(Calendar.DAY_OF_MONTH, day);
	}

	public void setHour(int hour) {
		date.set(Calendar.HOUR_OF_DAY, hour);
	}

	public void setMin(int minute) {
		date.set(Calendar.MINUTE, minute);
	}

	public void setMonth(int month) {
		date.set(Calendar.MONTH, month);
	}

	public void setMs(int millisecond) {
		date.set(Calendar.MILLISECOND, millisecond);
	}

	public void setSec(int second) {
		date.set(Calendar.SECOND, second);
	}

	public void setYear(int year) {
		date.set(Calendar.YEAR, year);
	}

}
