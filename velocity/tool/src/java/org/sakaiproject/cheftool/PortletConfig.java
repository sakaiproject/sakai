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

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletConfig;

import org.sakaiproject.tool.api.Placement;

/** Provides read access to tool parameters, which are (name, value) pairs.  
 * Write access is not allowed.  This provides
 * an unmodifiable Map that looks first in the tool configuration, then in 
 * the tool registration, then in the servlet configuration.  The values 
 * in the tool configuration may have been configured by the end-user,
 * the values in the tool registration are defaulted in the tool registration
 * file, and the values in the servlet configuration come from web.xml and
 * cannot be modified by the user.
 */
public class PortletConfig implements Map
{
	private ServletConfig m_servletCofig;
	private Properties m_toolConfig;
	private Properties m_toolReg;
	private Placement m_placement;
	
	/** Lazily instantiated map populated from the servlet config, the tool configuration,
	 * and the tool registration - not modifiable after creation.
	 */
	private Map m_map = null;

	PortletConfig(ServletConfig config, Properties toolConfig, Properties reg, Placement p)
	{
		m_servletCofig = config;
		m_toolConfig = toolConfig;
		m_toolReg = reg;
		m_placement = p;
	}

	public String getInitParameter(String name, String dflt)
	{
		String value = getInitParameter(name);

		if (value == null)
		{
			value = dflt;
		}

		return value;
	}

	public String getInitParameter(String name)
	{
		String value = null;

		// check the tool config if present
		if (m_toolConfig != null)
		{
			value = m_toolConfig.getProperty(name);
		}

		// check the registration if present and no value so far
		if (value == null)
		{
			if (m_toolReg != null)
			{
				value = m_toolReg.getProperty(name);
			}
		}

		// check the servlet config if no value so far
		if (value == null)
		{
			value = m_servletCofig.getInitParameter(name);
		}

		return value;
	}

	public Map getInitParameters()
	{
		return getMap();
	}
	
	/** Populate the Map, such that (name, value) pairs will be found in this order:
	 * <ol>
	 * <li>ToolConfiguration</li>
	 * <li>ToolRegistration</li>
	 * <li>ServletConfig</li>
	 * </ul>
	 */
	private synchronized Map getMap()
	{
		if (m_map != null) return m_map;
		
		Map map = new HashMap();

		// load up with ServletConfig parameters
		Enumeration e = m_servletCofig.getInitParameterNames();
		while (e.hasMoreElements())
		{
			String name = (String) e.nextElement();
			map.put(name, m_servletCofig.getInitParameter(name));
		}

		// add ToolRegistration
		if (m_toolReg != null)
		{
			for (e = m_toolReg.propertyNames(); e.hasMoreElements();)
			{
				String name = (String) e.nextElement();
				map.put(name, m_toolReg.getProperty(name));
			}
		}

		// add ToolConfiguration
		if (m_toolConfig != null)
		{
			for (Enumeration i = m_toolConfig.propertyNames(); i.hasMoreElements();)
			{
				String name = (String) i.nextElement();
				map.put(name, m_toolConfig.getProperty(name));
			}
		}

		// ensure that this Map won't get modified past this point
		m_map = Collections.unmodifiableMap(map);
		return m_map;
	}

	public String getTitle()
	{
		if (m_placement != null)
		{
			return m_placement.getTitle();
		}

		return "";
	}

	/**
	 * Special non-jetspeed imitation: get three possible init parameter values:
	 * [0] from servlet config
	 * [1] from tool registration
	 * [2] from tool config
	 * nulls if not present
	 */
	public String[] get3InitParameter(String name)
	{
		String[] value = new String[3];

		// check the tool config if present
		if (m_toolConfig != null)
		{
			value[2] = m_toolConfig.getProperty(name);
		}

		// check the registration if present
		if (m_toolReg != null)
		{
			value[1] = m_toolReg.getProperty(name);
		}

		// check the servlet config
		value[0] = m_servletCofig.getInitParameter(name);

		return value;
	}
	
	/** <b>Unsupported operation</b> */
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key)
	{
		return getMap().containsKey(key);
	}
	
	public boolean containsValue(Object value)
	{
		return getMap().containsValue(value);
	}
	
	public Set entrySet()
	{
		return getMap().entrySet();
	}
	
	public Object get(Object key)
	{
		return getMap().get(key);
	}
	
	public boolean isEmpty()
	{
		return getMap().isEmpty();
	}
	
	public Set keySet()
	{
		return getMap().keySet();
	}

	/** <b>Unsupported operation</b> */
	public Object put(Object key, Object value)
	{
		throw new UnsupportedOperationException();
	}
	
	/** <b>Unsupported operation</b> */
	public void putAll(Map t)
	{
		throw new UnsupportedOperationException();
	}
	
	/** <b>Unsupported operation</b> */
	public Object remove(Object key)
	{
		throw new UnsupportedOperationException();
	}
	
	public int size()
	{
		return getMap().size();
	}
	
	public Collection values()
	{
		return getMap().values();
	}
}



