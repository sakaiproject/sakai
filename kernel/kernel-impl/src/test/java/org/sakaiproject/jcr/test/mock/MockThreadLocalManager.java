/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.test.mock;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class MockThreadLocalManager implements ThreadLocalManager
{
	ThreadLocal<Map<String, Object>> m = new ThreadLocal<Map<String, Object>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.thread_local.api.ThreadLocalManager#clear()
	 */
	public void clear()
	{
		getTlm().clear();

	}

	/**
	 * @return
	 */
	private Map<String, Object> getTlm()
	{
		Map<String, Object> tlm = m.get();
		if (tlm == null)
		{
			tlm = new HashMap<String, Object>();
			m.set(tlm);
		}
		return tlm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.thread_local.api.ThreadLocalManager#get(java.lang.String)
	 */
	public Object get(String name)
	{
		return getTlm().get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.thread_local.api.ThreadLocalManager#set(java.lang.String,
	 *      java.lang.Object)
	 */
	public void set(String name, Object value)
	{
		getTlm().put(name, value);

	}

}
