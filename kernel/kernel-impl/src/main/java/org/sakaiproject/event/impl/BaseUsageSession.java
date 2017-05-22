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

package org.sakaiproject.event.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;

/*************************************************************************************************************************************************
 * UsageSession
 ************************************************************************************************************************************************/

public class BaseUsageSession implements UsageSession, SessionBindingListener
{
	/**
	 * UsageSessionService
	 */
	private transient UsageSessionServiceAdaptor usageSessionServiceAdaptor;

	/** The user id for this session. */
	protected String m_user = null;

	/** The unique id for this session. */
	protected String m_id = null;

	/** The server which is hosting the session. */
	protected String m_server = null;

	/** The IP Address from which this session originated. */
	protected String m_ip = null;

	/** The Hostname from which this session originated. */
	protected String m_hostname = null;

	/** The User Agent string describing the browser used in this session. */
	protected String m_userAgent = null;

	/** The BrowserID string describing the browser used in this session. */
	protected String m_browserId = null;

	/** The time the session was started */
	protected Time m_start = null;

	/** The time the session was closed. */
	protected Time m_end = null;

	/** Flag for active session */
	protected boolean m_active = false;

	/**
	 * Construct fully from persisted data.
	 *
	 * @param result SQL result set containing:
	 *        The session id,
	 *        The server id which is hosting the session,
	 *        The user id for this session,
	 *        The IP Address from which this session originated,
	 *        The User Agent string describing the browser used in this session,
	 *        True if the session is open; null if it's closed.
	 * @param usageSessionServiceAdaptor TODO
	 */
	public BaseUsageSession(UsageSessionServiceAdaptor usageSessionServiceAdaptor, ResultSet result) throws SQLException
	{
		this.usageSessionServiceAdaptor = usageSessionServiceAdaptor;
		m_id = result.getString(1);
		m_server = result.getString(2);
		m_user = result.getString(3);
		m_ip = result.getString(4);
		m_hostname = result.getString(5);
		m_userAgent = result.getString(6);
		if(result.getString(7) != null && this.usageSessionServiceAdaptor != null && this.usageSessionServiceAdaptor.timeService() != null && this.usageSessionServiceAdaptor.sqlService() != null){
			m_start = this.usageSessionServiceAdaptor.timeService().newTime(result.getTimestamp(7, this.usageSessionServiceAdaptor.sqlService().getCal()).getTime());
		}
		if(result.getString(8) != null && this.usageSessionServiceAdaptor != null && this.usageSessionServiceAdaptor.timeService() != null && this.usageSessionServiceAdaptor.sqlService() != null){
			m_end = this.usageSessionServiceAdaptor.timeService().newTime(result.getTimestamp(8, this.usageSessionServiceAdaptor.sqlService().getCal()).getTime());
		}
		Boolean isActive = result.getBoolean(9);
		m_active = ((isActive != null) && isActive.booleanValue());
		setBrowserId(m_userAgent);
	}

	protected void resolveTransientFields()
	{
		// These are spelled out instead of using imports, to be explicit
		org.sakaiproject.component.api.ComponentManager compMgr = 
			org.sakaiproject.component.cover.ComponentManager.getInstance();
		
		usageSessionServiceAdaptor = (UsageSessionServiceAdaptor)compMgr.get("org.sakaiproject.event.api.UsageSessionService");
	}

	/**
	 * Construct new active session.
	 *
	 * @param id
	 *        The session id.
	 * @param server
	 *        The server id which is hosting the session.
	 * @param user
	 *        The user id for this session.
	 * @param address
	 *        The IP Address from which this session originated.
	 * @param agent
	 *        The User Agent string describing the browser used in this session.
	 * @param usageSessionServiceAdaptor TODO
	 */
	public BaseUsageSession(UsageSessionServiceAdaptor usageSessionServiceAdaptor, String id, String server, String user, String address, String hostname, String agent)
	{
		this.usageSessionServiceAdaptor = usageSessionServiceAdaptor;
		m_id = id;
		m_server = server;
		m_user = user;
		m_ip = address;
		m_hostname = hostname;
		m_userAgent = agent;
		m_start = this.usageSessionServiceAdaptor.timeService().newTime();
		m_end = m_start;
		m_active = true;
		setBrowserId(agent);
	}


