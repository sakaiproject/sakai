/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

package org.sakaiproject.event.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.SessionStateBindingListener;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <p>
 * UsageSessionServiceAdaptor implements the UsageSessionService. The Session aspects are done as an adaptor to the SessionManager. UsageSession
 * entities are handled as was in the ClusterUsageSessionService.
 * </p>
 */
@Slf4j
public abstract class UsageSessionServiceAdaptor implements UsageSessionService
{
	// see http://jira.sakaiproject.org/browse/SAK-3793 for more info about these numbers
	private static final long WARNING_SAFE_SESSIONS_TABLE_SIZE = 1750000L;
	private static final long MAX_SAFE_SESSIONS_TABLE_SIZE = 2000000L;

	/** Storage manager for this service. */
	protected Storage m_storage = null;

	/** A Cache of recently refreshed users. This is to prevent frequent authentications refreshing user data */
	protected Cache<String, Boolean> m_recentUserRefresh = null;
	
	/*************************************************************************************************************************************************
	 * Abstractions, etc.
	 ************************************************************************************************************************************************/

	/**
	 * Construct storage for this service.
	 */
	protected Storage newStorage()
	{
		return new ClusterStorage();
	}

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/

	/**
	 * @return the TimeService collaborator.
	 */
	protected abstract TimeService timeService();

	/**
	 * @return the SqlService collaborator.
	 */
	protected abstract SqlService sqlService();

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected abstract ThreadLocalManager threadLocalManager();

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the IdManager collaborator.
	 */
	protected abstract IdManager idManager();

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the AuthzGroupService collaborator.
	 */
	protected abstract AuthzGroupService authzGroupService();

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**
	 * 
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();
	
