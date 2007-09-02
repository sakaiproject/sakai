/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.indexer.impl;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.uuid.UUID;
import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.indexer.api.IndexWorker;
import org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * @author ieb
 */
public class ConcurrentSearchIndexBuilderWorkerImpl implements Runnable, IndexWorkerDocumentListener
{

	private static final Log log = LogFactory.getLog(ConcurrentSearchIndexBuilderWorkerImpl.class);

	private boolean enabled = false;

	/**
	 * dependency
	 */
	private SessionManager sessionManager;

	/**
	 * dependency
	 */
	private UserDirectoryService userDirectoryService;

	/**
	 * dependency
	 */
	private SearchService searchService;

	/**
	 * dependency
	 */
	private IndexWorker indexWorker;
	
	/**
	 * dependency
	 */
	private ServerConfigurationService serverConfigurationService;

	/**
	 * dependency
	 */
	private ComponentManager componentManager;
	
	/**
	 * we need to watch local events to guage activity 
	 */
	private EventTrackingService eventTrackingService;

	/**
	 * Setting
	 * The maximum sleep time for the wait/notify semaphore
	 */
	public long sleepTime = 5L * 60000L;

	/**
	 * Setting
	 * A load factor 1 is full load, 100 is normal The load factor controlls the
	 * backoff of the indexer threads. If the load Factor is high, the search
	 * threads back off more.
	 */
	private long loadFactor = 1000L;

	/**
	 * Setting
	 * if true, the  indexer will perform a soak test operation
	 */
	private boolean  soakTest = false;

	/**
	 * The number of threads per instance
	 */
	private final int numThreads = 2;

	/**
	 * The currently running index Builder thread
	 */
	private Thread indexBuilderThread[] = new Thread[numThreads];

	/**
	 * Has been started once at least
	 */
	private boolean started = false;

	/**
	 * threads should run
	 */
	private boolean runThreads = false;

	/**
	 * The Node ID associated with the thread
	 */
	private ThreadLocal<String> nodeIDHolder = new ThreadLocal<String>();

	/**
	 * An ID generator
	 */
	private IdentifierGenerator idgenerator = new VersionFourGenerator();

	/**
	 * A list of node ID's
	 */
	private Map<String, String> nodeIDList = new ConcurrentHashMap<String, String>();

	/**
	 * The last time an index run was performed on this node
	 */
	private long lastIndexRun = System.currentTimeMillis();

	/**
	 * The time the last event was seen
	 */
	private long lastEvent;

	private ThreadLocal<String> nowIndexing = new ThreadLocal<String>();






