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

package org.sakaiproject.search.tool;

import java.text.MessageFormat;

import org.sakaiproject.search.api.SearchStatus;

/**
 * A backing bean for the search admin page. The implementation of this bean
 * should enfoce any authz that is required.
 * 
 * @author ieb
 */
public interface SearchAdminBean
{

	/**
	 * Get the Title of the index
	 * 
	 * @return
	 */
	String getTitle();

	/**
	 * get an HTML fragment representing the status of the index
	 * 
	 * @param statusFormatString
	 * 			{0} is the Last Load time
	 *           {1} is how long it took to load
	 *           {2} is the current worker node (none if none)
	 *           {3} is the Latest time of completion of the worker
	 * @return
	 */
	String getIndexStatus(String statusFormatString);
	
	/**
	 * 
	 * @param rowFormat
	 *      {0} is the worker name
	 *      {1} is the latest time the node should reaapear, after which 
	 *      time the node will be considered overdue and removed from the 
	 *      list of worker nodes.
	 * @return
	 */
	String getWorkers(String rowFormat);

	/**
	 * Get admin options formatted according to pattern
	 * 
	 * @param adminOptionsFormat
	 *        format pattern {0} is the URL, {1} is the text
	 * @return
	 */
	String getAdminOptions(String adminOptionsFormat);
	
	/**
	 * get a BIG list of all documents in the index
	 * @param rowFormat
	 * @return
	 */
	String getIndexDocuments( String rowFormat );
	/**
	 * get a list of Global Master documents
	 * @param rowFormat
	 * @return
	 */
	String getGlobalMasterDocuments( String rowFormat );
	/**
	 * get a list of Site Master Documents
	 * @param rowFormat
	 * @return
	 */
	String getSiteMasterDocuments( String rowFormat );
	String getCommandFeedback();
}
