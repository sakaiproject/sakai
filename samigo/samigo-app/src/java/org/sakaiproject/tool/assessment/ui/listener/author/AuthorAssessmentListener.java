/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.samlite.NameListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class AuthorAssessmentListener
    implements ActionListener
{

  public AuthorAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    AssessmentService assessmentService = new AssessmentService();

    //#0 - permission checking before proceeding - daisyf
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    if ("2".equals(author.getAssessCreationMode())) {
    	NameListener nameListener = new NameListener();
    	nameListener.processAction(null);
    	return;
    }
    
    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.
    lookupBean("assessmentSettings");
    author.setOutcome("createAssessment");
    author.setFirstFromPage("editAssessment");
    
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    if (!authzBean.isUserAllowedToCreateAssessment()) {
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_create_assessment_error");
        context.addMessage(null,new FacesMessage(err));
        author.setOutcome("author");
        return;
    }

    // pass authz test, move on
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
                                                      "assessmentBean");

    ItemAuthorBean itemauthorBean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
    itemauthorBean.setTarget(ItemAuthorBean.FROM_ASSESSMENT); // save to assessment


    // create an assessment based on the title entered and the assessment
    // template selected
    // #1 - read from form authorIndex_content.jsp
    String assessmentTitle = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(author.getAssessTitle());

    //HUONG's EDIT
    //check assessmentTitle and see if it is duplicated, if is not then proceed, else throw error
    boolean isUnique = assessmentService.assessmentTitleIsUnique("0", assessmentTitle, false);
    if (assessmentTitle!=null && (assessmentTitle.trim()).equals("")){
      String err1=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
      context.addMessage(null,new FacesMessage(err1));
      author.setOutcome("author");
      return;
    }
    if (!isUnique){
      String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","duplicateName_error");
      context.addMessage(null,new FacesMessage(err));
      author.setOutcome("author");
      return;
    }

    String description = author.getAssessmentDescription();
    String typeId = author.getAssessmentTypeId();
    String templateId = author.getAssessmentTemplateId();

    if (templateId == null){
      templateId = AssessmentTemplateFacade.DEFAULTTEMPLATE.toString();
    }
    
    // #2 - got all the info, create now
    AssessmentFacade assessment = null;
    try{
      assessment = createAssessment(
         assessmentTitle.trim(), description, typeId, templateId);
      EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_CREATE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId(), true));
    }
    catch(Exception e){
      // can't create assesment because gradebookService is not ready
      String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","gradebook_service_error");
      context.addMessage(null,new FacesMessage(err));
      author.setOutcome("author");
      return;
    }
	assessmentSettings.setAssessment(assessment);

    // #3a - goto editAssessment.jsp, so prepare assessmentBean
    assessmentBean.setAssessment(assessment);
    EditAssessmentListener.showPrintLink(assessmentBean);
    // #3b - reset the following
    author.setAssessTitle("");
    author.setAssessmentDescription("");
    author.setAssessmentTypeId("");
    author.setAssessmentTemplateId(AssessmentTemplateFacade.DEFAULTTEMPLATE.toString());
    author.setIsEditPendingAssessmentFlow(true);

    // #3c - update core AssessmentList
    List<AssessmentFacade> list = assessmentService.getBasicInfoOfAllActiveAssessments(AssessmentFacadeQueries.TITLE,true);
    // get the managed bean, author and set the list
    author.setAssessments(list);
    author.setOutcome("createAssessment");
  }

  public AssessmentFacade createAssessment(
      String assessmentTitle, String description, String typeId,
      String templateId) throws Exception{
    try{
      AssessmentService assessmentService = new AssessmentService();
      AssessmentFacade assessment = assessmentService.createAssessment(
        assessmentTitle, description, typeId, templateId);
      return assessment;
    }
    catch(Exception e){
      throw new Exception(e);
    }
  }

}
