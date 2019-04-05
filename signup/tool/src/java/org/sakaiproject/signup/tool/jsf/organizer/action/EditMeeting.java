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

package org.sakaiproject.signup.tool.jsf.organizer.action;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupEmailFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfoImpl;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.TimeslotWrapper;
import org.sakaiproject.signup.tool.jsf.attachment.AttachmentHandler;
import org.sakaiproject.signup.tool.jsf.organizer.UserDefineTimeslotBean;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.signup.util.SignupDateFormat;
import org.springframework.dao.OptimisticLockingFailureException;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * This class will provide business logic for modifying meeting action
 * by organizer.
 * </P>
 */
@Slf4j
public class EditMeeting extends SignupAction implements MeetingTypes {

	private int maxNumOfAttendees;

	private boolean showAttendeeName;

	private int currentNumberOfSlots;

	private int timeSlotDuration;

	private boolean unlimited;

	// private int totalEventDuration;// for group/announcement types

	private SignupMeeting originalMeetingCopy;

	private String signupBeginsType;

	/* singup can start before this minutes/hours/days */
	private int signupBegins;
	
	/*True means that the current recurring events have already start_Now Type*/
	private boolean isStartNowTypeForRecurEvents=false;
	
	private boolean signupBeginModifiedByUser;

	private String deadlineTimeType;

	/* singup deadline before this minutes/hours/days */
	private int deadlineTime;

	private boolean convertToNoRecurrent;

	private List<SignupMeeting> savedMeetings;

	private String recurringType = null;

	// may not used
	private Date firstRecurMeetingModifyDate = null;

	private Date lastRecurMeetingModifyDate = null;
	
	private List<SignupAttachment> currentAttachList;
	
	private List<SignupAttachment> needToCleanUpAttachInContentHS = new ArrayList<SignupAttachment>();
	
	private List<SignupAttachment> attachsForFutureRecurrences = new ArrayList<SignupAttachment>();

	private AttachmentHandler attachmentHandler;
	
	/*keep the final user-modified ts-list*/
	private List<TimeslotWrapper> customTimeSlotWrpList = null;
	
	private boolean userDefinedTS=false;
	
	private UserDefineTimeslotBean userDefineTimeslotBean;
	
	private List<SignupTimeslot> toBedeletedTSList = null;
	
	private SakaiFacade sakaiFacade;
	
	private boolean createGroups;
	
	private String coordinators;
	
	private boolean sendEmailByOwner;
	
	//it make sure not running this job multiple times
	private boolean hasProcessedSyn_createGroups_job = false;
	
	public EditMeeting(String userId, String siteId, SignupMeetingService signupMeetingService, AttachmentHandler attachmentHandler, boolean isOrganizer) {
		super(userId, siteId, signupMeetingService, isOrganizer);
		this.attachmentHandler = attachmentHandler;
	}

	public void saveModifiedMeeting(SignupMeeting meeting) throws Exception {
		handleVersion(meeting);

		/*
		 * give a warning to user and for announcement type no update is
		 * required
		 */
		if (!unlimited && this.maxNumOfAttendees < originalMeetingCopy.getMaxNumberOfAttendees()) {
			Utilities.addErrorMessage(Utilities.rb.getString("max.num_attendee_changed_and_attendee_mayOver_limit_inTS"));
		}

		log.info("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
				+ " - has modified the meeting at meeting time:"
				+ SignupDateFormat.format_date_h_mm_a(meeting.getStartTime()));
		if (getMaxNumOfAttendees() > this.originalMeetingCopy.getMaxNumberOfAttendees())
			log.info("Meeting Name:" + meeting.getTitle() + " - UserId:" + userId
					+ this.signupEventTrackingInfo.getAllAttendeeTransferLogInfo());
		
		/*cleanup unused attachments*/
		if(needToCleanUpAttachInContentHS !=null){
			for (SignupAttachment one : needToCleanUpAttachInContentHS) {			
				getAttachmentHandler().removeAttachmentInContentHost(one);
			}
		}
	}

