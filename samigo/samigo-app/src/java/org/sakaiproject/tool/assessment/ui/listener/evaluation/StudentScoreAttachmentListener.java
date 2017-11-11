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
package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class StudentScoreAttachmentListener implements ActionListener {

	private static Logger log = LoggerFactory.getLogger(StudentScoreAttachmentListener.class);

	public void processAction(ActionEvent event) throws AbortProcessingException {
		StudentScoresBean studentScoresBean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");

		String itemGradingId = ContextUtil.lookupParam("itemGradingId");
		log.debug("itemGradingId = " + itemGradingId);
		studentScoresBean.setItemGradingIdForFilePicker(Long.valueOf(itemGradingId));
	}
}
