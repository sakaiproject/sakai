package org.sakaiproject.search.dao;

import java.io.IOException;

import org.sakaiproject.search.api.SearchIndexBuilderWorker;


public interface SearchIndexBuilderWorkerDao
{

	/**
	 * This method processes the list of document modifications in the list
	 * @param worker 
	 * 
	 * @param runtimeToDo
	 * @throws IOException
	 * @throws HibernateException
	 */
	void processToDoListTransaction(SearchIndexBuilderWorker worker);

}