	private SecurityService securityService;

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	
	

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 *
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = BooleanUtils.toBoolean(value);
	}

	/** contains a map of the database dependent handlers. */
	protected Map<String, UsageSessionServiceSql> databaseBeans;

	/** The db handler we are using. */
	protected UsageSessionServiceSql usageSessionServiceSql;

	public void setDatabaseBeans(Map<String, UsageSessionServiceSql> databaseBeans)
	{
		this.databaseBeans = databaseBeans;
	}

	public UsageSessionServiceSql getUsageSessionServiceSql()
	{
		return usageSessionServiceSql;
	}

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	public void setUsageSessionServiceSql(String vendor)
	{
		this.usageSessionServiceSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
	}

	/*************************************************************************************************************************************************
	 * Init and Destroy
	 ************************************************************************************************************************************************/

	public UsageSessionServiceAdaptor()
	{
		m_storage = newStorage();
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// open storage
			m_storage.open();

			m_recentUserRefresh = memoryService().getCache("org.sakaiproject.event.api.UsageSessionService.recentUserRefresh");
			
			log.info("init()");
		}
		catch (Exception t)
		{
			log.error("init(): ", t);
		}
		setUsageSessionServiceSql(sqlService().getVendor());
		
		boolean sessionsSizeCheck = serverConfigurationService().getBoolean("sessions.size.check", true);
		if (sessionsSizeCheck) {
			long totalSessionsCount = getSessionsCount();
			if (totalSessionsCount > MAX_SAFE_SESSIONS_TABLE_SIZE) {
				log.warn("The SAKAI_SESSIONS table size (" + totalSessionsCount + ") has passed the point at which " +
						"performance will begin to degrade (" + MAX_SAFE_SESSIONS_TABLE_SIZE +
						"), we recommend you archive older events over to another table, " +
						"remove older rows, or truncate this table to ensure that performance is not affected negatively");
			} else if (totalSessionsCount > WARNING_SAFE_SESSIONS_TABLE_SIZE) {
				log.info("The SAKAI_SESSIONS table size ("+totalSessionsCount+") is approaching the point at which " +
						"performance will begin to degrade ("+MAX_SAFE_SESSIONS_TABLE_SIZE+
						"), we recommend you archive older sessions over to another table, " +
						"remove older rows, or truncate this table before it reaches a size of "+MAX_SAFE_SESSIONS_TABLE_SIZE);
			}
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_storage.close();

		log.info("destroy()");
	}

	/*************************************************************************************************************************************************
	 * UsageSessionService implementation
	 ************************************************************************************************************************************************/

	@Override
	public UsageSession startSession(String userId, String remoteAddress, String userAgent)
	{
		// do we have a current session?
		Session s = sessionManager().getCurrentSession();
		if (s != null)
		{
			UsageSession session = (UsageSession) s.getAttribute(USAGE_SESSION_KEY);
			if (session != null)
			{
				// If we have a session for this user, simply reuse
				if (userId != null && userId.equals(session.getUserId()))
				{
					return session;
				}

				// if it is for another user, we will create a new session, log a warning, and unbound/close the existing one
				s.setAttribute(USAGE_SESSION_KEY, null);
				log.warn("startSession: replacing existing UsageSession: " + session.getId() + " user: " + session.getUserId() + " for new user: "
						+ userId);
			}

			// resolve the hostname if required
			String hostName = null;
				
			if (serverConfigurationService().getBoolean("session.resolvehostname", false)) 
			{
				try
				{
					InetAddress inet = InetAddress.getByName(remoteAddress);
					hostName = inet.getHostName();
				}
				catch (UnknownHostException e)
				{
					log.debug("Cannot resolve host address " + remoteAddress);
				}
			}
			
			// create the usage session and bind it to the session
			session = new BaseUsageSession(this, idManager().createUuid(), serverConfigurationService().getServerIdInstance(), userId,
					remoteAddress, hostName, userAgent);

			// store
			if (m_storage.addSession(session))
			{
				// set a CSRF token
				StringBuilder sb = new StringBuilder();
				sb.append(System.currentTimeMillis());
				sb.append(session.getId());
				
				MessageDigest md;
				try {
					md = MessageDigest.getInstance("SHA-256");
					byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
					String hashedSessionId = byteArray2Hex(digest);					
					s.setAttribute(SAKAI_CSRF_SESSION_ATTRIBUTE, hashedSessionId);
				} catch (NoSuchAlgorithmException e) {
					log.error("Failed to create a hashed session id for use as CSRF token because no SHA-256 support", e);
				}

                // set as the current session
				s.setAttribute(USAGE_SESSION_KEY, session);

				return session;
			}
		}

		return null;
	}

	@Override
	public UsageSession getSession()
	{
		UsageSession rv = null;

		// do we have a current session?
		Session s = sessionManager().getCurrentSession();
		if (s != null)
		{
			// do we have a usage session in the session?
			rv = (BaseUsageSession) s.getAttribute(USAGE_SESSION_KEY);
		}

		else
		{
			log.warn("getSession: no current SessionManager session!");
		}

		return rv;
	}

	@Override
	public String getSessionId()
	{
		String rv = null;

		// See http://bugs.sakaiproject.org/jira/browse/SAK-1507
		// At server startup, when Spring is initializing components, there may not
		// be a session manager yet. This adaptor may be called before all components
		// are initialized since there are hidden dependencies (through static covers)
		// of which Spring is not aware. Therefore, check for and handle a null
		// sessionManager().
		if (sessionManager() == null) return null;

		// do we have a current session?
		Session s = sessionManager().getCurrentSession();
		if (s != null)
		{
			// do we have a usage session in the session?
			BaseUsageSession session = (BaseUsageSession) s.getAttribute(USAGE_SESSION_KEY);
			if (session != null)
			{
				rv = session.getId();
			}
		}

		// may be null, which indicates that there's no session
		return rv;
	}

	@Override
	public SessionState getSessionState(String key)
	{
		// map this to the sakai session's tool session concept, using key as the placement id
		Session s = sessionManager().getCurrentSession();
		if (s != null)
		{
			return new SessionStateWrapper(s.getToolSession(key));
		}

		log.warn("getSessionState(): no session:  key: " + key);
		return null;
	}

	@Override
	public UsageSession getSession(String id)
	{

        return m_storage.getSession(id);
	}

	@Override
	public List<UsageSession> getSessions(List<String> ids)
	{

        return m_storage.getSessions(ids);
	}

	@Override
	public List<UsageSession> getSessions(String joinTable, String joinAlias, String joinColumn, String joinCriteria, Object[] values)
	{
        return m_storage.getSessions(joinTable, joinAlias, joinColumn, joinCriteria, values);
	}

	@Override
	public int getSessionInactiveTimeout()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getSessionLostTimeout()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<UsageSession> getOpenSessions()
	{
		return m_storage.getOpenSessions();
	}

	@Override
	public Map<String, List<UsageSession>> getOpenSessionsByServer()
	{
		List<UsageSession> openSessions = m_storage.getOpenSessions();

		Map<String, List<UsageSession>> byServer = new HashMap<>();

        for (UsageSession session : openSessions) {
            String key = session.getServer();
            List<UsageSession> list = byServer.computeIfAbsent(key, k -> new ArrayList<>());
            list.add(session);
        }

		return byServer;
	}

	@Override
	public boolean login(Authentication authn, HttpServletRequest req)
	{
		return login(authn.getUid(), authn.getEid(), req.getRemoteAddr(), req.getHeader("user-agent"), null);
	}

	@Override
	public boolean login(Authentication authn, HttpServletRequest req, String event)
	{
		return login(authn.getUid(), authn.getEid(), req.getRemoteAddr(), req.getHeader("user-agent"), event);
	}
	
	@Override
	public boolean login(String uid, String eid, String remoteaddr, String ua, String event)
	{
		// establish the user's session - this has been known to fail
		UsageSession session = startSession(uid, remoteaddr, ua);
		if (session == null)
		{
			return false;
		}

		// set the user information into the current session
		Session sakaiSession = sessionManager().getCurrentSession();
		sakaiSession.setUserId(uid);
		sakaiSession.setUserEid(eid);

		// update the user's externally provided realm definitions
		if (m_recentUserRefresh != null && m_recentUserRefresh.get(uid) != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("User is still in cache of recent refreshes: "+ uid);
			}
		}
		else
		{
			authzGroupService().refreshUser(uid);
			if (m_recentUserRefresh != null)
			{
				// Cache the refresh.
				m_recentUserRefresh.put(uid, Boolean.TRUE);
				if (log.isDebugEnabled())
				{
					log.debug("User is not in recent cache of refreshes: "+ uid);
				}
			}
		}

		// post the login event
		sakaiSession.setAttribute(Session.JUST_LOGGED_IN, Boolean.TRUE);
		eventTrackingService().post(eventTrackingService().newEvent(event != null ? event : EVENT_LOGIN, null, true));
		return true;
	}

	
	@Override
	public void logout()
	{
		userDirectoryService().destroyAuthentication();

		// invalidate the sakai session, which makes it unavailable, unbinds all the bound objects,
		// including the session, which will close and generate the logout event
		Session sakaiSession = sessionManager().getCurrentSession();
		sakaiSession.invalidate();
	}

	/**
	 * Generate the logout event.
	 */
	protected void logoutEvent(UsageSession session)
	{
		if (session == null)
		{
			// generate a logout event (current session)
			eventTrackingService().post(eventTrackingService().newEvent(EVENT_LOGOUT, null, true));
		}
		else
		{
			// generate a logout event (this session)
			eventTrackingService().post(eventTrackingService().newEvent(EVENT_LOGOUT, null, true), session);
		}
		
	}

	public void impersonateUser(String userId) throws SakaiException {

		Session currentSession = sessionManager().getCurrentSession();
		if (currentSession != null) {
			try {
				User mockUser = userDirectoryService().getUser(userId);
				String mockUserId = mockUser.getId();
				String mockUserEid = mockUser.getEid();

				eventTrackingService().post(eventTrackingService().newEvent(UsageSessionService.EVENT_ROLEVIEW_BECOME, userDirectoryService().userReference(mockUserId), false));
				log.info("Entering into RoleView mode, real user [{}] is impersonating mock user [{}]", currentSession.getUserEid(), mockUserEid);

				// while keeping the official usage session under the real user id,
				// switch over everything else to be the mock user
				List<String> saveAttributes = List.of(
						UsageSessionService.USAGE_SESSION_KEY,
						UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
				// logout - clear, but do not invalidate, preserving the current session
				currentSession.clearExcept(saveAttributes);
				// login - set the user id and eid into session, and refresh this user's authz information
				currentSession.setUserId(mockUserId);
				currentSession.setUserEid(mockUserEid);
				authzGroupService().refreshUser(mockUserId);
			} catch (UserNotDefinedException undfe) {
				log.warn("The mock user [{}] could not be found, {}", userId, undfe.toString());
			} catch (Exception e) {
				log.error("Could not perform RoleView for user [{}], session [{}] maybe contaminated, {}", userId, currentSession.getId(), e.toString());
				currentSession.invalidate();
				throw new SakaiException(e);
			}
		} else {
			log.warn("Switch to roleview was requested for user [{}], but a session does not exist for this request, skipping", userId);
		}
	}

	public void restoreUser() throws SakaiException {
		Session currentSession = sessionManager().getCurrentSession();
		if (currentSession != null) {
			UsageSession usageSession = (UsageSession) currentSession.getAttribute(USAGE_SESSION_KEY);
			if (usageSession != null) {
				String realUserId = usageSession.getUserId();
				String realUserEid = usageSession.getUserEid();

				if (StringUtils.isAnyBlank(realUserId, realUserEid)) {
					log.error("Can not restore session from roleview mode, missing the real user information, session is likely corrupt");
					currentSession.invalidate();
					throw new SakaiException("Can not restore session from roleview mode, missing the real user information");
				}

				// Restore the original user session
				List<String> saveAttributes = List.of(
						UsageSessionService.USAGE_SESSION_KEY,
						UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
				currentSession.clearExcept(saveAttributes);

				currentSession.setUserId(realUserId);
				currentSession.setUserEid(realUserEid);
				authzGroupService().refreshUser(realUserId);
				log.info("Exiting from roleview mode, restored real user [{}] for session [{}]", realUserEid, currentSession.getId());
			} else {
				log.error("Can not restore session from roleview mode, missing the original session, session is likely corrupt");
				currentSession.invalidate();
				throw new SakaiException("Can not restore session from roleview mode, missing the original session");
			}
		} else {
			log.warn("Restore from roleview for user, but a session does not exist for this request, skipping");
		}
	}

	private UsageSession readSqlResultRecord(ResultSet result) {
		try {
			return new BaseUsageSession(UsageSessionServiceAdaptor.this, result);
		} catch (SQLException ignore) {
			return null;
		}
	}

	/*************************************************************************************************************************************************
	 * Storage
	 ************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open.
		 */
		void open();

		/**
		 * Close.
		 */
		void close();

		/**
		 * Take this session into storage.
		 *
		 * @param session
		 *        The usage session.
		 * @return true if added successfully, false if not.
		 */
		boolean addSession(UsageSession session);

		/**
		 * Access a session by id
		 *
		 * @param id
		 *        The session id.
		 * @return The session object.
		 */
		UsageSession getSession(String id);

		/**
		 * Access a bunch of sessions by the List id session ids.
		 *
		 * @param ids
		 *        The session id List.
		 * @return The List (UsageSession) of session objects for these ids.
		 */
		List<UsageSession> getSessions(List<String> ids);

		/**
		 * Access a List of active usage sessions by *arbitrary criteria* for the session ids.
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
		 * This session is now closed.
		 *
		 * @param session
		 *        The session which is closed.
		 */
		void closeSession(UsageSession session);

		/**
		 * Update the server field of this session.
		 *
		 * @param session
		 *        The session whose server has been udpated.
		 */
		void updateSessionServer(UsageSession session);

		/**
		 * Access a list of all open sessions.
		 *
		 * @return a List (UsageSession) of all open sessions, ordered by server, then by start (asc)
		 */
		List<UsageSession> getOpenSessions();
	}

	/*************************************************************************************************************************************************
	 * SessionState
	 ************************************************************************************************************************************************/

	public static class SessionStateWrapper implements SessionState
	{
		/** The ToolSession object wrapped. */
		protected ToolSession m_session = null;

		public SessionStateWrapper(ToolSession session)
		{
			m_session = session;
		}

		@Override
		public Object getAttribute(String name)
		{
			return m_session.getAttribute(name);
		}

		@Override
		public Object getAttribute(String name, Object def)
		{
			Object ret = m_session.getAttribute(name);
			if (ret == null) {
				return def;
			}
			return ret;
		}

		@Override
		public Object setAttribute(String name, Object value)
		{
			Object old = m_session.getAttribute(name);
			unBindAttributeValue(name, old);

			m_session.setAttribute(name, value);
			bindAttributeValue(name, value);

			return old;
		}

		@Override
		public Object removeAttribute(String name)
		{
			Object old = m_session.getAttribute(name);
			unBindAttributeValue(name, old);

			m_session.removeAttribute(name);

			return old;
		}

		@Override
		public void clear()
		{
			// unbind
			for (Enumeration<String> e = m_session.getAttributeNames(); e.hasMoreElements();)
			{
				String name = e.nextElement();
				Object value = m_session.getAttribute(name);
				unBindAttributeValue(name, value);
			}

			m_session.clearAttributes();
		}

		@Override
		public List<String> getAttributeNames()
		{
			List<String> list = new ArrayList<>();
			for (Enumeration<String> e = m_session.getAttributeNames(); e.hasMoreElements();) {
				list.add(e.nextElement());
			}
			return list;
		}

		/**
		 * If the object is a SessionStateBindingListener, unbind it
		 *
		 * @param attributeName
		 *        The attribute name.
		 * @param attribute
		 *        The attribute object
		 */
		protected void unBindAttributeValue(String attributeName, Object attribute)
		{
			// if this object wants session binding notification
			if (attribute instanceof SessionStateBindingListener)
			{
				try
				{
					((SessionStateBindingListener) attribute).valueUnbound(null, attributeName);
				}
				catch (Exception e)
				{
					log.error("unBindAttributeValue: unbinding exception: ", e);
				}
			}
		}

		/**
		 * If the object is a SessionStateBindingListener, bind it
		 *
		 * @param attributeName
		 *        The attribute name.
		 * @param attribute
		 *        The attribute object
		 */
		protected void bindAttributeValue(String attributeName, Object attribute)
		{
			// if this object wants session binding notification
			if (attribute instanceof SessionStateBindingListener)
			{
				try
				{
					((SessionStateBindingListener) attribute).valueBound(null, attributeName);
				}
				catch (Exception e)
				{
					log.error("bindAttributeValue: unbinding exception: ", e);
				}
			}
		}
	}

	/*************************************************************************************************************************************************
	 * Storage component
	 ************************************************************************************************************************************************/

	protected class ClusterStorage implements Storage
	{
		@Override
		public void open()
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_session");
			}
		}

		@Override
		public void close()
		{
		}

		@Override
		public boolean addSession(UsageSession session)
		{
			// and store it in the db
			String statement = usageSessionServiceSql.getInsertSakaiSessionSql();

			String userAgent = (session.getUserAgent() != null && session.getUserAgent().length() > 255) ? 
				session.getUserAgent().substring(0, 255) : session.getUserAgent();
			
			String hostName = session.getHostName();
			
			if (hostName != null && hostName.length() > 255) {
				hostName = hostName.substring(0, 255);
			}

			// process the insert
			boolean ok = sqlService().dbWrite(statement, new Object[] {
				session.getId(),
				session.getServer(),
				session.getUserId(),
				session.getIpAddress(),
				hostName,
				userAgent,
				session.getStartInstant(),
				session.getEndInstant(),
				Boolean.valueOf(!session.isClosed())
			});
			if (!ok)
			{
				log.warn(".addSession(): dbWrite failed");
				return false;
			}

			return true;

		}

		@Override
		public UsageSession getSession(String id)
		{
			// check the db
			String statement = usageSessionServiceSql.getSakaiSessionSql1();

			// send in the last seq number parameter
			Object[] fields = new Object[1];
			fields[0] = id;

			List<UsageSession> sessions = sqlService().dbRead(statement, fields, UsageSessionServiceAdaptor.this::readSqlResultRecord);

			return sessions.isEmpty() ? null : sessions.get(0);
		}

		@Override
		public List<UsageSession> getSessions(List<String> ids)
		{
			// TODO: do this in a single SQL call! -ggolden
			return ids.stream().map(this::getSession).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
		}

		@Override
		public List<UsageSession> getSessions(String joinTable, String joinAlias, String joinColumn, String joinCriteria, Object[] values)
		{
			// use an alias different from the alias given
			String alias = joinAlias + "X";

			// use criteria as the where clause
			String statement = usageSessionServiceSql.getSakaiSessionSql3(alias, joinAlias, joinTable, joinColumn, joinCriteria);

            return sqlService().dbRead(statement, values, UsageSessionServiceAdaptor.this::readSqlResultRecord);
		}

		@Override
		public void closeSession(UsageSession session)
		{
			// close the session on the db
			String statement = usageSessionServiceSql.getUpdateSakaiSessionSql();

			// process the statement
			boolean ok = sqlService().dbWrite(statement, new Object[] {
				session.getEndInstant(),
                Boolean.valueOf(!session.isClosed()),
				session.getId()
			});
			if (!ok)
			{
				log.warn(".closeSession(): dbWrite failed");
			}

		}

		@Override
		public void updateSessionServer(UsageSession session)
		{
			// get the update sql statement
			String statement = usageSessionServiceSql.getUpdateServerSakaiSessionSql();
			
			// execute the statement
			boolean ok = sqlService().dbWrite(statement, new Object[] {
					session.getServer(),
					session.getId()
			});
			if (!ok)
			{
				log.warn(".updateSessionServer(): dbWrite failed");
			}
		}

		@Override
		public List<UsageSession> getOpenSessions()
		{
			// check the db
			String statement = usageSessionServiceSql.getSakaiSessionSql2();

            return sqlService().dbRead(statement, null, UsageSessionServiceAdaptor.this::readSqlResultRecord);
		}
	}

	/**
	 * @return the current total number of sessions in the sessions table (data storage)
	 */
	protected long getSessionsCount() {
		// NOTE: this is a weird way to get the value, but it matches the existing code
		long totalSessionsCount = 0;
		final String sessionCountStmt = usageSessionServiceSql.getSessionsCountSql();
		try {
			List<Long> counts = sqlService().dbRead(sessionCountStmt, null, result -> {
                long value = 0;
                try {
                    value = result.getLong(1);
                } catch (SQLException ignore) {
                    log.warn("Could not get count of sessions table using SQL [{}]", sessionCountStmt);
                }
                return value;
            });
			if (!counts.isEmpty()) {
				totalSessionsCount = counts.get(0);
			}
		} catch (Exception e) {
			log.error("Could not get count of sessions.", e);
		}
		return totalSessionsCount;
	}

	@Override
	public int closeSessionsOnInvalidServers(List<String> validServerIds) {
		String statement = usageSessionServiceSql.getOpenSessionsOnInvalidServersSql(validServerIds);
		List<UsageSession> sessions = sqlService().dbRead(statement, null, this::readSqlResultRecord);
		
		for (UsageSession session : sessions) {
			log.debug("invalidating session {}", session.getId());
			((BaseUsageSession) session).invalidate();
		}
		
		return sessions.size();
	}
	
	private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
	
}