	/*
	 * give it a number of tries to update the event/meeting object into DB
	 * storage if this still satisfy the pre-condition regardless some changes
	 * in DB storage
	 */
	public void handleVersion(SignupMeeting meeting) throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {

				this.signupEventTrackingInfo = new SignupEventTrackingInfoImpl();
				/*
				 * TODO current eventTracking is not working for multiple
				 * recurring meetings. It's a bit more complex and need modify
				 * the SignupEventTrackingInfoImpl object or other way.
				 * Currently, no email are sent out to newly promoted people for
				 * multiple recurring meetings.
				 */
				this.signupEventTrackingInfo.setMeeting(meeting);
				//reset it
				this.toBedeletedTSList = null;

				List<SignupMeeting> sMeetings = prepareModify(meeting);
				signupMeetingService.updateModifiedMeetings(sMeetings, this.toBedeletedTSList, true);
				//signupMeetingService.updateMSignupMeetings(sMeetings, this.toBedeletedTSList, true);
				setSavedMeetings(sMeetings);// for email & postCalendar purpose
				return;
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		throw new SignupUserActionException(Utilities.rb.getString("edit.failed_due_to_other_updated_db"));
	}

	private SignupMeeting reloadMeeting(SignupMeeting meeting) {
		return signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
	}

	/* put the modification into right place */
	private List<SignupMeeting> prepareModify(SignupMeeting userModifiedMeeting) throws Exception {
		List<SignupMeeting> upTodateOrginMeetings = null;
		List<SignupMeeting> newlyModifyMeetings = new ArrayList<SignupMeeting>();

		Long recurrenceId = userModifiedMeeting.getRecurrenceId();
		if (recurrenceId != null && recurrenceId.longValue() > 0 && !isConvertToNoRecurrent()) {
			/* only update the future recurring meeting now now today */
			upTodateOrginMeetings = signupMeetingService.getRecurringSignupMeetings(siteId, userId, recurrenceId,
					new Date());
			retrieveRecurrenceData(upTodateOrginMeetings);
		} else {
			SignupMeeting upTodateOrginMeeting = reloadMeeting(userModifiedMeeting);
			upTodateOrginMeetings = new ArrayList<SignupMeeting>();
			upTodateOrginMeetings.add(upTodateOrginMeeting);
		}

		/*
		 * Since recurring meetings are identical by title, location, etc. only
		 * the corresponding one to original one will be checked here.
		 */
		/*
		 * If someone has changed it before you,it should be caught by versionId
		 * since meetings are saved as a bulk.
		 */
		SignupMeeting upToDateMatchOne = getCorrespondingMeeting(upTodateOrginMeetings, userModifiedMeeting);
		checkPreCondition(upToDateMatchOne, upTodateOrginMeetings);

		Calendar calendar = Calendar.getInstance();
		int recurNum=0;
		//initialize to-be-removed TS list for advanced user-defined cases
		this.toBedeletedTSList = new ArrayList<SignupTimeslot>();
		for (SignupMeeting upTodateOrginMeeting : upTodateOrginMeetings) {

			SignupMeeting newlyModifyMeeting = upTodateOrginMeeting;

			/*
			 * set event starting time for calendar due to multiple-recurring
			 * meetings
			 */
			long startTimeChangeDiff = userModifiedMeeting.getStartTime().getTime()
					- getOriginalMeetingCopy().getStartTime().getTime();
			calendar.setTimeInMillis(upTodateOrginMeeting.getStartTime().getTime() + startTimeChangeDiff);

			/* pass user changes */
			passModifiedValues(userModifiedMeeting, calendar, newlyModifyMeeting, recurNum);
			recurNum++;
			newlyModifyMeetings.add(newlyModifyMeeting);
		}

		return newlyModifyMeetings;
	}

