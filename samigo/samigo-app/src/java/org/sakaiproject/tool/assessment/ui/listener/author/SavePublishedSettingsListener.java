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
import java.util.Date;
import java.util.HashMap;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
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
    //log.info("**** save assessment assessmentId ="+assessmentId.toString());
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
    
    String id = ae.getComponent().getId();
    // Check if the action is clicking the the Retract button on Assessment Retract Confirmation button
    if (id.equals("retract")) {
    	control.setRetractDate(new Date());
    }
    else {
    	control.setRetractDate(assessmentSettings.getRetractDate());
    }
   	// a. LATER set dueDate, startDate, releaseTo
   	control.setStartDate(assessmentSettings.getStartDate());
   	control.setDueDate(assessmentSettings.getDueDate());
   	control.setFeedbackDate(assessmentSettings.getFeedbackDate());

    
    // check if the score is > 0, Gradebook doesn't allow assessments with total
	// point = 0.
		boolean error = false;

		if (assessmentSettings.getToDefaultGradebook().equals("1")) {
			if (assessment.getTotalScore().floatValue() <= 0) {

				String gb_err = (String) ContextUtil.getLocalizedString(
								"org.sakaiproject.tool.assessment.bundle.AuthorMessages","gradebook_exception_min_points");
				context.addMessage(null, new FacesMessage(gb_err));
				error = true;
			}
		}

		   if (error){
			      assessmentSettings.setOutcome("editPublishedAssessmentSettings");
			      return;
			    }


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
      evaluation.setToGradeBook(assessmentSettings.getToDefaultGradebook());
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
          ArrayList list = null;
          
          if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
        	  list = gradingService.getHighestAssessmentGradingList(assessment.getPublishedAssessmentId());
          }
          else {
           list = gradingService.getLastAssessmentGradingList(assessment.getPublishedAssessmentId());
          }
          
          //ArrayList list = gradingService.getAllSubmissions(assessment.getPublishedAssessmentId().toString());
          log.debug("list size =" + list.size()	);
          for (int i=0; i<list.size();i++){
         	   
            AssessmentGradingData ag = (AssessmentGradingData)list.get(i);
            log.debug("ag.scores " + ag.getTotalAutoScore());
            gbsHelper.updateExternalAssessmentScore(ag, g);
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

    assessmentService.saveAssessment(assessment);

    //#4 - regenerate the publsihed assessment list in autor bean again
    // sortString can be of these value:title,releaseTo,dueDate,startDate
    // get the managed bean, author and reset the list.
    // Yes, we need to do that just in case the user change those delivery
    // dates and turning an inactive pub to active pub
    GradingService gradingService = new GradingService();
    HashMap map = gradingService.getSubmissionSizeOfAllPublishedAssessments();
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
                       "author");
    ArrayList publishedList = assessmentService.
        getBasicInfoOfAllActivePublishedAssessments(author.getPublishedAssessmentOrderBy(),author.isPublishedAscending());
    // get the managed bean, author and set the list
    author.setPublishedAssessments(publishedList);
    setSubmissionSize(publishedList, map);

    ArrayList inactivePublishedList = assessmentService.
        getBasicInfoOfAllInActivePublishedAssessments(author.getInactivePublishedAssessmentOrderBy(),author.isInactivePublishedAscending());
    // get the managed bean, author and set the list
    author.setInactivePublishedAssessments(inactivePublishedList);
    setSubmissionSize(inactivePublishedList, map);
    
    assessmentSettings.setOutcome("saveSettings");
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


