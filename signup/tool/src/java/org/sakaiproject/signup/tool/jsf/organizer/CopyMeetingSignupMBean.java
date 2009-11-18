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
package org.sakaiproject.signup.tool.jsf.organizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupUser;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupSiteWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateMeetings;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateSitesGroups;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * copy meeting page:<b>copyMeeting.jsp</b> and backbone system.
 * </P>
 */
public class CopyMeetingSignupMBean extends SignupUIBaseBean {

	private SignupMeeting signupMeeting;

	private boolean keepAttendees;

	private int maxNumOfAttendees;

	private boolean unlimited;

	private String signupBeginsType;

	/* singup can start before this minutes/hours/days */
	private int signupBegins;

	private String deadlineTimeType;

	/* singup deadline before this minutes/hours/days */
	private int deadlineTime;

	private Date repeatUntil;

	private String repeatType;

	private int timeSlotDuration;

	private int numberOfSlots;

	private boolean showAttendeeName;

	private boolean truncateAttendee;

	private SignupSiteWrapper currentSite;

	private List<SignupSiteWrapper> otherSites;

	private List<SignupUser> allowedUserList;

	private boolean missingSitGroupWarning;

	private List<String> missingSites;

	private List<String> missingGroups;

	private boolean assignParicitpantsToAllRecurEvents;

	private boolean validationError;
	
	private boolean repeatTypeUnknown=true;

	private List<SelectItem> meetingTypeRadioBttns;

	/**
	 * this reset information which contains in this UIBean lived in a session
	 * scope
	 * 
	 */
	public void reset() {
		unlimited = false;
		keepAttendees = false;
		assignParicitpantsToAllRecurEvents = false;
		sendEmail = DEFAULT_SEND_EMAIL;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		repeatUntil = calendar.getTime();
		repeatType = ONCE_ONLY;
		repeatTypeUnknown=true;
		showAttendeeName = false;
		truncateAttendee = false;
		missingSitGroupWarning = false;
		
		/*cleanup previously unused attachments in CHS*/
		if(this.signupMeeting !=null)
			cleanUpUnusedAttachmentCopies(this.signupMeeting.getSignupAttachments());

		this.signupMeeting = signupMeetingService.loadSignupMeeting(meetingWrapper.getMeeting().getId(), sakaiFacade
				.getCurrentUserId(), sakaiFacade.getCurrentLocationId());
		
		/*prepare new attachments*/		
		assignMainAttachmentsCopyToSignupMeeting();
		//TODO not consider copy time slot attachment yet

		List<SignupTimeslot> signupTimeSlots = signupMeeting.getSignupTimeSlots();

		if (signupTimeSlots != null && !signupTimeSlots.isEmpty()) {
			SignupTimeslot ts = (SignupTimeslot) signupTimeSlots.get(0);
			maxNumOfAttendees = ts.getMaxNoOfAttendees();
			this.unlimited = ts.isUnlimitedAttendee();
			showAttendeeName = ts.isDisplayAttendees();
			this.numberOfSlots = signupTimeSlots.size();

		} else {// announcement meeting type
			setNumberOfSlots(1);

		}

		populateDataForBeginDeadline(this.signupMeeting);
		
		/*Case: recurrence events*/
		prepareRecurredEvents();

		/* Initialize site/groups for current organizer */
		initializeSitesGroups();
		
	}

	/* process the relative time for Signup begin/deadline */
	private void populateDataForBeginDeadline(SignupMeeting sMeeting) {
		long signupBeginsTime = sMeeting.getSignupBegins() == null ? new Date().getTime() : sMeeting.getSignupBegins()
				.getTime();
		long signupDeadline = sMeeting.getSignupDeadline() == null ? new Date().getTime() : sMeeting
				.getSignupDeadline().getTime();

		/* get signup begin & deadline relative time in minutes */
		long signupBeginBeforMeeting = (sMeeting.getStartTime().getTime() - signupBeginsTime) / MINUTE_IN_MILLISEC;
		long signupDeadLineBeforMeetingEnd = (sMeeting.getEndTime().getTime() - signupDeadline) / MINUTE_IN_MILLISEC;

		this.signupBeginsType = Utilities.getTimeScaleType(signupBeginBeforMeeting);
		this.signupBegins = Utilities.getRelativeTimeValue(signupBeginsType, signupBeginBeforMeeting);

		this.deadlineTimeType = Utilities.getTimeScaleType(signupDeadLineBeforMeetingEnd);
		this.deadlineTime = Utilities.getRelativeTimeValue(deadlineTimeType, signupDeadLineBeforMeetingEnd);

	}

