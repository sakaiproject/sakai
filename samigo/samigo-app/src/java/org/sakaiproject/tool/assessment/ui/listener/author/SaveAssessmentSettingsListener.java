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

import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
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

public class SaveAssessmentSettingsListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(SaveAssessmentSettingsListener.class);
  private static ContextUtil cu;
  private static final GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated =
      IntegrationContextFactory.getInstance().isIntegrated();

  public SaveAssessmentSettingsListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) cu.
        lookupBean("assessmentSettings");
    boolean error=false;
    String assessmentId=String.valueOf(assessmentSettings.getAssessmentId()); 
    AssessmentService assessmentService = new AssessmentService();
    SaveAssessmentSettings s= new SaveAssessmentSettings();
    String assessmentName=assessmentSettings.getTitle();
    System.out.println ("BEFORE CHECK NAME EMPTY");
    // check if name is empty
    if(assessmentName!=null &&(assessmentName.trim()).equals("")){
     	String nameEmpty_err=cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
	context.addMessage(null,new FacesMessage(nameEmpty_err));
	error=true;
    }
 System.out.println ("BEFORE CHECK NAME UNIQUE");
    // check if name is unique 
    if(!assessmentService.assessmentTitleIsUnique(assessmentId,assessmentName,false)){
	String nameUnique_err=cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
	context.addMessage(null,new FacesMessage(nameUnique_err));
	error=true;
    }
    // Huong's fix for assessment Settings - Save Assessment Settings should not be checked for duplication with the gradebook.(see bug)
    // check if gradebook exist, if so, if assessment title already exists in GB
    // GradebookService g = null;
    //  if (integrated){
    //  g = (GradebookService) SpringBeanLocator.getInstance().
    //	    getBean("org.sakaiproject.service.gradebook.GradebookService");
    //  }
    //  String toGradebook = assessmentSettings.getToDefaultGradebook();
    // try{
    //  if (toGradebook!=null && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString()) &&
    //      gbsHelper.isAssignmentDefined(assessmentName, g)){
    //    String gbConflict_err=cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","gbConflict_error");
    //	context.addMessage(null,new FacesMessage(gbConflict_err));
    //	error=true;
    // }
    //  }
    //catch(Exception e){
    //  log.warn("external assessment in GB has the same title:"+e.getMessage());
    // }

    //  if timed assessment, does it has value for time
    Object time=assessmentSettings.getValueMap().get("hasTimeAssessment");
    boolean isTime=false;
    try
    {
      if (time != null)
      {
        isTime = ( (Boolean) time).booleanValue();
      }
    }
    catch (Exception ex)
    {
      // keep default
      log.warn("Expecting Boolean hasTimeAssessment, got: " + time);

    }
    if((isTime) &&((assessmentSettings.getTimeLimit().intValue())==0)){
	String time_err=cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","timeSelect_error");
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

    // check username for high security
    if((hasUserName) &&((assessmentSettings.getUsername().trim()).equals(""))){
	String userName_err=cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","userName_error");
	context.addMessage(null,new FacesMessage(userName_err));
        error=true;
    }
    
   // check ip address not empty if only allow specific IP addresses
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
   
    // check valid ip addresses
    if(hasIp){
        boolean ipErr=false;
        String ipString = assessmentSettings.getIpAddresses().trim();    
         if(ipString.equals(""))
	   ipErr=true;
        String[]arraysIp=(ipString.split("\n"));
       
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
      assessmentSettings.setOutcomeSave("editAssessmentSettings");
      return;
    }
 
    assessmentSettings.setOutcomeSave("author");
    s.save(assessmentSettings);
    // reset the core listing in case assessment title changes
    AuthorBean author = (AuthorBean) cu.lookupBean(
                       "author");
 
    ArrayList assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
                      author.getCoreAssessmentOrderBy(),author.isCoreAscending());
    // get the managed bean, author and set the list
    author.setAssessments(assessmentList);

    // goto Question Authoring page
    EditAssessmentListener editA= new EditAssessmentListener();
    editA.processAction(null);

  }

}
