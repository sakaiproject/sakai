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

package org.sakaiproject.dash.model;

/**
 * 
 *
 */
public enum ItemType {
	
	CALENDAR_ITEM	(0, CalendarItem.class.getCanonicalName()),
	NEWS_ITEM		(1, NewsItem.class.getCanonicalName());
	
	private int value = 0; 
	private String name = null;
	
	private ItemType(int val, String name) {
		this.value = val;
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public static ItemType fromString(String name) {
		if(name != null && CALENDAR_ITEM.getName().equalsIgnoreCase(name)) {
			return CALENDAR_ITEM;
		} 
		return NEWS_ITEM;
	}

	public static ItemType fromInteger(int val) {
		if(CALENDAR_ITEM.getValue() == val) {
			return CALENDAR_ITEM;
		}
		return NEWS_ITEM;
	}
}
