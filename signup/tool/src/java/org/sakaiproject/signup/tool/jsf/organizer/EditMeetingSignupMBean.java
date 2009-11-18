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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupEventTypes;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.EditMeeting;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * modify meeting page:<b>modifyMeeting.jsp</b> and backbone system.
 * </P>
 */
public class EditMeetingSignupMBean extends SignupUIBaseBean {

	private SignupMeeting signupMeeting;

	private int maxNumOfAttendees;

	private boolean showAttendeeName;

	// private int addMoreTimeslots;

	private int durationOfTslot;

	private boolean unlimited;

	private int totalEventDuration;// for group/announcement types

	private int timeSlotDuration;

	private int numberOfSlots;

	private SignupMeeting originalMeetingCopy;
	
	private List<SignupAttachment> readyToModifyAttachmentCopyList;
	
	private boolean intentionToModfyAttachment;

	private String signupBeginsType;

	/* singup can start before this minutes/hours/days */
	private int signupBegins;

	private String deadlineTimeType;

	/* singup deadline before this minutes/hours/days */
	private int deadlineTime;

	private EditMeeting editMeeting;

	private boolean convertToNoRecurrent;

	private List<SelectItem> meetingTypeRadioBttns;

	private boolean validationError;
	
	private boolean someoneSignedUp;
	
	private boolean autoReminderOptionChoice = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.autoRiminder.option.choice.setting", "true")) ? true : false;

	private boolean userIdInputModeOptionChoice = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.userId.inputMode.choiceOption.setting", "true")) ? true : false;

	/**
	 * This method will reset everything to orignal value and also initialize
	 * the value to the variables in this UIBean, which lives in a session
	 * scope.
	 * 
	 */
	public void reset() {
		// addMoreTimeslots = 0;
		maxNumOfAttendees = 0;
		showAttendeeName = false;
		sendEmail = DEFAULT_SEND_EMAIL;
		unlimited = false;

		editMeeting = null;

		convertToNoRecurrent = false;
		
		this.signupMeeting = reloadMeeting(meetingWrapper.getMeeting());
		/* for check pre-condition purpose */
		this.originalMeetingCopy = reloadMeeting(meetingWrapper.getMeeting());
		// keep the last version need a deep copy?

		/*process attachments*/
		cleanUpUnusedAttachmentCopies(this.readyToModifyAttachmentCopyList);//using browser back click
		this.readyToModifyAttachmentCopyList = createTempAttachmentCopies(meetingWrapper.getEventMainAttachments());
		this.intentionToModfyAttachment=false;
		
		List<SignupTimeslot> signupTimeSlots = getSignupMeeting().getSignupTimeSlots();

		if (signupTimeSlots != null && !signupTimeSlots.isEmpty()) {
			SignupTimeslot ts = (SignupTimeslot) signupTimeSlots.get(0);
			maxNumOfAttendees = ts.getMaxNoOfAttendees();
			this.unlimited = ts.isUnlimitedAttendee();
			showAttendeeName = ts.isDisplayAttendees();
			this.numberOfSlots = signupTimeSlots.size();

			// setTotalEventDuration(timeSlotDuration * signupTimeSlots.size());
		} else {// announcement meeting type
			setNumberOfSlots(1);

		}
		populateDataForBeginDeadline(this.signupMeeting);
		
		/*warning organizer if someone already signed up during rescheduling event*/
		someoneSignedUp = initSomeoneSignupInfo();

	}

	/* get the relative time out */
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
	
	private boolean initSomeoneSignupInfo(){
		boolean someoneSignedUp = false;
		
		if(this.signupMeeting.isRecurredMeeting()){		
			/* only check the future recurring meeting from now: today */
			List<SignupMeeting> recurringMeetings = signupMeetingService.getRecurringSignupMeetings(sakaiFacade.getCurrentLocationId(), sakaiFacade.getCurrentUserId(), this.signupMeeting.getRecurrenceId(),
					new Date());
			for (SignupMeeting sm : recurringMeetings) {
				if(hasAttendeeInMeeting(sm))
					return true;
			}
		}
		/*no recurring event*/
		someoneSignedUp = hasAttendeeInMeeting(this.signupMeeting);		
		
		return someoneSignedUp;
	}
	
