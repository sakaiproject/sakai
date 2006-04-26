/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
