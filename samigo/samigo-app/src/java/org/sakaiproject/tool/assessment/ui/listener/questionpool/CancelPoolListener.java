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

package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: CancelPoolListener</p>
 * <p>Description: Samigo Cancel Pool Listener</p>
 * @version $Id$
 */
@Slf4j
public class CancelPoolListener implements ActionListener
{
  private static ContextUtil cu;


  /**
   * Check if the outcome pool is the parent of the current pool or the current outcome pool.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) cu.lookupBean("questionpool");
    boolean returnToParentPool = "true".equals((String)ae.getComponent().getAttributes().get("returnToParentPool"));
	if(qpoolbean.getCurrentPool().getId()!=null && qpoolbean.getCurrentPool().getId()==qpoolbean.getOutcomePool() && returnToParentPool){
		qpoolbean.setOutcomePool(qpoolbean.getCurrentPool().getParentPoolId());
	}	  

  }

}
