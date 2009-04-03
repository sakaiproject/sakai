/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/author/ConfirmRemoveAssessmentListener.java $
 * $Id: ConfirmRemoveAssessmentListener.java 16926 2006-10-09 23:19:51Z ktsao@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id: ConfirmRemoveAssessmentListener.java 16926 2006-10-09 23:19:51Z ktsao@stanford.edu $
 */

public class ConfirmRemovePublishedAssessmentListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ConfirmRemovePublishedAssessmentListener.class);

  public ConfirmRemovePublishedAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    // #1 - read the assessmentId from the form
    String publishedAssessmentId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("publishedAssessmentId");
    log.debug("publishedAssessmentId = " + publishedAssessmentId);
    
    // #2 -  and use it to set author bean, goto removeAssessment.jsp
    PublishedAssessmentBean publishedAssessmentBean = (PublishedAssessmentBean) ContextUtil.lookupBean("publishedassessment");
    
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentData publishedAssessment = publishedAssessmentService.getBasicInfoOfPublishedAssessment(publishedAssessmentId);
    if (publishedAssessment != null) {
    	// #3 - permission checking before proceeding - daisyf
    	AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    	if (!passAuthz(context, publishedAssessment.getCreatedBy())){
    		author.setOutcome("author");
    		return;
    	}
    	author.setOutcome("confirmRemovePublishedAssessment");
    	
    	publishedAssessmentBean.setAssessmentId(publishedAssessmentId);
    	publishedAssessmentBean.setTitle(publishedAssessment.getTitle());
    }
    else {
    	log.warn("publishedAssessment is null");
    }
  }

  public boolean passAuthz(FacesContext context, String ownerId){
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    boolean hasPrivilege_any = authzBean.getDeleteAnyAssessment();
    boolean hasPrivilege_own0 = authzBean.getDeleteOwnAssessment();
    boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(ownerId));
    boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
    if (!hasPrivilege){
      String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
				     "denied_delete_assessment_error");
      context.addMessage(null,new FacesMessage(err));
    }
    return hasPrivilege;
  }

  public boolean isOwner(String ownerId){
    boolean isOwner = false;
    String agentId = AgentFacade.getAgentString();
    isOwner = agentId.equals(ownerId);
    return isOwner;
  }

}
