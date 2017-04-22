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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.apache.commons.lang.mutable.MutableLong;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ContextSession;
import org.sakaiproject.tool.api.NonPortableSession;
import org.sakaiproject.tool.api.RebuildBreakdownService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionAttributeListener;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.SessionStore;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;


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
	 * The possible time this Session may be inactive and available for expiration.
	 * This value is an optimization for Terracotta clustered environments, to avoid
	 * faulting object in, unless we have a best guess that it may be out of date.
	 * We also choose not to use the m_accessed field directly, to avoid updating the
	 * SHARED (on every box) data structure, except every inactive/2 period.
	 */
	protected final MutableLong expirationTimeSuggestion;
	/** Hold attributes in a Map. */
	protected Map<String, Object> m_attributes = new ConcurrentHashMap<String, Object>();
	/** Hold toolSessions in a Map, by placement id. */
	protected Map<String, MyLittleSession> m_toolSessions = new ConcurrentHashMap<String, MyLittleSession>();
	/** Hold context toolSessions in a Map, by context (webapp) id. */
	protected Map<String, MyLittleSession> m_contextSessions = new ConcurrentHashMap<String, MyLittleSession>();
	/** The creation time of the session. */
	protected long m_created = 0;
	/** The session id. */
	protected String m_id = null;
	/** Time last accessed (via getSession()). */
	protected long m_accessed = 0;
	/** Seconds of inactive time before being automatically invalidated - 0 turns off this feature. */
	protected int m_inactiveInterval;
	/** The user id for this session. */
	protected String m_userId = null;
	/** The user enterprise id for this session. */
	protected String m_userEid = null;
	/** True while the session is valid. */
	protected boolean m_valid = true;
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
	private transient RebuildBreakdownService rebuildBreakdownService;
    public MySession(SessionManager sessionManager, String id, ThreadLocalManager threadLocalManager,
					 IdManager idManager, SessionStore sessionStore, SessionAttributeListener sessionListener,
					 int inactiveInterval, NonPortableSession nonPortableSession, MutableLong expirationTimeSuggestion,
					 RebuildBreakdownService rebuildBreakdownService)
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
		this.rebuildBreakdownService = rebuildBreakdownService;
	}

    /**
     * @return true if the session is valid OR false otherwise
     */
    public boolean isValid() {
        return m_valid;
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
	public void setMaxInactiveInterval(int interval)
	{
		m_inactiveInterval = interval;
		resetExpirationTimeSuggestion(); // added for KNL-1088
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
	public void setUserEid(String eid)
	{
		m_userEid = eid;
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
	public void setUserId(String uid)
	{
		m_userId = uid;
	}

	/**
	 * @inheritDoc
	 */
	public void invalidate()
	{
		String sessionId = getId();
		destroy();
		// ensure that the session cache is cleared when session is invalidated
		if (rebuildBreakdownService != null) {
		    rebuildBreakdownService.purgeSessionFromStorageById(sessionId);
		}
	}

    /**
     * TESTING ONLY
     * Special method to destroy a session without purging the sessions storage data
     */
    public void destroy() {
        m_valid = false;
        String sessionId = getId();
        synchronized (this) {
            clear();
            sessionStore.remove(sessionId);
        }
        // if this is the current session, remove it
        if (this.equals(this.sessionManager.getCurrentSession())) {
            this.sessionManager.setCurrentSession(null);
        }
    }

    /**
     * @return the current request associated with this Session
     */
    public HttpServletRequest currentRequest() {
        HttpServletRequest req = (HttpServletRequest) this.threadLocalManager.get(RequestFilter.CURRENT_HTTP_REQUEST);
        return req;
    }

    /**
     * @return info about the map attributes for debugging purposes mostly
     */
    public Map<String, String> currentAttributesSummary() {
        LinkedHashMap<String,String> data = new LinkedHashMap<String, String>();
        data.put("ID",this.m_id);
        data.put("userId",this.m_userId);
        data.put("userEid",this.m_userEid);
        data.put("created",this.m_created+"");
        data.put("accessed",this.m_accessed+"");
        data.put("valid",this.m_valid+"");
        for (Map.Entry<String, Object> entry : ((Map<String,Object>) m_attributes).entrySet()) {
            data.put(entry.getKey(), convertValueToString(entry.getValue()));
        }
        for (Iterator i = m_toolSessions.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            ToolSession s = (ToolSession) e.getValue();
            String sKey = "TS_"+s.getId()+"_";
            data.put(sKey+"placementId", s.getPlacementId());
            Enumeration<String> keys = s.getAttributeNames();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                Object val = s.getAttribute(key);
                data.put(sKey+key, convertValueToString(val));
            }
        }
        for (Iterator i = m_contextSessions.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            ContextSession s = (ContextSession) e.getValue();
            String sKey = "CS_"+s.getId()+"_";
            data.put(sKey+"contextId", s.getContextId());
            Enumeration<String> keys = s.getAttributeNames();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                Object val = s.getAttribute(key);
                data.put(sKey+key, convertValueToString(val));
            }
        }
        return data;
    }

    private String convertValueToString(Object value) {
        if (value == null) {
            return "NULL";
        }
        try {
            if (value.getClass().isPrimitive() || value instanceof String || value instanceof Number || value instanceof Boolean) {
                return value.toString();
            } else if (value.getClass().isArray()) {
                return value.getClass().getCanonicalName()+"("+Array.getLength(value)+")";
            } else if (value instanceof ResourceLoader) {
                // have to do this since the ResourceLoader looks like a map but doesn't have most of the methods implemented (like size)
                return value.toString();
            } else if (value instanceof UsageSession) {
                UsageSession us = (UsageSession) value;
                return value.getClass().getCanonicalName()+":("+us.getId()+")";
            } else if (value instanceof Collection) {
                Collection c = (Collection) value;
                return value.getClass().getCanonicalName()+"["+c.size()+"]";
            } else if (value instanceof Map) {
                Map m = (Map) value;
                return value.getClass().getCanonicalName()+"["+m.size()+"]";
            } else {
                return value.getClass().getCanonicalName();
            }
        } catch (Exception e) {
            return value.getClass().getCanonicalName()+":[e="+e.getMessage()+"]";
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
	public ToolSession getToolSession(String placementId)
	{
		MyLittleSession t = m_toolSessions.get(placementId);
		if (t == null)
		{
			NonPortableSession nPS = new MyNonPortableSession();
			t = new MyLittleSession(MyLittleSession.TYPE_TOOL, sessionManager,idManager.createUuid(),this,placementId,
					threadLocalManager, sessionListener,sessionStore,nPS);
			m_toolSessions.put(placementId, t);
			// try to populate the tool id and context when the session is created
			if (sessionStore instanceof SessionComponent) {
				String sakaiToolId = ((SessionComponent)sessionStore).identifyCurrentTool();
				t.setSessionToolId(sakaiToolId);
				String context = ((SessionComponent)sessionStore).identifyCurrentContext();
				t.setSessionContextId(context);
			}
		}

		// mark it as accessed
		t.setAccessed();

		return t;
	}

	/**
	 * @inheritDoc
	 */
	public ContextSession getContextSession(String contextId)
	{
        MyLittleSession t = m_contextSessions.get(contextId);
		if (t == null)
		{
			NonPortableSession nPS = new MyNonPortableSession();
			t = new MyLittleSession(MyLittleSession.TYPE_CONTEXT, sessionManager, idManager.createUuid(),this,contextId,
					threadLocalManager,sessionListener,sessionStore,nPS);
			m_contextSessions.put(contextId, t);
			t.setSessionContextId(contextId);
		}

		// mark it as accessed
		t.setAccessed();

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

    @Override
    public String toString() {
        return "MyS_"+m_userEid+"{" + m_id +
                       ", userId='" + m_userId + '\'' +
                       ", at=" + (m_attributes != null ? m_attributes.size() : 0) +
                       ", ts=" + (m_toolSessions != null ? m_toolSessions.size() : 0) +
                       ", cs=" + (m_contextSessions != null ? m_contextSessions.size() : 0) +
                       ", " + (m_created > 0 ? new Date(m_created) : "?") +
                       '}';
    }

}
