/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.mutable.MutableLong;
import org.springframework.util.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.NonPortableSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionAttributeListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.SessionStore;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;

/**
 * <p>
 * Standard implementation of the Sakai SessionManager.
 * </p>
 */
public abstract class SessionComponent implements SessionManager, SessionStore
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SessionComponent.class);

	/** The sessions - keyed by session id. */
	protected Map<String, Session> m_sessions = new ConcurrentHashMap<String, Session>();
	
	/**
	 * The expected time sessions may be ready for expiration.  This is only an optimization
	 * for when Terracotta is in use, to prevent faulting Session objects into the local
	 * JVM when it is not necessary. Session.isInactive() method remains the ultimate authority
	 * to determine if a session is invalid or not.
	 */
	protected Map<String,MutableLong> expirationTimeSuggestionMap = new ConcurrentHashMap<String, MutableLong>();
	
	private SessionAttributeListener sessionListener;

	/** The maintenance. */
	protected Maintenance m_maintenance = null;

	/** Key in the ThreadLocalManager for binding our current session. */
	protected final static String CURRENT_SESSION = "org.sakaiproject.api.kernel.session.current";

	/** Key in the ThreadLocalManager for binding our current tool session. */
	protected final static String CURRENT_TOOL_SESSION = "org.sakaiproject.api.kernel.session.current.tool";

	/** Key in the ThreadLocalManager for access to the current servlet context (from tool-util/servlet/RequestFilter). */
	protected final static String CURRENT_SERVLET_CONTEXT = "org.sakaiproject.util.RequestFilter.servlet_context";

	/** The set of tool ids that represent tools that can be clustered */
	protected Set<String> clusterableTools = new HashSet<String>();

	/** Salt for predictable session IDs */
	protected byte[] salt = null; 
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Will be used to get the current tool id when checking the whitelist */
	protected abstract ToolManager toolManager();

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected abstract ThreadLocalManager threadLocalManager();

	/**
	 * @return the IdManager collaborator.
	 */
	protected abstract IdManager idManager();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Configuration: default inactive period for sessions (seconds). */
	protected int m_defaultInactiveInterval = 30 * 60;

	/**
	 * Configuration - set the default inactive period for sessions.
	 * 
	 * @param value
	 *        The default inactive period for sessions.
	 */
	public void setInactiveInterval(String value)
	{
		try
		{
			m_defaultInactiveInterval = Integer.parseInt(value);
		}
		catch (Throwable t)
		{
			System.out.println(t);
		}
	}

	/**
	 * Configuration - set the default inactive period for sessions.
	 * 
	 * @param value
	 *        The default inactive period for sessions.
	 */
	public void setInactiveInterval(int value)
	{
		m_defaultInactiveInterval = value;
	}
	
	/**
	 * Configuration - set the default inactive period for sessions.
	 * 
	 * @return The default inactive period for sessions.
	 */
	public int getInactiveInterval()
	{
		return m_defaultInactiveInterval;
	}

	/** Configuration: how often to check for inactive sessions (seconds). */
	protected int m_checkEvery = 60;

	/**
	 * Configuration: set how often to check for inactive sessions (seconds).
	 * 
	 * @param value
	 *        The how often to check for inactive sessions (seconds) value.
	 */
	public void setCheckEvery(String value)
	{
		try
		{
			m_checkEvery = Integer.parseInt(value);
		}
		catch (Throwable t)
		{
			System.out.println(t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// start the maintenance thread
		if (m_checkEvery > 0)
		{
			m_maintenance = new Maintenance();
			m_maintenance.start();
		}
                
        // Salt generation 64 bits long

		salt = new byte[8];
		SecureRandom random;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
	        random.nextBytes(salt);
		} catch (NoSuchAlgorithmException e) {
			M_log.warn("Random number generator not available - using time randomness");
			salt = String.valueOf(System.currentTimeMillis()).getBytes();
		}

		M_log.info("init(): interval: " + m_defaultInactiveInterval + " refresh: " + m_checkEvery);
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		if (m_maintenance != null)
		{
			m_maintenance.stop();
			m_maintenance = null;
		}

		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: SessionManager
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Session getSession(String sessionId)
	{
		MySession s = (MySession) m_sessions.get(sessionId);

		return s;
	}
	
	public String makeSessionId(HttpServletRequest req, Principal principal)
	{
		MessageDigest sha;
		String sessionId;
		
		try {
			sha = MessageDigest.getInstance("SHA-1");
			
			sha.reset();
			sha.update(principal.getName().getBytes("UTF-8"));
			sha.update((byte) 0x0a);
			sha.update(req.getHeader("user-agent").getBytes("UTF-8"));
			sha.update(salt);			 
			
			sessionId = byteArrayToHexStr(sha.digest());			 
		} catch (NoSuchAlgorithmException e) {
			// Fallback to new uuid rather than a non-hashed id
			sessionId = idManager().createUuid();
		} catch (UnsupportedEncodingException e) {
			sessionId = idManager().createUuid();
		}
		
		return sessionId;
	}
	
	public List<Session> getSessions() {
	   return new ArrayList<Session>(m_sessions.values());
	}

	public void remove(String sessionId) {
		m_sessions.remove(sessionId);
		expirationTimeSuggestionMap.remove(sessionId);
	}

	/**
	 * Checks the current Tool ID to determine if this tool is marked for clustering.
	 * 
	 * @return true if the tool is marked for clustering, false otherwise.
	 */
	public boolean isCurrentToolClusterable() {
		ToolManager toolManager = toolManager();
		Tool tool = null;
		// ToolManager should exist.  Protect against it being
		// null and just log a message if it is.
		if (toolManager != null) {
			tool = toolManager.getCurrentTool();
			// tool can be null, this is common during startup for example
			if (tool != null) {
				String toolId = tool.getId();
				// if the tool exists, the toolid should.  But protect and only
				// log a message if it is null
				if (toolId != null) {
					return clusterableTools.contains(toolId);
				} else {
					M_log.error("SessionComponent.isCurrentToolClusterable(): toolId was null.");
				}
			}
		} else {
			M_log.error("SessionComponent.isCurrentToolClusterable(): toolManager was null.");
		}
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public Session startSession()
	{
		String id = idManager().createUuid();
		
		return startSession(id);
	}

	/**
	 * @inheritDoc
	 */
	public Session startSession(String id)
	{				
		// create a non portable session object if this is a clustered environment
		NonPortableSession nPS = new MyNonPortableSession();
		
		// create a new MutableLong object representing the current time that both
		// the Session and SessionManager can see.
		MutableLong currentTime = currentTimeMutableLong();

		// create a new session
		Session s = new MySession(this,id,threadLocalManager(),idManager(),this,sessionListener,m_defaultInactiveInterval,nPS,currentTime);

		// Place session into the main Session Storage, capture any old id
		Session old = m_sessions.put(s.getId(), s);
		
		// Place an entry in the expirationTimeSuggestionMap that corresponds to the entry in m_sessions
		expirationTimeSuggestionMap.put(id, currentTime);

		// check for id conflict
		if (old != null)
		{
			M_log.warn("startSession: duplication id: " + s.getId());
		}

		return s;
	}
	
	protected MutableLong currentTimeMutableLong()
	{
		return new MutableLong(System.currentTimeMillis());
	}

	/**
	 * @inheritDoc
	 */
	public Session getCurrentSession()
	{
		Session rv = (Session) threadLocalManager().get(CURRENT_SESSION);

		// if we don't have one already current, make one and bind it as current, but don't save it in our by-id table - let it just go away after the thread
		if (rv == null)
		{
			String id = idManager().createUuid();
			
			// create a non portable session object if this is a clustered environment
			NonPortableSession nPS = new MyNonPortableSession();

			rv = new MySession(this,id,threadLocalManager(),idManager(),this,sessionListener,m_defaultInactiveInterval,nPS,currentTimeMutableLong());
			setCurrentSession(rv);
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public String getCurrentSessionUserId()
	{
		Session s = (Session) threadLocalManager().get(CURRENT_SESSION);
		if (s != null)
		{
			return s.getUserId();
		}

		return null;
	}

	/**
	 * @inheritDoc
	 */
	public ToolSession getCurrentToolSession()
	{
		return (ToolSession) threadLocalManager().get(CURRENT_TOOL_SESSION);
	}

	/**
	 * @inheritDoc
	 */
	public void setCurrentSession(Session s)
	{
		threadLocalManager().set(CURRENT_SESSION, s);
	}

	/**
	 * @inheritDoc
	 */
	public void setCurrentToolSession(ToolSession s)
	{
		threadLocalManager().set(CURRENT_TOOL_SESSION, s);
	}

	public String getClusterableTools() {
		return StringUtils.collectionToCommaDelimitedString(clusterableTools);
		// return clusterableTools;
	}

	public void setClusterableTools(String clusterableToolList) {
		Set<?> newTools = StringUtils.commaDelimitedListToSet(clusterableToolList);
		this.clusterableTools.clear();
		for (Object o: newTools) {
			if (o instanceof java.lang.String) {
				this.clusterableTools.add((String)o);
			} else {
				M_log.error("SessionManager.setClusterableTools(String) unable to set value: "+o);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public int getActiveUserCount(int secs)
	{
		Set<String> activeusers = new HashSet<String>(m_sessions.size());

		long now = System.currentTimeMillis();

		for (Iterator<Session> i = m_sessions.values().iterator(); i.hasNext();)
		{
			MySession s = (MySession) i.next();

			if ((now - s.getLastAccessedTime()) < (secs * 1000))
			{
				activeusers.add(s.getUserId());
			}
		}

		// Ignore admin and postmaster
		activeusers.remove("admin");
		activeusers.remove("postmaster");
		activeusers.remove(null);

		return activeusers.size();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Maintenance
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class Maintenance implements Runnable
	{
		/** My thread running my timeout checker. */
		protected Thread m_maintenanceChecker = null;

		/** Signal to the timeout checker to stop. */
		protected boolean m_maintenanceCheckerStop = false;

		/**
		 * Construct.
		 */
		public Maintenance()
		{
		}

		/**
		 * Start the maintenance thread.
		 */
		public void start()
		{
			if (m_maintenanceChecker != null) return;

			m_maintenanceChecker = new Thread(this, "Sakai.SessionComponent.Maintenance");
			m_maintenanceCheckerStop = false;
			m_maintenanceChecker.setDaemon(true);
			m_maintenanceChecker.start();
		}

		/**
		 * Stop the maintenance thread.
		 */
		public void stop()
		{
			if (m_maintenanceChecker != null)
			{
				m_maintenanceCheckerStop = true;
				m_maintenanceChecker.interrupt();
				try
				{
					// wait for it to die
					m_maintenanceChecker.join();
				}
				catch (InterruptedException ignore)
				{
				}
				m_maintenanceChecker = null;
			}
		}

		/**
		 * Run the maintenance thread. Every m_checkEvery seconds, check for expired sessions.
		 */
		public void run()
		{
			// since we might be running while the component manager is still being created and populated, such as at server
			// startup, wait here for a complete component manager
			ComponentManager.waitTillConfigured();

			while (!m_maintenanceCheckerStop)
			{
				try
				{
					for (Map.Entry<String, MutableLong> entry: expirationTimeSuggestionMap.entrySet()) {
						if (entry.getValue().longValue() < System.currentTimeMillis()) {
							MySession s = (MySession)m_sessions.get(entry.getKey());
							if (M_log.isDebugEnabled()) M_log.debug("checking session " + s.getId());
							if (s.isInactive())
							{
								if (M_log.isDebugEnabled()) M_log.debug("invalidating session " + s.getId());
								synchronized(s) {
									s.invalidate();
								}
							}
						}
					}
				}
				catch (Throwable e)
				{
					M_log.warn("run(): exception: " + e);
				}

				// cycle every REFRESH seconds
				if (!m_maintenanceCheckerStop)
				{
					try
					{
						Thread.sleep(m_checkEvery * 1000L);
					}
					catch (Exception ignore)
					{
					}
				}
			}
		}
	}

	public SessionAttributeListener getSessionListener() {
		return sessionListener;
	}

	public void setSessionListener(SessionAttributeListener sessionListener) {
		this.sessionListener = sessionListener;
	}

	private static String byteArrayToHexStr(byte[] data)
	{
         char[] chars = new char[data.length * 2];
         for (int i = 0; i < data.length; i++)
         {
             byte current = data[i];
             int hi = (current & 0xF0) >> 4;
             int lo = current & 0x0F;
             chars[2*i] =  (char) (hi < 10 ? ('0' + hi) : ('A' + hi - 10));
             chars[2*i+1] =  (char) (lo < 10 ? ('0' + lo) : ('A' + lo - 10));
         }
         return new String(chars);
	}

}
