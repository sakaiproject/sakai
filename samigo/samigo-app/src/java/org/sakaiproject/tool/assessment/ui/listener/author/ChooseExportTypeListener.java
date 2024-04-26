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

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataBean;
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
    String qpid = (String) ContextUtil.lookupParam("qpid");

    if (StringUtils.isNotBlank(assessmentId)) {
        log.debug("ExportAssessmentListener assessmentId= {}", assessmentId);
        AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
        AssessmentService assessmentService = new AssessmentService();
        AssessmentFacade assessment = assessmentService.getBasicInfoOfAnAssessment(assessmentId);
        assessmentBean.setAssessmentId(assessment.getAssessmentBaseId().toString());
        assessmentBean.setTitle(assessment.getTitle());
    } else if (StringUtils.isNotBlank(qpid)) {
        log.debug("ExportAssessmentListener qpid= {}", qpid);
        QuestionPoolBean questionPoolBean = (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
        QuestionPoolService questionPoolService = new QuestionPoolService();
        QuestionPoolFacade questionPool = questionPoolService.getPool(Long.parseLong(qpid), AgentFacade.getAgentString());
        questionPoolBean.setQuestionPoolId(questionPool.getQuestionPoolId().toString());
        questionPoolBean.setName(questionPool.getTitle());
        // set current pool
        QuestionPoolDataBean pool = new QuestionPoolDataBean();
        pool.setId(Long.parseLong(qpid));
        questionPoolBean.setCurrentPool(pool);
    }
  }

}