	private boolean hasAttendeeInMeeting(SignupMeeting sm){
		List<SignupTimeslot> tsItems = sm.getSignupTimeSlots();
		if(tsItems !=null){
			for (SignupTimeslot ts : tsItems) {
				List<SignupAttendee> attendees = ts.getAttendees();
				if(attendees !=null && !attendees.isEmpty()){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public String doCancelAction(){
		cleanUpUnusedAttachmentCopies(this.readyToModifyAttachmentCopyList);
		return ORGANIZER_MEETING_PAGE_URL;
	}
	
	/**
	 * This method is called by JSP page for adding/removing attachments action.
	 * @return null.
	 */
	public String addRemoveAttachments(){
		this.intentionToModfyAttachment=true;
		getAttachmentHandler().processAddAttachRedirect(this.readyToModifyAttachmentCopyList, this.signupMeeting,true);
		return null;
	}
	
	private List<SignupAttachment> createTempAttachmentCopies(List<SignupAttachment> attachList){
		List<SignupAttachment> tempList = new ArrayList<SignupAttachment>();
		if(attachList !=null){
			for (SignupAttachment attach : attachList) {
				tempList.add(getAttachmentHandler().copySignupAttachment(this.signupMeeting,true,attach, ATTACH_TEMP));
			}
		}

		return tempList;
	}

	/**
	 * This is a JSF action call method by UI to modify the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String processSaveModify() {
		if (validationError) {
			validationError = false;
			return "";
		}

		try {
			SignupMeeting meeting = getSignupMeeting();

			EditMeeting editMeeting = new EditMeeting(getSakaiFacade().getCurrentUserId(), getSakaiFacade()
					.getCurrentLocationId(), getSignupMeetingService(),getAttachmentHandler(), true);
			/* Pass modified data */
			editMeeting.setCurrentNumberOfSlots(getNumberOfSlots());// (getAddMoreTimeslots());
			editMeeting.setSignupBegins(getSignupBegins());
			editMeeting.setSignupBeginsType(getSignupBeginsType());
			editMeeting.setDeadlineTime(getDeadlineTime());
			editMeeting.setDeadlineTimeType(getDeadlineTimeType());
			editMeeting.setTimeSlotDuration(getTimeSlotDuration());
			editMeeting.setMaxNumOfAttendees(getMaxNumOfAttendees());
			editMeeting.setShowAttendeeName(isShowAttendeeName());
			editMeeting.setOriginalMeetingCopy(this.originalMeetingCopy);
			editMeeting.setUnlimited(isUnlimited());
			// editMeeting.setTotalEventDuration(getTotalEventDuration());
			/* disable the association with other related recurrence events */
			editMeeting.setConvertToNoRecurrent(convertToNoRecurrent);
			
			/*set latest attachments changes*/
			/*pre-check if there is any attachment changes*/
			if(!areAttachmentChanges())
				editMeeting.setCurrentAttachList(null);
			else
				editMeeting.setCurrentAttachList(this.readyToModifyAttachmentCopyList);
			
			/* update to DB */
			editMeeting.saveModifiedMeeting(meeting);
			
			/*give warning to user in the next page if the event ending time get auto adjusted due to not even-division*/
			if (getIndividualType() && getNumberOfSlots()!=0) {
				double duration = (double)(getSignupMeeting().getEndTime().getTime() - getSignupMeeting().getStartTime().getTime())
						/ (double)(MINUTE_IN_MILLISEC * getNumberOfSlots());				
				if (duration != Math.floor(duration))
					Utilities.addErrorMessage(Utilities.rb.getString("event_endtime_auto_adjusted_warning"));				
			}

			/* For case: a set of recurring meetings are updated */
			List<SignupMeeting> successUpdatedMeetings = editMeeting.getSavedMeetings();
			/* only tracked the first one if it's a series of recurrences. may consider them later?*/
			Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_MTNG_MODIFY, ToolManager.getCurrentPlacement().getContext() + " title: "
					+ meeting.getTitle());

			if (meeting.getRecurrenceId() != null) {
				Utilities.resetMeetingList();// refresh main-page to catch
				// the changes
			}

			if (sendEmail) {
				try {
					signupMeetingService.sendEmail(successUpdatedMeetings.get(0), SIGNUP_MEETING_MODIFIED);
					/* send email to promoted waiter if size increased */
					/*
					 * Not for recurring meetings since it's not traced for all
					 * of them yet
					 */
					if (successUpdatedMeetings.get(0).getRecurrenceId() == null) {
						signupMeetingService.sendEmailToParticipantsByOrganizerAction(editMeeting
								.getSignupEventTrackingInfo());
					}
				} catch (Exception e) {
					logger.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}

			for (SignupMeeting savedMeeting : successUpdatedMeetings) {
				try {
					signupMeetingService.modifyCalendar(savedMeeting);

				} catch (PermissionException pe) {
					Utilities.addErrorMessage(Utilities.rb
							.getString("error.calendarEvent.updated_failed_due_to_permission"));
					logger.debug(Utilities.rb.getString("error.calendarEvent.updated_failed_due_to_permission")
							+ " - Meeting title:" + savedMeeting.getTitle());
				} catch (Exception e) {
					Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.updated_failed"));
					logger.warn(Utilities.rb.getString("error.calendarEvent.updated_failed") + " - Meeting title:"
							+ savedMeeting.getTitle());
				}
			}

		} catch (PermissionException pe) {
			Utilities.addErrorMessage(Utilities.rb.getString("no.permissoin.do_it"));
		} catch (SignupUserActionException ue) {
			/* TODO need to keep in the same page with new data if db changes */
			Utilities.addErrorMessage(ue.getMessage());
		} catch (Exception e) {
			Utilities.addErrorMessage(Utilities.rb.getString("db.error_or_event.notExisted"));
			logger.warn(Utilities.rb.getString("db.error_or_event.notExisted") + " - " + e.getMessage());
			Utilities.resetMeetingList();
			return MAIN_EVENTS_LIST_PAGE_URL;
		}
		reloadMeetingWrapperInOrganizerPage();
		
		cleanUpUnusedAttachmentCopies(this.readyToModifyAttachmentCopyList);//using browser back click

		return ORGANIZER_MEETING_PAGE_URL;
	}
	
	private boolean areAttachmentChanges(){
		List<SignupAttachment> origList = this.originalMeetingCopy.getSignupAttachments();
		
		if(!intentionToModfyAttachment){
			return false;
		}
		
		if(!(readyToModifyAttachmentCopyList ==null || readyToModifyAttachmentCopyList.isEmpty()) == (origList ==null || origList.isEmpty())){
			return true;
		}
		if(readyToModifyAttachmentCopyList.size() != origList.size()){
			return true;
		}
		
		for (SignupAttachment tempOne : readyToModifyAttachmentCopyList) {
			boolean found=false;
			for (SignupAttachment orig : origList) {
				String resourceId = orig.getResourceId();
				int index = resourceId.lastIndexOf("/");
				if(index > -1){
					resourceId = resourceId.substring(0,index+1) + ATTACH_TEMP +"/" + resourceId.substring(index+1, resourceId.length());
				}
				if(resourceId.equals(tempOne.getResourceId())){
					found=true;
					break;
				}
			}//for
			
			if(!found){
				return true;
			}
		}
		
		return false;
	}

	private void reloadMeetingWrapperInOrganizerPage() {
		OrganizerSignupMBean bean = (OrganizerSignupMBean) FacesContext.getCurrentInstance().getExternalContext()
				.getSessionMap().get("OrganizerSignupMBean");
		SignupMeeting meeting = reloadMeeting(meetingWrapper.getMeeting());
		this.meetingWrapper.setMeeting(meeting);
		bean.reset(meetingWrapper);
	}

	private SignupMeeting reloadMeeting(SignupMeeting meeting) {
		return signupMeetingService.loadSignupMeeting(meeting.getId(), sakaiFacade.getCurrentUserId(), sakaiFacade
				.getCurrentLocationId());
	}

	/**
	 * This is a validator to make sure that the event/meeting starting time is
	 * before ending time etc.
	 * 
	 * @param e
	 *            an ActionEvent object.
	 */
	public void validateModifyMeeting(ActionEvent e) {

		Date endTime = this.signupMeeting.getEndTime();
		Date startTime = this.signupMeeting.getStartTime();
		if (endTime.before(startTime) || startTime.equals(endTime)) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("event.endTime_should_after_startTime"));
			return;
		}
		
			
		if (DAILY.equals(this.signupMeeting.getRepeatType()) && isMeetingLengthOver24Hours(this.signupMeeting)) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.daily.problem"));
			return;
		}		
		
