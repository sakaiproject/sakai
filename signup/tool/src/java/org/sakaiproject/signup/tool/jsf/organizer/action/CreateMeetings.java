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
package org.sakaiproject.signup.tool.jsf.organizer.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupEventTypes;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.attachment.AttachmentHandler;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * <p>
 * This class provides methods to create Sign-up meeting events regardless of
 * single or recurrence events.
 * </p>
 */
public class CreateMeetings extends SignupAction implements MeetingTypes, SignupMessageTypes, SignupBeanConstants {

	private SignupMeeting signupMeeting;

	List<SignupMeeting> signupMeetings;

	private boolean sendEmail;

	private final SakaiFacade sakaiFacade;
	
	private AttachmentHandler attachmentHandler;

	private boolean assignParticitpantsToAllEvents;

	private boolean assignParticatpantsToFirstOne;

	private String signupBeginsType;

	/* sign up can start before this minutes/hours/days */
	private int signupBegins;

	private String deadlineTimeType;

	/* sign up deadline before this minutes/hours/days */
	private int deadlineTime;

	private int signupBegin;

	private String signupBeginType;

	/**
	 * Constructor
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object.
	 * @param sendEmail
	 *            a boolean value
	 * @param assignParticatpantsToFirstOne
	 *            a boolean value. True if attendee will assigned to first
	 *            meeting event.
	 * @param assignParicitpantsToAllEvents
	 *            a boolean value. True if attendees will assigned to all
	 *            recurring meetings.
	 * @param signupBegin
	 *            an int value
	 * @param signupBeginType
	 *            a String value
	 * @param signupDeadline
	 *            a int value
	 * @param signupDeadlineType
	 *            a String value
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 * @param currentUserId
	 *            an unique sakai internal user id.
	 * @param currentSiteId
	 *            an unique sakai site id.
	 * @param isOrganizer
	 *            a boolean value
	 */
	public CreateMeetings(SignupMeeting signupMeeting, boolean sendEmail, boolean assignParticatpantsToFirstOne,
			boolean assignParicitpantsToAllEvents, int signupBegin, String signupBeginType, int signupDeadline,
			String signupDeadlineType, SakaiFacade sakaiFacade, SignupMeetingService signupMeetingService, AttachmentHandler attachmentHandler,
			String currentUserId, String currentSiteId, boolean isOrganizer) {
		super(currentUserId, currentSiteId, signupMeetingService, isOrganizer);
		this.signupMeeting = signupMeeting;
		this.sendEmail = sendEmail;
		this.assignParticatpantsToFirstOne = assignParticatpantsToFirstOne;
		this.assignParticitpantsToAllEvents = assignParicitpantsToAllEvents;
		this.signupBegin = signupBegin;
		this.signupBeginType = signupBeginType;
		this.deadlineTime = signupDeadline;
		this.deadlineTimeType = signupDeadlineType;
		this.sakaiFacade = sakaiFacade;
		this.signupMeetings = new ArrayList<SignupMeeting>();
		this.attachmentHandler = attachmentHandler;

	}

	/**
	 * It will save the SignupMeeting list into DB and send email to notify
	 * participants
	 * 
	 * @throws Exception
	 */
	public void processSaveMeetings() throws Exception

	{
		Calendar calendar = Calendar.getInstance();
		int numOfRecurs = 0;// once Only
		/* For recurrence case */
		if (!ONCE_ONLY.equals(signupMeeting.getRepeatType())) {
			numOfRecurs = getNumOfRecurrence(signupMeeting.getRepeatType(), signupMeeting.getStartTime(), signupMeeting
					.getRepeatUntil());
		}
		calendar.setLenient(true);
		calendar.setTime(signupMeeting.getStartTime());
		// recurrence = true;
		if (DAILY.equals(signupMeeting.getRepeatType())) {
			createRecurMeetings(calendar, numOfRecurs, perDay);
		} else if (WEEKDAYS.equals(signupMeeting.getRepeatType())) {
			createRecurMeetings(calendar, numOfRecurs, perDay);
			removeWeekendDays();
		}else if (WEEKLY.equals(signupMeeting.getRepeatType())) {
			createRecurMeetings(calendar, numOfRecurs, perWeek);
		} else if (BIWEEKLY.equals(signupMeeting.getRepeatType())) {
			createRecurMeetings(calendar, numOfRecurs, perBiweek);
		} else
			createRecurMeetings(calendar, numOfRecurs, onceOnly);

		postMeetings(signupMeetings);

	}