	/**
	 * Just to overwrite the parent one
	 */
	public SignupMeetingWrapper getMeetingWrapper() {
		return meetingWrapper;
	}

	/**
	 * This is a JSF action call method by UI to copy the event/meeting into a
	 * new one
	 * 
	 * @return an action outcome string
	 */
	// TODO: what to do if timeslot is locked or canceled
	public String processSaveCopy() {
		if (validationError) {
			validationError = false;
			return "";
		}

		SignupMeeting sMeeting = getSignupMeeting();
		try {
			prepareCopy(sMeeting);

			sMeeting.setRepeatUntil(getRepeatUntil());
			sMeeting.setRepeatType(getRepeatType());

			CreateMeetings createMeeting = new CreateMeetings(sMeeting, sendEmail, keepAttendees
					&& !assignParicitpantsToAllRecurEvents, keepAttendees && assignParicitpantsToAllRecurEvents,
					getSignupBegins(), getSignupBeginsType(), getDeadlineTime(), getDeadlineTimeType(), sakaiFacade,
					signupMeetingService, getAttachmentHandler(), sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), true);

			createMeeting.processSaveMeetings();
			
			/*make sure that they don't get cleaned up in CHS when saved successfully*/
			this.signupMeeting.getSignupAttachments().clear();

		} catch (PermissionException e) {
			logger.info(Utilities.rb.getString("no.permission_create_event") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("no.permission_create_event"));
			return ORGANIZER_MEETING_PAGE_URL;
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
			return "";
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
			return ORGANIZER_MEETING_PAGE_URL;
		}

