/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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

package org.sakaiproject.signup.tool.entityproviders;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.signup.logic.Permission;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.restful.SignupEvent;
import org.sakaiproject.signup.restful.SignupGroupItem;
import org.sakaiproject.signup.restful.SignupParticipant;
import org.sakaiproject.signup.restful.SignupSiteItem;
import org.sakaiproject.signup.restful.SignupTimeslotItem;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This class will provides methods for converting the SignupMeeting object to
 * SignupEvent object, which contains the only necessary information for client
 * side and hide away the user information.
 * </P>
 * 
 * @author Peter Liu
 */
public class SignupObjectConverter {

	private static final int MAX_COMMENT_DISPLAY_LENGTH = 300;

	public static SignupEvent convertToSignupEventObj(SignupMeeting sm, String userId, String currentSiteId,
			boolean isDeepCopy, boolean isMySignUp, SakaiFacade sakaiFacade) {
		if (sm == null) {
			return null;
		}
		SignupEvent se = new SignupEvent();
		se.setEventId(sm.getId());
		se.setTitle(sm.getTitle());
		se.setLocation(sm.getLocation());
		se.setDescription(sm.getDescription());
		se.setStartTime(sm.getStartTime());
		se.setEndTime(sm.getEndTime());
		se.setOrganizerName(sakaiFacade.getUserDisplayName(sm.getCreatorUserId()));
		se.setSignupBegins(sm.getSignupBegins());
		se.setSignupDeadline(sm.getSignupDeadline());
		se.setId(sm.getId() + "");
		se.setMeetingType(sm.getMeetingType());
		se.setRecurrenceId(sm.getRecurrenceId());
		se.setRepeatType(sm.getRepeatType());
		
		/*my signup need no permission part*/
		if(isMySignUp){
			se.setAvailableStatus(Utilities.rb.getString("event.youSignedUp"));
		}
		else{
			se.setPermission(new Permission(sm.getPermission().isAttend(), sm.getPermission().isUpdate(), sm
					.getPermission().isDelete()));
			se.setAvailableStatus(Utilities.retrieveAvailStatus(sm, userId, sakaiFacade));
		}
		
		se.setSiteId(currentSiteId);// keep tracking siteId		
		se.setSignupSiteItems(null);
		se.setAllowWaitList(sm.isAllowWaitList());
		se.setAllowComment(sm.isAllowComment());
		se.setAllowAttendance(sm.isAllowAttendance());
		se.setAutoReminder(sm.isAutoReminder());
		se.setEidInputMode(sm.isEidInputMode());
		se.setMaxNumOfSlots(sm.getMaxNumOfSlots());
		if (isDeepCopy) {
			se.setSignupTimeSlotItems(getSignupTimeslotItems(sm.getSignupTimeSlots(), sm.getPermission().isUpdate(),
					sakaiFacade));
			se.setSignupSiteItems(SignupObjectConverter.getSignupSiteItems(sm.getSignupSites()));
			se.setSignupMainEventAttachItems(SignupObjectConverter.getSignupMainEventAttachmentItems(sm));
			se.setAllowedUserActionTypes(SignupEvent.USER_ATION_Types);
		}
		if (Utilities.rb.getString("event.youSignedUp").equals(se.getAvailableStatus())) {
			se.setCurrentUserSignedUp(true);
		}

		return se;
	}

	public static List<SignupSiteItem> getSignupSiteItems(List<SignupSite> sites) {
		List<SignupSiteItem> siteItems = new ArrayList<SignupSiteItem>();
		if (sites != null) {
			for (SignupSite site : sites) {
				SignupSiteItem sItem = new SignupSiteItem(site.getTitle(), site.getSiteId());
				sItem.setSignupGroupItems(getSignupGroupItems(site.getSignupGroups()));
				siteItems.add(sItem);
			}
		}

		return siteItems;
	}

	public static List<SignupGroupItem> getSignupGroupItems(List<SignupGroup> groups) {
		if (groups == null || groups.isEmpty())
			return null;

		List<SignupGroupItem> groupItems = new ArrayList<SignupGroupItem>();

		for (SignupGroup grp : groups) {
			SignupGroupItem grpItem = new SignupGroupItem(grp.getTitle(), grp.getGroupId());
			groupItems.add(grpItem);
		}

		return groupItems;
	}

	public static List<SignupTimeslotItem> getSignupTimeslotItems(List<SignupTimeslot> timeslots, boolean isOrganizer,
			SakaiFacade sakaiFacade) {
		List<SignupTimeslotItem> tsItems = new ArrayList<SignupTimeslotItem>();
		if (timeslots != null) {
			for (SignupTimeslot item : timeslots) {
				SignupTimeslotItem one = new SignupTimeslotItem();
				one.setId(item.getId());
				one.setStartTime(item.getStartTime());
				one.setEndTime(item.getEndTime());
				one.setLocked(item.isLocked());
				one.setCanceled(item.isCanceled());
				one.setDisplayAttendees(item.isDisplayAttendees());
				one.setMaxNoOfAttendees(item.getMaxNoOfAttendees());
				//TODO JIRA sorting Signup-204 in future
				one.setAttendees(convertToSignupParticipants(one, item.getAttendees(), item.isDisplayAttendees(),
						false, isOrganizer, sakaiFacade));
				one.setWaitingList(convertToSignupParticipants(one, item.getWaitingList(), item.isDisplayAttendees(),
						true, isOrganizer, sakaiFacade));

				tsItems.add(one);
			}
		}
		return tsItems;
	}

	public static List<SignupParticipant> convertToSignupParticipants(SignupTimeslotItem timeslotItem,
			List<SignupAttendee> attendees, boolean showAttendeeName, boolean isWaitList, boolean isOrganizer,
			SakaiFacade sakaiFacade) {
		List<SignupParticipant> participants = new ArrayList<SignupParticipant>();
		if (attendees != null) {
			for (SignupAttendee one : attendees) {
				SignupParticipant sp = new SignupParticipant(one.getAttendeeUserId(), one.getSignupSiteId());
				if (sakaiFacade.getCurrentUserId().equals(one.getAttendeeUserId())) {
					if (isWaitList)
						timeslotItem.setOnWaitList(true);
					else
						timeslotItem.setSignedUp(true);
				}

				if (!isOrganizer && !sakaiFacade.getCurrentUserId().equals(one.getAttendeeUserId())) {
					sp.setAttendeeUserId("private");// hide
				}
				String comment = one.getComments();
				if (comment != null && comment.length() > MAX_COMMENT_DISPLAY_LENGTH)
					comment = comment.subSequence(0, MAX_COMMENT_DISPLAY_LENGTH) + ".....";
				sp.setComments(comment);
				if (showAttendeeName || isOrganizer) {
					sp.setDisplayName(sakaiFacade.getUserDisplayName(one.getAttendeeUserId()));
				}

				participants.add(sp);
			}
		}

		return participants;
	}
	
	public static List<SignupAttachment> getSignupMainEventAttachmentItems(SignupMeeting sm){
		List<SignupAttachment> eventMainAttachments = new ArrayList<SignupAttachment>();
		if(sm.getSignupAttachments() != null){
			for (SignupAttachment attach: sm.getSignupAttachments()) {
				if(attach.getTimeslotId() ==null && attach.getViewByAll())
					eventMainAttachments.add(attach);
				
				//TODO other cases: such as attachment for a specific time slot only or attendee's attachments.
			}
		}
		
		return eventMainAttachments;
	}

}
