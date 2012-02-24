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

import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * MailArchiveMessageHeader is the Interface for a Sakai Mail Archive Message header.
 * </p>
 */
public interface MailArchiveMessageHeader extends MessageHeader
{
	/**
	 * Access the subject of the message.
	 * 
	 * @return The subject of the message.
	 */
	public String getSubject();

	/**
	 * Access the from: address of the message.
	 * 
	 * @return The from: address of the message.
	 */
	public String getFromAddress();

	/**
	 * Access the date: sent of the message.
	 * 
	 * @return The date: sent of the message.
	 */
	public Time getDateSent();

	/**
	 * Access the entire set of mail headers the message.
	 * 
	 * @return The entire set of mail headers of the message (List of String).
	 */
	public List getMailHeaders();
}
