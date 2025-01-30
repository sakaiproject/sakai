/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2023 Apereo Foundation
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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class UpdateAllPoolsQuestionsListener implements ActionListener {
    public void processAction(ActionEvent arg0) throws AbortProcessingException {
        AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
        assessmentBean.getSections().stream()
            .filter(sectionBean -> sectionBean.getPoolIdToBeDrawn() != null).forEach(sectionBean -> {
                int success = assessmentBean.updateRandomPoolQuestions(sectionBean.getSectionId());
                if (success != AssessmentService.UPDATE_SUCCESS) {
                    String errString = AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE == success ? "update_pool_error_size_too_large" : "update_pool_error_unknown";
                    FacesContext context = FacesContext.getCurrentInstance();
                    String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", errString);
                    context.addMessage(null, new FacesMessage(err));
                }
        });
    }
}