	/**
	 * It will give the number of recurrence for certail time frame
	 * 
	 * @param recurType
	 *            a String object.
	 * @param effectiveDate
	 *            a Date object which defines the starting date.
	 * @param untilDate
	 *            a Date object which defines the possible ending date.
	 * @return
	 */
	public static int getNumOfRecurrence(String recurType, Date effectiveDate, Date untilDate) {
		int numOfRecurs = 0;
		long firstMeetingEndTime = effectiveDate.getTime();
		int availDaysForRepeat = 0;
		Calendar untilCal = Calendar.getInstance();
		untilCal.setTime(untilDate);
		untilCal.set(Calendar.HOUR_OF_DAY, 23);
		untilCal.set(Calendar.MINUTE, 59);
		untilCal.set(Calendar.SECOND, 59);
		availDaysForRepeat = (int) ((untilCal.getTimeInMillis() - firstMeetingEndTime) / DAY_IN_MILLISEC);

		if (DAILY.equals(recurType) || WEEKDAYS.equals(recurType)) {
			numOfRecurs = availDaysForRepeat / perDay;
		} else if (WEEKLY.equals(recurType)) {
			numOfRecurs = availDaysForRepeat / perWeek;
		} else if (BIWEEKLY.equals(recurType)) {
			numOfRecurs = availDaysForRepeat / perBiweek;
		}
		
		/*Case: weekdays*/
		if(WEEKDAYS.equals(recurType) && numOfRecurs < 2){
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(effectiveDate);
			int dayname = startCal.get(Calendar.DAY_OF_WEEK);
			if(dayname == Calendar.SATURDAY)
				numOfRecurs =0;//no weekdays are there
		}
		return numOfRecurs;
	}

	/**
	 * It will generate a list of SignupMeeting object and wrap up with
	 * corresponding contents for saving to DB
	 * 
	 */
	private void createRecurMeetings(Calendar calendar, long numOfRecurs, int intervalOfRecurs) {
		int eday, sday, sdday, edday;

		if (!ONCE_ONLY.equals(signupMeeting.getRepeatType()) && numOfRecurs < 1) {
			Utilities.addErrorMessage(Utilities.rb.getString("event.repeatbeforestart"));
			return;
		}

		for (int i = 0; i <= numOfRecurs; i++) {
			SignupMeeting beta = new SignupMeeting();
			beta = prepareDeepCopy(this.signupMeeting, i * intervalOfRecurs);
			calendar.setTime(this.signupMeeting.getStartTime());
			sday = calendar.get(Calendar.DATE) + i * intervalOfRecurs;
			calendar.set(Calendar.DATE, sday);
			beta.setStartTime(calendar.getTime());
			calendar.setTime(this.signupMeeting.getEndTime());
			eday = calendar.get(Calendar.DATE) + i * intervalOfRecurs;
			calendar.set(Calendar.DATE, eday);
			beta.setEndTime(calendar.getTime());

			calendar.setTime(this.signupMeeting.getSignupBegins());
			sdday = calendar.get(Calendar.DATE) + i * intervalOfRecurs;
			calendar.set(Calendar.DATE, sdday);
			beta.setSignupBegins(calendar.getTime());
			calendar.setTime(this.signupMeeting.getSignupDeadline());
			edday = calendar.get(Calendar.DATE) + i * intervalOfRecurs;
			calendar.set(Calendar.DATE, edday);
			beta.setSignupDeadline(calendar.getTime());
			
			/*set attachments*/
			beta.setSignupAttachments(copyAttachments(this.signupMeeting, numOfRecurs, i));			

			if (this.assignParticatpantsToFirstOne) {
				/* Turn off after first one copy */
				this.assignParticitpantsToAllEvents = false;
				this.assignParticatpantsToFirstOne = false;
			}

			this.signupMeetings.add(beta);
		}
	}
	
	private void removeWeekendDays(){
		if(this.signupMeetings !=null && !this.signupMeetings.isEmpty()){
			for (int i = signupMeetings.size()-1; i >= 0; i--) {		
				SignupMeeting sm = (SignupMeeting) signupMeetings.get(i);				
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(sm.getStartTime());
				int dayOfweek = startCal.get(Calendar.DAY_OF_WEEK);
				if(dayOfweek == Calendar.SATURDAY || dayOfweek == Calendar.SUNDAY)
					signupMeetings.remove(i);
			}
		}
	}
	
