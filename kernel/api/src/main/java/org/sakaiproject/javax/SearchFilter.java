/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.javax;

/**
 * <p>
 * SearchFilter is a paricular kind of Filter where the code using the filter
 * may choose do something other than retrieve all the objects sequentally and 
 * present them for acceptance.  When code is using a SearchFilter the 
 * code may decide to consult a search index to more efficiently retrieve 
 * results.  This also might result in objects returned by relevance 
 * order.  SearchFilter objects must implement the accept() method 
 * because the calling code may or may not know how to peer inside 
 * the particular objects being searched.  If the calling code 
 * has no optimisation for search it may revert to an approach of retrieving
 * all items and presenting them to the accept() method of a SearchFilter.
 * </p>
 */
public interface SearchFilter extends Filter
{
	/**
	 * Returns the search string for this filter.
	 * 
	 * @return the search string for this filter.
	 */
	String getSearchString();
}
