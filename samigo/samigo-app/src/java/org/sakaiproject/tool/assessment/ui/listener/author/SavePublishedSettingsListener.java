/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SavePublishedSettingsListener
implements ActionListener
{
	private static Log log = LogFactory.getLog(SavePublishedSettingsListener.class);
	private static final GradebookServiceHelper gbsHelper =
		IntegrationContextFactory.getInstance().getGradebookServiceHelper();
	private static final boolean integrated =
		IntegrationContextFactory.getInstance().isIntegrated();

	public SavePublishedSettingsListener()
	{
	}

	public void processAction(ActionEvent ae) throws AbortProcessingException
	{
		FacesContext context = FacesContext.getCurrentInstance();
		PublishedAssessmentSettingsBean assessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean(
				"publishedSettings");
		// #1 - set Assessment
		Long assessmentId = assessmentSettings.getAssessmentId();
		log.debug("**** save assessment assessmentId ="+assessmentId.toString());
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
		PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(
				assessmentId.toString());

		boolean retractNow = false;
		String id = ae.getComponent().getId();
		// Check if the action is clicking the the "Retract" button on Assessment Retract Confirmation button
		if ("retract".equals(id)) {
			retractNow = true;
		}

		EventTrackingService.post(EventTrackingService.newEvent("sam.pubsetting.edit", "publishedAssessmentId=" + assessmentId, true));
		boolean error = checkPublishedSettings(assessmentService, assessmentSettings, context);
		
		if (error){
			assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			return;
		}
		boolean isTitleChanged = isTitleChanged(assessmentSettings, assessment);
		SaveAssessmentSettings saveAssessmentSettings = new SaveAssessmentSettings();
		setPublishedSettings(assessmentSettings, assessment, retractNow, saveAssessmentSettings);
		
		boolean gbError = checkScore(assessmentSettings, assessment, context);
		if (gbError){
			assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			return;
		}

		updateGB(assessmentSettings, assessment, isTitleChanged);
		
		assessment.setLastModifiedBy(AgentFacade.getAgentString());
		assessment.setLastModifiedDate(new Date());
		assessmentService.saveAssessment(assessment); 
		
		saveAssessmentSettings.updateAttachment(assessment.getAssessmentAttachmentList(), assessmentSettings.getAttachmentList(),(AssessmentIfc)assessment.getData(), false);
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubSetting.edit", "pubAssessmentId=" + assessmentSettings.getAssessmentId(), true));
	    
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		if ("editAssessment".equals(author.getFromPage())) {
			// If go back to edit assessment page, need to refresh the title
			AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
			assessmentBean.setTitle(assessmentSettings.getTitle());
		}
		else {
			resetPublishedAssessmentsList(author, assessmentService);
		}
		assessmentSettings.setOutcome(author.getFromPage());
	}

	public boolean checkPublishedSettings(PublishedAssessmentService assessmentService, PublishedAssessmentSettingsBean assessmentSettings, FacesContext context) {
		boolean error = false;
		// Title
		String assessmentName = FormattedText.convertPlaintextToFormattedText(assessmentSettings.getTitle().trim());
		// check if name is empty
		if(assessmentName != null &&(assessmentName.trim()).equals("")){
			String nameEmpty_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
			context.addMessage(null, new FacesMessage(nameEmpty_err));
			error=true;
		}

		// check if name is unique 
		if(!assessmentService.publishedAssessmentTitleIsUnique(assessmentSettings.getAssessmentId().toString(), assessmentName)){
			String nameUnique_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
			context.addMessage(null, new FacesMessage(nameUnique_err));
			error=true;
		}

		// check if start date is valid
		if(!assessmentSettings.getIsValidStartDate()){
			String startDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_start_date");
			context.addMessage(null, new FacesMessage(startDateErr));
			error=true;
		}

		// check if due date is valid
		if(!assessmentSettings.getIsValidDueDate()){
			String dueDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_due_date");
			context.addMessage(null,new FacesMessage(dueDateErr));
			error=true;
		}
		// check if retract date is valid
		if(!assessmentSettings.getIsValidRetractDate()){
			String retractDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_retrack_date");
			context.addMessage(null,new FacesMessage(retractDateErr));
			error=true;
		}

		// if timed assessment, does it has value for time
		Object time = assessmentSettings.getValueMap().get("hasTimeAssessment");
		boolean isTime = false;
		try
		{
			if (time != null)
			{
				isTime = ((Boolean) time).booleanValue();
			}
		}
		catch (Exception ex)
		{
			// keep default
			log.warn("Expecting Boolean hasTimeAssessment, got: " + time + ", exception: " + ex);
		}
		if(isTime && (assessmentSettings.getTimeLimit().intValue())==0){
			String time_err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages", "timeSelect_error");
			context.addMessage(null, new FacesMessage(time_err));
			error = true;
		}

		// check submissions
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

		// check feedback - if at specific time then time should be defined.
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

		return error;
	}
	
	// Check if title has been changed. If yes, update it.
	private boolean isTitleChanged(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment) {
		if (assessment.getTitle() != null && assessmentSettings.getTitle() != null) {
			String assessmentTitle = FormattedText.convertPlaintextToFormattedText(assessmentSettings.getTitle().trim());
				if (!assessment.getTitle().trim().equals(assessmentTitle)) {
					assessment.setTitle(assessmentTitle);
					return true;
				}
		}
		return false;
	}
	
	private void setPublishedSettings(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, boolean retractNow, SaveAssessmentSettings saveAssessmentSettings) {
		// Title is set in isTitleChanged()
		assessment.setDescription(assessmentSettings.getDescription());
	    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.AUTHORS, assessmentSettings.getAuthors());
	    
		PublishedAccessControl control = (PublishedAccessControl)assessment.getAssessmentAccessControl();
		if (control == null){
			control = new PublishedAccessControl();
			// need to fix accessControl so it can take AssessmentFacade later
			control.setAssessmentBase(assessment.getData());
		}
		// set startDate, dueDate, retractDate 
		control.setStartDate(assessmentSettings.getStartDate());
		control.setDueDate(assessmentSettings.getDueDate());
		if (retractNow)
		{
			control.setRetractDate(new Date());
		}
		else {
			control.setRetractDate(assessmentSettings.getRetractDate());
		}

		// set Assessment Orgainzation
		if (assessmentSettings.getItemNavigation()!=null ) {
			String nav = assessmentSettings.getItemNavigation();
			if ("1".equals(nav)) {
				assessmentSettings.setAssessmentFormat("1");
			}
			control.setItemNavigation(new Integer(nav));
		}
		if (assessmentSettings.getAssessmentFormat() != null ) {
			control.setAssessmentFormat(new Integer(assessmentSettings.getAssessmentFormat()));
		}	    
		if (assessmentSettings.getItemNumbering() != null) {
			control.setItemNumbering(new Integer(assessmentSettings.getItemNumbering()));
		}

		// set Timed Assessment
		control.setTimeLimit(assessmentSettings.getTimeLimit());
		if (assessmentSettings.getTimedAssessment()) {
			control.setTimedAssessment(AssessmentAccessControl.TIMED_ASSESSMENT);
		}
		else {
			control.setTimedAssessment(AssessmentAccessControl.DO_NOT_TIMED_ASSESSMENT);
		}

		// set Submissions
		if (assessmentSettings.getUnlimitedSubmissions()!=null){
			if (!assessmentSettings.getUnlimitedSubmissions().
					equals(AssessmentAccessControlIfc.UNLIMITED_SUBMISSIONS.toString())) {
				control.setUnlimitedSubmissions(Boolean.FALSE);
				if (assessmentSettings.getSubmissionsAllowed() != null)
					control.setSubmissionsAllowed(new Integer(assessmentSettings.
							getSubmissionsAllowed()));
				else
					control.setSubmissionsAllowed(new Integer("1"));
			}
			else {
				control.setUnlimitedSubmissions(Boolean.TRUE);
				control.setSubmissionsAllowed(null);
			}
		}

		if (assessmentSettings.getLateHandling()!=null){
			control.setLateHandling(new Integer(assessmentSettings.
					getLateHandling()));
		}
		if (assessmentSettings.getSubmissionsSaved()!=null){
			control.setSubmissionsSaved(new Integer(assessmentSettings.getSubmissionsSaved()));
		}
		
		if (assessmentSettings.getAutoSubmit())
	        control.setAutoSubmit(AssessmentAccessControl.AUTO_SUBMIT);
	    else {
	    	control.setAutoSubmit(AssessmentAccessControl.DO_NOT_AUTO_SUBMIT);
	    }
		assessment.setAssessmentAccessControl(control);

		// set Feedback
		AssessmentFeedbackIfc feedback = (AssessmentFeedbackIfc) assessment.getAssessmentFeedback();
		if (feedback == null){
			feedback = new AssessmentFeedback();
			// need to fix feeback so it can take AssessmentFacade later
			feedback.setAssessmentBase(assessment.getData());
		}
		// Feedback authoring
		if (assessmentSettings.getFeedbackAuthoring()!=null)
			feedback.setFeedbackAuthoring(new Integer(assessmentSettings.getFeedbackAuthoring()));
		// Feedback delivery
		if (assessmentSettings.getFeedbackDelivery()!=null)
			feedback.setFeedbackDelivery(new Integer(assessmentSettings.getFeedbackDelivery()));
		control.setFeedbackDate(assessmentSettings.getFeedbackDate());
		// Feedback Components Students Can See
		feedback.setShowStudentResponse(Boolean.valueOf(assessmentSettings.getShowStudentResponse()));
		feedback.setShowCorrectResponse(Boolean.valueOf(assessmentSettings.getShowCorrectResponse()));
		feedback.setShowStudentScore(Boolean.valueOf(assessmentSettings.getShowStudentScore()));
		feedback.setShowStudentQuestionScore(Boolean.valueOf(assessmentSettings.getShowStudentQuestionScore()));
		feedback.setShowQuestionLevelFeedback(Boolean.valueOf(assessmentSettings.getShowQuestionLevelFeedback()));
		feedback.setShowSelectionLevelFeedback(Boolean.valueOf(assessmentSettings.getShowSelectionLevelFeedback()));
		feedback.setShowGraderComments(Boolean.valueOf(assessmentSettings.getShowGraderComments()));
		feedback.setShowStatistics(Boolean.valueOf(assessmentSettings.getShowStatistics()));
		assessment.setAssessmentFeedback(feedback);

		// set Grading
		EvaluationModelIfc evaluation = (EvaluationModelIfc) assessment.getEvaluationModel();
		if (evaluation == null){
			evaluation = new EvaluationModel();
			evaluation.setAssessmentBase(assessment.getData());
		}
		if (assessmentSettings.getAnonymousGrading() != null) {
			evaluation.setAnonymousGrading(new Integer(assessmentSettings.getAnonymousGrading()));
		}	    
		// If there is value set for toDefaultGradebook, we reset it
		// Otherwise, do nothing
		if (assessmentSettings.getToDefaultGradebook() != null) {
			evaluation.setToGradeBook(assessmentSettings.getToDefaultGradebook());
		}
		if (assessmentSettings.getScoringType() != null) {
			evaluation.setScoringType(new Integer(assessmentSettings.getScoringType()));
		}
		assessment.setEvaluationModel(evaluation);

		// update ValueMap: it contains value for thh checkboxes in
		// publishedSettings.jsp for: hasAvailableDate, hasDueDate,
		// hasRetractDate, hasAnonymous, hasAuthenticatedUser, hasIpAddress,
		// hasUsernamePassword, hasTimeAssessment,hasAutoSubmit, hasPartMetaData, 
		// hasQuestionMetaData
		HashMap h = assessmentSettings.getValueMap();
		saveAssessmentSettings.updateMetaWithValueMap(assessment, h);
	}

	public boolean checkScore(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, FacesContext context) {
		// check if the score is > 0, Gradebook doesn't allow assessments with total
		// point = 0.
		boolean gbError = false;

		if (assessmentSettings.getToDefaultGradebook() != null && assessmentSettings.getToDefaultGradebook().equals("1")) {
			if (assessment.getTotalScore().floatValue() <= 0) {
				String gb_err = (String) ContextUtil.getLocalizedString(
						"org.sakaiproject.tool.assessment.bundle.AuthorMessages","gradebook_exception_min_points");
				context.addMessage(null, new FacesMessage(gb_err));
				gbError = true;
			}
		}
		return gbError;
	}

	public void updateGB(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment, boolean isTitleChanged) {
		//#3 - add or remove external assessment to gradebook
		// a. if Gradebook does not exists, do nothing, 'cos setting should have been hidden
		// b. if Gradebook exists, just call addExternal and removeExternal and swallow any exception. The
		//    exception are indication that the assessment is already in the Gradebook or there is nothing
		//    to remove.
		GradebookService g = null;
		if (integrated)
		{
			g = (GradebookService) SpringBeanLocator.getInstance().
			getBean("org.sakaiproject.service.gradebook.GradebookService");
		}

		if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g)){ // => something to do
			PublishedEvaluationModel evaluation = (PublishedEvaluationModel)assessment.getEvaluationModel();
			//Integer scoringType = EvaluationModelIfc.HIGHEST_SCORE;
			if (evaluation == null){
				evaluation = new PublishedEvaluationModel();
				evaluation.setAssessmentBase(assessment.getData());
			}
			// If there is value set for toDefaultGradebook, we reset it
			// Otherwise, do nothing
			if (assessmentSettings.getToDefaultGradebook() != null) {
				evaluation.setToGradeBook(assessmentSettings.getToDefaultGradebook());
			}

			// If the assessment is retracted for edit, we don't sync with gradebook (only until it is republished)
			if(AssessmentBaseIfc.RETRACT_FOR_EDIT_STATUS.equals(assessment.getStatus())) {
				return;
			}
			Integer scoringType = evaluation.getScoringType();
			if (evaluation.getToGradeBook()!=null && 
					evaluation.getToGradeBook().equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())){
				if (isTitleChanged) {
					// Because GB use title instead of id, we remove and re-add to GB if title changes.
					try {
						log.debug("before gbsHelper.removeGradebook()");
						gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(), assessment.getPublishedAssessmentId().toString(), g);
					} catch (Exception e1) {
						// Should be the external assessment doesn't exist in GB. So we quiet swallow the exception. Please check the log for the actual error.
						log.info("Exception thrown in updateGB():" + e1.getMessage());
					}
				}

				try{
					log.debug("before gbsHelper.addToGradebook()");
					gbsHelper.addToGradebook((PublishedAssessmentData)assessment.getData(), g);
					
					// any score to copy over? get all the assessmentGradingData and copy over
					GradingService gradingService = new GradingService();

					// need to decide what to tell gradebook
					List list = null;

					if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
						list = gradingService.getHighestSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
					}
					else {
						list = gradingService.getLastSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
					}

					//ArrayList list = gradingService.getAllSubmissions(assessment.getPublishedAssessmentId().toString());
					log.debug("list size =" + list.size()	);
					for (int i=0; i<list.size();i++){
						try {
							AssessmentGradingData ag = (AssessmentGradingData)list.get(i);
							log.debug("ag.scores " + ag.getTotalAutoScore());
							gbsHelper.updateExternalAssessmentScore(ag, g);
						}
						catch (Exception e) {
							log.warn("Exception occues in " + i + "th record. Message:" + e.getMessage());
						}
					}
				}
				catch(Exception e){
					log.warn("oh well, must have been added already:"+e.getMessage());
				}
			}
			else{ //remove
				try{
					gbsHelper.removeExternalAssessment(
							GradebookFacade.getGradebookUId(),
							assessment.getPublishedAssessmentId().toString(), g);
				}
				catch(Exception e){
					log.warn("*** oh well, looks like there is nothing to remove:"+e.getMessage());
				}
			}
		}
	}

	public void resetPublishedAssessmentsList(AuthorBean author,
			PublishedAssessmentService assessmentService) {
		AuthorActionListener authorActionListener = new AuthorActionListener();
		GradingService gradingService = new GradingService();
		authorActionListener.prepareAllPublishedAssessmentsList(author, gradingService, assessmentService);
	}
}


