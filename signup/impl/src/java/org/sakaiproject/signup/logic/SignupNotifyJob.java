/**
 * Copyright (c) 2007-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package org.sakaiproject.signup.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.signup.dao.SignupMeetingDao;
import org.sakaiproject.signup.logic.messages.AutoReminderEmail;
import org.sakaiproject.signup.logic.messages.OrganizerPreAssignEmail;
import org.sakaiproject.signup.logic.messages.SignupEmailNotification;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * 
 * @author Peter Liu
 */
@Slf4j
public class SignupNotifyJob implements Job {

	private EmailService emailService;

	private SignupMeetingDao signupMeetingDao;

	private SakaiFacade sakaiFacade;
	
	private UserDirectoryService userDirectoryService;

	private static final int HOURS_IN_ADVANCE = 24;
	
	private static final int ONE_DAY_INTERVAL=24;
	
	/*maximum number of events, which will be occurred at the same day*/
	private static final int MAX_EVENTS_LIMITS = 2000;
	

	/** Creates a new instance */
	public SignupNotifyJob() {
	}

	/** init method */
	public void init() {
	}

	/** destroy method */
	public void destroy() {
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.warn("Starting Signup Auto Reminder Notification job");

		List<SignupMeeting> signupMeetings = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		int currentMinutes = calendar.get(Calendar.MINUTE);
		calendar.add(Calendar.HOUR, -1 * currentHour);
		calendar.add(Calendar.MINUTE, -1 * currentMinutes);
		
		/*If the cronJob somehow not set up at morning time and instead at evening time*/
		if(currentHour > 20){
			/*notification is sent out after 8 PM, we will advance it one more day 
			 * since most user will read the email next day.
			 * */
			calendar.add(Calendar.HOUR, ONE_DAY_INTERVAL); 
		}
		
		calendar.add(Calendar.HOUR, HOURS_IN_ADVANCE); //next day 0 hour
		Date searchStarDate = calendar.getTime();
		
		calendar.add(Calendar.HOUR, ONE_DAY_INTERVAL);//next day 24 hours
		Date searchEndDate = calendar.getTime();
					
		int totalCounts = signupMeetingDao.getAutoReminderTotalEventCounts(searchStarDate, searchEndDate);
		if(totalCounts ==0){
			log.info("There is no upcoming event today for Signup Auto Reminder Notification");
			return;
		}
		/*safeguard for memory issue if there is some catastrophic failure with DB Query*/
		if(totalCounts > MAX_EVENTS_LIMITS){
			log.error("Notification will not be processed. The total upcoming events:" + totalCounts +" exceed the maximum process limits:" + MAX_EVENTS_LIMITS 
					+". Please check the DB errors or increase the maximum limit for notifiction process.");
			return;
		}
		
		int totalEmails = 0;
		signupMeetings = signupMeetingDao.getAutoReminderSignupMeetings(searchStarDate,searchEndDate);
		if (signupMeetings !=null){		
			for (SignupMeeting sm : signupMeetings) {
				List<SignupTimeslot> tsList = sm.getSignupTimeSlots();
				if(tsList !=null){
					for (SignupTimeslot tsItem : tsList) {
						if(tsItem !=null && tsItem.getStartTime().before(searchEndDate) && tsItem.getStartTime().after(searchStarDate)){
							List<SignupAttendee> attendees = tsItem.getAttendees();//itsItem===null
							if(attendees !=null){
								for (SignupAttendee att : attendees) {
									String userId = att.getAttendeeUserId();
									String siteId = att.getSignupSiteId();
									User user = null;
									try {
										user = userDirectoryService.getUser(userId);
										AutoReminderEmail email = new AutoReminderEmail(user, tsItem, sm, siteId, getSakaiFacade());
										sendEmail(user, email);
										totalEmails++;
										//eventTracking?
									} catch (UserNotDefinedException e) {
										log.warn("User is not found for userId: " + userId);
									}							
								}//for-loop
							}
						}
					}//for-loop
				}
			}//for-loop
		}			

		log.warn("Completed Signup Auto Reminder Notification job with total events:" + totalCounts + " and outgoing emails:" + totalEmails + ".");
	}

	/* send email via Sakai email Service */
	private List<User> userlist=new ArrayList<User>();
	private void sendEmail(User user, SignupEmailNotification email) {
		userlist.clear();
		userlist.add(user);
		emailService.sendToUsers(userlist, email.getHeader(), email.getMessage());
	}

	public SignupMeetingDao getSignupMeetingDao() {
		return signupMeetingDao;
	}

	public void setSignupMeetingDao(SignupMeetingDao signupMeetingDao) {
		this.signupMeetingDao = signupMeetingDao;
	}

	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}	

}
