/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class RemoveAssessmentListener implements ActionListener
{
  //rivate static Log log = LogFactory.getLog(RemoveAssessmentListener.class);
	
  public RemoveAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    AssessmentService s = new AssessmentService();

    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
                                                           "assessmentBean");

    // #1 - remove selected assessment on a separate thread
    String assessmentId = (String) assessmentBean.getAssessmentId();
    AssessmentIfc assessment = s.getAssessment(assessmentId); 
    RemoveAssessmentThread thread = new RemoveAssessmentThread(assessmentId, SessionManager.getCurrentSessionUserId());
    thread.start();

    // This should have been done inside AssessmentFacadeQueries.removeAssessment()
    // but it didn't work there nor inside RemoveAssessmentThread. 
    // Debugging log in Conntent Hosting doesn't show anything.
    // So I have to do it here
    // #2 - even if assessment is set to dead, we intend to remove any resources
    //    List resourceIdList = s.getAssessmentResourceIdList(assessment);
    // s.deleteResources(resourceIdList);

    //#3 - goto authorIndex.jsp so fix the assessment List in author bean by
    // removing an assessment from the list
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
                       "author");
    //int pageSize = 10;
    //int pageNumber = 1;
    ArrayList assessmentList = author.getAssessments();
    ArrayList l = new ArrayList();
    for (int i=0; i<assessmentList.size();i++){
      AssessmentFacade a = (AssessmentFacade) assessmentList.get(i);
      if (!(assessmentId).equals(a.getAssessmentBaseId().toString()))
        l.add(a);
    }
    author.setAssessments(l);

  }

}
