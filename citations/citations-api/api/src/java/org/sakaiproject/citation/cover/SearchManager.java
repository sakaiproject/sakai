/**
 * Copyright (c) 2003-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.citation.cover;

import org.sakaiproject.citation.util.api.SearchCancelException;
import org.sakaiproject.component.cover.ComponentManager;

public class SearchManager
{
	private static org.sakaiproject.citation.api.SearchManager m_instance;

	/**
	 * Access the component instance: special cover only method.
	 *
	 * @return the component instance.
	 */
	public static org.sakaiproject.citation.api.SearchManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.citation.api.SearchManager) ComponentManager
						.get(org.sakaiproject.citation.api.SearchManager.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.citation.api.SearchManager) ComponentManager
					.get(org.sakaiproject.citation.api.SearchManager.class);
		}
	}

	public static org.sakaiproject.citation.api.ActiveSearch doNextPage(org.sakaiproject.citation.api.ActiveSearch search) throws org.sakaiproject.citation.util.api.SearchException
	{
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.doNextPage(search);
	}

	public static org.sakaiproject.citation.api.ActiveSearch doPrevPage(org.sakaiproject.citation.api.ActiveSearch search) throws org.sakaiproject.citation.util.api.SearchException
	{
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.doPrevPage(search);
	}

	public static org.sakaiproject.citation.api.ActiveSearch doSearch(org.sakaiproject.citation.api.ActiveSearch search) throws org.sakaiproject.citation.util.api.SearchException, SearchCancelException
	{
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.doSearch(search);
	}

	public static org.sakaiproject.citation.api.SearchDatabaseHierarchy getSearchHierarchy() throws org.sakaiproject.citation.util.api.SearchException
	{
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.getSearchHierarchy();
	}

	public static org.sakaiproject.citation.api.ActiveSearch newSearch()
	{
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.newSearch();
	}

	/**
     * @param collection
     * @return
     */
    public static org.sakaiproject.citation.api.ActiveSearch newSearch(org.sakaiproject.citation.api.CitationCollection savedResults)
    {
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.newSearch(savedResults);
    }

    /**
     * @deprecated Replaced by {@link org.sakaiproject.citation.api.ActiveSearch#getDatabaseids()}
     */
    public static void setDatabaseIds( String[] databaseIds )
    {
      throw new RuntimeException("Not implemented. See org.sakaiproject.citation.api.ActiveSearch.getDatabaseids()");
    }

    public static String getGoogleScholarUrl(String resourceId)
    {
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.getGoogleScholarUrl(resourceId);
    }

    
    public static String getExternalSearchWindowName(String resourceId)
    {
        org.sakaiproject.citation.api.SearchManager instance = getInstance();
        if (instance ==null)
        {
           return null;
        }
        return instance.getExternalSearchWindowName(resourceId);
    }

    
    public static String getSaveciteUrl(String resourceId, String saveciteClientId) 
    {
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.getSaveciteUrl(resourceId, saveciteClientId);
    }
}
