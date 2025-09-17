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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.tool.assessment.util.TimeLimitValidator;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.api.FormattedText;

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

	private GradingService gradingService;
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

    // check if RetractDate needs to be nulled if not accepting late submissions
    if (AssessmentAccessControlIfc.NOT_ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling())){
        assessmentSettings.setRetractDateString(null);
    }

    if(assessmentSettings.getDueDate() == null && assessmentSettings.getRetractDate() != null && AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling())){
        String dueDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "due_null_with_retract_date");
        context.addMessage(null,new FacesMessage(dueDateErr));
        error = true;
    }

    // check if late submission date is valid
    if(!assessmentSettings.getIsValidRetractDate()){
    	String retractDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_retrack_date");
    	context.addMessage(null,new FacesMessage(retractDateErr));
    	error=true;
    }

    // check that retract is after due and due is not null
    if (!assessmentSettings.getIsRetractAfterDue()) {
    	String retractDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "retract_earlier_than_due");
    	context.addMessage(null, new FacesMessage(retractDateErr));
    	error = true;
    }

    // if using a time limit, ensure open window is greater than or equal to time limit
    boolean hasTimer = TimeLimitValidator.hasTimer(assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes());
    if(hasTimer) {
        Date due = assessmentSettings.getRetractDate() != null && AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.toString().equals(assessmentSettings.getLateHandling()) ? assessmentSettings.getRetractDate() : assessmentSettings.getDueDate();
        boolean availableLongerThanTimer = TimeLimitValidator.availableLongerThanTimer(assessmentSettings.getStartDate(), due, assessmentSettings.getTimedHours(), assessmentSettings.getTimedMinutes(),
                                                                                        "org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "open_window_less_than_time_limit", context);
        if(!availableLongerThanTimer) {
            error = true;
        }
    }

	GradingService gradingService = (GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");

	boolean isGradebookGroupEnabled = gradingService.isGradebookGroupEnabled(AgentFacade.getCurrentSiteId());
	boolean isReleaseToSelectedGroups = assessmentSettings.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS);

	if (isGradebookGroupEnabled && !isReleaseToSelectedGroups) {
		error = true;

		String categoriesInGroups = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","multi_gradebook.release_to.error");
		context.addMessage(null,new FacesMessage(categoriesInGroups));
	}

    if (assessmentSettings.getReleaseTo().equals(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
    	String[] groupsAuthorized =  assessmentSettings.getGroupsAuthorizedToSave(); //getGroupsAuthorized();
    	if (groupsAuthorized == null || groupsAuthorized.length == 0) {
    		String releaseGroupError = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","choose_one_group");
        	context.addMessage(null,new FacesMessage(releaseGroupError));
        	error=true;
        	assessmentSettings.setNoGroupSelectedError(true);
    	}
    	else {
			List<String> groupList = Arrays.asList(groupsAuthorized);

			String siteId = assessmentSettings.getCurrentSiteId();
			String defaultToGradebook = assessmentSettings.getToDefaultGradebook();

			if (defaultToGradebook != null && isGradebookGroupEnabled) {
				if (defaultToGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {
					String categorySelected = assessmentSettings.getCategorySelected();

					if (categorySelected != null && !categorySelected.isBlank() && !categorySelected.equals("-1")) {
						List<String> selectedCategories = Arrays.asList(categorySelected.split(","));

						boolean areCategoriesInGroups =
							gradingService.checkMultiSelectorList(siteId, groupList != null ? groupList : new ArrayList<>(), selectedCategories, true);

						if (!areCategoriesInGroups) {
							error = true;
							String categoriesInGroups = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","multi_gradebook.categories.error");

							context.addMessage(null,new FacesMessage(categoriesInGroups));
						}
					}
				} else if (defaultToGradebook.equals(EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString())) {
					String gradebookName = assessmentSettings.getGradebookName();
					List<String> gradebookList = Arrays.asList(gradebookName.split(","));

					boolean areItemsInGroups =
						gradingService.checkMultiSelectorList(siteId, groupList != null ? groupList : new ArrayList<>(), gradebookList, false);

					if (!areItemsInGroups) {
						error = true;
						String itemsInGroups = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","multi_gradebook.items.error");

						context.addMessage(null,new FacesMessage(itemsInGroups));
					}
				}
			}

    		assessmentSettings.setNoGroupSelectedError(false);
    	}
    }

	//  if timed assessment, does it has value for time
	Object time=assessmentSettings.getValueMap().get("hasTimeAssessment");
	boolean isTime=false;
	try
	{
		if (time != null) {
			if (time instanceof String) {
				String timeStr = time.toString();
				if ("true".equals(timeStr)) {
					isTime = true;
				} else if ("false".equals(timeStr)) {
					isTime = false;
				}
			} else {
				isTime = ( (Boolean) time).booleanValue();
			}
		}
	}
	catch (Exception ex)
	{
		// keep default
		log.warn("Expecting Boolean or String true/false for hasTimeAssessment, got: " + time + ", exception: " + ex.getMessage());
	}
    if((isTime) &&((assessmentSettings.getTimeLimit().intValue())==0)){
	String time_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","timeSelect_error");
	context.addMessage(null,new FacesMessage(time_err));
        error=true;
    }
    
    String ipString = assessmentSettings.getIpAddresses().trim().replace(" ", "");
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
    	if (StringUtils.isBlank(assessmentSettings.getFeedbackDateString())) {
    		error=true;
    		String  date_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","date_error");
    		context.addMessage(null,new FacesMessage(date_err));
    	}
    	else {
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

    List<SelectItem> existingGradebook = assessmentSettings.getExistingGradebook();
    ToolSession currentToolSession = SessionManager.getCurrentToolSession();
    for (SelectItem item : existingGradebook) {
        if (assessmentSettings.getGradebookName().equals(item.getValue()) &&
            item.getLabel().split("\\(").length > 1 &&
            currentToolSession.getAttribute("NEW_ASSESSMENT_PREVIOUSLY_ASSOCIATED") == null) {
                error = true;
                String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "addtogradebook.previouslyAssoc");
                currentToolSession.setAttribute("NEW_ASSESSMENT_PREVIOUSLY_ASSOCIATED", Boolean.TRUE);
                context.addMessage(null,new FacesMessage(err));
        }
    }

    if (error){
      String blockDivs = ContextUtil.lookupParam("assessmentSettingsAction:blockDivs");
      assessmentSettings.setBlockDivs(blockDivs);
      assessmentSettings.setOutcomeSave("editAssessmentSettings");
      return;
    }

    currentToolSession.removeAttribute("NEW_ASSESSMENT_PREVIOUSLY_ASSOCIATED");

    // Set the outcome once Save button is clicked
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    assessmentSettings.setOutcomeSave(author.getFromPage());

    s.save(assessmentSettings, false);

    // In case the user returns to EditAssessment, the exam title will be updated
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");

    // reset the core listing in case assessment title changes
    List<AssessmentFacade> assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(author.getCoreAssessmentOrderBy(),author.isCoreAscending());
    for (AssessmentFacade assessmentFacade : assessmentList) {
        assessmentFacade.setTitle(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
        if (assessmentFacade.getAssessmentBaseId().toString().equals(assessmentBean.getAssessmentId())) {
            assessmentBean.setTitle(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
        }
    }

    // get the managed bean, author and set the list
    List allAssessments = new ArrayList<>();
    if (authorization.getEditAnyAssessment() || authorization.getEditOwnAssessment()) {
        allAssessments.addAll(assessmentList);
    }
    if (authorization.getGradeAnyAssessment() || authorization.getGradeOwnAssessment()) {
        allAssessments.addAll(author.getPublishedAssessments());
    }
    author.setAssessments(assessmentList);
    author.setAllAssessments(allAssessments);

    // goto Question Authoring page
    EditAssessmentListener editA= new EditAssessmentListener();
    editA.setPropertiesForAssessment(author);
  }
}
