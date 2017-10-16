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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class UpdateAssessmentTotalPointsListener implements ActionListener{

	public void processAction(ActionEvent ae) throws AbortProcessingException
	  {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		
		AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		AuthorBean authorBean = (AuthorBean) ContextUtil.lookupBean("author");

		if (!authzBean.isUserAllowedToEditAssessment(assessmentBean.getAssessmentId(), assessmentBean.getAssessment().getCreatedBy(),
				!authorBean.getIsEditPendingAssessmentFlow()))
		{
			FacesContext context = FacesContext.getCurrentInstance();
			String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
			context.addMessage(null, new FacesMessage(err));
			return;
		}
		
		assessmentBean.setQuestionSizeAndTotalScore();
	  }
}
