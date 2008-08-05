/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/author/TemplateListener.java $
 * $Id: TemplateListener.java 11181 2006-06-26 08:13:58Z lydial@stanford.edu $
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Description: Listener for the Assessment page</p>
 */

public class AssessmentListener 
    implements ActionListener
{
  private static Log log = LogFactory.getLog(TemplateListener.class);
  private static ContextUtil cu;

  public AssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
	  AuthorBean author = (AuthorBean) cu.lookupBean("author");

	  // prepare core assessment list
	  AssessmentService assessmentService = new AssessmentService();
	  ArrayList assessmentList = assessmentService.getBasicInfoOfAllActiveAssessments(AssessmentFacadeQueries.TITLE, author.isCoreAscending());
	  author.setAssessments(assessmentList);
	  log.debug("core assessmen list size = " + assessmentList.size());
        
	  // prepare published assessment list
	  PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
	  ArrayList publishedList = publishedAssessmentService.getBasicInfoOfAllActivePublishedAssessments(PublishedAssessmentFacadeQueries.TITLE,true);
	  author.setPublishedAssessments(publishedList);
	  log.debug("published assessment list size = " + publishedList.size());
	  
	  // prepare published inactive assessment list
	  ArrayList inactivePublishedList = publishedAssessmentService.getBasicInfoOfAllInActivePublishedAssessments(PublishedAssessmentFacadeQueries.TITLE,true);
	  author.setInactivePublishedAssessments(inactivePublishedList);
	  log.debug("published inactive assessment list size = " + inactivePublishedList.size());
  }
}
