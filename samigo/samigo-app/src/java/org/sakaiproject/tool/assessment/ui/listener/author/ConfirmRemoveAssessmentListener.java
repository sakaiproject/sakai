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

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
public class ConfirmRemoveAssessmentListener implements ActionListener
{

  public ConfirmRemoveAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    // #1 - read the assessmentId from the form
    String assessmentId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("assessmentId");

    // #2 -  and use it to set author bean, goto removeAssessment.jsp
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
                                                           "assessmentBean");
    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.getBasicInfoOfAnAssessment(assessmentId);

    // #3 - permission checking before proceeding - daisyf
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    author.setOutcome("confirmRemoveAssessment");
    
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    if (!authzBean.isUserAllowedToDeleteAssessment(assessmentId, assessment.getCreatedBy(), false)) {
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_delete_other_members_assessment_error");
        context.addMessage(null,new FacesMessage(err));
        author.setOutcome("author");
    	return;
    }

    assessmentBean.setAssessmentId(assessment.getAssessmentBaseId().toString());
    assessmentBean.setTitle(assessment.getTitle());
  }

}
