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

import org.sakaiproject.sitestats.api.JobRun;

/**
 * @author Nuno Fernandes
 *
 */
public class JobRunImpl implements JobRun, Serializable {
	private static final long	serialVersionUID	= 1L;
	private long				id;
	private long				startEventId;
	private long				endEventId;
	private Date				jobStartDate;
	private Date				jobEndDate;
	private Date				lastEventDate;

	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof JobRunImpl)) return false;
		JobRunImpl other = (JobRunImpl) o;
		return id == other.getId()
				&& getStartEventId() == other.getStartEventId()
				&& getEndEventId() == other.getEndEventId()
				&& getJobStartDate().equals(other.getJobStartDate())
				&& getJobEndDate().equals(other.getJobEndDate())
				&& getLastEventDate().equals(other.getLastEventDate());
	}

	public int hashCode() {
		if(getStartEventId() == 0 || getEndEventId() == 0
				|| getJobStartDate() == null || getJobEndDate() == null){
			return Integer.MIN_VALUE;
		}
		String hashStr = this.getClass().getName() + ":"
				+ this.getId()
				+ this.getStartEventId()
				+ this.getEndEventId()
				+ this.getJobStartDate().hashCode()
				+ this.getJobEndDate().hashCode()
				+ this.getLastEventDate().hashCode();
		return hashStr.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#getJobEndDate()
	 */
	public Date getJobEndDate() {
		return jobEndDate;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#getEndEventId()
	 */
	public long getEndEventId() {
		return endEventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#getId()
	 */
	public long getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#getJobStartDate()
	 */
	public Date getJobStartDate() {
		return jobStartDate;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#getStartEventId()
	 */
	public long getStartEventId() {
		return startEventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#setJobEndDate(java.util.Date)
	 */
	public void setJobEndDate(Date jobEndDate) {
		this.jobEndDate = jobEndDate;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#setEndEventId(long)
	 */
	public void setEndEventId(long endEventId) {
		this.endEventId = endEventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#setJobStartDate(java.util.Date)
	 */
	public void setJobStartDate(Date jobStartDate) {
		this.jobStartDate = jobStartDate;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#setStartEventId(long)
	 */
	public void setStartEventId(long startEventId) {
		this.startEventId = startEventId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#getLastEventDate()
	 */
	public Date getLastEventDate(){
		return lastEventDate;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.JobRun#setLastEventDate(java.util.Date)
	 */
	public void setLastEventDate(Date lastEventDate){
		this.lastEventDate = lastEventDate;
	}

}