	private void passModifiedValues(SignupMeeting modifiedMeeting, Calendar calendar, SignupMeeting newlyModifyMeeting, int recurNum)
			throws Exception {
		//for Group title synch purpose
		boolean hasMeetingTitleChanged=false;		
		if(!newlyModifyMeeting.getTitle().equals(modifiedMeeting.getTitle())){
			hasMeetingTitleChanged=true;
		}			
		newlyModifyMeeting.setTitle(modifiedMeeting.getTitle());
		newlyModifyMeeting.setLocation(modifiedMeeting.getLocation());
		newlyModifyMeeting.setCategory(modifiedMeeting.getCategory());
		newlyModifyMeeting.setCreatorUserId(modifiedMeeting.getCreatorUserId());
		newlyModifyMeeting.setStartTime(calendar.getTime());
		newlyModifyMeeting.setDescription(modifiedMeeting.getDescription());
		newlyModifyMeeting.setLocked(modifiedMeeting.isLocked());
		newlyModifyMeeting.setCanceled(modifiedMeeting.isCanceled());
		newlyModifyMeeting.setReceiveEmailByOwner(modifiedMeeting.isReceiveEmailByOwner());
		newlyModifyMeeting.setAllowWaitList(modifiedMeeting.isAllowWaitList());
		newlyModifyMeeting.setAllowComment(modifiedMeeting.isAllowComment());
		newlyModifyMeeting.setAutoReminder(modifiedMeeting.isAutoReminder());
		newlyModifyMeeting.setEidInputMode(modifiedMeeting.isEidInputMode());
		newlyModifyMeeting.setAllowAttendance(modifiedMeeting.isAllowAttendance());
		newlyModifyMeeting.setCreateGroups(modifiedMeeting.isCreateGroups());
		newlyModifyMeeting.setMaxNumOfSlots(modifiedMeeting.getMaxNumOfSlots());
		
		newlyModifyMeeting.setSendEmailByOwner(isSendEmailByOwner());
		newlyModifyMeeting.setCoordinatorIds(getCoordinators());
		
		/*new attachments changes*/
		if(this.currentAttachList !=null){
			updateWithOrigalAttachments(newlyModifyMeeting, this.currentAttachList, recurNum);//what to do with recurrence
		}
		
	

		if (newlyModifyMeeting.getMeetingType().equals(INDIVIDUAL) && !isUserDefinedTS()) {
			List<SignupTimeslot> timeslots = newlyModifyMeeting.getSignupTimeSlots();
			/* add increased time slots */
			int newAddTs = getCurrentNumberOfSlots() - timeslots.size();
			for (int i = 0; i < newAddTs; i++) {
				SignupTimeslot newTs = new SignupTimeslot();
				newTs.setMaxNoOfAttendees(maxNumOfAttendees);
				newTs.setDisplayAttendees(showAttendeeName);
				timeslots.add(newTs);
			}
			int rownum =1;
			for (SignupTimeslot timeslot : timeslots) {
				timeslot.setMaxNoOfAttendees(maxNumOfAttendees);
				timeslot.setDisplayAttendees(showAttendeeName);
				timeslot.setStartTime(calendar.getTime());
				calendar.add(Calendar.MINUTE, timeSlotDuration);
				timeslot.setEndTime(calendar.getTime());
				if(!newlyModifyMeeting.isAllowWaitList()){
					timeslot.setWaitingList(null);
				}
				
				//create the groups for the timeslots, if enabled
				//this also loads the groupId into the timeslot object to be saved afterwards
				if(isCreateGroups() && !this.hasProcessedSyn_createGroups_job){
					log.error("Timeslot groupId: " + timeslot.getGroupId());
					boolean justCreated = false;	
					//if we don't already have a group for this timeslot, or the group has been deleted, create a group and set into timeslot
					if(StringUtils.isBlank(timeslot.getGroupId()) || !sakaiFacade.checkForGroup(sakaiFacade.getCurrentLocationId(), timeslot.getGroupId())) {
						log.error("Need to create a group for timeslot... ");
						String title = generateGroupTitle(newlyModifyMeeting.getTitle(), timeslot, rownum);
						String description = generateGroupDescription(newlyModifyMeeting.getTitle(), timeslot);
						String groupId = sakaiFacade.createGroup(sakaiFacade.getCurrentLocationId(), title, description, null);
						log.error("Created group for timeslot: " + groupId);
						timeslot.setGroupId(groupId);
						justCreated = true;
					}
					
					//Synch the group title with new meeting title
					if(hasMeetingTitleChanged && !justCreated){
						//use case: if the group title has been changes via Site-Info tool by removing GROUP_PREFIX : "SIGNUP_", 
						//it will be not synch any more for that group.
						String newTitle = generateGroupTitle(newlyModifyMeeting.getTitle(), timeslot, rownum);
						boolean success = this.sakaiFacade.synchonizeGroupTitle(sakaiFacade.getCurrentLocationId(), timeslot.getGroupId(), newTitle);
						if(!success){
							Utilities.addErrorMessage(Utilities.rb.getString("error.no.change.group.title"));
						}
					}
				}
				
				rownum++;				
			}
			
			if(isCreateGroups()){
				//JUST NEED TO RUN ONECE, the sequential recurrence meetings need not to create/sync groups again.
				this.hasProcessedSyn_createGroups_job= true;
			}
		}
		
		/*process user custom-defined timeslot blocks*/
		if (newlyModifyMeeting.getMeetingType().equals(CUSTOM_TIMESLOTS) || isUserDefinedTS()) {
			List<SignupTimeslot> timeslots = newlyModifyMeeting.getSignupTimeSlots();
			UserDefineTimeslotBean uBean = getUserDefineTimeslotBean();
			if(uBean ==null || !uBean.MODIFY_MEETING.equals(uBean.getPlaceOrderBean())){
				throw new SignupUserActionException(MessageFormat.format(Utilities.rb.getString("you.have.multiple.tabs.in.browser"),
						new Object[]{ServerConfigurationService.getServerName()}));
			}
			
			uBean.modifyTimesSlotsWithChanges(this.customTimeSlotWrpList, timeslots, calendar, showAttendeeName, toBedeletedTSList);
			if(recurNum ==0){
				/*TODO currently we only notify attendee for the first event at recurrence
				 * Need implementation for recurrence case*/
				notifyAttendeeIndeleteTimeslots(toBedeletedTSList);
			}
			newlyModifyMeeting.setMeetingType(CUSTOM_TIMESLOTS);
			/*for end time purpose*/
			int duration = getUserDefineTimeslotBean().getEventDuration();
			calendar.add(Calendar.MINUTE, duration);
		}

		if (newlyModifyMeeting.getMeetingType().equals(GROUP)) {
			List<SignupTimeslot> signupTimeSlots = newlyModifyMeeting.getSignupTimeSlots();
			SignupTimeslot timeslot = signupTimeSlots.get(0);
			timeslot.setStartTime(newlyModifyMeeting.getStartTime());

			calendar.add(Calendar.MINUTE, getTimeSlotDuration());
			timeslot.setEndTime(calendar.getTime());
			timeslot.setDisplayAttendees(showAttendeeName);
			int maxAttendees = (unlimited) ? SignupTimeslot.UNLIMITED : maxNumOfAttendees;
			timeslot.setMaxNoOfAttendees(maxAttendees);
			if(!newlyModifyMeeting.isAllowWaitList()){
				timeslot.setWaitingList(null);
			}
		}

		if (newlyModifyMeeting.getMeetingType().equals(ANNOUNCEMENT)) {
			calendar.add(Calendar.MINUTE, getTimeSlotDuration());

		}
		/*
		 * Promoting waiter to attendee status if max size increased and is
		 * allowed
		 */
		if (maxNumOfAttendees > originalMeetingCopy.getMaxNumberOfAttendees()) {
			promotingWaiters(newlyModifyMeeting);
		}

		// set up meeting end time
		newlyModifyMeeting.setEndTime(calendar.getTime());

		/* setup sign-up begin / deadline */
		setSignupBeginDeadlineData(newlyModifyMeeting, getSignupBegins(), getSignupBeginsType(), getDeadlineTime(),
				getDeadlineTimeType());

		/* This can happen only once to promote recurring meeting to stand-alone */
		if (isConvertToNoRecurrent()) {
			newlyModifyMeeting.setRecurrenceId(null);
		}

		if (newlyModifyMeeting.getRecurrenceId() != null) {
			newlyModifyMeeting.setRepeatType(getRecurringType());
			newlyModifyMeeting.setRepeatUntil(getLastRecurMeetingModifyDate());
		}
	}
	
