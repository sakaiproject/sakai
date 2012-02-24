/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

import java.util.List;

/**
 * A search status object
 * 
 * @author ieb
 */
public interface SearchStatus
{
	/**
	 * Last time the index was loaded
	 * 
	 * @return
	 */
	String getLastLoad();

	/**
	 * How long it tool to load the index
	 * 
	 * @return
	 */
	String getLoadTime();

	/**
	 * the current operational worker performing an index, none if there is none
	 * actively indexing
	 * 
	 * @return
	 */
	String getCurrentWorker();

	/**
	 * The latest expected time of completion of the current worker
	 * 
	 * @return
	 */
	String getCurrentWorkerETC();

	/**
	 * A list of all worker nodes in the cluster
	 * 
	 * @return
	 */
	List getWorkerNodes();

	/**
	 * the number of documents in the index, including those marked as deleted
	 * 
	 * @return
	 */
	String getNDocuments();

	/**
	 * get the number of documents pending to be indexed, including master
	 * items.
	 * 
	 * @return
	 */
	String getPDocuments();
	
}
