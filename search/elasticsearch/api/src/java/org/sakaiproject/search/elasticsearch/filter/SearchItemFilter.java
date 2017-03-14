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

package org.sakaiproject.search.elasticsearch.filter;

import org.sakaiproject.search.api.SearchResult;

/**
 * Provides a mechnism whereby a search item can be filtered from the list,
 * This is used to post process the list,
 * It should not be confused with the filters that pre-process the list during the search 
 * operation.
 * 
 * @author ieb
 */
public interface SearchItemFilter
{
	/**
	 * performs the filter operation on a result, retruning the filtered result
	 * 
	 * @param result
	 * @return
	 */
	SearchResult filter(SearchResult result);

}
