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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

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

    author.setAssessCreationMode("1");
    prepareAssessmentsList(author, assessmentService, gradingService, publishedAssessmentService);
    
    String s = ServerConfigurationService.getString("samigo.editPubAssessment.restricted");
	if (s != null && s.toLowerCase().equals("false")) {
		author.setEditPubAssessmentRestricted(false);
	}
	else {
		author.setEditPubAssessmentRestricted(true);
	}
	
	AuthorizationBean authorizationBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
	author.setIsGradeable(authorizationBean.getGradeAnyAssessment() || authorizationBean.getGradeOwnAssessment());
	author.setIsEditable(authorizationBean.getEditAnyAssessment() || authorizationBean.getEditOwnAssessment());
  }

  public void prepareAssessmentsList(AuthorBean author, AssessmentService assessmentService, GradingService gradingService, PublishedAssessmentService publishedAssessmentService) {
		// #2 - prepare core assessment list
		author.setCoreAssessmentOrderBy(AssessmentFacadeQueries.TITLE);
		ArrayList assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
						AssessmentFacadeQueries.TITLE, author.isCoreAscending());
		Iterator iter = assessmentList.iterator();
		while (iter.hasNext()) {
			AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
			assessmentFacade.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
		}
		// get the managed bean, author and set the list
		author.setAssessments(assessmentList);

		prepareAllPublishedAssessmentsList(author, gradingService, publishedAssessmentService);
  }

  public void prepareAllPublishedAssessmentsList(AuthorBean author, GradingService gradingService, PublishedAssessmentService publishedAssessmentService) {
	  ArrayList publishedAssessmentList = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments2(
			  PublishedAssessmentFacadeQueries.TITLE, true, AgentFacade.getCurrentSiteId());
	  HashMap inProgressCounts = gradingService.getInProgressCounts(AgentFacade.getCurrentSiteId());
	  HashMap submittedCounts = gradingService.getSubmittedCounts(AgentFacade.getCurrentSiteId());
	  
	  ArrayList dividedPublishedAssessmentList = getTakeableList(publishedAssessmentList);
	  prepareActivePublishedAssessmentsList(author, (ArrayList) dividedPublishedAssessmentList.get(0), inProgressCounts, submittedCounts);
	  prepareInactivePublishedAssessmentsList(author, (ArrayList) dividedPublishedAssessmentList.get(1), inProgressCounts, submittedCounts);  
  }
  
  public void prepareActivePublishedAssessmentsList(AuthorBean author, ArrayList<PublishedAssessmentFacade> activePublishedList, HashMap<Long, Integer> getInProgressCounts, HashMap<Long, Integer> submittedCounts) {
	  setInProgressAndSubmittedCount(activePublishedList, getInProgressCounts, submittedCounts);
	  author.setPublishedAssessments(activePublishedList);  
  }
  
  public void prepareInactivePublishedAssessmentsList(AuthorBean author, ArrayList inactivePublishedList, HashMap<Long, Integer> getInProgressCounts, HashMap<Long, Integer> submittedCounts) {	  
	  setInProgressAndSubmittedCount(inactivePublishedList, getInProgressCounts, submittedCounts);
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

  private void setInProgressAndSubmittedCount(ArrayList<PublishedAssessmentFacade> list, HashMap<Long, Integer> inProgressCounts, HashMap<Long, Integer> submittedCounts) {
	  for (int i = 0; i < list.size(); i++) {
		  boolean hasAssessmentGradingData = true;
		  boolean hasInProgressCounts = true;
		  boolean hasSubmitted = true;
		  PublishedAssessmentFacade p = (PublishedAssessmentFacade) list.get(i);
		  Long publishedAssessmentId = p.getPublishedAssessmentId();
		  if (publishedAssessmentId != null) {
			  if (inProgressCounts.get(publishedAssessmentId) != null) {
				  p.setInProgressCount(((Integer) inProgressCounts.get(publishedAssessmentId)).intValue());
			  } else {
				  p.setInProgressCount(0);
				  hasInProgressCounts = false;
			  }
			  if (submittedCounts.get(publishedAssessmentId) != null) {
				  p.setSubmittedCount(((Integer) submittedCounts.get(publishedAssessmentId)).intValue());
			  } else {
				  p.setSubmittedCount(0);
				  hasSubmitted = false;
			  }
			  hasAssessmentGradingData = hasInProgressCounts || hasSubmitted;
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

  public ArrayList getTakeableList(ArrayList assessmentList) {
	  ArrayList list = new ArrayList();
	  ArrayList activeList = new ArrayList();
	  ArrayList inActiveList = new ArrayList();
	  	  	  
	  for (int i = 0; i < assessmentList.size(); i++) {
		  PublishedAssessmentFacade f = (PublishedAssessmentFacade)assessmentList.get(i);
		  f.setTitle(FormattedText.convertFormattedTextToPlaintext(f.getTitle()));
		  if (isActive(f)) {
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

  public boolean isActive(PublishedAssessmentFacade f) {  
	  //1. prepare our significant parameters
	  Integer status = f.getStatus();
	  Date currentDate = new Date();
	  Date startDate = f.getStartDate();
	  Date retractDate = f.getRetractDate();
	  Date dueDate = f.getDueDate();

	  if (!Integer.valueOf(1).equals(status)) {
		  return false;
	  }

	  if (startDate != null && startDate.after(currentDate)) {
		  return false;
	  }

	  if (dueDate != null && dueDate.before(currentDate)) {
		  return false;
	  }
	  
	  if (retractDate != null && retractDate.before(currentDate)) {
		  return false;
	  }

	  return true;
  }
}
