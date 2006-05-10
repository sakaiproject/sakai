/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;

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
  private static final GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated =
      IntegrationContextFactory.getInstance().isIntegrated();

  public ConfirmPublishAssessmentListener() {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    boolean ipError=false;
    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) cu.
        lookupBean(
        "assessmentSettings");

    //#1 - permission checking before proceeding - daisyf
    String assessmentId=String.valueOf(assessmentSettings.getAssessmentId());
    SaveAssessmentSettings s = new SaveAssessmentSettings();
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

    //#2b - check if gradebook exist, if so, if assessment title already exists in GB
    GradebookService g = null;
    if (integrated){
      g = (GradebookService) SpringBeanLocator.getInstance().
            getBean("org.sakaiproject.service.gradebook.GradebookService");
    }
    String toGradebook = assessmentSettings.getToDefaultGradebook();
    try{
      if (toGradebook!=null && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString()) &&
          gbsHelper.isAssignmentDefined(assessmentName, g)){
        String gbConflict_err=cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","gbConflict_error");
        context.addMessage(null,new FacesMessage(gbConflict_err));
        error=true;
      }
    }
    catch(Exception e){
      log.warn("external assessment in GB has the same title:"+e.getMessage());
    }

    //#2c - validate if this is a time assessment, is there a time entry?
    Object time=assessmentSettings.getValueMap().get("hasTimeAssessment");
    boolean isTime=false;
    if (time!=null)
      isTime=((Boolean)time).booleanValue();
  
    if ((isTime) &&((assessmentSettings.getTimeLimit().intValue())==0)){
      String time_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","timeSelect_error");
      context.addMessage(null,new FacesMessage(time_err));
      error=true;
    }

    // check username not empty if Secondary ID and Password is checked
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

  // check ip address not empty if allow specific IP addresses
    Object ip=assessmentSettings.getValueMap().get("hasSpecificIP");
    boolean hasIp=false;
    try
    {
      if (ip != null)
      {
	  hasIp = ( (Boolean) ip).booleanValue();
      }
    }
    catch (Exception ex)
    {
      // keep default
      log.warn("Expecting Boolean hasSpecificIP, got: " + ip);
    }

    if(hasIp){
        boolean ipErr=false;
        String ipString = assessmentSettings.getIpAddresses().trim();    
         if(ipString.equals(""))
	   ipErr=true;
        String[]arraysIp=(ipString.split("\n"));
        //System.out.println("arraysIp.length: "+arraysIp.length);
        for(int a=0;a<arraysIp.length;a++){
            String currentString=arraysIp[a];
	    if(!currentString.trim().equals("")){
		if(a<(arraysIp.length-1))
		    currentString=currentString.substring(0,currentString.length()-1);           
		if(!s.isIpValid(currentString)){
		ipErr=true;
		break;
		}
	    }
	
	}
	if(ipErr){
	    error=true;
	    String  ip_err=cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","ip_error");
	    context.addMessage(null,new FacesMessage(ip_err));

	}
    }
 //check feedback - if at specific time then time should be defined.
    if((assessmentSettings.getFeedbackDelivery()).equals("2") && ((assessmentSettings.getFeedbackDateString()==null) || (assessmentSettings.getFeedbackDateString().equals("")))){
	error=true;
	String  date_err=cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","date_error");
	context.addMessage(null,new FacesMessage(date_err));

    }

   
    if (error){
      assessmentSettings.setOutcomePublish("editAssessmentSettings");
      return;
    }

    //#3 now u can proceed to save core assessment
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
