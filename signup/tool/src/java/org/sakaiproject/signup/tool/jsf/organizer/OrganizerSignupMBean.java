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

package org.sakaiproject.signup.tool.jsf.organizer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.exception.IdUnusedException;
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
import org.sakaiproject.signup.tool.jsf.attendee.EditCommentSignupMBean;
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
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * event/meeting view page:<b>orgSignupMeeting.jsp</b> and backbone system. It
 * provides all the necessary business logic
 * 
 * @author Peter Liu
 * 
 * </P>
 */
@Slf4j
public class OrganizerSignupMBean extends SignupUIBaseBean {

	private UIData timeslotWrapperTable;

	private UIInput attendeeTimeSlotWithId;

	private UIInput replacedAttendeeEidOrEmail;

	/* proxy param for eid by user input */
	private String eidOrEmailByUser;

	private UIInput waiterEidOrEmail;

	private UIData attendeeWrapperTable;

	private UIData waiterWrapperTable;

	private List<SelectItem> allAttendees;

	private List<SignupUser> allSignupUsers;

	public static final String DELIMITER = "::";

	private String selectedAction = REPLACE_ACTION;

	private String selectedEditTimeslotId;

	private UIInput selectedTimeslotId;

	private boolean addNewAttendee;

	private UIInput addNewAttendeeUserEidOrEmail;

	private UIInput listPendingType;

	private String userActionType = REPLACE_ACTION;

	private String selectedFirstUser = "gl256";

	private String currentUserId;

	private String currentSiteId;

	private CopyMeetingSignupMBean copyMeetingMBean;

	private EditMeetingSignupMBean editMeetingMBean;

	private EditCommentSignupMBean editCommentMBean;

	private CancelRestoreTimeslot cancelRestoreTimeslot;

	private boolean collapsedMeetingInfo;

	private boolean eidInputMode = false;
	public String timeslottoGroup;

	/**
	 * This will initialize all the wrapper objects such as
	 * SignupMeetingWrapper, SignupTimeslotWrapper etc.
	 * 
	 * @param meetingWrapper
	 *            a SignupMeetingWrapper object.
	 */
	public void init(SignupMeetingWrapper meetingWrapper) throws Exception {
		this.eidInputMode = false;
		this.collapsedMeetingInfo = false;
		reset(meetingWrapper);
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
		setEidInputMode(meetingWrapper.getMeeting().isEidInputMode());
		if(!isEidInputMode() && (getAllAttendees()==null || getAllAttendees().isEmpty())){
			loadAllAttendees(meetingWrapper.getMeeting());
		}
		
		//reset email checkBox value setting
		this.sendEmail = meetingWrapper.getMeeting().isSendEmailByOwner();

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
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
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
		
		this.editCommentMBean.init(attWrp, this.getAttendeeRole(attUserId), getMeetingWrapper(), timeslotId);
		return VIEW_COMMENT_PAGE_URL;
	}

	/**
	 * find an attendee in a specific time slot
	 * @param timeslotId
	 * @param userId
	 * @return
	 */
	private AttendeeWrapper findAttendee(String timeslotId, String userId) {
		if (getTimeslotWrappers() == null || getTimeslotWrappers().isEmpty())
			return null;

		String timeslotPeriod = null;
		for (TimeslotWrapper wrapper : getTimeslotWrappers()) {
			if (wrapper.getTimeSlot().getId().toString().equals(timeslotId)) {
				timeslotPeriod = getSakaiFacade().getTimeService().newTime(
						wrapper.getTimeSlot().getStartTime().getTime()).toStringLocalTime()
						+ " - "
						+ getSakaiFacade().getTimeService().newTime(wrapper.getTimeSlot().getEndTime().getTime())
								.toStringLocalTime();
				List<AttendeeWrapper> attWrp = wrapper.getAttendeeWrappers();
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
		SignupUser sUser = getSakaiFacade().getSignupUser(getMeetingWrapper().getMeeting(), attendeeUserId);		
		if (sUser == null)
			return "unknown";
		else
			return sUser.getUserRole().getId();
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
					log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
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
					log.warn(Utilities.rb.getString("exception.no.such.user")
							+ (String) replacedAttendeeEidOrEmail.getValue() + " -- " + e.getMessage());
					Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user")
							+ (String) replacedAttendeeEidOrEmail.getValue());
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
					log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}

		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());

		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
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
		String userEidOrEmail = null;
		if (isEidInputMode()) {
			userEidOrEmail = getEidOrEmailInputByUser();
		} else {
			userEidOrEmail = (String) replacedAttendeeEidOrEmail.getValue();
		}
		
