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

import java.util.Date;
import java.util.Map;


import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ConfirmPublishAssessmentListener
    implements ActionListener {

  private static Log log = LogFactory.getLog(ConfirmPublishAssessmentListener.class);
  private static ContextUtil cu;

  public ConfirmPublishAssessmentListener() {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    
    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) cu.
        lookupBean(
        "assessmentSettings");

    //#1 - permission checking before proceeding - daisyf
    String assessmentId=String.valueOf(assessmentSettings.getAssessmentId());
    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentId);
    if (!passAuthz(context, assessment.getCreatedBy())){
      assessmentSettings.setOutcomePublish("editAssessmentSettings");
      return;
    }

    //proceed to look for error, save assessment setting and confirm publish
    //#2a - look for error: check if core assessment title is unique
    boolean error=false;

    String assessmentName=assessmentSettings.getTitle();
    if(assessmentName!=null &&(assessmentName.trim()).equals("")){
     	String nameEmpty_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
	context.addMessage(null,new FacesMessage(nameEmpty_err));
	error=true;
    }
    if(!assessmentService.assessmentTitleIsUnique(assessmentId,assessmentName,false)){
      String nameUnique_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
      context.addMessage(null,new FacesMessage(nameUnique_err));
      error=true;
    }

    //#2b - validate if this is a time assessment, is there a time entry?
    Object time=assessmentSettings.getValueMap().get("hasTimeAssessment");
    boolean isTime=false;
    if (time!=null)
      isTime=((Boolean)time).booleanValue();
  
    if ((isTime) &&((assessmentSettings.getTimeLimit().intValue())==0)){
      String time_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","timeSelect_error");
      context.addMessage(null,new FacesMessage(time_err));
      error=true;
    }
     
    Object userName=assessmentSettings.getValueMap().get("hasUsernamePassword");
    boolean hasUserName=false;
    try
    {
      if (userName != null)
      {
	  hasUserName = ( (Boolean) userName).booleanValue();
      }
    }
    catch (Exception ex)
    {
      // keep default
      log.warn("Expecting Boolean hasUswerNamePassword, got: " + userName);

    }

    if((hasUserName) &&((assessmentSettings.getUsername().trim()).equals(""))){
	String userName_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","userName_error");
	context.addMessage(null,new FacesMessage(userName_err));
        error=true;

    }

   
    if (error){
      assessmentSettings.setOutcomePublish("editAssessmentSettings");
      return;
    }

    //#3 now u can proceed to save core assessment
    SaveAssessmentSettings s = new SaveAssessmentSettings();
    assessment = s.save(assessmentSettings);
    assessmentSettings.setAssessment(assessment);

    //  we need a publishedUrl, this is the url used by anonymous user
    String releaseTo = assessment.getAssessmentAccessControl().getReleaseTo();
    if (releaseTo != null) {
      // generate an alias to the pub assessment
      String alias = AgentFacade.getAgentString() + (new Date()).getTime();
      assessmentSettings.setAlias(alias);
      //log.info("servletPath=" + extContext.getRequestServletPath());
      String server = ( (javax.servlet.http.HttpServletRequest) extContext.
			      getRequest()).getRequestURL().toString();
      int index = server.indexOf(extContext.getRequestContextPath() + "/"); // "/samigo/"
      server = server.substring(0, index);
      //log.info("servletPath=" + server);
      String url = server + extContext.getRequestContextPath();
      assessmentSettings.setPublishedUrl(url + "/servlet/Login?id=" + alias);

    }
   
    //#4 - before going to confirm publishing, check if the title is unique
    PublishedAssessmentService publishedService = new PublishedAssessmentService();
    if ( !publishedService.publishedAssessmentTitleIsUnique(assessmentId,assessmentName)){
      String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","published_assessment_title_not_unique_error");
      context.addMessage(null,new FacesMessage(err));
      assessmentSettings.setOutcomePublish("editAssessmentSettings");
      return;
    }
    assessmentSettings.setOutcomePublish("saveSettingsAndConfirmPublish"); // finally goto confirm
  }

  public boolean passAuthz(FacesContext context, String ownerId){
    AuthorizationBean authzBean = (AuthorizationBean) cu.lookupBean("authorization");
    boolean hasPrivilege_any = authzBean.getPublishAnyAssessment();
    boolean hasPrivilege_own0 = authzBean.getPublishOwnAssessment();
    boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(ownerId));
    boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
    if (!hasPrivilege){
      String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
		     "denied_publish_assessment_error");
      context.addMessage("authorIndexForm:publish_assessment_denied",new FacesMessage(err));
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
