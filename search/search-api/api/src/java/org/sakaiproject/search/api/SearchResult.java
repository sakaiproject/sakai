/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.IOException;
import java.util.Map;

/**
 * @author ieb
 */
public interface SearchResult
{

	/**
	 * the result score
	 * 
	 * @return
	 */
	float getScore();

	/**
	 * The result ID (entity id)
	 * 
	 * @return
	 */
	String getId();

	/**
	 * All field names in the search record
	 * 
	 * @return
	 */
	String[] getFieldNames();

	/**
	 * All values in a search field
	 * 
	 * @param string
	 * @return
	 */
	String[] getValues(String string);

	/**
	 * Get a map of values in the result
	 * 
	 * @return
	 */
	Map<String, String[]> getValueMap();

	/**
	 * An absolute URL to the resource (no host, protocol or port)
	 * 
	 * @return
	 */
	String getUrl();

	/**
	 * The title of the resource, usually including the type (eg Wiki Page,
	 * Resource)
	 * 
	 * @return
	 */
	String getTitle();

	/**
	 * get the index of the search entry over the whole change set
	 * 
	 * @return
	 */
	int getIndex();

	/**
	 * get the search result content for display
	 */
	String getSearchResult();

	/**
	 * get the Sakai Entity Reference String
	 * @return
	 */
	String getReference();

	/**
	 * gets the term frequencies for this Document
	 * @return
	 * @throws IOException
	 */
	TermFrequency getTerms() throws IOException;

	/**
	 * get the tool name that this search cam from
	 * @return
	 */
	String getTool();
	
	/**
	 *  Has this item been censored by a filter?
	 * @return
	 */
	boolean isCensored();
	
	String getSiteId();

	void toXMLString(StringBuilder sb);
	
	
	/**
	 * Set the Url - needed for tols that generate portal urls
	 * @param newUrl
	 */
	void setUrl(String newUrl);
	
	/**
	 * This result has a ULR that displays within the portal
	 * @return
	 */
	boolean hasPortalUrl();
	

}
