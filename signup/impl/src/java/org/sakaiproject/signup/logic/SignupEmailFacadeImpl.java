/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Yale University
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
package org.sakaiproject.signup.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.signup.logic.messages.AddAttendeeEmail;
import org.sakaiproject.signup.logic.messages.AttendeeCancellationEmail;
import org.sakaiproject.signup.logic.messages.AttendeeSignupEmail;
import org.sakaiproject.signup.logic.messages.CancellationEmail;
import org.sakaiproject.signup.logic.messages.ModifyMeetingEmail;
import org.sakaiproject.signup.logic.messages.MoveAttendeeEmail;
import org.sakaiproject.signup.logic.messages.NewMeetingEmail;
import org.sakaiproject.signup.logic.messages.OrganizerPreAssignEmail;
import org.sakaiproject.signup.logic.messages.PromoteAttendeeEmail;
import org.sakaiproject.signup.logic.messages.SignupEmailNotification;
import org.sakaiproject.signup.logic.messages.SignupEventTrackingInfo;
import org.sakaiproject.signup.logic.messages.SwapAttendeeEmail;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <P>
 * This is an implementation of SignupEmailFacade interface, which provided
 * methods for Signup tool to send emails out via emailService
 * </P>
 */
public class SignupEmailFacadeImpl implements SignupEmailFacade {

	private EmailService emailService;

	private UserDirectoryService userDirectoryService;

	private SakaiFacade sakaiFacade;

	private Log logger = LogFactoryImpl.getLog(getClass());

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void sendEmailAllUsers(SignupMeeting meeting, String messageType) throws Exception {
		if (messageType.equals(SIGNUP_NEW_MEETING)) {
			sendEmailToAllUsers(meeting, messageType);
			return;
		}

		if (messageType.equals(SIGNUP_PRE_ASSIGN)) {
			sendEmailToAssignedUsers(meeting);
			return;
		}

		if (messageType.equals(SIGNUP_MEETING_MODIFIED)) {
			sendEmailToAllUsers(meeting, messageType);
			return;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEmailToOrganizer(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
		List<SignupTrackingItem> sigupTList = signupEventTrackingInfo.getAttendeeTransferInfos();
		for (SignupTrackingItem item : sigupTList) {
			if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP)) {
				User creator = null;
				User participant = null;
				try {
					creator = userDirectoryService.getUser(signupEventTrackingInfo.getMeeting().getCreatorUserId());
					participant = userDirectoryService.getUser(item.getAttendee().getAttendeeUserId());

					SignupEmailNotification email = new AttendeeSignupEmail(creator, participant,
							signupEventTrackingInfo.getMeeting(), item.getAddToTimeslot(), this.sakaiFacade);

					sendEmail(creator, email);
				} catch (UserNotDefinedException e) {
					throw new Exception("User is not found and Email is not sent away for oraginzer userId:"
							+ signupEventTrackingInfo.getMeeting().getCreatorUserId());
				}
			}
		}

		return;

	}

