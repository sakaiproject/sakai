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

import org.sakaiproject.sitestats.api.UserStat;

/**
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class UserStatImpl implements UserStat, Serializable {
	
	private static final long serialVersionUID	= 1L;
	private long id;
	private Date date;
	private String userId;
	private long count;

	@Override
	public int compareTo(UserStat other) {
		int val = userId.compareTo(other.getUserId());
		if (val != 0) return val;
		val = date.compareTo(other.getDate());
		if (val != 0) return val;
		val = Long.signum(count - other.getCount());
		if (val != 0) return val;
		val = Long.signum(id - other.getId());
		return val;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof UserStatImpl)) return false;
		UserStatImpl other = (UserStatImpl) o;
		return id == other.getId()
				&& date.equals(other.getDate())
				&& userId.equals(other.getUserId())
				&& count == other.getCount();
	}

	@Override
	public int hashCode() {
		String hashStr = this.getClass().getName() + ":" 
				+ id
				+ this.getDate().hashCode()
				+ this.getUserId().hashCode()
				+ count;
		return hashStr.hashCode();
	}
	
	public String toString(){
		return  date + " : " + userId + " : " + count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.UserStat#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.UserStat#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.UserStat#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.UserStat#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.UserStat#getUserId()
	 */
	public String getUserId() {
		return userId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.UserStat#setUserId(java.lang.String)
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.UserStat#getCount()
	 */
	public long getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.UserStat#setCount(long)
	 */
	public void setCount(long count) {
		this.count = count;
	}

}
