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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
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

/**********************************************************************************************************************************************************************************************************************************************************
 * Entity: ToolSession, ContextSession (and even HttpSession)
 *********************************************************************************************************************************************************************************************************************************************************/

public class MyLittleSession implements ToolSession, ContextSession, HttpSession, Serializable
{
    public static final String TYPE_TOOL = "tool";
    /**
     * Identify this as a tool or context session concretely
     */
    protected String m_type = TYPE_TOOL;
    public static final String TYPE_CONTEXT = "context";
    /**
	 * Value that identifies the version of this class that has been Serialized.
	 */
	private static final long serialVersionUID = 1L;
	/** Hold attributes in a Map. TODO: ConcurrentHashMap may be better for multiple writers */
	protected Map m_attributes = new ConcurrentHashMap();
    /**
     * Contains the tool id related to this session if there is one
     */
    protected String m_tool_id = null;
    /**
     * Contains the context id related to this session if there is one
     */
    protected String m_context_id = null;
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
    /**
	 * SessionManager
	 */
	private transient SessionManager sessionManager;
	private transient SessionStore sessionStore;
	private transient ThreadLocalManager threadLocalManager;
	private transient boolean TERRACOTTA_CLUSTER;
	private transient NonPortableSession m_nonPortalSession;
	private transient SessionAttributeListener sessionListener;

	public MyLittleSession(String type, SessionManager sessionManager, String id, Session s, String littleId,
			ThreadLocalManager threadLocalManager, SessionAttributeListener sessionListener,
			SessionStore sessionStore, NonPortableSession nonPortableSession)
	{
		this.m_type = type;
		this.sessionManager = sessionManager;
		this.m_id = id;
		this.m_session = s;
		this.m_littleId = littleId;
		this.threadLocalManager = threadLocalManager;
		this.sessionStore = sessionStore;
		this.m_nonPortalSession = nonPortableSession;
		this.sessionListener = sessionListener;
		m_created = System.currentTimeMillis();
		m_accessed = m_created;
		String clusterTerracotta = System.getProperty("sakai.cluster.terracotta");
		TERRACOTTA_CLUSTER = "true".equals(clusterTerracotta);
	}

    /**
     * the session type of this session (tool OR context)
     */
    public String getSessionType() {
        return m_type;
    }

    /**
     * the tool id related to this session OR null if there is not one
     */
    public String getSessionToolId() {
        return m_tool_id;
    }

    /**
     * Set the current tool id
     * @param m_tool_id
     */
    public void setSessionToolId(String m_tool_id) {
        this.m_tool_id = m_tool_id;
    }

    /**
     * the tool id related to this session OR null if there is not one
     */
    public String getSessionContextId() {
        return m_context_id;
    }

    /**
     * Set the current context id
     * @param m_context_id
     */
    public void setSessionContextId(String m_context_id) {
        this.m_context_id = m_context_id;
    }

	protected void resolveTransientFields()
	{
		// These are spelled out instead of using imports, to be explicit
		org.sakaiproject.component.api.ComponentManager compMgr = 
			org.sakaiproject.component.cover.ComponentManager.getInstance();
		
		sessionManager = (SessionManager)compMgr.get(org.sakaiproject.tool.api.SessionManager.class);

		sessionStore = (SessionStore)compMgr.get(org.sakaiproject.tool.api.SessionStore.class);
		
		threadLocalManager = (ThreadLocalManager)compMgr.get(org.sakaiproject.thread_local.api.ThreadLocalManager.class); 
		
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
		IteratorChain ic = new IteratorChain(m_attributes.keySet().iterator(),m_nonPortalSession.getAllAttributes().keySet().iterator());
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
		Map<String,Object> nonPortableMap = null;

		synchronized (this)
		{
			unbindMap = new HashMap(m_attributes);
			m_attributes.clear();

			nonPortableMap = m_nonPortalSession.getAllAttributes();
			m_nonPortalSession.clear();
		}

		// send unbind events
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

			// allow the tool sessions to be more easily identified
			if (this.m_tool_id == null && TYPE_TOOL.equals(m_type) && sessionStore instanceof SessionComponent) {
				String toolId = ((SessionComponent)sessionStore).identifyCurrentTool();
				if (toolId != null) {
					this.m_tool_id = toolId;
				}
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
		
		// Added for testing purposes. Very much unsure whether this is a proper
		// use of MySessionBindingEvent.
		if ( sessionListener != null ) {
			sessionListener.attributeRemoved(new MySessionBindingEvent(name, m_session, value));
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
		
		// Added for testing purposes. Very much unsure whether this is a proper
		// use of MySessionBindingEvent.
		if ( sessionListener != null ) {
			sessionListener.attributeAdded(new MySessionBindingEvent(name, m_session, value));
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
		return (ServletContext) threadLocalManager.get(SessionComponent.CURRENT_SERVLET_CONTEXT);
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
	public void setMaxInactiveInterval(int arg0)
	{
		// TODO: just ignore this ?
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

    @Override
    public String toString() {
        return "MLS_"+m_type+(m_tool_id!=null?"("+m_tool_id+")":"")
                       +(m_context_id!=null?"["+m_context_id+"]":"") +
                       "{at=" + (m_attributes != null ? m_attributes.size() : 0) +
                       ", id="+m_id +
                       ", ctx='" + m_littleId + '\'' +
                       ", " + (m_created > 0 ? new Date(m_created) : "?") +
                       '}';
    }

}