		return MAIN_EVENTS_LIST_PAGE_URL;
	}

	/**
	 * This is a validator to make sure that the event/meeting starting time is
	 * before ending time etc.
	 * 
	 * @param e
	 *            an ActionEvent object.
	 */
	public void validateCopyMeeting(ActionEvent e) {
		Date endTime = signupMeeting.getEndTime();
		Date startTime = signupMeeting.getStartTime();
		if (endTime.before(startTime) || startTime.equals(endTime)) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("event.endTime_should_after_startTime"));
			return;
		}

		if (!(getRepeatType().equals(ONCE_ONLY))) {
			int repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), signupMeeting.getStartTime(),
					getRepeatUntil());
			if (DAILY.equals(getRepeatType()) && isMeetingLengthOver24Hours(this.signupMeeting)) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.daily.problem"));
				return;
			}

			if (repeatNum < 1) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("event.repeatbeforestart"));
				return;
			}
		}

		if (!CreateSitesGroups.isAtleastASiteOrGroupSelected(this.getCurrentSite(), this.getOtherSites())) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("select.atleast.oneGroup.for.copyMeeting"));

		}
	}
	
	/**
	 * This method is called by JSP page for adding/removing attachments action.
	 * @return null.
	 */
	public String addRemoveAttachments(){
		getAttachmentHandler().processAddAttachRedirect(this.signupMeeting.getSignupAttachments(),null,true);
		return null;
	}
	
	public String doCancelAction(){
		cleanUpUnusedAttachmentCopies(this.signupMeeting.getSignupAttachments());
		return ORGANIZER_MEETING_PAGE_URL;
	}

	/**
	 * This is a ValueChange Listener to watch changes on the selection of
	 * 'unlimited attendee' choice by user.
	 * 
	 * @param vce
	 *            a ValuechangeEvent object.
	 * @return a outcome string.
	 */
	public String processGroup(ValueChangeEvent vce) {
		Boolean changeValue = (Boolean) vce.getNewValue();
		if (changeValue != null) {
			unlimited = changeValue.booleanValue();
			if (unlimited)
				maxNumOfAttendees = 10;

		}

		return "";

	}

	private void prepareCopy(SignupMeeting meeting) throws Exception {

		meeting.setId(null);// to save as new meeting in db
		meeting.setRecurrenceId(null);

		meeting.setSignupSites(CreateSitesGroups.getSelectedSignupSites(getCurrentSite(), getOtherSites()));

		this.allowedUserList = LoadAllowedUsers(meeting);

		List<SignupTimeslot> timeslots = meeting.getSignupTimeSlots();

		boolean lockOrCanceledTimeslot = false;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(meeting.getStartTime());

		/* Announcement type */
		if (getAnnouncementType() || timeslots == null || timeslots.isEmpty()) {
			calendar.add(Calendar.MINUTE, getTimeSlotDuration());
			meeting.setMeetingType(ANNOUNCEMENT);
			meeting.setSignupTimeSlots(null);
		} else {
			if (meeting.getMeetingType().equals(INDIVIDUAL) || meeting.getMeetingType().equals(GROUP)) {
				List<SignupTimeslot> origTsList = meeting.getSignupTimeSlots();

				SignupTimeslot origTs = null;
				List<SignupTimeslot> cpTimeslotList = new ArrayList<SignupTimeslot>();
				for (int i = 0; i < getNumberOfSlots(); i++) {
					SignupTimeslot cpTs = new SignupTimeslot();
					int maxAttendees = (unlimited) ? SignupTimeslot.UNLIMITED : maxNumOfAttendees;
					cpTs.setMaxNoOfAttendees(maxAttendees);
					cpTs.setDisplayAttendees(showAttendeeName);
					cpTs.setStartTime(calendar.getTime());
					calendar.add(Calendar.MINUTE, getTimeSlotDuration());
					cpTs.setEndTime(calendar.getTime());

					/* pass attendees */
					if (i < origTsList.size()) {
						origTs = origTsList.get(i);
						List<SignupAttendee> attList = origTs.getAttendees();
						/* screening attendees */
						removeNotAllowedAttedees(attList);

						if (!unlimited && attList != null && attList.size() > maxAttendees) {
							/* attendee may be truncated */
							this.truncateAttendee = true;
							for (int j = attList.size(); j > maxAttendees; j--)
								attList.remove(j - 1);
						}
						cpTs.setAttendees(attList);
						origTs.setAttendees(null);// cleanup,may not necessary
						cpTs.setLocked(origTs.isLocked());
						cpTs.setCanceled(origTs.isCanceled());
						if (origTs.isCanceled() || origTs.isLocked())
							lockOrCanceledTimeslot = true;

					}
					cpTimeslotList.add(cpTs);
				}

				meeting.setSignupTimeSlots(cpTimeslotList);// pass over

				if (lockOrCanceledTimeslot)
					Utilities.addMessage(Utilities.rb.getString("warning.some_timeslot_may_locked_canceled"));

				if (origTsList.size() < getNumberOfSlots()
						|| origTsList.get(0).getMaxNoOfAttendees() < getMaxNumOfAttendees()) {
					this.truncateAttendee = true;// attendee may be truncated
				}
			}

		}

		meeting.setEndTime(calendar.getTime());

		/* setup signup begin / deadline */
		setSignupBeginDeadlineData(meeting, getSignupBegins(), getSignupBeginsType(), getDeadlineTime(),
				getDeadlineTimeType());

		// copySites(meeting);

	}

	/**
	 * check if the attendees in the event/meeting should be copied along with
	 * it
	 * 
	 * @return true if the attendees in the event/meeting is copied along with
	 *         it
	 */
	public boolean isKeepAttendees() {
		return keepAttendees;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param keepAttendees
	 */
	public void setKeepAttendees(boolean keepAttendees) {
		this.keepAttendees = keepAttendees;
	}

	/**
	 * this is a getter method
	 * 
	 * @return an integer number
	 */
	public int getMaxNumOfAttendees() {
		return maxNumOfAttendees;
	}

	/**
	 * this is a setter
	 * 
	 * @param maxNumOfAttendees
	 *            an integer number
	 */
	public void setMaxNumOfAttendees(int maxNumOfAttendees) {
		this.maxNumOfAttendees = maxNumOfAttendees;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return a SignupMeeting object
	 */
	public SignupMeeting getSignupMeeting() {
		return signupMeeting;
	}

	/**
	 * this is a setter
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object
	 */
	public void setSignupMeeting(SignupMeeting signupMeeting) {
		this.signupMeeting = signupMeeting;
	}

	/**
	 * check to see if the attendees are limited in the event/meeting
	 * 
	 * @return true if the attendees are limited in the event/meeting
	 */
	public boolean isUnlimited() {
		return unlimited;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param unlimited
	 *            a boolean value
	 */
	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	/**
	 * this is a getter method to provide a relative time
	 * 
	 * @return am integer number
	 */
	public int getDeadlineTime() {
		return deadlineTime;
	}

	/**
	 * this is a setter
	 * 
	 * @param deadlineTime
	 *            an integer number, which represents a relative time to meeting
	 *            starting time
	 */
	public void setDeadlineTime(int deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return a string value
	 */
	public String getDeadlineTimeType() {
		return deadlineTimeType;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param deadlineTimeType
	 *            an integer number
	 */
	public void setDeadlineTimeType(String deadlineTimeType) {
		this.deadlineTimeType = deadlineTimeType;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return an integer number
	 */
	public int getSignupBegins() {
		return signupBegins;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param signupBegins
	 *            an integer number
	 */
	public void setSignupBegins(int signupBegins) {
		this.signupBegins = signupBegins;
	}

	/**
	 * this is a getter method for UI
	 * 
	 * @return an integer number
	 */
	public String getSignupBeginsType() {
		return signupBeginsType;
	}

	/**
	 * this is a setter for UI
	 * 
	 * @param signupBeginsType
	 *            an integer number
	 */
	public void setSignupBeginsType(String signupBeginsType) {
		this.signupBeginsType = signupBeginsType;
	}

	public Date getRepeatUntil() {
		return repeatUntil;
	}

	public void setRepeatUntil(Date repeatUntil) {
		this.repeatUntil = repeatUntil;
	}

	public String getRepeatType() {
		return repeatType;
	}

	public void setRepeatType(String repeatType) {
		this.repeatType = repeatType;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a HtmlInputHidden object.
	 */
	public int getTimeSlotDuration() {
		long duration = (getSignupMeeting().getEndTime().getTime() - getSignupMeeting().getStartTime().getTime())
				/ (MINUTE_IN_MILLISEC * getNumberOfSlots());
		return (int) duration;
	}

	public void setTimeSlotDuration(int timeSlotDuration) {
		this.timeSlotDuration = timeSlotDuration;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a HtmlInputHidden object.
	 */
	public int getNumberOfSlots() {
		return numberOfSlots;
	}

	/**
	 * This is a setter method for UI.
	 * 
	 * @param numberOfSlots
	 *            an int value
	 */
	public void setNumberOfSlots(int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isTruncateAttendee() {
		return truncateAttendee;
	}

	public void setTruncateAttendee(boolean truncateAttendee) {
		this.truncateAttendee = truncateAttendee;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of SignupSiteWrapper objects.
	 */
	public List<SignupSiteWrapper> getOtherSites() {
		return otherSites;
	}

	/**
	 * This is a setter method for UI.
	 * 
	 * @param signupSiteWrapperList
	 *            a list of SignupSiteWrapper object.
	 */
	public void setOtherSites(List<SignupSiteWrapper> signupSiteWrapperList) {
		this.otherSites = signupSiteWrapperList;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a SignupSiteWrapper object.
	 */
	public SignupSiteWrapper getCurrentSite() {
		return currentSite;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param currentSite
	 *            a SignupSiteWrapper object.
	 */
	public void setCurrentSite(SignupSiteWrapper currentSite) {
		this.currentSite = currentSite;
	}

	private void initializeSitesGroups() {
		/*
		 * Temporary bug fix for AuthZ code ( isAllowed(..) ), which gives wrong
		 * permission for the first time at 'Create new or Copy meeting pages'.
		 * The bug will be gone by second time go into it. Once it's fixed,
		 * remove this below and other places and make it into a more clean way
		 * by not sharing the same CreateSitesGroups Object. new
		 * CreateSitesGroups(getSignupMeeting(),sakaiFacade,signupMeetingService);
		 */
		CreateSitesGroups createSiteGroups = Utilities.getSignupMeetingsBean().getCreateSitesGroups();
		createSiteGroups.resetSiteGroupCheckboxMark();
		createSiteGroups.setSignupMeeting(this.getSignupMeeting());
		createSiteGroups.processSiteGroupSelectionMarks();
		setCurrentSite(createSiteGroups.getCurrentSite());
		setOtherSites(createSiteGroups.getOtherSites());
		setMissingSitGroupWarning(createSiteGroups.isSiteOrGroupTruncated());
		setMissingSites(createSiteGroups.getMissingSites());
		setMissingGroups(createSiteGroups.getMissingGroups());
	}

	private List<SignupUser> LoadAllowedUsers(SignupMeeting meeting) {
		return sakaiFacade.getAllUsers(getSignupMeeting());
	}

	private void removeNotAllowedAttedees(List<SignupAttendee> screenAttendeeList) {
		if (screenAttendeeList == null || screenAttendeeList.isEmpty())
			return;

		boolean notFound = true;
		for (int i = screenAttendeeList.size(); i > 0; i--) {
			notFound = true;
			for (SignupUser allowedOne : allowedUserList) {
				if (allowedOne.getInternalUserId().equals(screenAttendeeList.get(i - 1).getAttendeeUserId())) {
					notFound = false;
					break;
				}
			}
			if (notFound) {
				screenAttendeeList.remove(i - 1);
			}
		}
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isMissingSitGroupWarning() {
		return missingSitGroupWarning;
	}

	private void setMissingSitGroupWarning(boolean missingSitGroupWarning) {
		this.missingSitGroupWarning = missingSitGroupWarning;
	}

	public List<String> getMissingSites() {
		return missingSites;
	}

	private void setMissingSites(List<String> missingSites) {
		this.missingSites = missingSites;
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isMissingSitesThere() {
		if (this.missingSites == null || this.missingSites.isEmpty())
			return false;
		return true;
	}

	public List<String> getMissingGroups() {
		return missingGroups;
	}

	private void setMissingGroups(List<String> missingGroups) {
		this.missingGroups = missingGroups;
	}

	public boolean isMissingGroupsThere() {
		if (this.missingGroups == null || this.missingGroups.isEmpty())
			return false;
		return true;
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isAssignParicitpantsToAllRecurEvents() {
		return assignParicitpantsToAllRecurEvents;
	}

	/**
	 * It's a setter for UI
	 * 
	 * @param assignParicitpantsToAllRecurEvents
	 *            a boolean value
	 */
	public void setAssignParicitpantsToAllRecurEvents(boolean assignParicitpantsToAllRecurEvents) {
		this.assignParicitpantsToAllRecurEvents = assignParicitpantsToAllRecurEvents;
	}

	/**
	 * It's a getter method for UI
	 * 
	 * @return a list of SelectItem objects for radio buttons.
	 */
	public List<SelectItem> getMeetingTypeRadioBttns() {
		this.meetingTypeRadioBttns = Utilities.getMeetingTypeSelectItems(getSignupMeeting().getMeetingType(), true);
		return meetingTypeRadioBttns;
	}
	
	private void prepareRecurredEvents(){
		Long recurrenceId = this.signupMeeting.getRecurrenceId();
		if (recurrenceId != null && recurrenceId.longValue() > 0 ) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(this.signupMeeting.getStartTime());
			/*backward to one month and make sure we could get some recurrence events 
			 * if it's not the only one existed
			 * */
			cal.add(Calendar.HOUR,-24*31);
			List<SignupMeeting> recurredMeetings = signupMeetingService.getRecurringSignupMeetings(getSakaiFacade().getCurrentLocationId(), getSakaiFacade().getCurrentUserId(), recurrenceId,
					cal.getTime());
			retrieveRecurrenceData(recurredMeetings);
		}
	}
	
	/*This method only provide a most possible repeatType, not with 100% accuracy*/
	private void retrieveRecurrenceData(List<SignupMeeting> upTodateOrginMeetings) {
		Date lastDate=new Date();
		if (upTodateOrginMeetings == null || upTodateOrginMeetings.isEmpty())
			return;
		
		/*if this is the last one*/
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.signupMeeting.getStartTime());
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		setRepeatUntil(cal.getTime());
		
		int listSize = upTodateOrginMeetings.size();
		if (listSize > 1) {
			/*set last recurred Date for recurrence events*/
			lastDate = upTodateOrginMeetings.get(listSize -1).getStartTime();			
			cal.setTime(lastDate);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			setRepeatUntil(cal.getTime());
			
			String repeatType = upTodateOrginMeetings.get(listSize -1).getRepeatType();
			if(repeatType !=null && !ONCE_ONLY.equals(repeatType)){
				setRepeatType(repeatType);
				setRepeatTypeUnknown(false);
				return;
			}
			
			/*The following code is to make it old version backward compatible
			 * It will be cleaned after a while.
			 */
			Calendar calFirst = Calendar.getInstance();
			Calendar calSecond = Calendar.getInstance();							
			/*The following code is to make it old version backward compatible*/
			
			/*
			 * we can only get approximate estimation by assuming it's a
			 * succession. take the last two which are more likely be in a sequence
			 */
			calFirst.setTime(upTodateOrginMeetings.get(listSize - 2).getStartTime());
			calFirst.set(Calendar.SECOND, 0);
			calFirst.set(Calendar.MILLISECOND, 0);
			calSecond.setTime(upTodateOrginMeetings.get(listSize - 1).getStartTime());
			calSecond.set(Calendar.SECOND, 0);
			calSecond.set(Calendar.MILLISECOND, 0);
			int tmp = calSecond.get(Calendar.DATE);
			int daysDiff = (int) (calSecond.getTimeInMillis() - calFirst.getTimeInMillis()) / DAY_IN_MILLISEC;
			setRepeatTypeUnknown(false);
			if (daysDiff == perDay)//could have weekdays get into this one, not very accurate.
				setRepeatType(DAILY);
			else if (daysDiff == perWeek)
				setRepeatType(WEEKLY);
			else if (daysDiff == perBiweek)
				setRepeatType(BIWEEKLY);
			else if(daysDiff ==3 && calFirst.get(Calendar.DAY_OF_WEEK)== Calendar.FRIDAY)
				setRepeatType(WEEKDAYS);
			else{
				/*case:unknown repeatType*/
				setRepeatTypeUnknown(true);
			}
		}
	}
		
	/**
	 * This is a getter for UI and it is used for controlling the 
	 * recurring meeting warning message.
	 * @return true if the repeatType is unknown for a repeatable event.
	 */
	public boolean getRepeatTypeUnknown() {
		return repeatTypeUnknown;
	}

	public void setRepeatTypeUnknown(boolean repeatTypeUnknown) {
		this.repeatTypeUnknown = repeatTypeUnknown;
	}

	private void assignMainAttachmentsCopyToSignupMeeting(){
		List<SignupAttachment> attachList = new ArrayList<SignupAttachment>();
		if(attachList != null){
			for (SignupAttachment attach: this.signupMeeting.getSignupAttachments()) {
				if(attach.getTimeslotId() ==null && attach.getViewByAll())
					attachList.add(attach);
				
				//TODO Later: how about time slot attachment?.
			}
		}
				
		List<SignupAttachment> cpList = new ArrayList<SignupAttachment>();
		if(attachList.size() > 0){			
			for (SignupAttachment attach : attachList) {
				cpList.add(getAttachmentHandler().copySignupAttachment(this.signupMeeting,true,attach,ATTACH_COPY +this.signupMeeting.getId().toString()));
			}
		}
		
		this.signupMeeting.setSignupAttachments(cpList);
	}
	
	/*Overwrite default one*/
	public boolean getSignupAttachmentEmpty(){
		if(this.signupMeeting ==null)
			return true;
		
		if(this.signupMeeting.getSignupAttachments() ==null || this.signupMeeting.getSignupAttachments().isEmpty())
			return true;
		else
			return false;
	}
}
