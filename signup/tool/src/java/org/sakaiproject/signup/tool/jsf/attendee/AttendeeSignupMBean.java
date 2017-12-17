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

package org.sakaiproject.signup.tool.jsf.attendee;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.signup.logic.SignupUser;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.tool.jsf.AttendeeWrapper;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.jsf.TimeslotWrapper;
import org.sakaiproject.signup.tool.jsf.attendee.EditCommentSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.AddAttendee;
import org.sakaiproject.signup.tool.jsf.organizer.action.AddWaiter;
import org.sakaiproject.signup.tool.jsf.organizer.action.CancelAttendee;
import org.sakaiproject.signup.tool.jsf.organizer.action.RemoveWaiter;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Attendee's
 * event/meeting view page:<b>SignupMeeting.jsp</b> and backbone system. It
 * provides all the necessary business logic.
 * </P>
 */
@Slf4j
public class AttendeeSignupMBean extends SignupUIBaseBean {

	private UIData timeslotWrapperTable;

	private String currentUserId;

	private String currentSiteId;

	private boolean collapsedMeetingInfo;
	
	private EditCommentSignupMBean editCommentMBean;
	
	/**
	 * This will initialize all the wrapper objects such as
	 * SignupMeetingWrapper, SignupTimeslotWrapper etc.
	 * 
	 * @param meetingWrapper
	 *            a SignupMeetingWrapper object.
	 */
	public void init(SignupMeetingWrapper meetingWrapper) throws Exception {
		setMeetingWrapper(meetingWrapper);
		updateTimeSlotWrappers(meetingWrapper);
		this.collapsedMeetingInfo = false;
	}

	/**
	 * This is a JSF action call method by UI to let attendee sign up the
	 * event/meeting and go to add_comment page.
	 * 
	 * @return an action outcome string.
	 */
	public String attendeeSignup() {
		this.timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		SignupAttendee newAttendee = new SignupAttendee();
		String currentUserId = sakaiFacade.getCurrentUserId();
		newAttendee.setAttendeeUserId(currentUserId);
		newAttendee.setSignupSiteId(sakaiFacade.getCurrentLocationId());
		timeslotWrapper.setNewAttendee(newAttendee);
		
		if(this.meetingWrapper.getMeeting().isAllowComment())
			return ATTENDEE_ADD_COMMENT_PAGE_URL;
		else
			return attendeeSaveSignup();//skip comment page
		
	}

