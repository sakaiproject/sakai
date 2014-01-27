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

import org.sakaiproject.sitestats.api.ServerStat;

/**
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class ServerStatImpl implements ServerStat, Serializable {
	
	private static final long serialVersionUID	= 1L;
	private long id;
	private Date date;
	private String eventId;
	private long count;


	@Override
	public int compareTo(ServerStat other) {
		int val = eventId.compareTo(other.getEventId());
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
		if(!(o instanceof ServerStatImpl)) return false;
		ServerStatImpl other = (ServerStatImpl) o;
		return id == other.getId()
				&& date.equals(other.getDate())
				&& eventId.equals(other.getEventId())
				&& count == other.getCount();
	}

	public int hashCode() {
		String hashStr = this.getClass().getName() + ":" 
				+ id
				+ this.getDate().hashCode()
				+ this.getEventId().hashCode()
				+ count;
		return hashStr.hashCode();
	}
	
	public String toString(){
		return  date + " : " + eventId + " : " + count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerStat#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerStat#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerStat#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerStat#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerStat#getEventId()
	 */
	public String getEventId() {
		return eventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerStat#setEventId(java.lang.String)
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerStat#getCount()
	 */
	public long getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerStat#setCount(long)
	 */
	public void setCount(long count) {
		this.count = count;
	}


}
