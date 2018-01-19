/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.util.Date;

public class EventLogData
implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	private Long eventLogId;
	private Long assessmentId;
	private Long processId;
	private String title;
	private String userEid;
	private String siteId;
	private Date startDate;
	private Date endDate;
	private Integer eclipseTime;
	private String errorMsg;
	private Boolean isNoErrors;
	private String userDisplay;
	 private String ipAddress;


	public EventLogData() {}

	public EventLogData(Long id, Long assessmentId, Long processId, String title,
			String userId, String siteId, Date startDate, Date endDate, Integer eclipseTime,
			String errorMsg) {
		super();
		this.eventLogId = id;
		this.assessmentId = assessmentId;
		this.processId = processId;
		this.title = title;
		this.userEid = userId;
		this.siteId = siteId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.eclipseTime = eclipseTime;
		this.errorMsg = errorMsg;
	}

	public Long getEventLogId() {
		return eventLogId;
	}

	public void setEventLogId(Long eventLogId) {
		this.eventLogId = eventLogId;
	}

	public Long getAssessmentId() {
		return assessmentId;
	}
	
	/**
	 * Gets the string representation of the Long assessmentId
	 * @return
	 */
	public String getAssessmentIdStr() {
      return Long.toString(assessmentId);
   }

	public void setAssessmentId(Long assessmentId) {
		this.assessmentId = assessmentId;
	}
	
	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Truncate the title to 13 characters.  We'll append "..." to the end in the jsp page
	 * @return
	 */
	public String getShortenedTitle() {
	   String newTitle = title;
	   if (title.length() > 13) {
	      newTitle = title.substring(0, 13);
	   }
	   return newTitle;
	}

	public String getUserEid() {
		return userEid;
	}

	public void setUserEid(String userId) {
		this.userEid = userId;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Integer getEclipseTime() {
		return eclipseTime;
	}

	public void setEclipseTime(Integer eclipseTime) {
		this.eclipseTime = eclipseTime;
	}
	

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Boolean getIsNoErrors() {
		if(errorMsg.startsWith("No Errors")) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public void setIsNoErrors(Boolean isNoErrors) {
		this.isNoErrors = isNoErrors;
	}

	public String getUserDisplay() {
	   return userDisplay;
	}

	public void setUserDisplay(String userDisplay) {
	   this.userDisplay = userDisplay;
	}
	
	public String getIpAddress() {
           return ipAddress;
       }
	
       public void setIpAddress(String ipAddress) {
               this.ipAddress = ipAddress;
       }
}
