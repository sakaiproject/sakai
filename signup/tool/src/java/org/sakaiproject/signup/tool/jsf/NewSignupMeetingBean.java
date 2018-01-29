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

package org.sakaiproject.signup.tool.jsf;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.signup.logic.SignupUser;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.attachment.AttachmentHandler;
import org.sakaiproject.signup.tool.jsf.organizer.UserDefineTimeslotBean;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateMeetings;
import org.sakaiproject.signup.tool.jsf.organizer.action.CreateSitesGroups;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.DateFormatterUtil;

/**
 * <p>
 * This JSF UIBean class will handle the creation of different types of
 * event/meeting by Organizer It provides all the necessary business logic for
 * this process.
 * 
 * @author Peter Liu
 * 
 * </P>
 */
@Slf4j
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
	
	//Meeting title attribute
	private String title;
	
	//Location selected from the dropdown
	private String selectedLocation;
	
	//New Location added in the editable field
	private String customLocation;
	
	private List<SelectItem> locations=null;
	
	//Category selected from the dropdown
	private String selectedCategory;
	
	//New Category added in the editable field
	private String customCategory;
	
	private List<SelectItem> categories = null;

	private String repeatType;

	private Date repeatUntil;
	
	/* 0 for num of repeat, 1 for date choice*/
	private String recurLengthChoice;
	
	private int occurrences;

	private boolean assignParicitpantsToAllRecurEvents = false;

	/* sign up can start before this minutes/hours/days */
	private int signupBegins;

	private String deadlineTimeType;

	/* sign up deadline before this minutes/hours/days */

	private int deadlineTime;

	private SignupSiteWrapper currentSite;

	private List<SignupSiteWrapper> otherSites;

	private static boolean DEFAULT_SEND_EMAIL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.email.notification", "true")) ? true : false;

	protected boolean sendEmail = DEFAULT_SEND_EMAIL;

	private boolean receiveEmail;
	
	private boolean sendEmailByOwner;
	
	private static boolean DEFAULT_ALLOW_WAITLIST = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.allow.waitlist", "true")) ? true : false;
		
	private static boolean DEFAULT_ALLOW_COMMENT = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.allow.comment", "true")) ? true : false;
	
	private static boolean DEFAULT_AUTO_RIMINDER = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.event.default.auto.reminder", "true")) ? true : false;
	
	private static boolean DEFAULT_AUTO_RMINDER_OPTION_CHOICE = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.autoRiminder.option.choice.setting", "true")) ? true : false;
	
	private static boolean DEFAULT_USERID_INPUT_MODE_OPTION_CHOICE = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.userId.inputMode.choiceOption.setting", "true")) ? true : false;
	
	private static boolean DEFAULT_EXPORT_TO_CALENDAR_TOOL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.export.to.calendar.setting", "true")) ? true : false;

	private static boolean DEFAULT_CREATE_GROUPS = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.create.groups.setting", "true")) ? true : false;
	
	protected static String DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY = Utilities.getSignupConfigParamVal(
			"signup.default.email.selected", SEND_EMAIL_ALL_PARTICIPANTS, VALID_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY);

	protected static boolean NEW_MEETING_SEND_EMAIL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal(
			"signup.email.notification.mandatory.for.newMeeting", "true")) ? true : false;
	
	private boolean mandatorySendEmail = NEW_MEETING_SEND_EMAIL;
	
	private String sendEmailToSelectedPeopleOnly = DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;
	
	private boolean publishToCalendar = DEFAULT_EXPORT_TO_CALENDAR_TOOL;
	
	private boolean createGroups = DEFAULT_CREATE_GROUPS;
	
	private boolean allowWaitList = DEFAULT_ALLOW_WAITLIST;
	
	private boolean allowComment = DEFAULT_ALLOW_COMMENT;
	
	private boolean autoReminder = DEFAULT_AUTO_RIMINDER;
	
	private boolean autoReminderOptionChoice = DEFAULT_AUTO_RMINDER_OPTION_CHOICE;
	
	private boolean userIdInputModeOptionChoice = DEFAULT_USERID_INPUT_MODE_OPTION_CHOICE;

	private List<TimeslotWrapper> timeSlotWrappers;

	private List<SelectItem> meetingTypeRadioBttns;

	List<SignupUser> allSignupUsers;
	
	List<SignupUser> allPossibleCoordinators;

	private List<SelectItem> allAttendees;

	private UIInput newAttendeeInput;

	/* proxy param */
	private String eidOrEmailInputByUser;

	private UIData timeslotWrapperTable;

	private boolean showParticipants;

	private boolean validationError;

	private boolean eidInputMode = false;

	private Boolean publishedSite;
	
	private boolean endTimeAutoAdjusted=false;
	
	private List<SignupAttachment> attachments;
	
	private AttachmentHandler attachmentHandler;
	
	private UserDefineTimeslotBean userDefineTimeslotBean;
	
	//discontinued time slots case
	private List<TimeslotWrapper> customTimeSlotWrpList;
	
	private boolean otherSitesAvailability;
	
	private boolean userDefinedTS=false;	
	
	private String creatorUserId;
	
	private int maxSlots;
	
	private int maxAttendeesPerSlot;

	/* used for jsf parameter passing */
	private final static String PARAM_NAME_FOR_ATTENDEE_USERID = "attendeeUserId";

	/* to remember the number of the meeting slots */
	private int maxNumOfSlots;
	/* Used for populate the drop down box for the allowed number of meeting slots */
	private List<SelectItem> slots;
	
	private String startTimeString;
	private String endTimeString;
	private String repeatUntilString;
	private static String HIDDEN_ISO_STARTTIME = "startTimeISO8601";
	private static String HIDDEN_ISO_ENDTIME = "endTimeISO8601";
	private static String HIDDEN_ISO_UNTILTIME = "untilISO8601";
	
	public int getMaxNumOfSlots() {
		return maxNumOfSlots;
	}
	
	public void setMaxNumOfSlots(int preferredSlot) {
		this.maxNumOfSlots = preferredSlot;
	}
	
	public List<SelectItem> getSlots(){
		
		slots = new ArrayList<SelectItem>();
		for (int i =1; i <= numberOfSlots;i++) slots.add(new SelectItem(i, i+""));
		return slots;
	}
	
	public String getCurrentUserDisplayName() {
		return sakaiFacade.getUserDisplayName(sakaiFacade.getCurrentUserId());
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getselectedLocation() {
		return selectedLocation;
	}

	public void setselectedLocation(String selectedLocation) {
		this.selectedLocation = selectedLocation;
	}
	
	public String getcustomLocation() {
		return customLocation;
	}

	public void setcustomLocation(String customLocation) {
		this.customLocation = customLocation;
	}
	
	public String getselectedCategory() {
		return selectedCategory;
	}

	public void setselectedCategory(String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}
	
	public String getcustomCategory() {
		return customCategory;
	}

	public void setcustomCategory(String customCategory) {
		this.customCategory = customCategory;
	}
	
	public String getCreatorUserId() {
		if(this.creatorUserId ==null){
			//set current user as default meeting organizer if case people forget to select one
			return sakaiFacade.getCurrentUserId();
		}
		return creatorUserId;
	}

	public void setCreatorUserId(String creatorUserId) {
		this.creatorUserId = creatorUserId;
	}
	
	public boolean isOtherSitesAvailability() {
		
		//checking for tool property, if it doesn't exist,take value as default
		String toolProperty = sakaiFacade.getToolManager().getCurrentPlacement().getConfig().getProperty("signup.other.sites.availability", "default");
		if (toolProperty.equals("default")) {
			//If tool property is not set, then consider sakai property
			String sakaiProperty = Boolean.valueOf(sakaiFacade.getServerConfigurationService().getBoolean("signup.otherSitesAvailability", true)).toString();
			toolProperty = sakaiProperty;
		}
		//tool property would take precedence over sakai property
		otherSitesAvailability= "false".equalsIgnoreCase(toolProperty)? false : true;		
		return otherSitesAvailability;
	}

	public void setOtherSitesAvailability(boolean otherSitesAvailability) {
		this.otherSitesAvailability = otherSitesAvailability;
	}
	
	/**
	 * @return the maxSlots
	 */
	public int getMaxSlots() {
		String maxSlotsStringVal = Utilities.getSignupConfigParamVal("signup.maxSlots", "500");
		try{
			maxSlots = Integer.parseInt(maxSlotsStringVal);
		}
		catch (Exception e){
			maxSlots = 500;
		}
		return maxSlots;
	}

	/**
	 * @param maxSlots the maxSlots to set
	 */
	public void setMaxSlots(int maxSlots) {
		this.maxSlots = maxSlots;
	}
	
	/**
	 * @return the maxAttendeesPerSlot
	 */
	public int getMaxAttendeesPerSlot() {
		String maxAttendeesStringVal = Utilities.getSignupConfigParamVal("signup.maxAttendeesPerSlot", "500");
		try{
			maxAttendeesPerSlot = Integer.parseInt(maxAttendeesStringVal);
		}
		catch(Exception e){
			maxAttendeesPerSlot=500;
		}
		return maxAttendeesPerSlot;
	}

	/**
	 * @param maxAttendeesPerSlot the maxAttendeesPerSlot to set
	 */
	public void setMaxAttendeesPerSlot(int maxAttendeesPerSlot) {
		this.maxAttendeesPerSlot = maxAttendeesPerSlot;
	}


	/**
	 * The default Constructor. It will initialize all the required variables.
	 * 
	 */
	public NewSignupMeetingBean() {
		init();
	}

	public String getRepeatType() {
		return repeatType;
	}

	public Date getRepeatUntil() {
		return repeatUntil;
	}

	public void setRepeatType(String repeatType) {
		this.repeatType = repeatType;
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

	/** Initialize all the default setting for creating new events. */
	private void init() {

		signupMeeting = new SignupMeeting();
		signupMeeting.setMeetingType(INDIVIDUAL);
		
		Date date = new Date();
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(date);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);
		signupMeeting.setStartTime(startCal.getTime());
		
		Calendar endCal = startCal;
		endCal.add(Calendar.HOUR, 1);
		signupMeeting.setEndTime(endCal.getTime());
		
		unlimited = false;
		recurrence = false;
		assignParicitpantsToAllRecurEvents = false;
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
		if(NEW_MEETING_SEND_EMAIL){
			sendEmail = NEW_MEETING_SEND_EMAIL;
		}
		sendEmailToSelectedPeopleOnly=DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;
		receiveEmail = false;
		sendEmailByOwner= DEFAULT_SEND_EMAIL; /*will be inherited per meeting basis*/
		allowComment = DEFAULT_ALLOW_COMMENT;
		allowWaitList = DEFAULT_ALLOW_WAITLIST;
		autoReminder = DEFAULT_AUTO_RIMINDER;
		publishToCalendar= DEFAULT_EXPORT_TO_CALENDAR_TOOL;
		createGroups = DEFAULT_CREATE_GROUPS;
		currentStepHiddenInfo = null;
		eidInputMode = false;
		repeatType = ONCE_ONLY;
		repeatUntil = startCal.getTime();
		recurLengthChoice="1";//0 for num of repeat, 1 for date choice
		occurrences=0;
		this.publishedSite = null;
		//Custom defined time slots allocation
		userDefinedTS=false;
		customTimeSlotWrpList=null;
		otherSitesAvailability = true;
		creatorUserId = null;
		
		/*cleanup unused attachments in CHS*/
		if(this.attachments !=null && this.attachments.size()>0){
			for (SignupAttachment attach : attachments) {
				getAttachmentHandler().removeAttachmentInContentHost(attach);
			}
			this.attachments.clear();
		}
		else
			this.attachments = new ArrayList<SignupAttachment>();
	}

	public void reset() {
		init();
		signupBeginsType = Utilities.DAYS;
		deadlineTimeType = Utilities.HOURS;
		signupBegins = 6;
		deadlineTime = 1;
		timeSlotWrappers = null;
		currentSite = null;
		otherSites = null;
		/* for main meetingpage */
		Utilities.resetMeetingList();
		this.eidOrEmailInputByUser = null;
		this.selectedLocation=null;
		this.customLocation="";
		this.selectedCategory=null;
		this.customCategory="";
		this.creatorUserId=null;
		this.locations=null;
		this.categories=null;
		/*clean up everything in getUserDefineTimeslotBean*/
		getUserDefineTimeslotBean().reset(UserDefineTimeslotBean.NEW_MEETING);
	}
	
 	/**
 	 * This method is called to get all locations to populate the dropdown, for new signup creation.
 	 * 
 	 * @return list of allLocations
 	 */
 	public List<SelectItem> getAllLocations(){
 		if(locations ==null){
 			locations = new ArrayList<SelectItem>();
 			locations.addAll(Utilities.getSignupMeetingsBean().getAllLocations());
 			locations.add(0, new SelectItem(Utilities.rb.getString("select_location")));
 		}
 		
 		return locations;
 	}
 	
	/**
 	 * This method is called to get all categories to populate the dropdown, for new signup creation.
 	 * 
 	 * @return list of categories
 	 */
 	public List<SelectItem> getAllCategories(){
 		if(categories ==null){
 			categories = new ArrayList<SelectItem>();
 			categories.addAll(Utilities.getSignupMeetingsBean().getAllCategories());
 			categories.remove(0);//remove the 'All' default value from the list
 			categories.add(0, new SelectItem(Utilities.rb.getString("select_category")));
 		}
 		return categories;
 	}
 	
	/**
	 * This is a JSF action call method by UI to navigate to the next page.
	 * 
	 * @return an action outcome string.
	 */
	public String goNext() {
		if (validationError) {
			validationError = false;
			return ADD_MEETING_STEP1_PAGE_URL;
		}

		String step = (String) currentStepHiddenInfo.getValue();
		if (step.equals("step1")) {
			/*
			 * let recalculate the duration just in case of meeting endTime
			 * changes
			 */
			setTimeSlotDuration(0);
			if(isUserDefinedTS()){
				/*get the timeslots schedules for further process*/
				if(!Utilities.isDataIntegritySafe(isUserDefinedTS(),UserDefineTimeslotBean.NEW_MEETING,getUserDefineTimeslotBean()))
					return ADD_MEETING_STEP1_PAGE_URL;
				
				this.customTimeSlotWrpList=getUserDefineTimeslotBean().getDestTSwrpList();
			}
			
			return ADD_MEETING_STEP2_PAGE_URL;
		}

		return ADD_MEETING_STEP1_PAGE_URL;
	}
	
	/**
	 * This method is called by JSP page for adding/removing attachments action.
	 * @return null.
	 */
	public String addRemoveAttachments(){
		getAttachmentHandler().processAddAttachRedirect(this.attachments, this.signupMeeting,true);
		return null;
	}
	
	/**
	 * Create a new time slot blocks
	 * @return String object for the next page url
	 */
	public String createUserDefTimeSlots(){
		/* initially get the data from automatic time-slot creation as default*/
		this.timeSlotDuration = 0;//reset the value
		this.customTimeSlotWrpList = timeSlotWrappers();
		getUserDefineTimeslotBean().init(this.signupMeeting, ADD_MEETING_STEP1_PAGE_URL,this.customTimeSlotWrpList, UserDefineTimeslotBean.NEW_MEETING);
		return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
	}
	
	/**
	 * Modify the existing time slot blocks
	 * @return String object for next page url
	 */
	public String editUserDefTimeSlots(){
		if(!Utilities.isDataIntegritySafe(isUserDefinedTS(),UserDefineTimeslotBean.NEW_MEETING,getUserDefineTimeslotBean())){
			reset();
			return ADD_MEETING_STEP1_PAGE_URL;
		}
		
		this.customTimeSlotWrpList = getUserDefineTimeslotBean().getDestTSwrpList();
		getUserDefineTimeslotBean().init(this.signupMeeting, ADD_MEETING_STEP1_PAGE_URL,this.customTimeSlotWrpList, UserDefineTimeslotBean.NEW_MEETING);
		return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
	}
	
	/*Make sure the start/end time input fields have values.*/
	public boolean getPrePopulateValues(){
		if (this.signupMeeting.getStartTime() == null && isUserDefinedTS()){
			this.signupMeeting.setStartTime(getUserDefineTimeslotBean().getEventStartTime());
			this.signupMeeting.setEndTime(getUserDefineTimeslotBean().getEventEndTime());
		}
		return false;
	}

	/**
	 * This is a validator to make sure that the event/meeting starting time is
	 * before ending time.
	 * 
	 * @param e
	 *            an ActionEvent object.
	 */

	public void validateNewMeeting(ActionEvent e) {
		if(currentStepHiddenInfo == null)
			return;
		
		String step = (String) currentStepHiddenInfo.getValue();

		if (step.equals("step1")) {
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

			boolean locationSet = false;
			
			//Set Title		
			if (StringUtils.isNotBlank(title)){
				log.debug("title set: " + title);
				this.signupMeeting.setTitle(title);
			}else{
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("event.title_cannot_be_blank"));
				return;
			}
			
			//Set Location		
			if (StringUtils.isNotBlank(customLocation)){
				log.debug("custom location set: " + customLocation);
				this.signupMeeting.setLocation(customLocation);
				locationSet = true;
			}
			
			if (!locationSet && StringUtils.isNotBlank(selectedLocation) && !StringUtils.equals(selectedLocation, Utilities.rb.getString("select_location"))){
				this.signupMeeting.setLocation(selectedLocation);
				log.debug("chose a location: " + selectedLocation);
				locationSet = true;
			}
			
			if(!locationSet) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("event.location_not_assigned"));
				return;
			}
			
			
			//Set Category	
			//custom
			if (StringUtils.isNotBlank(customCategory)){
				this.signupMeeting.setCategory(customCategory);
			} 
			else{
				//or from the dropdown, but if we don't choose one or left it as the 'choose category method' then don't set it
				if (!StringUtils.equals(selectedCategory, Utilities.rb.getString("select_category"))){
					this.signupMeeting.setCategory(selectedCategory);
				}
			}

			// Need to filter for bad HTML
			StringBuilder descriptionErrors = new StringBuilder();
			String filteredDescription = sakaiFacade.getFormattedText()
					.processFormattedText(this.signupMeeting.getDescription(), descriptionErrors, true);
			this.signupMeeting.setDescription(filteredDescription);
			if (descriptionErrors.length() > 0) {
				validationError = true;
				Utilities.addErrorMessage(descriptionErrors.toString());
				return;
			}

			//set instructor
			this.signupMeeting.setCreatorUserId(creatorUserId);
			

			Date eventEndTime = signupMeeting.getEndTime();
			Date eventStartTime = signupMeeting.getStartTime();
			/*user defined own TS case*/
			if(isUserDefinedTS()){
				eventEndTime= getUserDefineTimeslotBean().getEventEndTime();
				eventStartTime = getUserDefineTimeslotBean().getEventStartTime();
				/*pass the value since they are null*/
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
				// signupMeeting.setMeetingType(null);
				return;
			}

			setRecurrence(false);
			if (!(getRepeatType().equals(ONCE_ONLY))) {
				int repeatNum = getOccurrences();
				if("1".equals(getRecurLengthChoice())){
					repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), eventStartTime,
						getRepeatUntil());
				}
					
				if ((DAILY.equals(getRepeatType()) || WEEKDAYS.equals(getRepeatType())) && isMeetingOverRepeatPeriod(eventStartTime, eventEndTime, 1)) {
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
				// TODO need to check for weekly too?

				if (repeatNum < 1) {
					validationError = true;
					if("1".equals(getRecurLengthChoice()))
						Utilities.addErrorMessage(Utilities.rb.getString("event.repeatbeforestart"));
					else
						Utilities.addErrorMessage(Utilities.rb.getString("event.repeatNnum.bigger.than.one"));
					
					return;
				}
				setRecurrence(true);
			}

			warnMeetingAccrossTwoDates(eventEndTime, eventStartTime);

			if (!CreateSitesGroups.isAtleastASiteOrGroupSelected(this.getCurrentSite(), this.getOtherSites())) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("select.atleast.oneGroup"));

			}

			if (signupMeeting.getMeetingType() == null) {
				validationError = true;
				Utilities.addErrorMessage(Utilities.rb.getString("signup.validator.selectMeetingType"));
				// signupMeeting.setMeetingType(null);

			}
			
			/*give warning to user in the next page if the event ending time get auto adjusted due to not even-division
			 * and it's not the case for custom defined time slot*/
			setEndTimeAutoAdjusted(false);
			if(!isUserDefinedTS()){
				if (isIndividualType() && getNumberOfSlots()!=0) {
					double duration = (double)(getSignupMeeting().getEndTime().getTime() - getSignupMeeting().getStartTime().getTime())
							/ (double)(MINUTE_IN_MILLISEC * getNumberOfSlots());				
					if (duration != Math.floor(duration)){
						setEndTimeAutoAdjusted(true);
						Utilities.addErrorMessage(Utilities.rb.getString("event_endtime_auto_adjusted_warning"));
					}
				}
			}
			
			/*for custom time slot case*/
			if(!validationError && isUserDefinedTS()){
				this.signupMeeting.setStartTime(eventStartTime);
				this.signupMeeting.setEndTime(eventEndTime);
				this.signupMeeting.setMeetingType(CUSTOM_TIMESLOTS);
			}
			/*reset meetingType for step1 */
			if(!isUserDefinedTS() && CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType())){
				this.signupMeeting.setMeetingType(INDIVIDUAL);
			}
			
			/*pre-load all possible coordinators for step2*/
			signupMeeting.setSignupSites(CreateSitesGroups.getSelectedSignupSites(getCurrentSite(), getOtherSites()));
			this.allPossibleCoordinators = this.sakaiFacade.getAllPossbileCoordinatorsOnFastTrack(this.signupMeeting);
			
			// tick the creator by default (SIGNUP-216)
			for(SignupUser u: this.allPossibleCoordinators) {
				if(StringUtils.equals(u.getInternalUserId(), this.creatorUserId)) {
					u.setChecked(true);
				}
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
		if (startYear != endYear){
			Utilities.addInfoMessage(Utilities.rb.getString("warning.event.crossed_twoYears"));
			return;
		}
		if (startMonth != endMonth){
			Utilities.addInfoMessage(Utilities.rb.getString("warning.event.crossed_twoMonths"));
			return;
		}
		if (startDay != endDay){
			Utilities.addInfoMessage(Utilities.rb.getString("warning.event.crossed_twoDays"));
			return;
		}
		
	}
	
	/*private boolean isMeetingLengthOver24Hours(SignupMeeting sm){
		long duration= sm.getEndTime().getTime()- sm.getStartTime().getTime();
		if( 24 - duration /(MINUTE_IN_MILLISEC * Hour_In_MINUTES) >= 0  )
			return false;
		
		return true;
	}*/
	
	private boolean isMeetingOverRepeatPeriod(Date startTime, Date endTime, int repeatPeriodInDays){
		long duration= endTime.getTime()- startTime.getTime();
		if( 24*repeatPeriodInDays - duration /(MINUTE_IN_MILLISEC * Hour_In_MINUTES) >= 0  )
			return false;
		
		return true;
	}

	/**
	 * This is a JSF action call method by UI to let user navigate one page
	 * back.
	 * 
	 * @return an action outcome string.
	 */
	public String goBack() {
		if(currentStepHiddenInfo == null){
			//it is rarely happening if any
			Utilities.addErrorMessage(Utilities.rb.getString("publish.withAttendee.exception"));
			//recover this from "assignAttendee" step case too
			//reset to remove timeslots info with attendees
			timeSlotWrappers = null; 
			assignParicitpantsToAllRecurEvents = false;
			//reset warning for ending time auto-adjustment
			setEndTimeAutoAdjusted(false);
			return ADD_MEETING_STEP1_PAGE_URL;
		}
		
		String step = (String) currentStepHiddenInfo.getValue();

		if (step.equals("step2")) {
			return ADD_MEETING_STEP1_PAGE_URL;
		}
		if (step.equals("assignAttendee")) {
			timeSlotWrappers = null; // reset to remove timeslots info with attendees
			assignParicitpantsToAllRecurEvents = false;
			//reset warning for ending time auto-adjustment
			setEndTimeAutoAdjusted(false);
			//reset who should receive emails
			//setSendEmailAttendeeOnly(false);
			sendEmailToSelectedPeopleOnly = DEFAULT_SEND_EMAIL_TO_SELECTED_PEOPLE_ONLY;//reset

			return ADD_MEETING_STEP2_PAGE_URL;
		}

		return ADD_MEETING_STEP1_PAGE_URL;
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
		if(!INDIVIDUAL.equals(newMeetingType)){
			setUserDefinedTS(false);
			//this.timeSlotWrappers = null;//reset
		}

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
			if (unlimited)
				maxOfAttendees = 10;

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
		if(!Utilities.isDataIntegritySafe(isUserDefinedTS(),UserDefineTimeslotBean.NEW_MEETING,getUserDefineTimeslotBean())){
			reset();
			return ADD_MEETING_STEP1_PAGE_URL;
		}
		
		preSaveAction();
		processSaveMeetings();
		reset();
		return MAIN_EVENTS_LIST_PAGE_URL;
	}

	/* Prepare the data for saving action */
	private void preSaveAction() {
		//SignupSite sSite = new SignupSite();
		//String currentLocationId = sakaiFacade.getCurrentLocationId();
		//sSite.setSiteId(currentLocationId);
		//sSite.setTitle(sakaiFacade.getLocationTitle(currentLocationId));
		//List<SignupSite> signupSites = new ArrayList<SignupSite>();
		//signupSites.add(sSite);
		//signupMeeting.setSignupSites(signupSites);
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
        //maybe duplicated, it is already set up after 'step1'
		signupMeeting.setSignupSites(CreateSitesGroups.getSelectedSignupSites(getCurrentSite(), getOtherSites()));

		signupMeeting.setCreatorUserId(this.creatorUserId);
		signupMeeting.setReceiveEmailByOwner(receiveEmail);
		signupMeeting.setSendEmailByOwner(sendEmailByOwner);
		signupMeeting.setAllowWaitList(this.allowWaitList);
		signupMeeting.setAllowComment(this.allowComment);
		signupMeeting.setAutoReminder(this.autoReminder);
		signupMeeting.setEidInputMode(this.eidInputMode);
		signupMeeting.setMaxNumOfSlots(new Integer(this.maxNumOfSlots));
		signupMeeting.setCoordinatorIds(Utilities.getSelectedCoordinators(this.allPossibleCoordinators,this.creatorUserId));
		/* add attachments */
		signupMeeting.setSignupAttachments(this.attachments);
		
		signupMeeting.setCreateGroups(this.createGroups);
		
	}

	/**
	 * This is a JSF action call method by UI to let user to go to next page,
	 * which will allow user to pre-assign the attendees into the event/meeting.
	 * 
	 * @return an action outcome string.
	 */
	public String proceesPreAssignAttendee() {
		if(!Utilities.isDataIntegritySafe(isUserDefinedTS(),UserDefineTimeslotBean.NEW_MEETING,getUserDefineTimeslotBean())){
			reset();
			return ADD_MEETING_STEP1_PAGE_URL;
		}
		
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
		processSaveMeetings();
		reset();
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

		String attendeeEidOrEmail = null;
		if (isEidInputMode()) {
			attendeeEidOrEmail = getEidOrEmailInputByUser();
		} else {
			attendeeEidOrEmail = (String) newAttendeeInput.getValue();
		}
		
		if(attendeeEidOrEmail ==null || attendeeEidOrEmail.length() <1)
			return PRE_ASSIGN_ATTENDEE_PAGE_URL;
		
		//check if there are multiple email addresses associated with input
		List<String> associatedEids = getEidsForEmail(attendeeEidOrEmail.trim());
		if(associatedEids.size() > 1) {
			Utilities.addErrorMessage(MessageFormat.format(Utilities.rb.getString("exception.multiple.eids"), new Object[] {attendeeEidOrEmail, StringUtils.join(associatedEids, ", ")}));
			return PRE_ASSIGN_ATTENDEE_PAGE_URL;
		}

		String attendeeUserId = getUserIdForEidOrEmail(attendeeEidOrEmail.trim());
		if(StringUtils.isBlank(attendeeEidOrEmail)){
			Utilities.addErrorMessage(Utilities.rb.getString("exception.no.such.user") + attendeeEidOrEmail);
			return PRE_ASSIGN_ATTENDEE_PAGE_URL;
		}
		
		SignupUser attendeeSignUser = getSakaiFacade().getSignupUser(this.signupMeeting, attendeeUserId);
		if(attendeeSignUser ==null){
			Utilities.addErrorMessage(MessageFormat.format(Utilities.rb.getString("user.has.no.permission.attend"), new Object[] {attendeeEidOrEmail}));
			return PRE_ASSIGN_ATTENDEE_PAGE_URL;
		}
		
		SignupAttendee attendee = new SignupAttendee(attendeeUserId, attendeeSignUser.getMainSiteId());

		if (isDuplicateAttendee(timeslotWrapper.getTimeSlot(), attendee)) {
			Utilities.addErrorMessage(Utilities.rb.getString("attendee.already.in.timeslot"));
		} else {
			timeslotWrapper.addAttendee(attendee, sakaiFacade.getUserDisplayLastFirstName(attendeeUserId));
		}

		return PRE_ASSIGN_ATTENDEE_PAGE_URL;
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
		
		if(meetingType.equals(CUSTOM_TIMESLOTS)){
			List<TimeslotWrapper> tmpTSList = new ArrayList<TimeslotWrapper>(this.customTimeSlotWrpList);
			for (TimeslotWrapper tsWrp : tmpTSList) {
				tsWrp.getTimeSlot().setDisplayAttendees(isShowParticipants());
			}
			return tmpTSList;
		}
		return null;
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
		if (this.currentSite == null) {
			getAvailableSiteGroups();
		}
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
		if (this.currentSite == null) {
			getAvailableSiteGroups();
		}
		return currentSite;
	}

	/*
	 * Due to Authz bug, we have to share the same CreateSitesGroups object. It
	 * will much simple if Authz bug is fixed.
	 */
	private void getAvailableSiteGroups() {
		Utilities.getSignupMeetingsBean().getCreateSitesGroups().resetSiteGroupCheckboxMark();
		currentSite = Utilities.getSignupMeetingsBean().getCreateSitesGroups().getCurrentSite();
		otherSites = Utilities.getSignupMeetingsBean().getCreateSitesGroups().getOtherSites();
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
	public boolean getSendEmail() {
		if (!getPublishedSite())
			sendEmail = false;// no email notification

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
	 * @return true if email notification will be the default value.
	 */
	public boolean getSendEmailByOwner() {
		return sendEmailByOwner;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a constant string.
	 */
	public void setSendEmailByOwner(boolean sendEmailByOwner) {
		this.sendEmailByOwner = sendEmailByOwner;
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
		if(isEidInputMode())
			return;
		
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

		if (allSignupUsers != null
				&& allSignupUsers.size() > MAX_NUM_PARTICIPANTS_FOR_DROPDOWN_BEFORE_AUTO_SWITCH_TO_EID_INPUT_MODE) {
			setEidInputMode(true);
			return;
		}

		setEidInputMode(false);
		this.allAttendees = new ArrayList<SelectItem>();
		SelectItem sItem = new SelectItem("", " " + Utilities.rb.getString("label.select.attendee"));
		allAttendees.add(sItem);
		String previous_displayName ="";
		int index = 0;
		for (SignupUser user : allSignupUsers) {
			if(user.getDisplayName().equals(previous_displayName)){
				allAttendees.add(new SelectItem(user.getEid(), user.getDisplayName()+ "(" + user.getEid() +")"));
				SelectItem prev_sItem = allAttendees.get(index);
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
	 * This is a getter method for UI.
	 * 
	 * @return true if it's an individual event/meeting type.
	 */
	public boolean isCustomTimeslotType() {
		return CUSTOM_TIMESLOTS.equals(getSignupMeeting().getMeetingType());
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
		else if (isUserDefinedTS())
			mType = Utilities.rb.getString("label_custom_timeslots");

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
	 * This is for Javascript UI only.
	 * 
	 * @return empty string.
	 */
	public String getUserInputEidOrEmail() {
		return "";
	}

	/**
	 * This is for Javascript UI only.
	 * 
	 * @param value
	 *            eid or email for the user
	 */
	public void setUserInputEidOrEmail(String value) {
		if (StringUtils.isNotBlank(value)) {
			this.eidOrEmailInputByUser = value;
		}
	}

	/* Proxy method */
	private String getEidOrEmailInputByUser() {
		String eid = this.eidOrEmailInputByUser;
		this.eidOrEmailInputByUser = null;// reset for use once only
		return eid;
	}

	private void processSaveMeetings() {
		signupMeeting.setRepeatUntil(getRepeatUntil());
		int repeatNum = getOccurrences();
		if("1".equals(getRecurLengthChoice())){
			repeatNum = CreateMeetings.getNumOfRecurrence(getRepeatType(), signupMeeting.getStartTime(),
				getRepeatUntil());
		}
		signupMeeting.setRepeatNum(repeatNum);		
		signupMeeting.setRepeatType(getRepeatType());
		
		if(CUSTOM_TIMESLOTS.equals(this.signupMeeting.getMeetingType())){
			boolean multipleCalBlocks = getUserDefineTimeslotBean().getPutInMultipleCalendarBlocks();
			signupMeeting.setInMultipleCalendarBlocks(multipleCalBlocks);
		}
		
		/*pass who should receive the emails*/
		signupMeeting.setSendEmailToSelectedPeopleOnly(this.sendEmailToSelectedPeopleOnly);
				
		CreateMeetings createMeeting = new CreateMeetings(signupMeeting, sendEmail,
				!assignParicitpantsToAllRecurEvents, assignParicitpantsToAllRecurEvents, getSignupBegins(),
				getSignupBeginsType(), getDeadlineTime(), getDeadlineTimeType(), getRecurLengthChoice(), sakaiFacade, signupMeetingService,
				getAttachmentHandler(), sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId(), true);

		try {
			/*need push to calendar tool*/
			createMeeting.setPublishToCalendar(getPublishToCalendar());
						
			/*do we want to also create groups? */
			createMeeting.setCreateGroups(this.createGroups);
			
			createMeeting.processSaveMeetings();
			
			/*handle attachments and it should not be cleaned up in CHS*/
			this.attachments.clear();
			
		} catch (PermissionException e) {
			log.info(Utilities.rb.getString("no.permission_create_event") + " - " + e.getMessage());
		} catch (Exception e) {
			log.error(Utilities.rb.getString("error.occurred_try_again") + " - " + e.getMessage());
			Utilities.addErrorMessage(Utilities.rb.getString("error.occurred_try_again"));
		}
	
	}

	/**
	 * This is a getter for UI
	 * 
	 * @return a boolean value
	 */
	public boolean isAssignParicitpantsToAllRecurEvents() {
		return assignParicitpantsToAllRecurEvents;
	}

	/**
	 * This is a setter method for UI
	 * 
	 * @param assignParicitpantsToAllRecurEvents
	 *            a boolean vaule
	 */
	public void setAssignParicitpantsToAllRecurEvents(boolean assignParicitpantsToAllRecurEvents) {
		this.assignParicitpantsToAllRecurEvents = assignParicitpantsToAllRecurEvents;
	}

	private String eventFreqType = "";

	/**
	 * This is a getter method for UI
	 * 
	 * @return a event frequency type string
	 */
	public String getEventFreqType() {
		eventFreqType = "";
		if (getRepeatType().equals(DAILY))
			eventFreqType = Utilities.rb.getString("label_daily");
		else if (getRepeatType().equals(WEEKDAYS))
			eventFreqType = Utilities.rb.getString("label_weekdays");
		else if (getRepeatType().equals(WEEKLY))
			eventFreqType = Utilities.rb.getString("label_weekly");
		else if (getRepeatType().equals(BIWEEKLY))
			eventFreqType = Utilities.rb.getString("label_biweekly");

		return eventFreqType;
	}

	/**
	 * This is a getter method for UI and it provides the selected
	 * site(s)/group(s) by user.
	 * 
	 * @return a list of SignupSite objects.
	 */
	public List<SignupSite> getSelectedSignupSites() {
		return CreateSitesGroups.getSelectedSignupSites(this.currentSite, this.otherSites);
	}

	/**
	 * This is a getter method for UI and it provides the meeting types for
	 * radio buttons.
	 * 
	 * @return a list of SelectItem objects.
	 */
	public List<SelectItem> getMeetingTypeRadioBttns() {
		this.meetingTypeRadioBttns = Utilities.getMeetingTypeSelectItems("", false);
		return meetingTypeRadioBttns;
	}

	/**
	 * This is a getter method for UI
	 * 
	 * @return true if the site is published.
	 */
	public Boolean getPublishedSite() {
		if (this.publishedSite == null) {
			try {
				boolean status = sakaiFacade.getSiteService().getSite(sakaiFacade.getCurrentLocationId()).isPublished();
				this.publishedSite = new Boolean(status);

			} catch (Exception e) {
				log.warn(e.getMessage());
				this.publishedSite = new Boolean(false);

			}
		}

		return publishedSite.booleanValue();
	}

	/**
	 * This is a getter method for UI
	 * @return true if the ending time is adjusted.
	 */
	public boolean isEndTimeAutoAdjusted() {
		return endTimeAutoAdjusted;
	}

	/**
	 * This is a setter method.
	 * @param endTimeAutoAdjusted
	 */
	public void setEndTimeAutoAdjusted(boolean endTimeAutoAdjusted) {
		this.endTimeAutoAdjusted = endTimeAutoAdjusted;
	}

	public boolean isAllowWaitList() {
		return allowWaitList;
	}

	public void setAllowWaitList(boolean allowWaitList) {
		this.allowWaitList = allowWaitList;
	}

	public boolean isAllowComment() {
		return allowComment;
	}

	public void setAllowComment(boolean allowComment) {
		this.allowComment = allowComment;
	}

	public boolean isAutoReminder() {
		return autoReminder;
	}

	public void setAutoReminder(boolean autoReminder) {
		this.autoReminder = autoReminder;
	}

	public boolean isAutoReminderOptionChoice() {
		return autoReminderOptionChoice;
	}

	public void setAutoReminderOptionChoice(boolean autoReminderOptionChoice) {
		this.autoReminderOptionChoice = autoReminderOptionChoice;
	}
	
	public boolean isUserIdInputModeOptionChoice() {
		return userIdInputModeOptionChoice;
	}

	public void setUserIdInputModeOptionChoice(boolean userIdInputModeOptionChoice) {
		this.userIdInputModeOptionChoice = userIdInputModeOptionChoice;
	}

	public List<SignupAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<SignupAttachment> attachments) {
		this.attachments = attachments;
	}

	public AttachmentHandler getAttachmentHandler() {
		return attachmentHandler;
	}

	public void setAttachmentHandler(AttachmentHandler attachmentHandler) {
		this.attachmentHandler = attachmentHandler;
	}
	
	public boolean isAttachmentsEmpty(){
		if (this.attachments !=null && this.attachments.size()>0)
			return false;
		else
			return true;
	}
	
	public boolean isAllLocationsEmpty(){
		// this is safe to call often since we cache the locations
		//it already has one label item in
		return !(getAllLocations().size()>1);
			
	}
	
	public boolean isCategoriesExist() {
		// this is safe to call often since we cache the categories
		return getAllCategories().size()>1;
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

	public boolean getPublishToCalendar() {
		return publishToCalendar;
	}

	public void setPublishToCalendar(boolean publishToCalendar) {
		this.publishToCalendar = publishToCalendar;
	}
	
	public boolean getCreateGroups() {
		return createGroups;
	}

	public void setCreateGroups(boolean createGroups) {
		this.createGroups = createGroups;
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
		return Utilities.getSignupMeetingsBean().getInstructors(null);
	}
	
	/**
	 * Get the instructor name attached to the value currently set for the instructor
	 */
	public String getInstructorName() {
		return Utilities.getSignupMeetingsBean().getInstructorName(creatorUserId);
	}
		
	/**
	 * Gets the userId for a user, given an eid or an email address. 
	 * We check if it matches the eid first, then if it matches an email address.
	 * If nothing, return null.
	 * 
	 * @param value		the string to lookup, could be an eid or an email address
	 * @return	the userId or null if User cannot be found
	 */
	public String getUserIdForEidOrEmail(String value) {
		User u = sakaiFacade.getUserByEid(value);
		if(u==null) {
			u=sakaiFacade.getUserByEmail(value);
		}
		
		if(u!=null) {
			return u.getId();
		}
		
		return null;
	}
	
	/**
	 * Get the eids assocaited with an email address, ie there may be two or more users with the same email address. 
	 * We need to be able to handle this in the UI.
	 * 
	 * @param email
	 * @return	List<String> of eids.
	 */
	public List<String> getEidsForEmail(String email) {
		Collection<User> users = sakaiFacade.getUsersByEmail(email);
		
		List<String> eids = new ArrayList<String>();
		for(User u:users) {
			eids.add(u.getEid());
		}
		
		return eids;
	}

	public List<SignupUser> getAllPossibleCoordinators() {
		return allPossibleCoordinators;
	}

	public void setAllPossibleCoordinators(List<SignupUser> allPossibleCoordinators) {
		this.allPossibleCoordinators = allPossibleCoordinators;
	}
	
	/**
	 * This is for UI page to determine whether the email checkbox should be checked and disabled to change
	 * @return
	 */
	public boolean isMandatorySendEmail(){
		return this.mandatorySendEmail;
	}

	public String getSendEmailToSelectedPeopleOnly() {
		return sendEmailToSelectedPeopleOnly;
	}

	public void setSendEmailToSelectedPeopleOnly(
			String sendEmailToSelectedPeopleOnly) {
		this.sendEmailToSelectedPeopleOnly = sendEmailToSelectedPeopleOnly;
	}
	
	private String iframeId = "";

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

	public void setIframeId(String iframeId) {
		this.iframeId = iframeId;
	}	
	
}
