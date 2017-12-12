/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.OptimisticLockingFailureException;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendaring.api.ExternalCalendaringService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.dao.SignupMeetingDao;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.restful.SignupTargetSiteEventInfo;
import org.sakaiproject.signup.util.PlainTextFormat;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;

/**
 * <p>
 * SignupMeetingServiceImpl is an implementation of SignupMeetingService, which
 * provides methods to manipulate the SignupMeeting object to the DB, send
 * email, post/edit Calendar and check permission.
 * 
 * @author Peter Liu
 * 
 * </p>
 */
@Slf4j
public class SignupMeetingServiceImpl implements SignupMeetingService, Retry, MeetingTypes, SignupMessageTypes {

	@Getter @Setter
	private SignupMeetingDao signupMeetingDao;

	@Getter @Setter
	private SakaiFacade sakaiFacade;
	
	@Getter @Setter
	private SignupCacheService signupCacheService;

	@Getter @Setter
	private SignupEmailFacade signupEmailFacade;

	public void init() {
		log.debug("init");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getAllSignupMeetings(String currentSiteId, String userId) {
		List<SignupMeeting> meetings = signupMeetingDao.getAllSignupMeetings(currentSiteId);

		return screenAllowableMeetings(currentSiteId, userId, meetings);

	}

	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getSignupMeetings(String currentSiteId, String userId, Date searchEndDate) {
		List<SignupMeeting> meetings = signupMeetingDao.getSignupMeetings(currentSiteId, searchEndDate);
		return screenAllowableMeetings(currentSiteId, userId, meetings);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getSignupMeetings(String currentSiteId, String userId, Date startDate, Date endDate) {
		List<SignupMeeting> meetings = signupMeetingDao.getSignupMeetings(currentSiteId, startDate, endDate);
		return screenAllowableMeetings(currentSiteId, userId, meetings);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getSignupMeetingsInSiteWithCache(String siteId, Date startDate, int timeFrameInDays) {
		List<SignupMeeting> meetings = signupCacheService.getAllSignupMeetingsInSite(siteId, startDate, timeFrameInDays);
		return meetings;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getSignupMeetingsInSitesWithCache(List<String> siteIds, Date startDate, int timeFrameInDays) {
		List<SignupMeeting> meetings = signupCacheService.getAllSignupMeetingsInSites(siteIds, startDate, timeFrameInDays);
		return meetings;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getSignupMeetingsInSite(String siteId, Date startDate, Date endDate) {
		List<SignupMeeting> meetings = signupMeetingDao.getSignupMeetingsInSite(siteId, startDate, endDate);
		return meetings;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getSignupMeetingsInSites(List<String> siteIds, Date startDate, Date endDate) {
		List<SignupMeeting> meetings = signupMeetingDao.getSignupMeetingsInSites(siteIds, startDate, endDate);
		return meetings;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getRecurringSignupMeetings(String currentSiteId, String userId, Long recurrenceId, Date startDate) {
		List<SignupMeeting> meetings = signupMeetingDao.getRecurringSignupMeetings(currentSiteId, recurrenceId, startDate);
		return screenAllowableMeetings(currentSiteId, userId, meetings);
	}

	/**
	 * This method will return a list of meetings, which contain all the
	 * populated permission information
	 * 
	 * @param currentSiteId
	 *            a unique id which represents the current site
	 * @param userId
	 *            the internal user id (not username)
	 * @param meetings
	 *            a list of SignupMeeting objects
	 * @return a list of SignupMeeting object
	 */
	private List<SignupMeeting> screenAllowableMeetings(String currentSiteId, String userId,
			List<SignupMeeting> meetings) {
		List<SignupMeeting> allowedMeetings = new ArrayList<SignupMeeting>();

		for (SignupMeeting meeting : meetings) {
			if (isAllowedToView(meeting, userId, currentSiteId)) {
				allowedMeetings.add(meeting);
			}
		}
		updatePermissions(userId, currentSiteId, meetings);

		return allowedMeetings;
	}

	/**
	 * This will obtain the permission for attend, update and delete
	 * 
	 * @param userId
	 *            a unique id which represents the current site
	 * @param siteId
	 *            a unique id which represents the current site
	 * @param meetings
	 *            a list of SignupMeeting objects
	 */
	private void updatePermissions(String userId, String siteId, List<SignupMeeting> meetings) {
		for (SignupMeeting meeting : meetings) {
			boolean attend = isAllowToAttend(userId, siteId, meeting);
			boolean update = isAllowToUpdate(userId, siteId, meeting);
			boolean delete = isAllowToDelete(userId, siteId, meeting);
			Permission permission = new Permission(attend, update, delete);
			meeting.setPermission(permission);
		}

	}
	
	private String assignPermission(String userId, String siteId, SignupMeeting meeting){
		String targetSiteId = siteId;
		if(targetSiteId == null){
			targetSiteId = findSiteWithHighestPermissionLevel(userId, meeting);
		}

		if(targetSiteId !=null){
			boolean attend = isAllowToAttend(userId, targetSiteId, meeting);
			boolean update = isAllowToUpdate(userId, targetSiteId, meeting);
			boolean delete = isAllowToDelete(userId, targetSiteId, meeting);
			Permission permission = new Permission(attend, update, delete);
			meeting.setPermission(permission);
		}
		
		return targetSiteId;
	}
	
	private String findSiteWithHighestPermissionLevel(String userId, SignupMeeting meeting){
		List<SignupSite> sites = meeting.getSignupSites();

		//look if user has update and delete permissions
		for (SignupSite site : sites) {
			String sId = site.getSiteId();
			boolean update = isAllowToUpdate(userId, sId, meeting);
			boolean delete = isAllowToDelete(userId, sId, meeting);
			if(update && delete){
				return sId;
			}
		}
		//look if user has update permission
		for (SignupSite site : sites) {
			String sId = site.getSiteId();
			boolean update = isAllowToUpdate(userId, sId, meeting);
			if(update){
				return sId;
			}
		}
		
		//look if user has attend permission
		for (SignupSite site : sites) {
			String sId = site.getSiteId();
			boolean attend = isAllowToAttend(userId, sId, meeting);
			if(attend){
				return sId;
			}
		}
		
		return null;
	}

	/*
	 * TODO Under what condition the organizer can delete the meeting? Have to
	 * have all level permission? across site
	 */
	private boolean isAllowToDelete(String userId, String siteId, SignupMeeting meeting) {
		if (sakaiFacade.isUserAdmin(userId))
			return true;

		SignupSite site = currentSite(meeting, siteId);
		if (site != null) {
			if (site.isSiteScope()) {// GroupList is null or empty case
				if (sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_DELETE_SITE, site.getSiteId()))
					return true;
				else
					return false;
			}

			/* It's groups scope */
			if (sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_DELETE_GROUP_ALL, site.getSiteId())
					|| sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_DELETE_SITE, site.getSiteId()))
				return true;

			/*
			 * organizer has to have permission to delete every group in the
			 * list,otherwise can't delete
			 */
			boolean allowedTodelete = true;
			List<SignupGroup> signupGroups = site.getSignupGroups();
			for (SignupGroup group : signupGroups) {
				if (!(sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_DELETE_GROUP, site.getSiteId(), group.getGroupId()) 
						|| sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_DELETE_GROUP_ALL, siteId, group.getGroupId()))) {
				allowedTodelete = false;
					break;
				}
			}
			return allowedTodelete;

		}
		return false;

	}

	/* check to see if the user has update permission at any level */
	private boolean isAllowToUpdate(String userId, String siteId, SignupMeeting meeting) {
		if (sakaiFacade.isUserAdmin(userId))
			return true;

		SignupSite site = currentSite(meeting, siteId);
		if (site != null) {
			if (site.isSiteScope()) {
				if (sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_UPDATE_SITE, site.getSiteId()))
					return true;
			}
			/* Do we allow people with a group.all permission to update the meeting with a site scope?  
			 * currently we allow them to update
			 * */
			if (sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_UPDATE_GROUP_ALL, site.getSiteId())
					|| sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_UPDATE_SITE, site.getSiteId()))
				return true;

			List<SignupGroup> signupGroups = site.getSignupGroups();
			for (SignupGroup group : signupGroups) {
				if (sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_UPDATE_GROUP, site.getSiteId(), group
						.getGroupId())
						|| sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_UPDATE_GROUP_ALL, siteId, group
								.getGroupId()))
					return true;
			}
		}

		return false;

	}

	/* check if the current user can attend all items (or is super user) */
	private boolean isAllowToAttend(String userId, String siteId, SignupMeeting meeting) {
		if (sakaiFacade.isUserAdmin(userId))
			return true;

		if (sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_ATTEND_ALL, siteId))
			return true;

		SignupSite site = currentSite(meeting, siteId);
		if (site != null) {
			if (site.isSiteScope())
				return sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_ATTEND, siteId);

			List<SignupGroup> signupGroups = site.getSignupGroups();
			for (SignupGroup group : signupGroups) {
				if (sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_ATTEND, siteId, group.getGroupId())
						|| sakaiFacade
								.isAllowedGroup(userId, SakaiFacade.SIGNUP_ATTEND_ALL, siteId, group.getGroupId()))
					return true;
			}
		}
		return false;

	}

	/* check if the current user can see all items (or is super user) */
	private boolean isAllowedToView(SignupMeeting meeting, String userId, String siteId) {
		if (sakaiFacade.isUserAdmin(userId))
			return true;

		if (sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_VIEW_ALL, siteId))
			return true;

		SignupSite site = currentSite(meeting, siteId);
		if (site != null) {
			if (site.isSiteScope())
				return sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_VIEW, siteId);

			List<SignupGroup> signupGroups = site.getSignupGroups();
			for (SignupGroup group : signupGroups) {
				if (sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_VIEW, siteId, group.getGroupId())
						|| sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_VIEW_ALL, siteId, group.getGroupId()))
					return true;
			}
		}
		return false;

	}

	/* get SignupSite object for the site Id */
	private SignupSite currentSite(SignupMeeting meeting, String siteId) {
		List<SignupSite> signupSites = meeting.getSignupSites();
		for (SignupSite site : signupSites) {
			if (site.getSiteId().equals(siteId))
				return site;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Long saveMeeting(SignupMeeting signupMeeting, String userId) throws PermissionException {
		if (isAllowedToCreate(userId, signupMeeting)) {
			return signupMeetingDao.saveMeeting(signupMeeting);
		}
		throw new PermissionException(userId, "signup.create", "signup tool");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void saveMeetings(List<SignupMeeting> signupMeetings, String userId) throws PermissionException {
		if (signupMeetings ==null || signupMeetings.isEmpty())
			return;
		
		if (isAllowedToCreate(userId, signupMeetings.get(0))) {
			signupMeetingDao.saveMeetings(signupMeetings);
		}
		else{ 
			throw new PermissionException(userId, "signup.create", "signup tool");
		}
	}

	/* check to see if the user has create permission at any level */
	private boolean isAllowedToCreate(String userId, SignupMeeting signupMeeting) {
		if (sakaiFacade.isUserAdmin(userId))
			return true;

		List<SignupSite> signupSites = signupMeeting.getSignupSites();
		for (SignupSite site : signupSites) {
			if (site.isSiteScope()) {
				if (!sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_CREATE_SITE, site.getSiteId()))
					return false;
				else {
					continue;
				}
			} else {
				if (sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_CREATE_SITE, site.getSiteId())
						|| sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_CREATE_GROUP_ALL, site.getSiteId()))
					continue;

				List<SignupGroup> signupGroups = site.getSignupGroups();
				for (SignupGroup group : signupGroups) {
					if (!(sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_CREATE_GROUP, site.getSiteId(), group.getGroupId()) 
							|| sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_CREATE_GROUP_ALL, site.getSiteId(), group.getGroupId())))
						return false;
				}
			}
		}

		return true;

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAllowedToCreateinGroup(String userId, String siteId, String groupId) {
		return sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_CREATE_GROUP_ALL, siteId)
				|| sakaiFacade.isAllowedGroup(userId, SakaiFacade.SIGNUP_CREATE_GROUP, siteId, groupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAllowedToCreateinSite(String userId, String siteId) {
		return sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_CREATE_SITE, siteId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAllowedToCreateAnyInSite(String userId, String siteId) {
		if (sakaiFacade.isUserAdmin(userId))
			return true;

		if (sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_CREATE_SITE, siteId))
			return true;

		/* check groups */
		Site site = null;
		try {
			site = sakaiFacade.getSiteService().getSite(siteId);
		} catch (IdUnusedException e) {
			log.info("IdUnusedException for siteId: siteId  -- " + e.getMessage());
			return false;
		}
		Collection groups = site.getGroups();
		if (groups == null || groups.isEmpty())
			return false;

		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			Group gp = (Group) iter.next();
			if (isAllowedToCreateinGroup(userId, siteId, gp.getId()))
				return true;

		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateSignupMeeting(SignupMeeting meeting, boolean isOrganizer) throws Exception {
		Permission permission = meeting.getPermission();

		/*
		 * if null, only consider an organizer-updating case (higher requirement
		 * level)
		 */
		if (permission == null) {
			if (isAllowToUpdate(sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), meeting)) {
				signupMeetingDao.updateMeeting(meeting);
				return;
			}
			throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.update", "SignupTool");
		}

		if (isOrganizer) {
			if (permission.isUpdate())
				signupMeetingDao.updateMeeting(meeting);
			else
				throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.update", "SignupTool");
		} else {
			if (permission.isAttend())
				signupMeetingDao.updateMeeting(meeting);
			else
				throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.attend", "SignupTool");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void updateSignupMeetings(List<SignupMeeting> meetings, boolean isOrganizer) throws Exception {
		
		if (meetings == null || meetings.isEmpty()) {
			return;
		}
		
		SignupMeeting oneMeeting = (SignupMeeting) meetings.get(0);
		/* Here, assuming that organizer has the update permission for any one meeting, then he has the permissions for all */	
		Permission permission = oneMeeting.getPermission();

		/*
		 * if null, only consider an organizer-updating case (higher requirement
		 * level)
		 */
		if (permission == null) {
			if (isAllowToUpdate(sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), oneMeeting)) {
				signupMeetingDao.updateMeetings(meetings);
				return;
			}
			throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.update", "SignupTool");
		}

		if (isOrganizer) {
			if (permission.isUpdate())
				signupMeetingDao.updateMeetings(meetings);
			else
				throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.update", "SignupTool");
		} else {
			if (permission.isAttend())
				signupMeetingDao.updateMeetings(meetings);
			else
				throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.attend", "SignupTool");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void updateModifiedMeetings(List<SignupMeeting> meetings, List<SignupTimeslot> removedTimeslots,
			boolean isOrganizer) throws Exception {
		
		if (meetings == null || meetings.isEmpty()) {
			return;
		}
		
		SignupMeeting oneMeeting = (SignupMeeting) meetings.get(0);
		/* Here, assuming that organizer has the update permission for any one meeting, then he has the permissions for all */	
		Permission permission = oneMeeting.getPermission();

		/*
		 * if null, only consider an organizer-updating case (higher requirement
		 * level)
		 */
		if (permission == null) {
			if (isAllowToUpdate(sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), oneMeeting)) {
				signupMeetingDao.updateModifiedMeetings(meetings, removedTimeslots);
				return;
			}
			throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.update", "SignupTool");
		}

		if (isOrganizer) {
			if (permission.isUpdate())
				signupMeetingDao.updateModifiedMeetings(meetings, removedTimeslots);
			else
				throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.update", "SignupTool");
		} else {
			if (permission.isAttend())
				signupMeetingDao.updateModifiedMeetings(meetings, removedTimeslots);
			else
				throw new PermissionException(sakaiFacade.getCurrentUserId(), "signup.attend", "SignupTool");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SignupMeeting loadSignupMeeting(Long meetingId, String userId, String siteId) {
		SignupMeeting meeting = signupMeetingDao.loadSignupMeeting(meetingId);
		List<SignupMeeting> temp = new ArrayList<SignupMeeting>();
		temp.add(meeting);
		updatePermissions(userId, siteId, temp);
		return meeting;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SignupTargetSiteEventInfo loadSignupMeetingWithAutoSelectedSite(Long meetingId, String userId, String siteId) {
		SignupMeeting meeting = signupMeetingDao.loadSignupMeeting(meetingId);
		String sId = assignPermission(userId, siteId, meeting);
		SignupTargetSiteEventInfo defaultSiteEvent = new SignupTargetSiteEventInfo(meeting,sId);
		return defaultSiteEvent;
	}

	/* TODO: attachments if any */
	/**
	 * {@inheritDoc}
	 */
	public void postToCalendar(SignupMeeting meeting) throws Exception {
		modifyCalendar(meeting);
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyCalendar(SignupMeeting meeting) throws Exception {
		List<SignupSite> signupSites = meeting.getSignupSites();
		boolean saveMeeting = false;
		List<SignupTimeslot> calendarBlocks = scanDivideCalendarBlocks(meeting);
		boolean hasMulptleBlock = calendarBlocks.size() > 1? true : false;
		
		/*we remove all the calendar events for custom-defined type to simplify process
		 * when existing meetings come here. New meeting don't have permission setting yet 
		 * and it is null.*/
		if(meeting.getPermission() !=null && meeting.getPermission().isUpdate()){
			//only instructor or maintainer/TF can do this since they can create/delete/move new blocks
			if(CUSTOM_TIMESLOTS.equals(meeting.getMeetingType())){
				List<SignupMeeting> smList = new ArrayList<SignupMeeting>();
				smList.add(meeting);
				removeCalendarEvents(smList);
			}
		}
		
		int sequence = 1;
		boolean firstBlockLoop = true;
		/*only custom-defined events have multiple discontinued calendar blocks*/
		int calBlock =0;
		for (SignupTimeslot calendarBlock : calendarBlocks) {			
			for (SignupSite site : signupSites) {
				try {
					Calendar calendar = chooseCalendar(site);
					
					if (calendar == null)// something went wrong when fetching the calendar
						continue;
	
					String eventId = null;
					if (site.isSiteScope()) {
						eventId = site.getCalendarEventId();
					} else {
						List<SignupGroup> signupGroups = site.getSignupGroups();
						for (SignupGroup group : signupGroups) {
							eventId = group.getCalendarEventId();
							break;
						}
					}
					CalendarEventEdit eventEdit = null;
					
					if(meeting.getPermission() !=null && !meeting.getPermission().isUpdate() && CUSTOM_TIMESLOTS.equals(meeting.getMeetingType())){
						/*Case: for customed Calendar blocks, we need to break down the eventIds and update one by one.
						 *only attendee can come here. Instructor/TF will first remove old Calendar blocks and create new ones again for simplicity!!!
						 */
						eventId = retrieveCustomCalendarEventId(calBlock,eventId);
					}
					else{
						/*make sure that instructor/TF will create new Calendar blocks since calendars is already removed*/
						if(CUSTOM_TIMESLOTS.equals(meeting.getMeetingType()))
								eventId =null;
					}
	
					boolean isNew = true;
					/*for custom_ts type, TF/instructor has no modification here eventId is null*/
					if (eventId != null && eventId.trim().length() > 1) {
						//allow attendee to update calendar
                        SecurityAdvisor advisor = sakaiFacade.pushAllowCalendarEdit(calendar);

						try {
							eventEdit = calendar.getEditEvent(eventId,
									org.sakaiproject.calendar.api.CalendarService.EVENT_MODIFY_CALENDAR);
							isNew = false;
							if (!calendar.allowEditEvent(eventId)) {
								continue;
                            }
						}catch (IdUnusedException e) {
							log.debug("IdUnusedException: " + e.getMessage());
							// If the event was removed from the calendar.
							eventEdit = calendarEvent(calendar, meeting, site);
                        } finally {
                            sakaiFacade.popSecurityAdvisor(advisor);
                        }
					} else {
						eventEdit = calendarEvent(calendar, meeting, site);
					}
					if (eventEdit == null)
						continue;
	
					/* new time frame */
					String title_suffix = "";
					if(hasMulptleBlock){
						title_suffix = " (part " + sequence + ")";
						sequence++;
					}

					populateDataForEventEditObject(eventEdit, meeting,title_suffix, calendarBlock.getStartTime(),calendarBlock.getEndTime());
					
					calendar.commitEvent(eventEdit);
					
					if (isNew) {
						saveMeeting = true; // Need to save these back
						if (site.isSiteScope()) {
							if(firstBlockLoop){
								site.setCalendarEventId(eventEdit.getId());
								site.setCalendarId(calendar.getId());
							}else{
								/*only custom-defined event type could come here*/
								String lastEventId = site.getCalendarEventId();
								//appending new one
								site.setCalendarEventId(lastEventId + "|" + eventEdit.getId());
							}
						} else {
							List<SignupGroup> signupGroups = site.getSignupGroups();
							for (SignupGroup group : signupGroups) {
								if(firstBlockLoop){
									group.setCalendarEventId(eventEdit.getId());
									group.setCalendarId(calendar.getId());
								}else{
									String lastEventId = group.getCalendarEventId();
									//appending new one
									group.setCalendarEventId(lastEventId + "|" + eventEdit.getId());
								}
								
							}
						}
					}
				} catch (PermissionException pe) {
					log.info("PermissionException for calendar-modification: " + pe.getMessage());
					throw pe;
				}
			}//end-for
			
			firstBlockLoop = false;
			calBlock++;
		}//end-for
		if (saveMeeting) {
			updateMeetingWithVersionHandling(meeting);
		}
		

	}
	
	/**
	 * Only custom-defined event type has discontinued calendar blocks
	 * since the user can do anything with the time slots in discontinued way.
	 * It will make the calendar unreadable.
	 */
	private void populateDataForEventEditObject(CalendarEventEdit eventEdit, SignupMeeting meeting,String title_suffix, 
			Date startTime, Date endTime ){
		TimeService timeService = getSakaiFacade().getTimeService();
		Time start = timeService.newTime(startTime.getTime());
		Time end = timeService.newTime(endTime.getTime());
		TimeRange timeRange = timeService.newTimeRange(start, end, true, false);
		eventEdit.setRange(timeRange);
		
		String attendeeNamesMarkup = "";
		int num = 0;

        if(meeting.getSignupTimeSlots().size() > 0) {
            // TODO: 'Attendees' needs internationalising
            attendeeNamesMarkup += "<br /><br /><span style=\"font-weight: bold\"><b>Attendees:</b></span><br />";
        }

        boolean displayAttendeeName = false;
        for(SignupTimeslot ts : meeting.getSignupTimeSlots()) {
        	displayAttendeeName = ts.isDisplayAttendees();//just need one of TS, it is not fine-grained yet
        	//case: custom calender blocks, only print the related info
        	if((startTime.getTime() <=  ts.getStartTime().getTime()) && endTime.getTime() >= ts.getEndTime().getTime()){
        		num += ts.getAttendees().size();
	            if(ts.isDisplayAttendees() && !ts.getAttendees().isEmpty()){
	            	//privacy issue
		            for(SignupAttendee attendee : ts.getAttendees()) {
		                attendeeNamesMarkup += ("<span style=\"font-weight: italic\"><i>" + sakaiFacade.getUserDisplayName(attendee.getAttendeeUserId()) + "</i></span><br />");
		            }
	            }
        	}
        }
        
        if(!displayAttendeeName || num < 1){
        	 attendeeNamesMarkup += ("<span style=\"font-weight: italic\"><i> Currently, " +  num + " attendees have been signed up.</i></span><br />");
        }
         
		String desc = meeting.getDescription() + attendeeNamesMarkup;
		eventEdit.setDescription(PlainTextFormat.convertFormattedHtmlTextToPlaintext(desc));
		eventEdit.setLocation(meeting.getLocation());
        // TODO: 'attendees' needs internationalising
		eventEdit.setDisplayName(meeting.getTitle() + title_suffix + " (" + num + " attendees)");			
		eventEdit.setRange(timeRange);
	}
	
	private String retrieveCustomCalendarEventId(int blockNum, String eventIds){
		/*separate the eventIds token by '|' */
		StringTokenizer token = new StringTokenizer(eventIds,"|"); 
		int index=0;
		while (token.hasMoreTokens()) {
			if(blockNum == index++)
				return token.nextToken().trim();
			else
				token.nextToken();
		}
		
		return null;
	}
	
	/**
	 * scan for existing meetings to see whether it has multiple calendar blocks
	 * @param meeting
	 * @return
	 */
	private boolean hasMeetingWithMultipleCalendarBlocks(SignupMeeting meeting){
		if(!CUSTOM_TIMESLOTS.equals(meeting.getMeetingType())){
			return false;
		}
		boolean hasMultipleBlocks = false;
		List<SignupSite> sites = meeting.getSignupSites();
		if (sites == null || sites.isEmpty())
			return false;
		
		for (SignupSite site : sites) {
			String eventId = null;
			if (site.isSiteScope()) {
				eventId = site.getCalendarEventId();
				if(eventId !=null && eventId.contains("|")){
					return true;
				}
			} else {
				List<SignupGroup> signupGroups = site.getSignupGroups();
				for (SignupGroup group : signupGroups) {
					eventId = group.getCalendarEventId();
					if(eventId !=null && eventId.contains("|")){
						return true;
					}
				}
			}
			
		}
		return hasMultipleBlocks;
	}
	
	private List<SignupTimeslot> scanDivideCalendarBlocks(SignupMeeting meeting){
		final int timeApart = 2*60*60*1000; // 2 hours
		List<SignupTimeslot> tsList = new ArrayList<SignupTimeslot>();
		if(CUSTOM_TIMESLOTS.equals(meeting.getMeetingType()) && (meeting.isInMultipleCalendarBlocks() || hasMeetingWithMultipleCalendarBlocks(meeting))){
			List<SignupTimeslot> tsLs = meeting.getSignupTimeSlots();
			if(tsLs !=null && !tsLs.isEmpty()){
				/*The meetings timeslots are already sorted before coming here*/
				Date startTime= tsLs.get(0).getStartTime();
				int cursor=0;
				for (int i = 0; i < tsLs.size()-1; i++) {
					long firstBlockEndTime = tsLs.get(cursor).getEndTime().getTime();
					
					//to see if next block is within first block
					long secondBlockEndTime = tsLs.get(i+1).getEndTime().getTime();					
					if(secondBlockEndTime - firstBlockEndTime >= 0){
						cursor = i+1;//advance to next block
					}
					
					long nextBlockStartTime = tsLs.get(i+1).getStartTime().getTime();
					if(nextBlockStartTime - firstBlockEndTime > timeApart){
						SignupTimeslot newTs = new SignupTimeslot();
						newTs.setStartTime(startTime);
						newTs.setEndTime(tsLs.get(i).getEndTime());
						tsList.add(newTs);
						//reset start time
						startTime = tsLs.get(i+1).getStartTime();
					}
					
				}
				
				/* add last block*/
				SignupTimeslot newTs = new SignupTimeslot();
				newTs.setStartTime(startTime);
				newTs.setEndTime(meeting.getEndTime());
				tsList.add(newTs);
			}
		}
		else{
			/*otherwise there is only one block in calendar*/
			SignupTimeslot newTs = new SignupTimeslot();
			newTs.setStartTime(meeting.getStartTime());
			newTs.setEndTime(meeting.getEndTime());
			tsList.add(newTs);
		}
		
		return tsList;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void removeCalendarEvents(List<SignupMeeting> meetings) throws Exception {
		if (meetings == null || meetings.isEmpty())
			return;

		for (SignupMeeting meeting : meetings) {
			List<SignupSite> sites = meeting.getSignupSites();
			if (sites == null || sites.isEmpty())
				continue;

			for (SignupSite site : sites) {
				try {
					Calendar calendar = chooseCalendar(site);

					if (calendar == null)// something went wrong when fetching the calendar
						continue;

					String eventIds = null;
					if (site.isSiteScope()) {
						eventIds = site.getCalendarEventId();
					} else {
						List<SignupGroup> signupGroups = site.getSignupGroups();
						for (SignupGroup group : signupGroups) {
							eventIds = group.getCalendarEventId();
							break;
						}
					}

					if (eventIds == null || eventIds.trim().length() < 1)
						continue;

					/*separate the eventIds token by '|' */
					StringTokenizer token = new StringTokenizer(eventIds,"|"); 
					List<String> evtIds = new ArrayList<String>(); 
					while (token.hasMoreTokens()) {
						evtIds.add(token.nextToken().trim()); 
					}
					
					for (String evtId : evtIds) {
						CalendarEventEdit eventEdit = calendar.getEditEvent(evtId,
								org.sakaiproject.calendar.api.CalendarService.EVENT_REMOVE_CALENDAR);
						if (eventEdit == null)
							continue;
	
						if (!calendar.allowEditEvent(evtId))
							continue;
	
						calendar.removeEvent(eventEdit);
					}
					
				} catch (PermissionException e) {
					log.info("PermissionException for removal of calendar: " + e.getMessage());
				}
			}
		}

	}

	// See if additional calendar is deployed and ready to use.
	// If not, we use the Sakai calendar tool by default.
	private Calendar chooseCalendar(SignupSite site) throws PermissionException {
		Calendar calendar = sakaiFacade.getAdditionalCalendar(site.getSiteId());
		if (calendar == null) {
			calendar = sakaiFacade.getCalendar(site.getSiteId());
		}
		return calendar;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void removeCalendarEventsOnModifiedMeeting(List<SignupMeeting> meetings) throws Exception {
		
		try{
			removeCalendarEvents(meetings);
			
			/*remove calendar info in the event object*/
			for (SignupMeeting sm : meetings) {			
				List<SignupSite> sites = sm.getSignupSites();
				if(sites !=null && !sites.isEmpty()){
					for (SignupSite s : sites) {
						if(s.getCalendarEventId() !=null){
							s.setCalendarEventId(null);
							s.setCalendarId(null);
						}
						List<SignupGroup> grps = s.getSignupGroups();
						if(grps !=null && !grps.isEmpty()){
							for (SignupGroup g : grps) {
								if(g.getCalendarEventId()!=null){
									g.setCalendarEventId(null);
									g.setCalendarId(null);
								}
							}
						}
					}
				}
				
				updateMeetingWithVersionHandling(sm);
			}
		}catch(Exception e){
			log.warn("Exception for removal of calendar and calendar info may not be removed from events objects: " + e.getMessage());
		}
	}

	/*
	 * This method will create a skeleton  event/meeting in the Scheduler tool
	 * in that site. It still needs to be committed.
	 */
	private CalendarEventEdit calendarEvent(Calendar calendar, SignupMeeting meeting, SignupSite site)
			throws IdUnusedException, PermissionException {
		CalendarEventEdit addEvent = calendar.addEvent();
		addEvent.setType("Meeting");
		if (!site.isSiteScope()) {
			List<Group> groups = groupIds(site);
			addEvent.setGroupAccess(groups, true);
		}

		return addEvent;
	}

	/*
	 * Main purpose is to update the calendarId and eventId into the related
	 * meeting site/groups
	 */
	private void updateMeetingWithVersionHandling(SignupMeeting meeting) throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				updateSignupMeeting(meeting, true);
				return;
			} catch (OptimisticLockingFailureException e) {
				// nothing
			}
		}
		throw new SignupUserActionException("Some one updated the meeting before your update. Please try again.");
	}

	/**
	 * This will return a list of Group objects from the site
	 * 
	 * @param site
	 *            a unique id which represents the current site
	 * @return a list of Group objects from the site
	 */
	private List<Group> groupIds(SignupSite site) {
		List<Group> groups = new ArrayList<Group>();
		List<SignupGroup> signupGroups = site.getSignupGroups();
		for (SignupGroup group : signupGroups) {
			try {
				groups.add(sakaiFacade.getGroup(site.getSiteId(), group.getGroupId()));
			} catch (IdUnusedException e) {
				log.info("IdUnusedException: " + e.getMessage());
			}
		}
		return groups;
	}


	/**
	 * {@inheritDoc}
	 */
	public void sendEmail(SignupMeeting signupMeeting, String messageType) throws Exception {
		signupEmailFacade.sendEmailAllUsers(signupMeeting, messageType);

	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEmailToOrganizer(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
		signupEmailFacade.sendEmailToOrganizer(signupEventTrackingInfo);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendCancellationEmail(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
		signupEmailFacade.sendCancellationEmail(signupEventTrackingInfo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void sendUpdateCommentEmail(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
		signupEmailFacade.sendUpdateCommentEmail(signupEventTrackingInfo);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeMeetings(List<SignupMeeting> meetings) throws Exception {
		signupMeetingDao.removeMeetings(meetings);
		Set<Long> sent = new HashSet<Long>();
				
		for(SignupMeeting m: meetings) {
			if(!m.isMeetingExpired()) {
				//Only send once per recurrenceid
				if (!sent.contains(m.getRecurrenceId())) {
					sent.add(m.getRecurrenceId());
					log.info("Meeting is still available, email notifications will be sent");
					//SIGNUP-188 :If an event is cancelled, all the site members get an email
					m.setSendEmailToSelectedPeopleOnly(SEND_EMAIL_ONLY_SIGNED_UP_ATTENDEES);
					signupEmailFacade.sendEmailAllUsers(m, SignupMessageTypes.SIGNUP_CANCEL_MEETING);
				}
				else {
					log.debug("Not sending email for duplicate reurrenceId: {}", m.getRecurrenceId());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEmailToParticipantsByOrganizerAction(SignupEventTrackingInfo signupEventTrackingInfo)
			throws Exception {
		signupEmailFacade.sendEmailToParticipantsByOrganizerAction(signupEventTrackingInfo);

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEventExisted(Long eventId) {
		return signupMeetingDao.isEventExisted(eventId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void sendEmailToAttendee(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
		signupEmailFacade.sendEmailToAttendee(signupEventTrackingInfo);
	}

	@Override
	public List<String> getAllLocations(String siteId) throws Exception {
		return signupMeetingDao.getAllLocations(siteId);
	}

	@Override
	public List<String> getAllCategories(String siteId) throws Exception {
		return signupMeetingDao.getAllCategories(siteId);
	}
	
	


}
