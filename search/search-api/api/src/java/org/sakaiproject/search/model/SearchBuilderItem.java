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
package org.sakaiproject.search.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Represents an operation or state of a document in the search engine. This
 * Object is used as a communication and persistance mechanism between the
 * changes made to entities and the thread processing the index updates
 * 
 * @author ieb
 */
public interface SearchBuilderItem
{
	String getId();
	void setId(String id);
	/**
	 * Name of the resource in the search index
	 * 
	 * @return
	 */
	String getName();

	/**
	 * The name of the resource in the search index
	 * 
	 * @param name
	 */
	void setName(String name);

	/**
	 * A master record is used to override the indexer thread operation and avoid
	 * hide updates to the database in the request cycle.
	 */
	public static final String INDEX_MASTER = "_master_control";

	/**
	 * The action being performed
	 * 
	 * @return
	 */
	Integer getSearchaction();

	/**
	 * The action being performed
	 * 
	 * @param searchaction
	 */
	void setSearchaction(Integer searchaction);

	/**
	 * Action Unknown, usually because the record has just been created
	 */
	public static final Integer ACTION_UNKNOWN = Integer.valueOf(0);

	/**
	 * Action ADD the record to the search engine, if the doc ID is set, then
	 * remove first, if not set, check its not there.
	 */
	public static final Integer ACTION_ADD = Integer.valueOf(1);

	/**
	 * Action DELETE the record from the search engine, once complete delete the
	 * record
	 */
	public static final Integer ACTION_DELETE = Integer.valueOf(2);

	/**
	 * The action REBUILD causes the indexer thread to rebuild the index from
	 * scratch, re-fetching all entities This should only ever appear on the
	 * master record
	 */
	public static final Integer ACTION_REBUILD = Integer.valueOf(11);

	/**
	 * The action REFRESH causes the indexer thread to refresh the search index
	 * from the current set of entities. If a Rebuild is in progress, the
	 * refresh will not override the rebuild
	 */
	public static final Integer ACTION_REFRESH = Integer.valueOf(10);

	/**
	 * The state of the record
	 * 
	 * @return
	 */
	Integer getSearchstate();

	/**
	 * The state of the record
	 * 
	 * @param searchstate
	 */
	void setSearchstate(Integer searchstate);

	/**
	 * Unknown state
	 */
	public static final Integer STATE_UNKNOWN = Integer.valueOf(0);

	/**
	 * Operation pending
	 */
	public static final Integer STATE_PENDING = Integer.valueOf(1);

	/**
	 * Operation completed
	 */
	public static final Integer STATE_COMPLETED = Integer.valueOf(2);

	public static final Integer STATE_PENDING_2 = Integer.valueOf(3);
	
	/**
	 * Locked for processing
	 */
	public static final Integer STATE_LOCKED = Integer.valueOf(5);
	
	public static final Integer STATE_FAILED = Integer.valueOf(6);


	public static final Integer ITEM = Integer.valueOf(0);
	
	public static final Integer ITEM_GLOBAL_MASTER = Integer.valueOf(1);
	
	public static final Integer ITEM_SITE_MASTER = Integer.valueOf(2);

	/**
	 * The last update to the record
	 * 
	 * @return
	 */
	Date getVersion();

	/**
	 * The last update to the record
	 * 
	 * @param version
	 */
	void setVersion(Date version);
	
	/**
	 * The context of the index item
	 * @return
	 */
	String getContext();
	/**
	 * The context of the index item
	 * @param context
	 */
	void setContext(String context);
	
	/**
	 * @return the itemscope
	 */
	Integer getItemscope();

	/**
	 * @param itemscope
	 *        the itemscope to set
	 */
	void setItemscope(Integer itemscope);

	public static final String GLOBAL_CONTEXT = "global";

	public static final String GLOBAL_MASTER = SearchBuilderItem.INDEX_MASTER+"_"+SearchBuilderItem.GLOBAL_CONTEXT;
	
	public static final String SITE_MASTER_FORMAT = SearchBuilderItem.INDEX_MASTER+"_{0}";

	public static final String SITE_MASTER_PATTERN = SearchBuilderItem.INDEX_MASTER+"_%";

	public static final String[] states = new String[] {
		"Unknown",
		"Pending",
		"Complete",
		"Pending2",
		"-",
		"Locked",
		"Failed"
	};
	public static final String[] actions = new String[] {
		"Unknown",
		"Add",
		"Delete",
		"-",
		"-",
		"-",
		"-",
		"-",
		"-",
		"-",
		"Refresh",
		"Rebuild"
	};
	public static final String[] scope = new String[] {
		"Queue Item",
		"Global Master",
		"Site Master"
	};

	/**
	 * @param dataOutputStream
	 * @throws IOException
	 */
	void output(DataOutputStream dataOutputStream) throws IOException;
	/**
	 * @param dataInputStream
	 * @throws IOException
	 */
	void input(DataInputStream dataInputStream) throws IOException;
	/**
	 * The lock id on this object
	 * @return
	 */
	int getLock();
	/**
	 * @return
	 */
	boolean isLocked();


	

}
