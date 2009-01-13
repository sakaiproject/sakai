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
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.sakaiproject.signup.logic.SignupUser;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.tool.jsf.AttendeeWrapper;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.jsf.TimeslotWrapper;
import org.sakaiproject.signup.tool.jsf.organizer.action.AddAttendee;
import org.sakaiproject.signup.tool.jsf.organizer.action.AddWaiter;
import org.sakaiproject.signup.tool.jsf.organizer.action.CancelAttendee;
import org.sakaiproject.signup.tool.jsf.organizer.action.CancelRestoreTimeslot;
import org.sakaiproject.signup.tool.jsf.organizer.action.LockUnlockTimeslot;
import org.sakaiproject.signup.tool.jsf.organizer.action.MoveAttendee;
import org.sakaiproject.signup.tool.jsf.organizer.action.RemoveWaiter;
import org.sakaiproject.signup.tool.jsf.organizer.action.ReplaceAttendee;
import org.sakaiproject.signup.tool.jsf.organizer.action.SwapAttendee;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * event/meeting view page:<b>orgSignupMeeting.jsp</b> and backbone system. It
 * provides all the necessary business logic
 * </P>
 */
public class OrganizerSignupMBean extends SignupUIBaseBean {

	private UIData timeslotWrapperTable;

	private UIInput attendeeTimeSlotWithId;

	private UIInput replacedAttendeeEid;

	/* proxy param for eid by user input */
	private String eidInputByUser;

	private UIInput waiterEid;

	private UIData attendeeWrapperTable;

	private UIData waiterWrapperTable;

	private List<SelectItem> allAttendees;

	private List<SignupUser> allSignupUsers;

	public static final String DELIMITER = "::";

	private String selectedAction = REPLACE_ACTION;

	private String selectedEditTimeslotId;

	private UIInput selectedTimeslotId;

	private Log logger = LogFactoryImpl.getLog(getClass());

	private boolean addNewAttendee;

	private UIInput addNewAttendeeUserEid;

	private UIInput listPendingType;

	private String userActionType = REPLACE_ACTION;

	private String selectedFirstUser = "gl256";

	private String currentUserId;

	private String currentSiteId;

	private CopyMeetingSignupMBean copyMeetingMBean;

	private EditMeetingSignupMBean editMeetingMBean;

	private ViewCommentSignupMBean viewCommentMBean;

	private CancelRestoreTimeslot cancelRestoreTimeslot;

	private boolean collapsedMeetingInfo;

	private boolean eidInputMode = false;

	/**
	 * This will initialize all the wrapper objects such as
	 * SignupMeetingWrapper, SignupTimeslotWrapper etc.
	 * 
	 * @param meetingWrapper
	 *            a SignupMeetingWrapper object.
	 */
	public void init(SignupMeetingWrapper meetingWrapper) throws Exception {
		reset(meetingWrapper);
		this.eidInputMode = false;
		this.collapsedMeetingInfo = false;
		loadAllAttendees(meetingWrapper.getMeeting());
	}

	/**
	 * This will basically update the Timeslot wrappers after event/meeting is
	 * updated
	 * 
	 * @param meetingWrapper
	 *            a SignupMeetingWrapper object.
	 */
	public void reset(SignupMeetingWrapper meetingWrapper) {
		setMeetingWrapper(meetingWrapper);
		updateTimeSlotWrappers(meetingWrapper);

	}

