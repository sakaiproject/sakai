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

package org.sakaiproject.tool.assessment.ui.servlet.cp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.contentpackaging.ManifestGenerator;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.qti.XMLController;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * @version $Id$
 */
@Slf4j
public class DownloadCPServlet extends HttpServlet {

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
	 * Get the faces context and display the contents of the XMLDisplay bean
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
			exportAssessmentZip(req, res, assessmentId, agentIdString);
		} else if (StringUtils.isNotBlank(questionPoolId)) {
			exportQuestionPoolZip(req, res, questionPoolId, currentItemIdsString, agentIdString);
		}
	}

	/**
	 * Export an assessment on a Zip.
	 * 
	 * @param req the HttpServletRequest object
	 * @param res the HttpServletResponse object
	 * @param assessmentId the assessment id
	 * @param agentIdString user
	 * @throws ServletException
	 * @throws IOException
	 */
	private void exportAssessmentZip(HttpServletRequest req, HttpServletResponse res, String assessmentId, String agentIdString) throws ServletException, IOException {
		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
		int success = assessmentService.updateAllRandomPoolQuestions(assessment);
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

		//only need the assessmentId
		writeZipResponse(req, res, true, assessmentId, null, null, null, null);
	}

	/**
	 * Export a question pool on a Zip.
	 * 
	 * @param req the HttpServletRequest object
	 * @param res the HttpServletResponse object
	 * @param questionPoolId the question pool id
	 * @param currentItemIdsString questions ids separated by comma
	 * @throws ServletException
	 * @throws IOException
	 */
	private void exportQuestionPoolZip(HttpServletRequest req, HttpServletResponse res, String questionPoolId, String currentItemIdsString, String agentIdString) throws ServletException, IOException {
		QuestionPoolService questionPoolService = new QuestionPoolService();
		QuestionPoolFacade questionPool = questionPoolService.getPool(Long.parseLong(questionPoolId), AgentFacade.getAgentString());
		StringBuilder sb = new StringBuilder();

		List items = questionPoolService.getAllItems(questionPool.getQuestionPoolId());

		// creating a question list separated by comma
		for (Object item : items) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(((ItemFacade) item).getItemId());
		}

		// checking user can export pool
		if (!questionPoolService.canExportPool(questionPoolId, agentIdString)) {
			handleAccessDenied(req, res);
			return;
		}

		writeZipResponse(req, res, false, null, questionPoolId, questionPool.getDisplayName(), currentItemIdsString, sb.toString());
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
	 * Writes the zip
	 */
	private void writeZipResponse(HttpServletRequest req, HttpServletResponse res, boolean isAssessment, String assessmentId, 
			String questionPoolId, String questionPoolDisplayName, String currentItemIdsString, String sb) throws IOException {

		String contentType = "application/x-zip-compressed";
		String contentDisposition = "Content-Disposition";
		String attachmentFilename = "attachment;filename=\"";
		String exportAssessmentZip = "exportAssessment.zip";
		String exportPoolZip = "exportPool.zip";
		String xmlFileName = "exportAssessment.xml";
		String manifestFileName = "imsmanifest.xml";
		XMLController xmlController = (XMLController) ContextUtil.lookupBeanFromExternalServlet("xmlController", req, res);

		res.setContentType(contentType);
		String zipFilename = (isAssessment) ? exportAssessmentZip : exportPoolZip;
		res.setHeader(contentDisposition, attachmentFilename + zipFilename + "\";");

		ServletOutputStream outputStream = null;
		ZipOutputStream zos = null;
		ZipEntry ze = null;

		try {
			byte[] b = null;
			outputStream = res.getOutputStream();
			zos = new ZipOutputStream(outputStream);

			// QTI file - exportAssessment.xml
			// we maintain this name according to the ManifestGenerator even if is a pool
			ze = new ZipEntry(xmlFileName);
			zos.putNextEntry(ze);
			if (isAssessment) {
				xmlController.setId(assessmentId);
				xmlController.displayAssessmentXml();
			} else {
				// this should be a pool question list separated by comma
				xmlController.setId(StringUtils.isBlank(currentItemIdsString) ? sb : currentItemIdsString);
				xmlController.displayItemBankXml(questionPoolDisplayName);
			}
			xmlController.setQtiVersion(1);
			String qtiString = xmlController.getXmlBean().getXml();
			log.debug("qtiString = {} ", qtiString);
			b = qtiString.getBytes();
			zos.write(b, 0, b.length);
			zos.closeEntry();

			// imsmanifest.xml
			ze = new ZipEntry(manifestFileName);
			zos.putNextEntry(ze);
			ManifestGenerator manifestGenerator = (isAssessment) ? new ManifestGenerator(assessmentId) : new ManifestGenerator(Long.parseLong(questionPoolId));
			String manString = manifestGenerator.getManifest();
			log.debug("manString = {}", manString);
			b = manString.getBytes();
			zos.write(b, 0, b.length);
			zos.closeEntry();

			// Attachments
			HashMap contentMap = manifestGenerator.getContentMap();

			String filename = null;
			for (Iterator it = contentMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				filename = (String)  entry.getKey();
				ze = new ZipEntry(filename.substring(1));
				zos.putNextEntry(ze);
				b = (byte[]) entry.getValue();
				zos.write(b, 0, b.length);
				zos.closeEntry();
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			if (zos != null) {
				try {
					zos.closeEntry();
					zos.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
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
		if (agentIdString == null || agentIdString.equals("")) { // try this
			PersonBean person = (PersonBean) ContextUtil
					.lookupBeanFromExternalServlet("person", req, res);
			agentIdString = person.getAnonymousId();
		}
		return agentIdString;
	}

	public boolean canExport(HttpServletRequest req, HttpServletResponse res,
			String agentId, String currentSiteId, String createdBy) {
		log.debug("agentId=" + agentId);
		log.debug("currentSiteId=" + currentSiteId);
		boolean hasPrivilege_any = hasPrivilege(req, "assessment.editAssessment.any",
				currentSiteId);
		boolean hasPrivilege_own = hasPrivilege(req, "assessment.editAssessment.own",
				currentSiteId);
		log.debug("hasPrivilege_any=" + hasPrivilege_any);
		log.debug("hasPrivilege_own=" + hasPrivilege_own);
		boolean hasPrivilege = (hasPrivilege_any || (hasPrivilege_own && isOwner(
				agentId, createdBy)));
		return hasPrivilege;
	}

	public boolean hasPrivilege(HttpServletRequest req, String functionName, String context) {
		boolean privilege = SecurityService.unlock(functionName, "/site/" + context);
		return privilege;
	}
}
