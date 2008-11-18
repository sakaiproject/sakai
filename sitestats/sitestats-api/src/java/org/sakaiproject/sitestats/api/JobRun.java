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
package org.sakaiproject.sitestats.api;

import java.util.Date;

public interface JobRun {	
	/** Get the db row id. */
	public long getId();
	
	/** Set the db row id. */
	public void setId(long id);
	
	/** Get the first event id processed by this job run. */
	public long getStartEventId();
	
	/** Set the first event id processed by this job run. */
	public void setStartEventId(long startEventId);
	
	/** Get the last event id processed by this job run. */
	public long getEndEventId();
	
	/** Set the last event id processed by this job run. */
	public void setEndEventId(long endEventId);
	
	/** Get the date this job run started. */
	public Date getJobStartDate();
	
	/** Set the date this job run started. */
	public void setJobStartDate(Date jobStartDate);
	
	/** Get the date this job run finished. */
	public Date getJobEndDate();
	
	/** Set the date this job run finished. */
	public void setJobEndDate(Date jobEndDate);
	
	/** Get the date of the last event processed by this job run. */
	public Date getLastEventDate();
	
	/** Set the date of the last event processed by this job run. */
	public void setLastEventDate(Date lastEventDate);
}
