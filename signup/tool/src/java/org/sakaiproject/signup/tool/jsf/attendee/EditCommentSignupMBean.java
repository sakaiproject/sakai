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

import java.util.List;

import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupUser;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.logic.messages.AttendeeComment;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfoImpl;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.AttendeeWrapper;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.jsf.organizer.OrganizerSignupMBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.EditComment;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Attendees's
 * edit comment page:<b>editComment.jsp</b> and backbone system.
 * </P>
 */
@Slf4j
public class EditCommentSignupMBean extends SignupUIBaseBean {

	private AttendeeWrapper attendeeWrapper;

	private String AttendeeRole;
	
	private boolean validationError;
	
	private String relatedTimeslotId;
	
	private String comment;
	
	private long tId;
	
	
	/**
	 * To initialize this UIBean, which lives in a session scope.
	 * 
	 * @param attwrp
	 *            an AttendeeWrapper object.
	 * @param role
	 *            a String value
	 * @param meetingwrp
	 *            a SignupMeetingWrapper object.
	 * @para timeslotId
	 * 			  a String value
	 */
	public void init(AttendeeWrapper attwrp, String role, SignupMeetingWrapper meetingwrp, String timeslotId) {
		this.attendeeWrapper = attwrp;
		this.AttendeeRole = role;
		this.meetingWrapper = meetingwrp;
		//reset
		this.relatedTimeslotId = null;
		this.tId = 0;
		this.sendEmail = false;
		
		if(timeslotId !=null){
			try{
				tId = Long.valueOf(timeslotId);			
			}
			catch(NumberFormatException e){
				tId = findTimeslotId();
			}
		}
		else{
			tId = findTimeslotId();//if missing
		}
		this.relatedTimeslotId = timeslotId;
		this.comment = meetingwrp.getMeeting().getTimeslot(tId).getAttendee(attendeeWrapper.getSignupAttendee().getAttendeeUserId()).getComments();
	}
	
	/*
	 * Method that directs users to the edit comment page when clicked
	 */
	public String editAttendeeComment(){
		String attUserId = attendeeWrapper.getSignupAttendee().getAttendeeUserId();
		String timeslotId = relatedTimeslotId;
		if (attUserId == null || timeslotId == null)
			return "";
		
		this.init(attendeeWrapper, this.getAttendeeRole(attUserId), getMeetingWrapper(), timeslotId);
		
		return EDIT_COMMENT_PAGE_URL;
	}
	
	/**
	 * Method that returns the attendee's role for permission purposes
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
	

	/*
	 * Method to find a timeslot's ID
	 */
	public Long findTimeslotId(){
		List<SignupTimeslot> timeslotList = meetingWrapper.getMeeting().getSignupTimeSlots();
		Long timeslotId = null;
		String attendeeId = attendeeWrapper.getSignupAttendee().getAttendeeUserId();
		
		for(int i = 0; i < timeslotList.size(); i++){
			for(int j = 0; j < timeslotList.get(i).getAttendees().size(); j++) {   
				if(timeslotList.get(i).getAttendees().get(j).getAttendeeUserId().equals(attendeeId)){
					timeslotId = timeslotList.get(i).getId();
				}
			}
		}
		
		return timeslotId;
	}
	
