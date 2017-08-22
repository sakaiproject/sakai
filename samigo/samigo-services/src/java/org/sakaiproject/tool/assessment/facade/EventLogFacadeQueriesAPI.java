/**
 * Copyright (c) 2005-2012 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.facade;

import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;

public interface EventLogFacadeQueriesAPI {

	public List<EventLogData> getDataBySiteId(String siteId);

	public void saveOrUpdateEventLog(EventLogFacade eventLog);

	public List<EventLogData> getEventLogData(Long assessmentGradingId);
	
	/**
	 * Get event log data
	 * @param siteId String siteId used to limit the amount of event data to return
	 * @param assessmentId Long assessmentId used to further limit the data returned
	 * @param userFilter String filter value that will be used (if supplied) to limit based on a user id or display name
	 * @return
	 */
	public List<EventLogData> getEventLogData(String siteId, Long assessmentId, String userFilter);
	
	/**
	 * Look up all of the assessment tiles for a site's event log.  
	 * This will be used as a filter to limit the actual log data 
	 * displayed in the event log
	 * @param siteId String siteId 
	 * @return
	 */
	public List<Object[]> getTitlesFromEventLogBySite(String siteId);

}