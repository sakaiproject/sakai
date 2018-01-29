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

import lombok.extern.slf4j.Slf4j;

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

@Slf4j
public class EventLogPreviousPageListener
implements ActionListener
{

	public EventLogPreviousPageListener()
	{
	}


	public void processAction(ActionEvent ae)
	{
		log.debug("*****Log: inside EventLogPreviousPageListener =debugging ActionEvent: " + ae);

		EventLogBean eventLog = (EventLogBean) ContextUtil.lookupBean("eventLog");

		int pageNumber = eventLog.getPreviousPageNumber();
		eventLog.setPageNumber(pageNumber);
		Map pageDataMap = eventLog.getPageDataMap();
		List eventLogDataList = (List)pageDataMap.get(Integer.valueOf(pageNumber));
		eventLog.setEventLogDataList((ArrayList)eventLogDataList);
		
		if(pageNumber < 2) {
			eventLog.setHasNextPage(Boolean.TRUE);
			eventLog.setHasPreviousPage(Boolean.FALSE);
		}
		else {
			eventLog.setHasNextPage(Boolean.TRUE);
			eventLog.setHasPreviousPage(Boolean.TRUE);
		}
	}
}
