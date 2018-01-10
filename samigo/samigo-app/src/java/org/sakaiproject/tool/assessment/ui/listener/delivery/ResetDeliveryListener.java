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

import java.util.ArrayList;
import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module handles the beginning of the assessment
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */
@Slf4j
public class ResetDeliveryListener implements ActionListener
{
  private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    DeliveryBean bean = (DeliveryBean) cu.lookupBean("delivery");
    if (!("takeAssessmentViaUrl").equals(bean.getActionString()))
      bean.setPublishedAssessment(null);
    bean.setPublishedItemHash(new HashMap());
    bean.setPublishedItemTextHash(new HashMap());
    bean.setPublishedAnswerHash(new HashMap());
    // reset timer before begin
    bean.setTimeElapse("0");
    bean.setTimeElapseAfterFileUpload(null);
    bean.setLastTimer(0);
    bean.setTimeLimit("0");
    bean.setNumberRetake(-1);
    bean.setActualNumberRetake(-1);
    bean.setHasShowTimeWarning(false);
    bean.setShowTimeWarning(false);
    bean.setTurnIntoTimedAssessment(false);
    bean.setSkipFlag(false);
    bean.setSubmitFromTimeoutPopup(false);
  }
}