	/**
	 * Set the browser id for this session, decoded from the user agent string.
	 *
	 * @param agent
	 *        The user agent string.
	 */
	protected void setBrowserId(String agent)
	{
		if (agent == null)
		{
			m_browserId = UNKNOWN;
		}

		// test whether agent is UserAgent value for a known browser.
		// should we also check version number?
		else if (agent.indexOf("Netscape") >= 0 && agent.indexOf("Mac") >= 0)
		{
			m_browserId = MAC_NN;
		}
		else if (agent.indexOf("Netscape") >= 0 && agent.indexOf("Windows") >= 0)
		{
			m_browserId = WIN_NN;
		}
		else if (agent.indexOf("MSIE") >= 0 && agent.indexOf("Mac") >= 0)
		{
			m_browserId = MAC_IE;
		}
		else if (agent.indexOf("MSIE") >= 0 && agent.indexOf("Windows") >= 0)
		{
			m_browserId = WIN_IE;
		}
		else if (agent.indexOf("Camino") >= 0 && agent.indexOf("Macintosh") >= 0)
		{
			m_browserId = MAC_CM;
		}
		else if (agent.startsWith("Mozilla") && agent.indexOf("Windows") >= 0)
		{
			m_browserId = WIN_MZ;
		}
		else if (agent.startsWith("Mozilla") && agent.indexOf("Macintosh") >= 0)
		{
			m_browserId = MAC_MZ;
		}
		else if (agent.startsWith("Mozilla") && agent.indexOf("Linux") >= 0)
		{
			m_browserId = LIN_MZ;
		}
		//KNL-1490 Android => Linux
		else if (agent.startsWith("Mozilla") && agent.indexOf("Android") >= 0)
		{
			m_browserId = LIN_MZ;
		}
		else if (agent.startsWith("Mozilla") && agent.indexOf("iPad") >= 0)
		{
			m_browserId = IOS_MZ;
		}
		else if (agent.startsWith("Mozilla") && agent.indexOf("iPhone") >= 0)
		{
			m_browserId = IOS_MZ;
		}
		else
		{
			m_browserId = UNKNOWN;
		}
	}

	/**
	 * Close the session.
	 */
	protected void close()
	{
		if (!isClosed())
		{
			m_end = this.usageSessionServiceAdaptor.timeService().newTime();
			m_active = false;
			this.usageSessionServiceAdaptor.m_storage.closeSession(this);
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean isClosed()
	{
		return !m_active;
	}

	/**
	 * @inheritDoc
	 */
	public String getUserId()
	{
		return m_user;
	}

	/**
	 * @inheritDoc
	 */
	public String getUserEid()
	{
		try
		{
			return this.usageSessionServiceAdaptor.userDirectoryService().getUserEid(m_user);
		}
		catch (UserNotDefinedException e)
		{
			return m_user;
		}
	}

	/**
	 * @inheritDoc
	 */
	public String getUserDisplayId()
	{
		try
		{
			User user = this.usageSessionServiceAdaptor.userDirectoryService().getUser(m_user);
			return user.getDisplayId();
		}
		catch (UserNotDefinedException e)
		{
			return m_user;
		}
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
	public String getServer()
	{
		return m_server;
	}

	/**
	 * @inheritDoc
	 */
	public void setServer(String serverId) {
		m_server = serverId;
		this.usageSessionServiceAdaptor.m_storage.updateSessionServer(this);
	}

	/**
	 * @inheritDoc
	 */
	public String getIpAddress()
	{
		return m_ip;
	}

	/**
	 * @inheritDoc
	 */
	public String getHostName()
	{
		return m_hostname;
	}

	/**
	 * @inheritDoc
	 */
	public String getUserAgent()
	{
		return m_userAgent;
	}

	/**
	 * @inheritDoc
	 */
	public String getBrowserId()
	{
		return m_browserId;
	}

	/**
	 * @inheritDoc
	 */
	public Time getStart()
	{
		return this.usageSessionServiceAdaptor.timeService().newTime(m_start.getTime());
	}

	/**
	 * @inheritDoc
	 */
	public Time getEnd()
	{
		return this.usageSessionServiceAdaptor.timeService().newTime(m_end.getTime());
	}

	/**
	 * There's new user activity now.
	 */
	protected void setActivity()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Has this session gone inactive?
	 *
	 * @return True if the session has seen no activity in the last timeout period, false if it's still active.
	 */
	protected boolean isInactive()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	public void valueBound(SessionBindingEvent sbe)
	{
	}

	/**
	 * @inheritDoc
	 */
	public void valueUnbound(SessionBindingEvent sbe)
	{
		invalidate();
	}
	
	/**
	 * Called when logging out, when timed out, and when being
	 * cleaned up after a server crash.
	 */
	public void invalidate()
	{
		// if we didn't close this already, close
		if (!isClosed())
		{
			// close the session
			close();

			// generate the logout event
			this.usageSessionServiceAdaptor.logoutEvent(this);
		}			
	}

	/**
	 * @inheritDoc
	 */
	public int compareTo(Object obj)
	{
		if (!(obj instanceof UsageSession)) throw new ClassCastException();

		// if the object are the same, say so
		if (obj == this) return 0;

		// start the compare by comparing their users
		int compare = getUserId().compareTo(((UsageSession) obj).getUserId());

		// if these are the same
		if (compare == 0)
		{
			// sort based on (unique) id
			compare = getId().compareTo(((UsageSession) obj).getId());
		}

		return compare;
	}

	/**
	 * @inheritDoc
	 */
	public String toString()
	{
		return "[" + ((m_id == null) ? "" : m_id) + " | " + ((m_server == null) ? "" : m_server) + " | " + ((m_user == null) ? "" : m_user)
				+ " | " + ((m_ip == null) ? "" : m_ip) + " | " + ((m_userAgent == null) ? "" : m_userAgent) + " | " + m_start.toStringGmtFull()
				+ " ]";
	}
}
