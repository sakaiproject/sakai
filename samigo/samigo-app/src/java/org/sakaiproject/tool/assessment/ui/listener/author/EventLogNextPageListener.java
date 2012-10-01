package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.EventLogBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class EventLogNextPageListener
implements ActionListener
{
	private static Log log = LogFactory.getLog(EventLogNextPageListener.class);

	public EventLogNextPageListener()
	{
	}


	public void processAction(ActionEvent ae)
	{
		log.debug("*****Log: inside EventLogNextPageListener =debugging ActionEvent: " + ae);

		EventLogBean eventLog = (EventLogBean) ContextUtil.lookupBean("eventLog");

		int pageNumber = eventLog.getNextPageNumber();
		eventLog.setPageNumber(pageNumber);
		Map pageDataMap = eventLog.getPageDataMap();
		List eventLogDataList = (List)pageDataMap.get(Integer.valueOf(pageNumber));
		eventLog.setEventLogDataList((ArrayList)eventLogDataList);
		
		if(pageNumber > (pageDataMap.size()-1)) {
			eventLog.setHasNextPage(Boolean.FALSE);
			eventLog.setHasPreviousPage(Boolean.TRUE);
		}
		else {
			eventLog.setHasNextPage(Boolean.TRUE);
			eventLog.setHasPreviousPage(Boolean.TRUE);
		}	
	}
}