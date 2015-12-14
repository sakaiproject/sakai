/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.component;

import java.util.Properties;
import java.util.Set;

import org.sakaiproject.component.api.ComponentManager;

/**
 * @author ieb
 */
public class ComponentManagerBean implements ComponentManager
{
	/**
	 * @return
	 */
	private ComponentManager getComponentManager()
	{
		return org.sakaiproject.component.cover.ComponentManager.getInstance();
	}

	public void init()
	{

	}

	public void destroy()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#close()
	 */
	public void close()
	{
		getComponentManager().close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#contains(java.lang.Class)
	 */
	public boolean contains(Class iface)
	{
		return getComponentManager().contains(iface);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#contains(java.lang.String)
	 */
	public boolean contains(String ifaceName)
	{
		return getComponentManager().contains(ifaceName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#get(java.lang.Class)
	 */
	public <T> T get(Class<T> iface)
	{
		return getComponentManager().get(iface);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#get(java.lang.String)
	 */
	public Object get(String ifaceName)
	{
		return getComponentManager().contains(ifaceName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#getConfig()
	 */
	public Properties getConfig()
	{
		return getComponentManager().getConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#getRegisteredInterfaces()
	 */
	public Set getRegisteredInterfaces()
	{
		return getComponentManager().getRegisteredInterfaces();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#hasBeenClosed()
	 */
	public boolean hasBeenClosed()
	{
		return getComponentManager().hasBeenClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#loadComponent(java.lang.Class,
	 *      java.lang.Object)
	 */
	public void loadComponent(Class iface, Object component)
	{
		getComponentManager().loadComponent(iface, component);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#loadComponent(java.lang.String,
	 *      java.lang.Object)
	 */
	public void loadComponent(String ifaceName, Object component)
	{
		getComponentManager().loadComponent(ifaceName, component);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.component.api.ComponentManager#waitTillConfigured()
	 */
	public void waitTillConfigured()
	{
		getComponentManager().waitTillConfigured();
	}

}
