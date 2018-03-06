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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.EventLogBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

@Slf4j
public class EventLogListener
implements ActionListener, ValueChangeListener
{
	private static BeanSort bs;
	private static String userFilterString = null;
	

	public EventLogListener()
	{
		userFilterString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EventLogMessages", "search_hint");
	}


	public void processAction(ActionEvent ae)
	{
		log.debug("*****Log: inside EventLogListener =debugging ActionEvent: " + ae);
		EventLogBean eventLog = (EventLogBean) ContextUtil.lookupBean("eventLog");
		
		String clear = ContextUtil.lookupParam("clear");
		if (clear != null) {
		   eventLog.setFilteredUser(null);
		}
		
		processPageLoad(eventLog);		
	}

	/**
	 * Process the ValueChangeEvent for when the Assessment filter dropdown changes 
	 */
	public void processValueChange(ValueChangeEvent event)
	{
	   log.debug("*****Log: inside EventLogListener =debugging ValueChangeEvent: " + event);
	   EventLogBean eventLog = (EventLogBean) ContextUtil.lookupBean("eventLog");
	   eventLog.setFilteredAssessmentId((Long)event.getNewValue());
	   processPageLoad(eventLog);
	}
	
	private List<EventLogData> updateData(List<EventLogData> eventLogDataList) {
		List<EventLogData> updatedEventLogDataList = new ArrayList<EventLogData>();
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();	
		Map<String, PublishedAccessControl> assessmentIdSettingMap = new HashMap<String, PublishedAccessControl>();
		PublishedAccessControl control = null;
		for(EventLogData data:eventLogDataList) {
			String assessmentId = data.getAssessmentIdStr();
			if(assessmentIdSettingMap.containsKey(assessmentId)) {
				control = (PublishedAccessControl)assessmentIdSettingMap.get(assessmentId);
			} else {
				PublishedAssessmentFacade assessment = assessmentService.getSettingsOfPublishedAssessment(assessmentId);	
				control = (PublishedAccessControl)assessment.getAssessmentAccessControl();	
				assessmentIdSettingMap.put(assessmentId, control);
			}
			String releaseInfo = control.getReleaseTo();
			if("Anonymous Users".equals(releaseInfo)) {
				data.setUserDisplay("N/A");
				data.setIpAddress("N/A");
				updatedEventLogDataList.add(data);
			}
			else {
				updatedEventLogDataList.add(data);
			}
		}
		return updatedEventLogDataList;
	}

	private Map<Long,Integer> setStatusEventLog(EventLogService eventLogService, String siteId, EventLogBean eventLog) {
		List<EventLogData> eventLogDataList = eventLogService.getEventLogData(siteId, -1L, "");
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();	
		Map<Long,Integer> statusMap = new  HashMap<>();
		for(EventLogData data:eventLogDataList) {
			Long assessmentId = data.getAssessmentId();
			if(!statusMap.containsKey(assessmentId)){
				statusMap.put(assessmentId, assessmentService.getPublishedAssessmentStatus(assessmentId));
			}
		}
		return statusMap;
	}
	
	/**
	 * These things need to happen each time the page loads.
	 * @param eventLog
	 */
	private void processPageLoad(EventLogBean eventLog) {
	   int numPerPage = 20;
	   EventLogService eventLogService = new EventLogService();
      
      String siteId = AgentFacade.getCurrentSiteId();
      String siteTitle = null;
      try {
		siteTitle = SiteService.getSite(siteId).getTitle();
	} catch (IdUnusedException e) {
		log.warn("can't find title for siteId: " + siteId, e);
		
	}
      
      if (eventLog.getFilteredUser() != null && eventLog.getFilteredUser().equals(userFilterString)) {
    	  eventLog.setFilteredUser(null);
      }
      Map<Long,Integer> statusMap = setStatusEventLog(eventLogService, siteId, eventLog);

      List<EventLogData> eventLogDataList = eventLogService.getEventLogData(siteId, eventLog.getFilteredAssessmentId(), eventLog.getFilteredUser());
      
      //check anonymous users setting, update user name and ip address to N/A
      List<EventLogData> updateEventLogDataList = updateData(eventLogDataList);
      
      List<Object[]> titles = eventLogService.getTitlesFromEventLogBySite(siteId);
      eventLog.setAssessments(initAssessmentListFilter(titles, statusMap));
      
      applySort(updateEventLogDataList, eventLog);
      
      Map<Integer, List<EventLogData>> pageDataMap = createMap(updateEventLogDataList, numPerPage);

      eventLog.setPageDataMap(pageDataMap);  
      eventLog.setEventLogDataList(pageDataMap.get(Integer.valueOf(1)));
      eventLog.setSiteId(siteId);
      eventLog.setSiteTitle(siteTitle);
      eventLog.setPageNumber(1);
      eventLog.setStatusMap(statusMap);
      if(pageDataMap.size()>1) {
         eventLog.setHasNextPage(Boolean.TRUE);
         eventLog.setHasPreviousPage(Boolean.FALSE);
      }
      else {
         eventLog.setHasNextPage(Boolean.FALSE);
         eventLog.setHasPreviousPage(Boolean.FALSE);
      }
      
	}
	
	/**
	 * Init the data in the Assessment filter dropdown
	 * @param assessmentTitles List of titles
	 * @return
	 */
	private List<SelectItem> initAssessmentListFilter(List<Object[]> assessmentTitles, Map<Long,Integer> statusMap) {
	   List<SelectItem> assessments = new ArrayList<SelectItem>();
	   
	   String allStr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EventLogMessages", "filterAll");
      assessments.add(new SelectItem((long)-1, allStr));
      for (Object[] assessment : assessmentTitles) {
         Long id = (Long)assessment[0];
         Integer status = statusMap.get( id );
         String strStatus = status != null && status == EventLogBean.DELETED_STATUS ? "-deleted" : "";
         String title = (String)assessment[1]+strStatus;
         assessments.add(new SelectItem(id, title));
      }
      return assessments;
	}
	
	/**
	 * Apply sorting
	 * @param dataList
	 * @param bean
	 */
	private void applySort(List<EventLogData> dataList, EventLogBean bean) {

      if (ContextUtil.lookupParam("sortBy") != null &&
            !ContextUtil.lookupParam("sortBy").trim().equals("")){
         bean.setSortType(ContextUtil.lookupParam("sortBy"));

      }
      boolean sortAscending = true;
      if (ContextUtil.lookupParam("sortAscending") != null &&
            !ContextUtil.lookupParam("sortAscending").trim().equals("")){
         sortAscending = Boolean.valueOf(ContextUtil.lookupParam("sortAscending")).booleanValue();
         bean.setSortAscending(sortAscending);
      }

      String sortProperty = bean.getSortType();
      bs = new BeanSort(dataList, sortProperty);
      if ((sortProperty).equals("title")) bs.toStringSort();
      if ((sortProperty).equals("userDisplay")) bs.toStringSort();
      if ((sortProperty).equals("errorMsg")) bs.toStringSort();
      if ((sortProperty).equals("startDate")) bs.toDateSort();
      if ((sortProperty).equals("endDate")) bs.toDateSort();
      if ((sortProperty).equals("ipAddress")) bs.toIPAddressSort();
      if (bean.isSortAscending()) {
         dataList = (List)bs.sort();
      }
      else {
         dataList= (List)bs.sortDesc();
      }
      bean.setEventLogDataList(dataList);
	}
	
	private Map<Integer, List<EventLogData>> createMap(List<EventLogData> eventLogAllDataList, int numPerPage) {
		Map<Integer, List<EventLogData>> pageDataMap = new HashMap<Integer, List<EventLogData>>();		
		List<EventLogData> dataListTempt = new ArrayList<EventLogData>();
		int listLength = eventLogAllDataList.size();
		Iterator<EventLogData> it= eventLogAllDataList.iterator();
		int dataNumber = 0;
		int pageNumber = 1;		
		List<EventLogData> dataList = new ArrayList<EventLogData>();
		
		while(dataNumber < listLength ) {
			for(int i = 0; i <numPerPage; i++) {
				if(it.hasNext()) {
					dataListTempt.add(it.next());
					dataNumber++;
				}else break;
			}			
			dataList = copyData(dataListTempt);					
			pageDataMap.put(Integer.valueOf(pageNumber), dataList);		
			dataListTempt.removeAll(dataListTempt);
			pageNumber ++;			
		}	
		return pageDataMap;
	}	
	
	private List<EventLogData> copyData (List<EventLogData> dataList) {
		List<EventLogData> list = new ArrayList<EventLogData>();
		for(int i = 0; i < dataList.size(); i++) {
			list.add(dataList.get(i));
		}
		return list;
	}
}
