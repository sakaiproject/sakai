/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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
import java.util.Map;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.StudentGradingSummaryData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class AuthorActionListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(AuthorActionListener.class);
  //private static ContextUtil cu;

  public AuthorActionListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    //FacesContext context = FacesContext.getCurrentInstance();
    //Map reqMap = context.getExternalContext().getRequestMap();
    //Map requestParams = context.getExternalContext().getRequestParameterMap();
    //log.debug("debugging ActionEvent: " + ae);
    //log.debug("debug requestParams: " + requestParams);
    //log.debug("debug reqMap: " + reqMap);
    log.debug("*****Log: inside AuthorActionListener =debugging ActionEvent: " + ae);

    // get service and managed bean
    AssessmentService assessmentService = new AssessmentService();
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    GradingService gradingService = new GradingService();
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
                       "author");

    //#1 - prepare active template list. Note that we only need the title. We don't need the
    // full template object - be cheap.
    author.setShowTemplateList(true);
    ArrayList templateList = assessmentService.getTitleOfAllActiveAssessmentTemplates();
    // get the managed bean, author and set the list
    if (templateList.size()==1){   //<= only contains Default Template
	author.setShowTemplateList(false);
    }
    else{
      // remove Default Template
      removeDefaultTemplate(templateList);
      author.setAssessmentTemplateList(templateList);
    }

    prepareAssessmentsList(author, assessmentService, gradingService, publishedAssessmentService);
    
    String s = ServerConfigurationService.getString("samigo.editPubAssessment.restricted");
	if (s != null && s.toLowerCase().equals("false")) {
		author.setEditPubAssessmentRestricted(false);
	}
	else {
		author.setEditPubAssessmentRestricted(true);
	}
  }

  public void prepareAssessmentsList(AuthorBean author, AssessmentService assessmentService, GradingService gradingService, PublishedAssessmentService publishedAssessmentService) {
	  // prepare core assessment list
	  author.setCoreAssessmentOrderBy(AssessmentFacadeQueries.TITLE);
	  ArrayList assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
			  AssessmentFacadeQueries.TITLE, author.isCoreAscending());
	  // get the managed bean, author and set the list
	  author.setAssessments(assessmentList);

	  ArrayList publishedAssessmentList = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments2(
			  PublishedAssessmentFacadeQueries.TITLE, true, AgentFacade.getCurrentSiteId());
	  HashMap agDataSizeMap = gradingService.getAGDataSizeOfAllPublishedAssessments();

	  ArrayList dividedPublishedAssessmentList = getTakeableList(publishedAssessmentList, gradingService);
	  prepareActivePublishedAssessmentsList(author, (ArrayList) dividedPublishedAssessmentList.get(0), agDataSizeMap);
	  prepareInactivePublishedAssessmentsList(author, (ArrayList) dividedPublishedAssessmentList.get(1), agDataSizeMap);
  }

  public void prepareActivePublishedAssessmentsList(AuthorBean author, ArrayList activePublishedList, HashMap agDataSizeMap) {
	  setHasAssessmentGradingData(activePublishedList, agDataSizeMap);
	  author.setPublishedAssessments(activePublishedList);  
  }
  
  public void prepareInactivePublishedAssessmentsList(AuthorBean author, ArrayList inactivePublishedList, HashMap agDataSizeMap) {	  
	  setHasAssessmentGradingData(inactivePublishedList, agDataSizeMap);
	  author.setInactivePublishedAssessments(inactivePublishedList);
	  boolean isAnyAssessmentRetractForEdit = false;
	  Iterator iter = inactivePublishedList.iterator();
	  while (iter.hasNext()) {
		  PublishedAssessmentFacade publishedAssessmentFacade = (PublishedAssessmentFacade) iter.next();
		  if (Integer.valueOf(3).equals(publishedAssessmentFacade.getStatus())) {
			  isAnyAssessmentRetractForEdit = true;
			  break;
		  }
	  }
	  if (isAnyAssessmentRetractForEdit) {
		  author.setIsAnyAssessmentRetractForEdit(true);
	  }
	  else {
		  author.setIsAnyAssessmentRetractForEdit(false);
	  }
  }
   
  private void setHasAssessmentGradingData(ArrayList list, HashMap agMap) {
      boolean hasAssessmentGradingData = true;
      for (int i = 0; i < list.size(); i++) {
              PublishedAssessmentFacade p = (PublishedAssessmentFacade) list
                              .get(i);
              if (agMap.get(p.getPublishedAssessmentId()) != null) {
                      hasAssessmentGradingData = true;
              } else {
                      hasAssessmentGradingData = false;
              }
              p.setHasAssessmentGradingData(hasAssessmentGradingData);
      }
  }
  
  private void removeDefaultTemplate(ArrayList templateList){
    for (int i=0; i<templateList.size();i++){
      AssessmentTemplateFacade a = (AssessmentTemplateFacade) templateList.get(i);
      if ((a.getAssessmentBaseId()).equals(new Long("1"))){
        templateList.remove(a);
        return;
      }
    }
  }

  public ArrayList getTakeableList(ArrayList assessmentList, GradingService gradingService) {
	  ArrayList list = new ArrayList();
	  ArrayList activeList = new ArrayList();
	  ArrayList inActiveList = new ArrayList();
	  String siteId = AgentFacade.getCurrentSiteId();
	  HashMap submissionCountHash = gradingService.getSiteSubmissionCountHash(siteId);
	  HashMap numberRetakeHash = gradingService.getSiteNumberRetakeHash(siteId);
	  HashMap actualNumberRetake = gradingService.getSiteActualNumberRetakeHash(siteId);
	  List needResubmitList = gradingService.getSiteNeedResubmitList(siteId);
	  	  
	  for (int i = 0; i < assessmentList.size(); i++) {
		  PublishedAssessmentFacade f = (PublishedAssessmentFacade)assessmentList.get(i);
		  if (isActive(f, (HashMap) submissionCountHash.get(f.getPublishedAssessmentId()), (HashMap) numberRetakeHash.get(f.getPublishedAssessmentId()), 
				  (HashMap) actualNumberRetake.get(f.getPublishedAssessmentId()), needResubmitList)) {
			  activeList.add(f);
		  }
		  else {
			  inActiveList.add(f);
		  }
	  }
	  list.add(activeList);
	  list.add(inActiveList);
	  return list;
  }

  public boolean isActive(PublishedAssessmentFacade f, HashMap submissionCountHash, HashMap numberRetakeHash, 
		  HashMap actualNumberRetakeHash, List needResubmitList) {
	  boolean returnValue = false;
	  //1. prepare our significant parameters
	  Integer status = f.getStatus();
	  Date currentDate = new Date();
	  Date startDate = f.getStartDate();
	  Date retractDate = f.getRetractDate();
	  Date dueDate = f.getDueDate();

	  if (submissionCountHash != null) {
		  f.setSubmissionSize(submissionCountHash.size());
	  }
	  else {
		  f.setSubmissionSize(0);
	  }
	  
	  if (!Integer.valueOf(1).equals(status)) {
		  return false;
	  }

	  if (startDate != null && startDate.after(currentDate)) {
		  return false;
	  }

	  if (retractDate != null && retractDate.before(currentDate)) {
		  return false;
	  }

	  if (needResubmitList.contains(f.getPublishedAssessmentId())) {
		  return true;
	  }

	  boolean acceptLateSubmission = AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION.equals(f.getLateHandling());
	  int maxSubmissionsAllowed = 9999;
	  if ((Boolean.FALSE).equals(f.getUnlimitedSubmissions())){
		  maxSubmissionsAllowed = f.getSubmissionsAllowed().intValue();
	  }
	  
	  TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
	  totalScores.setReleaseTo(f.getReleaseTo());
	  Map useridMap = null;
	  if (f.getReleaseTo() != null && !("").equals(f.getReleaseTo())) {
		  if (f.getReleaseTo().indexOf("Anonymous Users") >= 0) {
			  if (dueDate != null && dueDate.before(currentDate)) {
				  return false;
			  }
			  else {
				  return true;
			  }
		  }
		  else {
			  if (AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(f.getReleaseTo())) {
				  useridMap = totalScores.getUserIdMap(Integer.parseInt(TotalScoresBean.RELEASED_SECTIONS_GROUPS_SELECT_VALUE));
			  }
			  else {
				  useridMap = totalScores.getUserIdMap(Integer.parseInt(TotalScoresBean.ALL_SECTIONS_SELECT_VALUE));
			  }
			  Set uidSet = useridMap.keySet();
			  if (uidSet == null || uidSet.size() == 0) {
				  // if no student enroll into this site yet, use dueDate and acceptLateSubmission to decide the assessment status
				  if (dueDate != null && dueDate.before(currentDate) && !acceptLateSubmission) {
					  return false;
				  }
				  return true;
			  }
			  Iterator iter = uidSet.iterator();
			  String userId = null;
			  boolean isStillAvailable = false;
			  while(iter.hasNext()) {
				  userId = (String) iter.next();
				  isStillAvailable = isStillAvailable(submissionCountHash, numberRetakeHash, actualNumberRetakeHash, 
						  userId, currentDate, dueDate, acceptLateSubmission, maxSubmissionsAllowed);
				  if (isStillAvailable) {
					  return true;
				  }
			  }
			  returnValue = false;
		  }
	  }
	  else {
		  // should not come to here. but if this happens (
		  returnValue = true;
	  }
	  return returnValue;
  }

  private boolean isStillAvailable(HashMap submissionCountHash, HashMap numberRetakeHash, HashMap actualNumberRetakeHash,
		  String userId, Date currentDate, Date dueDate, 
		  boolean acceptLateSubmission, int maxSubmissionsAllowed) {
	  boolean isStillAvailable = false;

	  int totalSubmitted = 0;
	  if (submissionCountHash != null && submissionCountHash.get(userId) != null){
		  totalSubmitted = ( (Integer) submissionCountHash.get(userId)).intValue();
	  }

	  int numberRetake = 0;
	  if (numberRetakeHash != null && numberRetakeHash.get(userId) != null) {
		  numberRetake = ((Integer) numberRetakeHash.get(userId)).intValue();
	  }

	  //2. time to go through all the criteria
	  if (dueDate != null && dueDate.before(currentDate)) {
		  if (acceptLateSubmission) {
			  if (totalSubmitted == 0) {
				  return true;
			  }
		  }
		  int actualNumberRetake = 0;
		  if (actualNumberRetakeHash != null && actualNumberRetakeHash.get(userId) != null) {
			  actualNumberRetake = ((Integer) actualNumberRetakeHash.get(userId)).intValue();
		  }
		  if (actualNumberRetake < numberRetake) {
			  isStillAvailable = true;
		  }
	  }
	  else {
		  if (totalSubmitted < maxSubmissionsAllowed + numberRetake) {
			  isStillAvailable = true;
		  }
	  }
	  return isStillAvailable;
  }
}
