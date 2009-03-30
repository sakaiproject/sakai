/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.commons.lang.mutable.MutableLong;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ContextSession;
import org.sakaiproject.tool.api.NonPortableSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionAttributeListener;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.SessionStore;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.IteratorEnumeration;


/*************************************************************************************************************************************************
 * Entity: Session Also is an HttpSession
 ************************************************************************************************************************************************/

public class MySession implements Session, HttpSession, Serializable
{
	/**
	 * Value that identifies the version of this class that has been Serialized.
	 * 1 = original definition
	 * 2 = added expirationTimeSuggestion
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * SessionManager
	 */
	private transient SessionManager sessionManager;

	private transient SessionStore sessionStore;
	
	private transient ThreadLocalManager threadLocalManager; 

	private transient IdManager idManager;
	
	private transient boolean TERRACOTTA_CLUSTER;
	
	private transient NonPortableSession m_nonPortalSession;
	
	private transient SessionAttributeListener sessionListener;

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
	
	/**
	 * The possible time this Session may be inactive and available for expiration.
	 * This value is an optimization for Terracotta clustered environments, to avoid
	 * faulting object in, unless we have a best guess that it may be out of date.
	 * We also choose not to use the m_accessed field directly, to avoid updating the
	 * SHARED (on every box) data structure, except every inactive/2 period.
	 */
	protected final MutableLong expirationTimeSuggestion;

	/** Seconds of inactive time before being automatically invalidated - 0 turns off this feature. */
	protected int m_inactiveInterval;

	/** The user id for this session. */
	protected String m_userId = null;

	/** The user enterprise id for this session. */
	protected String m_userEid = null;

	/** True while the session is valid. */
	protected boolean m_valid = true;

	public MySession(SessionManager sessionManager, String id, ThreadLocalManager threadLocalManager,
			IdManager idManager, SessionStore sessionStore, SessionAttributeListener sessionListener,
			int inactiveInterval, NonPortableSession nonPortableSession, MutableLong expirationTimeSuggestion)
	{
		this.sessionManager = sessionManager;
		m_id = id;
		this.threadLocalManager = threadLocalManager;
		this.idManager = idManager;
		this.sessionStore = sessionStore;
		this.sessionListener = sessionListener;
		m_inactiveInterval = inactiveInterval;
		m_nonPortalSession = nonPortableSession;
		m_created = System.currentTimeMillis();
		m_accessed = m_created;
		this.expirationTimeSuggestion = expirationTimeSuggestion;
		resetExpirationTimeSuggestion();
		// set the TERRACOTTA_CLUSTER flag
		resolveTerracottaClusterProperty();
	}

	protected void resolveTransientFields()
	{
		// These are spelled out instead of using imports, to be explicit
		org.sakaiproject.component.api.ComponentManager compMgr = 
			org.sakaiproject.component.cover.ComponentManager.getInstance();
		
		sessionManager = (SessionManager)compMgr.get(org.sakaiproject.tool.api.SessionManager.class);

		sessionStore = (SessionStore)compMgr.get(org.sakaiproject.tool.api.SessionStore.class);
		
		threadLocalManager = (ThreadLocalManager)compMgr.get(org.sakaiproject.thread_local.api.ThreadLocalManager.class); 

		idManager = (IdManager)compMgr.get(org.sakaiproject.id.api.IdManager.class);
		
		// set the TERRACOTTA_CLUSTER flag
		resolveTerracottaClusterProperty();
		
		m_nonPortalSession = new MyNonPortableSession();
		
		sessionListener = (SessionAttributeListener)compMgr.get(org.sakaiproject.tool.api.SessionBindingListener.class);
	}
	
	protected void resolveTerracottaClusterProperty() 
	{
		String clusterTerracotta = System.getProperty("sakai.cluster.terracotta");
		TERRACOTTA_CLUSTER = "true".equals(clusterTerracotta);
	}

	/**
	 * @inheritDoc
	 */
	public Object getAttribute(String name)
	{
		Object target = m_attributes.get(name);
		if ((target == null) && (m_nonPortalSession != null)) {
			target = m_nonPortalSession.getAttribute(name);
		}
		return target;
	}

