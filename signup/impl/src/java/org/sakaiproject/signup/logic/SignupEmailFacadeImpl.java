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
		if (messageType.equals(SIGNUP_NEW_MEETING) || messageType.equals(SIGNUP_MEETING_MODIFIED)) {
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
		// TODO Do we need to send info about promoted guys to organizer?
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
					email = new CancellationEmail(organizer, participant, item, signupEventTrackingInfo.getMeeting(),
							sakaiFacade);
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

	@SuppressWarnings("unchecked")
	private List<EmailUserSiteGroup> getUserSiteEmailGroups(List<SignupUser> signupUsers) {
		List<EmailUserSiteGroup> userSiteGroupList = new ArrayList<EmailUserSiteGroup>();
		for (SignupUser signupUser : signupUsers) {
			String siteId = signupUser.getMainSiteId();
			boolean found = false;
			for (EmailUserSiteGroup userSiteGroup : userSiteGroupList) {
				if (siteId.equals(userSiteGroup.getSiteId())) {
					userSiteGroup.addSignupUser(signupUser);
					found = true;
					break;
				}
			}
			if (!found) {
				EmailUserSiteGroup usg = new EmailUserSiteGroup(siteId, signupUser);
				userSiteGroupList.add(usg);
			}
		}

		return userSiteGroupList;
	}

	/* send email to all according to the message type */
	@SuppressWarnings("unchecked")
	private void sendEmailToAllUsers(SignupMeeting meeting, String messageType) throws Exception {
		List<SignupUser> signupUsers = sakaiFacade.getAllUsers(meeting);

		List<EmailUserSiteGroup> userSiteGroupList = getUserSiteEmailGroups(signupUsers);
		boolean isException = false;		
		for (EmailUserSiteGroup emailUserSiteGroup : userSiteGroupList) {
			if (!emailUserSiteGroup.isPublishedSite())
				continue;// skip sending email

			List<String> userIds = null;
			List<User> sakaiUsers = null;
			User organizer = null;			
			try {
				SignupEmailNotification email = null;
				if (messageType.equals(SIGNUP_NEW_MEETING)) {
					organizer = userDirectoryService.getUser(meeting.getCreatorUserId());
					email = new NewMeetingEmail(organizer, meeting, this.sakaiFacade, emailUserSiteGroup.getSiteId());
					/*
					 * send email to pre-assiged people for this site group and
					 * also excluding them for next step.
					 */
					sendEmailToPreAssignedAttendee(emailUserSiteGroup, meeting);
					/* get the people list excluding pre-assigned ones */
					userIds = emailUserSiteGroup.getUserInternalIds();
					sakaiUsers = userDirectoryService.getUsers(userIds);

				} else if (messageType.equals(SIGNUP_MEETING_MODIFIED)) {
					userIds = emailUserSiteGroup.getUserInternalIds();
					sakaiUsers = userDirectoryService.getUsers(userIds);
					organizer = userDirectoryService.getUser(getSakaiFacade().getCurrentUserId());
					email = new ModifyMeetingEmail(organizer, meeting, this.sakaiFacade, emailUserSiteGroup.getSiteId());
				}

				if (email != null)
					emailService.sendToUsers(sakaiUsers, email.getHeader(), email.getMessage());

			} catch (UserNotDefinedException ue) {
				isException = true;
				logger.warn("User is not found for userId: " + meeting.getCreatorUserId());
			} catch (Exception e) {
				isException = true;
				logger.warn(e.getMessage());
			}
		}
		
		if(isException)
			throw new Exception("Some emails may not be sent out due to error.");
	}

	private void sendEmailToPreAssignedAttendee(EmailUserSiteGroup emailUserSiteGroup, SignupMeeting meeting)
			throws Exception {
		List<SignupUser> sgpUsers = emailUserSiteGroup.getSignupUsers();
		List<SignupTimeslot> signupTimeSlots = meeting.getSignupTimeSlots();
		if (signupTimeSlots == null)
			return;

		boolean isExcepiotn = false;
		User currentUser = userDirectoryService.getCurrentUser();
		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> attendees = timeslot.getAttendees();
			if (attendees == null)
				continue;

			for (SignupAttendee attendee : attendees) {
				for (Iterator iter = sgpUsers.iterator(); iter.hasNext();) {
					SignupUser spUser = (SignupUser) iter.next();
					if (spUser.getInternalUserId().equals(attendee.getAttendeeUserId())) {
						User user;
						try {
							user = userDirectoryService.getUser(attendee.getAttendeeUserId());
							SignupEmailNotification email = new OrganizerPreAssignEmail(currentUser, meeting, timeslot,
									user, this.sakaiFacade, emailUserSiteGroup.getSiteId());
							sendEmail(user, email);
						} catch (UserNotDefinedException e) {
							logger.warn("User is not found for userId: " + attendee.getAttendeeUserId());
							isExcepiotn = true;
						}
						/*
						 * remove it to avoid send new meeting notification
						 * again
						 */
						iter.remove();
						break;
					}
				}

			}
		}

		if (isExcepiotn)
			throw new Exception("User is not found and email may not be sent out.");
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
		if (signupTimeSlots == null)
			return;

		for (SignupTimeslot timeslot : signupTimeSlots) {
			List<SignupAttendee> attendees = timeslot.getAttendees();
			if (attendees == null)
				continue;

			User preAssignedUser = null;
			for (SignupAttendee attendee : attendees) {
				try {
					preAssignedUser = userDirectoryService.getUser(attendee.getAttendeeUserId());
					for (Iterator iter = sakaiUsers.iterator(); iter.hasNext();) {
						User sakaiUser = (User) iter.next();
						if (sakaiUser.getEid().equals(preAssignedUser.getEid())) {
							iter.remove();
						}
					}

				} catch (UserNotDefinedException e) {
					logger.warn("User is not found for userId: " + attendee.getAttendeeUserId());
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

	private class EmailUserSiteGroup {
		private String siteId;

		private boolean publishedSite;

		private List<SignupUser> signupUsers = new ArrayList<SignupUser>();

		public EmailUserSiteGroup(String siteId, SignupUser user) {
			this.siteId = siteId;
			this.signupUsers.add(user);
			this.publishedSite = user.isPublishedSite();
		}

		public void addSignupUser(SignupUser user) {
			this.signupUsers.add(user);
		}

		public String getSiteId() {
			return siteId;
		}

		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		public List<SignupUser> getSignupUsers() {
			return signupUsers;
		}

		public void setSignupUsers(List<SignupUser> signupUsers) {
			this.signupUsers = signupUsers;
		}

		public List<String> getUserInternalIds() {
			List<String> userIds = new ArrayList<String>();
			for (SignupUser signupUser : signupUsers) {
				userIds.add(signupUser.getInternalUserId());
			}
			return userIds;
		}

		public boolean isPublishedSite() {
			return publishedSite;
		}

		public void setPublishedSite(boolean publishedSite) {
			this.publishedSite = publishedSite;
		}

	}

}
