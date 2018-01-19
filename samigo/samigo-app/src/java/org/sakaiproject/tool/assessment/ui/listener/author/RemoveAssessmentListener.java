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
import java.util.Iterator;
import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class RemoveAssessmentListener implements ActionListener
{

  public RemoveAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    AssessmentService s = new AssessmentService();

    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");

    final String assessmentId = (String) assessmentBean.getAssessmentId();
    s.removeAssessment(assessmentId);

    final String context = s.getAssessmentSiteId(assessmentId);
    EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_REMOVE, "assessmentId=" + assessmentId, context, true, NotificationService.NOTI_NONE));
    try{
      AssessmentFacade assessmentFacade = s.getAssessment(assessmentId);

    Iterator<SectionFacade> sectionFacadeIterator = assessmentFacade.getSectionSet().iterator();
    while (sectionFacadeIterator.hasNext()){
      SectionFacade sectionFacade = sectionFacadeIterator.next();
      Iterator<ItemFacade> itemFacadeIterator = sectionFacade.getItemFacadeSet().iterator();
      while (itemFacadeIterator.hasNext()){
        ItemFacade itemFacade = itemFacadeIterator.next();
        EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_UNINDEXITEM, "/sam/" + context + "/unindexed, itemId=" + itemFacade.getItemIdString(), true));
      }
    }
    }catch(Exception ex){
      //The assessment doesn't exist. No-op in this case.
    }

    // This should have been done inside AssessmentFacadeQueries.removeAssessment()
    // but it didn't work there nor inside RemoveAssessmentThread. 
    // Debugging log in Conntent Hosting doesn't show anything.
    // So I have to do it here
    // #2 - even if assessment is set to dead, we intend to remove any resources
    // s.deleteResources(resourceIdList);

    //#3 - goto authorIndex_content.jsp so fix the assessment List in author bean by
    // removing an assessment from the list
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
                       "author");
    //int pageSize = 10;
    //int pageNumber = 1;
    List assessmentList = author.getAssessments();
    List l = new ArrayList();
    for (int i=0; i<assessmentList.size();i++){
      AssessmentFacade a = (AssessmentFacade) assessmentList.get(i);
      if (!(assessmentId).equals(a.getAssessmentBaseId().toString()))
        l.add(a);
    }
    author.setAssessments(l);

  }

}