	/**
	 * {@inheritDoc}
	 */
	public void sendCancellationEmail(SignupEventTrackingInfo signupEventTrackingInfo) throws Exception {
		/* send email to everyone who get promoted during the process */
		//TODO Do we need to send info about promoted guys to organizer?
		List<SignupTrackingItem> sigupTList = signupEventTrackingInfo.getAttendeeTransferInfos();
		for (SignupTrackingItem item : sigupTList) {
			/* no email send to the cancellation guy */
			if (item.getMessageType().equals(SIGNUP_ATTENDEE_PROMOTE)) {
				User attendee = null;
				try {
					attendee = userDirectoryService.getUser(item.getAttendee().getAttendeeUserId());
					PromoteAttendeeEmail email = new PromoteAttendeeEmail(attendee, item, signupEventTrackingInfo
							.getMeeting(), this.sakaiFacade);
					sendEmail(attendee, email);
				} catch (UserNotDefinedException e) {
					throw new Exception("User is not found and Email is not sent away for attendee userId: "
							+ item.getAttendee().getAttendeeUserId());
				}
			}

		}

		/* send one email to organizer about the update status */
		if (!signupEventTrackingInfo.getMeeting().isReceiveEmailByOwner())
			return;
		
		User organizer = null;
		User initiator = null;		
		try {
			organizer = userDirectoryService.getUser(signupEventTrackingInfo.getMeeting().getCreatorUserId());
			initiator = userDirectoryService.getUser(signupEventTrackingInfo.getInitiatorAllocationInfo().getAttendee()
					.getAttendeeUserId());

			AttendeeCancellationEmail email = new AttendeeCancellationEmail(organizer, initiator, sigupTList,
					signupEventTrackingInfo.getMeeting(), this.sakaiFacade);
			sendEmail(organizer, email);
		} catch (UserNotDefinedException e) {
			throw new Exception("User is not found for userId: "
					+ signupEventTrackingInfo.getMeeting().getCreatorUserId());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEmailToParticipantsByOrganizerAction(SignupEventTrackingInfo signupEventTrackingInfo)
			throws Exception {
		List<SignupTrackingItem> sigupTList = signupEventTrackingInfo.getAttendeeTransferInfos();
		for (SignupTrackingItem item : sigupTList) {

			User organizer = null;
			User participant = null;
			User participant2 = null;
			try {
				organizer = userDirectoryService.getUser(getSakaiFacade().getCurrentUserId());
				participant = userDirectoryService.getUser(item.getAttendee().getAttendeeUserId());
				if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP_SWAP)
						|| item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP_REPLACE)) {
					participant2 = userDirectoryService.getUser(item.getReplacedAttendde().getAttendeeUserId());
				}

				SignupEmailNotification email = null;
				if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP_MOVE))
					email = new MoveAttendeeEmail(organizer, participant, item, signupEventTrackingInfo.getMeeting(),
							sakaiFacade);
				else if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP_SWAP))
					email = new SwapAttendeeEmail(organizer, participant, participant2, item, signupEventTrackingInfo
							.getMeeting(), sakaiFacade);
				else if (item.getMessageType().equals(SIGNUP_ATTENDEE_CANCEL))
					email = new CancellationEmail(organizer, participant,item, signupEventTrackingInfo.getMeeting(), sakaiFacade);
				else if (item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP_REPLACE)
						|| item.getMessageType().equals(SIGNUP_ATTENDEE_SIGNUP))
					email = new AddAttendeeEmail(organizer, participant, item, signupEventTrackingInfo.getMeeting(),
							sakaiFacade);
				else if (item.getMessageType().equals(SIGNUP_ATTENDEE_PROMOTE))
					email = new PromoteAttendeeEmail(participant, item, signupEventTrackingInfo.getMeeting(),
							sakaiFacade);
				else {
					logger.warn("For attendee(Eid):" + participant.getEid() + " - No such message type:"
							+ item.getMessageType() + " was found and no email was able to send away");
					return;
				}
				// send email out
				sendEmail(participant, email);

			} catch (UserNotDefinedException e) {
				throw new Exception("User is not found and Email is not sent away for oraginzer userId:"
						+ signupEventTrackingInfo.getMeeting().getCreatorUserId());
			}

		}

	}

	/* send email via Sakai email Service */
	private void sendEmail(User user, SignupEmailNotification email) {
		List<User> list = new ArrayList<User>();
		list.add(user);
		emailService.sendToUsers(list, email.getHeader(), email.getMessage());
	}

	/* send email to all according to the message type */
	@SuppressWarnings("unchecked")
	private void sendEmailToAllUsers(SignupMeeting meeting, String messageType) throws Exception {
		List<SignupUser> signupUsers = sakaiFacade.getAllUsers(meeting);
		List<String> userIds = new ArrayList<String>();
		for (SignupUser signupUser : signupUsers) {
			userIds.add(signupUser.getInternalUserId());
		}
		List<User> sakaiUsers = userDirectoryService.getUsers(userIds);
		User organizer = null;
		try {
			SignupEmailNotification email = null;
			if (messageType.equals(SIGNUP_NEW_MEETING)) {
				organizer = userDirectoryService.getUser(meeting.getCreatorUserId());
				email = new NewMeetingEmail(organizer, meeting, this.sakaiFacade);
				excludPreAssignedAttendee(sakaiUsers, meeting);
			} else if (messageType.equals(SIGNUP_MEETING_MODIFIED)){
				organizer = userDirectoryService.getUser(getSakaiFacade().getCurrentUserId());
				email = new ModifyMeetingEmail(organizer, meeting, this.sakaiFacade);
			}

			emailService.sendToUsers(sakaiUsers, email.getHeader(), email.getMessage());
		} catch (UserNotDefinedException e) {
			throw new Exception("User is not found for userId: " + meeting.getCreatorUserId());
		}
	}

	/**
	 * This will exlude the pre-assigned attendee for a new meeting since they
	 * will receive a different email
	 * 
	 * @param sakaiUsers
	 *            a list of User objects.
	 * @param meeting
	 *            a SignupMeeting Object.
	 */
	private void excludPreAssignedAttendee(List<User> sakaiUsers, SignupMeeting meeting) {
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		if (signupTimeSlots ==null)
			return;
		
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> attendees = timeslot.getAttendees();			
			if (attendees ==null)
				continue;
			
			User preAssignedUser = null;
			for (SignupAttendee attendee : attendees) {
				try {
					preAssignedUser = userDirectoryService.getUser(attendee.getAttendeeUserId());
					for (Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
						User sakaiUser = (User) iter.next();
						if (sakaiUser.getEid().equals(preAssignedUser.getEid()))
							iter.remove();
					}

				} catch (UserNotDefinedException e) {
					logger.warn("User is not found for userId: " + attendee.getAttendeeUserId());
				}

			}
		}
	}

	/*
	 * when organizer pre-assign attendees to an event/meeting, this will send
	 * an email to notify them
	 */
	@SuppressWarnings("unchecked")
	private void sendEmailToAssignedUsers(SignupMeeting signupMeeting) throws Exception {
		List<String> userIds = new ArrayList<String>();
		List<SignupTimeslot> signupTimeSlots = signupMeeting.getSignupTimeSlots();
		User currentUser = userDirectoryService.getCurrentUser();
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> attendees = timeslot.getAttendees();
			if (attendees == null)
				continue;
			
			for (SignupAttendee attendee : attendees) {
				userIds.add(attendee.getAttendeeUserId());
				User user;
				try {
					user = userDirectoryService.getUser(attendee.getAttendeeUserId());
					SignupEmailNotification email = new OrganizerPreAssignEmail(currentUser, signupMeeting, timeslot,
							user, this.sakaiFacade);
					sendEmail(user, email);
				} catch (UserNotDefinedException e) {
					throw new Exception("User is not found for userId: " + attendee.getAttendeeUserId());
				}

			}
		}

	}

	/**
	 * get EmailService object,which is a Sakai emailService
	 * 
	 * @return EmailService object
	 */
	public EmailService getEmailService() {
		return emailService;
	}

	/**
	 * set an EmailService object
	 * 
	 * @param emailService
	 *            an EmailService object, which is provided by Sakai
	 */
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	/**
	 * get an UserDirectoryService object
	 * 
	 * @return an UserDirectoryService object, which is provided by Sakai
	 */
	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}

	/**
	 * set an UserDirectoryService object
	 * 
	 * @param userDirectoryService
	 *            an UserDirectoryService object
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	/**
	 * get a SakaiFacade object
	 * 
	 * @return a SakaiFacade object
	 */
	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	/**
	 * set a a SakaiFacade object
	 * 
	 * @param sakaiFacade
	 *            a a SakaiFacade object
	 */
	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

}
