/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.sitestats.api.EventStat;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class EventStatImpl implements EventStat, Serializable, Comparable<EventStat>{
	private static final long	serialVersionUID	= 1L;
	private long	id;
	private String	siteId;
	private String	userId;
	private String	eventId;
	private String	toolId;
	private long	count;
	private Date	date;
	
	/** Minimal constructor. */
	public EventStatImpl() {		
	}
	
	/** Default constructor. */
	public EventStatImpl(String siteId, long count) {
		this(0, siteId, null, null, count, null);
	}
	
	/** Full constructor. */
	public EventStatImpl(long id, String siteId, String userId, String eventId, long count, Date date) {
		setId(id);
		setSiteId(siteId);
		setUserId(userId);
		setEventId(eventId);		
		setCount(count);
		setDate(date);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#getId()
	 */
	public long getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#getUserId()
	 */
	public String getUserId() {
		return userId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#setUserId(java.lang.String)
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#getSiteId()
	 */
	public String getSiteId() {
		return siteId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#setSiteId(java.lang.String)
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#getEventId()
	 */
	public String getEventId() {
		return eventId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#setEventId(java.lang.String)
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#getToolId()
	 */
	public String getToolId() {
		return toolId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#setToolId(java.lang.String)
	 */
	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#getCount()
	 */
	public long getCount() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#setCount(long)
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.EventStat#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}


	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof EventStatImpl)) return false;
		EventStatImpl other = (EventStatImpl) o;
		return id == other.getId()
				&& siteId.equals(other.getSiteId())
				&& userId.equals(other.getUserId())
				&& eventId.equals(other.getEventId())
				&& ((toolId != null && toolId.equals(other.getToolId())) || (toolId == null && other.getToolId() == null))
				&& count == other.getCount()
				&& date.equals(other.getDate());
	}
	
	@Override
	public int compareTo(EventStat other) {
		int val = siteId.compareTo(other.getSiteId());
		if (val != 0) return val;
		val = userId.compareTo(other.getUserId());
		if (val != 0) return val;
		val = eventId.compareTo(other.getEventId());
		if (val != 0) return val;
		val = date.compareTo(other.getDate());
		if (val != 0) return val;
		val = Long.signum(count - other.getCount());
		if (val != 0) return val;
		val = compare(toolId, other.getToolId());
		if (val != 0) return val;
		val = Long.signum(id - other.getId());
		return val;
	}
	
	private int compare(String one, String two) {
		if (one == null) {
			if (two == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (two == null) {
				return -1;
			} else {
				return one.compareTo(two);
			}
		}
	}
	
	public boolean equalExceptForCount(Object o) {
		if(o == null) return false;
		if(!(o instanceof EventStatImpl)) return false;
		EventStatImpl other = (EventStatImpl) o;
		return 	   ( (siteId == null && other.getSiteId() == null)
					 || (siteId != null && other.getSiteId() != null && siteId.equals(other.getSiteId())) )
				&& ( (userId == null && other.getUserId() == null)
						 || (userId != null && other.getUserId() != null && userId.equals(other.getUserId())) )
				&& ( (eventId == null && other.getEventId() == null)
						 || (eventId != null && other.getEventId() != null && eventId.equals(other.getEventId())) )
				&& ( (toolId == null && other.getToolId() == toolId)
						 || (toolId != null && other.getToolId() != null && toolId.equals(other.getToolId())) )
				&& ( (date == null && other.getDate() == null)
						 || (date != null && other.getDate() != null && date.equals(other.getDate())) );
	}

	public int hashCode() {
		// Why do we have strange hashCode rules for objects without a siteId?
		if(siteId == null) return Integer.MIN_VALUE;
		String hashStr = this.getClass().getName() + ":" 
				+ id
				+ this.getUserId().hashCode()
				+ this.getSiteId().hashCode()
				+ this.getEventId().hashCode()
				+ (this.getToolId() != null ? this.getToolId().hashCode() : 0)
				+ count
				+ this.getDate().hashCode();
		return hashStr.hashCode();
	}
	
	public String toString(){
		return siteId + " : " + userId + " : " + eventId + " : " + toolId + " : " + count + " : " + date;
	}

}
