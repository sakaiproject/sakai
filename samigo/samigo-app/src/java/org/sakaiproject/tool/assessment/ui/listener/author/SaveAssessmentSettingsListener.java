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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class SaveAssessmentSettingsListener
    implements ActionListener
{
  //private static final GradebookServiceHelper gbsHelper = IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  //private static final boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();

  public SaveAssessmentSettingsListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.
        lookupBean("assessmentSettings");
    boolean error=false;
    String assessmentId=String.valueOf(assessmentSettings.getAssessmentId()); 
    AssessmentService assessmentService = new AssessmentService();
    SaveAssessmentSettings s = new SaveAssessmentSettings();
    String assessmentName = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getTitle());
 
    // check if name is empty
    if(assessmentName!=null &&(assessmentName.trim()).equals("")){
     	String nameEmpty_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
	context.addMessage(null,new FacesMessage(nameEmpty_err));
	error=true;
    }

    // check if name is unique 
    if(!assessmentService.assessmentTitleIsUnique(assessmentId,assessmentName,false)){
	String nameUnique_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
	context.addMessage(null,new FacesMessage(nameUnique_err));
	error=true;
    }
    
    // check if start date is valid
    if(!assessmentSettings.getIsValidStartDate()){
    	String startDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_start_date");
    	context.addMessage(null,new FacesMessage(startDateErr));
    	error=true;
    }
    // check if due date is valid
    if(!assessmentSettings.getIsValidDueDate()){
    	String dueDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_due_date");
    	context.addMessage(null,new FacesMessage(dueDateErr));
    	error=true;
    }
    // check if late submission date is valid
    if(!assessmentSettings.getIsValidRetractDate()){
    	String retractDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_retrack_date");
    	context.addMessage(null,new FacesMessage(retractDateErr));
    	error=true;
    }
    
    if (assessmentSettings.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
    	String[] groupsAuthorized = assessmentSettings.getGroupsAuthorizedToSave(); //getGroupsAuthorized();
    	if (groupsAuthorized == null || groupsAuthorized.length == 0) {
    		String releaseGroupError = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","choose_one_group");
        	context.addMessage(null,new FacesMessage(releaseGroupError));
        	error=true;
        	assessmentSettings.setNoGroupSelectedError(true);
    	}
    	else {
    		assessmentSettings.setNoGroupSelectedError(false);
    	}
    }
    
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
	String time_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","timeSelect_error");
	context.addMessage(null,new FacesMessage(time_err));
        error=true;
    }
    
    String ipString = assessmentSettings.getIpAddresses().trim();  
     String[]arraysIp=(ipString.split("\n"));
     boolean ipErr=false;
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
	    String  ip_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","ip_error");
	    context.addMessage(null,new FacesMessage(ip_err));

	}

	String unlimitedSubmissions = assessmentSettings.getUnlimitedSubmissions();
	if (unlimitedSubmissions != null && unlimitedSubmissions.equals(AssessmentAccessControlIfc.LIMITED_SUBMISSIONS.toString())) {
		try {
			String submissionsAllowed = assessmentSettings.getSubmissionsAllowed().trim();
			int submissionAllowed = Integer.parseInt(submissionsAllowed);
			if (submissionAllowed < 1) {
				throw new RuntimeException();
			}
		}
		catch (RuntimeException e){
			error=true;
			String  submission_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","submissions_allowed_error");
			context.addMessage(null,new FacesMessage(submission_err));
		}
	}
	
	//String unlimitedSubmissions = assessmentSettings.getUnlimitedSubmissions();
	String scoringType=assessmentSettings.getScoringType();
	if ((scoringType).equals(EvaluationModelIfc.AVERAGE_SCORE.toString()) && "0".equals(assessmentSettings.getUnlimitedSubmissions())) {
		try {
			String submissionsAllowed = assessmentSettings.getSubmissionsAllowed().trim();
			int submissionAllowed = Integer.parseInt(submissionsAllowed);
			if (submissionAllowed < 2) {
				throw new RuntimeException();
			}
		}
		catch (RuntimeException e){
			error=true;
			String  submission_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","averag_grading_single_submission");
			context.addMessage(null,new FacesMessage(submission_err));
		}
	}
		
    //check feedback - if at specific time then time should be defined.
    if((assessmentSettings.getFeedbackDelivery()).equals("2")) {
    	if (assessmentSettings.getFeedbackDateString()==null || assessmentSettings.getFeedbackDateString().equals("")) {
    		error=true;
    		String  date_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","date_error");
    		context.addMessage(null,new FacesMessage(date_err));
    	}
    	else if(!assessmentSettings.getIsValidFeedbackDate()){
        	String feedbackDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_feedback_date");
        	context.addMessage(null,new FacesMessage(feedbackDateErr));
        	error=true;
        }
    }
    
    // check secure delivery exit password
    SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
    if ( secureDeliveryService.isSecureDeliveryAvaliable() ) {
    	
    	String moduleId = assessmentSettings.getSecureDeliveryModule();
    	if ( ! SecureDeliveryServiceAPI.NONE_ID.equals( moduleId ) ) {
		
    		String exitPassword = assessmentSettings.getSecureDeliveryModuleExitPassword(); 
    		if ( exitPassword != null && exitPassword.length() > 0 ) {
   				
    			for ( int i = 0; i < exitPassword.length(); i++ ) {
					
    				char c = exitPassword.charAt(i);
    				if ( ! (( c >= 'a' && c <= 'z' ) || ( c >= 'A' && c <= 'Z' ) || ( c >= '0' && c <= '9' )) ) {
    					error = true;
    					String  submission_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","exit_password_error");
    					context.addMessage(null,new FacesMessage(submission_err));
    					break;
    				}
    			}					
    		}
    	}			
    }


    if (error){
      String blockDivs = ContextUtil.lookupParam("assessmentSettingsAction:blockDivs");
      assessmentSettings.setBlockDivs(blockDivs);
      assessmentSettings.setOutcomeSave("editAssessmentSettings");
      return;
    }
 
    // Set the outcome once Save button is clicked
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    assessmentSettings.setOutcomeSave(author.getFromPage());

    s.save(assessmentSettings, false);

    // reset the core listing in case assessment title changes
    List<AssessmentFacade> assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
    		author.getCoreAssessmentOrderBy(),author.isCoreAscending());
    Iterator iter = assessmentList.iterator();
	while (iter.hasNext()) {
		AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
		assessmentFacade.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
	}
    // get the managed bean, author and set the list
    author.setAssessments(assessmentList);

    // goto Question Authoring page
    EditAssessmentListener editA= new EditAssessmentListener();
    editA.setPropertiesForAssessment(author);
  }
}
