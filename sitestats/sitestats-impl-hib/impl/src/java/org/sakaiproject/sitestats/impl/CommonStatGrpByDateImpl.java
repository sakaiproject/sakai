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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sakaiproject.sitestats.api.CommonStatGrpByDate;

/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class CommonStatGrpByDateImpl implements CommonStatGrpByDate, Serializable {
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
	public String getRef() {
		return ref;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#setRef(java.lang.String)
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#setRefUrl(java.lang.String)
	 */
	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getRefUrl()
	 */
	public String getRefUrl() {
		return refUrl;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getRefImg()
	 */
	public String getRefImg() {
		return refImg;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#setRefImg(java.lang.String)
	 */
	public void setRefImg(String refImg) {
		this.refImg = refImg;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getRefAction()
	 */
	public String getRefAction() {
		return refAction;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#setRefAction(java.lang.String)
	 */
	public void setRefAction(String refAction) {
		this.refAction = refAction;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStatGrpByDate#getDateAsString()
	 */
	public String getDateAsString() {
		// STAT-6: changed to ISO while dates are not localized
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(date);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#getUserId()
	 */
	public String getUserId() {
		return userId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#setUserId(java.lang.String)
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#getSiteId()
	 */
	public String getSiteId() {
		return siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#setSiteId(java.lang.String)
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#getCount()
	 */
	public long getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#setCount(long)
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.CommonStat#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}

}
