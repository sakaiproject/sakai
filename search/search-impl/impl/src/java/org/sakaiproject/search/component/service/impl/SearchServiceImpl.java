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

package org.sakaiproject.search.component.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.search.api.SearchIndexBuilderWorker;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.component.Messages;
import org.sakaiproject.search.model.SearchWriterLock;

/**
 * The search service
 * 
 * @author ieb
 */
public class SearchServiceImpl extends BaseSearchServiceImpl
{

	private static Log log = LogFactory.getLog(SearchServiceImpl.class);

	/**
	 * The index builder dependency
	 */
	private SearchIndexBuilderWorker searchIndexBuilderWorker;

	private long reloadStart;

	private long reloadEnd;

	

	/**
	 * Register a notification action to listen to events and modify the search
	 * index
	 */
	@Override
	public void init()
	{

		super.init();

		try
		{

			try
			{
				if (autoDdl)
				{
					SqlService.getInstance().ddl(this.getClass().getClassLoader(),
							"sakai_search");
				}
			}
			catch (Exception ex)
			{
				log.error("Perform additional SQL setup", ex);
			}

			initComplete = true;


		}
		catch (Throwable t)
		{
			log.error("Failed to start ", t); //$NON-NLS-1$
		}

	}



	@Override
	public String getStatus()
	{

		String lastLoad = (new Date(reloadEnd)).toString();
		String loadTime = String.valueOf((double) (0.001 * (reloadEnd - reloadStart)));

		return Messages.getString("SearchServiceImpl.40") + lastLoad + Messages.getString("SearchServiceImpl.38") + loadTime + Messages.getString("SearchServiceImpl.37"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public SearchStatus getSearchStatus()
	{
		String ll = Messages.getString("SearchServiceImpl.36"); //$NON-NLS-1$
		String lt = ""; //$NON-NLS-1$
		if (reloadEnd != 0)
		{
			ll = (new Date(reloadEnd)).toString();
			lt = String.valueOf((double) (0.001 * (reloadEnd - reloadStart)));
		}
		final String lastLoad = ll;
		final String loadTime = lt;
		final SearchWriterLock lock = searchIndexBuilderWorker.getCurrentLock();
		final List<SearchWriterLock> lockNodes = searchIndexBuilderWorker.getNodeStatus();
		final String pdocs = String.valueOf(getPendingDocs());
		final String ndocs = String.valueOf(getNDocs());

		return new SearchStatus()
		{
			public String getLastLoad()
			{
				return lastLoad;
			}

			public String getLoadTime()
			{
				return loadTime;
			}

			public String getCurrentWorker()
			{
				return lock.getNodename();
			}

			public String getCurrentWorkerETC()
			{
				if (SecurityService.isSuperUser())
				{
					return MessageFormat.format(Messages
							.getString("SearchServiceImpl.35"), //$NON-NLS-1$
							new Object[] { lock.getExpires(),
									searchIndexBuilderWorker.getLastDocument(),
									searchIndexBuilderWorker.getLastElapsed(),
									searchIndexBuilderWorker.getCurrentDocument(),
									searchIndexBuilderWorker.getCurrentElapsed(),
									ServerConfigurationService.getServerIdInstance() });
				}
				else
				{
					return MessageFormat.format(Messages
							.getString("SearchServiceImpl.39"), new Object[] { lock //$NON-NLS-1$
							.getExpires() });
				}
			}

			public List<Object[]> getWorkerNodes()
			{
				List<Object[]> l = new ArrayList<Object[]>();
				for (Iterator<SearchWriterLock> i = lockNodes.iterator(); i.hasNext();)
				{
					SearchWriterLock swl = (SearchWriterLock) i.next();
					Object[] result = new Object[3];
					result[0] = swl.getNodename();
					result[1] = swl.getExpires();
					if (lock.getNodename().equals(swl.getNodename()))
					{
						result[2] = Messages.getString("SearchServiceImpl.47"); //$NON-NLS-1$
					}
					else
					{
						result[2] = Messages.getString("SearchServiceImpl.48"); //$NON-NLS-1$
					}
					l.add(result);
				}
				return l;
			}

			public String getNDocuments()
			{
				return ndocs;
			}

			public String getPDocuments()
			{
				return pdocs;
			}

		};

	}

	@Override
	public boolean removeWorkerLock()
	{
		return searchIndexBuilderWorker.removeWorkerLock();

	}





	/**
	 * @return the searchIndexBuilderWorker
	 */
	public SearchIndexBuilderWorker getSearchIndexBuilderWorker()
	{
		return searchIndexBuilderWorker;
	}

	/**
	 * @param searchIndexBuilderWorker the searchIndexBuilderWorker to set
	 */
	public void setSearchIndexBuilderWorker(SearchIndexBuilderWorker searchIndexBuilderWorker)
	{
		this.searchIndexBuilderWorker = searchIndexBuilderWorker;
	}




	

}
