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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.user.api.Authentication;

/**
 * <p>
 * UsageSessionService keeps track of usage sessions.
 * </p>
 */
public interface UsageSessionService
{
	/** Names for the events of logging in from well-known services. */
	public static final String EVENT_LOGIN = "user.login";
	public static final String EVENT_LOGIN_WS = "user.login.ws";
	public static final String EVENT_LOGIN_DAV = "user.login.dav";
	public static final String EVENT_LOGIN_CONTAINER = "user.login.container";

	/** Name for the event of logging out. */
	public static final String EVENT_LOGOUT = "user.logout";
	
	/** Name for the CSRF token session attribute */
	public static final String SAKAI_CSRF_SESSION_ATTRIBUTE = "sakai.csrf.token";

	/**
	 * Name for the session key to retrieve the UsageSession.
	 * Note: This must be a constant and not based on classname - it must stay the same regardless of the name of the implementing class.
	 */
	public static final String USAGE_SESSION_KEY = "org.sakaiproject.event.api.UsageSessionService";

	/**
	 * Establish a usage session associated with the current request or thread.
	 * 
	 * @param userId
	 *        The user id.
	 * @param remoteAddress
	 *        The IP address from the user is making a request.
	 * @param userAgent
	 *        The string describing the user's browser.
	 * @return The new UsageSession, or null if one could not be created.
	 */
	UsageSession startSession(String userId, String remoteAddress, String userAgent);

	/**
	 * Access the usage session associated with the current request or thread.
	 * 
	 * @return The UsageSession object holding the information about this session.
	 */
	UsageSession getSession();

	/**
	 * Access the session id from the usage session associated with the current request or thread, or null if no session.
	 * 
	 * @return The session id from the usage session associated with the current request or thread, or null if no session.
	 */
	String getSessionId();

	/**
	 * Access a SessionState object with the given key associated with the current usage session.
	 * 
	 * @param key
	 *        The SessionState key.
	 * @return an SessionState object with the given key.
	 */
	SessionState getSessionState(String key);

	/**
	 * Access a usage session (may be other than the current one) by id.
	 * 
	 * @param id
	 *        the Session id.
	 * @return The UsageSession object for this id, or null if not defined.
	 */
	UsageSession getSession(String id);

	/**
	 * Access a List of usage sessions by List of ids.
	 * 
	 * @param ids
	 *        the List (String) of Session ids.
	 * @return The List (UsageSession) of UsageSession object for these ids.
	 * @deprecated not used will be removed in 1.3
	 */
	List<UsageSession> getSessions(List<String> ids);

	/**
	 * Access a List of usage sessions by *arbitrary criteria* for te session ids.
	 * 
	 * @param joinTable
	 *        the table name to (inner) join to
	 * @param joinAlias
	 *        the alias used in the criteria string for the joinTable
	 * @param joinColumn
	 *        the column name of the joinTable that is to match the session id in the join ON clause
	 * @param joinCriteria
	 *        the criteria of the select (after the where)
	 * @param values
	 *        Optional values to go with the criteria in an implementation specific way.
	 * @return The List (UsageSession) of UsageSession object for these ids.
	 */
	List<UsageSession> getSessions(String joinTable, String joinAlias, String joinColumn, String joinCriteria, Object[] values);

	/**
	 * Access the time (seconds) we will wait for any user generated request from a session before we consider the session inactive.
	 * 
	 * @return the time (seconds) used for session inactivity detection.
	 * @deprecated not used will be removed in 1.3
	 */
	int getSessionInactiveTimeout();

	/**
	 * Access the time (seconds) we will wait for hearing anyting from a session before we consider the session lost.
	 * 
	 * @return the time (seconds) used for lost session detection.
	 * @deprecated not used will be removed in 1.3
	 */
	int getSessionLostTimeout();

	/**
	 * Access a list of all open sessions.
	 * 
	 * @return a List (UsageSession) of all open sessions, ordered by server, then by start (asc)
	 * @deprecated - not used will be removed in 1.3
	 */
	List<UsageSession> getOpenSessions();

	/**
	 * Access a list of all open sessions, grouped by server.
	 * 
	 * @return a Map (server id -> List (UsageSession)) of all open sessions, ordered by server, then by start (asc)
	 */
	Map<String, List<UsageSession>> getOpenSessionsByServer();
	
	/**
	 * Start a usage session and do any other book-keeping needed to login a user who has already been authenticated.
	 * 
	 * @param authn
	 *        The user authentication.
	 * @param req
	 *        The servlet request.
	 * @return true if all went well, false if not (may fail if the userId is not a valid User)
	 */
	boolean login(Authentication authn, HttpServletRequest req);

	/**
	 * Start a usage session and do any other book-keeping needed to login a user who has already been authenticated.
	 * Use this method to specify a login event other than the default (e.g. WebDAV clients)
	 *  
	 * @param authn
	 *        The user authentication.
	 * @param req
	 *        The servlet request.
	 * @param event
	 *        The event code to post for this login.
	 * @return true if all went well, false if not (may fail if the userId is not a valid User)
	 */
	public boolean login(Authentication authn, HttpServletRequest req, String event);

	/**
	 * Start a usage session and do any other book-keeping needed to login a user who has already been authenticated.
	 * Use this method for logins that do not originate through the portal / request-filter (e.g. axis webservices)
	 * where an HttpServletRequest is not available.
	 * 
	 * @param uid
	 *        The user's uid
	 * @param eid
	 *        The user's eid
	 * @param remoteaddr
	 *        The IP address of the client
	 * @param ua
	 *        The client's user agent string
	 * @param event
	 *        The event code to post for this login.
	 * @return true if all went well, false if not (may fail if the userId is not a valid User)
	 */
	public boolean login(String uid, String eid, String remoteaddr, String ua, String event);
	
	/**
	 * End a usage session and otherwise cleanup from a login.
	 *
	 */
	void logout();
	
	/**
	 * Close any orphaned usage session records left over after a server crash.
	 * 
	 * @param validServerIds collection of server ID strings which are currently valid; an assumption is
	 * made that there will be fewer than 1000 active servers at any one time
	 * 
	 * @return number of invalid sessions closed
	 */
	int closeSessionsOnInvalidServers(List<String> validServerIds);

}
