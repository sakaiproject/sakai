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
import java.util.HashMap;
import java.util.Iterator;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
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

    author.setAssessCreationMode("1");
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
		// #2 - prepare core assessment list
		author.setCoreAssessmentOrderBy(AssessmentFacadeQueries.TITLE);
		ArrayList assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(
						AssessmentFacadeQueries.TITLE, author.isCoreAscending());
		// get the managed bean, author and set the list
		author.setAssessments(assessmentList);

		// #3 This map contains (Long, Integer)=(publishedAssessmentId,
		// submissionSize)
		HashMap map = gradingService.getSubmissionSizeOfAllPublishedAssessments();
		HashMap agMap = gradingService.getAGDataSizeOfAllPublishedAssessments();

		// #4 - prepare published assessment list
		author.setPublishedAssessmentOrderBy(PublishedAssessmentFacadeQueries.TITLE);
		ArrayList publishedList = publishedAssessmentService.getBasicInfoOfAllActivePublishedAssessments(
						PublishedAssessmentFacadeQueries.TITLE, true);
		setSubmissionSize(publishedList, map);
		setHasAssessmentGradingData(publishedList, agMap);
		// get the managed bean, author and set the list
		author.setPublishedAssessments(publishedList);
		log.debug("**** published list size =" + publishedList.size());

		// #5 - prepare published inactive assessment list
		author.setInactivePublishedAssessmentOrderBy(PublishedAssessmentFacadeQueries.TITLE);
		ArrayList inactivePublishedList = publishedAssessmentService.getBasicInfoOfAllInActivePublishedAssessments(
						PublishedAssessmentFacadeQueries.TITLE, true);
		setSubmissionSize(inactivePublishedList, map);
		setHasAssessmentGradingData(inactivePublishedList, agMap);
		// get the managed bean, author and set the list
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
  
  private void setSubmissionSize(ArrayList list, HashMap map){
    for (int i=0; i<list.size();i++){
      PublishedAssessmentFacade p =(PublishedAssessmentFacade)list.get(i);
      Integer size = (Integer) map.get(p.getPublishedAssessmentId());
      if (size != null){
        p.setSubmissionSize(size.intValue());
      }
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
}