		//check if there are multiple email addresses associated with input
		List<String> associatedEids = getEidsForEmail(userEidOrEmail.trim());
		if(associatedEids.size() > 1) {
			throw new SignupUserActionException(MessageFormat.format(Utilities.rb.getString("exception.multiple.eids"), new Object[] {userEidOrEmail, StringUtils.join(associatedEids, ", ")}));
		}
		
		String replacerUserId = getUserIdForEidOrEmail(userEidOrEmail);
		SignupUser replSignUser = getSakaiFacade().getSignupUser(getMeetingWrapper().getMeeting(), replacerUserId);
		if(replSignUser ==null){
			throw new SignupUserActionException(MessageFormat.format(Utilities.rb.getString("user.has.no.permission.attend"), new Object[] {userEidOrEmail}));
		}
		
		TimeslotWrapper wrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();

		ReplaceAttendee replaceAttendee = new ReplaceAttendee(this.currentUserId(), this.currentSiteId(),
				signupMeetingService);		
		replaceAttendee.replace(getMeetingWrapper().getMeeting(), wrapper.getTimeSlot(), this.selectedFirstUser,
				replacerUserId, replSignUser.getMainSiteId());

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
		if(meeting.isEidInputMode()){
			setEidInputMode(true);
			return;
		}
			

		try {
			Site site = getSakaiFacade().getSiteService().getSite(getSakaiFacade().getCurrentLocationId());
			if(site !=null){
				int allMemeberSize = site.getMembers()!=null? site.getMembers().size() : 0;
				/*
				 * due to efficiency, user has to input EID instead of using dropdown
				 * user name list
				 */
				/*First check to avoid load all site member up if there is ten of thousends*/
				if(allMemeberSize > MAX_NUM_PARTICIPANTS_FOR_DROPDOWN_BEFORE_AUTO_SWITCH_TO_EID_INPUT_MODE){
					setEidInputMode(true);		
					return;
				}
			}
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}
		
