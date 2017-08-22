/**
 * Copyright (c) 2005-2014 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class QuestionPoolListener implements ActionListener {

	/**
	 * Standard process action method.
	 * 
	 * @param ae
	 *            ActionEvent
	 * @throws AbortProcessingException
	 */
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		author.setIsEditPendingAssessmentFlow(true);

		QuestionPoolBean qpoolbean = (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
		// If from "Question Pools" link, always get data from db to rebuild the tree 
		if (ae != null && ae.getComponent().getId().equals("questionPoolsLink")) {
			qpoolbean.buildTree();
		}
		//Reset the currentpool in root
		qpoolbean.setCurrentPool(null);
		qpoolbean.setQpDataModelByLevel();
	}
}
