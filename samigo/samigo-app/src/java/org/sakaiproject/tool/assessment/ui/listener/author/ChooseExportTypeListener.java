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

package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class ChooseExportTypeListener implements ActionListener
{

  public ChooseExportTypeListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    String assessmentId = (String) ContextUtil.lookupParam("assessmentId");
    log.info("ChooseExportTypeListener assessmentId="+assessmentId);
    boolean published = ContextUtil.lookupParam("publishedId") != null;

    String title = "";
    if (published) {
        assessmentId = (String) ContextUtil.lookupParam("publishedId");
        PublishedAssessmentService pubAssessmentService = new PublishedAssessmentService();
        PublishedAssessmentData pubAssessmentData = pubAssessmentService.getBasicInfoOfPublishedAssessment(assessmentId);
        title = pubAssessmentData.getTitle();
    } else {
        AssessmentService assessmentService = new AssessmentService();
        AssessmentFacade assessment = assessmentService.getBasicInfoOfAnAssessment(assessmentId);
        title = assessment.getTitle();
    }
    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
    assessmentBean.setIsFromPublished(published);
    assessmentBean.setAssessmentId(assessmentId);
    assessmentBean.setTitle(title);
  }

}