	/**
	 * This is a JSF action call method by UI to modify the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String modifyMeeting() {
		/* get latest copy of meeting */
		SignupMeeting meeting = null;
		String goMainPageDueToError = updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);
		if (goMainPageDueToError.equals(MAIN_EVENTS_LIST_PAGE_URL))
			return goMainPageDueToError;

		editMeetingMBean.setMeetingWrapper(getMeetingWrapper());
		editMeetingMBean.reset();

		return MODIFY_MEETING_PAGE_URL;
	}

	/**
	 * This is a JSF action call method by UI to copy the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String copyMeeting() {
		copyMeetingMBean.setMeetingWrapper(getMeetingWrapper());
		copyMeetingMBean.reset();
		return COPTY_MEETING_PAGE_URL;
	}

	/**
	 * This is a JSF action call method by UI to lock/unlock time slot of the
	 * event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String processLockTsAction() {
		SignupMeeting meeting = null;
		try {
			TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
			LockUnlockTimeslot lockTimeslot = new LockUnlockTimeslot(getMeetingWrapper().getMeeting(), timeslotWrapper
					.getTimeSlot(), currentUserId(), currentSiteId(), signupMeetingService);
			meeting = lockTimeslot.lockOrUnlock();
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		return updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);
	}

	/**
	 * This is a JSF action call method by UI to go to Cancel confirmation page.
	 * 
	 * @return an action outcome string.
	 */
	public String initiateCancelTimeslot() {
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		cancelRestoreTimeslot = new CancelRestoreTimeslot(getMeetingWrapper().getMeeting(), timeslotWrapper
				.getTimeSlot(), currentUserId(), currentSiteId(), signupMeetingService);
		return cancelRestoreTimeslot();
	}

	/**
	 * This is a JSF action call method by UI to restore time slot of the
	 * event/meeting.
	 * 
	 * @return an action outcome string
	 */
	public String restoreTimeslot() {
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		cancelRestoreTimeslot = new CancelRestoreTimeslot(getMeetingWrapper().getMeeting(), timeslotWrapper
				.getTimeSlot(), currentUserId(), currentSiteId(), signupMeetingService);
		return cancelRestoreTimeslot();
	}

	/**
	 * This is a JSF action call method by UI to cancel time slot of the
	 * event/meeting.
	 * 
	 * @return an action outcome string
	 */
	public String cancelTimeslot() {
		return cancelRestoreTimeslot();
	}

	/**
	 * This is a JSF action call method by UI to view a specific attendee's
	 * comment of the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String viewAttendeeComment() {
		String attUserId = (String) Utilities.getRequestParam("attendeeUserId");
		String timeslotId = (String) Utilities.getRequestParam("timeslotId");
		if (attUserId == null || timeslotId == null)
			return "";

		AttendeeWrapper attWrp = findAttendee(timeslotId, attUserId);
		if (attWrp == null)
			return "";

		this.viewCommentMBean.init(attWrp, this.getAttendeeRole(attUserId), getMeetingWrapper());
		return VIEW_COMMENT_PAGE_URL;
	}

	/* find an attendee in a specific time slot */
	private AttendeeWrapper findAttendee(String timeslotId, String userId) {
		if (getTimeslotWrappers() == null || getTimeslotWrappers().isEmpty())
			return null;

		String timeslotPeriod = null;
		for (TimeslotWrapper wraper : getTimeslotWrappers()) {
			if (wraper.getTimeSlot().getId().toString().equals(timeslotId)) {
				timeslotPeriod = getSakaiFacade().getTimeService().newTime(
						wraper.getTimeSlot().getStartTime().getTime()).toStringLocalTime()
						+ " - "
						+ getSakaiFacade().getTimeService().newTime(wraper.getTimeSlot().getEndTime().getTime())
								.toStringLocalTime();
				List<AttendeeWrapper> attWrp = wraper.getAttendeeWrappers();
				for (AttendeeWrapper att : attWrp) {
					if (att.getSignupAttendee().getAttendeeUserId().equals(userId)) {
						att.setTimeslotPeriod(timeslotPeriod);
						return att;
					}
				}
				break;
			}
		}
		return null;
	}

	private String getAttendeeRole(String attendeeUserId) {
		if (this.allSignupUsers == null & this.allSignupUsers.isEmpty())
			return "unknown";

		for (SignupUser user : allSignupUsers) {
			if (user.getInternalUserId().equals(attendeeUserId))
				return user.getUserRole().getId();
		}
		return "unknown";
	}

	private String cancelRestoreTimeslot() {
		if (cancelRestoreTimeslot == null)
			return ORGANIZER_MEETING_PAGE_URL;

		SignupMeeting meeting = null;
		try {
			meeting = cancelRestoreTimeslot.cancelOrRestore();
			if (sendEmail) {
				try {
					signupMeetingService.sendEmailToParticipantsByOrganizerAction(cancelRestoreTimeslot
							.getSignupEventTrackingInfo());
				} catch (Exception e) {
					logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		return updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);
	}

	/**
	 * This is a JSF action call method by UI to edit the attendee in the
	 * event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String editTimeslotAttendee() {
		SignupMeeting meeting = null;
		try {
			SignupEventTrackingInfo signupEventTrackingInfo = null;
			if (this.userActionType.equals(MOVE_ACTION)) { // old:selectedAction
				signupEventTrackingInfo = moveAttendee();
			}
			if (this.userActionType.equals(REPLACE_ACTION)) {
				try {
					signupEventTrackingInfo = replaceAttendee();
				} catch (UserNotDefinedException e) {
					logger.warn(Utilities.rb.getString("exception.no.such.user")
							+ (String) replacedAttendeeEid.getValue() + " -- " + e.getMessage());
					Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user")
							+ (String) replacedAttendeeEid.getValue());
					return "";
				}
			}

			if (this.userActionType.equals(SWAP_ACTION)) {
				signupEventTrackingInfo = swapAttendees();
			}

			if (sendEmail) {
				try {
					signupMeetingService.sendEmailToParticipantsByOrganizerAction(signupEventTrackingInfo);
				} catch (Exception e) {
					logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}

		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());

		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		/* reset */
		setSelectedAction(REPLACE_ACTION);

		/*
		 * refresh meeting list to catch the changes when go back the main
		 * meetings list page
		 */
		if (Utilities.getSignupMeetingsBean().isShowMyAppointmentTime())
			Utilities.resetMeetingList();

		return updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);
	}

	/* The logic below assumes an attendee appears only once in a timeslot */
	private SignupEventTrackingInfo swapAttendees() throws Exception {
		SwapAttendee swapAttendee = new SwapAttendee(currentUserId(), currentSiteId(), signupMeetingService);
		TimeslotWrapper currentTimeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();

		swapAttendee.swapAttendee(getMeetingWrapper().getMeeting(), currentTimeslotWrapper.getTimeSlot(),
				this.selectedFirstUser, (String) attendeeTimeSlotWithId.getValue());

		return swapAttendee.getSignupEventTrackingInfo();
	}

	private SignupEventTrackingInfo replaceAttendee() throws Exception {
		String userEid = null;
		if (isEidInputMode())
			userEid = getEidInputByUser();
		else
			userEid = (String) replacedAttendeeEid.getValue();

		String replacerUserId = sakaiFacade.getUserId(userEid);

		TimeslotWrapper wrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();

		ReplaceAttendee replaceAttendee = new ReplaceAttendee(this.currentUserId(), this.currentSiteId(),
				signupMeetingService);
		replaceAttendee.replace(getMeetingWrapper().getMeeting(), wrapper.getTimeSlot(), this.selectedFirstUser,
				replacerUserId, this.allSignupUsers);

		return replaceAttendee.getSignupEventTrackingInfo();
	}

	private SignupEventTrackingInfo moveAttendee() throws Exception {
		TimeslotWrapper wrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();

		MoveAttendee moveAttendee = new MoveAttendee(this.currentUserId(), this.currentSiteId(), signupMeetingService);
		moveAttendee.move(getMeetingWrapper().getMeeting(), wrapper.getTimeSlot(), this.selectedFirstUser,
				(String) selectedTimeslotId.getValue());

		return moveAttendee.getSignupEventTrackingInfo();

	}

	/**
	 * This will load all the potential participants for an event/meeting and
	 * wrap it for UI purpose. Due to efficiency issue, it will auto rolled back
	 * to Eid-Input mode when the number of users are bigger thank 600 ( defined
	 * by <I>MAX_NUM_ATTENDEES_ALLOWED_BEFORE_AUTO_SWITCH_TO_EID_INPUT_MODE</I>}
	 * value).
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 */
	public void loadAllAttendees(SignupMeeting meeting) {
		this.allSignupUsers = sakaiFacade.getAllUsers(meeting);
		/*
		 * due to efficiency, user has to input EID instead of using dropdown
		 * user name list
		 */
		if (allSignupUsers != null
				&& allSignupUsers.size() > MAX_NUM_PARTICIPANTS_FOR_DROPDOWN_BEFORE_AUTO_SWITCH_TO_EID_INPUT_MODE) {
			setEidInputMode(true);
			return;
		}

		setEidInputMode(false);
		allAttendees = new ArrayList<SelectItem>();
		for (SignupUser user : allSignupUsers) {
			allAttendees.add(new SelectItem(user.getEid(), user.getDisplayName()));
		}
	}

	/* This one overwrites the parent one due to two more stuffs to create */
	protected void updateTimeSlotWrappers(SignupMeetingWrapper meetingWrapper) {
		super.updateTimeSlotWrappers(meetingWrapper);
		/* for organizer UI swap-dropdown list */
		createUISwapListForEachTimeSlot(getTimeslotWrappers());
		createMoveAvailabelTimeSlots(getTimeslotWrappers());

	}

	/* create the avaiable timeslot for UI purpose */
	private void createMoveAvailabelTimeSlots(List<TimeslotWrapper> timeslotWrapperList) {

		List<SelectItem> list = new ArrayList<SelectItem>();
		for (TimeslotWrapper wrapper : timeslotWrapperList) {
			list.add(new SelectItem(wrapper.getTimeSlot().getId().toString(), wrapper.getLabel()));
		}

		/*
		 * list of avalilable timeslot for move excluding the current timeslot
		 */
		for (int i = 0; i < list.size(); i++) {
			List<SelectItem> moveAvailableTimeSlots = new ArrayList<SelectItem>(list);
			moveAvailableTimeSlots.remove(i);// remove the current one

			TimeslotWrapper wrapper = timeslotWrapperList.get(i);
			wrapper.setMoveAvailableTimeSlots(moveAvailableTimeSlots);
		}
	}

	/* create possible swap dropdown choices for UI purpose */
	private void createUISwapListForEachTimeSlot(List<TimeslotWrapper> timeslotWrapperList) {
		List<SelectItem> tsAttendeeGroups = new ArrayList<SelectItem>();
		for (TimeslotWrapper wrapper : timeslotWrapperList) {
			String grpLabel = wrapper.getLabel();
			List<SelectItem> attendeeOnTS = new ArrayList<SelectItem>();
			List<SignupAttendee> Attendees = wrapper.getTimeSlot().getAttendees();
			if (Attendees != null && !Attendees.isEmpty()) {
				for (SignupAttendee att : Attendees) {
					SelectItem sItem = new SelectItem(wrapper.getTimeSlot().getId() + DELIMITER
							+ att.getAttendeeUserId(), sakaiFacade.getUserDisplayName(att.getAttendeeUserId()));
					attendeeOnTS.add(sItem);
				}
			}

			SelectItemGroup sigrp = new SelectItemGroup(grpLabel, "Timeslot Name", false, (SelectItem[]) attendeeOnTS
					.toArray(new SelectItem[0]));
			tsAttendeeGroups.add(sigrp);
		}

		/*
		 * list of avalilable timeslot for swap excluding the current timeslot
		 */
		for (int i = 0; i < tsAttendeeGroups.size(); i++) {
			List<SelectItem> swapAvailableTimeSlots = new ArrayList<SelectItem>(tsAttendeeGroups);
			swapAvailableTimeSlots.remove(i);// remove the current one

			TimeslotWrapper wrapper = timeslotWrapperList.get(i);
			wrapper.setSwapDropDownList(swapAvailableTimeSlots);
		}
	}

	public UIData getTimeslotWrapperTable() {
		return timeslotWrapperTable;
	}

	public void setTimeslotWrapperTable(UIData timeslotWrapperTable) {
		this.timeslotWrapperTable = timeslotWrapperTable;
	}

	public UIInput getAttendeeTimeSlotWithId() {
		return attendeeTimeSlotWithId;
	}

	public void setAttendeeTimeSlotWithId(UIInput attendeeEid) {
		this.attendeeTimeSlotWithId = attendeeEid;
	}

	public UIInput getWaiterEid() {
		return waiterEid;
	}

	public void setWaiterEid(UIInput waiterEid) {
		this.waiterEid = waiterEid;
	}

	public String prepareAddAttendee() {
		setAddNewAttendee(true);
		setSelectedEditTimeslotId(null);
		return ORGANIZER_MEETING_PAGE_URL;
	}

	/**
	 * This is a JSF action call method by UI to add a new attendee into the
	 * event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String addAttendee() {
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();

		String newAttendeeEid = null;
		if (isEidInputMode())
			newAttendeeEid = getEidInputByUser();
		else {
			if (addNewAttendeeUserEid != null && ((String) addNewAttendeeUserEid.getValue()).trim().length() > 0)
				newAttendeeEid = ((String) addNewAttendeeUserEid.getValue()).trim();
		}

		if (newAttendeeEid == null || newAttendeeEid.trim().length() < 1)
			return ORGANIZER_MEETING_PAGE_URL;

		String newUserId;
		try {
			newUserId = sakaiFacade.getUserId(newAttendeeEid.trim());
		} catch (UserNotDefinedException e) {
			Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user") + newAttendeeEid);
			return ORGANIZER_MEETING_PAGE_URL;
		}

		SignupAttendee newAttendee = new SignupAttendee(newUserId, getAttendeeMainActiveSiteId(newUserId,
				this.allSignupUsers, getSakaiFacade().getCurrentLocationId()));
		timeslotWrapper.setNewAttendee(newAttendee);

		SignupMeeting meeting = null;
		try {
			AddAttendee addAttendee = new AddAttendee(signupMeetingService, currentUserId(), currentSiteId(), true);
			meeting = addAttendee.signup(getMeetingWrapper().getMeeting(), timeslotWrapper.getTimeSlot(),
					timeslotWrapper.getNewAttendee());

			if (sendEmail) {
				try {
					signupMeetingService.sendEmailToParticipantsByOrganizerAction(addAttendee
							.getSignupEventTrackingInfo());
				} catch (Exception e) {
					logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());

		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		String nextPage = updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);
		// TODO calendar event id;

		if (ORGANIZER_MEETING_PAGE_URL.equals(nextPage)) {
			setAddNewAttendee(false);
			setSelectedTimeslotId(null);
		}
		return nextPage;
	}

	/**
	 * This is a JSF action call method by UI to reset user input.
	 * 
	 * @return an action outcome string.
	 */
	public String cancelAddAttendee() {
		setAddNewAttendee(false);
		setAddNewAttendeeUserEid(null);

		return ORGANIZER_MEETING_PAGE_URL;
	}

	/**
	 * This is a JSF action call method by UI to remove an attendee from the
	 * event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String removeAttendee() {
		SignupMeeting meeting = null;
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		String removedUserId = (String) Utilities.getRequestParam(ATTENDEE_USER_ID);
		try {
			CancelAttendee remove = new CancelAttendee(signupMeetingService, currentUserId(), currentSiteId(), true);
			SignupAttendee removedAttendee = new SignupAttendee(removedUserId, currentSiteId());
			meeting = remove.cancelSignup(getMeetingWrapper().getMeeting(), timeslotWrapper.getTimeSlot(),
					removedAttendee);

			if (sendEmail) {
				try {
					signupMeetingService.sendEmailToParticipantsByOrganizerAction(remove.getSignupEventTrackingInfo());
				} catch (Exception e) {
					logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		/*
		 * refresh meeting list to catch the changes when go back the main
		 * meeting list page
		 */
		if (Utilities.getSignupMeetingsBean().isShowMyAppointmentTime())
			Utilities.resetMeetingList();

		return updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);
	}

	/**
	 * This is a JSF action call method by UI to add the attendee to a waiting
	 * list in the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String addAttendeeToWList() {
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();

		String newWaiterEid = null;
		if (isEidInputMode())
			newWaiterEid = getEidInputByUser();
		else {
			if (getWaiterEid() != null && ((String) getWaiterEid().getValue()).trim().length() > 0)
				newWaiterEid = ((String) getWaiterEid().getValue()).trim();
		}

		if (newWaiterEid == null || newWaiterEid.trim().length() < 1)
			return ORGANIZER_MEETING_PAGE_URL;

		String waiterUserId;
		try {
			waiterUserId = sakaiFacade.getUserId(newWaiterEid.trim());
		} catch (UserNotDefinedException e) {
			Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user") + newWaiterEid);
			logger.warn(Utilities.rb.getString("exception.no.such.user") + newWaiterEid + "  -- " + e.getMessage());
			return ORGANIZER_MEETING_PAGE_URL;
		}

		SignupAttendee newWaiter = new SignupAttendee(waiterUserId, getAttendeeMainActiveSiteId(waiterUserId,
				allSignupUsers, getSakaiFacade().getCurrentLocationId()));
		SignupMeeting meeting = null;
		try {
			AddWaiter addWaiter = new AddWaiter(signupMeetingService, currentUserId(), currentSiteId(),
					getListOperationType(), true);
			meeting = addWaiter.addToWaitingList(getMeetingWrapper().getMeeting(), timeslotWrapper.getTimeSlot(),
					newWaiter);
			/* reset */
			resetWaitingListPendingType();
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		// TODO calendar event id;

		return updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);
	}

	private void resetWaitingListPendingType() {
		setListPendingType(null);
	}

	/**
	 * This is a JSF action call method by UI to remove the attendee from the
	 * waiting list in the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String removeAttendeeFromWList() {
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		String removedUserId = (String) Utilities.getRequestParam(ATTENDEE_USER_ID);

		if (removedUserId == null || removedUserId.trim().length() < 1)
			return ORGANIZER_MEETING_PAGE_URL;

		SignupAttendee removedWaiter = new SignupAttendee(removedUserId, currentSiteId());
		SignupMeeting meeting = null;

		try {
			RemoveWaiter removeWaiter = new RemoveWaiter(signupMeetingService, currentUserId(), currentSiteId(),
					getListOperationType(), true);
			meeting = removeWaiter.removeFromWaitingList(getMeetingWrapper().getMeeting(), timeslotWrapper
					.getTimeSlot(), removedWaiter);
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			logger.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		return updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);
	}

	/**
	 * This is a JSF getter method to get the selected attendee.
	 * 
	 * @return an UIData object.
	 */
	public UIData getAttendeeWrapperTable() {
		return attendeeWrapperTable;
	}

	/**
	 * This is a setter.
	 * 
	 * @param attendeeWrapperTable
	 *            an UIData object, which is selected by user.
	 */
	public void setAttendeeWrapperTable(UIData attendeeWrapperTable) {
		this.attendeeWrapperTable = attendeeWrapperTable;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an UIData object.
	 */
	public UIData getWaiterWrapperTable() {
		return waiterWrapperTable;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param waiterWrapperTable
	 *            an UIData object.
	 */
	public void setWaiterWrapperTable(UIData waiterWrapperTable) {
		this.waiterWrapperTable = waiterWrapperTable;
	}

	/**
	 * This is getter for UI.
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
	 */
	public void setAllAttendees(List<SelectItem> allAttendees) {
		this.allAttendees = allAttendees;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return a constant string.
	 */
	public String getAttendeeUserId() {
		return ATTENDEE_USER_ID;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a string value.
	 */
	public String getSelectedEditTimeslotId() {
		return selectedEditTimeslotId;
	}

	/**
	 * This is a setter.
	 * 
	 * @param selectedEditTimeslotId
	 *            a time slot Id string.
	 */
	public void setSelectedEditTimeslotId(String selectedEditTimeslotId) {
		this.selectedEditTimeslotId = selectedEditTimeslotId;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return a constant string.
	 */
	public String getMoveAction() {
		return MOVE_ACTION;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return a constant string
	 */
	public String getReplaceAction() {
		return REPLACE_ACTION;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return a constant string.
	 */
	public String getSwapAction() {
		return SWAP_ACTION;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return a constant string.
	 */
	public String getOnTopList() {
		return ON_TOP_LIST;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return a constant string.
	 */
	public String getOnBottomList() {
		return ON_BOTTOM_LIST;
	}

	/**
	 * This is a getter.
	 * 
	 * @return a selected action string.
	 */
	public String getSelectedAction() {
		return selectedAction;
	}

	/**
	 * This is a setter.
	 * 
	 * @param selectedAction
	 *            a string value.
	 */
	public void setSelectedAction(String selectedAction) {
		this.selectedAction = selectedAction;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return an UIInput object.
	 */
	public UIInput getReplacedAttendeeEid() {
		return replacedAttendeeEid;
	}

	/**
	 * This is a setter.
	 * 
	 * @param replacedAttendeeEid
	 *            an UIInput object.
	 */
	public void setReplacedAttendeeEid(UIInput replacedAttendeeEid) {
		this.replacedAttendeeEid = replacedAttendeeEid;
	}

	/**
	 * This is a getter.
	 * 
	 * @return an UIInput object.
	 */
	public UIInput getSelectedTimeslotId() {
		return selectedTimeslotId;
	}

	/**
	 * This is a setter.
	 * 
	 * @param selectedTimeslotId
	 *            a time slot Id string.
	 */
	public void setSelectedTimeslotId(UIInput selectedTimeslotId) {
		this.selectedTimeslotId = selectedTimeslotId;
	}

	/**
	 * Check if the action is a add-new-attendee.
	 */
	public boolean isAddNewAttendee() {
		return addNewAttendee;
	}

	/**
	 * This is a setter.
	 * 
	 * @param addNewAttendee
	 *            a boolean value.
	 */
	public void setAddNewAttendee(boolean addNewAttendee) {
		this.addNewAttendee = addNewAttendee;
	}

	/**
	 * This is a getter.
	 * 
	 * @return am UIInput object.
	 */
	public UIInput getAddNewAttendeeUserEid() {
		return addNewAttendeeUserEid;
	}

	/**
	 * This is a setter.
	 * 
	 * @param addNewAttendeeUserEid
	 *            an attendee's Eid string.
	 */
	public void setAddNewAttendeeUserEid(UIInput addNewAttendeeUserEid) {
		this.addNewAttendeeUserEid = addNewAttendeeUserEid;
	}

	/**
	 * This is a getter.
	 * 
	 * @return a user-first selected attendee eid string.
	 */
	public String getSelectedFirstUser() {
		return selectedFirstUser;
	}

	/**
	 * This is a setter.
	 * 
	 * @param selectedFirstUser
	 *            an eid stirng.
	 */
	public void setSelectedFirstUser(String selectedFirstUser) {
		this.selectedFirstUser = selectedFirstUser;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return a action type string.
	 */
	public String getUserActionType() {
		return userActionType;
	}

	/**
	 * This is a setter.
	 * 
	 * @param userActionType
	 *            a action type string.
	 */
	public void setUserActionType(String userActionType) {
		this.userActionType = userActionType;
	}

	/**
	 * This is a getter, which tells how to pend an attendee into a waiting list
	 * (on top or on bottom).
	 * 
	 * @return an UIInput object.
	 */
	public UIInput getListPendingType() {
		return listPendingType;
	}

	/**
	 * This is a setter.
	 * 
	 * @param listPendingType
	 *            a pending type string (on top or on bottom).
	 */
	public void setListPendingType(UIInput listPendingType) {
		this.listPendingType = listPendingType;
	}

	private String getListOperationType() {
		if (this.listPendingType != null && this.listPendingType.getValue() != null
				&& ((String) this.listPendingType.getValue()).trim().length() > 0)
			return ((String) this.listPendingType.getValue()).trim();

		return ON_BOTTOM_LIST;
	}

	private String currentUserId() {
		if (this.currentUserId == null)
			currentUserId = sakaiFacade.getCurrentUserId();

		return currentUserId;
	}

	private String currentSiteId() {
		if (this.currentSiteId == null)
			currentSiteId = sakaiFacade.getCurrentLocationId();

		return currentSiteId;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return a CopyMeetingSignupMBean object.
	 */
	public CopyMeetingSignupMBean getCopyMeetingMBean() {
		return copyMeetingMBean;
	}

	/**
	 * This is a setter.
	 * 
	 * @param copyMeetingMBean
	 *            a CopyMeetingSignupMBean object.
	 */
	public void setCopyMeetingMBean(CopyMeetingSignupMBean copyMeetingMBean) {
		this.copyMeetingMBean = copyMeetingMBean;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return an EditMeetingSignupMBean object.
	 */
	public EditMeetingSignupMBean getEditMeetingMBean() {
		return editMeetingMBean;
	}

	/**
	 * This is a setter.
	 * 
	 * @param editMeetingMBean
	 *            an EditMeetingSignupMBean object.
	 */
	public void setEditMeetingMBean(EditMeetingSignupMBean editMeetingMBean) {
		this.editMeetingMBean = editMeetingMBean;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return a ViewCommentSignupMBean object.
	 */
	public ViewCommentSignupMBean getViewCommentMBean() {
		return viewCommentMBean;
	}

	/**
	 * This is a setter.
	 * 
	 * @param viewCommentMBean
	 *            a ViewCommentSignupMBean object.
	 */
	public void setViewCommentMBean(ViewCommentSignupMBean viewCommentMBean) {
		this.viewCommentMBean = viewCommentMBean;
	}

	/**
	 * Check if the user input mode is Eid mode or not.
	 * 
	 * @return true if the user input mode is Eid mode
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
	 * This is a setter.
	 * 
	 * @param userInputEid
	 *            an user input eid value.
	 */
	public void setUserInputEid(String userInputEid) {
		if (userInputEid != null && userInputEid.length() > 1)
			this.eidInputByUser = userInputEid;
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a list of SignupSite objects.
	 */
	public List<SignupSite> getPublishedSignupSites() {
		return getMeetingWrapper().getMeeting().getSignupSites();
	}

	/* proxy method */
	private String getEidInputByUser() {
		String eid = this.eidInputByUser;
		this.eidInputByUser = null;// reset for only use once
		return eid;
	}

	/**
	 * It's a getter method for UI
	 * 
	 * @return a boolean value
	 */
	public boolean isCollapsedMeetingInfo() {
		return collapsedMeetingInfo;
	}

	/**
	 * This is a setter method for UI.
	 * 
	 * @param collapsedMeetingInfo
	 *            a boolean value
	 */
	public void setCollapsedMeetingInfo(boolean collapsedMeetingInfo) {
		this.collapsedMeetingInfo = collapsedMeetingInfo;
	}

}
