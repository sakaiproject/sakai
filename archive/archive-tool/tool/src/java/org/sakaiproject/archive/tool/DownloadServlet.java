package org.sakaiproject.archive.tool;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
		}
		
		String archiveName = (String)request.getParameter("archive");
		String archiveBaseDir = serverConfigurationService.getString("archive.storage.path", "sakai/archive");
		
		//add in some basic protection
		if(StringUtils.contains(archiveName, "..")) {
			log.error("Archive param must be a valid site archive");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Archive param must be a valid site archive. Param was: " + archiveName);
		}
		
		response.setContentType("application/zip");
        response.setHeader("Content-Disposition","attachment;filename=" +archiveName);
		
		//need to ensure user cant spoof this to get other files. Check for .. in name?
		//does this even matter? Surely we can trust an admin?
		String archivePath = archiveBaseDir + File.separator + archiveName;
		File archiveFile = new File(archivePath);
		
		OutputStream out = response.getOutputStream();
		
        InputStream in = FileUtils.openInputStream(archiveFile);
        IOUtils.copyLarge(in, out);
        out.flush();
        out.close();
        		
        
	}
}
