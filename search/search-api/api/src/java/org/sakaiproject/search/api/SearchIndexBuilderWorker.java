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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.sakaiproject.search.model.SearchWriterLock;

public interface SearchIndexBuilderWorker
{

	/**
	 * update the node lock for the current Worker Object on the current thread.
	 * This MUST only be used by a worker thread
	 * @throws SQLException
	 */
	void updateNodeLock() throws SQLException;

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

	void destroy();

	boolean removeWorkerLock();

}
