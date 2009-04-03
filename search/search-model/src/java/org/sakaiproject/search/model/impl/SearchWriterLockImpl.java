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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.model.impl;

import java.util.Date;

import org.sakaiproject.search.model.SearchWriterLock;

/**
 * @author ieb
 */
public class SearchWriterLockImpl implements SearchWriterLock
{
	private String nodename = null;

	private Date expires = null;

	private String id = null;

	private String lockkey = null;
	

	/**
	 * @return Returns the id.
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id
	 *        The id to set.
	 */
	public void setId(String id)
	{
		this.id = id;
	}



	/**
	 * @return Returns the nodeName.
	 */
	public String getNodename()
	{
		return nodename;
	}

	/**
	 * @param nodeName
	 *        The nodeName to set.
	 */
	public void setNodename(String nodename)
	{
		this.nodename = nodename;
	}

	public void setLockkey(String lockkey)
	{
		this.lockkey = lockkey;

	}

	public String getLockkey()
	{
		return lockkey;
	}

	/**
	 * @return Returns the expires.
	 */
	public Date getExpires()
	{
		return expires;
	}

	/**
	 * @param expires The expires to set.
	 */
	public void setExpires(Date expires)
	{
		this.expires = expires;
	}


}
