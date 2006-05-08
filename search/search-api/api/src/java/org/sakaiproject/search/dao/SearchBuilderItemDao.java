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

package org.sakaiproject.search.dao;

import java.util.List;

import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * @author ieb
 */
public interface SearchBuilderItemDao
{

	/**
	 * create a new item
	 * 
	 * @return
	 */
	SearchBuilderItem create();

	/**
	 * Update a single item
	 * 
	 * @param sb
	 */
	void update(SearchBuilderItem sb);

	/**
	 * Locate the resource entry
	 * 
	 * @param resourceName
	 * @return
	 */
	SearchBuilderItem findByName(String resourceName);

	/**
	 * count the number of entries pending
	 * 
	 * @return
	 */
	int countPending();

	List getAll();

	List getGlobalMasters();

	List getSiteMasters();

}
