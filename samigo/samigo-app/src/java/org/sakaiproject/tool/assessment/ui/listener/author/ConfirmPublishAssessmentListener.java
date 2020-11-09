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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.tool.assessment.util.TimeLimitValidator;
import org.sakaiproject.util.api.FormattedText;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class ConfirmPublishAssessmentListener
    implements ActionListener {

  //private static ContextUtil cu;
  private static final GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated =
      IntegrationContextFactory.getInstance().isIntegrated();
  private boolean isFromActionSelect = false;

  public ConfirmPublishAssessmentListener() {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext extContext = context.getExternalContext();
    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.lookupBean("assessmentSettings");
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    //#1 - permission checking before proceeding - daisyf
    String assessmentId=String.valueOf(assessmentSettings.getAssessmentId());
    SaveAssessmentSettings s = new SaveAssessmentSettings();
    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
    
    // Check permissions
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    if (!authzBean.isUserAllowedToPublishAssessment(assessmentId, assessment.getCreatedBy(), false)) {
        String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_publish_assessment_error");
        context.addMessage(null,new FacesMessage(err));
        assessmentSettings.setOutcomePublish("editAssessmentSettings");
        author.setIsErrorInSettings(true);
        return;
    }

    assessmentBean.setAssessment(assessment);
    
    // Only if the assessment already have question, this page will be excuted
    assessmentSettings.setHasQuestions(true);
    
    //proceed to look for error, save assessment setting and confirm publish
    //#2a - look for error: check if core assessment title is unique
    boolean error=false;

    String assessmentName = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessmentSettings.getTitle());
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
    

    final Date startDate = assessmentSettings.getStartDate();
    final Date dueDate = assessmentSettings.getDueDate();
    final Date retractDate = assessmentSettings.getRetractDate();
    final boolean isAcceptingLateSubmissions = assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling());
    boolean isRetractEarlierThanAvaliable = false;

    if ((dueDate != null && startDate != null && dueDate.before(startDate)) ||
    	(dueDate != null && startDate == null && dueDate.before(new Date()))) {
    	String dateError1 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","due_earlier_than_avaliable");
    	context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError1, null));
    	error=true;
    	assessmentSettings.setStartDate(new Date());
    }
    if (isAcceptingLateSubmissions) {
	    if ((retractDate != null && startDate != null && retractDate.before(startDate)) ||
	        (retractDate != null && startDate == null && retractDate.before(new Date()))) {
	    	String dateError2 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","retract_earlier_than_avaliable");
	    	context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError2, null));
	    	error=true;
	    	isRetractEarlierThanAvaliable = true;
	    	assessmentSettings.setStartDate(new Date());
	    }
	    if (!isRetractEarlierThanAvaliable && (retractDate != null && dueDate != null && retractDate.before(dueDate))) {
	    	String dateError3 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","retract_earlier_than_due");
	    	context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError3, null));
	    	error=true;
	    }
    }

	if(dueDate != null && startDate != null && dueDate.equals(startDate)) {
		String dateError4 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "due_same_as_available");
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, dateError4, null));
		error=true;
	}

	// if using a time limit, ensure open window is greater than or equal to time limit
	boolean hasTimer = TimeLimitValidator.hasTimer(assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes());
	if (hasTimer) {
		Date due = retractDate != null && isAcceptingLateSubmissions ? retractDate : dueDate;
		boolean availableLongerThanTimer = TimeLimitValidator.availableLongerThanTimer(startDate, due, assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes(),
																						"org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "open_window_less_than_time_limit", context);
		if(!availableLongerThanTimer) {
			error = true;
		}
	}

    // if due date is null we cannot have late submissions
    if (assessmentSettings.getDueDate() == null && assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling()) &&
        assessmentSettings.getRetractDate() !=null){
        String noDueDate = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","due_null_with_retract_date");
        context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, noDueDate, null));
        error=true;
    }
    
    // SAM-1088
    // if late submissions not allowed and late submission date is null, set late submission date to due date
    final boolean autoSubmitEnabled = ServerConfigurationService.getBoolean("samigo.autoSubmit.enabled", true);
    if (assessmentSettings.getLateHandling() != null && AssessmentAccessControlIfc.NOT_ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling()) &&
    		retractDate == null && dueDate != null && assessmentSettings.getAutoSubmit()) {
    	if (autoSubmitEnabled) {
    		assessmentSettings.setRetractDate(dueDate);
    	}
    }
    // if auto-submit is enabled, make sure late submission date is set
    else if (assessmentSettings.getAutoSubmit() && retractDate == null) {
    	if (autoSubmitEnabled) {
    		String dateError4 = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","retract_required_with_auto_submit");
    		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN, dateError4, null));
    		error=true;
    	}
    }

    if (!isFromActionSelect) {
    	if (assessmentSettings.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
    		String[] groupsAuthorized = assessmentSettings.getGroupsAuthorized(); //getGroupsAuthorized();
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


    	//#2c - validate if this is a time assessment, is there a time entry?
    	Object time=assessmentSettings.getValueMap().get("hasTimeAssessment");
    	boolean isTime=false;
    	if (time!=null) {
    		// Because different flow might get different type, we test it before cast.
    		if (time instanceof java.lang.String) {
    			isTime = Boolean.getBoolean((String) time);
    		}
    		else if (time instanceof java.lang.Boolean) {
    			isTime= (Boolean) time;
    		}
    	}


    	if ((isTime) &&((assessmentSettings.getTimeLimit())==0)){
    		String time_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","timeSelect_error");
    		context.addMessage(null,new FacesMessage(time_err));
    		error=true;
    	}
    	boolean ipErr=false;
    	String ipString = assessmentSettings.getIpAddresses().trim().replace(" ", "");
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
    		String  ip_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","ip_error");
    		context.addMessage(null,new FacesMessage(ip_err));
    	}

    	String unlimitedSubmissions = assessmentSettings.getUnlimitedSubmissions();
    	if (unlimitedSubmissions != null && unlimitedSubmissions.equals(AssessmentAccessControlIfc.LIMITED_SUBMISSIONS.toString())) {
    		String submissionsAllowed = assessmentSettings.getSubmissionsAllowed().trim();
    		try {
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
		if(assessmentSettings.getFeedbackDelivery().equals("2")) {
			if(StringUtils.isBlank(assessmentSettings.getFeedbackDateString())){
				error=true;
				String date_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","date_error");
				context.addMessage(null,new FacesMessage(date_err));
			}
			else {
				// TODO this logic should be refactored
				if(StringUtils.isNotBlank(assessmentSettings.getFeedbackEndDateString()) && assessmentSettings.getFeedbackDate().after(assessmentSettings.getFeedbackEndDate())){
					String feedbackDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_feedback_ranges");
					context.addMessage(null,new FacesMessage(feedbackDateErr));
					error=true;
				}
			}

			if(!assessmentSettings.getIsValidFeedbackDate()){
				String feedbackDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_feedback_date");
				context.addMessage(null,new FacesMessage(feedbackDateErr));
				error=true;
			}

			boolean scoreThresholdEnabled = assessmentSettings.getFeedbackScoreThresholdEnabled();
			//Check if the value is empty
			boolean scoreThresholdError = StringUtils.isBlank(assessmentSettings.getFeedbackScoreThreshold());
			//If the threshold value is not empty, check if is a valid percentage
			if (!scoreThresholdError) {
				String submittedScoreThreshold = StringUtils.replace(assessmentSettings.getFeedbackScoreThreshold(), ",", ".");
				try {
					Double doubleInput = new Double(submittedScoreThreshold);
					if(doubleInput.compareTo(new Double("0.0")) == -1 || doubleInput.compareTo(new Double("100.0")) == 1){
						throw new Exception();
					}
				} catch(Exception ex) {
					scoreThresholdError = true;
				}
			}
			//If the threshold is enabled and is not valid, display an error.
			if(scoreThresholdEnabled && scoreThresholdError){
				error = true;
				String str_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","feedback_score_threshold_required");
				context.addMessage(null,new FacesMessage(str_err));
			}
		}
    }
    else {
    	if (assessmentSettings.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
    		String[] groupsAuthorized = assessmentSettings.getGroupsAuthorized(); //populate groupsAuthorized;
    		if (groupsAuthorized == null || groupsAuthorized.length == 0) {
    			String releaseGroupError = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","choose_release_to");
    			context.addMessage(null,new FacesMessage(releaseGroupError));
    			error=true;
    			assessmentSettings.setNoGroupSelectedError(true);
    		}
    		else {
    			assessmentSettings.setNoGroupSelectedError(false);
    		} 
    	}
    }
    
    //Gradebook right now only excep if total score >0 check if total score<=0 then throw error.
    if(assessmentSettings.getToDefaultGradebook())
	{
 	    if(assessmentBean.getTotalScore()<=0)
		{
 	    	String gb_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "gradebook_exception_min_points");
            context.addMessage(null, new FacesMessage(gb_err));
            error=true;
		}
	}

    //#2b - check if gradebook exist, if so, if assessment title already exists in GB
    GradebookExternalAssessmentService g = null;
    if (integrated){
      g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().
            getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
    }
    try{
	if (assessmentSettings.getToDefaultGradebook() && gbsHelper.isAssignmentDefined(assessmentSettings.getTitle(), g)){
        String gbConflict_err= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages" , "gbConflict_error");
        context.addMessage(null,new FacesMessage(gbConflict_err));
        error=true;
      }
    }
    catch(Exception e){
      log.warn("external assessment in GB has the same title:"+e.getMessage());
    }
   
    if (!isFromActionSelect) {
    	// To convertFormattedTextToPlaintext for the fields that have been through convertPlaintextToFormattedTextNoHighUnicode
    	FormattedText formattedText = ComponentManager.get(FormattedText.class);
    	assessmentSettings.setTitle(formattedText.convertFormattedTextToPlaintext(assessmentSettings.getTitle()));
    	assessmentSettings.setAuthors(formattedText.convertFormattedTextToPlaintext(assessmentSettings.getAuthors()));
    	assessmentSettings.setFinalPageUrl(formattedText.convertFormattedTextToPlaintext(assessmentSettings.getFinalPageUrl()));
    	assessmentSettings.setBgColor(formattedText.convertFormattedTextToPlaintext(assessmentSettings.getBgColor()));
    	assessmentSettings.setBgImage(formattedText.convertFormattedTextToPlaintext(assessmentSettings.getBgImage()));
    	assessmentSettings.setKeywords(formattedText.convertFormattedTextToPlaintext(assessmentSettings.getKeywords()));
    	assessmentSettings.setObjectives(formattedText.convertFormattedTextToPlaintext(assessmentSettings.getObjectives()));
    	assessmentSettings.setRubrics(formattedText.convertFormattedTextToPlaintext(assessmentSettings.getRubrics()));
    	assessmentSettings.setPassword(formattedText.convertFormattedTextToPlaintext(StringUtils.trim(assessmentSettings.getPassword())));
    }
    
    if (error){
      assessmentSettings.setOutcomePublish("editAssessmentSettings");
      author.setIsErrorInSettings(true);
      return;
    }

    //#3 now u can proceed to save core assessment
    if (!isFromActionSelect) {
    	assessment = s.save(assessmentSettings, true);

    	//unEscape the TextFormat.convertPlaintextToFormattedTextNoHighUnicode in s.save()
    	assessment.setTitle(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(assessment.getTitle()));
    	assessmentSettings.setAssessment(assessment);
    }
    
    //  we need a publishedUrl, this is the url used by anonymous user
    String releaseTo = assessment.getAssessmentAccessControl().getReleaseTo();
    if (releaseTo != null) {
      // generate an alias to the pub assessment
      String alias = AgentFacade.getAgentString() + (new Date()).getTime();
      assessmentSettings.setAlias(alias);

      String server = ( (javax.servlet.http.HttpServletRequest) extContext.
			      getRequest()).getRequestURL().toString();
      int index = server.indexOf(extContext.getRequestContextPath() + "/"); // "/samigo-app/"
      server = server.substring(0, index);

      String url = server + extContext.getRequestContextPath();
      assessmentSettings.setPublishedUrl(url + "/servlet/Login?id=" + alias);

      //SAK-40811 show groups on publish from action select
      if(isFromActionSelect && "Selected Groups".equals(releaseTo) && assessmentSettings.getGroupsAuthorized() != null){
        String[] groupsAuthorized = assessmentSettings.getGroupsAuthorized();					
        String result = Arrays.stream(groupsAuthorized)
			                  .map(group -> getGroupName(group, assessmentSettings))
			                  .collect(Collectors.joining(", "));
        assessmentSettings.setReleaseToGroupsAsString(result);
      }
    }
   
    //#4 - before going to confirm publishing, check if the title is unique
    PublishedAssessmentService publishedService = new PublishedAssessmentService();
    if ( !publishedService.publishedAssessmentTitleIsUnique(assessmentId,assessmentName)){
      String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","published_assessment_title_not_unique_error");
      context.addMessage(null,new FacesMessage(err));
      assessmentSettings.setOutcomePublish("editAssessmentSettings");
      author.setIsErrorInSettings(true);
      return;
    }
    
    //#4 - regenerate the core assessment list in autor bean again
    // sortString can be of these value:title,releaseTo,dueDate,startDate
    // get the managed bean, author and reset the list.
    // Yes, we need to do that just in case the user change those delivery
    // dates and turning an inactive pub to active pub and then go back to assessment list page
    List assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(author.getCoreAssessmentOrderBy(),author.isCoreAscending());
	// get the managed bean, author and set the list
	author.setAssessments(assessmentList);
	
	PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
	publishRepublishNotification.setSendNotification(false);
	publishRepublishNotification.setPrePopulateText(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","pre_populate_text_publish"));
	assessmentSettings.setOutcomePublish("saveSettingsAndConfirmPublish"); // finally goto confirm
	SetFromPageAsAuthorSettingsListener setFromPageAsAuthorSettingsListener = new SetFromPageAsAuthorSettingsListener();
	setFromPageAsAuthorSettingsListener.processAction(null);
  }

  public void setIsFromActionSelect(boolean isFromActionSelect){
	  this.isFromActionSelect = isFromActionSelect;
  }  

	private String getGroupName(String groupId, AssessmentSettingsBean settings) {
		return getName(groupId, settings.getGroupsForSite());
	}

  private String getName(String parameter, SelectItem[] entries) {
	if(parameter == null || parameter.isEmpty()) {
		return "";
}

	for(SelectItem item : entries) {
		if(item.getValue().equals(parameter)) {
			return item.getLabel();
		}
	}

	return ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","extended_time_name_not_found");
  }
}
