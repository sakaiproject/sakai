/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/tool/src/java/org/sakaiproject/signup/tool/jsf/SignupUIBaseBean.java $
 * $Id: SignupUIBaseBean.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
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
package org.sakaiproject.signup.tool.jsf;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupCalendarHelper;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.attachment.AttachmentHandler;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This is a abstract base class for JSF Signup tool UIBean. It provides some
 * must-have or common used methods such as getMeetingWrapper(), sakakFacade
 * etc.
 * </P>
 */
abstract public class SignupUIBaseBean implements SignupBeanConstants, SignupMessageTypes, MeetingTypes {

	protected SakaiFacade sakaiFacade;

	protected SignupMeetingService signupMeetingService;
	
	@Getter @Setter
	protected SignupCalendarHelper calendarHelper;
	
	private AttachmentHandler attachmentHandler;

	protected SignupMeetingWrapper meetingWrapper;

	protected List<TimeslotWrapper> timeslotWrappers;

	protected TimeslotWrapper timeslotWrapper;

	protected boolean currentUserSignedup;

	protected static boolean DEFAULT_SEND_EMAIL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal(
										"signup.default.email.notification", "true")) ? true : false;

	protected static boolean DEFAULT_EXPORT_TO_CALENDAR_TOOL = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.default.export.to.calendar.setting", "true")) ? true : false;
	
	protected boolean publishToCalendar = DEFAULT_EXPORT_TO_CALENDAR_TOOL;
	
	protected boolean sendEmail = DEFAULT_SEND_EMAIL;

	protected Log logger = LogFactoryImpl.getLog(getClass());

	protected Boolean publishedSite;
	
	//protected boolean sendEmailAttendeeOnly = false;
	
	protected String sendEmailToSelectedPeopleOnly = SEND_EMAIL_ALL_PARTICIPANTS;
	
	private int maxSlots; 

	private int maxAttendeesPerSlot;
	
	protected String customLocation;
		
	protected String customCategory;
	
	protected static final String ICS_MIME_TYPE="text/calendar";


	/**
	 * This method will get the most updated event/meeting data and handle all
	 * the wrapping process for UI. Due to effeciency, the data is only
	 * guarantied fresh at a 10 minutes interval (defined by
	 * <b>dataRefreshInterval</b> value).
	 * 
	 * @return a SignupMeetingWrapper object.
	 */
	public SignupMeetingWrapper getMeetingWrapper() {
		if (meetingWrapper != null && meetingWrapper.isRefresh()) {
			try {
				SignupMeeting meeting = signupMeetingService.loadSignupMeeting(meetingWrapper.getMeeting().getId(),
						sakaiFacade.getCurrentUserId(), sakaiFacade.getCurrentLocationId());
				meetingWrapper.setMeeting(meeting);
				updateTimeSlotWrappers(meetingWrapper);
			} catch (Exception e) {
				Utilities.addErrorMessage(Utilities.rb.getString("db.error_or_event.notExisted"));
				logger.error(Utilities.rb.getString("db.error_or_event.notExisted") + " - " + e.getMessage());
			}
		}
		return meetingWrapper;
	}

	/** process new data into Timeslot wrapper for UI purpose */
	protected void updateTimeSlotWrappers(SignupMeetingWrapper meetingWrapper) {
		SignupMeeting meeting = this.meetingWrapper.getMeeting();
		if (meeting == null)
			return;

		List timeslots = meeting.getSignupTimeSlots();
		if (timeslots == null)
			return;

		List<TimeslotWrapper> timeslotWrapperList = new ArrayList<TimeslotWrapper>();
		setCurrentUserSignedup(false);// reset and make sure to capture new
		// changes
		int i = 0;
		int totalSignedupSlots=0;
		for (Iterator iter = timeslots.iterator(); iter.hasNext();) {
			SignupTimeslot elm = (SignupTimeslot) iter.next();
			TimeslotWrapper tsw = new TimeslotWrapper(elm, sakaiFacade.getCurrentUserId());
			
			List<AttendeeWrapper> attendeeWrp = new ArrayList<AttendeeWrapper>();
			int posIndex = 0;
			//clean the list
			List<SignupAttendee> cleanedList = getValidAttendees(elm.getAttendees());
			for (SignupAttendee attendee : cleanedList) {
				AttendeeWrapper attWrp = new AttendeeWrapper(attendee, sakaiFacade.getUserDisplayName(attendee
						.getAttendeeUserId()));
				attWrp.setPositionIndex(posIndex++);
				attendeeWrp.add(attWrp);

				/* current user is already signed up in one of the timeslot */
				if (attendee.getAttendeeUserId().equals(sakaiFacade.getCurrentUserId()))
					//setCurrentUserSignedup(true);
					totalSignedupSlots++;
			}
			tsw.setAttendeeWrappers(attendeeWrp);

			tsw.setWaitingList(wrapWaiters(elm.getWaitingList()));
			tsw.setPositionInTSlist(i++);
			timeslotWrapperList.add(tsw);

		}
		int preferredSlot = meeting.getMaxNumOfSlots();
		if (totalSignedupSlots >= preferredSlot){
			setCurrentUserSignedup(true);
		}
		setTimeslotWrappers(timeslotWrapperList);

	}

	/** process the new data into Meeting wrapper for UI purpose */
	protected String updateMeetingwrapper(SignupMeeting meeting, String destinationUrl) {
		/* if null,reload due to exception */
		try {
			if (meeting == null)
				meeting = signupMeetingService.loadSignupMeeting(this.meetingWrapper.getMeeting().getId(), sakaiFacade
						.getCurrentUserId(), sakaiFacade.getCurrentLocationId());

			getMeetingWrapper().setMeeting(meeting);
			getMeetingWrapper().resetAvailableStatus();// re-process avail.
			// status
			updateTimeSlotWrappers(getMeetingWrapper());
			return destinationUrl;
		} catch (Exception e) {
			Utilities.addErrorMessage(Utilities.rb.getString("db.error_or_event.notExisted"));
			logger.warn(Utilities.rb.getString("db.error_or_event.notExisted") + " - " + e.getMessage());
			Utilities.resetMeetingList();
			return MAIN_EVENTS_LIST_PAGE_URL;
		}
	}
	
	protected void updateSignupAttachmentWrapper(SignupMeeting meeting){
		
	}

	/**
	 * setup the event/meeting's signup begin and deadline time and validate it
	 * too
	 */
	protected void setSignupBeginDeadlineData(SignupMeeting meeting, int signupBegin, String signupBeginType,
			int signupDeadline, String signupDeadlineType) throws Exception {
		Date sBegin = Utilities.subTractTimeToDate(meeting.getStartTime(), signupBegin, signupBeginType);
		Date sDeadline = Utilities.subTractTimeToDate(meeting.getEndTime(), signupDeadline, signupDeadlineType);

		if (!START_NOW.equals(signupBeginType) && sBegin.before(new Date())) {
			// a warning for user
			Utilities.addErrorMessage(Utilities.rb.getString("warning.your.event.singup.begin.time.passed.today.time"));
		}

		meeting.setSignupBegins(sBegin);

		if (sBegin.after(sDeadline))
			throw new SignupUserActionException(Utilities.rb.getString("signup.deadline.is.before.signup.begin"));

		meeting.setSignupDeadline(sDeadline);
	}
	
	public boolean isMeetingOverRepeatPeriod(Date startTime, Date endTime, int repeatPeriodInDays){
		long duration= endTime.getTime()- startTime.getTime();
		if( 24*repeatPeriodInDays - duration /(MINUTE_IN_MILLISEC * Hour_In_MINUTES) >= 0  )
			return false;
		
		return true;
	}

	/** convert SignupAttendee to AttendeeWrapper object */
	/*private List<AttendeeWrapper> wrapAttendees(List<SignupAttendee> attendees) {
		List<AttendeeWrapper> attendeeWrp = new ArrayList<AttendeeWrapper>();
		int posIndex = 0;
		
		//clean the list
		List<SignupAttendee> cleanedList = getValidAttendees(attendees);
		
		for (SignupAttendee attendee : cleanedList) {
			AttendeeWrapper attWrp = new AttendeeWrapper(attendee, sakaiFacade.getUserDisplayName(attendee.getAttendeeUserId()));
			attWrp.setPositionIndex(posIndex++);
			attendeeWrp.add(attWrp);

			// current user is already signed up in one of the timeslot 
			if (attendee.getAttendeeUserId().equals(sakaiFacade.getCurrentUserId())) {
				setCurrentUserSignedup(true);
			}
		}
		return attendeeWrp;
	}*/

	/** convert SignupAttendee to AttendeeWrapper object */
	private List<AttendeeWrapper> wrapWaiters(List<SignupAttendee> attendees) {
		List<AttendeeWrapper> attendeeWrp = new ArrayList<AttendeeWrapper>();
		for (SignupAttendee attendee : attendees) {
			attendeeWrp
					.add(new AttendeeWrapper(attendee, sakaiFacade.getUserDisplayName(attendee.getAttendeeUserId())));
		}

		return attendeeWrp;
	}

	/**
	 * This is a setter.
	 * 
	 * @param meetingWrapper
	 *            a SignupMeetingWrapper object.
	 */
	public void setMeetingWrapper(SignupMeetingWrapper meetingWrapper) {
		this.meetingWrapper = meetingWrapper;
	}

	/**
	 * Get a SakaiFacade object.
	 * 
	 * @return a SakaiFacade object.
	 */
	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
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
	 * This is a setter.
	 * 
	 * @param sakaiFacade
	 *            a SakaiFacade object.
	 */
	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	/**
	 * Get a SignupMeetingService object.
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
	 * This is for UI purpose to see if current user has signed up in the
	 * event/meeting.
	 */
	public boolean isCurrentUserSignedup() {
		return currentUserSignedup;
	}

	/**
	 * This is a setter.
	 * 
	 * @param currentUserSignedup
	 *            a boolean value.
	 */
	public void setCurrentUserSignedup(boolean currentUserSignedup) {
		this.currentUserSignedup = currentUserSignedup;
	}

	/**
	 * Get a list of TimeslotWrapper objects.
	 * 
	 * @return a list of TimeslotWrapper objects.
	 */
	public List<TimeslotWrapper> getTimeslotWrappers() {
		return timeslotWrappers;
	}

	/**
	 * This is a setter.
	 * 
	 * @param timeslotWrappers
	 *            a list of TimeslotWrapper objects.
	 */
	public void setTimeslotWrappers(List<TimeslotWrapper> timeslotWrappers) {
		this.timeslotWrappers = timeslotWrappers;
	}

	/**
	 * This is only for UI purpose to check if the event/meeting is an open
	 * session style and signup is not required.
	 */
	public boolean getAnnouncementType() {
		boolean anoun = false;
		if (meetingWrapper !=null && meetingWrapper.getMeeting() !=null 
				&& ANNOUNCEMENT.equals(meetingWrapper.getMeeting().getMeetingType()))
			anoun= true;
		
		return anoun;
	}

	/**
	 * This is only for UI purpose to check if the event/meeting is an
	 * individual style (manay time slots) and it requires signup.
	 */
	public boolean getIndividualType() {
		return INDIVIDUAL.equals(meetingWrapper.getMeeting().getMeetingType());
	}

	/**
	 * This is only for UI purpose to check if the event/meeting is an group
	 * style (only one time slot) and it requires signup.
	 */
	public boolean getGroupType() {
		return GROUP.equals(meetingWrapper.getMeeting().getMeetingType());
	}
	
	/**
	 * This is only for UI purpose to check if the event/meeting is an
	 * individual style (manay time slots) and it requires signup.
	 */
	public boolean getCustomTsType() {
		return CUSTOM_TIMESLOTS.equals(meetingWrapper.getMeeting().getMeetingType());
	}

	/**
	 * Get a TimeslotWrapper object for UI.
	 * 
	 * @return an TimeslotWrapper object.
	 */
	public TimeslotWrapper getTimeslotWrapper() {
		return timeslotWrapper;
	}

	/**
	 * This is a setter.
	 * 
	 * @param timeslotWrapper
	 *            a TimeslotWrapper object.
	 */
	public void setTimeslotWrapper(TimeslotWrapper timeslotWrapper) {
		this.timeslotWrapper = timeslotWrapper;
	}

	/**
	 * Check if email should be sent away. This is used by organizer of an
	 * event/meeting.
	 * 
	 * @return true if email should be sent away.
	 */
	public boolean isSendEmail() {
		if (!getPublishedSite())
			sendEmail = false;

		return sendEmail;
	}

	/**
	 * This is a setter.
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
				logger.warn(e.getMessage());
				this.publishedSite = new Boolean(false);

			}
		}

		return publishedSite.booleanValue();
	}


	public void cleanUpUnusedAttachmentCopies(List<SignupAttachment> attachList){
		if(attachList !=null){
			for (SignupAttachment attach : attachList) {
				getAttachmentHandler().removeAttachmentInContentHost(attach);
			}
			attachList.clear();
		}
	}
	
	public boolean getSignupAttachmentEmpty(){
		return this.meetingWrapper.getEmptyEventMainAttachment();
	}

	public AttachmentHandler getAttachmentHandler() {
		return attachmentHandler;
	}

	public void setAttachmentHandler(AttachmentHandler attachmentHandler) {
		this.attachmentHandler = attachmentHandler;
	}
	
	protected void markerTimeslots(List<TimeslotWrapper> TimeSlotWrpList){
		int i=0;
		if(TimeSlotWrpList !=null){
			for (TimeslotWrapper tsWrp : TimeSlotWrpList) {
				tsWrp.setTsMarker(i);
				i++;
			}
		}
	}

	public boolean isPublishToCalendar() {
		return publishToCalendar;
	}

	public void setPublishToCalendar(boolean publishToCalendar) {
		this.publishToCalendar = publishToCalendar;
	}
	
	/*public boolean getSendEmailAttendeeOnly() {
		return sendEmailAttendeeOnly;
	}

	public void setSendEmailAttendeeOnly(boolean sendEmailAttendeeOnly) {
		this.sendEmailAttendeeOnly = sendEmailAttendeeOnly;
	}*/
	
	public String getSendEmailToSelectedPeopleOnly() {
		return sendEmailToSelectedPeopleOnly;
	}

	public void setSendEmailToSelectedPeopleOnly(
			String sendEmailToSelectedPeopleOnly) {
		this.sendEmailToSelectedPeopleOnly = sendEmailToSelectedPeopleOnly;
	}
	
	/**
	 * Clean the list of attendees by checking that each user is valid
	 * @param attendees	List of attendees to be cleaned
	 * @return	the cleaned list
	 */
	public List<SignupAttendee> getValidAttendees(List<SignupAttendee> attendees) {
		List<SignupAttendee> cleanedList = new ArrayList<SignupAttendee>();
		
		for(SignupAttendee attendee: attendees){
			if(sakaiFacade.checkForUser(attendee.getAttendeeUserId())) {
				cleanedList.add(attendee);
			}
		}
		
		return cleanedList;
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
		List<User> users = sakaiFacade.getUsersByEmail(email);
		
		List<String> eids = new ArrayList<String>();
		for(User u:users) {
			eids.add(u.getEid());
		}
		
		return eids;
	}
	
	// Generate a group title based on the input given
	public String generateGroupTitle(String meetingTitle, SignupTimeslot timeslot) {
		
		final char SEPARATOR = '-';
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(meetingTitle);
		sb.append(SEPARATOR);
		sb.append(df.format(timeslot.getStartTime()));
		sb.append(SEPARATOR);
		sb.append(df.format(timeslot.getEndTime()));
		
		return sb.toString();
	}
	
	//generate a group description
	public String generateGroupDescription(String meetingTitle, SignupTimeslot timeslot) {
		return Utilities.rb.getString("group.description.default");
	}
	//convert a list of SignupAttendees to a list of userIds
	public List<String> convertAttendeesToUuids(List<SignupAttendee> attendees) {
		
		List<String> uuids = new ArrayList<String>();
		
		for(SignupAttendee a: attendees) {
			uuids.add(a.getAttendeeUserId());
		}
		
		return uuids;
	}
	
	//convert a list of AttendeeWrappers to a list of userIds
	public List<String> convertAttendeeWrappersToUuids(List<AttendeeWrapper> attendees) {
		
		List<String> uuids = new ArrayList<String>();
		
		for(AttendeeWrapper a: attendees) {
			uuids.add(a.getSignupAttendee().getAttendeeUserId());
		}
		
		return uuids;
	}

	
	/**
	 * Helper to get a formatted string of all attendee email addresses for all tineslots
	 * so we can use them in a mailto link
	 * @return String of all email addresses
	 */
	public String getAllAttendeesEmailAddressesFormatted() {
		
		Set<String> emails = new HashSet<String>();
		
		StringBuilder sb = new StringBuilder();
		for (TimeslotWrapper tsWrapper : timeslotWrappers) {
			for(AttendeeWrapper atWrapper : tsWrapper.getAttendeeWrappers()) {
				String email = atWrapper.getAttendeeEmail();
				if(StringUtils.isNotBlank(email)){
					emails.add(email);
				}
			}
		}
		
		for(String e: emails) {
			sb.append(e);
			//for compatibility with Outlook, this should be a semicolon not a comma as per the RFC. 
			//Also tested in Thunderbird, Yahoo and GMail.
			sb.append(';'); 
		}
		
		//trim off last , and return
		return StringUtils.removeEnd(sb.toString(), ";");
	}
	
	/**
	 * Generate and send for download an ICS file for the meeting. Contains no timeslots, just the meeting itself.
	 * This method is in this particular bean because 1. We have access to the meeting here, and 2. it is used in more than one sub-bean.
	 */
	public void downloadICSForMeeting() {
		String filePath;
		try{	
			filePath = calendarHelper.createCalendarFile(Collections.singletonList(calendarHelper.generateVEventForMeeting(meetingWrapper.getMeeting())));
		}catch(NullPointerException ne){
			handleICSDownloadWarningToUser();
			return;
		}
		
		if(StringUtils.isNotBlank(filePath)) {
			logger.debug("filepath: " + filePath);
			sendDownload(filePath, ICS_MIME_TYPE);
		} else {
			logger.error("Could not generate file for download");
			//TODO this could set an error and return perhaps.
		}
		
	}
	
	public void downloadICSForMeetingTimeSlot(TimeslotWrapper timeslotWrapper) {
		String filePath;
		try{	
			filePath = calendarHelper.createCalendarFile(Collections.singletonList(calendarHelper.generateVEventForTimeslot(meetingWrapper.getMeeting(), timeslotWrapper.getTimeSlot())));;
		}catch(NullPointerException ne){
			handleICSDownloadWarningToUser();
			return;
		}
		
		if(StringUtils.isNotBlank(filePath)) {
			logger.debug("filepath: " + filePath);
			sendDownload(filePath, ICS_MIME_TYPE);
		} else {
			logger.error("Could not generate file for download");
			//TODO this could set an error and return perhaps.
		}
		
	}
	
	private void handleICSDownloadWarningToUser(){
		logger.error("The Scheduler tool may not be availabe for the site");
		String warningFileName = Utilities.rb.getString("ics_file_name_for_missing_scheduler_tool");
		String warningMsg = Utilities.rb.getString("ics_warning_msg_for_missing_scheduler_tool");			
		sendDownloadWarning(warningFileName,warningMsg);
	}
	
	/**
	 * Send a file for download. 
	 * 
	 * @param filePath
	 * 
	 */
	protected void sendDownload(String filePath, String mimeType) {

		FacesContext fc = FacesContext.getCurrentInstance();
		ServletOutputStream out = null;
		
		String filename = StringUtils.substringAfterLast(filePath, File.separator);
		
		try {
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
			
			response.reset();
			response.setHeader("Pragma", "public");
			response.setHeader("Cache-Control","public, must-revalidate, post-check=0, pre-check=0, max-age=0"); 
			response.setContentType(mimeType);
			response.setHeader("Content-disposition", "attachment; filename=" + filename);
			
			out = response.getOutputStream();

			IOUtils.copy(FileUtils.openInputStream(new File(filePath)), out);

			out.flush();
		} catch (IOException ex) {
			logger.warn("Error generating file for download:" + ex.getMessage());
		} finally {
			IOUtils.closeQuietly(out);
		}
		fc.responseComplete();
		
	}
	
	/**
	 * Send a warning message to user about failed ICS file generation
	 * @param fileName
	 * @param warningMsg
	 */
	protected void sendDownloadWarning(String fileName, String warningMsg) {

		FacesContext fc = FacesContext.getCurrentInstance();
		ServletOutputStream out = null;
		
		
		try {
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
			
			response.reset();
			response.setHeader("Pragma", "public");
			response.setHeader("Cache-Control","public, must-revalidate, post-check=0, pre-check=0, max-age=0"); 
			response.setContentType("text/plain");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName);
			
			out = response.getOutputStream();
			warningMsg= warningMsg!=null? warningMsg:"Missing Scheduler tool on site";
			out.print(warningMsg);

			out.flush();
		} catch (IOException ex) {
			logger.warn("Error generating file for download:" + ex.getMessage());
		} finally {
			try{
				out.close();
			}catch (Exception e){
				//do nothing;
			}
		}
		fc.responseComplete();
		
	}
	
	/**
	 * Is ICS calendar generation enabled in the external calendaring service?
	 * @return	true/false
	 */
	public boolean isIcsEnabled() {
		return calendarHelper.isIcsEnabled();
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

	public String getCustomLocation() {
		return customLocation;
	}

	public void setCustomLocation(String customLocation) {
		this.customLocation = customLocation;
	}

	public String getCustomCategory() {
		return customCategory;
	}

	public void setCustomCategory(String customCategory) {
		this.customCategory = customCategory;
	}

}
