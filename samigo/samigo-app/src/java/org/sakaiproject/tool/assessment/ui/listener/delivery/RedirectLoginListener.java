/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2004, 2005, 2006 Sakai Foundation
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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Purpose:  this module creates the lists of published assessments for the select index
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class RedirectLoginListener
    implements ActionListener
{

  static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  //private static Log log = LogFactory.getLog(RedirectLoginListener.class);

  /**
   * ACTION.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    //log.info("RedirectLoginListener.processAction() ");

    try {
      // if users is authenticated, goto selectAssessment
      // else go to login page
      // get managed bean
      String outcome = "select";
      //log.info("agentId="+AgentFacade.getAgentString());
      if (AgentFacade.getAgentString()==null || ("").equals(AgentFacade.getAgentString()))
        outcome = "index";
      DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
      delivery.setOutcome(outcome);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
