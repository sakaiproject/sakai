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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ContextSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.IteratorEnumeration;

/**
 * <p>
 * Standard implementation of the Sakai SessionManager.
 * </p>
 */
public abstract class SessionComponent implements SessionManager
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SessionComponent.class);

	/** The sessions - keyed by session id. */
	protected Map m_sessions = new ConcurrentHashMap();

	/** The maintenance. */
	protected Maintenance m_maintenance = null;

	/** Key in the ThreadLocalManager for binding our current session. */
	protected final static String CURRENT_SESSION = "org.sakaiproject.api.kernel.session.current";

	/** Key in the ThreadLocalManager for binding our current tool session. */
	protected final static String CURRENT_TOOL_SESSION = "org.sakaiproject.api.kernel.session.current.tool";

	/** Key in the ThreadLocalManager for access to the current servlet context (from tool-util/servlet/RequestFilter). */
	protected final static String CURRENT_SERVLET_CONTEXT = "org.sakaiproject.util.RequestFilter.servlet_context";

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

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

	/**
	 * @inheritDoc
	 */
	public Session startSession()
	{
		// create a new session
		Session s = new MySession();

		// remember it by id
		Session old = (Session) m_sessions.put(s.getId(), s);

		// check for id conflict
		if (old != null)
		{
			M_log.warn("startSession: duplication id: " + s.getId());
		}

		return s;
	}

	/**
	 * @inheritDoc
	 */
	public Session startSession(String id)
	{
		// create a new session
		Session s = new MySession(id);

		// remember it by id
		Session old = (Session) m_sessions.put(s.getId(), s);

		// check for id conflict
		if (old != null)
		{
			M_log.warn("startSession(id): duplication id: " + s.getId());
		}

		return s;
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
			rv = new MySession();
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

	/**
	 * @inheritDoc
	 */
	public int getActiveUserCount(int secs)
	{
		Set activeusers = new HashSet(m_sessions.size());

		long now = System.currentTimeMillis();

		for (Iterator i = m_sessions.values().iterator(); i.hasNext();)
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

	/*************************************************************************************************************************************************
	 * Entity: Session Also is an HttpSession
	 ************************************************************************************************************************************************/

	public class MySession implements Session, HttpSession
	{
		/** Hold attributes in a Map. TODO: ConcurrentHashMap may be better for multiple writers */
		protected Map m_attributes = new ConcurrentHashMap();

		/** Hold toolSessions in a Map, by placement id. TODO: ConcurrentHashMap may be better for multiple writers */
		protected Map m_toolSessions = new ConcurrentHashMap();

		/** Hold context toolSessions in a Map, by context (webapp) id. TODO: ConcurrentHashMap may be better for multiple writers */
		protected Map m_contextSessions = new ConcurrentHashMap();

		/** The creation time of the session. */
		protected long m_created = 0;

		/** The session id. */
		protected String m_id = null;

		/** Time last accessed (via getSession()). */
		protected long m_accessed = 0;

		/** Seconds of inactive time before being automatically invalidated - 0 turns off this feature. */
		protected int m_inactiveInterval = m_defaultInactiveInterval;

		/** The user id for this session. */
		protected String m_userId = null;

		/** The user enterprise id for this session. */
		protected String m_userEid = null;

		/** True while the session is valid. */
		protected boolean m_valid = true;

		public MySession()
		{
			m_id = idManager().createUuid();
			m_created = System.currentTimeMillis();
			m_accessed = m_created;
		}

		public MySession(String id)
		{
			m_id = id;
			m_created = System.currentTimeMillis();
			m_accessed = m_created;
		}

		/**
		 * @inheritDoc
		 */
		public Object getAttribute(String name)
		{
			return m_attributes.get(name);
		}

		/**
		 * @inheritDoc
		 */
		public Enumeration getAttributeNames()
		{
			return new IteratorEnumeration(m_attributes.keySet().iterator());
		}

		/**
		 * @inheritDoc
		 */
		public long getCreationTime()
		{
			return m_created;
		}

		/**
		 * @inheritDoc
		 */
		public String getId()
		{
			return m_id;
		}

		/**
		 * @inheritDoc
		 */
		public long getLastAccessedTime()
		{
			return m_accessed;
		}

		/**
		 * @inheritDoc
		 */
		public int getMaxInactiveInterval()
		{
			return m_inactiveInterval;
		}

		/**
		 * @inheritDoc
		 */
		public String getUserEid()
		{
			return m_userEid;
		}

		/**
		 * @inheritDoc
		 */
		public String getUserId()
		{
			return m_userId;
		}

		/**
		 * @inheritDoc
		 */
		public void invalidate()
		{
			m_valid = false;

			// move the attributes and tool sessions to local maps in a synchronized block so the unbinding happens only on one thread
			Map unbindMap = null;
			Map toolMap = null;
			Map contextMap = null;
			synchronized (this)
			{
				unbindMap = new HashMap(m_attributes);
				m_attributes.clear();

				toolMap = new HashMap(m_toolSessions);
				m_toolSessions.clear();

				contextMap = new HashMap(m_contextSessions);
				m_contextSessions.clear();

				// let it not be found
				m_sessions.remove(getId());
			}

			// clear each tool session
			for (Iterator i = toolMap.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry e = (Map.Entry) i.next();
				ToolSession t = (ToolSession) e.getValue();
				t.clearAttributes();
			}

			// clear each context session
			for (Iterator i = contextMap.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry e = (Map.Entry) i.next();
				ToolSession t = (ToolSession) e.getValue();
				t.clearAttributes();
			}

			// send unbind events
			for (Iterator i = unbindMap.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry e = (Map.Entry) i.next();
				String name = (String) e.getKey();
				Object value = e.getValue();
				unBind(name, value);
			}

			// if this is the current session, remove it
			if (this.equals(getCurrentSession()))
			{
				setCurrentSession(null);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void clear()
		{
			// move the attributes and tool sessions to local maps in a synchronized block so the unbinding happens only on one thread
			Map unbindMap = null;
			Map toolMap = null;
			Map contextMap = null;
			synchronized (this)
			{
				unbindMap = new HashMap(m_attributes);
				m_attributes.clear();

				toolMap = new HashMap(m_toolSessions);
				m_toolSessions.clear();

				contextMap = new HashMap(m_contextSessions);
				m_contextSessions.clear();
			}

			// clear each tool session
			for (Iterator i = toolMap.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry e = (Map.Entry) i.next();
				ToolSession t = (ToolSession) e.getValue();
				t.clearAttributes();
			}

			// clear each context session
			for (Iterator i = contextMap.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry e = (Map.Entry) i.next();
				ToolSession t = (ToolSession) e.getValue();
				t.clearAttributes();
			}

			// send unbind events
			for (Iterator i = unbindMap.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry e = (Map.Entry) i.next();
				String name = (String) e.getKey();
				Object value = e.getValue();
				unBind(name, value);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void clearExcept(Collection names)
		{
			// save any attributes in names
			Map saveAttributes = new HashMap();
			for (Iterator i = names.iterator(); i.hasNext();)
			{
				String name = (String) i.next();
				Object value = m_attributes.get(name);
				if (value != null)
				{
					// remvove, but do NOT unbind
					m_attributes.remove(name);
					saveAttributes.put(name, value);
				}
			}

			// clear the remaining
			clear();

			// restore the saved attributes
			for (Iterator i = saveAttributes.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry e = (Map.Entry) i.next();
				String name = (String) e.getKey();
				Object value = e.getValue();
				m_attributes.put(name, value);
			}
		}

		/**
		 * @inheritDoc
		 */
		public void setActive()
		{
			m_accessed = System.currentTimeMillis();
		}

		/**
		 * @inheritDoc
		 */
		public void removeAttribute(String name)
		{
			// remove
			Object value = m_attributes.remove(name);

			// unbind event
			unBind(name, value);
		}

		/**
		 * @inheritDoc
		 */
		public void setAttribute(String name, Object value)
		{
			// treat a set to null as a remove
			if (value == null)
			{
				removeAttribute(name);
			}

			else
			{
				// add
				Object old = m_attributes.put(name, value);
	
				// bind event
				bind(name, value);
	
				// unbind event if old exiss
				if (old != null)
				{
					unBind(name, old);
				}
			}
		}

		/**
		 * @inheritDoc
		 */
		public void setMaxInactiveInterval(int interval)
		{
			m_inactiveInterval = interval;
		}

		/**
		 * @inheritDoc
		 */
		public void setUserEid(String eid)
		{
			m_userEid = eid;
		}

		/**
		 * @inheritDoc
		 */
		public void setUserId(String uid)
		{
			m_userId = uid;
		}

		/**
		 * @inheritDoc
		 */
		public ToolSession getToolSession(String placementId)
		{
			ToolSession t = (ToolSession) m_toolSessions.get(placementId);
			if (t == null)
			{
				t = new MyLittleSession(this, placementId);
				m_toolSessions.put(placementId, t);
			}

			// mark it as accessed
			((MyLittleSession) t).setAccessed();

			return t;
		}

		/**
		 * @inheritDoc
		 */
		public ContextSession getContextSession(String contextId)
		{
			ContextSession t = (ContextSession) m_contextSessions.get(contextId);
			if (t == null)
			{
				t = new MyLittleSession(this, contextId);
				m_contextSessions.put(contextId, t);
			}

			// mark it as accessed
			((MyLittleSession) t).setAccessed();

			return t;
		}

		/**
		 * Check if the session has become inactive
		 * 
		 * @return true if the session is capable of becoming inactive and has done so, false if not.
		 */
		protected boolean isInactive()
		{
			return ((m_inactiveInterval > 0) && (System.currentTimeMillis() > (m_accessed + (m_inactiveInterval * 1000))));
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Session))
			{
				return false;
			}

			return ((Session) obj).getId().equals(getId());
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode()
		{
			return getId().hashCode();
		}

		/**
		 * {@inheritDoc}
		 */
		public ServletContext getServletContext()
		{
			return (ServletContext) threadLocalManager().get(CURRENT_SERVLET_CONTEXT);
		}

		/**
		 * {@inheritDoc}
		 */
		public HttpSessionContext getSessionContext()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getValue(String arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public String[] getValueNames()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void putValue(String arg0, Object arg1)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void removeValue(String arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isNew()
		{
			return false;
		}

		/**
		 * Unbind the value if it's a SessionBindingListener. Also does the HTTP unbinding if it's a HttpSessionBindingListener.
		 * 
		 * @param name
		 *        The attribute name bound.
		 * @param value
		 *        The bond value.
		 */
		protected void unBind(String name, Object value)
		{
			if (value instanceof SessionBindingListener)
			{
				SessionBindingEvent event = new MySessionBindingEvent(name, this, value);
				((SessionBindingListener) value).valueUnbound(event);
			}

			// also unbind any objects that are regular HttpSessionBindingListeners
			if (value instanceof HttpSessionBindingListener)
			{
				HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
				((HttpSessionBindingListener) value).valueUnbound(event);
			}
		}

		/**
		 * Bind the value if it's a SessionBindingListener. Also does the HTTP binding if it's a HttpSessionBindingListener.
		 * 
		 * @param name
		 *        The attribute name bound.
		 * @param value
		 *        The bond value.
		 */
		protected void bind(String name, Object value)
		{
			if (value instanceof SessionBindingListener)
			{
				SessionBindingEvent event = new MySessionBindingEvent(name, this, value);
				((SessionBindingListener) value).valueBound(event);
			}

			// also bind any objects that are regular HttpSessionBindingListeners
			if (value instanceof HttpSessionBindingListener)
			{
				HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
				((HttpSessionBindingListener) value).valueBound(event);
			}
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Entity: SessionBindingEvent
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class MySessionBindingEvent implements SessionBindingEvent
	{
		/** The attribute name. */
		protected String m_name = null;

		/** The session. */
		protected Session m_session = null;

		/** The value. */
		protected Object m_value = null;

		/**
		 * Construct.
		 * 
		 * @param name
		 *        The name.
		 * @param session
		 *        The session.
		 * @param value
		 *        The value.
		 */
		MySessionBindingEvent(String name, Session session, Object value)
		{
			m_name = name;
			m_session = session;
			m_value = value;
		}

		/**
		 * @inheritDoc
		 */
		public String getName()
		{
			return m_name;
		}

		/**
		 * @inheritDoc
		 */
		public Session getSession()
		{
			return m_session;
		}

		/**
		 * @inheritDoc
		 */
		public Object getValue()
		{
			return m_value;
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Entity: ToolSession, ContextSession (and even HttpSession)
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class MyLittleSession implements ToolSession, ContextSession, HttpSession
	{
		/** Hold attributes in a Map. TODO: ConcurrentHashMap may be better for multiple writers */
		protected Map m_attributes = new ConcurrentHashMap();

		/** The creation time of the session. */
		protected long m_created = 0;

		/** The session id. */
		protected String m_id = null;

		/** The tool placement / context id. */
		protected String m_littleId = null;

		/** The sakai session in which I live. */
		protected Session m_session = null;

		/** Time last accessed (via getSession()). */
		protected long m_accessed = 0;

		public MyLittleSession(Session s, String id)
		{
			m_id = idManager().createUuid();
			m_created = System.currentTimeMillis();
			m_accessed = m_created;
			m_littleId = id;
			m_session = s;
		}

		/**
		 * @inheritDoc
		 */
		public Object getAttribute(String name)
		{
			return m_attributes.get(name);
		}

		/**
		 * @inheritDoc
		 */
		public Enumeration getAttributeNames()
		{
			return new IteratorEnumeration(m_attributes.keySet().iterator());
		}

		/**
		 * @inheritDoc
		 */
		public long getCreationTime()
		{
			return m_created;
		}

		/**
		 * @inheritDoc
		 */
		public String getId()
		{
			return m_id;
		}

		/**
		 * @inheritDoc
		 */
		public long getLastAccessedTime()
		{
			return m_accessed;
		}

		/**
		 * @inheritDoc
		 */
		public String getPlacementId()
		{
			return m_littleId;
		}

		/**
		 * @inheritDoc
		 */
		public String getContextId()
		{
			return m_littleId;
		}

		/**
		 * @inheritDoc
		 */
		public void clearAttributes()
		{
			// move the attributes to a local map in a synchronized block so the unbinding happens only on one thread
			Map unbindMap = null;
			synchronized (this)
			{
				unbindMap = new HashMap(m_attributes);
				m_attributes.clear();
			}

			// send unbind events
			for (Iterator i = unbindMap.entrySet().iterator(); i.hasNext();)
			{
				Map.Entry e = (Map.Entry) i.next();
				String name = (String) e.getKey();
				Object value = e.getValue();
				unBind(name, value);
			}
		}

		/**
		 * Mark the session as just accessed.
		 */
		protected void setAccessed()
		{
			m_accessed = System.currentTimeMillis();
		}

		/**
		 * @inheritDoc
		 */
		public void removeAttribute(String name)
		{
			// remove
			Object value = m_attributes.remove(name);

			// unbind event
			unBind(name, value);
		}

		/**
		 * @inheritDoc
		 */
		public void setAttribute(String name, Object value)
		{
			// treat a set to null as a remove
			if (value == null)
			{
				removeAttribute(name);
			}

			else
			{
				// add
				Object old = m_attributes.put(name, value);

				// bind event
				bind(name, value);

				// unbind event if old exiss
				if (old != null)
				{
					unBind(name, old);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof ToolSession))
			{
				return false;
			}

			return ((ToolSession) obj).getId().equals(getId());
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode()
		{
			return getId().hashCode();
		}

		/**
		 * Unbind the value if it's a SessionBindingListener. Also does the HTTP unbinding if it's a HttpSessionBindingListener.
		 * 
		 * @param name
		 *        The attribute name bound.
		 * @param value
		 *        The bond value.
		 */
		protected void unBind(String name, Object value)
		{
			if (value instanceof SessionBindingListener)
			{
				SessionBindingEvent event = new MySessionBindingEvent(name, null, value);
				((SessionBindingListener) value).valueUnbound(event);
			}

			// also unbind any objects that are regular HttpSessionBindingListeners
			if (value instanceof HttpSessionBindingListener)
			{
				HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
				((HttpSessionBindingListener) value).valueUnbound(event);
			}
		}

		/**
		 * Bind the value if it's a SessionBindingListener. Also does the HTTP binding if it's a HttpSessionBindingListener.
		 * 
		 * @param name
		 *        The attribute name bound.
		 * @param value
		 *        The bond value.
		 */
		protected void bind(String name, Object value)
		{
			if (value instanceof SessionBindingListener)
			{
				SessionBindingEvent event = new MySessionBindingEvent(name, m_session, value);
				((SessionBindingListener) value).valueBound(event);
			}

			if (value instanceof HttpSessionBindingListener)
			{
				HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
				((HttpSessionBindingListener) value).valueBound(event);
			}
		}

		/**
		 * @inheritDoc
		 */
		public String getUserEid()
		{
			return m_session.getUserEid();
		}

		/**
		 * @inheritDoc
		 */
		public String getUserId()
		{
			return m_session.getUserId();
		}

		/**
		 * @inheritDoc
		 */
		public ServletContext getServletContext()
		{
			return (ServletContext) threadLocalManager().get(CURRENT_SERVLET_CONTEXT);
		}

		/**
		 * @inheritDoc
		 */
		public void setMaxInactiveInterval(int arg0)
		{
			// TODO: just ignore this ?
		}

		/**
		 * @inheritDoc
		 */
		public int getMaxInactiveInterval()
		{
			return m_session.getMaxInactiveInterval();
		}

		/**
		 * @inheritDoc
		 */
		public HttpSessionContext getSessionContext()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @inheritDoc
		 */
		public Object getValue(String arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @inheritDoc
		 */
		public String[] getValueNames()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @inheritDoc
		 */
		public void putValue(String arg0, Object arg1)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @inheritDoc
		 */
		public void removeValue(String arg0)
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @inheritDoc
		 */
		public void invalidate()
		{
			clearAttributes();
			// TODO: cause to go away?
		}

		/**
		 * @inheritDoc
		 */
		public boolean isNew()
		{
			return false;
		}
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
					for (Iterator i = m_sessions.values().iterator(); i.hasNext();)
					{
						MySession s = (MySession) i.next();
						if (M_log.isDebugEnabled()) M_log.debug("checking session " + s.getId());
						if (s.isInactive())
						{
							if (M_log.isDebugEnabled()) M_log.debug("invalidating session " + s.getId());
							s.invalidate();
						}
					}
				}
				catch (Throwable e)
				{
					M_log.warn("run(): exception: " + e);
				}
				finally
				{
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
}
