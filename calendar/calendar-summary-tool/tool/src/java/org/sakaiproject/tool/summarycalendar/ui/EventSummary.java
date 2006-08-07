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

import java.io.Serializable;

import org.sakaiproject.time.api.TimeRange;


public class EventSummary implements Serializable {
	private static final long	serialVersionUID	= 4943854683550852507L;

	private final int	MAX_TEXT_SIZE	= 30;

	private String		displayName		= "";
	private String		type			= "";
	private String		description		= "";
	private String		date			= "";
	private String		location		= "";
	private String		site			= "";
	private String		url				= "";
	private String		calendarRef		= "";
	private String		eventRef		= "";
	private String		groups			= "";

	public String getDisplayName() {
		return displayName;
	}

	public String getTruncatedDisplayName() {
		return getTruncated(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(TimeRange range) {
		StringBuffer tmp = new StringBuffer();
		tmp.append(range.firstTime().toStringLocalTime());
		tmp.append(" - ");
		tmp.append(range.lastTime().toStringLocalTime());
		this.date = tmp.toString();
	}

	public String getDescription() {
		return description;
	}

	public String getTruncatedDescription() {
		return getTruncated(description);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean getHasLocation() {
		return !location.equals("");
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSite() {
		return site;
	}

	public String getTruncatedSite() {
		return getTruncated(site);
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getGroups() {
		return groups;
	}

	public void setGroups(String groups) {
		this.groups = groups;
	}

	public boolean getShowGroups() {
		return !groups.equals("");
	}

	private String getTruncated(String str) {
		if(str.length() < MAX_TEXT_SIZE) return str;
		return str.substring(0, MAX_TEXT_SIZE).concat("...");
	}

	public String getCalendarRef() {
		return calendarRef;
	}

	public void setCalendarRef(String calendarRef) {
		this.calendarRef = calendarRef;
	}

	public String getEventRef() {
		return eventRef;
	}

	public void setEventRef(String eventRef) {
		this.eventRef = eventRef;
	}

}
