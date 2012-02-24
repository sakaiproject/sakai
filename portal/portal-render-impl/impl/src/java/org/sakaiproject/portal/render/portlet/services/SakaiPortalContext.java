/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.render.portlet.services;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * @author ddwolf
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public class SakaiPortalContext implements PortalContext
{

	private ArrayList modes;

	private ArrayList states;

	private Map properties;

	public SakaiPortalContext()
	{
		properties = new HashMap();
		modes = new ArrayList();
		states = new ArrayList();

		modes.add(PortletMode.VIEW);
		modes.add(PortletMode.HELP);
		modes.add(PortletMode.EDIT);

		states.add(WindowState.MAXIMIZED);
		states.add(WindowState.MINIMIZED);
		states.add(WindowState.NORMAL);
	}

	public SakaiPortalContext(Map properties)
	{
		this.properties = properties;
	}

	public String getProperty(String key)
	{
		return (String) properties.get(key);
	}

	public Enumeration getPropertyNames()
	{
		return new IteratorEnumeration(properties.keySet().iterator());
	}

	public Enumeration getSupportedPortletModes()
	{
		return new IteratorEnumeration(modes.iterator());
	}

	public Enumeration getSupportedWindowStates()
	{
		return new IteratorEnumeration(states.iterator());
	}

	/**
	 * @todo Dynamic
	 * @return
	 */
	public String getPortalInfo()
	{

		return "Sakai-Charon/" + ServerConfigurationService.getString("version.sakai");
	}

	class IteratorEnumeration implements Enumeration
	{

		private Iterator iterator;

		public IteratorEnumeration(Iterator iterator)
		{
			this.iterator = iterator;
		}

		public boolean hasMoreElements()
		{
			return iterator.hasNext();
		}

		public Object nextElement()
		{
			return iterator.next();
		}
	}
}
