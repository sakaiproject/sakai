/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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
}
