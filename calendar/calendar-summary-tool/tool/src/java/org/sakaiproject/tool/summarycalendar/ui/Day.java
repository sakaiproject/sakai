/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.summarycalendar.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Day {
	public final static String	STYLE_TODAY						= "calToday";
	public final static String	STYLE_SELECTEDDAY				= "calSelectedDay";
	public final static String	STYLE_WITH_ACTIVITY				= "calDayWithActivity";
	public final static String	STYLE_WITHOUT_ACTIVITY			= "calDayWithoutActivity";
	public final static String	STYLE_OTHER_WITHOUT_ACTIVITY	= "calOtherDayWithNoActivity";

	Date						date							= null;
	String						styleClass						= "";
	boolean						hasEvents						= false;

	int							dayOfMonth						= -1;
	boolean						isToday							= false;
	boolean						occursInOtherMonth				= false;
	boolean						isSelected						= false;

	public Day() {
	}

	public Day(Date date, boolean hasEvents) {
		setHasEvents(hasEvents);
		setDate(date);
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDateAsString() {
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
		return formatter.format(date);
	}

	public int getDayOfMonth() {
		if(dayOfMonth == -1){
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		}
		return dayOfMonth;
	}

	public boolean getHasEvents() {
		return hasEvents;
	}

	public void setHasEvents(boolean hasEvents) {
		this.hasEvents = hasEvents;
	}

	public String getStyleClass() {
		StringBuffer buff = new StringBuffer();
		if(isToday) buff.append(" " + STYLE_TODAY + " ");
		if(occursInOtherMonth && !hasEvents) buff.append(" " + STYLE_OTHER_WITHOUT_ACTIVITY + " ");
		else{
			if(hasEvents) buff.append(" " + STYLE_WITH_ACTIVITY + " ");
			else buff.append(" " + STYLE_WITHOUT_ACTIVITY + " ");
			if(isSelected) buff.append(" " + STYLE_SELECTEDDAY + " ");
		}
		this.styleClass = buff.toString();
		return this.styleClass;
	}

	public void setToday(boolean val) {
		this.isToday = val;
	}

	public void setOccursInOtherMonth(boolean val) {
		this.occursInOtherMonth = val;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

}
