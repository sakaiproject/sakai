/**
 * Copyright (c) 2007-2015 The Apereo Foundation
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

package org.sakaiproject.signup.impl.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.user.api.User;

/**
 * This class handles the email notification sent to participants when an event/meeting 
 * is cancelled by its organizer. It extends AllUsersEmailBase to provide meeting 
 * cancellation specific email functionality.
 *
 * <p>The email includes:
 * <ul>
 *   <li>Notification of cancellation</li>
 *   <li>Meeting title and site information</li>
 *   <li>Organizer's information</li>
 * </ul>
 *
 * @see AllUsersEmailBase
 */
public class CancelMeetingEmail extends AllUsersEmailBase {

	private final User organizer;
	private final String emailReturnSiteId;

    /**
     * @param orgainzer an User, who organizes the event/meeting
     * @param meeting a SignupMeeting object
     * @param sakaiFacade a SakaiFacade object
     * @param emailReturnSiteId a unique SiteId string
	 */
	public CancelMeetingEmail(User orgainzer, SignupMeeting meeting, SakaiFacade sakaiFacade, String emailReturnSiteId) {
		this.organizer = orgainzer;
		this.meeting = meeting;
		this.emailReturnSiteId = emailReturnSiteId;
		this.setSakaiFacade(sakaiFacade);
		this.cancellation = true;
	}

    @Override
	public List<String> getHeader() {
		List<String> rv = new ArrayList<>();
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getFromAddress());
		rv.add("To: " + getSakaiFacade().getServerConfigurationService().getSmtpFrom());

		return rv;
	}

    @Override
	public String getMessage() {
		StringBuilder message = new StringBuilder();

		Object[] params = new Object[] {
                makeFirstCapLetter(organizer.getDisplayName()),
                meeting.getTitle(),
                getSiteTitleWithQuote(this.emailReturnSiteId),
                getServiceName()
        };
		message.append(NEWLINE).append(NEWLINE).append(MessageFormat.format(rb.getString("body.organizerCancel.meeting.part"), params));
		message.append(NEWLINE).append(getFooter(NEWLINE, emailReturnSiteId));
		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return StringUtils.defaultIfEmpty(organizer.getEmail(), getServerFromAddress());
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.meeting.cancel.field"), getAbbreviatedMeetingTitle(), getSiteTitle(this.emailReturnSiteId));
	}
}
