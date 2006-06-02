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

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class EditAssessmentListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(EditAssessmentListener.class);
  private static ContextUtil cu;

  public EditAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();


    AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean(
                                          "assessmentBean");
   

    ItemAuthorBean itemauthorBean = (ItemAuthorBean) cu.lookupBean(
                                          "itemauthor");

    // #1a - come from authorIndex.jsp, load the assessment
    // goto editAssessment.jsp if successful
    String assessmentId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("assessmentId");
    AssessmentService assessmentService = new AssessmentService();
  
   	AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) cu.
	    lookupBean("assessmentSettings");


	if (assessmentId == null){
	    assessmentId = assessmentSettings.getAssessmentId().toString();
	}
  AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentId);
	assessmentSettings.setAssessment(assessment);

    //#1b - permission checking before proceeding - daisyf
    AuthorBean author = (AuthorBean) cu.lookupBean("author");
    author.setOutcome("editAssessment");
    if (!passAuthz(context, assessment.getCreatedBy())){
      author.setOutcome("author");
      return;
    }

    // pass authz, move on
    assessmentBean.setAssessment(assessment);
    itemauthorBean.setTarget(itemauthorBean.FROM_ASSESSMENT); // save to assessment
    // initalize the itemtype
    itemauthorBean.setItemType("");
    itemauthorBean.setItemTypeString("");

  }

  public boolean passAuthz(FacesContext context, String ownerId){
    AuthorizationBean authzBean = (AuthorizationBean) cu.lookupBean("authorization");
    boolean hasPrivilege_any = authzBean.getEditAnyAssessment();
    boolean hasPrivilege_own0 = authzBean.getEditOwnAssessment();
    boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(ownerId));
    boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
    if (!hasPrivilege){
       String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
						 "denied_edit_assessment_error");
       context.addMessage(null,new FacesMessage(err));
    }
    return hasPrivilege;
  }

  public boolean isOwner(String ownerId){
    boolean isOwner = false;
    String agentId = AgentFacade.getAgentString();
    isOwner = agentId.equals(ownerId);
    log.debug("***isOwner="+isOwner);
    return isOwner;
  }
}
