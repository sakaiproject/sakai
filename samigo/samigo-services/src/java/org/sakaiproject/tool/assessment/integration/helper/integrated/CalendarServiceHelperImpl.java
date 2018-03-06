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
package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.cover.ToolManager;

@Slf4j
public class CalendarServiceHelperImpl implements CalendarServiceHelper {
	private CalendarService calendarService;
	private Boolean calendarExistsForSite = null;
	private Map<String, Boolean> calendarExistCache = new HashMap<String, Boolean>();
	private String calendarTitle;

	public String getString(String key, String defaultValue) {
		return (ServerConfigurationService.getString(key, defaultValue));
	}

	public String calendarReference(String siteId, String container){
		return calendarService.calendarReference(siteId, container);
	}

	public Calendar getCalendar(String ref) throws IdUnusedException, PermissionException {
		return calendarService.getCalendar(ref);
	}

	public CalendarService getCalendarService() {
		return calendarService;
	}

	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}

	public void removeCalendarEvent(String siteId, String eventId){
		try{
			String calendarId = calendarReference(siteId, SiteService.MAIN_CONTAINER);
			Calendar calendar = getCalendar(calendarId);
			if(calendar != null && eventId != null && !"".equals(eventId)){
				try{
					CalendarEvent calendarEvent = calendar.getEvent(eventId);
					calendar.removeEvent(calendar.getEditEvent(calendarEvent.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
				}catch (PermissionException | InUseException | IdUnusedException e)
				{
					log.warn(e.getMessage(), e);
				}
			}

		}catch (IdUnusedException | PermissionException e)
		{
			log.warn(e.getMessage(), e);
		}
	}

	public String addCalendarEvent(String siteId, String title, String desc, long dateTime, List<Group> groupRestrictions, String calendarEventType){
		String eventId = null;		
		String calendarId = calendarReference(siteId, SiteService.MAIN_CONTAINER);
		try {
			Calendar calendar = getCalendar(calendarId);
			if(calendar != null){
				TimeRange timeRange = TimeService.newTimeRange(dateTime, 0);
				CalendarEvent.EventAccess eAccess = CalendarEvent.EventAccess.SITE;
				if(groupRestrictions != null && groupRestrictions.size() > 0){
					eAccess = CalendarEvent.EventAccess.GROUPED;
				}

				// add event to calendar
				CalendarEvent event = calendar.addEvent(timeRange,
						title,
						desc,
						calendarEventType,
						"",
						eAccess,
						groupRestrictions,
						EntityManager.newReferenceList());

				eventId = event.getId();

				// now add the linkage to the assignment on the calendar side
				if (event.getId() != null) {
					// add the assignmentId to the calendar object

					CalendarEventEdit edit = calendar.getEditEvent(event.getId(), CalendarService.EVENT_ADD_CALENDAR);

					edit.setDescriptionFormatted(desc);

					calendar.commitEvent(edit);
				}
			}
		} catch (IdUnusedException | InUseException | PermissionException e) {
			log.warn(e.getMessage(), e);
		}

		return eventId;
	}

	public void updateAllCalendarEvents(PublishedAssessmentFacade pub, String releaseTo, String[] groupsAuthorized, String dueDateTitlePrefix, boolean addDueDateToCalendar, String eventDesc){
		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		//remove all previous events:
		String newDueDateEventId = null;
		//Due Date
		try{
			String calendarDueDateEventId = pub.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.CALENDAR_DUE_DATE_EVENT_ID);
			if(calendarDueDateEventId != null){
				removeCalendarEvent(AgentFacade.getCurrentSiteId(), calendarDueDateEventId);
			}
		}catch(Exception e){
			//user could have manually removed the calendar event
			log.warn(e.getMessage(), e);
		}

		//add any new  calendar events
		List<Group> authorizedGroups = getAuthorizedGroups(releaseTo, groupsAuthorized);

		//Due Date
		if (addDueDateToCalendar && pub.getAssessmentAccessControl().getDueDate() != null) {
			newDueDateEventId = addCalendarEvent(
					AgentFacade.getCurrentSiteId(),
					dueDateTitlePrefix + pub.getTitle(), eventDesc, pub
					.getAssessmentAccessControl().getDueDate()
					.getTime(), authorizedGroups,
					CalendarServiceHelper.DEADLINE_EVENT_TYPE);
			
		}
		
		
		boolean found = false;
		PublishedMetaData meta = null;
		for(PublishedMetaData pubMetData : (Set<PublishedMetaData>) pub.getAssessmentMetaDataSet()){
			if(AssessmentMetaDataIfc.CALENDAR_DUE_DATE_EVENT_ID.equals(pubMetData.getLabel())){
				meta = pubMetData;
				meta.setEntry(newDueDateEventId);
				found = true;
				break;
			}
		}
		if(!found){
			meta = new PublishedMetaData(pub.getData(),
					AssessmentMetaDataIfc.CALENDAR_DUE_DATE_EVENT_ID, newDueDateEventId);
		}
		publishedAssessmentService.saveOrUpdateMetaData(meta);
	}

	private List<Group> getAuthorizedGroups(String releaseTo, String[] authorizedGroupsArray){
		List<Group> authorizedGroups = null;
		if(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS.equals(releaseTo) && authorizedGroupsArray != null && authorizedGroupsArray.length > 0){
			authorizedGroups = new ArrayList<Group>();
			Site site = null;
			try {
				site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
				Collection groups = site.getGroups();
				if (groups != null && groups.size() > 0) {
					Iterator groupIter = groups.iterator();
					while (groupIter.hasNext()) {
						Group group = (Group) groupIter.next();
                                                
						if(authorizedGroupsArray != null && authorizedGroupsArray.length > 0) {
						for(int i = 0; i < authorizedGroupsArray.length; i++){
							if(authorizedGroupsArray[i].equals(group.getId())){
								authorizedGroups.add(group);
							}
						}
 						}
					}		    	 
				}
			}
			catch (IdUnusedException ex) {
				log.debug(ex.getMessage());
			}		  
		}
		return authorizedGroups;
	}

	public Boolean getCalendarExistsForSite(){
		String siteContext = ToolManager.getCurrentPlacement().getContext();
		Site site = null;
		try
		{
			site = SiteService.getSite(siteContext);
			if (site.getToolForCommonId("sakai.schedule") != null)
			{
				return true;
			}else{
				return false;
			}

		}
		catch (Exception e) {
			log.warn("Exception thrown while getting site", e);
		}
		return false;
	}

	public void setCalendarExistsForSite(Boolean calendarExistsForSite) {
		this.calendarExistsForSite = calendarExistsForSite;
	}
	
	public String getCalendarTitle(){
		return ToolManager.getTool("sakai.schedule").getTitle();
	}

	public void setCalendarTitle(String calendarTitle) {
		this.calendarTitle = calendarTitle;
	}
}
