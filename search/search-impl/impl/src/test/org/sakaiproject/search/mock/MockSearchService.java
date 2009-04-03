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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.mock;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.api.TermFrequency;
import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * @author ieb
 */
public class MockSearchService implements SearchService
{

	private DataSource datasource;

	private static final Log log = LogFactory.getLog(MockSearchService.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#forceReload()
	 */
	public void forceReload()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getAllSearchItems()
	 */
	public List getAllSearchItems()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getGlobalMasterSearchItems()
	 */
	public List getGlobalMasterSearchItems()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getNDocs()
	 */
	public int getNDocs()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getPendingDocs()
	 */
	public int getPendingDocs()
	{
		int pendingDocs = 0;
		Connection connection = null;
		PreparedStatement countPST = null;
		ResultSet rs = null;
		try
		{
			connection = datasource.getConnection();
			countPST = connection
					.prepareStatement("select count(*) from searchbuilderitem  where searchstate = ? ");

			countPST.clearParameters();
			countPST.setLong(1, SearchBuilderItem.STATE_PENDING);
			rs = countPST.executeQuery();
			if (rs.next())
			{
				pendingDocs = rs.getInt(1);
			}
			connection.commit();
		}
		catch (Exception ex)
		{
			log.error("Failed to get pending docs ", ex);
		}
		finally
		{
			try
			{
				rs.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
			try
			{
				countPST.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
			try
			{
				connection.close();
			}
			catch (Exception ex2)
			{
				log.debug(ex2);
			}
		}
		return pendingDocs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getSearchStatus()
	 */
	public SearchStatus getSearchStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getSegmentInfo()
	 */
	public List getSegmentInfo()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getSiteMasterSearchItems()
	 */
	public List getSiteMasterSearchItems()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getStatus()
	 */
	public String getStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#getTerms(int)
	 */
	public TermFrequency getTerms(int documentId) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#rebuildInstance()
	 */
	public void rebuildInstance()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#rebuildSite(java.lang.String)
	 */
	public void rebuildSite(String currentSiteId)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#refreshInstance()
	 */
	public void refreshInstance()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#refreshSite(java.lang.String)
	 */
	public void refreshSite(String currentSiteId)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#registerFunction(java.lang.String)
	 */
	public void registerFunction(String function)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#reload()
	 */
	public void reload()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#removeWorkerLock()
	 */
	public boolean removeWorkerLock()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#search(java.lang.String,
	 *      java.util.List, int, int)
	 */
	public SearchList search(String searchTerms, List contexts, int searchStart,
			int searchEnd)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#search(java.lang.String,
	 *      java.util.List, int, int, java.lang.String, java.lang.String)
	 */
	public SearchList search(String searchTerms, List contexts, int start, int end,
			String filterName, String sorterName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.SearchService#searchXML(java.util.Map)
	 */
	public String searchXML(Map parameterMap)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#disableDiagnostics()
	 */
	public void disableDiagnostics()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#enableDiagnostics()
	 */
	public void enableDiagnostics()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.api.Diagnosable#hasDiagnostics()
	 */
	public boolean hasDiagnostics()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the datasource
	 */
	public DataSource getDatasource()
	{
		return datasource;
	}

	/**
	 * @param datasource
	 *        the datasource to set
	 */
	public void setDatasource(DataSource datasource)
	{
		this.datasource = datasource;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.SearchService#isEnabled()
	 */
	public boolean isEnabled()
	{
		return true;
	}

}
