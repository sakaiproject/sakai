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

package org.sakaiproject.event.api;


import java.util.Date;

/**
 * 
 * ActivityService provides the ability to check if users are online and when they were last active.
 * 
 * <p>
 * Users are registered and updated each time they publish an event. As such you can tell <b>if</b> a user is active
 * and <b>when</b> they were last active (last event time).
 * </p>
 * 
 * <p>
 * For clustered environments, you may experience discrepancies when a new node comes online as the cache will start afresh
 * so users will only be repopulated into the cache once they start publishing events.</p>
 * 
 * @since 1.2.1
 */
public interface EventQueryService {

	/**
	 * Returns a list of events of a user between 2 dates.
	 * @param eid	is the user that we want to query
	 * @param startDate limit the query ti these dates
	 * @param endDate limit the query ti these dates
	 * @return	String as the result of the Query in xml
	 */
	public String getUserActivity(String eid, Date startDate, Date endDate);

	/**
	 * Returns a list of events of a user between 2 dates.
	 * @param eid	is the user that we want to query
	 * @param startDateString limit the query ti these dates. In this case as String if we call it as a rest
	 * @param endDateString limit the query ti these dates. In this case as String if we call it as a rest
	 * @return	String as the result of the Query in xml
	 */
	public String getUserActivityRestVersion(String eid, String startDateString, String endDateString);


	/**
	 * Returns the User's logon activity.
	 * @param eid	is the user that we want to query
	 * @return	String as the result of the Query in xml
	 */
	public String getUserLogonActivity(String eid);

	/**
	 * Returns the User's activity filtered by one event type.
	 * @param eid	is the user that we want to query
	 * @param eventType the event type to filter
	 * @return	String as the result of the Query in xml
	 */
	public String getUserActivityByType(String eid, String eventType);



}

