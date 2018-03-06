/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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

import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class UpdateRandomPoolQuestionsListener  implements ActionListener{
	public void processAction(ActionEvent arg0) throws AbortProcessingException {
		String sectionId = (String) FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get("assessmentForm:randomQuestionsSectionId");

		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade af = assessmentService.getBasicInfoOfAnAssessmentFromSectionId(new Long(sectionId));
		Long assessmentId = af.getAssessmentBaseId();
		String createdBy = af.getCreatedBy();
		AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");

		if (!authzBean.isUserAllowedToEditAssessment(assessmentId.toString(), createdBy, false)) {
		    FacesContext context = FacesContext.getCurrentInstance();
		    String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
		    context.addMessage(null,new FacesMessage(err));
		    return;
		}

		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		int success = assessmentBean.updateRandomPoolQuestions(sectionId);
		if(success != AssessmentService.UPDATE_SUCCESS){
			FacesContext context = FacesContext.getCurrentInstance();
			if(success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE){  		    		
				String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_size_too_large");
				context.addMessage(null,new FacesMessage(err));
			}else{
				String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
				context.addMessage(null,new FacesMessage(err));
			}
		}
	}

}
