/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.delivery;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */
@Slf4j
public class ResultsActionListener implements ActionListener
{

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("ResultsActionListener.processAction() ");

    // get managed bean and set its action accordingly
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    log.debug("****DeliveryBean= "+delivery);
    String actionString = ContextUtil.lookupParam("actionString");
    if (actionString != null) {
      // if actionString is null, likely that action & actionString has been set already, 
      // e.g. take assessment via url, actionString is set by LoginServlet.
      // preview and take assessment is set by the parameter in the jsp pages
      delivery.setActionString(actionString);
    }

    delivery.setFeedback("true");
    delivery.getFeedbackComponent().setShowCorrectResponse(true);
    delivery.getFeedbackComponent().setShowGraderComment(true);
    delivery.getFeedbackComponent().setShowItemLevel(true);
    delivery.getFeedbackComponent().setShowQuestion(true);
    delivery.getFeedbackComponent().setShowResponse(true);
    delivery.getFeedbackComponent().setShowSelectionLevel(true);
    delivery.getFeedbackComponent().setShowStats(true);
    delivery.getFeedbackComponent().setShowStudentScore(true);
    delivery.getFeedbackComponent().setShowStudentQuestionScore(true);
  }
}
