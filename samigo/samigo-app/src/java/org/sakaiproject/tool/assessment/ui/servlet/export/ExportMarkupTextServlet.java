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
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;


/**
 * Servlet to export assessments to Markup text language
 */
@Slf4j
public class ExportMarkupTextServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private SecurityService securityService = ComponentManager.get(SecurityService.class);

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

		//update random question pools (if any) before exporting
		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
		int success = assessmentService.updateAllRandomPoolQuestions(assessment);
		if(success == AssessmentService.UPDATE_SUCCESS){
			String agentIdString = getAgentString(req, res);
			String currentSiteId = assessmentService.getAssessmentSiteId(assessmentId);
			String assessmentCreatedBy = assessmentService.getAssessmentCreatedBy(assessmentId);
			boolean accessDenied = true;
			if (canExport(req, res, agentIdString, currentSiteId,
					assessmentCreatedBy)) {
				accessDenied = false;
			}

			if (accessDenied) {
				String path = "/jsf/qti/exportDenied.faces";
				RequestDispatcher dispatcher = req.getRequestDispatcher(path);
				dispatcher.forward(req, res);
			} else {
				
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
				
				String markupText = assessmentService.exportAssessmentToMarkupText(assessment, bundle);
				 
				res.setContentType("text/plain");
				String filename = "exportAssessment.txt";
				res.setHeader("Content-Disposition", "attachment;filename=\""
						+ filename + "\";");

				res.setContentLength(markupText.length());
				PrintWriter out = res.getWriter();
				out.println(markupText);
				out.close();
				out.flush();
			}
		}else{
			if(success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE){  		    		
				String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_size_too_large");
				req.setAttribute("error", err);
			}else{
				String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
				req.setAttribute("error", err);
			}

			String path = "/jsf/qti/poolUpdateError.faces";
			RequestDispatcher dispatcher = req.getRequestDispatcher(path);
			dispatcher.forward(req, res);
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
}
