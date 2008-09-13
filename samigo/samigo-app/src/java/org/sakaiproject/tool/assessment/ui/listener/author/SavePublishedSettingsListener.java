/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

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
	    
	    // create an assessment based on the title entered and the assessment
	    // template selected
	    // #1 - set Assessment
	    Long assessmentId = assessmentSettings.getAssessmentId();
	    log.debug("**** save assessment assessmentId ="+assessmentId.toString());
	    PublishedAssessmentService assessmentService = new PublishedAssessmentService();
	    PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(
	        assessmentId.toString());
	    //log.info("** assessment = "+assessment);

	    // #2 - update delivery dates in AssessmentAccessControl
	    PublishedAccessControl control = (PublishedAccessControl)assessment.getAssessmentAccessControl();
	    if (control == null){
	        control = new PublishedAccessControl();
	        // need to fix accessControl so it can take AssessmentFacade later
	        control.setAssessmentBase(assessment.getData());
	    }
	    boolean retractNow = false;
	    String id = ae.getComponent().getId();
	    // Check if the action is clicking the the "Retract" button on Assessment Retract Confirmation button
	    if (id.equals("retract")) {
	    	retractNow = true;
	    }

	EventTrackingService.post(EventTrackingService.newEvent("sam.pubsetting.edit", "publishedAssessmentId=" + assessmentId, true));
    boolean error = setPublishedSettings(assessmentSettings, context, control, assessment, retractNow);
    if (error){
        assessmentSettings.setOutcome("editPublishedAssessmentSettings");
        return;
    }

    boolean gbError = checkScore(assessmentSettings, assessment, context);
    if (gbError){
    	assessmentSettings.setOutcome("editPublishedAssessmentSettings");
		return;
    }
    
    updateGB(assessmentSettings, assessment);
    
    assessmentService.saveAssessment(assessment);

    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    resetPublishedAssessmentsList(author, assessmentService);
        
    assessmentSettings.setOutcome(author.getFromPage());
  }
 
  public boolean setPublishedSettings(PublishedAssessmentSettingsBean assessmentSettings, FacesContext context, PublishedAccessControl control, PublishedAssessmentFacade assessment, boolean retractNow) {
	    boolean error = false;
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
	    // check if retract date is valid
	    if(!assessmentSettings.getIsValidRetractDate()){
	    	String retractDateErr = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","invalid_retrack_date");
	    	context.addMessage(null,new FacesMessage(retractDateErr));
	    	error=true;
	    }
	    
	   	// LATER set dueDate, startDate, retractDate 
	   	control.setStartDate(assessmentSettings.getStartDate());
	   	control.setDueDate(assessmentSettings.getDueDate());
	   	if (retractNow)
	   	{
	   		control.setRetractDate(new Date());
	   	}
	   	else {
	   		control.setRetractDate(assessmentSettings.getRetractDate());
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
	   	control.setFeedbackDate(assessmentSettings.getFeedbackDate());

	   	
	    //#3 Feedback
	   	AssessmentFeedbackIfc feedback = (AssessmentFeedbackIfc) assessment.getAssessmentFeedback();
	    if (feedback == null){
	      feedback = new AssessmentFeedback();
	      // need to fix feeback so it can take AssessmentFacade later
	      feedback.setAssessmentBase(assessment.getData());
	    }
	    if (assessmentSettings.getFeedbackDelivery()!=null)
	     feedback.setFeedbackDelivery(new Integer(assessmentSettings.getFeedbackDelivery()));
	    
	    feedback.setShowStudentResponse(Boolean.valueOf(assessmentSettings.getShowStudentResponse()));
	    feedback.setShowCorrectResponse(Boolean.valueOf(assessmentSettings.getShowCorrectResponse()));
	    feedback.setShowStudentScore(Boolean.valueOf(assessmentSettings.getShowStudentScore()));
	    feedback.setShowStudentQuestionScore(Boolean.valueOf(assessmentSettings.getShowStudentQuestionScore()));
	    feedback.setShowQuestionLevelFeedback(Boolean.valueOf(assessmentSettings.getShowQuestionLevelFeedback()));
	    feedback.setShowSelectionLevelFeedback(Boolean.valueOf(assessmentSettings.getShowSelectionLevelFeedback()));
	    feedback.setShowGraderComments(Boolean.valueOf(assessmentSettings.getShowGraderComments()));
	    feedback.setShowStatistics(Boolean.valueOf(assessmentSettings.getShowStatistics()));
	    assessment.setAssessmentFeedback(feedback);

	    return error;
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

  public void updateGB(PublishedAssessmentSettingsBean assessmentSettings, PublishedAssessmentFacade assessment) {
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
	        //add and copy scores over if any

	        try{
	         log.debug("before gbsHelper.addToGradebook()");
	         gbsHelper.addToGradebook((PublishedAssessmentData)assessment.getData(), g);
	         log.debug("before gbsHelper.updateGradebook()");
	         gbsHelper.updateGradebook((PublishedAssessmentData)assessment.getData(), g);
	          // any score to copy over? get all the assessmentGradingData and copy over
	          GradingService gradingService = new GradingService();
	          
	           // need to decide what to tell gradebook
	          List list = null;
	          
	          if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
	        	  list = gradingService.getHighestSubmittedAssessmentGradingList(assessment.getPublishedAssessmentId());
	          }
	          else {
	           list = gradingService.getLastSubmittedAssessmentGradingList(assessment.getPublishedAssessmentId());
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
		// #4 - regenerate the publsihed assessment list in autor bean again
		// sortString can be of these value:title,releaseTo,dueDate,startDate
		// get the managed bean, author and reset the list.
		// Yes, we need to do that just in case the user change those delivery
		// dates and turning an inactive pub to active pub
		GradingService gradingService = new GradingService();
		HashMap map = gradingService
				.getSubmissionSizeOfAllPublishedAssessments();
		ArrayList publishedList = assessmentService
				.getBasicInfoOfAllActivePublishedAssessments(author
						.getPublishedAssessmentOrderBy(), author
						.isPublishedAscending());
		// get the managed bean, author and set the list
		author.setPublishedAssessments(publishedList);
		setSubmissionSize(publishedList, map);

		ArrayList inactivePublishedList = assessmentService
				.getBasicInfoOfAllInActivePublishedAssessments(author
						.getInactivePublishedAssessmentOrderBy(), author
						.isInactivePublishedAscending());
		// get the managed bean, author and set the list
		author.setInactivePublishedAssessments(inactivePublishedList);
		setSubmissionSize(inactivePublishedList, map);
	}
  
  private void setSubmissionSize(ArrayList list, HashMap map){
    for (int i=0; i<list.size();i++){
      PublishedAssessmentFacade p =(PublishedAssessmentFacade)list.get(i);
      Integer size = (Integer) map.get(p.getPublishedAssessmentId());
      if (size != null){
        p.setSubmissionSize(size.intValue());
        //log.info("*** submission size" + size.intValue());
      }
    }
  }


}


