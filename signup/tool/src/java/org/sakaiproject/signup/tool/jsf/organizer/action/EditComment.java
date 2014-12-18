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

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupUserActionException;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.tool.util.Utilities;
import org.springframework.dao.OptimisticLockingFailureException;

public class EditComment extends SignupAction {
	
	private SignupMeeting originalMeeting;
	
	private Long relatedTimeslotId;
	
	private String modifiedComment;
	
	private SakaiFacade sakaiFacade;

	/**
	 * 
	 * @param signupMeetingService
	 * @param currentUserId
	 * @param currentSiteId
	 * @param isOrganizer
	 * @param meeting
	 * @param timeslotId
	 * @param sakaiFacade
	 */
	public EditComment(SignupMeetingService signupMeetingService, String currentUserId, String currentSiteId,
			boolean isOrganizer, SignupMeeting meeting, Long timeslotId, SakaiFacade sakaiFacade) {
		super(currentUserId, currentSiteId, signupMeetingService, isOrganizer);
		
		this.originalMeeting = meeting;
		this.relatedTimeslotId = timeslotId;
		this.sakaiFacade = sakaiFacade;
	}
	
	/**
	 * This method checks all the preconditions before saving the data into the database
	 * 
	 * @param upToDateMeeting
	 * 			takes in the most up to date signup meeting
	 * @param timeslotId
	 * 			takes in a timeslotId
	 * @throws Exception
	 */
	private void checkPrecondition(SignupMeeting upToDateMeeting, Long timeslotId) throws Exception {
		// TODO Auto-generated method stub
		String originalComment = findUserComment(originalMeeting, timeslotId);
		String upToDateComment = findUserComment(upToDateMeeting, timeslotId);
		
		if(!originalComment.equals(upToDateComment)){
			throw new SignupUserActionException(Utilities.rb.getString("someone.already.updated.comment"));
		}
		if(upToDateMeeting.getTimeslot(timeslotId) != null && upToDateMeeting.getTimeslot(timeslotId).getAttendee(userId) != null){
			upToDateMeeting.getTimeslot(timeslotId).getAttendee(userId).setComments(this.modifiedComment);
		}
		else
			throw new SignupUserActionException(Utilities.rb.getString("someone.already.updated.comment"));
	}
	
	/**
	 * This method is used to find the user's comment
	 * 
	 * @param meeting
	 * 			takes in a signup meeting
	 * @param timeslotId
	 * 			takes in a timeslotId 
	 * @return
	 * @throws Exception
	 */
	private String findUserComment(SignupMeeting meeting, Long timeslotId) throws Exception {
		if(meeting.getTimeslot(timeslotId) != null && meeting.getTimeslot(timeslotId).getAttendee(userId) != null){
			return meeting.getTimeslot(timeslotId).getAttendee(userId).getComments();
		}
		else
			throw new SignupUserActionException(Utilities.rb.getString("someone.already.updated.comment"));
	}

	/**
	 * 
	 * @param newComment
	 * 			takes in a string new comment
	 * @return
	 * @throws Exception
	 */
	public SignupMeeting updateComment(String newComment) throws Exception {
		this.modifiedComment = newComment;
		boolean isOrganizer = originalMeeting.getPermission().isUpdate();
		String currentUserId = sakaiFacade.getCurrentUserId();
		
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				SignupMeeting upToDateMeeting = signupMeetingService.loadSignupMeeting(originalMeeting.getId(), currentUserId, siteId);
				checkPrecondition(upToDateMeeting, relatedTimeslotId);
				if(isOrganizer)
					upToDateMeeting.setPermission(originalMeeting.getPermission());
				signupMeetingService.updateSignupMeeting(upToDateMeeting, isOrganizer);
				upToDateMeeting = signupMeetingService.loadSignupMeeting(originalMeeting.getId(), currentUserId, siteId);
				
				return upToDateMeeting;
				
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		throw new SignupUserActionException(Utilities.rb.getString("someone.already.updated.db"));
	}
}
