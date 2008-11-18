package org.sakaiproject.tool.impl;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionBindingEvent;

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