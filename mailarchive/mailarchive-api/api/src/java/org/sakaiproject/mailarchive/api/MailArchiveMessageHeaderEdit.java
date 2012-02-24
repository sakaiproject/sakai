/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.mailarchive.api;

import java.util.List;

import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * MailArchiveMessageHeader is the Interface for a Sakai Mail Archive Message header.
 * </p>
 */
public interface MailArchiveMessageHeaderEdit extends MailArchiveMessageHeader, MessageHeaderEdit
{
	/**
	 * Set the subject of the message.
	 * 
	 * @param subject
	 *        The subject of the message.
	 */
	public void setSubject(String subject);

	/**
	 * Set the the from: address of the message.
	 * 
	 * @param from
	 *        The from: address of the message.
	 */
	public void setFromAddress(String from);

	/**
	 * Set the date: sent of the message.
	 * 
	 * @param sent
	 *        The the date: sent of the message.
	 */
	public void setDateSent(Time sent);

	/**
	 * Set the entire set of mail headers of the message.
	 * 
	 * @param headers
	 *        The the entire set of mail headers of the message (List of String).
	 */
	public void setMailHeaders(List headers);
}
