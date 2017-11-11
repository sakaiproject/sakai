/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.cheftool;

import java.io.Serializable;

/**
 * <p>
 * Alert is a set of messages intended for user display in the user interface.
 * </p>
 */
public class AlertImpl implements org.sakaiproject.cheftool.api.Alert, Serializable
{

	private static final long serialVersionUID = 1L;
	
	/** The Alert text. */
	protected String m_msg = null;

	/**
	 * Add a new alert line. A line separator will be appended as needed.
	 * 
	 * @param alert
	 *        The alert message to add.
	 */
	public void add(String alert)
	{
		// if this is the first, just set it
		if (m_msg == null)
		{
			m_msg = alert;
		}

		// otherwise append it with a line break
		else
		{
			m_msg = "\n" + alert;
		}
	}

	/**
	 * Access the alert message. Once accessed, the message is cleared.
	 * 
	 * @return The alert message.
	 */
	public String getAlert()
	{
		String tmp = m_msg;
		m_msg = null;

		return tmp;
	}

	/**
	 * Access the alert message, but unlike getAlert(), do not clear the message.
	 * 
	 * @return The alert message.
	 */
	public String peekAlert()
	{
		return m_msg;
	}

	/**
	 * Check to see if the alert is empty, or has been populated.
	 * 
	 * @return true of the alert is empty, false if there have been alerts set.
	 */
	public boolean isEmpty()
	{
		return ((m_msg == null) || (m_msg.length() == 0));
	}

	/**
	 * Remove any messages in the Alert.
	 */
	public void clear()
	{
		m_msg = null;
	}
}