	private void notifyAttendeeIndeleteTimeslots(List<SignupTimeslot> toBedeletedTSList){
		for (SignupTimeslot timeslot : toBedeletedTSList) {
			List<SignupAttendee> attendees = timeslot.getAttendees();
			if( attendees !=null && !attendees.isEmpty()){
				for (SignupAttendee attendee : attendees) {
					signupEventTrackingInfo.addOrUpdateAttendeeAllocationInfo(attendee, timeslot,
							SignupEmailFacade.SIGNUP_ATTENDEE_CANCEL, true);
				}
			}
		}
	}

	private SignupMeeting getCorrespondingMeeting(List<SignupMeeting> upTodateOrginMeetings, SignupMeeting modifyMeeting) {
		for (SignupMeeting sm : upTodateOrginMeetings) {
			if (sm.getId().equals(modifyMeeting.getId()))
				return sm;
		}
		return null;
	}

	/* Promoting all possible waiters to attendee status */
	private void promotingWaiters(SignupMeeting modifiedMeeting) {
		List<SignupTimeslot> timeSlots = modifiedMeeting.getSignupTimeSlots();
		if (timeSlots == null || timeSlots.isEmpty())
			return;

		for (SignupTimeslot timeslot : timeSlots) {
			List attList = timeslot.getAttendees();
			int maxNumOfAttendees = timeslot.getMaxNoOfAttendees();
			if (attList == null)
				continue;// nobody on wait list for sure

			if (attList.size() < maxNumOfAttendees) {
				int availNum = maxNumOfAttendees - attList.size();
				for (int i = 0; i < availNum; i++) {
					promoteAttendeeFromWaitingList(modifiedMeeting, timeslot);
				}
			}
		}

	}

