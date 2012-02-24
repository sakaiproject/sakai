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

package org.sakaiproject.search.model.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * @author ieb
 */
public class SearchBuilderItemImpl implements SearchBuilderItem
{
	private static final int OBJECT_VERSION = 1;

	private String id = null;

	private String name = null;

	private Integer searchaction = SearchBuilderItem.ACTION_UNKNOWN;

	private Integer searchstate = SearchBuilderItem.STATE_UNKNOWN;

	private Integer itemscope = SearchBuilderItem.ITEM;

	private Date version = null;

	private String context = null;

	private int lock = 0;

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


	public String toString()
	{

		String action = "invalid";
		if (searchaction != null && searchaction.intValue() >= 0
				&& searchaction.intValue() < SearchBuilderItem.actions.length)
		{
			action = SearchBuilderItem.actions[searchaction.intValue()];
		}
		String state = "invalid:"+searchstate;
		if (searchstate != null && searchstate.intValue() >= 0
				&& searchstate.intValue() < SearchBuilderItem.states.length)
		{
			state = SearchBuilderItem.states[searchstate.intValue()];
		}
		String locked = " Not Locked";
		if ( isLocked() ) {
			locked = " Locked:"+getLock();
		}

		return "Action:" + action + " State:" + state + locked +" Resource:" + name;
	}

	/**
	 * @return the itemscope
	 */
	public Integer getItemscope()
	{
		return itemscope;
	}

	/**
	 * @param itemscope
	 *        the itemscope to set
	 */
	public void setItemscope(Integer itemscope)
	{
		this.itemscope = itemscope;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.search.model.SearchBuilderItem#output(java.io.DataOutputStream)
	 */
	public void output(DataOutputStream dataOutputStream) throws IOException
	{
		dataOutputStream.writeInt(OBJECT_VERSION);
		dataOutputStream.writeInt(searchaction);
		dataOutputStream.writeInt(searchstate);
		dataOutputStream.writeInt(itemscope);
		dataOutputStream.writeLong(version.getTime());
		dataOutputStream.writeUTF(id);
		dataOutputStream.writeUTF(name);
		dataOutputStream.writeUTF(context);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.model.SearchBuilderItem#input(java.io.DataInputStream)
	 */
	public void input(DataInputStream dataInputStream) throws IOException
	{
		int objectVersion = dataInputStream.readInt();
		switch( objectVersion) {
			case 1:
				searchaction = dataInputStream.readInt();
				searchstate = dataInputStream.readInt();
				itemscope = dataInputStream.readInt();
				version = new Date(dataInputStream.readLong());
				id = dataInputStream.readUTF();
				name = dataInputStream.readUTF();
				context = dataInputStream.readUTF();
				break;
			default:
				throw new RuntimeException("Unknown object Version while loading SearchBuiderItem");
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.model.SearchBuilderItem#getLock()
	 */
	public int getLock()
	{
		if ( lock == 0 && isLocked() ) {
			return searchstate;
		}
		return lock ;
	}

	/**
	 * @param nodeLock
	 */
	public void setLock(int nodeLock)
	{
		this.lock = nodeLock;
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.model.SearchBuilderItem#isLocked()
	 */
	public boolean isLocked()
	{
		return (this.lock > 0 || 
				this.searchstate >= SearchBuilderItem.states.length ||
				this.searchstate < 0);
	}




}
