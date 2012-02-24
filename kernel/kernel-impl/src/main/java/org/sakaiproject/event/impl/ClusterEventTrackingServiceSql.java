/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.event.impl;

/**
 * database methods.
 */
public interface ClusterEventTrackingServiceSql
{
	/**
	 * returns the sql statement which inserts an event into the sakai_event table.
	 */
	String getInsertEventSql();

	/**
	 * returns the sql statement which retrieves an event from the sakai_event and sakai_session tables.
	 */
	String getEventSql();

	/**
	 * returns the sql statement which retrieves the largest event id from the sakai_event table.
	 */
	String getMaxEventIdSql();

    /**
     * returns the sql statement which counts the number of events in the event table
     */
    String getEventsCountSql();

}
