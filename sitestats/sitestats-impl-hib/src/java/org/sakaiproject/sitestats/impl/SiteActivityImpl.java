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

import org.sakaiproject.sitestats.api.SiteActivity;

/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class SiteActivityImpl implements SiteActivity, Serializable {
	private static final long	serialVersionUID	= 1L;
	private long id;
	private String siteId;
	private Date date;
	private String	eventId;
	private long	count;

	@Override
	public int compareTo(SiteActivity other) {
		int val = siteId.compareTo(other.getSiteId());
		if (val != 0) return val;
		val = eventId.compareTo(other.getEventId());
		if (val != 0) return val;
		val = date.compareTo(other.getDate());
		if (val != 0) return val;
		val = Long.signum(count - other.getCount());
		if (val != 0) return val;
		val = Long.signum(id - other.getId());
		return val;
	} 
	
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof SiteActivityImpl)) return false;
		SiteActivityImpl other = (SiteActivityImpl) o;
		return id == other.getId()
				&& siteId.equals(other.getSiteId())
				&& date.equals(other.getDate())
				&& eventId.equals(other.getEventId())
				&& count == other.getCount();
	}

	public int hashCode() {
		if(siteId == null) return Integer.MIN_VALUE;
		String hashStr = this.getClass().getName() + ":" 
				+ id
				+ this.getSiteId().hashCode()
				+ this.getDate().hashCode()
				+ this.getEventId().hashCode()
				+ count;
		return hashStr.hashCode();
	}
	
	public String toString(){
		return siteId + " : " + date + " : " + eventId + " : " + count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#getSiteId()
	 */
	public String getSiteId() {
		return siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#setSiteId(java.lang.String)
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#getEventId()
	 */
	public String getEventId() {
		return eventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#setEventId(java.lang.String)
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#getCount()
	 */
	public long getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteActivity#setCount(long)
	 */
	public void setCount(long count) {
		this.count = count;
	}

}
