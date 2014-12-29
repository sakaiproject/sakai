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
