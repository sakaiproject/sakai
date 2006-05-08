/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.search.model.impl;

import java.util.Date;

import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * @author ieb
 */
public class SearchBuilderItemImpl implements SearchBuilderItem
{
	private String id = null;

	private String name = null;

	private Integer searchaction = SearchBuilderItem.ACTION_UNKNOWN;

	private Integer searchstate = SearchBuilderItem.STATE_UNKNOWN;

	private Date version = null;
	
	private String context = null;

	/**
	 * @return Returns the action.
	 */
	public Integer getSearchaction()
	{
		return searchaction;
	}

	/**
	 * @param action
	 *        The action to set.
	 */
	public void setSearchaction(Integer searchaction)
	{
		this.searchaction = searchaction;
	}

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
	 * @return Returns the version.
	 */
	public Date getVersion()
	{
		return version;
	}

	/**
	 * @param version
	 *        The version to set.
	 */
	public void setVersion(Date version)
	{
		this.version = version;
	}

	/**
	 * @return Returns the state.
	 */
	public Integer getSearchstate()
	{
		return searchstate;
	}

	/**
	 * @param state
	 *        The state to set.
	 */
	public void setSearchstate(Integer searchstate)
	{
		this.searchstate = searchstate;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *        The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	public String getContext()
	{
		return context;
	}

	public void setContext(String context)
	{
		this.context = context;
		
	}
	
}
