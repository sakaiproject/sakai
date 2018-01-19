/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class ConfirmRemovePublishedAssessmentListener implements ActionListener
{

  public ConfirmRemovePublishedAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    // #1 - read the assessmentId from the form
    String publishedAssessmentId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("publishedAssessmentId");
    log.debug("publishedAssessmentId = " + publishedAssessmentId);
    
    // #2 -  and use it to set author bean, goto removeAssessment.jsp
    PublishedAssessmentBean publishedAssessmentBean = (PublishedAssessmentBean) ContextUtil.lookupBean("publishedassessment");
    
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade publishedAssessment = publishedAssessmentService.getPublishedAssessmentInfoForRemove(Long.valueOf(publishedAssessmentId));
    if (publishedAssessment != null) {
    	// #3 - permission checking before proceeding - daisyf
    	AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    	
        AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
        if (!authzBean.isUserAllowedToDeleteAssessment(publishedAssessmentId, publishedAssessment.getCreatedBy(), true)) {
          String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_delete_other_members_assessment_error");
          context.addMessage(null,new FacesMessage(err));
  		  author.setOutcome("author");
  		  return;
        }

    	//Alert user to remove submissions associated with the assessment before delete the assessment
    	int submissions = publishedAssessmentService.getTotalSubmissionForEachAssessment(publishedAssessmentId);
    	if (submissions > 0) {
    		author.setOutcome("requireRemoveSubmissions");
    	} else {
    		author.setOutcome("confirmRemovePublishedAssessment");
    	}
    	//Should be publishedId or publishedAssessmentId; Set value publishedId value for totalscores.jsp
    	publishedAssessmentBean.setPublishedID(publishedAssessmentId);
    	publishedAssessmentBean.setAssessmentId(publishedAssessmentId);
    	publishedAssessmentBean.setTitle(FormattedText.convertFormattedTextToPlaintext(publishedAssessment.getTitle()));
    }
    else {
    	log.warn("publishedAssessment is null");
    }
  }

}