	/**
	 * @inheritDoc
	 */
	public Enumeration getAttributeNames()
	{
		Set<String> nonPortableAttributeNames = m_nonPortalSession.getAllAttributes().keySet();
		IteratorChain ic = new IteratorChain(m_attributes.keySet().iterator(),nonPortableAttributeNames.iterator());
		return new IteratorEnumeration(ic);
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

		synchronized (this)
		{	
			clear();
	
			// let it not be found
			sessionStore.remove(getId());
		}


		// if this is the current session, remove it
		if (this.equals(this.sessionManager.getCurrentSession()))
		{
			this.sessionManager.setCurrentSession(null);
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
		Map<String,Object> nonPortableMap = null;
		synchronized (this)
		{
			unbindMap = new HashMap(m_attributes);
			m_attributes.clear();

			toolMap = new HashMap(m_toolSessions);
			m_toolSessions.clear();

			contextMap = new HashMap(m_contextSessions);
			m_contextSessions.clear();
			
			nonPortableMap = m_nonPortalSession.getAllAttributes();
			m_nonPortalSession.clear();
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

		// send unbind events for normal (possibly clustered) session data
		for (Iterator i = unbindMap.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry e = (Map.Entry) i.next();
			String name = (String) e.getKey();
			Object value = e.getValue();
			unBind(name, value);
		}

		// send unbind events for non clustered session data (in a clustered environment)
		for (Map.Entry<String, Object> e: nonPortableMap.entrySet())
		{
			unBind(e.getKey(), e.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearExcept(Collection names)
	{
		// save any attributes in names
		Map saveAttributes = new HashMap();
		Map<String,Object> saveNonPortableAttributes = new HashMap<String,Object>();
		for (Iterator i = names.iterator(); i.hasNext();)
		{
			String name = (String) i.next();
			Object value = m_attributes.get(name);
			if (value != null)
			{
				// remove, but do NOT unbind
				m_attributes.remove(name);
				saveAttributes.put(name, value);
			} else {
				value = m_nonPortalSession.removeAttribute(name);
				if (value != null)
				{
					saveNonPortableAttributes.put(name, value);
				}
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

		for(Map.Entry<String,Object> e: saveNonPortableAttributes.entrySet())
		{
			m_nonPortalSession.setAttribute(e.getKey(), e.getValue());
		}
	}

	/**
	 * @inheritDoc
	 */
	public void setActive()
	{
		m_accessed = System.currentTimeMillis();
		updateExpirationTimeSuggestion();
	}
	
	protected void updateExpirationTimeSuggestion()
	{
		long now = System.currentTimeMillis();
		long diff = expirationTimeSuggestion.longValue() - now;
		long max = getMaxInactiveIntervalMillis();
		if (diff < (max/2))
		{
			resetExpirationTimeSuggestion();
		}
	}
	
	protected void resetExpirationTimeSuggestion()
	{
		expirationTimeSuggestion.setValue(System.currentTimeMillis() + getMaxInactiveIntervalMillis());
	}
	
	protected long getMaxInactiveIntervalMillis()
	{
		return getMaxInactiveInterval() * 1000L;
	}

	/**
	 * @inheritDoc
	 */
	public void removeAttribute(String name)
	{
		// remove
		Object value = m_attributes.remove(name);
		
		if ((value == null) && (m_nonPortalSession != null))
		{
			value = m_nonPortalSession.removeAttribute(name);
		}

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
			Object old = null;
			
			// If this is not a terracotta clustered environment then immediately
			// place the attribute in the normal data structure
			// Otherwise, if this *IS* a TERRACOTTA_CLUSTER, then check the current
			// tool id against the tool whitelist, to see if attributes from this
			// tool should be clustered, or not.
			if ((!TERRACOTTA_CLUSTER) || (sessionStore.isCurrentToolClusterable()))
			{
				old = m_attributes.put(name, value);
			}
			else
			{
				old = m_nonPortalSession.setAttribute(name, value);
			}

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
			NonPortableSession nPS = new MyNonPortableSession();
			t = new MyLittleSession(sessionManager,idManager.createUuid(),this,placementId,
					threadLocalManager, sessionListener,sessionStore,nPS);
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
			NonPortableSession nPS = new MyNonPortableSession();
			t = new MyLittleSession(sessionManager, idManager.createUuid(),this,contextId,
					threadLocalManager,sessionListener,sessionStore,nPS);
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
		return (ServletContext) threadLocalManager.get(SessionComponent.CURRENT_SERVLET_CONTEXT);
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
		
		// Added for testing purposes. Very much unsure whether this is a proper
		// use of MySessionBindingEvent.
		if ( sessionListener != null ) {
			sessionListener.attributeRemoved(new MySessionBindingEvent(name, this, value));
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
		
		// Added for testing purposes. Very much unsure whether this is a proper
		// use of MySessionBindingEvent.
		if ( sessionListener != null ) {
			sessionListener.attributeAdded(new MySessionBindingEvent(name, this, value));
		}

	}
}