	/*
	 * Check if there is any update in DB before this one is saved into DB
	 * storage.
	 */
	private void checkPreCondition(SignupMeeting upTodateMeeting, List<SignupMeeting> upTodateOrginMeetings) throws SignupUserActionException {
		if (upTodateMeeting == null
				|| !originalMeetingCopy.getTitle().equals(upTodateMeeting.getTitle())
				|| !originalMeetingCopy.getLocation().equals(upTodateMeeting.getLocation())
				|| !StringUtils.equals(originalMeetingCopy.getCategory(), upTodateMeeting.getCategory())
				|| !StringUtils.equals(originalMeetingCopy.getCreatorUserId(), upTodateMeeting.getCreatorUserId())
				|| originalMeetingCopy.getStartTime().getTime() != upTodateMeeting.getStartTime().getTime()
				|| originalMeetingCopy.getEndTime().getTime() != upTodateMeeting.getEndTime().getTime()
				|| originalMeetingCopy.getSignupBegins().getTime() != upTodateMeeting.getSignupBegins().getTime()
				|| originalMeetingCopy.getSignupDeadline().getTime() != upTodateMeeting.getSignupDeadline().getTime()
				/* TODO more case to consider here */
				|| !originalMeetingCopy.getMeetingType().equals(upTodateMeeting.getMeetingType())
				|| !((originalMeetingCopy.getRecurrenceId() == null && upTodateMeeting.getRecurrenceId() == null) || (originalMeetingCopy
						.getRecurrenceId() != null && originalMeetingCopy.getRecurrenceId().equals(
						upTodateMeeting.getRecurrenceId())))
				|| originalMeetingCopy.getNoOfTimeSlots() != upTodateMeeting.getNoOfTimeSlots()
				|| originalMeetingCopy.getMaxNumOfSlots().intValue() != upTodateMeeting.getMaxNumOfSlots().intValue()
				|| originalMeetingCopy.isSendEmailByOwner() != upTodateMeeting.isSendEmailByOwner()
				//|| originalMeetingCopy.getCoordinatorIds() !=null && !originalMeetingCopy.getCoordinatorIds().equals(upTodateMeeting.getCoordinatorIds())
				|| !((originalMeetingCopy.getDescription() == null && upTodateMeeting.getDescription() == null) || (originalMeetingCopy
						.getDescription() != null && upTodateMeeting.getDescription() != null)
						&& (originalMeetingCopy.getDescription().length() == upTodateMeeting.getDescription().length()))
				|| checkAttachmentsChanges(upTodateMeeting)
				|| checkAdvancedUserDefinedTSCase(originalMeetingCopy,upTodateMeeting,upTodateOrginMeetings)) {
			throw new SignupUserActionException(Utilities.rb.getString("someone.modified.event.content"));
		}
	}
	
	private boolean checkAdvancedUserDefinedTSCase(SignupMeeting originalMeetingCopy,SignupMeeting upTodateMeeting,
			List<SignupMeeting> upTodateOrginMeetings) throws SignupUserActionException{
		if (!upTodateMeeting.getMeetingType().equals(CUSTOM_TIMESLOTS) && !isUserDefinedTS()) {
			return false;
		}
		
		List<SignupTimeslot> origTSList = originalMeetingCopy.getSignupTimeSlots();		
		/*any unexpected events with timeslots changes on one of the recurrences
		 * for example, one TS is deleted via DB by other somehow.
		 * It happens very rarely and just for data integrity issue*/
		if(origTSList !=null){
			int numOf_ts = origTSList.size();
			for (SignupMeeting upMeeting : upTodateOrginMeetings) {
				if(numOf_ts != upMeeting.getNoOfTimeSlots()){
					throw new SignupUserActionException("One of the recurring event has been modified without synchronization with others! " +
							"You have to disassociate it from the recurring events. The event starting date:" + upMeeting.getStartTime().toLocaleString());
				}
			}
		}
		
		/*any changes by others*/
		List<SignupTimeslot> upTodateTSList = upTodateMeeting.getSignupTimeSlots();
		if(origTSList !=null && upTodateTSList !=null){
			for (SignupTimeslot oTS : origTSList) {
				if(oTS.getId() == null)
					continue;
				
				boolean found = false;
				for (SignupTimeslot uTS : upTodateTSList) {
					if(oTS.getId().equals(uTS.getId())){
						found = true;
						if(oTS.getMaxNoOfAttendees() != uTS.getMaxNoOfAttendees()){
							/*one of the TS Max# has been changed by others */
							return true; 
						}
						if(oTS.getStartTime().getTime() != uTS.getStartTime().getTime()
							||(oTS.getEndTime().getTime() != uTS.getEndTime().getTime())){
							return true;
						}
							
						break;
					}
				}
				
				if(!found){
					return true;//one TS is deleted or replaced by others
				}
			}
			
			return false;
		}
		
		return true;//any unexpected case
	}

