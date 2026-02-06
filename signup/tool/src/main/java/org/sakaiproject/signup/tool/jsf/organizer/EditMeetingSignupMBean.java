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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.api.SignupEventTypes;
import org.sakaiproject.signup.api.SignupUser;
import org.sakaiproject.signup.api.SignupUserActionException;
import org.sakaiproject.signup.api.model.SignupAttachment;
import org.sakaiproject.signup.api.model.SignupAttendee;
import org.sakaiproject.signup.api.model.SignupGroup;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupSite;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupSiteWrapper;
import org.sakaiproject.signup.tool.jsf.SignupUIBaseBean;
import org.sakaiproject.signup.tool.jsf.TimeslotWrapper;
import org.sakaiproject.signup.tool.jsf.organizer.action.EditMeeting;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.DateFormatterUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * modify meeting page:<b>modifyMeeting.jsp</b> and backbone system.
 * 
 * @author Peter Liu
 * 
 * </P>
 */
@Slf4j
public class EditMeetingSignupMBean extends SignupUIBaseBean {

	private SignupMeeting signupMeeting;

	private int maxNumOfAttendees;

	private boolean showAttendeeName;

	// private int addMoreTimeslots;
	
	//Meeting title
	private String title;

	//Location selected from the dropdown
	private String selectedLocation;
	
	//Category selected from the dropdown
	private String selectedCategory;
	
	//from the dropdown
	private String creatorUserId;
	
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

	private UserDefineTimeslotBean userDefineTimeslotBean;
	
	//discontinued time slots case
	private List<TimeslotWrapper> customTimeSlotWrpList;
	
	private boolean userDefinedTS=false;
	
	List<SelectItem> slots;
	
	List<SignupUser> allPossibleCoordinators;
	
	private boolean sendEmailByOwner;
	
 	private List<SelectItem> categories = null;
 	private List<SelectItem> locations=null;
		
	private String startTimeString;
	private String endTimeString;
	private static String HIDDEN_ISO_STARTTIME = "startTimeISO8601";
	private static String HIDDEN_ISO_ENDTIME = "endTimeISO8601";

	private SignupSiteWrapper currentSite;
	private List<SignupSiteWrapper> otherSites;

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
		//sendEmailAttendeeOnly = false;
		sendEmailToSelectedPeopleOnly = DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;
		
		unlimited = false;

		editMeeting = null;

		convertToNoRecurrent = false;
		
		
		
		/*refresh copy of original*/
		this.signupMeeting = reloadMeeting(meetingWrapper.getMeeting());

		/*get meeting title*/
		title = this.signupMeeting.getTitle();

		/*get meeting default notification value*/
		sendEmail =this.signupMeeting.isSendEmailByOwner();
		//pass default value
		this.sendEmailByOwner = this.signupMeeting.isSendEmailByOwner();
		
		/* for check pre-condition purpose */
		this.originalMeetingCopy = reloadMeeting(meetingWrapper.getMeeting());
		// keep the last version need a deep copy?
		
		/*publish to calendar tool */
		this.publishToCalendar= getOriginalMeetingCalendarPublishInfo(this.signupMeeting);

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
		this.someoneSignedUp = initSomeoneSignupInfo();
		
