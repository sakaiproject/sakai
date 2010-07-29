/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/logic/messages/SignupEventTrackingInfo.java $
 * $Id: SignupEventTrackingInfo.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
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
package org.sakaiproject.signup.logic.messages;

import java.util.List;

import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;

/**
 * <P>
 * This interface provides the necessary access methods to retrieve all related
 * information caused by an user action inside the Signup tool
 * </P>
 */
public interface SignupEventTrackingInfo {

	/**
	 * get a list of SignupTrackingItem objects which contains all related
	 * information caused by an user action inside the Signup tool
	 * 
	 * @return a list of SignupTrackingItem object
	 */
	public List<SignupTrackingItem> getAttendeeTransferInfos();

	/**
	 * add a new/updated user action information triggered by an user
	 * 
	 * @param attendee
	 *            an SignupAttendee object
	 * @param timeslot
	 *            an SignupTimeslot object
	 * @param messageType
	 *            a message type which defines what type of an action caused by
	 *            the an user
	 * @param isInitiator
	 *            boolean value
	 */
	public void addOrUpdateAttendeeAllocationInfo(SignupAttendee attendee, SignupTimeslot timeslot, String messageType,
			boolean isInitiator);

	/**
	 * add a new/updated user action information triggered by an user
	 * 
	 * @param attendee
	 *            an SignupAttendee object
	 * @param timeslot
	 *            an SignupTimeslot object
	 * @param messageType
	 *            a message type which defines what type of an action caused by
	 *            the an user
	 * @param isInitiator
	 *            boolean value
	 * @param replacedAttendee
	 *            an SignupAttendee object
	 */
	public void addOrUpdateAttendeeAllocationInfo(SignupAttendee attendee, SignupTimeslot timeslot, String messageType,
			boolean isInitiator, SignupAttendee replacedAttendee);

	/**
	 * get the SignupMeeting object
	 * 
	 * @return the SignupMeeting object
	 */
	public SignupMeeting getMeeting();

	/**
	 * get the Initiator's SignupTrackingItem object
	 * 
	 * @return the Initiator's SignupTrackingItem object
	 */
	public SignupTrackingItem getInitiatorAllocationInfo();

	/**
	 * set the SignupMeeting object
	 * 
	 * @param meeting
	 *            a SignupMeeting object
	 */
	public void setMeeting(SignupMeeting meeting);

	/**
	 * get the Transfer Log Info for all users. It's for Log purpose
	 * 
	 * @return a string value.
	 */
	public String getAllAttendeeTransferLogInfo();
}