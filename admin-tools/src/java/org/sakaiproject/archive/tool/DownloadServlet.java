/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.archive.tool;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Download servlet for archive downloads. Restricted to super user as per normal archive tool
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private SecurityService securityService;
	private ServerConfigurationService serverConfigurationService;
	private SessionManager sessionManager;
  
	/**
	 * inject dependencies
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ComponentManager manager = org.sakaiproject.component.cover.ComponentManager.getInstance();
		
		if(securityService == null) {
			securityService = (SecurityService) manager.get(SecurityService.class.getName());
		}
		if(serverConfigurationService == null) {
			serverConfigurationService = (ServerConfigurationService) manager.get(ServerConfigurationService.class.getName());
		}
		if(sessionManager == null) {
			sessionManager = (SessionManager) manager.get(SessionManager.class.getName());
		}
	}

	/** 
	 * get file
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (!securityService.isSuperUser()){
			log.error("Must be super user to download archives");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Must be super user to download archives");
			return;
		}
		
		String archiveName = request.getParameter("archive");
		if (archiveName == null || archiveName.isEmpty()) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "You must supply a archive name");
			return;
		}
		Path sakaiHome = Paths.get(serverConfigurationService.getSakaiHomePath());
		Path archives = sakaiHome.resolve(serverConfigurationService.getString("archive.storage.path", "archive"));
		

		Path archivePath = archives.resolve(archiveName).normalize();
		if (!archivePath.startsWith(archives)) {
			log.error(String.format("The archive file (%s) is not inside the archives folder (%s)",
			archivePath.toString(), archives.toString()));
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Archive param must be a valid site archive. Param was: " + archiveName);
			return;
		}
		if (!Files.exists(archivePath)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition","attachment;filename=" +archiveName);
		OutputStream out = response.getOutputStream();
		
		try (InputStream in = FileUtils.openInputStream(archivePath.toFile())) {
			IOUtils.copyLarge(in, out);
			out.flush();
			out.close();
		}
	}
}