		/* custom-ts case */
		this.customTimeSlotWrpList = null;
		this.userDefinedTS = false;
		updateTimeSlotWrappers(this.meetingWrapper);
		if(CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType())){
			this.userDefinedTS=true;
			this.customTimeSlotWrpList= getTimeslotWrappers();
			markerTimeslots(this.customTimeSlotWrpList);
			//getUserDefineTimeslotBean().setSomeoneSignedUp(this.someoneSignedUp);
		}
			
		getUserDefineTimeslotBean().init(this.signupMeeting, MODIFY_MEETING_PAGE_URL, this.customTimeSlotWrpList, UserDefineTimeslotBean.MODIFY_MEETING);
		populateDropDown();	
		
		//populate organizer data
		this.creatorUserId = this.signupMeeting.getCreatorUserId();
		
		//populate location and cateogry data for new meeting
		//since it's modifying meeting, the dropdown selections should have it already there.
		this.selectedLocation=this.signupMeeting.getLocation();
		this.selectedCategory = this.signupMeeting.getCategory();
		this.customLocation="";
		this.customCategory="";
		this.categories = null;
		this.locations = null;
		
		/*pre-load all possible coordinators for step2*/
		this.allPossibleCoordinators = this.sakaiFacade.getAllPossbileCoordinatorsOnFastTrack(this.signupMeeting);
		populateExistingCoordinators();

	}

	/* get the relative time out */
	private int copyDisplaySignupBegins;
	private String copyDisplaySignupBeginsType;
	private int originalSignupBegins;
	private String originalSignupBeginsType;
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
		
		/*user readability case for 'start now'*/		
		this.originalSignupBegins=this.signupBegins;
		this.originalSignupBeginsType=this.signupBeginsType;		
		if(this.signupBegins < 0)
			this.signupBegins = 0;//negative number is not allowed
		
		if(MINUTES.equals(this.signupBeginsType) && sMeeting.getSignupBegins().before(new Date())
				&& this.signupBegins > 500){
			/*we assume it has to be 'start now' before and we convert it to round to days*/			
			this.signupBeginsType=DAYS;
			this.signupBegins = Utilities.getRelativeTimeValue(DAYS, signupBeginBeforMeeting);
			if(this.signupBegins == 0)
				this.signupBegins = 1; //add a day					
		}
		/*keep a copy to detect if user has modified this field*/
		this.copyDisplaySignupBegins=this.signupBegins;
		this.copyDisplaySignupBeginsType=this.signupBeginsType;

	}
		
	void populateDropDown(){
		slots = new ArrayList<SelectItem>();
		for (int i =1; i <= numberOfSlots;i++) slots.add(new SelectItem(i, i+""));
	}
	
	boolean hasUserChangedSignupBeginTime(){
		if(this.copyDisplaySignupBegins == getSignupBegins() && getSignupBeginsType().equals(this.copyDisplaySignupBeginsType))
			return false;
		else
			return true;
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
	
	private boolean getOriginalMeetingCalendarPublishInfo(SignupMeeting sm){
		this.publishToCalendar= DEFAULT_EXPORT_TO_CALENDAR_TOOL;
		List<SignupSite> sites = sm.getSignupSites();
		if(sites !=null && !sites.isEmpty()){
			for (SignupSite s : sites) {
				if(s.getCalendarEventId() !=null)
					return true;
				
				List<SignupGroup> grps = s.getSignupGroups();
				if(grps !=null && !grps.isEmpty()){
					for (SignupGroup g : grps) {
						if(g.getCalendarEventId()!=null)
							return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public String doCancelAction(){
		cleanUpUnusedAttachmentCopies(this.readyToModifyAttachmentCopyList);
		getUserDefineTimeslotBean().reset(UserDefineTimeslotBean.MODIFY_MEETING);
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
			return MODIFY_MEETING_PAGE_URL;
		}

		try {
			SignupMeeting meeting = getSignupMeeting();

			EditMeeting editMeeting = new EditMeeting(getSakaiFacade().getCurrentUserId(), getSakaiFacade()
					.getCurrentLocationId(), getSignupMeetingService(),getAttachmentHandler(), true);
			/* Pass modified data */
			editMeeting.setCurrentNumberOfSlots(getNumberOfSlots());// (getAddMoreTimeslots());
			if(hasUserChangedSignupBeginTime()){
				editMeeting.setSignupBegins(getSignupBegins());
				editMeeting.setSignupBeginsType(getSignupBeginsType());
				editMeeting.setSignupBeginModifiedByUser(true);
			}else{
				/*no changes*/
				editMeeting.setSignupBegins(this.originalSignupBegins);
				editMeeting.setSignupBeginsType(this.originalSignupBeginsType);
				editMeeting.setSignupBeginModifiedByUser(false);
			}
			editMeeting.setDeadlineTime(getDeadlineTime());
			editMeeting.setDeadlineTimeType(getDeadlineTimeType());
			editMeeting.setTimeSlotDuration(getTimeSlotDuration());
			editMeeting.setMaxNumOfAttendees(getMaxNumOfAttendees());
			editMeeting.setShowAttendeeName(isShowAttendeeName());
			editMeeting.setOriginalMeetingCopy(this.originalMeetingCopy);
			editMeeting.setUnlimited(isUnlimited());
			editMeeting.setSendEmailByOwner(isSendEmailByOwner());//set default value
			editMeeting.setCoordinators(Utilities.getSelectedCoordinators(this.allPossibleCoordinators,this.creatorUserId));
			// editMeeting.setTotalEventDuration(getTotalEventDuration());
			/* disable the association with other related recurrence events */
			editMeeting.setConvertToNoRecurrent(convertToNoRecurrent);
			
			//signup-51 send sakaifacade into the edit meetings wrapper
			editMeeting.setSakaiFacade(getSakaiFacade());
			
			//signup-51 set the value in edit meetings so we can process the timeslot groups if needed
			editMeeting.setCreateGroups(meeting.isCreateGroups());

						
			/* Case: custom defined TS */
			if(isUserDefinedTS()){
				if(this.customTimeSlotWrpList !=null){
					editMeeting.setUserDefinedTS(true);
					editMeeting.setUserDefineTimeslotBean(getUserDefineTimeslotBean());
					editMeeting.setCustomTimeSlotWrpList(getUserDefineTimeslotBean().getDestTSwrpList());
				}else{
					/*user never has initialized for custom-ts case(by click the edit link)
					 * So we will treat it as original meeting type*/
					//editMeeting.setCustomTimeSlotWrpList(getTimeslotWrappers());
					editMeeting.setUserDefinedTS(false);
				}
			}
			
			/*set latest attachments changes*/
			/*pre-check if there is any attachment changes*/
			if(!areAttachmentChanges()) {
				editMeeting.setCurrentAttachList(null);
			} else {
				editMeeting.setCurrentAttachList(this.readyToModifyAttachmentCopyList);
			}
			
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
			Utilities.postEventTracking(SignupEventTypes.EVENT_SIGNUP_MTNG_MODIFY, ToolManager.getCurrentPlacement().getContext(),
					meeting.getId(), meeting.getTitle(), "");

			if (meeting.getRecurrenceId() != null) {
				Utilities.resetMeetingList();// refresh main-page to catch
				// the changes
			}

			if (sendEmail) {
				try {
					SignupMeeting sm = successUpdatedMeetings.get(0);
					sm.setSendEmailToSelectedPeopleOnly(getSendEmailToSelectedPeopleOnly());
					signupMeetingService.sendEmail(sm, SIGNUP_MEETING_MODIFIED);
					/* send email to promoted waiter if size increased 
					 * or send email to notify attedees in a deleted TS*/
					/*
					 * TODO Not for recurring meetings yet since it's not traced for all
					 * of them yet
					 */
					if (successUpdatedMeetings.get(0).getRecurrenceId() == null) {
						signupMeetingService.sendEmailToParticipantsByOrganizerAction(editMeeting
								.getSignupEventTrackingInfo());
					}
				} catch (Exception e) {
					log.error(Utilities.rb.getString("email.exception") + " - " + e.getMessage(), e);
					Utilities.addErrorMessage(Utilities.rb.getString("email.exception"));
				}
			}
			
			if(isPublishToCalendar()){
				for (SignupMeeting savedMeeting : successUpdatedMeetings) {
					try {
						if(CUSTOM_TIMESLOTS.equals(savedMeeting.getMeetingType())){
							boolean multipleCalBlocks = getUserDefineTimeslotBean().getPutInMultipleCalendarBlocks();
							savedMeeting.setInMultipleCalendarBlocks(multipleCalBlocks);
						}
						
						signupMeetingService.modifyCalendar(savedMeeting);
	
					} catch (PermissionException pe) {
						Utilities.addErrorMessage(Utilities.rb
								.getString("error.calendarEvent.updated_failed_due_to_permission"));
						log.debug(Utilities.rb.getString("error.calendarEvent.updated_failed_due_to_permission")
								+ " - Meeting title:" + savedMeeting.getTitle());
					} catch (Exception e) {
						Utilities.addErrorMessage(Utilities.rb.getString("error.calendarEvent.updated_failed"));
						log.warn(Utilities.rb.getString("error.calendarEvent.updated_failed") + " - Meeting title:"
								+ savedMeeting.getTitle());
					}
				}
			}
			else{
				/*remove calendar if any*/
				signupMeetingService.removeCalendarEventsOnModifiedMeeting(successUpdatedMeetings);
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
		reloadMeetingWrapperInOrganizerPage();
		
		cleanUpUnusedAttachmentCopies(this.readyToModifyAttachmentCopyList);//using browser back click
		getUserDefineTimeslotBean().reset(UserDefineTimeslotBean.MODIFY_MEETING);
		
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
		//update latest creator for UI
		this.meetingWrapper.setCreator(sakaiFacade.getUserDisplayName(meeting.getCreatorUserId()));
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

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String isoStartTime = params.get(HIDDEN_ISO_STARTTIME);

		if(DateFormatterUtil.isValidISODate(isoStartTime)){
			this.signupMeeting.setStartTime(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoStartTime));
		}

		String isoEndTime = params.get(HIDDEN_ISO_ENDTIME);

		if(DateFormatterUtil.isValidISODate(isoEndTime)){
			this.signupMeeting.setEndTime(sakaiFacade.getTimeService().parseISODateInUserTimezone(isoEndTime));
		}

		Date eventEndTime = this.signupMeeting.getEndTime();
		Date eventStartTime = this.signupMeeting.getStartTime();

		//Set Title		
		if (StringUtils.isNotBlank(title)){
			log.debug("title set: " + title);
			this.signupMeeting.setTitle(title);
		}else{
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("event.title_cannot_be_blank"));
			return;
		}

		/*user defined own TS case*/
		if(isUserDefinedTS()){
			if(getUserDefineTimeslotBean().getDestTSwrpList()==null || getUserDefineTimeslotBean().getDestTSwrpList().isEmpty()){
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("event.create_custom_defined_TS_blocks"));
				return;
			} else {
				eventEndTime= getUserDefineTimeslotBean().getEventEndTime();
				eventStartTime = getUserDefineTimeslotBean().getEventStartTime();
			}
				
		}
		
		if (eventEndTime.before(eventStartTime) || eventStartTime.equals(eventEndTime)) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("event.endTime_should_after_startTime"));
			return;
		}
		
		/*for custom defined time slot case*/
		if(!validationError && isUserDefinedTS()){
			this.signupMeeting.setStartTime(eventStartTime);
			this.signupMeeting.setEndTime(eventEndTime);
			this.signupMeeting.setMeetingType(CUSTOM_TIMESLOTS);
		}
				
		if ((DAILY.equals(this.signupMeeting.getRepeatType())|| WEEKDAYS.equals(this.signupMeeting.getRepeatType())) && isMeetingOverRepeatPeriod(this.signupMeeting.getStartTime(),this.signupMeeting.getEndTime(), 1)) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.daily.problem"));
			return;
		}
		
		if (WEEKLY.equals(this.signupMeeting.getRepeatType()) && isMeetingOverRepeatPeriod(this.signupMeeting.getStartTime(), this.signupMeeting.getEndTime(), 7)) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.weekly.problem"));
			return;
		}
		
		if (BIWEEKLY.equals(this.signupMeeting.getRepeatType()) && isMeetingOverRepeatPeriod(this.signupMeeting.getStartTime(), this.signupMeeting.getEndTime(), 14)) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.biweekly.problem"));
			return;
		}
		
		int timeduration = getTimeSlotDuration();
		if (timeduration < 1) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("event.timeslot_duration_should_not_lessThan_one"));
			return;
		}
		
		//Set Location		
		if (StringUtils.isBlank(getCustomLocation())){
			if (StringUtils.isBlank(selectedLocation) || selectedLocation.equals(Utilities.rb.getString("select_location"))){
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("event.location_not_assigned"));
				return;
			}
			this.signupMeeting.setLocation(selectedLocation);
			
		}
		else{
			this.signupMeeting.setLocation(getCustomLocation());
		}
		//clear the location fields???
		this.selectedLocation="";
		
		//Set Category
		//if textfield is blank, check the dropdown
		if (StringUtils.isBlank(getCustomCategory())){
			//if dropdown is not the default, then use its value
			if(!StringUtils.equals(selectedCategory, Utilities.rb.getString("select_category"))) {
					this.signupMeeting.setCategory(selectedCategory);
			}
		}else{
			this.signupMeeting.setCategory(getCustomCategory());
		}
		//clear the category fields??
		this.selectedCategory="";
		
		//set the creator/organiser
		this.signupMeeting.setCreatorUserId(creatorUserId);
		this.creatorUserId="";

		// Need to filter for bad HTML
		String filteredDescription = sakaiFacade.getFormattedText()
				.processFormattedText(this.signupMeeting.getDescription(), null, true);
		this.signupMeeting.setDescription(filteredDescription);

	}
	
	/**
	 * Modify the existing time slot blocks
	 * @return String object for next page url
	 */
	public String editUserDefTimeSlots(){
		if(this.customTimeSlotWrpList == null){
			/* need initialization when it comes from other meeting type*/
			this.customTimeSlotWrpList = getTimeslotWrappers();
			/*Mark the time slot sequence for recurring events changes issues*/
			markerTimeslots(this.customTimeSlotWrpList);
			getUserDefineTimeslotBean().init(this.signupMeeting, MODIFY_MEETING_PAGE_URL,this.customTimeSlotWrpList, UserDefineTimeslotBean.MODIFY_MEETING);
		}else{	
			if(!Utilities.isDataIntegritySafe(isUserDefinedTS(),UserDefineTimeslotBean.MODIFY_MEETING,getUserDefineTimeslotBean())){
				return ORGANIZER_MEETING_PAGE_URL;
			}
			
			this.customTimeSlotWrpList = getUserDefineTimeslotBean().getDestTSwrpList();
			getUserDefineTimeslotBean().init(this.signupMeeting, MODIFY_MEETING_PAGE_URL,this.customTimeSlotWrpList, UserDefineTimeslotBean.MODIFY_MEETING);
		}
		
		return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
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
	 * This is getter method for UI
	 * @return a String for the meeting id
	 */
        public String getSignupMeetingId() {
	    String rv = "";
	    if (signupMeeting != null) {
		Long id = signupMeeting.getId();
		if (id != null) {
		    rv = id.toString();
		}
	    }
	    return rv;
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
 	 * This method is called to get all locations to populate the dropdown
 	 * 
 	 * @return list of allLocations
 	 */
 	public List<SelectItem> getAllLocations(){
 		if(locations ==null){
 			locations= new ArrayList<SelectItem>();
 			locations.addAll(Utilities.getSignupMeetingsBean().getAllLocations());
 			locations.add(0, new SelectItem(Utilities.rb.getString("select_location")));
 		}
 		return locations;
 	}
 	
 	/**
 	 * This method is called to get all categories to populate the dropdown
 	 * 
 	 * @return list of categories
 	 */
 	public List<SelectItem> getAllCategories(){
 		if(categories==null){
 			categories= new ArrayList<SelectItem>();
 			categories.addAll(Utilities.getSignupMeetingsBean().getAllCategories());
 			//remove option 'All'
 			categories.remove(0);
 			categories.add(0, new SelectItem(Utilities.rb.getString("select_category")));
 	}
 		return categories;
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
	 * This is a getter method to provide selected location.
	 * 
	 * @return String
	 */
	public String getselectedLocation() {
		return selectedLocation;
	}

	/**
	 * This is a setter.
	 * 
	 * @param selectedLoction
	 *           String that represents the selected location
	 */
	public void setselectedLocation(String selectedLocation) {
		this.selectedLocation = selectedLocation;
	}
	
	/**
	 * This is a getter method to provide selected category.
	 * 
	 * @return String
	 */
	public String getselectedCategory() {
		return selectedCategory;
	}
	
	/**
	 * This is a setter.
	 * 
	 * @param selectedCategory
	 *           String that represents the selected location
	 */
	public void setselectedCategory(String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}
	
	public String getcreatorUserId() {
		return creatorUserId;
	}
	public void setcreatorUserId(String creatorUserId) {
		this.creatorUserId=creatorUserId;
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
	
	/**
	 * This is only for UI purpose to check if the event/meeting is an
	 * custom-ts style (manay time slots) and it requires signup.
	 */
	public boolean getCustomTsType() {
		return CUSTOM_TIMESLOTS.equals(this.originalMeetingCopy.getMeetingType());
	}

	public UserDefineTimeslotBean getUserDefineTimeslotBean() {
		return userDefineTimeslotBean;
	}

	public void setUserDefineTimeslotBean(UserDefineTimeslotBean userDefineTimeslotBean) {
		this.userDefineTimeslotBean = userDefineTimeslotBean;
	}

	public List<TimeslotWrapper> getCustomTimeSlotWrpList() {
		return customTimeSlotWrpList;
	}

	public void setCustomTimeSlotWrpList(List<TimeslotWrapper> customTimeSlotWrpList) {
		this.customTimeSlotWrpList = customTimeSlotWrpList;
	}

	public boolean isUserDefinedTS() {
		return userDefinedTS;
	}

	public void setUserDefinedTS(boolean userDefinedTS) {
		this.userDefinedTS = userDefinedTS;
	}
	
	/**
	 * @return true if sakai property signup.enableAttendance is true, else will return false
	 */
	public boolean isAttendanceOn() {
		return Utilities.getSignupMeetingsBean().isAttendanceOn();
	}
	
	/**
	 * Get a list of users that have permission, but format it as a SelectItem list for the dropdown.
	 * Since this is a new item there will be no current instructor so it returns the current user at the top of the list
	 * We send a null signup meeting param as this is a new meeting.
	 */
	public List<SelectItem> getInstructors() {
		return Utilities.getSignupMeetingsBean().getInstructors(signupMeeting);
	}
	
	public List<SelectItem> getSlots() {
		return slots;
	}

	public void setSlots(List<SelectItem> slots) {
		this.slots = slots;
	}

	public List<SignupUser> getAllPossibleCoordinators() {
		return allPossibleCoordinators;
	}

	public void setAllPossibleCoordinators(List<SignupUser> allPossibleCoordinators) {
		this.allPossibleCoordinators = allPossibleCoordinators;
	}
	
	/* 
	 * get the list of coords and check the appropriate ones.
	 */
	private void populateExistingCoordinators(){
		List<String> existingCoUserIds = this.signupMeeting.getCoordinatorIdsList();
		for (SignupUser coord : allPossibleCoordinators) {
			if(existingCoUserIds.contains(coord.getInternalUserId())) {
				coord.setChecked(true);
			}
		}
	}

	public boolean isSendEmailByOwner() {
		return sendEmailByOwner;
	}

	public void setSendEmailByOwner(boolean sendEmailByOwner) {
		this.sendEmailByOwner = sendEmailByOwner;
	}
		
	public String getStartTimeString() {
		return startTimeString;
	}

	public String getEndTimeString() {
		return endTimeString;
	}

	public void setStartTimeString(String startTimeString) {
		this.startTimeString = startTimeString;
	}

	public void setEndTimeString(String endTimeString) {
		this.endTimeString = endTimeString;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<SignupSiteWrapper> getOtherSites() {
		if (otherSites == null) {
			otherSites = Utilities.getSignupMeetingsBean().getCreateSitesGroups().getOtherSites();
		}
		return otherSites;
	}

	public SignupSiteWrapper getCurrentSite() {
		if (currentSite == null) {
			currentSite = Utilities.getSignupMeetingsBean().getCreateSitesGroups().getCurrentSite();
		}
		return currentSite;
	}
}
