/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sakaiproject.sitestats.api.CommonStatGrpByDate;

/**
 * This class is deprecated and wil be removed in version 2.1.
 * Please use {@link StatsRecord} instead.
 */
@Deprecated public class CommonStatGrpByDateImpl implements CommonStatGrpByDate, Serializable {
	private static final long	serialVersionUID	= 1L;
	private long				id					= 0;
	private long				count				= 0;
	private String				siteId				= "";
	private String				userId				= "";
	private String				ref					= "";
	private String				refImg				= "";
	private String				refUrl				= "";
	private String				refAction			= "";
	private Date				date;

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getRef()
	 */
	@Deprecated	public String getRef() {
		return ref;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#setRef(java.lang.String)
	 */
	@Deprecated	public void setRef(String ref) {
		this.ref = ref;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#setRefUrl(java.lang.String)
	 */
	@Deprecated	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getRefUrl()
	 */
	@Deprecated	public String getRefUrl() {
		return refUrl;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getRefImg()
	 */
	@Deprecated	public String getRefImg() {
		return refImg;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#setRefImg(java.lang.String)
	 */
	@Deprecated	public void setRefImg(String refImg) {
		this.refImg = refImg;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getRefAction()
	 */
	@Deprecated	public String getRefAction() {
		return refAction;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#setRefAction(java.lang.String)
	 */
	@Deprecated	public void setRefAction(String refAction) {
		this.refAction = refAction;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getDateAsString()
	 */
	@Deprecated	public String getDateAsString() {
		// STAT-6: changed to ISO while dates are not localized
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(date);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getId()
	 */
	@Deprecated	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setId(long)
	 */
	@Deprecated	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getUserId()
	 */
	@Deprecated	public String getUserId() {
		return userId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setUserId(java.lang.String)
	 */
	@Deprecated	public void setUserId(String userId) {
		this.userId = userId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getSiteId()
	 */
	@Deprecated	public String getSiteId() {
		return siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setSiteId(java.lang.String)
	 */
	@Deprecated	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getCount()
	 */
	@Deprecated	public long getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setCount(long)
	 */
	@Deprecated	public void setCount(long count) {
		this.count = count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#getDate()
	 */
	@Deprecated	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Stat#setDate(java.util.Date)
	 */
	@Deprecated	public void setDate(Date date) {
		this.date = date;
	}

}
