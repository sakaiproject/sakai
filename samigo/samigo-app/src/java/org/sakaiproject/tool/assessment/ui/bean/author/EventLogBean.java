/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

@Slf4j
public class EventLogBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int DELETED_STATUS=2;
	
	private String siteId;
	private  Map pageDataMap;
	private int pageNumber=0;
	private List<EventLogData> eventLogDataList;
	private Boolean hasNextPage;
	private Boolean hasPreviousPage;
	
	private String sortType="startDate";
	private boolean sortAscending = false;
	
	private List<SelectItem> assessments = new ArrayList<SelectItem>();
	private Long filteredAssessmentId = (long) -1;
	private String filteredUser;

	private String siteTitle;
	
	private Map<Long,Integer> statusMap;
		
	private static final String SAMIGO_EVENTLOG_IPADDRESS_ENABLE = "samigo.eventlog.ipaddress.enabled";
	private boolean enabledIpAddress = false;
	private ServerConfigurationService serverConfigurationService;
		
	public EventLogBean()
	{		
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		enabledIpAddress = serverConfigurationService.getBoolean(SAMIGO_EVENTLOG_IPADDRESS_ENABLE, false);			
	}
		
	/**
	 * Set the site Title
	 * @return
	 */
	public String getSiteTitle() {
		if (siteTitle != null) {
			return siteTitle;
		}
		return siteId;
	}
	
	/**
	 * Get the site tile
	 * @param siteTitle the site title or if not set the site id
	 */
	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public Map getPageDataMap() {
		return pageDataMap;
	}
	public void setPageDataMap(Map pageDataMap) {
		this.pageDataMap = pageDataMap;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getNextPageNumber() {
		return ++pageNumber;
	}
	public int getPreviousPageNumber() {
		return --pageNumber;
	}	

	public List<EventLogData> getEventLogDataList() {
		return eventLogDataList;
	}
	public void setEventLogDataList(List<EventLogData> eventLogDataList) {
		this.eventLogDataList = eventLogDataList;		
	}
	public Boolean getHasNextPage() {
		return hasNextPage;
	}
	public void setHasNextPage(Boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}
	public Boolean getHasPreviousPage() {
		return hasPreviousPage;
	}
	public void setHasPreviousPage(Boolean hasPreviousPage) {
		this.hasPreviousPage = hasPreviousPage;
	}
	public String getSortType() {
	   return sortType;
	}
	public void setSortType(String sortType) {
	   this.sortType = sortType;
	}
	public boolean isSortAscending() {
	   return sortAscending;
	}
	public void setSortAscending(boolean sortAscending) {
	   this.sortAscending = sortAscending;
	}
   public Long getFilteredAssessmentId() {
      return filteredAssessmentId;
   }
   public void setFilteredAssessmentId(Long filteredAssessmentId) {
      this.filteredAssessmentId = filteredAssessmentId;
   }
   public String getFilteredUser() {
      return filteredUser;
   }
   public void setFilteredUser(String filteredUser) {
      this.filteredUser = filteredUser;
   }
   public List<SelectItem> getAssessments() {
      return assessments;
   }
   public void setAssessments(List<SelectItem> assessments) {
      this.assessments = assessments;
   }
   public Map<Long, Integer> getStatusMap() {
	   return statusMap;
   }
   public void setStatusMap(Map<Long, Integer> statusMap) {
	   this.statusMap = statusMap;
   }
   public boolean isDeleted(Long id){
	   return statusMap.get(id)==DELETED_STATUS;
   }   
   public boolean getEnabledIpAddress(){
	   return enabledIpAddress;
   }
   public void setEnabledIpAddress(boolean enabledIpAddress){
	   this.enabledIpAddress = enabledIpAddress;
   }	
   
}
