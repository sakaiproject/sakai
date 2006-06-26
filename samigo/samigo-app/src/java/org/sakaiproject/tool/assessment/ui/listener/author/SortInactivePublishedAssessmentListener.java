/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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



package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Description: SortInactivePublishedAssessmentListener</p>
 */

public class SortInactivePublishedAssessmentListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(SortInactivePublishedAssessmentListener.class);
  private static ContextUtil cu;

  public SortInactivePublishedAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    // get service and managed bean
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    AuthorBean author = (AuthorBean) cu.lookupBean(
                       "author");

   processSortInfo(author);

    ArrayList inactivePublishedList = new ArrayList();
    inactivePublishedList = publishedAssessmentService.
          getBasicInfoOfAllInActivePublishedAssessments(this.getInactivePublishedOrderBy(author),author.isInactivePublishedAscending());

   // get the managed bean, author and set the list
   author.setInactivePublishedAssessments(inactivePublishedList);

  }

/**
   * get orderby parameter for takable table
   * @param select the SelectAssessment bean
   * @return
   */
  private String getInactivePublishedOrderBy(AuthorBean author) {
    String sort = author.getInactivePublishedAssessmentOrderBy();
    String returnType =  PublishedAssessmentFacadeQueries.TITLE;
    if (sort != null && sort.equals("releaseTo"))
    {
	returnType = PublishedAssessmentFacadeQueries.PUB_RELEASETO;
    }
    else if (sort.equals("startDate"))
    {
    	returnType = PublishedAssessmentFacadeQueries.PUB_STARTDATE;
    }
    else if (sort.equals("dueDate"))
    {
    	returnType = PublishedAssessmentFacadeQueries.PUB_DUEDATE;
    }


    return returnType;
  }

/**
   * look at sort info from post and set bean accordingly
   * @param bean the select index managed bean
   */
  private void processSortInfo(AuthorBean bean) {
    String inactiveOrder = cu.lookupParam("inactiveSortType");
    String inactivePublishedAscending = cu.lookupParam("inactivePublishedAscending");

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
