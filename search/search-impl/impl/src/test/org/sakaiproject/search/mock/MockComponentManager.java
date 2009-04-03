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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.mock;

import java.util.Properties;
import java.util.Set;

import org.sakaiproject.component.api.ComponentManager;

/**
 * @author ieb
 *
 */
public class MockComponentManager implements ComponentManager
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#close()
	 */
	public void close()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#contains(java.lang.Class)
	 */
	public boolean contains(Class iface)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#contains(java.lang.String)
	 */
	public boolean contains(String ifaceName)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#get(java.lang.Class)
	 */
	public Object get(Class iface)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#get(java.lang.String)
	 */
	public Object get(String ifaceName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#getConfig()
	 */
	public Properties getConfig()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#getRegisteredInterfaces()
	 */
	public Set getRegisteredInterfaces()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#hasBeenClosed()
	 */
	public boolean hasBeenClosed()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#loadComponent(java.lang.Class, java.lang.Object)
	 */
	public void loadComponent(Class iface, Object component)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#loadComponent(java.lang.String, java.lang.Object)
	 */
	public void loadComponent(String ifaceName, Object component)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ComponentManager#waitTillConfigured()
	 */
	public void waitTillConfigured()
	{
		// TODO Auto-generated method stub

	}

}
