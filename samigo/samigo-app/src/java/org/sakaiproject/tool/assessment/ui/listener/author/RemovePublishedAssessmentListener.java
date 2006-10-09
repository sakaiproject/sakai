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
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class RemovePublishedAssessmentListener
    implements ActionListener
{
  public RemovePublishedAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    PublishedAssessmentService service = new PublishedAssessmentService();
    // #1 - remove selected assessment
    String assessmentId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("publishedAssessmentId");

    if (assessmentId == null)  // means from the preview assessment button in delivery
    {
       DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
       assessmentId = delivery.getAssessmentId();
       PublishedAssessmentIfc pub = service.getPublishedAssessment(assessmentId.toString());
       storeResourceIdListInPersonBean(pub);
       RemovePublishedAssessmentThread thread = new RemovePublishedAssessmentThread(assessmentId);
       thread.start();
    }
    else
    {
      PublishedAssessmentIfc pub = service.getPublishedAssessment(assessmentId.toString());
      storeResourceIdListInPersonBean(pub);
      RemovePublishedAssessmentThread thread = new RemovePublishedAssessmentThread(assessmentId);
      thread.start();
      PublishedAssessmentService assessmentService = new PublishedAssessmentService();

      //#3 - goto authorIndex.jsp so fix the assessment List in author bean after
      // removing an assessment
      AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
      ArrayList assessmentList = assessmentService.getBasicInfoOfAllActivePublishedAssessments(
        author.getPublishedAssessmentOrderBy(),author.isPublishedAscending());
      // get the managed bean, author and set the list
      author.setPublishedAssessments(assessmentList);

      ArrayList inactivePublishedList = assessmentService.getBasicInfoOfAllInActivePublishedAssessments(
        author.getInactivePublishedAssessmentOrderBy(),author.isInactivePublishedAscending());
       // get the managed bean, author and set the list
       author.setInactivePublishedAssessments(inactivePublishedList);
    }

  }

  private void storeResourceIdListInPersonBean(AssessmentIfc pub){
    AssessmentService s = new AssessmentService();
    List resourceIdList = s.getAssessmentResourceIdList(pub);
    PersonBean personBean = (PersonBean) ContextUtil.lookupBean("person");
    personBean.setResourceIdListInPreview(resourceIdList);
  }
}
