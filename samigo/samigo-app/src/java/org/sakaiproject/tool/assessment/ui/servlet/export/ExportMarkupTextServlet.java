/**********************************************************************************
 * $URL: 
 * $Id: 
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.servlet.export;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;


/**
 * Servlet to export assessments to Markup text language
 */
@Slf4j
public class ExportMarkupTextServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private SecurityService securityService = ComponentManager.get(SecurityService.class);
	private static final SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
	private static final ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class);

	/**
	 * passthu to post
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doPost(req, res);
	}

	/**
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		String assessmentId = req.getParameter("assessmentId");
		String questionPoolId = req.getParameter("questionPoolId");
		String currentItemIdsString = req.getParameter("currentItemIdsString");
		String agentIdString = getAgentString(req, res);

		if (StringUtils.isNotBlank(assessmentId)) {
			exportAssessment(req, res, assessmentId, agentIdString);
		} else if (StringUtils.isNotBlank(questionPoolId)) {
			exportQuestionPool(req, res, questionPoolId, currentItemIdsString, agentIdString);
		}
	}

	private boolean isOwner(String agentId, String ownerId) {
		boolean isOwner = false;
		isOwner = agentId.equals(ownerId);
		return isOwner;
	}

	private String getAgentString(HttpServletRequest req,
			HttpServletResponse res) {
		String agentIdString = AgentFacade.getAgentString();
		if (StringUtils.isEmpty(agentIdString)) { // try this
			PersonBean person = (PersonBean) ContextUtil
					.lookupBeanFromExternalServlet("person", req, res);
			agentIdString = person.getAnonymousId();
		}
		return agentIdString;
	}

	public boolean canExport(HttpServletRequest req, HttpServletResponse res,
			String agentId, String currentSiteId, String createdBy) {
		log.debug("agentId={}" + agentId);
		log.debug("currentSiteId=" + currentSiteId);
		boolean hasPrivilege_any = hasPrivilege(req, SamigoConstants.AUTHZ_EDIT_ASSESSMENT_ANY,
				currentSiteId);
		boolean hasPrivilege_own = hasPrivilege(req, SamigoConstants.AUTHZ_EDIT_ASSESSMENT_OWN,
				currentSiteId);
		log.debug("hasPrivilege_any=" + hasPrivilege_any);
		log.debug("hasPrivilege_own=" + hasPrivilege_own);
		return (hasPrivilege_any || (hasPrivilege_own && isOwner(
				agentId, createdBy)));
	}

	public boolean hasPrivilege(HttpServletRequest req, String functionName, String context) {
		return securityService.unlock(functionName, "/site/" + context);
	}

	private Map<String, String> createBundle() {
		Map<String, String> bundle = new HashMap<>();
		String points = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "points_lower_case");
		String discount = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.SamLite", "samlite_discount");
		String randomize = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.SamLite", "example_mc_question_random");
		String rationale = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.SamLite", "example_mc_question_rationale");
		String str_true = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.SamLite", "samlite_true");
		String str_false = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.SamLite", "samlite_false");
		bundle.put("points", points);
		bundle.put("discount", discount.toLowerCase());
		bundle.put("randomize", randomize);
		bundle.put("rationale", rationale);
		bundle.put("true", str_true);
		bundle.put("false", str_false);

		return bundle;
	}

	/**
	 * Export assessment on a txt file.
	 * 
	 * @param req the HttpServletRequest object
	 * @param res the HttpServletResponse object
	 * @param assessmentId the assessment id
	 * @param agentIdString user
	 * @throws ServletException
	 * @throws IOException
	 */
	private void exportAssessment(HttpServletRequest req, HttpServletResponse res, String assessmentId, String agentIdString) throws ServletException, IOException {
		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
		int success = assessmentService.updateAllRandomPoolQuestions(assessment);
		String fileName = "exportAssessment.txt";
		String errorPoolSizeTooLarge = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "update_pool_error_size_too_large");
		String errorPoolUpdateUnknown = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
		String pathUpdateError = "/jsf/qti/poolUpdateError.faces";

		if (success != AssessmentService.UPDATE_SUCCESS) {
			if (success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE){
				handleExportError(req, res, errorPoolSizeTooLarge, pathUpdateError);
			} else {
				handleExportError(req, res, errorPoolUpdateUnknown, pathUpdateError);
			}
			return;
		}

		String currentSiteId = assessmentService.getAssessmentSiteId(assessmentId);
		String createdBy = assessmentService.getAssessmentCreatedBy(assessmentId);

		if (!canExport(req, res, agentIdString, currentSiteId, createdBy)) {
			handleAccessDenied(req, res);
			return;
		}

		Map<String, String> bundle = createBundle();
		String markupText = assessmentService.exportAssessmentToMarkupText(assessment, bundle);

		writeMarkupTextResponse(res, markupText, fileName);
	}

	/**
	 * Export question pool on a file.
	 * 
	 * @param req the HttpServletRequest object
	 * @param res the HttpServletResponse object
	 * @param questionPoolId the question pool id
	 * @param currentItemIdsString questions ids separated by comma
	 * @param agentIdString user
	 * @throws ServletException
	 * @throws IOException
	 */
	private void exportQuestionPool(HttpServletRequest req, HttpServletResponse res, String questionPoolId, String currentItemIdsString, String agentIdString) throws ServletException, IOException {
		QuestionPoolService questionPoolService = new QuestionPoolService();
		QuestionPoolFacade questionPool = questionPoolService.getPool(Long.parseLong(questionPoolId), AgentFacade.getAgentString());
		String fileName = "exportPool.txt";

		// checking user can export pool
		if (!questionPoolService.canExportPool(questionPoolId, agentIdString)) {
			handleAccessDenied(req, res);
			return;
		}

		Map<String, String> bundle = createBundle();
		String markupText = questionPoolService.exportQuestionPoolToMarkupText(questionPool, currentItemIdsString, bundle);

		writeMarkupTextResponse(res, markupText, fileName);
	}

	/**
	 * Handles the error when exporting fails.
	 * 
	 * @param req the HttpServletRequest object
	 * @param res the HttpServletResponse object
	 * @param errorMessage the error message to display
	 * @param errorPage the error page to forward to
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleExportError(HttpServletRequest req, HttpServletResponse res, String errorMessage, String errorPage) throws ServletException, IOException {
		req.setAttribute("error", errorMessage);
		RequestDispatcher dispatcher = req.getRequestDispatcher(errorPage);
		dispatcher.forward(req, res);
	}

	/**
	 * Handles the case when the user doesn't have permission to export.
	 * 
	 * @param req the HttpServletRequest object
	 * @param res the HttpServletResponse object
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleAccessDenied(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String pathDenied = "/jsf/qti/exportDenied.faces";

		RequestDispatcher dispatcher = req.getRequestDispatcher(pathDenied);
		dispatcher.forward(req, res);
	}

	/**
	 * Writes the markup text response to the HttpServletResponse object.
	 * 
	 * @param res the HttpServletResponse object
	 * @param markupText the markup text to write
	 * @param filename the name of the file to download
	 * @throws IOException
	 */
	private void writeMarkupTextResponse(HttpServletResponse res, String markupText, String filename) throws IOException {
		String contentType = "text/plain";
		String contentDisposition = "Content-Disposition";
		String attachmentFilename = "attachment;filename=\"";

		res.setContentType(contentType);
		res.setHeader(contentDisposition, attachmentFilename + filename + "\";");
		res.setContentLength(markupText.length());
		PrintWriter out = res.getWriter();
		out.println(markupText);
		out.close();
		out.flush();
	}
}
