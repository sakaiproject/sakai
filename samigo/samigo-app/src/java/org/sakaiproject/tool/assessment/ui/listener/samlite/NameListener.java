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
package org.sakaiproject.tool.assessment.ui.listener.samlite;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.samlite.SamLiteBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class NameListener implements ActionListener {
	
	
	public NameListener() {}
	
	public void processAction(ActionEvent ae) {
		FacesContext context = FacesContext.getCurrentInstance();
		
		SamLiteBean samLiteBean = (SamLiteBean) ContextUtil.lookupBean("samLiteBean");
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		
		author.setOutcome("samLiteEntry");
		
		// Permission check
	    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		if (!authzBean.isUserAllowedToCreateAssessment()) {
			String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_create_assessment_error");
			context.addMessage(null,new FacesMessage(err));
			author.setOutcome("author");
			return;
		}
	    
	    String assessmentTitle = ContextUtil.lookupParam("title");
	    
	    if (null == assessmentTitle || "".equals(assessmentTitle.trim())) {
	        String err1=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
	        context.addMessage(null,new FacesMessage(err1));
	        author.setOutcome("author");
	        return;
	    }
	    
	    samLiteBean.setName(assessmentTitle);
	    samLiteBean.setDescription(author.getAssessmentDescription());
	    
	    String templateId = ContextUtil.lookupParam("assessmentTemplate");

	    if (templateId == null){
	      templateId = AssessmentTemplateFacade.DEFAULTTEMPLATE.toString();
	    }
		
	    
	    samLiteBean.setAssessmentTemplateId(templateId);
	    
	}
			
}
