/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Description: SortCoreAssessmentListener</p>
 */
@Slf4j
public class SortCoreAssessmentListener
    implements ActionListener
{

  public SortCoreAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    // get service and managed bean
    AssessmentService assessmentService = new AssessmentService();
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
                       "author");

    // look for some sort information passed as parameters
    processSortInfo(author);

    //String orderBy = (String) FacesContext.getCurrentInstance().
    //   getExternalContext().getRequestParameterMap().get("coreOrderBy");
    //author.setCoreAssessmentOrderBy(orderBy);

    List assessmentList = new ArrayList();

      assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
        this.getCoreOrderBy(author), author.isCoreAscending());

    Iterator iter = assessmentList.iterator();
  	while (iter.hasNext()) {
  		AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
  		assessmentFacade.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
  	}
    // get the managed bean, author and set the list
    author.setAssessments(assessmentList);
    author.setJustPublishedAnAssessment(false);
  }

/**
   * get orderby parameter for takable table
   * @param select the SelectAssessment bean
   * @return
   */
  private String getCoreOrderBy(AuthorBean author) {
    return AssessmentFacadeQueries.TITLE;
  }

/**
   * look at sort info from post and set bean accordingly
   * @param bean the select index managed bean
   */
  private void processSortInfo(AuthorBean bean) {
    String coreOrder = ContextUtil.lookupParam("coreSortType");
    String coreAscending = ContextUtil.lookupParam("coreAscending");

    if (coreOrder != null && !coreOrder.trim().equals("")) {
      bean.setCoreAssessmentOrderBy(coreOrder);
    }

    if (coreAscending != null && !coreAscending.trim().equals("")) {
      try {
        bean.setCoreAscending(Boolean.valueOf(coreAscending));
      }
      catch (Exception ex) { //skip
        log.warn(ex.getMessage());
      }
    }
    else
    {
	bean.setCoreAscending(true);
    }

  }

}
