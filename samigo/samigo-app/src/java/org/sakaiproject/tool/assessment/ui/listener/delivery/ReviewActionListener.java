/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.HashMap;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ReviewActionListener implements ActionListener
{
  //private static Log log = LogFactory.getLog(ReviewActionListener.class);
  //private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    //log.info("ReviewActionListener.processAction() ");

    try {
      // get managed bean
      DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");

      String reviewAssessmentId = ContextUtil.lookupParam("reviewAssessmentId");

      // get service
      PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();

      // get assessment
      PublishedAssessmentFacade publishedAssessment =
        publishedAssessmentService.getPublishedAssessment(reviewAssessmentId);
      HashMap publishedAnswerHash = publishedAssessmentService.preparePublishedAnswerHash(publishedAssessment);

      GradingService service = new GradingService();
      HashMap itemData = service.getLastItemGradingData
        (reviewAssessmentId, AgentFacade.getAgentString());

      // get current page contents
      if (delivery.getPageContents() == null)
      {
        DeliveryActionListener listener = new DeliveryActionListener();
        delivery.setPageContents(listener.getPageContents(publishedAssessment, delivery, 
        itemData, publishedAnswerHash));
      }

      /*
      Iterator iter = delivery.getPageContents().getPartsContents().iterator();
      while (iter.hasNext()) {
        SectionContentsBean sectbean = (SectionContentsBean) iter.next();
        Iterator itemiter = sectbean.getItemContents().iterator();
        while (itemiter.hasNext()) {
          ItemContentsBean itembean = (ItemContentsBean) itemiter.next();
        }
      }
      */

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