		this.allSignupUsers = sakaiFacade.getAllPossibleAttendees(meeting);
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
		String previous_displayName ="";
		int index = 0;
		for (SignupUser user : allSignupUsers) {
			if(user.getDisplayName().equals(previous_displayName)){
				allAttendees.add(new SelectItem(user.getEid(), user.getDisplayName()+ "(" + user.getEid() +")"));
				SelectItem prev_sItem = allAttendees.get(index-1);
				//checking: not already has eid for triple duplicates case
				if(!prev_sItem.getLabel().contains("(")){
					prev_sItem.setLabel(prev_sItem.getLabel() + " (" + prev_sItem.getValue() +")");
				}
				
			}else {
				allAttendees.add(new SelectItem(user.getEid(), user.getDisplayName()));
			}
			
			previous_displayName = user.getDisplayName();
			index++;
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
			
			//clean the list of attendees
			List<SignupAttendee> attendees = getValidAttendees(wrapper.getTimeSlot().getAttendees());
			
			if (attendees != null && !attendees.isEmpty()) {
				for (SignupAttendee att : attendees) {
					SelectItem sItem = new SelectItem(wrapper.getTimeSlot().getId() + DELIMITER + att.getAttendeeUserId(), sakaiFacade.getUserDisplayName(att.getAttendeeUserId()));
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

	public UIInput getWaiterEidOrEmail() {
		return waiterEidOrEmail;
	}

	public void setWaiterEidOrEmail(UIInput waiterEidOrEmail) {
		this.waiterEidOrEmail = waiterEidOrEmail;
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

		String newAttendeeEidOrEmail = null;
		if (isEidInputMode()) {
			newAttendeeEidOrEmail = getEidOrEmailInputByUser();
		} else {
			if (addNewAttendeeUserEidOrEmail != null && ((String) addNewAttendeeUserEidOrEmail.getValue()).trim().length() > 0) {
				newAttendeeEidOrEmail = ((String) addNewAttendeeUserEidOrEmail.getValue()).trim();
			}
		}

		if (StringUtils.isBlank(newAttendeeEidOrEmail)) {
			return ORGANIZER_MEETING_PAGE_URL;
		}
		
		//check if there are multiple email addresses associated with input
		List<String> associatedEids = getEidsForEmail(newAttendeeEidOrEmail.trim());
		if(associatedEids.size() > 1) {
			Utilities.addErrorMessage(MessageFormat.format(Utilities.rb.getString("exception.multiple.eids"), new Object[] {newAttendeeEidOrEmail, StringUtils.join(associatedEids, ", ")}));
			return ORGANIZER_MEETING_PAGE_URL;
		}

		String newUserId = getUserIdForEidOrEmail(newAttendeeEidOrEmail.trim());
		if(StringUtils.isBlank(newUserId)){
			Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user") + newAttendeeEidOrEmail);
			return ORGANIZER_MEETING_PAGE_URL;
		}
		
		SignupUser newAttendeeSignUser = getSakaiFacade().getSignupUser(getMeetingWrapper().getMeeting(), newUserId);
		if(newAttendeeSignUser ==null){
			Utilities.addErrorMessage(MessageFormat.format(Utilities.rb.getString("user.has.no.permission.attend"), new Object[] {newAttendeeEidOrEmail}));
			return ORGANIZER_MEETING_PAGE_URL;
		}

		SignupAttendee newAttendee = new SignupAttendee(newUserId, newAttendeeSignUser.getMainSiteId());
		timeslotWrapper.setNewAttendee(newAttendee);

		SignupMeeting meeting = null;
		try {
			AddAttendee addAttendee = new AddAttendee(signupMeetingService, currentUserId(), currentSiteId(), true);
			meeting = addAttendee.signup(getMeetingWrapper().getMeeting(), timeslotWrapper.getTimeSlot(),
					timeslotWrapper.getNewAttendee());

			/*TODO : we may need to re-think adding this sendAtteneeEmail condition here.  We want that the Organizer will be enable to control 
			 * the email notification via that checkBox on 'Organizer Meeting' page.
			 * */
			boolean sendAttendeeEmail = false;//Utilities.getSignupConfigParamVal("signup.email.notification.attendee.signed.up", true);

			if (sendEmail || sendAttendeeEmail) {
				try {
					signupMeetingService.sendEmailToParticipantsByOrganizerAction(addAttendee
							.getSignupEventTrackingInfo());
				} catch (Exception e) {
					log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());

		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
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
		setAddNewAttendeeUserEidOrEmail(null);

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
					log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
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

		String newWaiterEidOrEmail = null;
		if (isEidInputMode()) {
			newWaiterEidOrEmail = getEidOrEmailInputByUser();
		} else {
			if (waiterEidOrEmail != null && ((String) waiterEidOrEmail.getValue()).trim().length() > 0)
				newWaiterEidOrEmail = ((String) waiterEidOrEmail.getValue()).trim();
		}

		if (StringUtils.isBlank(newWaiterEidOrEmail)) {
			return ORGANIZER_MEETING_PAGE_URL;
		}
		
		//check if there are multiple email addresses associated with input
		List<String> associatedEids = getEidsForEmail(newWaiterEidOrEmail.trim());
		if(associatedEids.size() > 1) {
			Utilities.addErrorMessage(MessageFormat.format(Utilities.rb.getString("exception.multiple.eids"), new Object[] {newWaiterEidOrEmail, StringUtils.join(associatedEids, ", ")}));
			return ORGANIZER_MEETING_PAGE_URL;
		}
		
		String waiterUserId = getUserIdForEidOrEmail(newWaiterEidOrEmail.trim());
		if(StringUtils.isBlank(waiterUserId)){
			Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user") + newWaiterEidOrEmail);
			return ORGANIZER_MEETING_PAGE_URL;
		}
		
		SignupUser waiterSignUser = getSakaiFacade().getSignupUser(getMeetingWrapper().getMeeting(), waiterUserId);
		if(waiterSignUser ==null){
			Utilities.addErrorMessage(MessageFormat.format(Utilities.rb.getString("user.has.no.permission.attend"), new Object[] {newWaiterEidOrEmail}));
			return ORGANIZER_MEETING_PAGE_URL;
		}
		
		SignupAttendee newWaiter = new SignupAttendee(waiterUserId, waiterSignUser.getMainSiteId());
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
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
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

		if (StringUtils.isBlank(removedUserId)) {
			return ORGANIZER_MEETING_PAGE_URL;
		}
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
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
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
	public UIInput getReplacedAttendeeEidOrEmail() {
		return replacedAttendeeEidOrEmail;
	}

	/**
	 * This is a setter.
	 * 
	 * @param replacedAttendeeEid
	 *            an UIInput object.
	 */
	public void setReplacedAttendeeEidOrEmail(UIInput replacedAttendeeEidOrEmail) {
		this.replacedAttendeeEidOrEmail = replacedAttendeeEidOrEmail;
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
	public UIInput getAddNewAttendeeUserEidOrEmail() {
		return addNewAttendeeUserEidOrEmail;
	}

	/**
	 * This is a setter.
	 * 
	 * @param addNewAttendeeUserEidOrEmail
	 *            an attendee's Eid or email string.
	 */
	public void setAddNewAttendeeUserEidOrEmail(UIInput addNewAttendeeUserEidOrEmail) {
		this.addNewAttendeeUserEidOrEmail = addNewAttendeeUserEidOrEmail;
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
	 * @return a EditCommentSignupMBean object.
	 */
	public EditCommentSignupMBean getEditCommentMBean() {
		return editCommentMBean;
	}

	/**
	 * This is a setter.
	 * 
	 * @param editCommentMBean
	 *            a EditCommentSignupMBean object.
	 */
	public void setEditCommentMBean(EditCommentSignupMBean editCommentMBean) {
		this.editCommentMBean = editCommentMBean;
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
	public String getUserInputEidOrEmail() {
		return "";
	}

	/**
	 * This is a setter.
	 * 
	 * @param value
	 *            an user input eid or email address value .
	 */
	public void setUserInputEidOrEmail(String value) {
		if (StringUtils.isNotBlank(value)) {
			this.eidOrEmailByUser = value;
		}
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a list of SignupSite objects.
	 */
	public List<SignupSite> getPublishedSignupSites() {
		SignupMeetingWrapper signupMeetingWrapper = getMeetingWrapper();
		if (signupMeetingWrapper!=null){
			return getMeetingWrapper().getMeeting().getSignupSites();
		}
		return null;
	}

	/* proxy method */
	private String getEidOrEmailInputByUser() {
		String value = this.eidOrEmailByUser;
		this.eidOrEmailByUser = null;// reset for only use once
		return value;
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
	
	/**
	 * This is to retrieve attr from UI Commandbutton
	 * 
	 * @param timeslottoGroup
	 *            a String value
	 */
	public void attrListener(ActionEvent event){
		 
		timeslottoGroup = (String) Utilities.getActionAttribute(event, "timeslottoGroup");
		 
		} 
	
	/**
	 * It's a getter method for UI
	 * denotes the direction of the synchronize process
	 * @return a String value timeslottoGroup
	 */
	public String gettimeslottoGroup() {
		return timeslottoGroup;
		}
	
	/**
	 * This is a setter
	 *  @return a String value
	 */	 
		public void setNickname(String timeslottoGroup) {
		this.timeslottoGroup = timeslottoGroup;
		} 
	
	/**
	 * Synchronise the users in a timeslot with the users in a group.
	 * VT customized to allow the user choose the synchronize direction
	 
	 * @return url to the same page which will trigger a reload
	 */
	public String synchroniseGroupMembership() {

		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		
		//get groupId for timeslot
		String groupId = timeslotWrapper.getGroupId();
		SignupMeeting meeting = null;
		
		if(StringUtils.isBlank(groupId)){
			//TODO. 
			//Create the group. Grab the list of attendees in the timeslot and add all at once.
			//Will need to also save the groupId into the timeslot.
			//For now, we just give a message.
			
			Utilities.addErrorMessage(Utilities.rb.getString("error.no_group_for_timeslot"));
			return ORGANIZER_MEETING_PAGE_URL;
		} else {
			List<String> attendeeUserIds = convertAttendeeWrappersToUuids(timeslotWrapper.getAttendeeWrappers());
			
				//process to synchronize the time slot attendees to group
			
				if(timeslottoGroup != null && !timeslottoGroup.trim().isEmpty() && !sakaiFacade.addUsersToGroup(attendeeUserIds, currentSiteId(), groupId, timeslottoGroup)) {
					Utilities.addErrorMessage(Utilities.rb.getString("error.group_sync_failed"));
					return ORGANIZER_MEETING_PAGE_URL;
				}
			
				//retrieve all members in group
				List<String> groupMembers = sakaiFacade.getGroupMembers(currentSiteId(), groupId);
			
				//process to synchronize from site group members to time slot
				if (timeslottoGroup == null ||  timeslottoGroup.isEmpty()){
					
					//1. first to keep the common members of timeslot and group
					
					List<String> commonmem = new ArrayList<String>(attendeeUserIds);
					commonmem.retainAll(groupMembers); 
					
					//2. only add the group members not existed in timeslot
					groupMembers.removeAll(attendeeUserIds); 
					
					//3. remove the time slot attendees that existed only in timeslot
						try {
							for (String mem: attendeeUserIds){
								if(!commonmem.contains(mem)){
									CancelAttendee remove = new CancelAttendee(signupMeetingService, currentUserId(), currentSiteId(), true);
									SignupAttendee removedAttendee = new SignupAttendee(mem, currentSiteId());
									meeting = remove.cancelSignup(getMeetingWrapper().getMeeting(), timeslotWrapper.getTimeSlot(),removedAttendee);
									if (sendEmail) {
										try {
											signupMeetingService.sendEmailToParticipantsByOrganizerAction(remove.getSignupEventTrackingInfo());
											} catch (Exception e) {
												log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
												Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
											}
									}
								}
							}
						}catch (SignupUserActionException ue) {
								Utilities.addErrorMessage(ue.getMessage());
						} catch (Exception e) {
							log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
							Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
						} 
				} else{
						//remove all of the existing attendees from this list to remove duplicates
						groupMembers.removeAll(attendeeUserIds);
					}
				
			//add members and go to return page
			return addAttendeesToTimeslot(currentSiteId(),timeslotWrapper, groupMembers);
		}
	}
	
	
	/**
	 * Helper to add users to a timeslot and get the return URL
	 * @param userId
	 * @return
	 */
	private String addAttendeesToTimeslot(String siteId, TimeslotWrapper timeslotWrapper, List<String> userIds) {
		
		boolean errors = false;
		SignupMeeting meeting = null;
		
		//foreach userId, add to timeslot
		for(String userId: userIds) {
		
			SignupAttendee attendee = new SignupAttendee(userId, siteId);
			timeslotWrapper.setNewAttendee(attendee);
			
			try {
				AddAttendee addAttendee = new AddAttendee(signupMeetingService, currentUserId(), currentSiteId(), true);
				meeting = addAttendee.signup(getMeetingWrapper().getMeeting(), timeslotWrapper.getTimeSlot(),timeslotWrapper.getNewAttendee());
	
				if (sendEmail) {
					try {
						signupMeetingService.sendEmailToParticipantsByOrganizerAction(addAttendee.getSignupEventTrackingInfo());
					} catch (Exception e) {
						log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					}
				}
			} catch (SignupUserActionException ue) {
				Utilities.addErrorMessage(ue.getMessage());
				log.error(ue.getMessage());
				errors = true;
				break;
	
			} catch (Exception e) {
				log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage(), e);
				errors = true;
				break;
			}
		}
		
		if(errors) {
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
			return ORGANIZER_MEETING_PAGE_URL;
		}
		
		String nextPage = updateMeetingwrapper(meeting, ORGANIZER_MEETING_PAGE_URL);

		if (ORGANIZER_MEETING_PAGE_URL.equals(nextPage)) {
			setAddNewAttendee(false);
			setSelectedTimeslotId(null);
			
			Utilities.addInfoMessage(Utilities.rb.getString("group_synchronise_done"));
		}
		return nextPage;
	}
	
	/**
	 * Helper to check if we need to show the email link
	 * If we have email addresses, then the link shows up.
	 * @return true/false
	 */
	public boolean isShowEmailAllAttendeesLink() {
		return StringUtils.isNotBlank(getAllAttendeesEmailAddressesFormatted());
	}
	
	/**
	 * Helper to get the email address for the current user
	 * @return
	 */
	public String getCurrentUserEmailAddress() {
		return sakaiFacade.getUser(sakaiFacade.getCurrentUserId()).getEmail();
	}
}
