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
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.ui.model.DataTableConfig;
import org.sakaiproject.tool.assessment.ui.servlet.event.ExportEventLogServlet;
import org.springframework.web.util.UriComponentsBuilder;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.samigo.util.SamigoConstants;

/* Event Log backing bean. */
@Slf4j
@ManagedBean(name="eventLog")
@SessionScoped
public class EventLogBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int DELETED_STATUS=2;
	
	private String siteId;
	private int pageNumber=0;
	private List<EventLogData> eventLogDataList;
	@Getter @Setter
	private DataTableConfig dataTableConfig;
	
	private String sortType="startDate";
	private boolean sortAscending = false;
	
	private List<SelectItem> assessments = new ArrayList<SelectItem>();
	private Long filteredAssessmentId = (long) -1;

	private String siteTitle;
	
	private Map<Long,Integer> statusMap;
		
	private boolean enabledIpAddress = false;
	private ServerConfigurationService serverConfigurationService;
		
	public EventLogBean()
	{		
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		enabledIpAddress = serverConfigurationService.getBoolean(SamigoConstants.SAK_PROP_EVENTLOG_IPADDRESS_ENABLED,
				SamigoConstants.SAK_PROP_DEFAULT_EVENTLOG_IPADDRESS_ENABLED);			
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

    public String getExportUrl() {
        Optional<String> assessmentId = filteredAssessmentId != null && filteredAssessmentId != -1
                ? Optional.of(filteredAssessmentId.toString())
                : Optional.empty();

        return UriComponentsBuilder.fromPath(SamigoConstants.SERVLET_MAPPING_EXPORT_EVENT_LOG)
                .queryParam(ExportEventLogServlet.PARAM_SITE_ID, siteId)
                .queryParamIfPresent(ExportEventLogServlet.PARAM_ASSESSMENT_ID, assessmentId)
                .build().toUriString();
    }
   
}
