/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

import java.time.Instant;

import org.sakaiproject.time.api.Time;

/**
 * <p>
 * UsageSession models an end user's usage session.
 * </p>
 */
public interface UsageSession extends Comparable
{
	String UNKNOWN = "UnknownBrowser";

	/**
	 * Access the unique id for this session.
	 * 
	 * @return the unique id for this session.
	 */
	String getId();

	/**
	 * Access the server id which is hosting this session.
	 * 
	 * @return the server id which is hosting this session.
	 */
	String getServer();
	
	/**
	 * Set the server id which is hosting this session.
	 * 
	 * @param serverId the server id which is hosting this session.
	 */
	public void setServer(String serverId);

	/**
	 * Access the user id for this session.
	 * 
	 * @return the user id for this session.
	 */
	String getUserId();

	/**
	 * Access the user eid for this session, if known - fallback to the id if not.
	 * 
	 * @return The user eid for this session, or the use id if the eid cannot be found.
	 */
	String getUserEid();

	/**
	 * Access the user display id for this session, if known - fallback to the id if not.
	 * 
	 * @return The user display id for this session, or the use id if the user cannot be found.
	 */
	String getUserDisplayId();

	/**
	 * Access the IP Address from which this session originated.
	 * 
	 * @return the IP Address from which this session originated.
	 */
	String getIpAddress();

	/**
	 * Access the Hostname from which this session originated.
	 * 
	 * @return the Hostname resolved from the IP Address from which this session originated.
	 */
	String getHostName();
	
	/**
	 * Access the User Agent string describing the browser used in this session.
	 * 
	 * @return the User Agent string describing the browser used in this session.
	 */
	String getUserAgent();

	/**
	 * Access a short string describing the class of browser used in this session.
	 * 
	 * @return the short ID describing the browser used in this session.
	 */
	String getBrowserId();

	/**
	 * Is this session closed?
	 * 
	 * @return true if the session is closed, false if open.
	 */
	boolean isClosed();

	/**
	 * Access the start time of the session
	 * 
	 * @return The time the session started.
	 * 
	 * @deprecated {@link #getStartInstant()}
	 */
	@Deprecated
	Time getStart();

	/**
	 * Access the end time of the session.
	 * 
	 * @return The time the session ended. If still going, this will .equals() the getStart() value.
	 * @deprecated {@link #getEndInstant()}
	 */
	@Deprecated
	Time getEnd();
	
	/**
	 * Access the start time of the session
	 * 
	 * @return The time the session started.
	 */
	Instant getStartInstant();
	
	/**
	 * Access the end time of the session.
	 * 
	 * @return The time the session ended. If still going, this will .equals() the getStart() value.
	 */
	Instant getEndInstant();
}
