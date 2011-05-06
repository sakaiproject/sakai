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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class RemovePartListener implements ActionListener
{
  //private static Log log = LogFactory.getLog(RemovePartListener.class);

  public RemovePartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
        "assessmentBean");
    SectionBean sectionBean = (SectionBean) ContextUtil.lookupBean(
        "sectionBean");

    // #1. get all the info need from bean
    String sectionId = sectionBean.getSectionId();
    String destSectionId = sectionBean.getDestSectionId();
    String removeAllQuestions = sectionBean.getRemoveAllQuestions();

    // #2 - check if we are removing all question or we
    // need to move question to another part
    AssessmentService assessmentService = new AssessmentService();
    //log.info("** removeAll Question="+removeAllQuestions);
    if (!("1").equals(removeAllQuestions)){
      // move questions to destinated Section when removing a section
      if (destSectionId == null || ("").equals(destSectionId)){
        destSectionId = assessmentBean.getFirstSectionId();
      }
      assessmentService.moveAllItems(sectionId,destSectionId);
    }
    assessmentService.removeSection(sectionId);
    EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.revise", "siteId=" + AgentFacade.getCurrentSiteId() + ", removed sectionId=" + sectionId, true));
    
    // #2 - goto editAssessment.jsp, so reset assessmentBean
    //log.info("** assessmentId in RemovePartListener ="+assessmentBean.getAssessmentId());
    AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentBean.getAssessmentId());
    assessmentBean.setAssessment(assessment);

  }

}
