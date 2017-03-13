/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: SortInactivePublishedAssessmentListener</p>
 */

public class SortInactivePublishedAssessmentListener
    implements ActionListener
{
  private static Logger log = LoggerFactory.getLogger(SortInactivePublishedAssessmentListener.class);

  public SortInactivePublishedAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    // get service and managed bean
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
                       "author");

   processSortInfo(author);
   
   // Refresh the inactive published assessment list.
   AuthorActionListener authorActionListener = new AuthorActionListener();
   GradingService gradingService = new GradingService();
   List publishedAssessmentList = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments2(
		   this.getInactivePublishedOrderBy(author),author.isInactivePublishedAscending(), AgentFacade.getCurrentSiteId());
   authorActionListener.prepareAllPublishedAssessmentsList(author, gradingService, publishedAssessmentList);
   author.setJustPublishedAnAssessment(true);
  }

/**
   * get orderby parameter for takable table
   * @param select the SelectAssessment bean
   * @return
   */
  private String getInactivePublishedOrderBy(AuthorBean author) {
    String sort = author.getInactivePublishedAssessmentOrderBy();
    String returnType =  PublishedAssessmentFacadeQueries.TITLE;
    if (sort == null) {
    	return returnType;
    }
    else {
    	if("releaseTo".equals(sort))
    	{
    		returnType = PublishedAssessmentFacadeQueries.PUB_RELEASETO;
    	}
    	else if ("startDate".equals(sort))
    	{
    		returnType = PublishedAssessmentFacadeQueries.PUB_STARTDATE;
    	}
    	else if ("dueDate".equals(sort))
    	{
    		returnType = PublishedAssessmentFacadeQueries.PUB_DUEDATE;
    	}
    }

    return returnType;
  }

/**
   * look at sort info from post and set bean accordingly
   * @param bean the select index managed bean
   */
  private void processSortInfo(AuthorBean bean) {
    String inactiveOrder = ContextUtil.lookupParam("inactiveSortType");
    String inactivePublishedAscending = ContextUtil.lookupParam("inactivePublishedAscending");

    if (inactiveOrder != null && !inactiveOrder.trim().equals("")) {
      bean.setInactivePublishedAssessmentOrderBy(inactiveOrder);
    }

    if (inactivePublishedAscending != null && !inactivePublishedAscending.trim().equals("")) {
      try {
        bean.setInactivePublishedAscending((Boolean.valueOf(inactivePublishedAscending)).booleanValue());
      }
      catch (Exception ex) { //skip
        log.warn(ex.getMessage());
      }
    }
    else
    {
	bean.setInactivePublishedAscending(true);
    }

  }
}