	private boolean checkAttachmentsChanges(SignupMeeting latestOne){
		//TODO excluding attendee's attachment???
		List<SignupAttachment> latestList = getEventMainAttachments(latestOne.getSignupAttachments());
		List<SignupAttachment> orignalAttachList = getEventMainAttachments(this.originalMeetingCopy.getSignupAttachments());
		if(latestList.isEmpty() != orignalAttachList.isEmpty()){
			return true;
		}
		if(latestList.size() != orignalAttachList.size()){
			return true;
		}
		for (int i = 0; i < latestList.size(); i++) {
			Date latest = latestList.get(i).getLastModifiedDate();
			Date orignal = orignalAttachList.get(i).getLastModifiedDate();
			if(!latest.equals(orignal))
				return true;
		}
		
		return false;
	}
	/**
	 * setup the event/meeting's signup begin and deadline time and validate it
	 * too
	 */
	private void setSignupBeginDeadlineData(SignupMeeting meeting, int signupBegin, String signupBeginType,
			int signupDeadline, String signupDeadlineType) throws Exception {
		Date sBegin = Utilities.subTractTimeToDate(meeting.getStartTime(), signupBegin, signupBeginType);
		Date sDeadline = Utilities.subTractTimeToDate(meeting.getEndTime(), signupDeadline, signupDeadlineType);

		boolean origSignupStarted = originalMeetingCopy.getSignupBegins().before(new Date());// ????
		/* TODO have to pass it in?? */
		if (!START_NOW.equals(signupBeginType) && sBegin.before(new Date()) && !origSignupStarted) {
			// a warning for user
			Utilities.addErrorMessage(Utilities.rb.getString("warning.your.event.singup.begin.time.passed.today.time"));
		}

		if(!isSignupBeginModifiedByUser() && this.isStartNowTypeForRecurEvents){
			/*do nothing and keep the original value since the Sign-up process is already started
			 * No need to re-assign a new start_now value*/
		}else {
			meeting.setSignupBegins(sBegin);		
		}

		if (sBegin.after(sDeadline))
			throw new SignupUserActionException(Utilities.rb.getString("signup.deadline.is.before.signup.begin"));

		meeting.setSignupDeadline(sDeadline);
	}

	public int getCurrentNumberOfSlots() {
		return currentNumberOfSlots;
	}

	public void setCurrentNumberOfSlots(int currentNumberOfSlots) {
		this.currentNumberOfSlots = currentNumberOfSlots;
	}

	public int getDeadlineTime() {
		return deadlineTime;
	}

