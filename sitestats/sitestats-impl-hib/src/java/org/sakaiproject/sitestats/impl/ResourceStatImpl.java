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

import org.sakaiproject.sitestats.api.ResourceStat;

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

/**
 * @author nfernandes
 *
 */
public class ResourceStatImpl implements ResourceStat, Serializable {
	private static final long	serialVersionUID	= 1L;
	private long id;
	private String userId;
	private String siteId;
	private String resourceRef;
	private String resourceAction;
	private long count;
	private Date date;

	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof ResourceStatImpl)) return false;
		ResourceStatImpl other = (ResourceStatImpl) o;
		return id == other.getId()
				&& siteId.equals(other.getSiteId())
				&& userId.equals(other.getUserId())
				&& resourceRef.equals(other.getResourceRef())
				&& resourceAction.equals(other.getResourceAction())
				&& count == other.getCount()
				&& date.equals(other.getDate());
	}

	@Override
	public int compareTo(ResourceStat other) {
		int val = siteId.compareTo(other.getSiteId());
		if (val != 0) return val;
		val = userId.compareTo(other.getUserId());
		if (val != 0) return val;
		val = resourceRef.compareTo(other.getResourceRef());
		if (val != 0) return val;
		val = resourceAction.compareTo(other.getResourceAction());
		if (val != 0) return val;
		val = date.compareTo(other.getDate());
		if (val != 0) return val;
		val = Long.signum(count - other.getCount());
		if (val != 0) return val;
		val = Long.signum(id - other.getId());
		return val;
	}

	public int hashCode() {
		if(siteId == null) return Integer.MIN_VALUE;
		String hashStr = this.getClass().getName() + ":" 
				+ id
				+ this.getUserId().hashCode()
				+ this.getSiteId().hashCode()
				+ this.getResourceRef().hashCode()
				+ this.getResourceAction().hashCode()
				+ count
				+ this.getDate().hashCode();
		return hashStr.hashCode();
	}
	
	public String toString(){
		return siteId + " : " + userId + " : " + resourceRef + " : " + resourceAction + " : " + count + " : " + date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#getUserId()
	 */
	public String getUserId() {
		return userId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#setUserId(java.lang.String)
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#getSiteId()
	 */
	public String getSiteId() {
		return siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#setSiteId(java.lang.String)
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#getResourceRef()
	 */
	public String getResourceRef() {
		return resourceRef;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#setResourceRef(java.lang.String)
	 */
	public void setResourceRef(String resourceRef) {
		this.resourceRef = resourceRef;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#getResourceAction()
	 */
	public String getResourceAction() {
		return resourceAction;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#setResourceAction(java.lang.String)
	 */
	public void setResourceAction(String resourceAction) {
		this.resourceAction = resourceAction;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#getCount()
	 */
	public long getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#setCount(long)
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ResourceStat#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}

}
