package org.sakaiproject.archive.tool;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
@CommonsLog
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
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (!securityService.isSuperUser()){
			log.error("Must be super user to download archives");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Must be super user to download archives");
			return;
		}
		
		String archiveName = request.getParameter("archive");
		Path sakaiHome = Paths.get(serverConfigurationService.getSakaiHomePath());
		Path archives = sakaiHome.resolve(serverConfigurationService.getString("archive.storage.path", "archive"));
		
		response.setContentType("application/zip");
        response.setHeader("Content-Disposition","attachment;filename=" +archiveName);
		
		Path archivePath = archives.resolve(archiveName).normalize();
		if (!archivePath.startsWith(archives)) {
			log.error(String.format("The archive file (%s) is not inside the archives folder (%s)",
			archivePath.toString(), archives.toString()));
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Archive param must be a valid site archive. Param was: " + archiveName);
			return;
		}
		OutputStream out = response.getOutputStream();

		try (InputStream in = FileUtils.openInputStream(archivePath.toFile())) {
			IOUtils.copyLarge(in, out);
			out.flush();
			out.close();
		}
	}
}
