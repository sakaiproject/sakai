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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.qti.XMLController;
import org.sakaiproject.tool.assessment.ui.bean.qti.XMLDisplay;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author jesusmmp
 * @version $Id$
 */
@Slf4j
public class ExportPoolListener implements ActionListener {

	public void processAction(ActionEvent ae) throws AbortProcessingException {

		String questionPoolId = (String) ContextUtil.lookupParam("questionPoolId");
		String currentItemIdsString = (String) ContextUtil.lookupParam("currentItemIdsString");
		XMLDisplay xmlDisp = (XMLDisplay) ContextUtil.lookupBean("xml");
		String agentIdString = AgentFacade.getAgentString();
		boolean accessDenied = true;
		log.info("ExportPoolListener questionPoolId={}", questionPoolId);

		QuestionPoolService questionPoolService = new QuestionPoolService();
		QuestionPoolFacade questionPool = questionPoolService.getPool(Long.parseLong(questionPoolId),
				AgentFacade.getAgentString());

		List items = questionPoolService.getAllItems(questionPool.getQuestionPoolId());

		StringBuilder sb = new StringBuilder();
		// creating a question list separated by comma
		for (Object item : items) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(((ItemFacade) item).getItemId());
		}

		// checking user can export pool
		accessDenied = !questionPoolService.canExportPool(questionPoolId, agentIdString);

		if (accessDenied) {
			xmlDisp.setOutcome("exportDenied");
			String thisIp = ((javax.servlet.http.HttpServletRequest) FacesContext.getCurrentInstance()
					.getExternalContext().getRequest()).getRemoteAddr();
			// logging IP , as requested in SAK-17984
			log.warn("Unauthorized attempt to access /samigo-app/jsf/qti/exportAssessment.xml?questionPoolId="
					+ questionPoolId + " from IP : " + thisIp);
			return;
		}

		XMLController xmlController = (XMLController) ContextUtil.lookupBean("xmlController");
		xmlController.setId(StringUtils.isEmpty(currentItemIdsString) ? sb.toString() : currentItemIdsString);
		xmlController.setQtiVersion(1);
		xmlController.displayItemBankXml(questionPool.getDisplayName());
		xmlDisp.setOutcome("xmlPoolDisplay");

	}

}
