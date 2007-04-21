/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.search.api;

import java.sql.SQLException;
import java.util.List;

import org.sakaiproject.search.model.SearchWriterLock;

public interface SearchIndexBuilderWorker extends Diagnosable
{


	/**
	 * Check running, and ping the thread if in a wait state
	 */
	void checkRunning();
	
	/**
	 * Should the thread be running
	 */
	boolean isRunning();
	/**
	 * get the current running Lock
	 * @return
	 */
	SearchWriterLock getCurrentLock();

	/**
	 * get a list of node status records
	 * @return
	 */
	List getNodeStatus();

	/**
	 * destroy the search index builder worker and release all resources
	 *
	 */
	void destroy();

	/**
	 * forge the removal of the worker lock
	 * @return
	 */
	boolean removeWorkerLock();

	/**
	 * Increment the activity counter
	 *
	 */
	void incrementActivity();

	/**
	 * get an indication of the current activity level
	 * @return
	 */
	int getActivity();

	/**
	 * reset the activity levels
	 *
	 */
	void resetActivity();

	/**
	 * Get the ms time of the last search add/remove event (excluding master events)
	 * @return
	 */
	long getLastEventTime();

	/**
	 * 
	 * @param lifetime
	 * @return
	 */
	boolean getLockTransaction(long lifetime);
	/**
	 * 
	 * @param nodeLifetime
	 * @param forceLock force the lock to be taken even if there are no items in the queue
	 * @return
	 */
	boolean getLockTransaction(long nodeLifetime, boolean forceLock);

	/**
	 * 
	 * @param l
	 * @throws SQLException
	 */
	void updateNodeLock(long l) throws SQLException;

	/**
	 * 
	 * @param l
	 */
	void setLastIndex(long l);

	/**
	 * 
	 * @param startDocIndex
	 */
	void setStartDocIndex(long startDocIndex);

	/**
	 * 
	 * @param reference
	 */
	void setNowIndexing(String reference);
	
	/**
	 * 
	 * @return
	 */
	long getLastIndex();
	/**
	 * 
	 * @return
	 */
	String getNowIndexing();
	/**
	 * 
	 * @return
	 */
	long getStartDocIndex();

        String getLastDocument();

        String getLastElapsed();

        String getCurrentDocument();

        String getCurrentElapsed();

		/**
		 * @return
		 */
		boolean isLocalLock();


}