	private List<SignupAttachment> copyAttachments(SignupMeeting sm, long numOfRecurs, int index){
		if(numOfRecurs ==0){
			List<SignupAttachment> attachs = sm.getSignupAttachments();
			if(attachs !=null){
				/*case: published to other site only*/
				for (SignupAttachment attach : attachs) {
					this.attachmentHandler.determineAndAssignPublicView(sm, attach);
				}	
			}
			return sm.getSignupAttachments();
		}
		else{
			List<SignupAttachment> newOnes = new ArrayList<SignupAttachment>();
			List<SignupAttachment> olds = sm.getSignupAttachments();
			if (olds !=null){
				for (SignupAttachment old : olds) {
					SignupAttachment newOne = this.attachmentHandler.copySignupAttachment(sm, true,old,ATTACH_RECURRING + index);
					newOne.setTimeslotId(old.getTimeslotId());
					newOne.setViewByAll(old.getViewByAll());
					newOnes.add(newOne);
				}
			}
			return newOnes;
		}
	}

	/**
	 * It will save the SignupMeeting list into DB and send email to notify
	 * participants
	 * 
	 * @param signupMeetings
	 *            a SignupMeeting object.
	 * @throws PermissionException
	 *             a PermissionException object.
	 * @throws Exception
	 *             a Exception Object.
	 */
	private void postMeetings(List<SignupMeeting> signupMeetings) throws PermissionException, Exception

