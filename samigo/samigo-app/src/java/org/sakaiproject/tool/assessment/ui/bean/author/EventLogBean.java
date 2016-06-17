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

}