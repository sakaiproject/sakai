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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.api;

import java.util.Iterator;
import java.util.List;

/**
 * @author ieb
 */
public interface SearchList extends List<SearchResult>
{

	/**
	 * get an Iterator starting at
	 * 
	 * @param startAt
	 * @return
	 */
	Iterator<SearchResult> iterator(int startAt);

	/**
	 * 
	 * @return Get the full size of all the search results
	 */
	int getFullSize();

	/**
	 * @return the start of the list
	 */
	int getStart();


}
