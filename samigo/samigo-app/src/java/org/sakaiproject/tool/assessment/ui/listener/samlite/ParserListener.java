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
package org.sakaiproject.tool.assessment.ui.listener.samlite;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.samlite.SamLiteBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.TextFormat;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

@Slf4j
public class ParserListener implements ActionListener {
	
	public ParserListener() {}
	
	public void processAction(ActionEvent ae) {
		SamLiteBean samLiteBean = (SamLiteBean) ContextUtil.lookupBean("samLiteBean");
		
		FacesContext context = FacesContext.getCurrentInstance();
		AssessmentService assessmentService = new AssessmentService();
			    
		String assessmentTitle = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(samLiteBean.getName());
		
		samLiteBean.setOutcome("samLiteValidation");
		//check assessmentTitle and see if it is duplicated, if is not then proceed, else throw error
		if (assessmentTitle!=null && (assessmentTitle.trim()).equals("")){
			String err1=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_empty");
			context.addMessage(null,new FacesMessage(err1));
			samLiteBean.setOutcome("samLiteEntry");
			return;
		}
			    
		boolean isUnique = assessmentService.assessmentTitleIsUnique("0", assessmentTitle, false);
		if (!isUnique){
			String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","duplicateName_error");
			context.addMessage(null,new FacesMessage(err));
			samLiteBean.setOutcome("samLiteEntry");
			return;
		}
			
		samLiteBean.parse();
	}
	
}
