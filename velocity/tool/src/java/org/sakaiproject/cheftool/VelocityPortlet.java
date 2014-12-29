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

public class VelocityPortlet
{
	protected PortletConfig m_config = null;

	protected String m_id = null;

	public VelocityPortlet(String id, PortletConfig config)
	{
		m_id = id;
		m_config = config;
	}

	public String getID()
	{
		return m_id;
	}

	public PortletConfig getServletConfig()
	{
		return m_config;
	}

	public PortletConfig getPortletConfig()
	{
		return m_config;
	}

	public void setAttribute(String name, String value, RunData data)
	{
	}
}
