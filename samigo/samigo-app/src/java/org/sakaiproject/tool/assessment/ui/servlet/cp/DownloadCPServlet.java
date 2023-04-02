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
		String currentSiteId="";
		String createdBy="";
		boolean accessDenied = true;

		if (StringUtils.isNotEmpty(assessmentId)) {
			//update random question pools (if any) before exporting
			AssessmentService assessmentService = new AssessmentService();
			int success = assessmentService.updateAllRandomPoolQuestions(assessmentService.getAssessment(assessmentId));
			if(success == AssessmentService.UPDATE_SUCCESS) {
				currentSiteId = assessmentService.getAssessmentSiteId(assessmentId);
				createdBy = assessmentService.getAssessmentCreatedBy(assessmentId);
				if (canExport(req, res, agentIdString, currentSiteId, createdBy)) {
					accessDenied = false;
				}

				if (accessDenied) {
					String path = "/jsf/qti/exportDenied.faces";
					RequestDispatcher dispatcher = req.getRequestDispatcher(path);
					dispatcher.forward(req, res);
				} else {
					res.setContentType("application/x-zip-compressed");
					String zipFilename = "exportAssessment.zip";
					res.setHeader("Content-Disposition", "attachment;filename=\"" + zipFilename + "\";");

					ServletOutputStream outputStream = null;
					ZipOutputStream zos = null;
					ZipEntry ze = null;

					try {
						byte[] b = null;
						outputStream = res.getOutputStream();
						zos = new ZipOutputStream(outputStream);

						// QTI file
						ze = new ZipEntry("exportAssessment.xml");
						zos.putNextEntry(ze);
						XMLController xmlController = (XMLController) ContextUtil.lookupBeanFromExternalServlet("xmlController", req, res);
						xmlController.setId(assessmentId);
						xmlController.setQtiVersion(1);
						xmlController.displayAssessmentXml();
						String qtiString = xmlController.getXmlBean().getXml();
						log.debug("qtiString = " + qtiString);
						b = qtiString.getBytes();
						zos.write(b, 0, b.length);
						zos.closeEntry();

						// imsmanifest.xml
						ze = new ZipEntry("imsmanifest.xml");
						zos.putNextEntry(ze);
						ManifestGenerator manifestGenerator = new ManifestGenerator(assessmentId);
						String manString = manifestGenerator.getManifest();
						log.debug("manString = " + manString);
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
							} catch (IOException e) {
								log.error(e.getMessage());
							}
							try {
								zos.close();
							} catch (IOException e) {
								log.error(e.getMessage());
							}

						}
					}
				}
			} else {
				if (success == AssessmentService.UPDATE_ERROR_DRAW_SIZE_TOO_LARGE) {
					String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_size_too_large");
					req.setAttribute("error", err);
				} else {
					String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","update_pool_error_unknown");
					req.setAttribute("error", err);
				}

				String path = "/jsf/qti/poolUpdateError.faces";
				RequestDispatcher dispatcher = req.getRequestDispatcher(path);
				dispatcher.forward(req, res);
			}
		} else if (StringUtils.isNotEmpty(questionPoolId)) {
			QuestionPoolService questionPoolService = new QuestionPoolService();
			QuestionPoolFacade questionPool = questionPoolService.getPool(Long.parseLong(questionPoolId), AgentFacade.getAgentString());

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
				String path = "/jsf/qti/exportDenied.faces";
				RequestDispatcher dispatcher = req.getRequestDispatcher(path);
				dispatcher.forward(req, res);
			} else {
				res.setContentType("application/x-zip-compressed");
				String zipFilename = "exportPool.zip";
				res.setHeader("Content-Disposition", "attachment;filename=\"" + zipFilename + "\";");

				ServletOutputStream outputStream = null;
				ZipOutputStream zos = null;
				ZipEntry ze = null;

				try {
					byte[] b = null;
					outputStream = res.getOutputStream();
					zos = new ZipOutputStream(outputStream);

					// QTI file
					// we maintain this name according to the ManifestGenerator although is a pool
					ze = new ZipEntry("exportAssessment.xml");
					zos.putNextEntry(ze);
					XMLController xmlController = (XMLController) ContextUtil.lookupBeanFromExternalServlet("xmlController", req, res);
					// this should be a pool question list separated by comma
					xmlController.setId(StringUtils.isEmpty(currentItemIdsString) ? sb.toString() : currentItemIdsString);
					xmlController.setQtiVersion(1);
					xmlController.displayItemBankXml(questionPool.getDisplayName());
					String qtiString = xmlController.getXmlBean().getXml();
					log.debug("qtiString = " + qtiString);
					b = qtiString.getBytes();
					zos.write(b, 0, b.length);
					zos.closeEntry();

					// imsmanifest.xml
					ze = new ZipEntry("imsmanifest.xml");
					zos.putNextEntry(ze);
					ManifestGenerator manifestGenerator = new ManifestGenerator(Long.parseLong(questionPoolId));
					String manString = manifestGenerator.getManifest();
					log.debug("manString = " + manString);
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
						} catch (IOException e) {
							log.error(e.getMessage());
						}
						try {
							zos.close();
						} catch (IOException e) {
							log.error(e.getMessage());
						}

					}
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