	/**
	 * This is a JSF action call method by UI to allow attendee to save his/her
	 * signup.
	 * 
	 * @return an action outcome string.
	 */
	public String attendeeSaveSignup() {
		SignupMeeting meeting = null;
		try {
			AddAttendee signup = new AddAttendee(signupMeetingService, currentUserId(), currentSiteId(), false);
			meeting = signup.signup(meetingWrapper.getMeeting(), timeslotWrapper.getTimeSlot(), timeslotWrapper
					.getNewAttendee());
			/* send notification to organizer */
			if (meeting.isReceiveEmailByOwner()) {
				try {
					signupMeetingService.sendEmailToOrganizer(signup.getSignupEventTrackingInfo());
				} catch (Exception e) {
					log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}

			//also send confirmation email to attendee
			boolean sendAttendeeEmail = Utilities.getSignupConfigParamVal("signup.email.notification.attendee.signed.up", true);

			if(sendEmail || sendAttendeeEmail) {
				try {
					signupMeetingService.sendEmailToAttendee(signup.getSignupEventTrackingInfo());
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

		// TODO calendar event id;

		return updateMeetingwrapper(meeting, ATTENDEE_MEETING_PAGE_URL);
	}

	/**
	 * This is a JSF action call method by UI to cancel the signup from the
	 * event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String attendeeCancelSignup() {
		SignupMeeting meeting = null;
		this.timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		try {
			CancelAttendee signup = new CancelAttendee(signupMeetingService, currentUserId(), currentSiteId(), false);
			SignupAttendee removedAttendee = new SignupAttendee(currentUserId(), currentSiteId());
			meeting = signup.cancelSignup(meetingWrapper.getMeeting(), timeslotWrapper.getTimeSlot(), removedAttendee);
			/* send notification to organizer and possible promoted participants.
			 * This also handles sending email to the attendee as well */
			try {
				signupMeetingService.sendCancellationEmail(signup.getSignupEventTrackingInfo());
			} catch (Exception e) {
				log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
				Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
			}
			
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		// TODO calendar event id;

		/*
		 * refresh meeting list to catch the changes when go back the main
		 * meeting list page
		 */
		if (Utilities.getSignupMeetingsBean().isShowMyAppointmentTime())
			Utilities.resetMeetingList();

		return updateMeetingwrapper(meeting, ATTENDEE_MEETING_PAGE_URL);
	}

	/**
	 * This is a JSF action call method by UI to let the attendee add himself on
	 * the waiting list in the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String attendeeAddToWaitingList() {
		this.timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		SignupMeeting meeting = null;
		try {
			AddWaiter addWaiter = new AddWaiter(signupMeetingService, currentUserId(), currentSiteId(), ON_BOTTOM_LIST,
					false);
			SignupAttendee newWaiter = new SignupAttendee();
			newWaiter.setAttendeeUserId(sakaiFacade.getCurrentUserId());
			newWaiter.setSignupSiteId(sakaiFacade.getCurrentLocationId());
			meeting = addWaiter.addToWaitingList(meetingWrapper.getMeeting(), timeslotWrapper.getTimeSlot(), newWaiter);

		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		return updateMeetingwrapper(meeting, ATTENDEE_MEETING_PAGE_URL);
	}

	/**
	 * This is a JSF action call method by UI to let the attendee remove himself
	 * from the waiting list in the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String attendeeRemoveFromWaitingList() {
		this.timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		SignupMeeting meeting = null;

		try {
			// when attendee add him/her to the waiting list -> add to the
			// bottom of the list
			RemoveWaiter removeWaiter = new RemoveWaiter(signupMeetingService, currentUserId(), currentSiteId(),
					ON_BOTTOM_LIST, false);
			SignupAttendee newWaiter = new SignupAttendee(currentUserId(), currentSiteId());
			meeting = removeWaiter.removeFromWaitingList(meetingWrapper.getMeeting(), timeslotWrapper.getTimeSlot(),
					newWaiter);
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
		}

		// TODO calendar event id;

		return updateMeetingwrapper(meeting, ATTENDEE_MEETING_PAGE_URL);
	}
	
	/**
	 * This is a JSF action call method by UI to edit the attendee's
	 * comments of the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String editAttendeeComment() {
		String attUserId = sakaiFacade.getCurrentUserId();
		String timeslotId = (String) Utilities.getRequestParam("timeslotId");
		if (attUserId == null || timeslotId == null)
			return "";

		AttendeeWrapper attWrp = findAttendee(timeslotId, attUserId);
		if (attWrp == null)
			return "";
		
		this.editCommentMBean.init(attWrp, this.getAttendeeRole(attUserId), getMeetingWrapper(), timeslotId);
		
		return EDIT_COMMENT_PAGE_URL;
	}
	
	/**
	 * this methods returns the attendee's user role
	 * @param attendeeUserId
	 * @return
	 */
	private String getAttendeeRole(String attendeeUserId) {
		SignupUser sUser = getSakaiFacade().getSignupUser(getMeetingWrapper().getMeeting(), attendeeUserId);		
		if (sUser == null)
			return "unknown";
		else
			return sUser.getUserRole().getId();
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
	 * This is a getter.
	 * 
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
	 * It's a getter method for UI.
	 * 
	 * @return a boolean value
	 */
	public boolean isCollapsedMeetingInfo() {
		return collapsedMeetingInfo;
	}

	/**
	 * It's a setter method for UI.
	 * 
	 * @param collapsedMeetingInfo
	 *            a boolean value
	 */
	public void setCollapsedMeetingInfo(boolean collapsedMeetingInfo) {
		this.collapsedMeetingInfo = collapsedMeetingInfo;
	}
	
	/**
	 * This is a getter method which provide current Iframe id for refresh
	 * IFrame purpose.
	 * 
	 * @return a String
	 */
	public String getIframeId() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		String iFrameId = (String) request.getAttribute("sakai.tool.placement.id");
		return iFrameId;
	}
	
	public String getCurUserDisplayName(){
		return getSakaiFacade().getUserDisplayName(currentUserId());
	}
	
	/**
	 * Generate and send for download an ICS file for the timeslot
	 */
	public void downloadICSForTimeslot() {
		TimeslotWrapper timeslotWrapper = (TimeslotWrapper) timeslotWrapperTable.getRowData();
		downloadICSForMeetingTimeSlot(timeslotWrapper);
	}

}
