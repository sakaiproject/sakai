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

import java.sql.SQLException;
import java.util.List;

public interface SearchIndexBuilderWorker 
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
