/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.tool.summarycalendar.ui;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.sakaiproject.util.ResourceLoader;

public class Day implements Serializable {
	private static final long	serialVersionUID				= 1136403394613377956L;
	public final static String	STYLE_TODAY						= "calToday";
	public final static String	STYLE_SELECTEDDAY				= "calSelectedDay";
	public final static String	STYLE_WITH_ACTIVITY				= "calDayWithActivity";
	public final static String	STYLE_WITHOUT_ACTIVITY			= "calDayWithoutActivity";
	public final static String	STYLE_OTHER_WITHOUT_ACTIVITY	= "calOtherDayWithNoActivity";
	private transient ResourceLoader				msgs					= new ResourceLoader("calendar");

	Date						date							= null;
	String						styleClass						= "";
	boolean						hasEvents						= false;

	int							dayOfMonth						= -1;
	boolean						isToday							= false;
	boolean						occursInOtherMonth				= false;
	boolean						isSelected						= false;
	
	List						dayEvents						= null;
	
	String 						backgroundCSSProperty			= "";

	public Day() {
	}

	public Day(Calendar c, boolean hasEvents) {
		this.hasEvents = hasEvents;
		this.date = c.getTime();
		this.dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDateAsString() {
		if(date == null) return "";
		SimpleDateFormat formatter = new SimpleDateFormat(msgs.getString("date_link_format"), msgs.getLocale());
		return formatter.format(date);
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}

	public boolean getHasEvents() {
		return hasEvents;
	}

	public void setHasEvents(boolean hasEvents) {
		this.hasEvents = hasEvents;
	}

	public String getStyleClass() {
		StringBuilder buff = new StringBuilder();
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

	public void setBackgroundCSSProperty(String color) {
		this.backgroundCSSProperty = color;
	}
	
	public String getBackgroundCSSProperty() {
		return this.backgroundCSSProperty;
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

	public List getDayEvents() {
		return dayEvents;
	}

	public void setDayEvents(List dayEvents) {
		this.dayEvents = dayEvents;
	}

	public String getEventCount() {
		if(dayEvents == null || dayEvents.size() <= 1)
			return "";
		return "("+dayEvents.size()+")";
	}
}
