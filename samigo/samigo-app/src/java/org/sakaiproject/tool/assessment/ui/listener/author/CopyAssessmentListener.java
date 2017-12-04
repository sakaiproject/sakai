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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
public class CopyAssessmentListener implements ActionListener {

	public void processAction(ActionEvent ae) throws AbortProcessingException {
		log.debug("Enter processAction()");
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
		String assessmentId = assessmentBean.getAssessmentId();
		log.debug("assessmentId = " + assessmentId);
		AssessmentService assessmentService = new AssessmentService();
		String apepndCopyTitle = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "append_copy_title");
		assessmentService.copyAssessment(assessmentId, apepndCopyTitle);
	}
}
