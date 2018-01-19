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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.RetakeAssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ConfirmRetakeAssessmentListener implements ActionListener {

	public ConfirmRetakeAssessmentListener() {
	}

	public void processAction(ActionEvent ae) throws AbortProcessingException {
		RetakeAssessmentBean retakeAssessment = (RetakeAssessmentBean) ContextUtil.lookupBean("retakeAssessment");
		AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		PublishedAssessmentService pubService = new PublishedAssessmentService();

		String publishedAssessmentId = ContextUtil.lookupParam("publishedAssessmentId");
		Long pubId = new Long(publishedAssessmentId);
		String agentIdString = ContextUtil.lookupParam("agentIdString");
		AgentFacade agent = new AgentFacade(agentIdString);
		String studentName = agent.getDisplayName();
		String assessmentOwner = pubService.getPublishedAssessmentOwner(pubId);

		if (!authzBean.isUserAllowedToGradeAssessment(publishedAssessmentId, assessmentOwner, true)) {
			throw new IllegalArgumentException("ConfirmRetakeAssessmentListener unauthorized attempt to set retake for " + publishedAssessmentId);
		}
		retakeAssessment.setPublishedAssessmentId(Long.valueOf(publishedAssessmentId));
		retakeAssessment.setAgentId(agentIdString);
		retakeAssessment.setStudentName(studentName);
	}

}
