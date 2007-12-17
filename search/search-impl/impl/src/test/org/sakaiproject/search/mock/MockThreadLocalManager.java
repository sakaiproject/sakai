/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.search.mock;

import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 *
 */
public class MockThreadLocalManager implements ThreadLocalManager
{

	/**
	 * 
	 */
	public MockThreadLocalManager()
	{
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.thread_local.api.ThreadLocalManager#clear()
	 */
	public void clear()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.thread_local.api.ThreadLocalManager#get(java.lang.String)
	 */
	public Object get(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.thread_local.api.ThreadLocalManager#set(java.lang.String, java.lang.Object)
	 */
	public void set(String name, Object value)
	{
		// TODO Auto-generated method stub

	}

}
