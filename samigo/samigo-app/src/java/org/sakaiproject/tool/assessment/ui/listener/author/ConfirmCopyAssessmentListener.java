/**
 * Copyright (c) 2005-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

public class ConfirmCopyAssessmentListener implements ActionListener {
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		FacesContext context = FacesContext.getCurrentInstance();

		// #1 - read the assessmentId from the form
		String assessmentId = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("assessmentId");

		// #2 -  and use it to set author bean, goto removeAssessment.jsp
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade assessment = assessmentService.getBasicInfoOfAnAssessment(assessmentId);

		// #3 - permission checking before proceeding - daisyf
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		author.setOutcome("confirmCopyAssessment");

		AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		if (!authzBean.isUserAllowedToEditAssessment(assessmentId, assessment.getCreatedBy(), false)) {
			author.setOutcome("author");
			return;
		}

		assessmentBean.setAssessmentId(assessment.getAssessmentBaseId().toString());
		assessmentBean.setTitle(FormattedText.convertFormattedTextToPlaintext(assessment.getTitle()));
	}
}
