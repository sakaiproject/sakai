/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.services.assessment;

import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;

public class EventLogService{

	public EventLogService() {
	}

	public List<EventLogData> getEventLogDataBySiteId(String siteId) {
		return PersistenceService.getInstance().getEventLogFacadeQueries().getDataBySiteId(siteId);
	}

	public List<EventLogData> getEventLogData(String siteId, Long assessmentId, String userFilter) {
      return PersistenceService.getInstance().getEventLogFacadeQueries().getEventLogData(siteId, assessmentId, userFilter);
   }

	public void saveOrUpdateEventLog(EventLogFacade eventLog) {
		PersistenceService.getInstance().getEventLogFacadeQueries().saveOrUpdateEventLog(eventLog);
	}

	public List<EventLogData> getEventLogData(Long assessmentGradingId) {
		return PersistenceService.getInstance().getEventLogFacadeQueries().getEventLogData(assessmentGradingId);
	}

	public List<Object[]> getTitlesFromEventLogBySite(String siteId) {
	   return PersistenceService.getInstance().getEventLogFacadeQueries().getTitlesFromEventLogBySite(siteId);
	}
}
