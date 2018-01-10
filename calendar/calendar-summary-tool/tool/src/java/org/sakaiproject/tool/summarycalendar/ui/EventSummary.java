/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;

import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class EventSummary implements Serializable {
	private static final long	serialVersionUID	= 4943854683550852507L;

	/** Resource bundle */
	private transient ResourceLoader msgs 			= new ResourceLoader("calendar");
	
	private final int	MAX_TEXT_SIZE	= 30;

	private String		displayName		= "";
	private String		type			= "";
	private String		typeLocalized	= "";
	private String		description		= "";
	private String		date			= "";
	private String		location		= "";
	private String		site			= "";
	private String		url				= "";
	private String		calendarRef		= "";
	private String		eventRef		= "";
	private String		groups			= "";
	private boolean		hasAttachments	= false;
	private List		attachments		= new ArrayList();
	private List		attachmentsWrp	= new ArrayList();
	private boolean 	openDateError	= false;

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
		StringBuilder tmp = new StringBuilder();
		Time firstTime = range.firstTime();
		Time lastTime = range.lastTime(0);
		if(isSameDay(firstTime.getTime(), lastTime.getTime())) {
			tmp.append(getDateStr(firstTime));
			tmp.append(" ");
			tmp.append(getTimeStr(firstTime));
			if(!firstTime.equals(lastTime)) {
				tmp.append(" - ");
				tmp.append(getTimeStr(lastTime));
			}
		} else {
			tmp.append(getDateStr(firstTime));
			tmp.append(" ");
			tmp.append(getTimeStr(firstTime));
			tmp.append(" - ");
			tmp.append(getDateStr(lastTime));
			tmp.append(" ");
			tmp.append(getTimeStr(lastTime));
			
		}
		this.date = tmp.toString();
	}

	public String getDescription() {
		return description;
	}

	public String getTruncatedDescription() {
		return getTruncated(description);
	}

	public void setDescription(String description) {
		if(description != null) {
			this.description = description.trim();
		}else{
			this.description = null;
		}
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

	public void setTypeLocalized(String localizedEventType) {
		this.typeLocalized = localizedEventType;
		
	}	
	public String getTypeLocalized() {
		return typeLocalized;
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
		if(str == null) return "";
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

	public List getAttachments() {
		return attachments;
	}

	public void setAttachments(List attachments) {
		setHasAttachments(attachments != null && attachments.size() > 0); 
		this.attachmentsWrp = new ArrayList();
		if(attachments == null){
			this.attachments = new ArrayList();
			return;
		}
		this.attachments = attachments;
		Iterator it = attachments.iterator();
		while(it.hasNext()){
			Reference ref = (Reference) it.next();
			if(ref != null){
				ResourceProperties resProp = ref.getProperties();
				if(resProp != null) {
					String displayName = resProp.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
					AttachmentWrapper aw = new AttachmentWrapper();
					aw.setUrl(ref.getUrl());
					aw.setDisplayName(displayName);
					attachmentsWrp.add(aw);
				}
			}
		}
	}

	public boolean getHasAttachments(){
		return this.hasAttachments;
	}
	
	public void setHasAttachments(boolean hasAttachments){
		this.hasAttachments = hasAttachments;
	}
	
	public List getAttachmentsWrapper() {
		return this.attachmentsWrp;
	}

	private boolean isSameDay(long startMs, long endMs) {
		Calendar s = Calendar.getInstance();
		Calendar e = (Calendar) s.clone();
		s.setTimeInMillis(startMs);
		e.setTimeInMillis(endMs);
		return (s.get(Calendar.ERA) == e.get(Calendar.ERA) &&
        		s.get(Calendar.YEAR) == e.get(Calendar.YEAR) &&
        		s.get(Calendar.DAY_OF_YEAR) == e.get(Calendar.DAY_OF_YEAR));
	}
	
	private boolean isToday(long dateMs) {
		TimeZone timeZone = getCurrentUserTimezone();
		DateTime thisDate = new DateTime(dateMs).withZone(DateTimeZone.forTimeZone(timeZone));
		//Start of day at this local
		DateTime today = new DateTime().withTime(0, 0, 0, 0).withZone(DateTimeZone.forTimeZone(timeZone));
		DateTimeComparator dtComp = DateTimeComparator.getDateOnlyInstance();
		return  (dtComp.compare(thisDate, today) == 0);
		
	}
	
	/**
	 * Get the TimeZone for the current user
	 * @return
	 */
	private TimeZone getCurrentUserTimezone() {
		
		TimeZone tz = TimeService.getLocalTimeZone();
		log.debug("got tz " + tz.getDisplayName());
		return tz;
	}
	
	private String getDateStr(Time time) {
		if(isToday(time.getTime()))
			return msgs.getString("today") + ",";
		else
			return time.toStringLocalDate();
	}
	
	private String getTimeStr(Time time) {
		return time.toStringLocalTimeZ();
	}
	
	public boolean isOpenDateError() {
		return openDateError;
	}
	
	public void setOpenDateError(boolean openDateError) {
		this.openDateError = openDateError;
	}
}
