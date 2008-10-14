/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Yale University
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
package org.sakaiproject.signup.tool.jsf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.signup.logic.SignupUser;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.signup.util.SignupDateFormat;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <p>
 * This JSF UIBean class will handle the creation of different types of
 * event/meeting by Organizer It provides all the necessary business logic for
 * this process.
 * </P>
 */
public class NewSignupMeetingBean implements MeetingTypes, SignupMessageTypes, SignupBeanConstants {

	private SignupMeetingService signupMeetingService;

	private SignupMeeting signupMeeting;

	private SakaiFacade sakaiFacade;

	private HtmlInputHidden currentStepHiddenInfo;

	private boolean unlimited;

	private int numberOfSlots;

	private int numberOfAttendees;

	private int maxOfAttendees;

	private int timeSlotDuration;

	private boolean recurrence;

	private String signupBeginsType;

	/* singup can start before this minutes/hours/days */
	private int signupBegins;

	private String deadlineTimeType;

	/* singup deadline before this minutes/hours/days */
	private int deadlineTime;

	private SignupSiteWrapper currentSite;

	private List<SignupSiteWrapper> otherSites;

	private static boolean DEFAULT_SEND_EMAIL= "true".equalsIgnoreCase(Utilities.rb.getString("default.email.notification"))? true : false;

	private boolean sendEmail = DEFAULT_SEND_EMAIL;

	private boolean receiveEmail;

	private List<TimeslotWrapper> timeSlotWrappers;

	private List<SelectItem> allAttendees;

	private UIInput newAttendeeInput;

	/* proxy param */
	private String eidInputByUser;

	private UIData timeslotWrapperTable;

	private SignupMeetingsBean signupMeetingsBean;

	private boolean showParticipants;

	private boolean validationError;

	private boolean eidInputMode = false;

	private Log logger = LogFactory.getLog(getClass());

	/* used for jsf parameter passing */
	private final static String PARAM_NAME_FOR_ATTENDEE_USERID = "attendeeUserId";

	public void setSignupMeetingsBean(SignupMeetingsBean signupMeetingsBean) {
		this.signupMeetingsBean = signupMeetingsBean;
	}

	public String getCurrentUserDisplayName() {
		return sakaiFacade.getUserDisplayName(sakaiFacade.getCurrentUserId());
	}

	/**
	 * The default Constructor. It will initialize all the required variables.
	 * 
	 */
	public NewSignupMeetingBean() {
		init();
	}

	/** Initialize all the default setting for creating new events. */
	private void init() {
		signupMeeting = new SignupMeeting();
		signupMeeting.setMeetingType(INDIVIDUAL);

		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		signupMeeting.setStartTime(calendar.getTime());
		signupMeeting.setEndTime(calendar.getTime());
		unlimited = false;
		recurrence = false;
		numberOfSlots = 4;
		numberOfAttendees = 1;
		maxOfAttendees = 10;
		timeSlotDuration = 0; // minutes
		signupBegins = 6;
		deadlineTime = 1;
		signupBeginsType = Utilities.DAYS;
		deadlineTimeType = Utilities.HOURS;
		validationError = false;
		sendEmail = DEFAULT_SEND_EMAIL;
		receiveEmail = false;
		currentStepHiddenInfo = null;
		eidInputMode = false;
	}

	private void reset() {
		init();
		signupBeginsType = Utilities.DAYS;
		deadlineTimeType = Utilities.HOURS;
		signupBegins = 6;
		deadlineTime = 1;
		timeSlotWrappers = null;
		currentSite = null;
		otherSites = null;
		/* for main meetingpage */
		this.signupMeetingsBean.setSignupMeetings(null);
		this.eidInputByUser = null;
	}

