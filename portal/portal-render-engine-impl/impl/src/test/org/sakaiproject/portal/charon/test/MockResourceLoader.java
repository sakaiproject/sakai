/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.portal.charon.test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created on 28 Aug 2007 Antranig Basman
 */
public class MockResourceLoader implements Map
{
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key)
	{
		return true;
	}

	public boolean containsValue(Object value)
	{
		throw new UnsupportedOperationException();
	}

	public Set entrySet()
	{
		throw new UnsupportedOperationException();
	}

	public Object get(Object key)
	{
		return "Message for key " + key;
	}

	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	public Set keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Object put(Object arg0, Object arg1)
	{
		throw new UnsupportedOperationException();
	}

	public void putAll(Map arg0)
	{
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public int size()
	{
		throw new UnsupportedOperationException();
	}

	public Collection values()
	{
		throw new UnsupportedOperationException();
	}
}
