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
package org.sakaiproject.search.model;

import java.util.Date;

/**
 * Represents an operation or stat of a document in the search engine. This
 * Object is used as a communication and persisntance mechanism between the
 * changes made to entities and the thread processing the indec updates
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
	 * A master record is used to override the indexer threa operation and avoid
	 * hude updates to the database in the request cycle.
	 */
	public static final String INDEX_MASTER = "_master_control";

	/**
	 * The action being performent
	 * 
	 * @return
	 */
	Integer getSearchaction();

	/**
	 * The action being performed
	 * 
	 * @param action
	 */
	void setSearchaction(Integer searchaction);

	/**
	 * Action Unknown, usually becuase the record has just been created
	 */
	public static final Integer ACTION_UNKNOWN = new Integer(0);

	/**
	 * Action ADD the record to the search engine, if the doc ID is set, then
	 * remove first, if not set, check its not there.
	 */
	public static final Integer ACTION_ADD = new Integer(1);

	/**
	 * Action DELETE the record from the search engine, once complete delete the
	 * record
	 */
	public static final Integer ACTION_DELETE = new Integer(2);

	/**
	 * The action REBUILD causes the indexer thread to rebuild the index from
	 * scratch, refetching all entities This sould only ever appear on the
	 * master record
	 */
	public static final Integer ACTION_REBUILD = new Integer(11);

	/**
	 * The action REFRESH causes the indexer thread to refresh the search index
	 * from the current set of entities. If a Rebuild is in progress, the
	 * refresh will not overrise the rebuild
	 */
	public static final Integer ACTION_REFRESH = new Integer(10);

	/**
	 * The state of the record
	 * 
	 * @return
	 */
	Integer getSearchstate();

	/**
	 * The state of the record
	 * 
	 * @param state
	 */
	void setSearchstate(Integer searchstate);

	/**
	 * Unknown state
	 */
	public static final Integer STATE_UNKNOWN = new Integer(0);

	/**
	 * Operation pending
	 */
	public static final Integer STATE_PENDING = new Integer(1);

	/**
	 * Operation completed
	 */
	public static final Integer STATE_COMPLETED = new Integer(2);

	public static final Integer STATE_PENDING_2 = new Integer(3);

	/**
	 * The last update to the record
	 * 
	 * @return
	 */
	Date getVersion();

	/**
	 * The last update to the record
	 * 
	 * @param lastupdate
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
	
	public static final String GLOBAL_CONTEXT = "global";

	public static final String GLOBAL_MASTER = SearchBuilderItem.INDEX_MASTER+"_"+SearchBuilderItem.GLOBAL_CONTEXT;
	
	public static final String SITE_MASTER_FORMAT = SearchBuilderItem.INDEX_MASTER+"_{0}";

	public static final String SITE_MASTER_PATTERN = SearchBuilderItem.INDEX_MASTER+"_%";

	public static final String[] states = new String[] {
		"Unknown",
		"Pending",
		"Complete",
		"Pending2"
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

	public static final Integer STATE_LOCKED = new Integer(5);
	

}
