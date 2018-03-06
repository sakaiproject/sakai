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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

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
import org.sakaiproject.signup.tool.jsf.TimeslotWrapper;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateMeetings;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateSitesGroups;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.util.DateFormatterUtil;

/**
 * <p>
 * This JSF UIBean class will handle information exchanges between Organizer's
 * copy meeting page:<b>copyMeeting.jsp</b> and backbone system.
 * 
 * @author Peter Liu
 * 
 * </P>
 */
@Slf4j
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
	
	//Meeting title
	private String title;
	
	//Location selected from the dropdown
	private String selectedLocation;
	
	//Category selected from the dropdown
	private String selectedCategory;
	
	//from the dropdown
	private String creatorUserId;

	private Date repeatUntil;

	private String repeatType;
	
	/* 0 for num of repeat, 1 for date choice*/
	private String recurLengthChoice;
	
	private int occurrences;

	//private int timeSlotDuration;

	private int numberOfSlots;

	private boolean showAttendeeName;

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
	
	private UserDefineTimeslotBean userDefineTimeslotBean;
	
	//discontinued time slots case
	private List<TimeslotWrapper> customTimeSlotWrpList;
	
	private boolean userDefinedTS=false;
	
	protected static boolean NEW_MEETING_SEND_EMAIL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal(
			"signup.email.notification.mandatory.for.newMeeting", "true")) ? true : false;
	
	private boolean mandatorySendEmail = NEW_MEETING_SEND_EMAIL;
	
 	private List<SelectItem> categories = null;
 	private List<SelectItem> locations=null;

	private String startTimeString;
	private String endTimeString;
	private String repeatUntilString;
	private static String HIDDEN_ISO_STARTTIME = "startTimeISO8601";
	private static String HIDDEN_ISO_ENDTIME = "endTimeISO8601";
	private static String HIDDEN_ISO_UNTILTIME = "untilISO8601";

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
		if(NEW_MEETING_SEND_EMAIL){
			//mandatory send email out
			sendEmail= true;
		}
		
		//sendEmailAttendeeOnly = false;
		sendEmailToSelectedPeopleOnly = DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;
		publishToCalendar= DEFAULT_EXPORT_TO_CALENDAR_TOOL;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		repeatUntil = calendar.getTime();
		recurLengthChoice="1";//0 for num of repeat, 1 for date choice
		occurrences=0;
		repeatType = ONCE_ONLY;
		repeatTypeUnknown=true;
		showAttendeeName = false;
		missingSitGroupWarning = false;
		
		/*cleanup previously unused attachments in CHS*/
		if(this.signupMeeting !=null)
			cleanUpUnusedAttachmentCopies(this.signupMeeting.getSignupAttachments());

		/*refresh copy of original*/
		this.signupMeeting = signupMeetingService.loadSignupMeeting(meetingWrapper.getMeeting().getId(), sakaiFacade
				.getCurrentUserId(), sakaiFacade.getCurrentLocationId());

		/*get meeting title*/
		title = this.signupMeeting.getTitle();

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
		
		//populate location and cateogry data for new meeting
		//since it's copymeeting, the dropdown selections should have it already there.
		this.selectedLocation=this.signupMeeting.getLocation();
		this.selectedCategory = this.signupMeeting.getCategory();
		this.customLocation="";
		this.customCategory="";
		this.categories = null;
		this.locations = null;

		populateDataForBeginDeadline(this.signupMeeting);
		
		/*Case: recurrence events*/
		prepareRecurredEvents();

		/* Initialize site/groups for current organizer */
		initializeSitesGroups();
		
		/* custom-ts case */
		this.customTimeSlotWrpList = null;
		this.userDefinedTS = false;
		/*populate timeslot data*/
		updateTimeSlotWrappers(this.meetingWrapper);
		if(CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType())){
			this.userDefinedTS=true;
			this.customTimeSlotWrpList= getTimeslotWrappers();
			markerTimeslots(this.customTimeSlotWrpList);
		}
			
		getUserDefineTimeslotBean().init(this.signupMeeting, COPTY_MEETING_PAGE_URL, this.customTimeSlotWrpList, UserDefineTimeslotBean.COPY_MEETING);

		
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
		
		/*user readability case for big numbers of minutes*/
		if(MINUTES.equals(this.signupBeginsType) && sMeeting.getSignupBegins().before(new Date())
				&& this.signupBegins > 500){
			/*we assume it has to be 'start now' before and we convert it to round to days*/			
			this.signupBeginsType=DAYS;
			this.signupBegins = Utilities.getRelativeTimeValue(DAYS, signupBeginBeforMeeting);
			if(this.signupBegins == 0)
				this.signupBegins = 1; //add a day					
		}

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
			return COPTY_MEETING_PAGE_URL;
		}

		SignupMeeting sMeeting = getSignupMeeting();
		try {
			prepareCopy(sMeeting);

			sMeeting.setRepeatUntil(getRepeatUntil());
			int repeatNum = getOccurrences();
			if("1".equals(getRecurLengthChoice())){
				repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), sMeeting.getStartTime(),
					getRepeatUntil());
			}
			sMeeting.setRepeatNum(repeatNum);
			sMeeting.setRepeatType(getRepeatType());
			
			if(CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType())){
				boolean multipleCalBlocks = getUserDefineTimeslotBean().getPutInMultipleCalendarBlocks();
				sMeeting.setInMultipleCalendarBlocks(multipleCalBlocks);
			}
			
			/*pass who are receiving emails*/
			sMeeting.setSendEmailToSelectedPeopleOnly(getSendEmailToSelectedPeopleOnly());
			
			CreateMeetings createMeeting = new CreateMeetings(sMeeting, sendEmail, keepAttendees
					&& !assignParicitpantsToAllRecurEvents, keepAttendees && assignParicitpantsToAllRecurEvents,
					getSignupBegins(), getSignupBeginsType(), getDeadlineTime(), getDeadlineTimeType(), getRecurLengthChoice(), sakaiFacade,
					signupMeetingService, getAttachmentHandler(), sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), true);

			createMeeting.setPublishToCalendar(isPublishToCalendar());
			createMeeting.processSaveMeetings();
			
			/*make sure that they don't get cleaned up in CHS when saved successfully*/
			this.signupMeeting.getSignupAttachments().clear();

		} catch (PermissionException e) {
			log.info(Utilities.rb.getString("no.permission_create_event") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("no.permission_create_event"));
			return ORGANIZER_MEETING_PAGE_URL;
		} catch (SignupUserActionException ue) {
			Utilities.addErrorMessage(ue.getMessage());
			return COPTY_MEETING_PAGE_URL;
		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
			return ORGANIZER_MEETING_PAGE_URL;
		}
		
		getUserDefineTimeslotBean().reset(UserDefineTimeslotBean.COPY_MEETING);
		
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
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String isoStartTime = params.get(HIDDEN_ISO_STARTTIME);

		if(DateFormatterUtil.isValidISODate(isoStartTime)){
			this.signupMeeting.setStartTime(DateFormatterUtil.parseISODate(isoStartTime));
		}

		String isoEndTime = params.get(HIDDEN_ISO_ENDTIME);

		if(DateFormatterUtil.isValidISODate(isoEndTime)){
			this.signupMeeting.setEndTime(DateFormatterUtil.parseISODate(isoEndTime));
		}

		String isoUntilTime = params.get(HIDDEN_ISO_UNTILTIME);

		if(DateFormatterUtil.isValidISODate(isoUntilTime)){
			setRepeatUntil(DateFormatterUtil.parseISODate(isoUntilTime));
		}
		Date eventEndTime = signupMeeting.getEndTime();
		Date eventStartTime = signupMeeting.getStartTime();
		
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
			eventEndTime= getUserDefineTimeslotBean().getEventEndTime();
			eventStartTime = getUserDefineTimeslotBean().getEventStartTime();		
			/*pass the value since they may be null*/
			this.signupMeeting.setStartTime(eventStartTime);
			this.signupMeeting.setEndTime(eventEndTime);
			
			if(getUserDefineTimeslotBean().getDestTSwrpList()==null || getUserDefineTimeslotBean().getDestTSwrpList().isEmpty()){
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("event.create_custom_defined_TS_blocks"));
				return;
			}
				
		}
		
		if (eventEndTime.before(eventStartTime) || eventStartTime.equals(eventEndTime)) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("event.endTime_should_after_startTime"));
			return;
		}

		if (!(getRepeatType().equals(ONCE_ONLY))) {
			int repeatNum = getOccurrences();
			if("1".equals(getRecurLengthChoice())){
				repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), eventStartTime,
					getRepeatUntil());
			}

			if ((DAILY.equals(getRepeatType())|| WEEKDAYS.equals(getRepeatType())) && isMeetingOverRepeatPeriod(eventStartTime, eventEndTime, 1)) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.daily.problem"));
				return;
			}
			
			if (WEEKLY.equals(getRepeatType()) && isMeetingOverRepeatPeriod(eventStartTime, eventEndTime, 7)) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.weekly.problem"));
				return;
			}
			
			if (BIWEEKLY.equals(getRepeatType()) && isMeetingOverRepeatPeriod(eventStartTime, eventEndTime, 14)) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("crossDay.event.repeat.biweekly.problem"));
				return;
			}

			if (repeatNum < 1) {
				validationError = true;
				if("1".equals(getRecurLengthChoice()))
					Utilities.addErrorMessage(Utilities.rb.getString("event.repeatbeforestart"));
				else
					Utilities.addErrorMessage(Utilities.rb.getString("event.repeatNnum.bigger.than.one"));
				
				return;
			}
		}

		if (!CreateSitesGroups.isAtleastASiteOrGroupSelected(this.getCurrentSite(), this.getOtherSites())) {
			validationError = true;
			Utilities.addErrorMessage(Utilities.rb.getString("select.atleast.oneGroup.for.copyMeeting"));

		}
		
		/*for custom defined time slot case*/
		if(!validationError && isUserDefinedTS()){
			this.signupMeeting.setStartTime(eventStartTime);
			this.signupMeeting.setEndTime(eventEndTime);
			this.signupMeeting.setMeetingType(CUSTOM_TIMESLOTS);
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
		}
		else{
			this.signupMeeting.setCategory(getCustomCategory());
		}
		//clear the category fields???
		this.selectedCategory="";
		
		//set the creator/organiser
		this.signupMeeting.setCreatorUserId(creatorUserId);
		this.creatorUserId="";

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
		getUserDefineTimeslotBean().reset(UserDefineTimeslotBean.COPY_MEETING);
		this.selectedLocation=null; //Reset selected option
		this.selectedCategory=null; //Reset selected option
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
	
	/**
	 * Modify the existing time slot blocks
	 * @return String object for next page url
	 */
	public String editUserDefTimeSlots(){
		if(this.customTimeSlotWrpList == null){
			/*initialize when it comes from other meeting type*/
			this.customTimeSlotWrpList = getTimeslotWrappers();
			/*Mark the time slot sequence for recurring events changes issues*/
			markerTimeslots(this.customTimeSlotWrpList);
			
			getUserDefineTimeslotBean().init(this.signupMeeting, COPTY_MEETING_PAGE_URL,this.customTimeSlotWrpList, UserDefineTimeslotBean.COPY_MEETING);
		}else{	
			if(!Utilities.isDataIntegritySafe(isUserDefinedTS(),UserDefineTimeslotBean.COPY_MEETING,getUserDefineTimeslotBean())){
				return ORGANIZER_MEETING_PAGE_URL;
			}
			
			this.customTimeSlotWrpList = getUserDefineTimeslotBean().getDestTSwrpList();
			getUserDefineTimeslotBean().init(this.signupMeeting, COPTY_MEETING_PAGE_URL,this.customTimeSlotWrpList, UserDefineTimeslotBean.COPY_MEETING);
		}
		
		return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
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
			List<SignupTimeslot> cpTimeslotList = new ArrayList<SignupTimeslot>();
			List<SignupTimeslot> origTsList=null;
			if (!isUserDefinedTS() && (meeting.getMeetingType().equals(INDIVIDUAL) || meeting.getMeetingType().equals(GROUP))){
				origTsList = meeting.getSignupTimeSlots();

				SignupTimeslot origTs = null;
				
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
							//this.truncateAttendee = true; validate by javaScript
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
			}
				
			/*User defined time slots case*/
			if (meeting.getMeetingType().equals(CUSTOM_TIMESLOTS) || isUserDefinedTS()){
				UserDefineTimeslotBean uBean = getUserDefineTimeslotBean();
				if(uBean ==null || !uBean.COPY_MEETING.equals(uBean.getPlaceOrderBean())){
					throw new SignupUserActionException(MessageFormat.format(Utilities.rb.getString("you.have.multiple.tabs.in.browser"),
							new Object[]{getSakaiFacade().getServerConfigurationService().getServerName()}));
				}
				List<TimeslotWrapper> tsWrpList = uBean.getDestTSwrpList();
				if (tsWrpList != null){						
					for (TimeslotWrapper wrapper : tsWrpList) {
						SignupTimeslot slot = wrapper.getTimeSlot();
						
						List<SignupAttendee> attList = slot.getAttendees();
						/* screening attendees */
						removeNotAllowedAttedees(attList);
						
						if (attList != null && attList.size() > slot.getMaxNoOfAttendees()) {
							/* attendee may be truncated */
							for (int j = attList.size(); j > slot.getMaxNoOfAttendees(); j--)
								attList.remove(j - 1);
						}
						
						if(slot.isLocked() || slot.isCanceled())
							lockOrCanceledTimeslot = true;
						
						cpTimeslotList.add(slot);
					}
				}
				
				/*for end time purpose*/
				int duration = getUserDefineTimeslotBean().getEventDuration();
				calendar.add(Calendar.MINUTE, duration);				
			}
				

			meeting.setSignupTimeSlots(cpTimeslotList);// pass over

			if (lockOrCanceledTimeslot)
				Utilities.addErrorMessage(Utilities.rb.getString("warning.some_timeslot_may_locked_canceled"));
		}

		meeting.setEndTime(calendar.getTime());

		/* setup signup begin / deadline */
		setSignupBeginDeadlineData(meeting, getSignupBegins(), getSignupBeginsType(), getDeadlineTime(),
				getDeadlineTimeType());

		// copySites(meeting);
		
		/*Remove the coordinates who are not in the meeting any more due to the site group changes
		 * we are simplify and just copy over coordinators over and user can change it via modify meeting page*/
		//TODO later we may add the coordinators ability in the copy page too and need ajax to the trick.
		meeting.setCoordinatorIds(getValidatedMeetingCoordinators(meeting));

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
 		if(categories == null){
 			categories= new ArrayList<SelectItem>();
 			categories.addAll(Utilities.getSignupMeetingsBean().getAllCategories());
 			//remove option 'All'
 			categories.remove(0);
 			categories.add(0, new SelectItem(Utilities.rb.getString("select_category")));
 		}
 		return categories;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
	
	
	public String getselectedCategory() {
		return selectedCategory;
	}
	public void setselectedCategory(String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}
	
	public String getcreatorUserId() {
		if(this.creatorUserId ==null){
			//set current user as default meeting organizer if case people forget to select one
			return sakaiFacade.getCurrentUserId();
		}
		return creatorUserId;
	}
	public void setcreatorUserId(String creatorUserId) {
		this.creatorUserId=creatorUserId;
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

	public String getRepeatUntilString() {
		return repeatUntilString;
	}

	public void setRepeatUntilString(String repeatUntilString) {
		this.repeatUntilString = repeatUntilString;
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

	/*public void setTimeSlotDuration(int timeSlotDuration) {
		this.timeSlotDuration = timeSlotDuration;
	}*/

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
	
	private String getValidatedMeetingCoordinators(SignupMeeting meeting){
		List<String> allCoordinatorIds = meeting.getCoordinatorIdsList();
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (String couId : allCoordinatorIds) {
			if(this.sakaiFacade.hasPermissionToCreate(meeting,couId)){
				if(isFirst){
					sb.append(couId);
					isFirst = false;
				}else{
					//safeguard -db column max size, hardly have over 10 coordinators per meeting
					if(sb.length() < 1000)
						sb.append("|" + couId);
				}
			}
		}
		
		return sb.length()<1? null : sb.toString();
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
		/*to see if the recurring events have a 'Start_Now' type already*/
		if(Utilities.testSignupBeginStartNowType(upTodateOrginMeetings)){
			setSignupBeginsType(START_NOW);//overwrite previous value
			setSignupBegins(6);//default value; not used
		}
		 
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

	public UserDefineTimeslotBean getUserDefineTimeslotBean() {
		return userDefineTimeslotBean;
	}

	public void setUserDefineTimeslotBean(UserDefineTimeslotBean userDefineTimeslotBean) {
		this.userDefineTimeslotBean = userDefineTimeslotBean;
	}

	public boolean isUserDefinedTS() {
		return userDefinedTS;
	}

	public void setUserDefinedTS(boolean userDefinedTS) {
		this.userDefinedTS = userDefinedTS;
	}

	public List<TimeslotWrapper> getCustomTimeSlotWrpList() {
		return customTimeSlotWrpList;
	}

	public void setCustomTimeSlotWrpList(List<TimeslotWrapper> customTimeSlotWrpList) {
		this.customTimeSlotWrpList = customTimeSlotWrpList;
	}
	
	/**
	 * This is only for UI purpose to check if the event/meeting is an
	 * custom-ts style (manay time slots) and it requires signup.
	 */
	public boolean getCustomTsType() {
		return CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType());
	}

	public String getRecurLengthChoice() {
		return recurLengthChoice;
	}

	public void setRecurLengthChoice(String recurLengthChoice) {
		this.recurLengthChoice = recurLengthChoice;
	}

	public int getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(int occurrences) {
		this.occurrences = occurrences;
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
	
	/**
	 * This is for UI page to determine whether the email checkbox should be checked and disabled to change
	 * @return
	 */
	public boolean isMandatorySendEmail(){
		return this.mandatorySendEmail;
	}
	
}
