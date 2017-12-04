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



package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.RetakeAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.SubmissionStatusBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;

/**
 * <p>Description: Action Listener for displaying Submission Status for anonymnous grading</p>
 * @version $Id$
 */

@Slf4j
public class SubmissionStatusListener
  implements ActionListener, ValueChangeListener
{
  //private static EvaluationListenerUtil util;
  private static BeanSort bs;
  //private static ContextUtil cu;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    SubmissionStatusBean bean = (SubmissionStatusBean) ContextUtil.lookupBean("submissionStatus");
    TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");

    // we probably want to change the poster to be consistent
    String publishedId = ContextUtil.lookupParam("publishedId");

    // Reset the search field
    String defaultSearchString = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "search_default_student_search_string");
    bean.setSearchString(defaultSearchString);
    
    if (!submissionStatus(publishedId, bean, totalScoresBean, false))
    {
      throw new RuntimeException("failed to call submissionStatus.");
    }
  }

  /**
   * Process a value change.
   */
  public void processValueChange(ValueChangeEvent event)
  {
    log.debug("QuestionScore CHANGE LISTENER.");
    SubmissionStatusBean bean = (SubmissionStatusBean) ContextUtil.lookupBean("submissionStatus");
    TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
    QuestionScoresBean questionbean = (QuestionScoresBean) ContextUtil.lookupBean("questionScores");
    
    // we probably want to change the poster to be consistent
    String publishedId = ContextUtil.lookupParam("publishedId");

    String selectedvalue= (String) event.getNewValue();
    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
      if (event.getComponent().getId().indexOf("sectionpicker") >-1 )
      {
        bean.setSelectedSectionFilterValue(selectedvalue);   // changed section pulldown
        totalScoresBean.setSelectedSectionFilterValue(selectedvalue);
        questionbean.setSelectedSectionFilterValue(selectedvalue);
      }
    }

    if (!submissionStatus(publishedId, bean, totalScoresBean, false))
    {
      throw new RuntimeException("failed to call questionScores.");
    }
  }


  /**
   * This will populate the SubmissionStatusBean with the data associated with the
   * particular versioned assessment based on the publishedId.
   *
   * @todo Some of this code will change when we move this to Hibernate persistence.
   * @param publishedId String
   * @param bean SubmissionStatusBean
   * @return boolean
   */
  public boolean submissionStatus(
    String publishedId, SubmissionStatusBean bean, TotalScoresBean totalScoresBean, boolean isValueChange)
  {
    log.debug("submissionStatus()");
    try
    {
      TotalScoreListener totalScorelistener = new TotalScoreListener();

      GradingService delegate =	new GradingService();

      if (ContextUtil.lookupParam("sortBy") != null &&
          !ContextUtil.lookupParam("sortBy").trim().equals(""))
        bean.setSortType(ContextUtil.lookupParam("sortBy"));

      boolean sortAscending = true;
      if (ContextUtil.lookupParam("sortAscending") != null &&
      		!ContextUtil.lookupParam("sortAscending").trim().equals("")){
      	sortAscending = Boolean.valueOf(ContextUtil.lookupParam("sortAscending")).booleanValue();
      	bean.setSortAscending(sortAscending);
      	log.debug("submissionStatus() :: sortAscending = " + sortAscending);
      }

      bean.setPublishedId(publishedId);

      totalScoresBean.setSelectedSectionFilterValue(bean.getSelectedSectionFilterValue());

      // we are only interested in showing last submissions

      List scores = delegate.getLastSubmittedAssessmentGradingList(new Long(publishedId));
      List agents = new ArrayList();
      Iterator iter = scores.iterator();
      if (!iter.hasNext())
      {
        // this section has no students
      bean.setAgents(agents);
      bean.setAllAgents(agents);
      bean.setTotalPeople(Integer.toString(bean.getAgents().size()));
      return true;
      }

      Object next = iter.next();
      //Date dueDate = null;

      // Collect a list of all the users in the scores list
      Map useridMap= totalScoresBean.getUserIdMap(TotalScoresBean.CALLED_FROM_SUBMISSION_STATUS_LISTENER);


      List agentUserIds = totalScorelistener.getAgentIds(useridMap);
      AgentHelper helper = IntegrationContextFactory.getInstance().getAgentHelper();
      Map userRoles = helper.getUserRolesFromContextRealm(agentUserIds);


      // Okay, here we get the first result set, which has a summary of
      // information and a pointer to the graded assessment we should be
      // displaying.  We get the graded assessment.
      AssessmentGradingData data = (AssessmentGradingData) next;
      PublishedAssessmentService pubService = new PublishedAssessmentService();
      PublishedAssessmentIfc pub = (PublishedAssessmentIfc) pubService.getPublishedAssessment(data.getPublishedAssessmentId().toString());
      if (pub != null)
      {
        bean.setAssessmentName(pub.getTitle());
      }

      if (ContextUtil.lookupParam("roleSelection") != null)
      {
        bean.setRoleSelection(ContextUtil.lookupParam("roleSelection"));
      }

      if (bean.getSortType() == null)
      {
          bean.setSortType("agentEid");
      }


      /* Dump the grading and agent information into AgentResults */
      List students_submitted= new ArrayList();
      iter = scores.iterator();
      Map studentGradingSummaryDataMap = new HashMap();
      RetakeAssessmentBean retakeAssessment = (RetakeAssessmentBean) ContextUtil.lookupBean("retakeAssessment");
      while (iter.hasNext())
      {
        AgentResults results = new AgentResults();
        AssessmentGradingData gdata = (AssessmentGradingData) iter.next();
        gdata.setItemGradingSet(new HashSet());
        BeanUtils.copyProperties(results, gdata);

        results.setAssessmentGradingId(gdata.getAssessmentGradingId());
        String agentid =  gdata.getAgentId();
        AgentFacade agent = new AgentFacade(agentid);
        results.setLastName(agent.getLastName());
        results.setFirstName(agent.getFirstName());
        results.setEmail(agent.getEmail());
        if (results.getLastName() != null &&
            results.getLastName().length() > 0)
          results.setLastInitial(results.getLastName().substring(0,1));
        else if (results.getFirstName() != null &&
                 results.getFirstName().length() > 0)
          results.setLastInitial(results.getFirstName().substring(0,1));
        else
          results.setLastInitial("A");
        results.setIdString(agent.getIdString());
        results.setAgentEid(agent.getEidString());
        results.setAgentDisplayId(agent.getDisplayIdString());
        results.setRole((String)userRoles.get(agentid));
        results.setRetakeAllowed(getRetakeAllowed(agent.getIdString(), studentGradingSummaryDataMap, retakeAssessment));
        if (useridMap.containsKey(agentid) ) {
          agents.add(results);
          students_submitted.add(agentid);
        }
      }
      retakeAssessment.setStudentGradingSummaryDataMap(studentGradingSummaryDataMap);

      List students_not_submitted= new ArrayList();
      Iterator useridIterator = useridMap.keySet().iterator();
      while (useridIterator.hasNext()) {
        String userid = (String) useridIterator.next();
        if (!students_submitted.contains(userid)) {
          students_not_submitted.add(userid);
        }
      }
      prepareNotSubmittedAgentResult(students_not_submitted.iterator(), agents, userRoles, retakeAssessment, studentGradingSummaryDataMap);
      bs = new BeanSort(agents, bean.getSortType());
      if (
        (bean.getSortType()).equals("assessmentGradingId") )
      {
        bs.toNumericSort();
      } else {
        bs.toStringSort();
      }

      if (sortAscending) {
      	log.debug("TotalScoreListener: setRoleAndSortSection() :: sortAscending");
      	agents = (List)bs.sort();
      }
      else {
      	log.debug("TotalScoreListener: setRoleAndSortSection() :: !sortAscending");
      	agents = (List)bs.sortDesc();
      }
      
      bean.setAgents(agents);
      bean.setAllAgents(agents);
      bean.setTotalPeople(Integer.toString(bean.getAgents().size()));
    }

    catch (RuntimeException e)
    {
      log.error(e.getMessage(), e);
      return false;
    } catch (IllegalAccessException e) {
		log.error(e.getMessage(), e);
		return false;
	} catch (InvocationTargetException e) {
		log.error(e.getMessage(), e);
		return false;
	}

    return true;
  }


  //add those students that have not submitted scores, need to display them
  // in the UI 
  public void prepareNotSubmittedAgentResult(Iterator notsubmitted_iter, List agents, Map userRoles, RetakeAssessmentBean retakeAssessment, Map studentGradingSummaryDataMap){
    while (notsubmitted_iter.hasNext()){
      String studentid = (String) notsubmitted_iter.next();
      AgentResults results = new AgentResults();
      AgentFacade agent = new AgentFacade(studentid);
      results.setLastName(agent.getLastName());
      results.setFirstName(agent.getFirstName());
      results.setEmail(agent.getEmail());
      if (results.getLastName() != null &&
        results.getLastName().length() > 0)
      {
        results.setLastInitial(results.getLastName().substring(0,1));
      }
      else if (results.getFirstName() != null &&
               results.getFirstName().length() > 0)
      {
        results.setLastInitial(results.getFirstName().substring(0,1));
      }
      else
      {
        results.setLastInitial("Anonymous");
      }
      results.setIdString(agent.getIdString());
      results.setAgentEid(agent.getEidString());
      results.setAgentDisplayId(agent.getDisplayIdString());
      results.setRole((String)userRoles.get(studentid));
      results.setRetakeAllowed(getRetakeAllowed(agent.getIdString(), studentGradingSummaryDataMap, retakeAssessment));
      retakeAssessment.setStudentGradingSummaryDataMap(studentGradingSummaryDataMap);
      agents.add(results);
    }
  }
  
  public boolean getRetakeAllowed(String agentId, Map studentGradingSummaryDataMap, RetakeAssessmentBean retakeAssessment) {
	    TotalScoresBean totalScoresBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
	    PublishedAssessmentData publishedAssessmentData = totalScoresBean.getPublishedAssessment();
	    PublishedAssessmentService pubService = new PublishedAssessmentService();
	    int totalSubmitted = (pubService.getTotalSubmission(agentId, publishedAssessmentData.getPublishedAssessmentId().toString())).intValue();
		//List allAssessmentGradingList = gradingService.getAllAssessmentGradingByAgentId(publishedAssessmentData.getPublishedAssessmentId(), agentId);
		//int totalSubmitted = allAssessmentGradingList.size();
		AssessmentAccessControlIfc assessmentAccessControl = publishedAssessmentData.getAssessmentAccessControl();
		Date currentDate = new Date();
		Date startDate = assessmentAccessControl.getStartDate();
		Date dueDate = assessmentAccessControl.getDueDate();
		GradingService gradingService = new GradingService();
		List studentGradingSummaryDataList = gradingService.getStudentGradingSummaryData(publishedAssessmentData.getPublishedAssessmentId(), agentId);
		StudentGradingSummaryData studentGradingSummaryData = null;
		int numberRetake = 0;
		if (studentGradingSummaryDataList.size() != 0) {
			studentGradingSummaryData = (StudentGradingSummaryData) studentGradingSummaryDataList.get(0);
			studentGradingSummaryDataMap.put(agentId, studentGradingSummaryData);
			retakeAssessment.setStudentGradingSummaryData(studentGradingSummaryData);
			numberRetake = studentGradingSummaryData.getNumberRetake().intValue();
		}
		else {
			retakeAssessment.setStudentGradingSummaryData(null);
		}
			
		boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(assessmentAccessControl.getLateHandling());
		if (startDate == null || startDate.before(currentDate)) {
			if (dueDate != null && dueDate.before(currentDate)) {
				if (acceptLateSubmission) {
					// no submission at all, there will be one more last chance for student to submit
					// therefore, don't show the retake
					if (totalSubmitted == 0) { 
						return false;
					}
				}
				// if there are submission(s) and already retake issued to the student, 
				// if the student's acutalNumberRetake is the same as numberRetake issued by the instructor
				// we can display the retake link to allow the instructor to give out another chance to the student
				int actualNumberRetake = gradingService.getActualNumberRetake(publishedAssessmentData.getPublishedAssessmentId(), agentId);
				if (actualNumberRetake == numberRetake) {
					return true;
				}
			}
			else {
				int maxSubmissionsAllowed = 9999;
				if ((Boolean.FALSE).equals(assessmentAccessControl.getUnlimitedSubmissions())) {
					maxSubmissionsAllowed = assessmentAccessControl.getSubmissionsAllowed().intValue();
				}
				if ((totalSubmitted >= maxSubmissionsAllowed + numberRetake)) {
					return true;
				}
			}
		}
		return false;
	} 
}
