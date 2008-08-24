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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class ThreadLocalCache
{
	private static final Log log = LogFactory.getLog(ThreadLocalCache.class);

	private ThreadLocalManager threadLocalManager;

	private String name;

	private int hits = 0;

	private int miss = 0;

	private int remove = 0;

	private int add = 0;

	private boolean clean = true;
	
	

	/**
	 * @param id
	 * @param cc
	 */
	@SuppressWarnings("unchecked")
	public Object put(String id, Object cc)
	{
		
		if ( cc == null ) {
			return null;
		}
		Map<String, Object> ccc = (Map<String, Object>) threadLocalManager.get(name);
		if (ccc == null)
		{
			ccc = new HashMap<String, Object>();
			threadLocalManager.set(name, ccc);
		}
		id = cleanId(id);
		ccc.put(id, cc);
		add++;
		return cc;
	}

	/**
	 * @param id
	 * @return
	 */
	private String cleanId(String id)
	{
		if ( clean  &&  id != null && id.endsWith("/") ) {
			return id.substring(0,id.length()-1);
		}
		return id;
	}

	/**
	 * @param id
	 */
	@SuppressWarnings("unchecked")
	public void remove(String id)
	{
		Map<String, Object> ccc = (Map<String, Object>) threadLocalManager.get(name);
		if (ccc == null)
		{
			return;
		}
		remove++;
		id = cleanId(id);
		ccc.remove(id);
	}

	/**
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object get(String id)
	{
		Map<String, Object> ccc = (Map<String, Object>) threadLocalManager.get(name);
		if (ccc == null)
		{
			return null;
		}
		id = cleanId(id);
		Object o = ccc.get(id);
		if ( o == null ) {
			//log.info("Cache "+name+" Miss "+id);
			hits++;
		} else {
			//log.info("Cache "+name+" Hit "+id+" "+o);
			miss++;
		}
		return o;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *        the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the threadLocalManager
	 */
	public ThreadLocalManager getThreadLocalManager()
	{
		return threadLocalManager;
	}

	/**
	 * @param threadLocalManager
	 *        the threadLocalManager to set
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager)
	{
		this.threadLocalManager = threadLocalManager;
	}

	/**
	 * @return the clean
	 */
	public boolean isClean()
	{
		return clean;
	}

	/**
	 * @param clean the clean to set
	 */
	public void setClean(boolean clean)
	{
		this.clean = clean;
	}

}
