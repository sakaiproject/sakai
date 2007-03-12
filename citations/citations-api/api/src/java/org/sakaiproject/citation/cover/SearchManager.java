package org.sakaiproject.citation.cover;

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

	public static org.sakaiproject.citation.api.ActiveSearch doNextPage(org.sakaiproject.citation.api.ActiveSearch search) throws org.sakaiproject.citation.util.SearchException
	{
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.doNextPage(search);
	}

	public static org.sakaiproject.citation.api.ActiveSearch doPrevPage(org.sakaiproject.citation.api.ActiveSearch search) throws org.sakaiproject.citation.util.SearchException
	{
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.doPrevPage(search);
	}

	public static org.sakaiproject.citation.api.ActiveSearch doSearch(org.sakaiproject.citation.api.ActiveSearch search) throws org.sakaiproject.citation.util.SearchException
	{
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance == null)
		{
			return null;
		}

		return instance.doSearch(search);
	}

	public static org.sakaiproject.citation.api.SearchDatabaseHierarchy getSearchHierarchy() throws org.sakaiproject.citation.util.SearchException
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
    
    public static void setDatabaseIds( String[] databaseIds )
    {
		org.sakaiproject.citation.api.SearchManager instance = getInstance();
		if(instance != null)
		{
			instance.setDatabaseIds( databaseIds );
		}
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
}
