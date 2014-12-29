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

import org.sakaiproject.sitestats.api.SiteVisits;

/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class SiteVisitsImpl implements SiteVisits, Serializable {
	private static final long	serialVersionUID	= 1L;
	private long id;
	private String siteId;
	private Date date;
	private long totalVisits;
	private long totalUnique;

	@Override
	public int compareTo(SiteVisits other) {
		int val = siteId.compareTo(other.getSiteId());
		if (val != 0) return val;
		val = date.compareTo(other.getDate());
		if (val != 0) return val;
		val = Long.signum(totalVisits - other.getTotalVisits());
		if (val != 0) return val;
		val = Long.signum(totalUnique - other.getTotalUnique());
		if (val != 0) return val;
		val = Long.signum(id - other.getId());
		return val;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof SiteVisitsImpl)) return false;
		SiteVisitsImpl other = (SiteVisitsImpl) o;
		return id == other.getId()
				&& siteId.equals(other.getSiteId())
				&& date.equals(other.getDate())
				&& totalVisits == other.getTotalVisits()
				&& totalUnique == other.getTotalUnique();
	}

	@Override
	public int hashCode() {
		if(siteId == null) return Integer.MIN_VALUE;
		String hashStr = this.getClass().getName() + ":" 
				+ id
				+ this.getSiteId().hashCode()
				+ this.getDate().hashCode()
				+ totalVisits
				+ totalUnique;
		return hashStr.hashCode();
	}
	
	public String toString(){
		return siteId + " : " + date + " : " + totalVisits + " : " + totalUnique;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getSiteId()
	 */
	public String getSiteId() {
		return siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setSiteId(java.lang.String)
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteVisits#getTotalVisits()
	 */
	public long getTotalVisits() {
		return totalVisits;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteVisits#setTotalVisits(long)
	 */
	public void setTotalVisits(long totalVisits) {
		this.totalVisits = totalVisits;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteVisits#getTotalUnique()
	 */
	public long getTotalUnique() {
		return totalUnique;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.SiteVisits#setTotalUnique(long)
	 */
	public void setTotalUnique(long totalUnique) {
		this.totalUnique = totalUnique;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getCount()
	 */
	public long getCount() {
		return getTotalVisits();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getUserId()
	 */
	public String getUserId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setCount(long)
	 */
	public void setCount(long count) {
		setTotalVisits(count);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setUserId(java.lang.String)
	 */
	public void setUserId(String userId) {		
	}

}
