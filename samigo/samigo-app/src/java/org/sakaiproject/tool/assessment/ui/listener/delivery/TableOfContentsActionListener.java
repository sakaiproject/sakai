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

import java.util.Iterator;
import java.util.Set;
import java.util.Date;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.UpdateTimerListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module handles the beginning of the assessment
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id: TableOfContentsActionListener.java 5215 2006-01-09 22:26:01Z daisyf@stanford.edu $
 */

public class TableOfContentsActionListener implements ActionListener
{
  private static Log log = LogFactory.getLog(TableOfContentsActionListener.class);
  private static ContextUtil cu;

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("TableOfContentsActionListener.processAction() ");

    // get managed bean and set its action accordingly
    DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
    String nextAction = delivery.checkBeforeProceed();
    if (!("safeToProceed").equals(nextAction)) {
      delivery.setOutcome(nextAction);
    }
    else{
      SubmitToGradingActionListener s = new SubmitToGradingActionListener();
      s.processAction(ae);
      UpdateTimerListener u = new UpdateTimerListener();
      u.processAction(ae);
      DeliveryActionListener d = new DeliveryActionListener();
      d.processAction(ae);
      delivery.setOutcome("tableOfContents");
    }
  }

}