	{
		this.signupMeetingService.saveMeetings(signupMeetings, sakaiFacade.getCurrentUserId());
		/* refresh main-page to catch the changes */
		Utilities.resetMeetingList();

		/* they are identical and take the first one */
		SignupMeeting firstOne = signupMeetings.get(0);
		if (firstOne.isRecurredMeeting()) {
			Date lastRecurmeetingDate = signupMeetings.get(signupMeetings.size() - 1).getStartTime();
			firstOne.setRepeatUntil(lastRecurmeetingDate);
			firstOne.setApplyToAllRecurMeetings(assignParticitpantsToAllEvents);
		}

		if (sendEmail) {
			try {
				/* take the first one, which should not be null */
				signupMeetingService.sendEmail(firstOne, SIGNUP_NEW_MEETING);

			} catch (Exception e) {
				logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
				Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
			}
		}

		/* post Calendar */
		for (int i = 0; i < signupMeetings.size(); i++) {

			try {
				signupMeetingService.postToCalendar(signupMeetings.get(i));
			} catch (PermissionException pe) {
				Utilities
						.addErrorMessage(Utilities.rb.getString("error.calendarEvent.posted_failed_due_to_permission"));
				logger.info(Utilities.rb.getString("error.calendarEvent.posted_failed_due_to_permission")
						+ " - Meeting title:" + signupMeetings.get(i).getTitle());
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.posted_failed"));
				logger.info(Utilities.rb.getString("error.calendarEvent.posted_failed") + " - Meeting title:"
						+ signupMeetings.get(i).getTitle());
			}
		}

		/* post eventTracking info */
		String recurringInfo = firstOne.isRecurredMeeting() ? " recur_mtng" : "";
		for (int i = 0; i < signupMeetings.size(); i++) {
			logger.info(recurringInfo
					+ "title:"
					+ signupMeetings.get(i).getTitle()
					+ " - UserId:"
					+ sakaiFacade.getCurrentUserId()
					+ " - has created  new meeting(s) at meeting startTime:"
					+ sakaiFacade.getTimeService().newTime(signupMeetings.get(i).getStartTime().getTime())
							.toStringLocalFull());
			Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_MTNG_ADD, ToolManager.getCurrentPlacement()
					.getContext()
					+ " meetingId|title:"
					+ signupMeetings.get(i).getId()
					+ "|"
					+ signupMeetings.get(i).getTitle()
					+ " at startTime:"
					+ sakaiFacade.getTimeService().newTime(signupMeetings.get(i).getStartTime().getTime()).toStringLocalFull() 
					+ recurringInfo);
		}

	}

	/**
	 * It will deep copy the SignupMeeting object
	 * 
	 * @param s
	 *            a SignupMeeting object for copy
	 * @param addDaysForRecurringLength
	 *            number of days, which will be added to each time-slot due to recurrences
	 * @return a deep-copied SignupMeeting object.
	 */
	public SignupMeeting prepareDeepCopy(SignupMeeting s, int addDaysForRecurringLength) {

		List<SignupSite> copySites = s.getSignupSites();
		List<SignupSite> indivSite = new ArrayList<SignupSite>();

		for (int j = 0; j < copySites.size(); j++) {
			SignupSite ss = new SignupSite();
			ss.setSiteId(copySites.get(j).getSiteId());
			ss.setTitle(copySites.get(j).getTitle());

			List<SignupGroup> signupGroups = new ArrayList<SignupGroup>();
			if (copySites.get(j).getSignupGroups() != null) {
				for (int g = 0; g < copySites.get(j).getSignupGroups().size(); g++) {
					SignupGroup newGroup = new SignupGroup();
					newGroup.setGroupId(copySites.get(j).getSignupGroups().get(g).getGroupId());
					newGroup.setTitle(copySites.get(j).getSignupGroups().get(g).getTitle());
					signupGroups.add(newGroup);

				}

				ss.setSignupGroups(signupGroups);
			}

			indivSite.add(ss);
		}

		/* set up time slots, and for each time slot, attendee: */
		List<SignupTimeslot> origSlots = s.getSignupTimeSlots();
		List<SignupTimeslot> timeSlots = new ArrayList<SignupTimeslot>();
		if (origSlots != null && !origSlots.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			int sday, eday;
			for (int t = 0; t < origSlots.size(); t++) {

				SignupTimeslot slot = new SignupTimeslot();
				slot.setMaxNoOfAttendees(s.getMaxNumberOfAttendees());
				cal.setTime(origSlots.get(t).getStartTime());
				sday = cal.get(Calendar.DATE) + addDaysForRecurringLength;
				cal.set(Calendar.DATE, sday);				
				slot.setStartTime(cal.getTime());
				
				cal.setTime(origSlots.get(t).getEndTime());
				eday = cal.get(Calendar.DATE) + addDaysForRecurringLength;
				cal.set(Calendar.DATE, eday);	
				slot.setEndTime(cal.getTime());
				
				slot.setLocked(origSlots.get(t).isLocked());
				slot.setCanceled(origSlots.get(t).isCanceled());
				slot.setDisplayAttendees(origSlots.get(t).isDisplayAttendees());

				if (assignParticatpantsToFirstOne || assignParticitpantsToAllEvents) {
					/*
					 * copy attendees to all recurring events (not just the
					 * first one):
					 */
					List<SignupAttendee> attendees = new ArrayList<SignupAttendee>();
					for (int a = 0; a < origSlots.get(t).getAttendees().size(); a++) {
						SignupAttendee newAttendee = new SignupAttendee();
						newAttendee.setAttendeeUserId(origSlots.get(t).getAttendees().get(a).getAttendeeUserId());
						newAttendee.setSignupSiteId(origSlots.get(t).getAttendees().get(a).getSignupSiteId());
						attendees.add(newAttendee);
					}

					slot.setAttendees(attendees);
				}
				timeSlots.add(slot);

			}
		}

		SignupMeeting copy = new SignupMeeting();
		copy.setTitle(s.getTitle());
		copy.setLocation(s.getLocation());
		copy.setDescription(s.getDescription());
		copy.setCreatorUserId(s.getCreatorUserId());
		copy.setStartTime(s.getStartTime());
		copy.setEndTime(s.getEndTime());
		copy.setMeetingType(s.getMeetingType());
		copy.setSignupTimeSlots(timeSlots);
		copy.setSignupSites(indivSite); // copy sites
		copy.setCreatorUserId(sakaiFacade.getCurrentUserId());
		Date sBegin = Utilities.subTractTimeToDate(s.getStartTime(), getSignupBegins(), getSignupBeginsType());
		Date sDeadline = Utilities.subTractTimeToDate(s.getEndTime(), getDeadlineTime(), getDeadlineTimeType());
		copy.setSignupBegins(sBegin);
		copy.setSignupDeadline(sDeadline);
		copy.setRepeatType(s.getRepeatType());
		copy.setRepeatUntil(s.getRepeatUntil());
		copy.setReceiveEmailByOwner(s.isReceiveEmailByOwner());
		copy.setAllowWaitList(s.isAllowWaitList());
		copy.setAllowComment(s.isAllowComment());
		copy.setAutoReminder(s.isAutoReminder());
		copy.setEidInputMode(s.isEidInputMode());

		return copy;

	}

	public String getSignupBeginsType() {
		return signupBeginsType;
	}

	public void setSignupBeginsType(String signupBeginsType) {
		this.signupBeginsType = signupBeginsType;
	}

	public int getSignupBegins() {
		return signupBegins;
	}

	public void setSignupBegins(int signupBegins) {
		this.signupBegins = signupBegins;
	}

	public String getDeadlineTimeType() {
		return deadlineTimeType;
	}

	public void setDeadlineTimeType(String deadlineTimeType) {
		this.deadlineTimeType = deadlineTimeType;
	}

	public int getDeadlineTime() {
		return deadlineTime;
	}

	public void setDeadlineTime(int deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

}