	public void setDeadlineTime(int deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

	public String getDeadlineTimeType() {
		return deadlineTimeType;
	}

	public void setDeadlineTimeType(String deadlineTimeType) {
		this.deadlineTimeType = deadlineTimeType;
	}

	public int getTimeSlotDuration() {
		return timeSlotDuration;
	}

	public void setTimeSlotDuration(int timeSlotDuration) {
		this.timeSlotDuration = timeSlotDuration;
	}

	public int getMaxNumOfAttendees() {
		return maxNumOfAttendees;
	}

	public void setMaxNumOfAttendees(int maxNumOfAttendees) {
		this.maxNumOfAttendees = maxNumOfAttendees;
	}

	public SignupMeeting getOriginalMeetingCopy() {
		return originalMeetingCopy;
	}

	public void setOriginalMeetingCopy(SignupMeeting originalMeetingCopy) {
		this.originalMeetingCopy = originalMeetingCopy;
	}

	public boolean isShowAttendeeName() {
		return showAttendeeName;
	}

	public void setShowAttendeeName(boolean showAttendeeName) {
		this.showAttendeeName = showAttendeeName;
	}

	public int getSignupBegins() {
		return signupBegins;
	}

	public void setSignupBegins(int signupBegins) {
		this.signupBegins = signupBegins;
	}

	public String getSignupBeginsType() {
		return signupBeginsType;
	}

	public void setSignupBeginsType(String signupBeginsType) {
		this.signupBeginsType = signupBeginsType;
	}

	/*
	 * public int getTotalEventDuration() { return totalEventDuration; }
	 * 
	 * public void setTotalEventDuration(int totalEventDuration) {
	 * this.totalEventDuration = totalEventDuration; }
	 */

	public boolean isUnlimited() {
		return unlimited;
	}

	public void setUnlimited(boolean unlimited) {
		this.unlimited = unlimited;
	}

	public boolean isConvertToNoRecurrent() {
		return convertToNoRecurrent;
	}

	public void setConvertToNoRecurrent(boolean convertToNoRecurrent) {
		this.convertToNoRecurrent = convertToNoRecurrent;
	}

	public List<SignupMeeting> getSavedMeetings() {
		return savedMeetings;
	}

	private void setSavedMeetings(List<SignupMeeting> savedMeetings) {
		this.savedMeetings = savedMeetings;
	}

	public List<SignupAttachment> getCurrentAttachList() {
		return currentAttachList;
	}

	public void setCurrentAttachList(List<SignupAttachment> currentAttachList) {
		this.currentAttachList = currentAttachList;
	}
	
	public AttachmentHandler getAttachmentHandler() {
		return attachmentHandler;
	}
	
	public boolean isSignupBeginModifiedByUser() {
		return signupBeginModifiedByUser;
	}

	public void setSignupBeginModifiedByUser(boolean signupBeginModifiedByUser) {
		this.signupBeginModifiedByUser = signupBeginModifiedByUser;
	}

	private void retrieveRecurrenceData(List<SignupMeeting> upTodateOrginMeetings) {
		/*to see if the recurring events have a 'Start_Now' type already*/
		this.isStartNowTypeForRecurEvents = Utilities.testSignupBeginStartNowType(upTodateOrginMeetings);
		 
		String repeatType = upTodateOrginMeetings.get(0).getRepeatType();
		if(repeatType !=null && !ONCE_ONLY.equals(repeatType)){
			int lSize = upTodateOrginMeetings.size();
			setLastRecurMeetingModifyDate(upTodateOrginMeetings.get(lSize - 1).getStartTime());
			setRecurringType(repeatType);
			return;
		}
			
		/*The following code is to make it old version backward compatible*/
		setFirstRecurMeetingModifyDate(null);
		setLastRecurMeetingModifyDate(null);
		if (upTodateOrginMeetings == null || upTodateOrginMeetings.isEmpty())
			return;

		setFirstRecurMeetingModifyDate(upTodateOrginMeetings.get(0).getStartTime());
		/* in case: it's the last one */
		setLastRecurMeetingModifyDate(upTodateOrginMeetings.get(0).getStartTime());

		int listSize = upTodateOrginMeetings.size();
		if (listSize > 1) {
			setLastRecurMeetingModifyDate(upTodateOrginMeetings.get(listSize - 1).getStartTime());
			Calendar calFirst = Calendar.getInstance();
			Calendar calSecond = Calendar.getInstance();
			/*
			 * we can only get approximate estimation by assuming it's a
			 * succession
			 */
			calFirst.setTime(upTodateOrginMeetings.get(listSize - 2).getStartTime());
			calFirst.set(Calendar.SECOND, 0);
			calFirst.set(Calendar.MILLISECOND, 0);
			calSecond.setTime(upTodateOrginMeetings.get(listSize - 1).getStartTime());
			calSecond.set(Calendar.SECOND, 0);
			calSecond.set(Calendar.MILLISECOND, 0);
			int tmp = calSecond.get(Calendar.DATE);
			int daysDiff = (int) (calSecond.getTimeInMillis() - calFirst.getTimeInMillis()) / DAY_IN_MILLISEC;
			if (daysDiff == perDay)
				setRecurringType(DAILY);
			else if (daysDiff == perWeek)
				setRecurringType(WEEKLY);
			else if (daysDiff == perBiweek)
				setRecurringType(BIWEEKLY);
			else if(daysDiff ==3 && calFirst.get(Calendar.DAY_OF_WEEK)== Calendar.FRIDAY)
				setRecurringType(WEEKDAYS);
		}
	}

	private Date getFirstRecurMeetingModifyDate() {
		return firstRecurMeetingModifyDate;
	}

	private void setFirstRecurMeetingModifyDate(Date firstRecurMeetingModifyDate) {
		this.firstRecurMeetingModifyDate = firstRecurMeetingModifyDate;
	}

	private Date getLastRecurMeetingModifyDate() {
		return lastRecurMeetingModifyDate;
	}

	private void setLastRecurMeetingModifyDate(Date lastRecurMeetingModifyDate) {
		this.lastRecurMeetingModifyDate = lastRecurMeetingModifyDate;
	}

	private String getRecurringType() {
		return recurringType;
	}

	private void setRecurringType(String recurringType) {
		this.recurringType = recurringType;
	}
	
	
	private void updateWithOrigalAttachments(SignupMeeting sMeeting, List<SignupAttachment> currentAttachList, int recurNum){
		List<SignupAttachment> upToDateList = sMeeting.getSignupAttachments();
		if(upToDateList ==null)
			upToDateList = new ArrayList<SignupAttachment>();
		
		if(currentAttachList ==null)
			currentAttachList = new ArrayList<SignupAttachment>();
		
		/*case: original one has no attachments*/
		if(upToDateList.isEmpty()){
			for (SignupAttachment curAttach : currentAttachList) {
				upToDateList.add(getAttachmentHandler().copySignupAttachment(sMeeting,true,curAttach, ATTACH_MODIFY + recurNum));
			}
			sMeeting.setSignupAttachments(upToDateList);
			return;
		}
		
		if(recurNum == 0){
			for (int i = upToDateList.size()-1; i >=0; i--) {
				String upToDateResourceId = upToDateList.get(i).getResourceId();
				int index = upToDateResourceId.lastIndexOf("/");
				if(index > -1){
					upToDateResourceId = upToDateResourceId.substring(0,index+1) + ATTACH_TEMP +"/" + upToDateResourceId.substring(index+1, upToDateResourceId.length());
				}
				boolean found=false;
				for (Iterator iter = currentAttachList.iterator(); iter.hasNext();) {
					SignupAttachment mdOne = (SignupAttachment) iter.next();
					String tm=mdOne.getResourceId();
					if(upToDateResourceId.equals(mdOne.getResourceId())){
						found=true;
						this.needToCleanUpAttachInContentHS.add(mdOne);
						/*duplicated one with original and keep original one*/
						iter.remove();
						break;
					}
				}
				if(!found){
					if(upToDateList.get(i).getTimeslotId() ==null){
						/*not there any more but not the attendee's attachments*/
						this.needToCleanUpAttachInContentHS.add(upToDateList.get(i));
						upToDateList.remove(i);
					}
				}
			}
			/*unchanged attachments needed to copy over for recurrence events purpose*/
			List<SignupAttachment> tempList = new ArrayList<SignupAttachment>(upToDateList);
			
			for (SignupAttachment curAttach : currentAttachList) {
				upToDateList.add(getAttachmentHandler().copySignupAttachment(sMeeting,true,curAttach, ATTACH_MODIFY + recurNum));
			}
			/*get currentAttachList contain all and ready for upcoming recurring events*/
			for (SignupAttachment tmp : tempList) {
				if(tmp.getTimeslotId() ==null)
					currentAttachList.add(getAttachmentHandler().copySignupAttachment(sMeeting,true,tmp, ATTACH_TEMP ));//recurring purpose
			}
			
		}
		else{
			/*case: after first recurring events */
			/*clean up*/
			if(upToDateList.size()>0){
				for (int i = upToDateList.size()-1; i >=0; i--) {
					SignupAttachment one = upToDateList.get(i);			
					if(one.getTimeslotId()==null){//only cleanup the main attachments
						this.needToCleanUpAttachInContentHS.add(one);
						upToDateList.remove(i);
					}
				}
			}
			for (SignupAttachment curAttach : currentAttachList) {
				upToDateList.add(getAttachmentHandler().copySignupAttachment(sMeeting,true,curAttach, ATTACH_MODIFY + recurNum));
			}
		}
		
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

	public UserDefineTimeslotBean getUserDefineTimeslotBean() {
		return userDefineTimeslotBean;
	}

	public void setUserDefineTimeslotBean(UserDefineTimeslotBean userDefineTimeslotBean) {
		this.userDefineTimeslotBean = userDefineTimeslotBean;
	}

	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	public boolean isCreateGroups() {
		return createGroups;
	}

	public void setCreateGroups(boolean createGroups) {
		this.createGroups = createGroups;
	}

	public String getCoordinators() {
		return coordinators;
	}

	public void setCoordinators(String coordinators) {
		this.coordinators = coordinators;
	}

	public boolean isSendEmailByOwner() {
		return sendEmailByOwner;
	}

	public void setSendEmailByOwner(boolean sendEmailByOwner) {
		this.sendEmailByOwner = sendEmailByOwner;
	}
	
	
}