	public void init()
	{
		if (started && !runThreads)
		{
			log.warn("JVM Shutdown in progress, will not startup");
			return;
		}
		if (org.sakaiproject.component.cover.ComponentManager.hasBeenClosed())
		{
			log.warn("Component manager Shutdown in progress, will not startup");
			return;
		}
		started = true;
		runThreads = true;

		enabled = "true".equals(serverConfigurationService.getString("search.enable",
				"false"));

		enabled = enabled
				& "true".equals(serverConfigurationService.getString("search.indexbuild",
						"true"));
		
		eventTrackingService.addLocalObserver(new Observer(){

			public void update(Observable arg0, Object arg1)
			{
				lastEvent = System.currentTimeMillis();
			}
			
		});
		
		
		try
		{
			log.debug("init start");
			for (int i = 0; i < indexBuilderThread.length; i++)
			{
				indexBuilderThread[i] = new Thread(this);
				indexBuilderThread[i].setName("SearchBuilder_" + String.valueOf(i));
				indexBuilderThread[i].start();
			}

			/*
			 * Capture shutdown
			 */
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Thread#run()
				 */
				@Override
				public void run()
				{
					runThreads = false;
				}
			});

		}
		catch (Throwable t)
		{
			log.error("Failed to init ", t);
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		if (!enabled) return;
		nowIndexing.set("-");

		int threadno = -1;
		Thread tt = Thread.currentThread();
		for (int i = 0; i < indexBuilderThread.length; i++)
		{
			if (indexBuilderThread[i] == tt)
			{
				threadno = i;
			}
		}

		String nodeID = getNodeID();

		org.sakaiproject.component.cover.ComponentManager.waitTillConfigured();

		try
		{

			while (runThreads)
			{
				log.debug("Run Processing Thread");
				boolean locked = false;
				org.sakaiproject.tool.api.Session s = null;
				if (s == null)
				{
					s = sessionManager.startSession();
					User u = userDirectoryService.getUser("admin");
					s.setUserId(u.getId());
				}

				while (runThreads)
				{
					sessionManager.setCurrentSession(s);
					int totalDocs = searchService.getPendingDocs();
					long now = System.currentTimeMillis();
					long interval = now - lastEvent;
					boolean process = false;
					boolean createIndex = false;

					if (totalDocs > 200)
					{
						loadFactor = 10L;
					}
					else
					{
						loadFactor = 1000L;
					}
					if (totalDocs == 0)
					{
						process = false;
					}
					else if (totalDocs < 20 && interval > (20 * loadFactor))
					{
						process = true;
					}
					else if (totalDocs >= 20 && totalDocs < 50
							&& interval > (10 * loadFactor))
					{
						process = true;
					}
					else if (totalDocs >= 50 && totalDocs < 90
							&& interval > (5 * loadFactor))
					{
						process = true;
					}
					else if (totalDocs > ((90 * loadFactor) / 1000))
					{
						process = true;
					}

					// should this node consider taking the lock ?
					long lastIndexInterval = (System.currentTimeMillis() - lastIndexRun);
					long lastIndexMetric = lastIndexInterval * totalDocs;

					// if we have 1000 docs, then indexing should happen
					// after 10 seconds break
					// 1000*10000 10000000
					// 500 docs/ 20 seconds
					//

					log.debug("Activity " + (lastIndexMetric > (10000L * loadFactor))
							+ " " + (lastIndexInterval > (60L * loadFactor)) + " "
							+ createIndex);

					if (lastIndexMetric > (10000L * loadFactor)
							|| lastIndexInterval > (60L * loadFactor))
					{
						log.debug("===" + process + "=============PROCESSING ");
						if (process)
						{

							lastIndexRun = System.currentTimeMillis();

							int batchSize = 100;
							if (totalDocs > 500)
							{
								batchSize = 200;
							}
							else if (totalDocs > 1000)
							{
								batchSize = 500;
							}
							else if (totalDocs > 10000)
							{
								batchSize = 1000;
							}
							indexWorker.process(batchSize);

							lastIndexRun = System.currentTimeMillis();

						}
						else
						{
							// too few documents to process
							break;
						}
					}
					else
					{
						// too much activity on this node
						break;
					}
				}
				if (!runThreads)
				{
					break;
				}
				try
				{
					log.debug("Sleeping Processing Thread");
					Thread.sleep(sleepTime);
					log.debug("Wakey Wakey Processing Thread");
					
					if (componentManager.hasBeenClosed())
					{
						runThreads = false;
						break;
					}

					if ( soakTest && (searchService.getPendingDocs() == 0))
					{
						log
								.error("SOAK TEST---SOAK TEST---SOAK TEST. Index Rebuild Started");
						searchService.rebuildInstance();
					}
				}
				catch (InterruptedException e)
				{
					log.debug(" Exit From sleep " + e.getMessage());
					break;
				}
				
			}
		}
		catch (Throwable t)
		{

			log.warn(
					"Failed in IndexBuilder when indexing document: " + nowIndexing,
					t);
		}
		finally
		{

			log.debug("IndexBuilder run exit " + tt.getName());
			if (threadno != -1)
			{
				indexBuilderThread[threadno] = null;
			}
		}
	}

	/**
	 * Get a unique node and attach it to the thread, this used to be used for
	 * performing locks on the node, but this is not really required any more,
	 * however it probably needs to be there for other purposes.
	 * 
	 * @return
	 */
	private String getNodeID()
	{
		String nodeID = (String) nodeIDHolder.get();
		if (nodeID == null)
		{
			UUID uuid = (UUID) idgenerator.nextIdentifier();
			nodeID = uuid.toString();
			nodeIDHolder.set(nodeID);
			if (nodeIDList.get(nodeID) == null)
			{
				nodeIDList.put(nodeID, nodeID);
			}
			else
			{
				log.error("============NODE ID " + nodeID
						+ " has already been issued, there must be a clash");
			}
		}
		return nodeID;
	}


	/**
	 * @return the soakTest
	 */
	public boolean getSoakTest()
	{
		return soakTest;
	}

	/**
	 * Puts the index builder into a Soak test, when there are no pending items,
	 * it starts building again.
	 * 
	 * @param soakTest
	 *        the soakTest to set
	 */
	public void setSoakTest(boolean soakTest)
	{

		this.soakTest = soakTest;
		if (soakTest)
		{
			log.warn("SOAK TEST ACTIVE ======================DONT USE FOR PRODUCTION ");
		}
	}

	
	/** 
	 * @see org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener#indexDocumentEnd(org.sakaiproject.search.indexer.api.IndexWorker, java.lang.String)
	 */
	public void indexDocumentEnd(IndexWorker worker, String ref)
	{
		nowIndexing.set("-");
	}

	/**
	 * @see org.sakaiproject.search.indexer.api.IndexWorkerDocumentListener#indexDocumentStart(org.sakaiproject.search.indexer.api.IndexWorker, java.lang.String)
	 */
	public void indexDocumentStart(IndexWorker worker, String ref)
	{
		nowIndexing.set(ref);
		
	}

}
