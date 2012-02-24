/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * ComponentMap exposes the registered components as a map - the component id is mapped to the component implementation.
 * </p>
 */
public class ComponentMap implements Map
{
	/**
	 * @inheritDoc
	 */
	public int size()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isEmpty()
	{
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsKey(Object arg0)
	{
		return org.sakaiproject.component.cover.ComponentManager.contains((String) arg0);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsValue(Object arg0)
	{
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public Object get(Object arg0)
	{
		return org.sakaiproject.component.cover.ComponentManager.get((String) arg0);
	}

	/**
	 * @inheritDoc
	 */
	public Object put(Object arg0, Object arg1)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Object remove(Object arg0)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public void putAll(Map arg0)
	{
	}

	/**
	 * @inheritDoc
	 */
	public void clear()
	{
	}

	/**
	 * @inheritDoc
	 */
	public Set keySet()
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Collection values()
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Set entrySet()
	{
		return null;
	}
}
