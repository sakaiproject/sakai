/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
 **********************************************************************************/

package org.sakaiproject.email.api;

import java.util.List;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * Digest stores sets of messages for a given user (id).
 * </p>
 */
public interface Digest extends Entity, Comparable
{
	/**
	 * Access the (user) id for this digest.
	 * 
	 * @return The (user) id for this digest.
	 */
	String getId();

	/**
	 * Access the list (DigestMessage) of messages, for the time period.
	 * 
	 * @param period
	 *        A time in the time period to select.
	 * @return The List (DigestMessage) of messages (possibly empty).
	 */
	List getMessages(Time period);

	/**
	 * Access the list (String, TimePeriod string) of periods.
	 * 
	 * @return The List (String, TimePeriod string) of periods.
	 */
	List getPeriods();
	
	/**
	 * Add another message, in the current time period.
	 * 
	 * @param subject
	 *        The to (user id) of the message.
	 * @param subject
	 *        The subject of the message.
	 * @param body
	 *        The subject of the message.
	 */
	void add(String to, String subject, String body);

	/**
	 * Add another message, in the current time period.
	 * 
	 * @param msg
	 *        The message to add.
	 */
	void add(DigestMessage msg);

	/**
	 * Clear all messages from a time period.
	 * 
	 * @param period
	 *        a Time in the time period.
	 */
	void clear(Time period);

}
