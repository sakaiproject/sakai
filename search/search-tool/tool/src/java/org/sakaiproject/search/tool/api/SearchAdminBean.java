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

package org.sakaiproject.search.tool.api;

import java.util.List;
import java.util.Set;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.tool.model.AdminOption;
import org.sakaiproject.search.tool.model.MasterRecord;
import org.sakaiproject.search.tool.model.Segment;
import org.sakaiproject.search.tool.model.WorkerThread;

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
	 *        {0} is the Last Load time {1} is how long it took to load {2} is
	 *        the current worker node (none if none) {3} is the Latest time of
	 *        completion of the worker
	 * @return
	 * @throws PermissionException
	 *         if the user does not have permissions to perform the action
	 * @deprecated
	 */
	String getIndexStatus(String statusFormatString) throws PermissionException;

	/**
	 * @param rowFormat
	 *        {0} is the worker name {1} is the latest time the node should
	 *        reaapear, after which time the node will be considered overdue and
	 *        removed from the list of worker nodes.
	 * @return
	 * @deprecated
	 */
	String getWorkers(String rowFormat);

	/**
	 * Get admin options formatted according to pattern
	 * 
	 * @param adminOptionsFormat
	 *        format pattern {0} is the URL, {1} is the text
	 * @return
	 * @deprecated
	 */
	String getAdminOptions(String adminOptionsFormat);

	/**
	 * get a BIG list of all documents in the index
	 * 
	 * @param rowFormat
	 * @return
	 * @deprecated
	 */
	String getIndexDocuments(String rowFormat);

	/**
	 * get a list of Global Master documents
	 * 
	 * @param rowFormat
	 * @return
	 * @deprecated
	 */
	String getGlobalMasterDocuments(String rowFormat);

	/**
	 * get a list of Site Master Documents
	 * 
	 * @param rowFormat
	 * @return
	 * @deprecated
	 */
	String getSiteMasterDocuments(String rowFormat);

	/**
	 * get feedback from the command
	 * @return
	 */
	String getCommandFeedback();

	/**
	 * 
	 * @param rowFormat
	 * @return
	 * @deprecated
	 */
	String getSegmentInfo(String rowFormat);

    /**
     * @return all of the names of the different index builders
     */
    Set<String> getIndexBuilderNames();

	/**
	 * @return get a list of options for the admin user that only apply
     * to the default index or to all index builders
	 */
	List<AdminOption> getDefaultOptions();

	/**
	 * @return get a list of options for the admin user that only apply
     * to the one index builder at a time (requires selecting which
     * index builder to use)
	 */
	List<AdminOption> getIndexSpecificOptions();

	/**
	 * get the search status
	 * @return
	 */
	List<SearchStatus> getSearchStatus();

	/**
	 * get a list of master objects 
	 * @return
	 */
	List<MasterRecord> getGlobalMasterRecords();

	/**
	 * get a list of site master records
	 * @return
	 */
	List<MasterRecord> getSiteMasterRecords();

	/**
	 * get a list of segments
	 * @return
	 */
	List<Segment> getSegments();

	/**
	 * get a list of worker threads
	 * @return
	 */
	List<WorkerThread> getWorkerThreads();

	/**
	 * @return
	 */
	boolean isRedirectRequired();

	/**
	 * returns true if search isEnabled
	 * @return
	 */
	boolean isEnabled();
}
