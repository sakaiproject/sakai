/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/


package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
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

public class AuthorAssessmentListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(AuthorAssessmentListener.class);
  private static ContextUtil cu;

  public AuthorAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    AssessmentService assessmentService = new AssessmentService();
    PublishedAssessmentService publishedService= new PublishedAssessmentService();
    //#0 - permission checking before proceeding - daisyf
    AuthorBean author = (AuthorBean) cu.lookupBean(
                         "author");
AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) cu.
    lookupBean("assessmentSettings");
    author.setOutcome("createAssessment");
    if (!passAuthz(context)){
      author.setOutcome("author");
      return;
    }

    // pass authz test, move on
    AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean(
                                                      "assessmentBean");

    ItemAuthorBean itemauthorBean = (ItemAuthorBean) cu.lookupBean("itemauthor");
    itemauthorBean.setTarget(itemauthorBean.FROM_ASSESSMENT); // save to assessment


    // create an assessment based on the title entered and the assessment
    // template selected
    // #1 - read from form authorIndex.jsp
    String assessmentTitle = author.getAssessTitle();

    //HUONG's EDIT
    //check assessmentTitle and see if it is duplicated, if is not then proceed, else throw error
    boolean isUnique = assessmentService.assessmentTitleIsUnique("0", assessmentTitle, false);
    boolean isUniquePublish= publishedService.publishedAssessmentTitleIsUnique("0", assessmentTitle);
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
    // #3b - reset the following
    author.setAssessTitle("");
    author.setAssessmentDescription("");
    author.setAssessmentTypeId("");
    author.setAssessmentTemplateId(AssessmentTemplateFacade.DEFAULTTEMPLATE.toString());

    // #3c - update core AssessmentList
    ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessments(AssessmentFacadeQueries.TITLE,true);
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

  public boolean passAuthz(FacesContext context){
    AuthorizationBean authzBean = (AuthorizationBean) cu.lookupBean(
                         "authorization");
    boolean hasPrivilege = authzBean.getCreateAssessment();
    if (!hasPrivilege){
      String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                  "denied_create_assessment_error");
      context.addMessage(null,new FacesMessage(err));
    }
    return hasPrivilege;
  }
}
