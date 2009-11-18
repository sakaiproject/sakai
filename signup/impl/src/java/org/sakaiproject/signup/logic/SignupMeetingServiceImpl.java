/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarEvent.EventAccess;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.dao.SignupMeetingDao;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.util.PlainTextFormat;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.time.api.TimeRange;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * <p>
 * SignupMeetingServiceImpl is an implementation of SignupMeetingService, which
 * provides methods to manipulate the SignupMeeting object to the DB, send
 * email, post/edit Calendar and check permission.
 * </p>
 */
public class SignupMeetingServiceImpl implements SignupMeetingService, Retry {

	private static Log log = LogFactory.getLog(SignupMeetingServiceImpl.class);

	private SignupMeetingDao signupMeetingDao;

	private SakaiFacade sakaiFacade;

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

	/* check to see if the user has updte permission at any level */
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

	/* get SignupSit object for the site Id */
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
	 * get SignupMeetingDao object
	 */
	public SignupMeetingDao getSignupMeetingDao() {
		return signupMeetingDao;
	}

	/**
	 * set SignupMeetingDao object
	 * 
	 * @param signupMeetingDao
	 *            a SignupMeetingDao object, which has implemented the
	 *            SignupMeetingDao interface
	 */
	public void setSignupMeetingDao(SignupMeetingDao signupMeetingDao) {
		this.signupMeetingDao = signupMeetingDao;
	}

	/**
	 * get a SakaiFacade object, which has implemented the SakaiFacade interface
	 * 
	 * @return a SakaiFacade object
	 */
	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	/**
	 * set the SakaiFacade object
	 * 
	 * @param sakaiFacade
	 *            a SakaFacade object
	 */
	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
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
		if (groups == null && groups.isEmpty())
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
	public SignupMeeting loadSignupMeeting(Long meetingId, String userId, String siteId) {
		SignupMeeting meeting = signupMeetingDao.loadSignupMeeting(meetingId);
		List<SignupMeeting> temp = new ArrayList<SignupMeeting>();
		temp.add(meeting);
		updatePermissions(userId, siteId, temp);
		return meeting;
	}

	/* TODO: attachments if any */
	/**
	 * {@inheritDoc}
	 */
	public void postToCalendar(SignupMeeting meeting) throws Exception {
		try {
			List<SignupSite> signupSites = meeting.getSignupSites();
			for (SignupSite site : signupSites) {
				Calendar calendar = sakaiFacade.getCalendar(site.getSiteId());
				if (calendar == null)// site does not have calendar tool
					continue;
				CalendarEvent event = calendarEvent(calendar, meeting, site);
				if (event == null)
					throw new Exception("TODO: A database error occured");

				if (site.isSiteScope()) {
					site.setCalendarEventId(event.getId());
					site.setCalendarId(calendar.getId());
					continue;
				}

				List<SignupGroup> signupGroups = site.getSignupGroups();
				for (SignupGroup group : signupGroups) {
					group.setCalendarEventId(event.getId());
					group.setCalendarId(calendar.getId());
				}

			}
			updateMeetingWithVersionHandling(meeting);

		} catch (IdUnusedException e) {
			log.info("IdUnusedException: " + e.getMessage());
			throw e;
		} catch (PermissionException pe) {
			log.info("PermissionException for posting calendar: " + pe.getMessage());
			throw pe;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyCalendar(SignupMeeting meeting) throws Exception {
		List<SignupSite> signupSites = meeting.getSignupSites();
		for (SignupSite site : signupSites) {
			try {
				Calendar calendar = sakaiFacade.getCalendar(site.getSiteId());
				if (calendar == null)// site does not have calendar tool
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

				if (eventId == null || eventId.trim().length() < 1)
					continue;

				CalendarEventEdit eventEdit = calendar.getEditEvent(eventId,
						org.sakaiproject.calendar.api.CalendarService.EVENT_MODIFY_CALENDAR);
				if (eventEdit == null)
					continue;

				if (!calendar.allowEditEvent(eventId))
					continue;

				/* new time frame */
				TimeRange timeRange = getSakaiFacade().getTimeService().newTimeRange(meeting.getStartTime().getTime(),
						meeting.getEndTime().getTime() - meeting.getStartTime().getTime());

				String desc = meeting.getDescription();
				eventEdit.setDescription(PlainTextFormat.convertFormattedHtmlTextToPlaintext(desc));
				eventEdit.setLocation(meeting.getLocation());
				eventEdit.setDisplayName(meeting.getTitle());
				eventEdit.setRange(timeRange);
				calendar.commitEvent(eventEdit);
			} catch (IdUnusedException e) {
				log.info("IdUnusedException: " + e.getMessage());
				throw e;
			} catch (PermissionException pe) {
				log.info("PermissionException for calendar-modification: " + pe.getMessage());
				throw pe;
			}
		}

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
					Calendar calendar = sakaiFacade.getCalendar(site.getSiteId());
					if (calendar == null)// site does not have calendar tool
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

					if (eventId == null || eventId.trim().length() < 1)
						continue;

					CalendarEventEdit eventEdit = calendar.getEditEvent(eventId,
							org.sakaiproject.calendar.api.CalendarService.EVENT_REMOVE_CALENDAR);
					if (eventEdit == null)
						continue;

					if (!calendar.allowEditEvent(eventId))
						continue;

					calendar.removeEvent(eventEdit);
				} catch (IdUnusedException e) {
					log.info("IdUnusedException: " + e.getMessage());
				} catch (PermissionException e) {
					log.info("PermissionException for removal of calendar: " + e.getMessage());
				}
			}
		}

	}

	/*
	 * This method will post the event/meeting to the Calendar at Scheduler tool
	 * in that site
	 */
	@SuppressWarnings("unchecked")
	private CalendarEvent calendarEvent(Calendar calendar, SignupMeeting meeting, SignupSite site)
			throws IdUnusedException, PermissionException {
		TimeRange timeRange = getSakaiFacade().getTimeService().newTimeRange(meeting.getStartTime().getTime(),
				meeting.getEndTime().getTime() - meeting.getStartTime().getTime());
		String title = meeting.getTitle();
		Collection<Group> groups = Collections.EMPTY_LIST;
		EventAccess eventAccess = CalendarEvent.EventAccess.SITE;
		if (!site.isSiteScope()) {
			groups = groupIds(site);
			eventAccess = CalendarEvent.EventAccess.GROUPED;
		}
		String mDescription = PlainTextFormat.convertFormattedHtmlTextToPlaintext(meeting.getDescription());
		CalendarEvent addEvent = calendar.addEvent(timeRange, title, mDescription, "Meeting", meeting.getLocation(),
				eventAccess, groups, Collections.EMPTY_LIST);

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
	 * get SignupEmailFacade object
	 * 
	 * @return a SignupEmailFacade object
	 */
	public SignupEmailFacade getSignupEmailFacade() {
		return signupEmailFacade;
	}

	/**
	 * set a SignupEmailFacade object
	 * 
	 * @param signupEmailFacade
	 *            a SignupEmailFacade object
	 */
	public void setSignupEmailFacade(SignupEmailFacade signupEmailFacade) {
		this.signupEmailFacade = signupEmailFacade;
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
	public void removeMeetings(List<SignupMeeting> meetings) {
		signupMeetingDao.removeMeetings(meetings);
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

}
