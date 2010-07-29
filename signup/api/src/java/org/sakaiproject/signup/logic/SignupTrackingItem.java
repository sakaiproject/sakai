/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/logic/SignupTrackingItem.java $
 * $Id: SignupTrackingItem.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
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
import java.util.List;

import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupTimeslot;

/**
 * <P>
 * This class is a place holder, which contains all the related informaion along
 * the way for <b>one attendee</B> when an action occurs in Signup tool
 * </P>
 */
public class SignupTrackingItem {

	private SignupTimeslot addToTimeslot;

	private List<SignupTimeslot> removedFromTimeslot;

	private SignupAttendee attendee;

	private SignupAttendee replacedAttendde;

	private String messageType;

	private boolean initiator;

	/**
	 * Constructor
	 * 
	 * @param attendee
	 *            an SignupAttendee object
	 * @param messageType
	 *            a message type string, which defines what type email message
	 *            should be generated
	 */
	public SignupTrackingItem(SignupAttendee attendee, String messageType) {
		this(attendee, messageType, false);
	}

	/**
	 * Constructor
	 * 
	 * @param attendee
	 *            an SignupAttendee object
	 * @param messageType
	 *            a message type string, which defines what type email message
	 *            should be generated
	 * @param initiator
	 *            the user, who triggers the chain of reaction
	 */
	public SignupTrackingItem(SignupAttendee attendee, String messageType, boolean initiator) {
		this.attendee = attendee;
		this.initiator = initiator;
		this.messageType = messageType;
		this.removedFromTimeslot = new ArrayList<SignupTimeslot>();
	}

	/**
	 * get the assigned time slot
	 * 
	 * @return an SignupTimeslot object
	 */
	public SignupTimeslot getAddToTimeslot() {
		return addToTimeslot;
	}

	/**
	 * set the assigned time slot
	 * 
	 * @param addToTimeslot
	 *            an SignupTimeslot object
	 */
	public void setAddToTimeslot(SignupTimeslot addToTimeslot) {
		this.addToTimeslot = addToTimeslot;
	}

	/**
	 * get a SignupAttendee object
	 * 
	 * @return a SignupAttendee object
	 */
	public SignupAttendee getAttendee() {
		return attendee;
	}

	/**
	 * set a SignupAttendee object
	 * 
	 * @param attendee
	 *            a SignupAttendee object
	 */
	public void setAttendee(SignupAttendee attendee) {
		this.attendee = attendee;
	}

	/**
	 * return true if the user is the event trigger
	 * 
	 * @return true if the user is the event trigger
	 */
	public boolean isInitiator() {
		return initiator;
	}

	/**
	 * set true, when the user is the event trigger
	 * 
	 * @param initiator
	 *            a boolean value
	 */
	public void setInitiator(boolean initiator) {
		this.initiator = initiator;
	}

	/**
	 * get message type, which defines email message
	 * 
	 * @return a message type
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * set a message type, which defines email message
	 * 
	 * @param messageType
	 *            a message type
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * get a list of SignupTimeslot objects, from which the user is removed
	 * 
	 * @return a list of SignupTimeslot objects
	 */
	public List<SignupTimeslot> getRemovedFromTimeslot() {
		return removedFromTimeslot;
	}

	/**
	 * add to the SignupTimeslot object list when a user is removed from that
	 * time slot
	 * 
	 * @param timeslot
	 *            a SignupTimeslot object
	 */
	public void addToRemovedTimeslotList(SignupTimeslot timeslot) {
		removedFromTimeslot.add(timeslot);
	}

	/**
	 * get the replaced SignupAttendee object
	 * 
	 * @return a SignupAttendee object
	 */
	public SignupAttendee getReplacedAttendde() {
		return replacedAttendde;
	}

	/**
	 * set the replaced SignupAttendee object
	 * 
	 * @param replacedAttendde
	 *            a SignupAttendee object
	 */
	public void setReplacedAttendde(SignupAttendee replacedAttendde) {
		this.replacedAttendde = replacedAttendde;
	}

}
