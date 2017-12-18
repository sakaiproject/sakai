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

package org.sakaiproject.signup.tool.jsf.attachment;

import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.tool.jsf.organizer.action.SignupAction;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This class will provide business logic for 'Remove Attachment' action by user.
 * </P>
 */
@Slf4j
public class RemoveAttachment extends SignupAction {

	/**
	 * Constructor
	 * 
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 * @param currentUserId
	 *            an unique sakai internal user id.
	 * @param currentSiteId
	 *            an unique sakai site id.
	 */
	public RemoveAttachment(SignupMeetingService signupMeetingService, String currentUserId, String currentSiteId,
			boolean isOrganizer) {
		super(currentUserId, currentSiteId, signupMeetingService, isOrganizer);
	}

	public void removeAttachment(SignupMeeting meeting, SignupAttachment remAttach){
		try {
			if(meeting ==null || meeting.getId() ==null)
				return;
			
			handleVersion(meeting, remAttach);
		} catch (PermissionException pe) {
			log.warn(Utilities.rb.getString("no.permissoin.do_it"));
		}catch (Exception e){
			log.warn(e.getMessage());
		}
	}

	/**
	 * Check if the pre-condition is still satisfied for continuing the update
	 * process after retrieving the up-to-dated data. This process is a
	 * concurrency process.
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param SignupAttachment
	 *            a SignupAttachment object.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	public void actionsForOptimisticVersioning(SignupMeeting meeting, SignupAttachment remAttach)
			throws Exception {
		prepareRemoveAttachment(meeting, remAttach);
	}

	/**
	 * Give it a number of tries to update the event/meeting object into DB
	 * storage if this still satisfy the pre-condition regardless some changes
	 * in DB storage
	 * 
	 * @param meeting
	 *            a SignupMeeting object.
	 * @param SignupAttachment
	 *            a SignupAttachment object.
	 * @throws Exception
	 *             throw if anything goes wrong.
	 */
	private void handleVersion(SignupMeeting meeting, SignupAttachment remAttach) throws Exception {
		for (int i = 0; i < MAX_NUMBER_OF_RETRY; i++) {
			try {
				meeting = signupMeetingService.loadSignupMeeting(meeting.getId(), userId, siteId);
				actionsForOptimisticVersioning(meeting, remAttach);
				signupMeetingService.updateSignupMeeting(meeting, isOrganizer);
			} catch (OptimisticLockingFailureException oe) {
				// don't do any thing
			}
		}
		throw new Exception("It's already removed");
	}

	private void prepareRemoveAttachment(SignupMeeting meeting, SignupAttachment remAttach) throws Exception {
		List<SignupAttachment> attachList = meeting.getSignupAttachments();
		boolean found = false;
		if(attachList !=null){
			for (Iterator iter = attachList.iterator(); iter.hasNext();) {
				SignupAttachment a = (SignupAttachment) iter.next();
				if(a.getResourceId().equals(remAttach.getResourceId())){
					iter.remove();
					found = true;
				}
			}
		}
		
		if (!found) {
			throw new Exception("It's already removed");
		}
	}

}
