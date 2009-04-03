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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.mbeans;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.indexer.api.IndexWorker;
import org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener;
import org.sakaiproject.search.indexer.api.IndexWorkerListener;
import org.sakaiproject.search.journal.api.IndexListener;
import org.sakaiproject.search.journal.api.IndexStorageProvider;
import org.sakaiproject.search.journal.impl.IndexListenerCloser;
import org.sakaiproject.search.journal.impl.RefCountIndexSearcher;
import org.sakaiproject.search.journal.impl.RefCountMultiReader;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * @author ieb
 */
public class SearchServiceManagement extends NotificationBroadcasterSupport implements
		SearchServiceManagementMBean
{

	private static final Log log = LogFactory.getLog(SearchServiceManagement.class);

	private static final String MBEAN_COMPONENT_BASE = "Sakai:type=SearchService";

	private SearchService searchService;

	protected long notificationNo = 0;

	private IndexStorageProvider indexStorageProvider;

	private IndexWorker indexWorker;

	private IndexListenerCloser indexListenerCloser;

	private ThreadLocalManager threadLocalManager;

	private String name;

	public SearchServiceManagement()
	{
		name = "";
	}

	/**
	 * @param instanceName
	 */
	public SearchServiceManagement(String instanceName)
	{
		name = ",instance="+instanceName;
	}

	/**
	 * 
	 */
	public void init()
	{
		try
		{

			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

			final ObjectName searchServiceON = new ObjectName(MBEAN_COMPONENT_BASE+name);
			mbs.registerMBean(this, searchServiceON);

			indexStorageProvider.addIndexListener(new IndexListener()
			{

				public void doIndexReaderClose(IndexReader oldMultiReader)
						throws IOException
				{
					sendNotification(new Notification("index-reader-close",
							searchServiceON, notificationNo++, "Closed oldMultiReader" ));
				}

				public void doIndexReaderOpen(IndexReader newMultiReader)
				{
					sendNotification(new Notification("index-reader-open",
							searchServiceON, notificationNo++, "Opened newMultiReader"));
				}

				public void doIndexSearcherClose(IndexSearcher indexSearcher)
						throws IOException
				{
					sendNotification(new Notification("index-searcher-close",
							searchServiceON, notificationNo++, "Closed "
									+ indexSearcher.toString()));

				}

				public void doIndexSearcherOpen(IndexSearcher indexSearcher)
				{
					sendNotification(new Notification("index-searcher-open",
							searchServiceON, notificationNo++, "Opened "
									+ indexSearcher.toString()));
				}

			});

			indexWorker.addIndexWorkerDocumentListener(new IndexWorkerDocumentListener()
			{

				public void indexDocumentEnd(IndexWorker worker, String ref)
				{
					sendNotification(new Notification("index-document-start",
							searchServiceON, notificationNo++, "Doc Ref " + ref));
				}

				public void indexDocumentStart(IndexWorker worker, String ref)
				{
					sendNotification(new Notification("index-document-end",
							searchServiceON, notificationNo++, "Doc Ref " + ref));
				}

			});

			indexWorker.addIndexWorkerListener(new IndexWorkerListener()
			{

				public void indexWorkerEnd(IndexWorker worker)
				{
					sendNotification(new Notification("index-woker-start",
							searchServiceON, notificationNo++, "Worker " + worker));

				}

				public void indexWorkerStart(IndexWorker worker)
				{
					sendNotification(new Notification("index-woker-end", searchServiceON,
							notificationNo++, "Worker " + worker));
				}

			});

		}
		catch (Exception ex)
		{
			log.warn("Failed to register mbean for search service ", ex);

		}

	}

	public int getOpenIndexSearchers()
	{
		try
		{
			return RefCountIndexSearcher.getOpened();
		}
		finally
		{
			threadLocalManager.clear();
		}
	}

	public int getOpenMultiReaders()
	{
		try
		{
			return RefCountMultiReader.getOpened();
		}
		finally
		{
			threadLocalManager.clear();
		}
	}

	public int getNumberOfDocuments()
	{
		try
		{
			return searchService.getNDocs();
		}
		finally
		{
			threadLocalManager.clear();
		}
	}

	public int getNumberOfPendingDocuments()
	{
		try
		{
			return searchService.getPendingDocs();
		}
		finally
		{
			threadLocalManager.clear();
		}
	}

	public String getStatus()
	{
		try
		{
			return searchService.getStatus();
		}
		finally
		{
			threadLocalManager.clear();
		}
	}

	public int getNumberOfPendingOpenIndexes()
	{
		try
		{
			return indexListenerCloser.size();
		}
		finally
		{
			threadLocalManager.clear();
		}
	}

	public String[] getOpenIndexes()
	{
		try
		{
			return indexListenerCloser.getOpenIndexNames();
		}
		finally
		{
			threadLocalManager.clear();
		}
	}

	public String[] getSegments()
	{
		try
		{
			List<Object[]> segments = searchService.getSegmentInfo();
			String[] segmentInfo = new String[segments.size()];
			for (int i = 0; i < segmentInfo.length; i++)
			{
				StringBuilder sb = new StringBuilder();
				for (Object o : segments.get(i))
				{
					sb.append(o).append(" ");
				}
				segmentInfo[i] = sb.toString();
			}
			return segmentInfo;
		}
		finally
		{
			threadLocalManager.clear();
		}
	}

	/**
	 * @return the searchService
	 */
	public SearchService getSearchService()
	{
		return searchService;
	}

	/**
	 * @param searchService
	 *        the searchService to set
	 */
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	/**
	 * @return the journaledIndex
	 */
	public IndexStorageProvider getIndexStorageProvider()
	{
		return indexStorageProvider;
	}

	/**
	 * @param journaledIndex
	 *        the journaledIndex to set
	 */
	public void setIndexStorageProvider(IndexStorageProvider indexStorageProvider)
	{
		this.indexStorageProvider = indexStorageProvider;
	}

	/**
	 * @return the indexWorker
	 */
	public IndexWorker getIndexWorker()
	{
		return indexWorker;
	}

	/**
	 * @param indexWorker
	 *        the indexWorker to set
	 */
	public void setIndexWorker(IndexWorker indexWorker)
	{
		this.indexWorker = indexWorker;
	}

	/**
	 * @return the indexListenerCloser
	 */
	public IndexListenerCloser getIndexListenerCloser()
	{
		return indexListenerCloser;
	}

	/**
	 * @param indexListenerCloser
	 *        the indexListenerCloser to set
	 */
	public void setIndexListenerCloser(IndexListenerCloser indexListenerCloser)
	{
		this.indexListenerCloser = indexListenerCloser;
	}

	/**
	 * @return the threadLocalManager
	 */
	public ThreadLocalManager getThreadLocalManager()
	{
		return threadLocalManager;
	}

	/**
	 * @param threadLocalManager
	 *        the threadLocalManager to set
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager)
	{
		this.threadLocalManager = threadLocalManager;
	}

}
