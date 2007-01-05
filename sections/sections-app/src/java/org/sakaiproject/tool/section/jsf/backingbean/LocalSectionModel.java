/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.Meeting;

public class LocalSectionModel implements Serializable {
	private static final Log log = LogFactory.getLog(LocalSectionModel.class);
	private static final long serialVersionUID = 1L;


	private String title;
    private Integer maxEnrollments;

	// We need a string to represent size limit due to this JSF bug: http://issues.apache.org/jira/browse/MYFACES-570
	private String limitSize;

	private List<Meeting> meetings;

	public LocalSectionModel() {}
	
	public LocalSectionModel(String title) {
		this.title = title;
		limitSize = Boolean.FALSE.toString();
	}
	
	public LocalSectionModel(CourseSection section) {
		this.title = section.getTitle();
		this.maxEnrollments = section.getMaxEnrollments();
		if(maxEnrollments == null) {
			limitSize = Boolean.FALSE.toString();
		} else {
			limitSize = Boolean.TRUE.toString();
		}
		this.meetings = new ArrayList<Meeting>();
		for(Iterator iter = section.getMeetings().iterator(); iter.hasNext();) {
			Meeting meeting = (Meeting)iter.next();
			meetings.add(new LocalMeetingModel(meeting));
		}
	}
    
    public Integer getMaxEnrollments() {
		return maxEnrollments;
	}
	public void setMaxEnrollments(Integer maxEnrollments) {
		this.maxEnrollments = maxEnrollments;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<Meeting> getMeetings() {
		if(meetings == null) {
			// Keep this out of the constructor to avoid it in deserialization
			this.meetings = new ArrayList<Meeting>();
		}
		return meetings;
	}

	public void setMeetings(List<Meeting> meetings) {
		this.meetings = meetings;
	}

	public String toString() {
		return new ToStringBuilder(this).append(title).append(maxEnrollments).toString();
	}

	public String getLimitSize() {
		return limitSize;
	}

	public void setLimitSize(String limitSize) {
		this.limitSize = limitSize;
	}
}