		int timeduration = getTimeSlotDuration();
		if (timeduration < 1) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("event.timeslot_duration_should_not_lessThan_one"));
			return;
		}

	}

	/* overwrite the default one */
	public SignupMeetingWrapper getMeetingWrapper() {
		return meetingWrapper;
	}

	/**
	 * Check if the name of attendees should be made public.
	 * 
	 * @return true if the name of attendees is made public.
	 */
	public boolean isShowAttendeeName() {
		return showAttendeeName;
	}

	/**
	 * This is a setter.
	 * 
	 * @param showAttendeeName
	 *            a boolean value.
	 */
	public void setShowAttendeeName(boolean showAttendeeName) {
		this.showAttendeeName = showAttendeeName;
	}

	/**
	 * This is a getter method.
	 * 
	 * @return an integer number.
	 */
	public int getMaxNumOfAttendees() {
		return maxNumOfAttendees;
	}

	/**
	 * This is a setter.
	 * 
	 * @param maxNumOfAttendees
	 *            an integer number.
	 */
	public void setMaxNumOfAttendees(int maxNumOfAttendees) {
		this.maxNumOfAttendees = maxNumOfAttendees;
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
	 * This is a setter.
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting object.
	 */
	public void setSignupMeeting(SignupMeeting signupMeeting) {
		this.signupMeeting = signupMeeting;
	}

	/**
	 * Check to see if the attendees are limited in the event/meeting.
	 * 
	 * @return true if the attendees are limited in the event/meeting
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
	 * This is a getter method to provide a relative time.
	 * 
	 * @return am integer number.
	 */
	public int getDeadlineTime() {
		return deadlineTime;
	}

	/**
	 * This is a setter.
	 * 
	 * @param deadlineTime
	 *            an integer number, which represents a relative time to meeting
	 *            starting time.
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
	 *            an integer number.
	 */
	public void setDeadlineTimeType(String deadlineTimeType) {
		this.deadlineTimeType = deadlineTimeType;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an integer number.
	 */
	public int getSignupBegins() {
		return signupBegins;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param signupBegins
	 *            an integer number.
	 */
	public void setSignupBegins(int signupBegins) {
		this.signupBegins = signupBegins;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return an integer number.
	 */
	public String getSignupBeginsType() {
		return signupBeginsType;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param signupBeginsType
	 *            an integer number.
	 */
	public void setSignupBeginsType(String signupBeginsType) {
		this.signupBeginsType = signupBeginsType;
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return true if the recurring event will be converted to stand-alone
	 *         event
	 */
	public boolean isConvertToNoRecurrent() {
		return convertToNoRecurrent;
	}

	public void setConvertToNoRecurrent(boolean convertToNoRecurrent) {
		this.convertToNoRecurrent = convertToNoRecurrent;
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
	 * It's a getter method for UI
	 * 
	 * @return a list of SelectItem objects for radio buttons.
	 */
	public List<SelectItem> getMeetingTypeRadioBttns() {
		this.meetingTypeRadioBttns = Utilities.getMeetingTypeSelectItems(getSignupMeeting().getMeetingType(), true);
		return meetingTypeRadioBttns;
	}

	public boolean isValidationError() {
		return validationError;
	}

	public void setValidationError(boolean validationError) {
		this.validationError = validationError;
	}

	public boolean getAutoReminderOptionChoice() {
		return autoReminderOptionChoice;
	}

	public void setAutoReminderOptionChoice(boolean autoReminderOptionChoice) {
		this.autoReminderOptionChoice = autoReminderOptionChoice;
	}
	
	public boolean getUserIdInputModeOptionChoice() {
		return userIdInputModeOptionChoice;
	}

	public void setUserIdInputModeOptionChoice(boolean userIdInputModeOptionChoice) {
		this.userIdInputModeOptionChoice = userIdInputModeOptionChoice;
	}

	/*Overwrite default one*/
	public boolean getSignupAttachmentEmpty(){
		if (this.readyToModifyAttachmentCopyList ==null || this.readyToModifyAttachmentCopyList.isEmpty())
			return true;
		else
			return false;
	}

	/**
	 * This is a getter for UI
	 * @return a list of SignupAttachment objects
	 */
	public List<SignupAttachment> getTempAttachmentCopyList() {
		return this.readyToModifyAttachmentCopyList;
	}

	public boolean getSomeoneSignedUp() {
		return someoneSignedUp;
	}

}
