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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.SessionStateBindingListener;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.UserDirectoryService;

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
	private static final long WARNING_SAFE_SESSIONS_TABLE_SIZE = 1750000l;
	private static final long MAX_SAFE_SESSIONS_TABLE_SIZE = 2000000l;

	/** Storage manager for this service. */
	protected Storage m_storage = null;

	/** A Cache of recently refreshed users. This is to prevent frequent authentications refreshing user data */
	protected Cache m_recentUserRefresh = null;
	
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

	/** Dependency: SqlService. */
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
		m_autoDdl = Boolean.valueOf(value).booleanValue();
	}

	/** contains a map of the database dependent handlers. */
	protected Map<String, UsageSessionServiceSql> databaseBeans;

	/** The db handler we are using. */
	protected UsageSessionServiceSql usageSessionServiceSql;

	public void setDatabaseBeans(Map databaseBeans)
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

			m_recentUserRefresh = memoryService().newCache("org.sakaiproject.event.api.UsageSessionService.recentUserRefresh");
			
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
			if (totalSessionsCount > WARNING_SAFE_SESSIONS_TABLE_SIZE) {
				log.info("The SAKAI_SESSIONS table size ("+totalSessionsCount+") is approaching the point at which " +
						"performance will begin to degrade ("+MAX_SAFE_SESSIONS_TABLE_SIZE+
						"), we recommend you archive older sessions over to another table, " +
						"remove older rows, or truncate this table before it reaches a size of "+MAX_SAFE_SESSIONS_TABLE_SIZE);
			} else if (totalSessionsCount > MAX_SAFE_SESSIONS_TABLE_SIZE) {
				log.warn("The SAKAI_SESSIONS table size ("+totalSessionsCount+") has passed the point at which " +
						"performance will begin to degrade ("+MAX_SAFE_SESSIONS_TABLE_SIZE+
						"), we recommend you archive older events over to another table, " +
						"remove older rows, or truncate this table to ensure that performance is not affected negatively");
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

	/**
	 * @inheritDoc
	 */
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
				StringBuffer sb = new StringBuffer();
				sb.append(System.currentTimeMillis());
				sb.append(session.getId());
				
				MessageDigest md;
				try {
					md = MessageDigest.getInstance("SHA-256");
					byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));
					String hashedSessionId = byteArray2Hex(digest);					
					s.setAttribute(SAKAI_CSRF_SESSION_ATTRIBUTE, hashedSessionId);
				} catch (NoSuchAlgorithmException e) {
					log.error("Failed to create a hashed session id for use as CSRF token because no SHA-256 support", e);
				} catch (UnsupportedEncodingException e) {
					log.error("Failed to create a hashed session id for use as CSRF token because could not get UTF-8 bytes of session id", e);
				}
				
				// set as the current session
				s.setAttribute(USAGE_SESSION_KEY, session);

				return session;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * @inheritDoc
	 */
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

	/**
	 * @inheritDoc
	 */
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

	/**
	 * @inheritDoc
	 */
	public UsageSession getSession(String id)
	{
		UsageSession rv = m_storage.getSession(id);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public List getSessions(List ids)
	{
		List rv = m_storage.getSessions(ids);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public List getSessions(String joinTable, String joinAlias, String joinColumn, String joinCriteria, Object[] values)
	{
		List rv = m_storage.getSessions(joinTable, joinAlias, joinColumn, joinCriteria, values);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public int getSessionInactiveTimeout()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	public int getSessionLostTimeout()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	public List getOpenSessions()
	{
		return m_storage.getOpenSessions();
	}

	/**
	 * @inheritDoc
	 */
	public Map getOpenSessionsByServer()
	{
		List all = m_storage.getOpenSessions();

		Map byServer = new TreeMap();

		List current = null;
		String key = null;

		for (Iterator i = all.iterator(); i.hasNext();)
		{
			UsageSession s = (UsageSession) i.next();

			// to start, or when the server changes, create a new inner list and add to the map
			if ((key == null) || (!key.equals(s.getServer())))
			{
				key = s.getServer();
				current = new Vector();
				byServer.put(key, current);
			}

			current.add(s);
		}

		return byServer;
	}

	/**
	 * @inheritDoc
	 */
	public boolean login(Authentication authn, HttpServletRequest req)
	{
		return login(authn.getUid(), authn.getEid(), req.getRemoteAddr(), req.getHeader("user-agent"), null);
	}

	/**
	 * @inheritDoc
	 */
	public boolean login(Authentication authn, HttpServletRequest req, String event)
	{
		return login(authn.getUid(), authn.getEid(), req.getRemoteAddr(), req.getHeader("user-agent"), event);
	}
	
	/**
	 * @inheritDoc
	 */
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
		eventTrackingService().post(eventTrackingService().newEvent(event != null ? event : EVENT_LOGIN, null, true));

		return true;
	}

	
	/**
	 * @inheritDoc
	 */
	public void logout()
	{
		userDirectoryService().destroyAuthentication();

		securityService.clearUserEffectiveRoles();
		
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
		List getSessions(List ids);

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
		 * @param fields
		 *        Optional values to go with the criteria in an implementation specific way.
		 * @return The List (UsageSession) of UsageSession object for these ids.
		 */
		List getSessions(String joinTable, String joinAlias, String joinColumn, String joinCriteria, Object[] values);

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
		List getOpenSessions();
	}

	/*************************************************************************************************************************************************
	 * SessionState
	 ************************************************************************************************************************************************/

	public class SessionStateWrapper implements SessionState
	{
		/** The ToolSession object wrapped. */
		protected ToolSession m_session = null;

		public SessionStateWrapper(ToolSession session)
		{
			m_session = session;
		}

		/**
		 * @inheritDoc
		 */
		public Object getAttribute(String name)
		{
			return m_session.getAttribute(name);
		}

		/**
		 * @inheritDoc
		 */
		public Object setAttribute(String name, Object value)
		{
			Object old = m_session.getAttribute(name);
			unBindAttributeValue(name, old);

			m_session.setAttribute(name, value);
			bindAttributeValue(name, value);

			return old;
		}

		/**
		 * @inheritDoc
		 */
		public Object removeAttribute(String name)
		{
			Object old = m_session.getAttribute(name);
			unBindAttributeValue(name, old);

			m_session.removeAttribute(name);

			return old;
		}

		/**
		 * @inheritDoc
		 */
		public void clear()
		{
			// unbind
			for (Enumeration e = m_session.getAttributeNames(); e.hasMoreElements();)
			{
				String name = (String) e.nextElement();
				Object value = m_session.getAttribute(name);
				unBindAttributeValue(name, value);
			}

			m_session.clearAttributes();
		}

		/**
		 * @inheritDoc
		 */
		@SuppressWarnings("unchecked")
		public List<String> getAttributeNames()
		{
			List<String> rv = new Vector<String>();
			for (Enumeration<String> e = m_session.getAttributeNames(); e.hasMoreElements();)
			{
				String name = (String) e.nextElement();
				rv.add(name);
			}

			return rv;
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
			if ((attribute != null) && (attribute instanceof SessionStateBindingListener))
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
			if ((attribute != null) && (attribute instanceof SessionStateBindingListener))
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
		/**
		 * Open and be ready to read / write.
		 */
		public void open()
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_session");
			}
		}

		/**
		 * Close.
		 */
		public void close()
		{
		}

		/**
		 * Take this session into storage.
		 *
		 * @param session
		 *        The usage session.
		 * @return true if added successfully, false if not.
		 */
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
				session.getStart(),
				session.getEnd(),
				session.isClosed() ? null : Boolean.valueOf(true)
			});
			if (!ok)
			{
				log.warn(".addSession(): dbWrite failed");
				return false;
			}

			return true;

		} // addSession

		/**
		 * Access a session by id
		 *
		 * @param id
		 *        The session id.
		 * @return The session object.
		 */
		public UsageSession getSession(String id)
		{
			UsageSession rv = null;

			// check the db
			String statement = usageSessionServiceSql.getSakaiSessionSql1();

			// send in the last seq number parameter
			Object[] fields = new Object[1];
			fields[0] = id;

			List sessions = sqlService().dbRead(statement, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						return new BaseUsageSession(UsageSessionServiceAdaptor.this,result);
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});

			if (!sessions.isEmpty()) rv = (UsageSession) sessions.get(0);

			return rv;

		} // getSession

		/**
		 * @inheritDoc
		 */
		public List getSessions(List ids)
		{
			// TODO: do this in a single SQL call! -ggolden
			List rv = new Vector();
			for (Iterator i = ids.iterator(); i.hasNext();)
			{
				String id = (String) i.next();
				UsageSession s = getSession(id);
				if (s != null)
				{
					rv.add(s);
				}
			}

			return rv;
		}

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
		 * @param fields
		 *        Optional values to go with the criteria in an implementation specific way.
		 * @return The List (UsageSession) of UsageSession object for these ids.
		 */
		public List getSessions(String joinTable, String joinAlias, String joinColumn, String joinCriteria, Object[] values)
		{
			// use an alias different from the alias given
			String alias = joinAlias + "X";

			// use criteria as the where clause
			String statement = usageSessionServiceSql.getSakaiSessionSql3(alias, joinAlias, joinTable, joinColumn, joinCriteria);
			List sessions = sqlService().dbRead(statement, values, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						return new BaseUsageSession(UsageSessionServiceAdaptor.this,result);
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});

			return sessions;
		}

		/**
		 * This session is now closed.
		 *
		 * @param session
		 *        The session which is closed.
		 */
		public void closeSession(UsageSession session)
		{
			// close the session on the db
			String statement = usageSessionServiceSql.getUpdateSakaiSessionSql();

			// process the statement
			boolean ok = sqlService().dbWrite(statement, new Object[]{
				session.getEnd(),
				session.isClosed() ? null : Boolean.valueOf(true),
				session.getId()
			});
			if (!ok)
			{
				log.warn(".closeSession(): dbWrite failed");
			}

		} // closeSession

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

		/**
		 * Access a list of all open sessions.
		 *
		 * @return a List (UsageSession) of all open sessions, ordered by server, then by start (asc)
		 */
		public List getOpenSessions()
		{
			// check the db
			String statement = usageSessionServiceSql.getSakaiSessionSql2();
			List sessions = sqlService().dbRead(statement, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						return new BaseUsageSession(UsageSessionServiceAdaptor.this,result);
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});

			return sessions;
		}
	}

	/**
	 * @return the current total number of sessions in the sessions table (data storage)
	 */
	protected long getSessionsCount() {
		/*
		 * NOTE: this is a weird way to get the value out but it matches the existing code
		 * Added for SAK-3793
		 */
		long totalSessionsCount = 0;
		final String sessionCountStmt = usageSessionServiceSql.getSessionsCountSql();
		try {
			List<Long> counts = sqlService().dbRead(sessionCountStmt, null, new SqlReader() {
				public Object readSqlResultRecord(ResultSet result) {
					long value = 0;
					try {
						value = result.getLong(1);
					} catch (SQLException ignore) {
						log.info("Could not get count of sessions table using SQL (" + sessionCountStmt + ")");
					}
					return new Long(value);
				}
			});
			if (counts.size() > 0) {
				totalSessionsCount = counts.get(0);
			}
		} catch (Exception e) {
			log.error("Could not get count of sessions.", e);
		}
		return totalSessionsCount;
	}

	@SuppressWarnings("unchecked")
	public int closeSessionsOnInvalidServers(List<String> validServerIds) {
		String statement = usageSessionServiceSql.getOpenSessionsOnInvalidServersSql(validServerIds);
		if (log.isDebugEnabled()) log.debug("will get sessions with SQL=" + statement);
		List<BaseUsageSession> sessions = sqlService().dbRead(statement, null, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					return new BaseUsageSession(UsageSessionServiceAdaptor.this,result);
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});
		
		for (BaseUsageSession session : sessions)
		{
			if (log.isDebugEnabled()) log.debug("invalidating session " + session.getId());
			session.invalidate();
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
