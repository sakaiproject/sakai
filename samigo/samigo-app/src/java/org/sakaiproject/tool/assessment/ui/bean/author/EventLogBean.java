package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;

public class EventLogBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(EventLogBean.class);

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

}