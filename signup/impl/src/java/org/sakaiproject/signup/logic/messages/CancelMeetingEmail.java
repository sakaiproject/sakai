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
package org.sakaiproject.signup.logic.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer of an event/meeting to notify participants the meeting has been cancelled.
 * </p>
 */
public class CancelMeetingEmail extends SignupEmailBase {

	private final User organizer;

	private final String emailReturnSiteId;

	/**
	 * Constructor
	 * 
	 * @param orgainzer
	 *            an User, who organizes the event/meeting
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 * @param emailReturnSiteId
	 *            a unique SiteId string
	 */
	public CancelMeetingEmail(User orgainzer, SignupMeeting meeting, SakaiFacade sakaiFacade, String emailReturnSiteId) {
		this.organizer = orgainzer;
		this.meeting = meeting;
		this.emailReturnSiteId = emailReturnSiteId;
		this.setSakaiFacade(sakaiFacade);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getFromAddress());
		rv.add("To: " + rb.getString("noReply@") + getSakaiFacade().getServerConfigurationService().getServerName());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {
		StringBuilder message = new StringBuilder();

		Object[] params = new Object[] { makeFirstCapLetter(organizer.getDisplayName()), meeting.getTitle(), getSiteTitleWithQuote(this.emailReturnSiteId), getServiceName() };
		message.append(newline + newline + MessageFormat.format(rb.getString("body.organizerCancel.meeting.part"), params));
		
		message.append(newline + getFooter(newline, emailReturnSiteId));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return organizer.getEmail();
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.meeting.cancel.field"), new Object[] { getAbbreviatedMeetingTitle(), getSiteTitle(this.emailReturnSiteId)});
	}
}