	/**
	 * This is a JSF action call method by UI to navigate to the next page.
	 * 
	 * @return an action outcome string.
	 */
	public String goNext() {
		if (validationError) {
			validationError = false;
			return "";
		}

		String step = (String) currentStepHiddenInfo.getValue();
		if (step.equals("step1") && !isRecurrence()) {
			/*
			 * let recalculate the duration just in case of meeting endTime
			 * changes
			 */
			setTimeSlotDuration(0);
			return ADD_MEETING_STEP3_PAGE_URL;
		}

		if (step.equals("step1") && isRecurrence())
			return ADD_MEETING_STEP2_PAGE_URL;

		if (step.equals("step2"))
			return ADD_MEETING_STEP3_PAGE_URL;

		if (step.equals("step3"))
			return ADD_MEETING_STEP4_PAGE_URL;

		if (step.equals("step4")) {
			return ADD_MEETING_STEP5_PAGE_URL;
		}

		return "";
	}

	/**
	 * This is a validator to make sure that the event/meeting starting time is
	 * before ending time.
	 * 
	 * @param e
	 *            an ActionEvent object.
	 */
	public void validateNewMeeting(ActionEvent e) {
		String step = (String) currentStepHiddenInfo.getValue();
		if (step.equals("step1")) {
			Date endTime = signupMeeting.getEndTime();
			Date startTime = signupMeeting.getStartTime();
			if (endTime.before(startTime)) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("event.endTime_should_after_startTime"));
				return;
			}
			warnMeetingAccrossTwoDates(endTime, startTime);

		}
		if (step.equals("step4")) {
			if (!isAtleastASiteOrGroupSelected(this.getCurrentSite(), this.getOtherSites())) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("select.atleast.oneGroup"));
			}

		}
	}

	private void warnMeetingAccrossTwoDates(Date endTime, Date startTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startTime);
		int startYear = calendar.get(Calendar.YEAR);
		int startMonth = calendar.get(Calendar.MONTH);
		int startDay = calendar.get(Calendar.DATE);

		calendar.setTime(endTime);
		int endYear = calendar.get(Calendar.YEAR);
		int endMonth = calendar.get(Calendar.MONTH);
		int endDay = calendar.get(Calendar.DATE);
		if (startDay != endDay)
			Utilities.addMessage(Utilities.rb.getString("warning.event.crossed_twoDays"));
		if (startMonth != endMonth)
			Utilities.addMessage(Utilities.rb.getString("warning.event.crossed_twoMonths"));
		if (startYear != endYear)
			Utilities.addMessage(Utilities.rb.getString("warning.event.crossed_twoYears"));
	}

	/* make sure that one site or group is selected. */
	private boolean isAtleastASiteOrGroupSelected(SignupSiteWrapper currentSite, List<SignupSiteWrapper> otherSites) {
		if (currentSite.isSelected())
			return true;
		List<SignupGroupWrapper> currentGroupsW = currentSite.getSignupGroupWrappers();
		for (SignupGroupWrapper wrapper : currentGroupsW) {
			if (wrapper.isSelected())
				return true;
		}

		for (SignupSiteWrapper siteW : otherSites) {
			if (siteW.isSelected())
				return true;
			List<SignupGroupWrapper> otherGroupsW = siteW.getSignupGroupWrappers();
			for (SignupGroupWrapper groupW : otherGroupsW) {
				if (groupW.isSelected())
					return true;
			}
		}
		return false;
	}

	/**
	 * This is a JSF action call method by UI to let user navigate one page
	 * back.
	 * 
	 * @return an action outcome string.
	 */
	public String goBack() {
		String step = (String) currentStepHiddenInfo.getValue();
		if (step.equals("step2"))
			return ADD_MEETING_STEP1_PAGE_URL;

		if (step.equals("step3") && isRecurrence())
			return ADD_MEETING_STEP2_PAGE_URL;

		if (step.equals("step3") && !isRecurrence())
			return ADD_MEETING_STEP1_PAGE_URL;

		if (step.equals("step4"))
			return ADD_MEETING_STEP3_PAGE_URL;

		if (step.equals("step5"))
			return ADD_MEETING_STEP4_PAGE_URL;

		if (step.equals("assignAttendee")) {
			timeSlotWrappers = null; // reset to remove timeslots info with
			// attendees
			return ADD_MEETING_STEP5_PAGE_URL;
		}

		return "";
	}

	/**
	 * This is a JSF action call method by UI to let user cancel the action.
	 * 
	 * @return an action outcome string.
	 */
	public String processCancel() {
		reset();
		return CANCEL_ADD_MEETING_PAGE_URL;
	}

	/**
	 * This is a ValueChange Listener to watch the meeting type selection by
	 * user.
	 * 
	 * @param vce
	 *            a ValuechangeEvent object.
	 * @return a outcome string.
	 */
	public String processSelectedType(ValueChangeEvent vce) {
		String newMeetingType = (String) vce.getNewValue();
		signupMeeting.setMeetingType(newMeetingType);

		return "";

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
		}
		return "";

	}

	/**
	 * This is a JSF action call method by UI to let user to save and create a
	 * new event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String processSave() {
		preSaveAction();
		try {
			signupMeetingService.saveMeeting(signupMeeting, sakaiFacade.getCurrentUserId());

			logger.info("Meeting Name:"
					+ signupMeeting.getTitle()
					+ " - UserId:"
					+ sakaiFacade.getCurrentUserId()
					+ " - has created a new meeting at meeting startTime:"
					+ getSakaiFacade().getTimeService().newTime(signupMeeting.getStartTime().getTime())
							.toStringLocalFull());

			/*
			 * reset the meetings in the SignupMeetingsBean to null so we will
			 * fetch all the meeting again
			 */
			this.signupMeetingsBean.setSignupMeetings(null);

			if (sendEmail) {
				try {
					signupMeetingService.sendEmail(signupMeeting, SIGNUP_NEW_MEETING);
				} catch (Exception e) {
					logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}

			try {
				signupMeetingService.postToCalendar(signupMeeting);
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.posted_failed"));
				logger.info(Utilities.rb.getString("error.calendarEvent.posted_failed") + " - " + e.getMessage());
			}

			reset();
		} catch (PermissionException e) {
			logger.info(Utilities.rb.getString("no.permission_create_event") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("no.permission_create_event"));
		} catch (Exception e) {
			logger.info(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		return MAIN_EVENTS_LIST_PAGE_URL;
	}

	/* Prepare the data for saving action */
	private void preSaveAction() {
		SignupSite sSite = new SignupSite();
		String currentLocationId = sakaiFacade.getCurrentLocationId();
		sSite.setSiteId(currentLocationId);
		sSite.setTitle(sakaiFacade.getLocationTitle(currentLocationId));
		List<SignupSite> signupSites = new ArrayList<SignupSite>();
		signupSites.add(sSite);
		signupMeeting.setSignupSites(signupSites);
		List<SignupTimeslot> slots = timeslots();
		signupMeeting.setSignupTimeSlots(slots);

		Date sBegin = Utilities.subTractTimeToDate(signupMeeting.getStartTime(), getSignupBegins(),
				getSignupBeginsType());
		Date sDeadline = Utilities.subTractTimeToDate(signupMeeting.getEndTime(), getDeadlineTime(),
				getDeadlineTimeType());

		// TODO need a better way to warn people?
		/*
		 * if (sBegin.before(new Date())) { // a warning for user
		 * Utilities.addErrorMessage(Utilities.rb.getString("warning.your.event.singup.begin.time.passed.today.time")); }
		 */
		signupMeeting.setSignupBegins(sBegin);
		// TODO need validate and handle error for case: deadline is before
		// sBegin
		signupMeeting.setSignupDeadline(sDeadline);

		signupMeeting.setSignupSites(getSelectedSignupSites());

		signupMeeting.setCreatorUserId(sakaiFacade.getCurrentUserId());
		signupMeeting.setReceiveEmailByOwner(receiveEmail);
	}

	/**
	 * This is a JSF action call method by UI to let user to go to next page,
	 * which will allow user to pre-assign the attendees into the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String proceesPreAssignAttendee() {
		preSaveAction();
		loadAllAttendees(this.getSignupMeeting());
		return PRE_ASSIGN_ATTENDEE_PAGE_URL;
	}

	/**
	 * This is a JSF action call method by UI to let user to save and publish
	 * the new event/meeting with pre-assigned attendees.
	 * 
	 * @return an action outcome string.
	 */
	public String processAssignStudentsAndPublish() {
		preSaveAction();
		try {
			signupMeetingService.saveMeeting(signupMeeting, sakaiFacade.getCurrentUserId());

			logger.info("Meeting Name:"
					+ signupMeeting.getTitle()
					+ " - UserId:"
					+ sakaiFacade.getCurrentUserId()
					+ " - has created a new meeting at meeting startTime:"
					+ getSakaiFacade().getTimeService().newTime(signupMeeting.getStartTime().getTime())
							.toStringLocalFull());

			/* send email to notiy attendee's assigned spot */			
			try {
				if (sendEmail)
					signupMeetingService.sendEmail(signupMeeting, SIGNUP_NEW_MEETING);
				signupMeetingService.sendEmail(signupMeeting, SIGNUP_PRE_ASSIGN);
			} catch (Exception e) {
				logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
				Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
			}

			try {
				signupMeetingService.postToCalendar(signupMeeting);
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.posted_failed"));
				logger.info(Utilities.rb.getString("error.calendarEvent.posted_failed") + " - " + e.getMessage());
			}

			reset();

		} catch (PermissionException e) {
			logger.info(Utilities.rb.getString("no.permission_create_event") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("no.permission_create_event"));
		} catch (Exception e) {
			logger.info(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
		}
		return MAIN_EVENTS_LIST_PAGE_URL;
	}

	/**
	 * This is a JSF action call method by UI to let user add an attendee into
	 * the page.
	 * 
	 * @return an action outcome string.
	 */
	public String addAttendee() {
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();

		String attendeeEid = null;
		String attendeeUserId;
		try {
			if (isEidInputMode())
				attendeeEid = getEidInputByUser();
			else
				attendeeEid = (String) newAttendeeInput.getValue();

			attendeeUserId = sakaiFacade.getUserId(attendeeEid);
		} catch (UserNotDefinedException e) {
			Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user") + attendeeEid);
			return "";
		}
		SignupAttendee attendee = new SignupAttendee(attendeeUserId, sakaiFacade.getCurrentLocationId());
		if (isDuplicateAttendee(timeslotWrapper.getTimeSlot(), attendee))
			Utilities.addErrorMessage(Utilities.rb.getString("attendee.already.in.timeslot"));
		else {
			timeslotWrapper.addAttendee(attendee, sakaiFacade.getUserDisplayName(attendeeUserId));
		}

		return "";
	}

	/**
	 * This is a JSF action call method by UI to let user remove an attendee
	 * from the page.
	 * 
	 * @return an action outcome string.
	 */
	public String removeAttendee() {
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		String attendeeUserId = Utilities.getRequestParam(PARAM_NAME_FOR_ATTENDEE_USERID);

		timeslotWrapper.removeAttendee(attendeeUserId);

		return "";
	}

	private boolean isDuplicateAttendee(SignupTimeslot timeslot, SignupAttendee newAttendee) {
		List<SignupAttendee> attendees = timeslot.getAttendees();
		if (attendees != null && !attendees.isEmpty()) {
			for (SignupAttendee attendee : attendees) {
				if (attendee.getAttendeeUserId().equals(newAttendee.getAttendeeUserId()))
					return true;
			}
		}
		return false;
	}

	private List<SignupTimeslot> timeslots() {
		List<SignupTimeslot> slots = new ArrayList<SignupTimeslot>();
		List<TimeslotWrapper> timeSlotWrappers = getTimeSlotWrappers();

		if (timeSlotWrappers == null)
			return null;// for Announcement type

		for (TimeslotWrapper wrapper : timeSlotWrappers) {
			SignupTimeslot slot = wrapper.getTimeSlot();
			slots.add(slot);
		}
		return slots;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of TimeslotWrapper objects.
	 */
	public List<TimeslotWrapper> getTimeSlotWrappers() {
		if (timeSlotWrappers == null)
			timeSlotWrappers = timeSlotWrappers();

		return timeSlotWrappers;

	}

	/* construct the TimeslotWrapper list from the raw data */
	private List<TimeslotWrapper> timeSlotWrappers() {
		String meetingType = signupMeeting.getMeetingType();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(signupMeeting.getStartTime());

		List<TimeslotWrapper> timeSlotWrappers = new ArrayList<TimeslotWrapper>();
		if (meetingType.equals(INDIVIDUAL)) {
			for (int i = 0; i < numberOfSlots; i++) {
				SignupTimeslot slot = new SignupTimeslot();
				slot.setMaxNoOfAttendees(numberOfAttendees);
				slot.setStartTime(calendar.getTime());
				calendar.add(Calendar.MINUTE, getTimeSlotDuration());
				slot.setEndTime(calendar.getTime());
				slot.setDisplayAttendees(isShowParticipants());

				TimeslotWrapper wrapper = new TimeslotWrapper(slot);
				wrapper.setPositionInTSlist(i);
				timeSlotWrappers.add(wrapper);
			}
			/* set endTime for meeting */
			getMeetingEndTime();
			return timeSlotWrappers;
		}

		if (meetingType.equals(GROUP)) {
			SignupTimeslot slot = new SignupTimeslot();
			slot.setMaxNoOfAttendees(unlimited ? SignupTimeslot.UNLIMITED : maxOfAttendees);
			slot.setStartTime(signupMeeting.getStartTime());
			slot.setEndTime(signupMeeting.getEndTime());
			slot.setDisplayAttendees(isShowParticipants());

			TimeslotWrapper wrapper = new TimeslotWrapper(slot);
			timeSlotWrappers.add(wrapper);
			return timeSlotWrappers;
		}
		return null;
	}

	/**
	 * This is a getter method for UI and it provides the selected
	 * site(s)/group(s) by user.
	 * 
	 * @return a list of SignupSite objects.
	 */
	public List<SignupSite> getSelectedSignupSites() {
		List<SignupSite> sites = new ArrayList<SignupSite>();

		List<SignupSiteWrapper> siteWrappers = new ArrayList<SignupSiteWrapper>(this.getOtherSites());
		siteWrappers.add(0, this.getCurrentSite());

		for (SignupSiteWrapper wrapper : siteWrappers) {
			SignupSite site = wrapper.getSignupSite();
			if (wrapper.isSelected()) {
				site.setSignupGroups(null); /*
											 * the meeting is 'site scope' for
											 * this site
											 */
			} else {
				List<SignupGroupWrapper> signupGroupWrappers = wrapper.getSignupGroupWrappers();
				List<SignupGroup> groups = new ArrayList<SignupGroup>();
				for (SignupGroupWrapper groupWrapper : signupGroupWrappers) {
					if (groupWrapper.isSelected())
						groups.add(groupWrapper.getSignupGroup());
				}
				if (groups.isEmpty())/*
										 * neither site or it's groups aren't
										 * selected
										 */
					continue;
				site.setSignupGroups(groups);
			}
			sites.add(site);
		}
		return sites;

	}

	/**
	 * This is a getter method for UI and it provides the time for Signup-begin.
	 * 
	 * @return a Date object.
	 */
	public Date getSignupBeginInDate() {
		return Utilities.subTractTimeToDate(signupMeeting.getStartTime(), signupBegins, signupBeginsType);
	}

	/**
	 * This is a getter method for UI and it provides the time for
	 * Signup-Deadline.
	 * 
	 * @return a Date object.
	 */
	public Date getSignupDeadlineInDate() {
		return Utilities.subTractTimeToDate(signupMeeting.getEndTime(), deadlineTime, deadlineTimeType);
	}

	/**
	 * This is a getter method for UI and it calculates the meeting ending time
	 * according to meeting type
	 * 
	 * @return a Date object.
	 */
	/*  */
	public Date getMeetingEndTime() {
		if (signupMeeting.getMeetingType().equals(INDIVIDUAL)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(signupMeeting.getStartTime());
			int internval = getTimeSlotDuration() * numberOfSlots;
			calendar.add(Calendar.MINUTE, internval);
			signupMeeting.setEndTime(calendar.getTime());
			return calendar.getTime();
		}

		return signupMeeting.getEndTime();
	}

	/**
	 * This is a getter method for UI purpose *
	 * 
	 * @return a long value.
	 */
	public long getMeetingStarTime() {
		return this.signupMeeting.getStartTime().getTime();
	}

	/**
	 * This is a setter method for UI and it's doing nothing.
	 * 
	 * @param v
	 *            a long value
	 */
	public void setMeetingStarTime(long v) {
		// do-nothing; it's only for UI purpose
	}

	/**
	 * This is a getter method for UI purpose
	 * 
	 * @return a formated date string value.
	 */
	public String getMeetingEndTimeFormat() {
		/*
		 * not use TimeServie due to UI JavaScript format,otherwise the UI
		 * javaScript need to be changed.
		 */
		return SignupDateFormat.format_date_h_mm_a(getMeetingEndTime());
	}

	/**
	 * This is a setter method for UI and it's doing nothing.
	 * 
	 * @param in
	 *            a string value
	 */
	public void setMeetingEndTimeFormat(String in) {
		// nothing, it's only for UI purpose
	}

	/*
	 * public String addMeeting() { return null; }
	 */

	/**
	 * This is a getter method.
	 * 
	 * @return a SakaiFacade object.
	 */
	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	/**
	 * This is a setter.
	 * 
	 * @param sakaiFacade
	 *            a SakaiFacade object.
	 */
	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return a SignupMeetingService object.
	 */
	public SignupMeetingService getSignupMeetingService() {
		return signupMeetingService;
	}

	/**
	 * This is a setter.
	 * 
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 */
	public void setSignupMeetingService(SignupMeetingService signupMeetingService) {
		this.signupMeetingService = signupMeetingService;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a SignupMeeting object.
	 */
	public SignupMeeting getSignupMeeting() {
		return signupMeeting;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object.
	 */
	public void setSignupMeeting(SignupMeeting signupMeeting) {
		this.signupMeeting = signupMeeting;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a HtmlInputHidden object.
	 */
	public HtmlInputHidden getCurrentStepHiddenInfo() {
		return currentStepHiddenInfo;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param htmlInputHidden
	 *            a HtmlInputHidden object.
	 */
	public void setCurrentStepHiddenInfo(HtmlInputHidden htmlInputHidden) {
		this.currentStepHiddenInfo = htmlInputHidden;
	}

	/**
	 * This is a getter method for UI and check if the event/meeting is
	 * recurred.
	 * 
	 * @return true if the event/meeting is recurred.
	 */
	public boolean isRecurrence() {
		return recurrence;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param recurrence
	 *            a boolean value.
	 */
	public void setRecurrence(boolean recurrence) {
		this.recurrence = recurrence;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getMaxOfAttendees() {
		if (unlimited)
			return SignupTimeslot.UNLIMITED;

		return maxOfAttendees;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param maxOfAttendees
	 *            an int value
	 */
	public void setMaxOfAttendees(int maxOfAttendees) {
		this.maxOfAttendees = maxOfAttendees;
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
	 * This is a getter method for UI.
	 * 
	 * @return a HtmlInputHidden object.
	 */
	public int getTimeSlotDuration() {
		if (this.timeSlotDuration == 0) {// first time
			long duration = (getSignupMeeting().getEndTime().getTime() - getSignupMeeting().getStartTime().getTime())
					/ (MINUTE_IN_MILLISEC * getNumberOfSlots());
			setTimeSlotDuration((int) duration);
		}
		return this.timeSlotDuration;
	}

	public void setTimeSlotDuration(int timeSlotDuration) {
		this.timeSlotDuration = timeSlotDuration;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getNumberOfAttendees() {
		return numberOfAttendees;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param numberOfAttendees
	 *            an int value.
	 */
	public void setNumberOfAttendees(int numberOfAttendees) {
		this.numberOfAttendees = numberOfAttendees;
	}

	/**
	 * This is a getter method and it checks if the number of attendees is
	 * limited.
	 * 
	 * @return true if the number of attendees is unlimited.
	 */
	public boolean isUnlimited() {
		return unlimited;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param unlimited
	 *            a boolean value.
	 */
	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getSignupBegins() {
		return signupBegins;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param bookingTime
	 *            an int value.
	 */
	public void setSignupBegins(int bookingTime) {
		this.signupBegins = bookingTime;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a string value.
	 */
	public String getSignupBeginsType() {
		return signupBeginsType;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param bookingTimeType
	 *            a string value.
	 */
	public void setSignupBeginsType(String bookingTimeType) {
		this.signupBeginsType = bookingTimeType;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an int value.
	 */
	public int getDeadlineTime() {
		return deadlineTime;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param deadlineTime
	 *            an int value.
	 */
	public void setDeadlineTime(int deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a string value.
	 */
	public String getDeadlineTimeType() {
		return deadlineTimeType;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param deadlineTimeType
	 *            a string value.
	 */
	public void setDeadlineTimeType(String deadlineTimeType) {
		this.deadlineTimeType = deadlineTimeType;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of SignupSiteWrapper objects.
	 */
	public List<SignupSiteWrapper> getOtherSites() {
		if (otherSites == null)
			setupSingupSites();

		return otherSites;
	}

	private void setupSingupSites() {
		String currentUserId = sakaiFacade.getCurrentUserId();
		String currentSiteId = sakaiFacade.getCurrentLocationId();
		List tmpSites = sakaiFacade.getUserSites(currentUserId);
		List<SignupSiteWrapper> siteWrappers = new ArrayList<SignupSiteWrapper>();

		for (Iterator iter = tmpSites.iterator(); iter.hasNext();) {
			SignupSite signupSite = (SignupSite) iter.next();
			String siteId = signupSite.getSiteId();
			boolean siteAllowed = signupMeetingService.isAllowedToCreateinSite(currentUserId, siteId);
			SignupSiteWrapper sSiteWrapper = new SignupSiteWrapper(signupSite, siteAllowed);

			List<SignupGroup> signupGroups = signupSite.getSignupGroups();
			List<SignupGroupWrapper> groupWrappers = new ArrayList<SignupGroupWrapper>();
			for (SignupGroup group : signupGroups) {
				boolean groupAllowed = false;
				if (siteAllowed)
					groupAllowed = true;
				else
					groupAllowed = signupMeetingService.isAllowedToCreateinGroup(currentUserId, siteId, group
							.getGroupId());
				
				if(groupAllowed){
					SignupGroupWrapper groupWrapper = new SignupGroupWrapper(group, groupAllowed);
					groupWrappers.add(groupWrapper);
				}
			}
			sSiteWrapper.setSignupGroupWrappers(groupWrappers);
			/* default setting if having site permission */
			if (siteId.equals(currentSiteId))
				sSiteWrapper.setSelected(siteAllowed);

			if (!currentSiteId.equals(signupSite.getSiteId()))
				siteWrappers.add(sSiteWrapper);
			else
				this.currentSite = sSiteWrapper;
		}
		this.otherSites = siteWrappers;
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
		if (currentSite == null)
			setupSingupSites();

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

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if email notification will be sent away.
	 */
	public boolean isSendEmail() {
		return sendEmail;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param sendEmail
	 *            a boolean value.
	 */
	public void setSendEmail(boolean sendEmail) {
		this.sendEmail = sendEmail;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a constant string.
	 */
	public String getIndividual() {
		return INDIVIDUAL;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a constant string.
	 */
	public String getGroup() {
		return GROUP;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a constant string.
	 */
	public String getAnnouncement() {
		return ANNOUNCEMENT;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a list of SelectItem objects.
	 */
	public List<SelectItem> getAllAttendees() {
		return allAttendees;
	}

	/**
	 * This is a setter.
	 * 
	 * @param allAttendees
	 *            a list of SelectItem objects.
	 */
	public void setAllAttendees(List<SelectItem> allAttendees) {
		this.allAttendees = allAttendees;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if the name of attendees will be made public.
	 */
	public boolean isShowParticipants() {
		return showParticipants;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param showParticipants
	 *            a boolean value.
	 */
	public void setShowParticipants(boolean showParticipants) {
		this.showParticipants = showParticipants;
	}

	private void loadAllAttendees(SignupMeeting meeting) {
		List<SignupUser> users = sakaiFacade.getAllUsers(meeting);

		if (users != null && users.size() > MAX_NUM_PARTICIPANTS_FOR_DROPDOWN_BEFORE_AUTO_SWITCH_TO_EID_INPUT_MODE) {
			setEidInputMode(true);
			return;
		}

		setEidInputMode(false);
		this.allAttendees = new ArrayList<SelectItem>();
		SelectItem sItem = new SelectItem("", " " + Utilities.rb.getString("label.select.attendee"));
		allAttendees.add(sItem);
		for (SignupUser user : users) {
			allAttendees.add(new SelectItem(user.getEid(), user.getDisplayName()));
		}
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an UIInput object.
	 */
	public UIInput getNewAttendeeInput() {
		return newAttendeeInput;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param newAttendeeInput
	 *            an UIInput object.
	 */
	public void setNewAttendeeInput(UIInput newAttendeeInput) {
		this.newAttendeeInput = newAttendeeInput;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an UIData object.
	 */
	public UIData getTimeslotWrapperTable() {
		return timeslotWrapperTable;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param timeslotWrapperTable
	 *            an UIData object.
	 */
	public void setTimeslotWrapperTable(UIData timeslotWrapperTable) {
		this.timeslotWrapperTable = timeslotWrapperTable;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if the organizer want to receive email notification from
	 *         attendees.
	 */
	public boolean isReceiveEmail() {
		return receiveEmail;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param receiveEmail
	 *            a boolean value.
	 */
	public void setReceiveEmail(boolean receiveEmail) {
		this.receiveEmail = receiveEmail;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a constant string value.
	 */
	public String getAttendeeUserId() {
		return PARAM_NAME_FOR_ATTENDEE_USERID;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if it's an announcement event/meeting type.
	 */
	public boolean isAnnouncementType() {
		return ANNOUNCEMENT.equals(getSignupMeeting().getMeetingType());
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if it's an group event/meeting type.
	 */
	public boolean isGroupType() {
		return GROUP.equals(getSignupMeeting().getMeetingType());
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if it's an individual event/meeting type.
	 */
	public boolean isIndividualType() {
		return INDIVIDUAL.equals(getSignupMeeting().getMeetingType());
	}

	/**
	 * This is for UI purpose and it displays the meeting type, which user can
	 * redefine in message bundle
	 * 
	 * @return a meeting display type
	 */
	public String getDisplayCurrentMeetingType() {
		String mType = "";
		if (isIndividualType())
			mType = Utilities.rb.getString("label_individaul");
		else if (isGroupType())
			mType = Utilities.rb.getString("label_group");
		else if (isAnnouncementType())
			mType = Utilities.rb.getString("label_announcement");

		return mType;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if it's in the eid-input mode.
	 */
	public boolean isEidInputMode() {
		return eidInputMode;
	}

	/**
	 * This is a setter.
	 * 
	 * @param eidInputMode
	 *            a boolean value.
	 */
	public void setEidInputMode(boolean eidInputMode) {
		this.eidInputMode = eidInputMode;
	}

	/**
	 * This is for javascrip UI only.
	 * 
	 * @return empty string.
	 */
	public String getUserInputEid() {
		return "";
	}

	/**
	 * This is for javascrip UI only.
	 * 
	 * @param userInputEid
	 *            a String value.
	 */
	public void setUserInputEid(String userInputEid) {
		if (userInputEid != null && userInputEid.length() > 0)
			this.eidInputByUser = userInputEid;
	}

	/* proxy method */
	private String getEidInputByUser() {
		String eid = this.eidInputByUser;
		this.eidInputByUser = null;// reset for use once only
		return eid;
	}

}
