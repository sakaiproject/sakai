/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
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
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.sitestats.api.EventStat;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class EventStatImpl implements EventStat, Serializable {
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
				&& count == other.getCount()
				&& date.equals(other.getDate());
	}

	public int hashCode() {
		if(siteId == null) return Integer.MIN_VALUE;
		String hashStr = this.getClass().getName() + ":" 
				+ id
				+ this.getUserId().hashCode()
				+ this.getSiteId().hashCode()
				+ this.getEventId().hashCode()
				+ count
				+ this.getDate().hashCode();
		return hashStr.hashCode();
	}
	
	public String toString(){
		return siteId + " : " + userId + " : " + eventId + " : " + count + " : " + date;
	}
}
