package org.sakaiproject.tool.assessment.services.assessment;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.facade.EventLogFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;

public class EventLogService{
	private Log log = LogFactory.getLog(EventLogService.class);

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