	/*
	 * when the save button is clicked, this method saves the comment to the database if conditions are met 
	 */
	public String attendeeSaveComment(){
		
		if (validationError) {
			validationError = false;
			return "";
		}
		
		SignupMeeting meeting = meetingWrapper.getMeeting(); //old meeting
		Long timeslotId = this.tId;
		boolean isOrganizer = meeting.getPermission().isUpdate();
		String attendeeId = attendeeWrapper.getSignupAttendee().getAttendeeUserId();
		
		try {
			if(!isOrganizer && !sakaiFacade.getCurrentUserId().equals(attendeeId)){
				Utilities.addErrorMessage(Utilities.rb.getString("no.permissoin.do_it"));
				return "";
			}
			else{
				EditComment editComment = new EditComment(signupMeetingService, attendeeId,
						sakaiFacade.getCurrentLocationId(), false, meeting, timeslotId, getSakaiFacade());
				meeting = editComment.updateComment(comment);
			}
			
			if (sendEmail) {
				try {
					SignupEventTrackingInfo trackingInfo = new SignupEventTrackingInfoImpl();
					trackingInfo.setMeeting(meeting);
					trackingInfo.setAttendeeComment(new AttendeeComment(comment, attendeeId, sakaiFacade.getCurrentUserId()));
					signupMeetingService.sendUpdateCommentEmail(trackingInfo);
				} catch (Exception e) {
					log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
			
		} catch (PermissionException pe) {
				Utilities.addErrorMessage(Utilities.rb.getString("no.permissoin.do_it"));
			} catch (SignupUserActionException ue) {
				/* TODO need to keep in the same page with new data if db changes */
				Utilities.addErrorMessage(ue.getMessage());
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("db.error_or_event.notExisted"));
				log.error(Utilities.rb.getString("db.error_or_event.notExisted") + " - " + e.getClass() + ":" + e.getMessage());
				Utilities.resetMeetingList();
				return MAIN_EVENTS_LIST_PAGE_URL;
			}
			if(isOrganizer){
				reloadMeetingWrapperInOrganizerPage();
			}
			
			/*
			 * refresh meeting list to catch the changes when go back the main
			 * meeting list page
			 */
			if (Utilities.getSignupMeetingsBean().isShowMyAppointmentTime())
				Utilities.resetMeetingList();
			
			return updateMeetingwrapper(meeting, checkReturnUrl());
	
	}
	
	/*
	 * Method used to reload the meeting wrapper for the organizer to get the newest updated meeting list
	 */
	private void reloadMeetingWrapperInOrganizerPage() {
		OrganizerSignupMBean bean = (OrganizerSignupMBean) FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap().get("OrganizerSignupMBean");
		SignupMeeting meeting = reloadMeeting(meetingWrapper.getMeeting());
		this.meetingWrapper.setMeeting(meeting);
		//update latest creator for UI
		this.meetingWrapper.setCreator(sakaiFacade.getUserDisplayName(meeting.getCreatorUserId()));
		bean.reset(meetingWrapper);
	}
	
	/**
	 * 
	 * @param meeting
	 * @return
	 */
	private SignupMeeting reloadMeeting(SignupMeeting meeting) {
		return signupMeetingService.loadSignupMeeting(meeting.getId(), sakaiFacade.getCurrentUserId(), sakaiFacade
				.getCurrentLocationId());
	}
	
	/*
	 * This method returns the meeting page url depending on the user's role
	 */
	public String checkReturnUrl(){
		boolean isOrganizer = meetingWrapper.getMeeting().getPermission().isUpdate();
		
		if(isOrganizer)
			return ORGANIZER_MEETING_PAGE_URL;
		else
			return ATTENDEE_MEETING_PAGE_URL;
	}
	
	/*
	 * Checks if the user is an organizer and returns the comment note
	 */
	public String getUserType(){
		boolean isOrganizer = meetingWrapper.getMeeting().getPermission().isUpdate();
		
		if(isOrganizer)
			return Utilities.rb.getString(ATTENDEE_EDIT_COMMENT_NOTE);
		else
			return Utilities.rb.getString(ORGANIZER_EDIT_COMMENT_NOTE);
	}
	
	/**
	 * This is a getter method for UI.
	 * 
	 * @return an AttendeeWrapper object.
	 */
	public AttendeeWrapper getAttendeeWrapper() {
		return attendeeWrapper;
	}

	/**
	 * This is a setter.
	 * 
	 * @param attendeeWrapper
	 *            an AttendeeWrapper object.
	 */
	public void setAttendeeWrapper(AttendeeWrapper attendeeWrapper) {
		this.attendeeWrapper = attendeeWrapper;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a string value.
	 */
	public String getAttendeeRole() {
		return AttendeeRole;
	}

	/**
	 * This is a setter.
	 * 
	 * @param attendeeRole
	 *            a string value.
	 */
	public void setAttendeeRole(String attendeeRole) {
		AttendeeRole = attendeeRole;
	}

	/**
	 * Overwrite the default one.
	 * 
	 * @return a SignupMeetingWrapper object.
	 */
	public SignupMeetingWrapper getMeetingWrapper() {
		return meetingWrapper;
	}
	
	/**
	 * show the attendee's Eid (user Id)
	 * @return eid String
	 */
	public String getAttendeeEid(){
		String eid = attendeeWrapper.getSignupAttendee().getAttendeeUserId();
		User user = sakaiFacade.getUser(attendeeWrapper.getSignupAttendee().getAttendeeUserId());
		if(user !=null){
			eid = user.getEid();
		}

		return eid;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
