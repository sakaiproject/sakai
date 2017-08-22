/**
 * Copyright (c) 2003-2008 The Apereo Foundation
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