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

import org.sakaiproject.sitestats.api.SitePresence;

public class SitePresenceImpl implements SitePresence, Serializable {
	private static final long	serialVersionUID	= 1L;
	private long id;
	private String siteId;
	private String userId;
	private Date date;
	private long duration;
	private Date lastVisitStartTime;

	@Override
	public int compareTo(SitePresence other) {
		int val = siteId.compareTo(other.getSiteId());
		if (val != 0) return val;
		val = userId.compareTo(other.getUserId());
		if (val != 0) return val;
		val = Long.signum(duration - other.getDuration());
		if (val != 0) return val;
		val = date.compareTo(other.getDate());
		if (val != 0) return val;
		val = lastVisitStartTime.compareTo(other.getLastVisitStartTime());
		if (val != 0) return val;
		val = Long.signum(id - other.getId());
		return val;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof SitePresenceImpl)) return false;
		SitePresenceImpl other = (SitePresenceImpl) o;
		return id == other.getId()
				&& siteId.equals(other.getSiteId())
				&& userId.equals(other.getUserId())
				&& date.equals(other.getDate())
				&& duration == other.getDuration()
				&& getCount() == other.getCount()
				&& lastVisitStartTime == other.getLastVisitStartTime();
	}

	@Override
	public int hashCode() {
		if(siteId == null) return Integer.MIN_VALUE;
		String hashStr = this.getClass().getName() + ":" 
				+ id
				+ this.getSiteId().hashCode()
				+ this.getUserId().hashCode()
				+ this.getDate().hashCode()
				+ duration
				+ this.getLastVisitStartTime();
		return hashStr.hashCode();
	}
	
	public String toString(){
		return siteId + " : " + userId + " : " + date + " : " + duration + " (" + lastVisitStartTime + ")";
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public Date getLastVisitStartTime() {
		return lastVisitStartTime;
	}
	
	public void setLastVisitStartTime(Date lastVisitStartTime) {
		this.lastVisitStartTime = lastVisitStartTime;
	}

	public Date getDate() {
		return date;
	}

	public long getId() {
		return id;
	}

	public String getSiteId() {
		return siteId;
	}

	public String getUserId() {
		return userId;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setCount(long count) {
		// Does nothing
	}

	public long getCount() {
		return duration;
	}

}
