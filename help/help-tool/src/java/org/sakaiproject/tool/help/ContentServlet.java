/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.help;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.api.app.help.Resource;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * Content Servlet serves help documents to document frame.
 * @version $Id$
 */
@Slf4j
public class ContentServlet extends HttpServlet
{

  private static final long serialVersionUID = 1L;

  private static final String DOC_ID = "docId";
  
  private static final String TEXT_HTML = "text/html; charset=UTF-8";
  private HelpManager helpManager;
  private ServerConfigurationService serverConfigurationService;

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

      getHelpManager().initialize();
      String docId = req.getParameter(DOC_ID);

      if (docId == null) {
          res.sendError(HttpServletResponse.SC_BAD_REQUEST);
	  return;
      }

      OutputStreamWriter writer = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
      try {
          res.setContentType(TEXT_HTML);

          URL url = null;
          Resource resource = null;
          
       	  resource = getHelpManager().getResourceByDocId(docId);
          //Possibly a fileURL
          if (resource == null && docId != null && docId.indexOf('/') >= 0) {
        	  if (log.isDebugEnabled())
        		  log.debug("Adding new resource:"+docId);
        	  resource = getHelpManager().createResource();
        	  resource.setLocation("/"+docId);
        	  resource.setDocId(docId);
        	  url = new URL(req.getScheme(),req.getServerName(),req.getServerPort(),req.getContextPath()+"/"+docId);
        	  //Can't save it without a category as is null
        	  //getHelpManager().storeResource(resource);
          } 
          
          if (resource != null)
          {
    	  		String sakaiHomePath = getServerConfigurationService().getSakaiHomePath();
    	  		String localHelpPath = sakaiHomePath+getServerConfigurationService().getString("help.localpath","/help/");
    	  		File localFile = new File(localHelpPath+resource.getLocation());
    	  		boolean localFileIsFile = false;
    	  		String localFileCanonicalPath = localFile.getCanonicalPath();
    	  		if(localFileCanonicalPath.contains(localHelpPath) && localFile.isFile()) { 
    	  			log.debug("Local help file overrides: "+resource.getLocation());
    	  			localFileIsFile = true;
    	  		}

              if (!getHelpManager().getRestConfiguration().getOrganization()
                      .equalsIgnoreCase("sakai"))
              {
                  writer.write(RestContentProvider.getTransformedDocument(
                          getServletContext(), getHelpManager(), resource));
              } else {
                  if (resource.getLocation().startsWith("/"))
                  {
                      if (!"".equals(getHelpManager().getExternalLocation()))
                      {
                          url = new URL(getHelpManager().getExternalLocation()
                                  + resource.getLocation());
                      }
                      else
                      {
                    	  if(localFileIsFile) { 
                    		  url = localFile.toURI().toURL();
                    	  }
                    	  else {
                    		  //If url hasn't been set yet, look it up in the classpath
                    		  if (url == null) {
                    			  url = HelpManager.class.getResource(resource.getLocation());
                    		  }
                    	  }
                	  }
                      String defaultRepo = "/library/skin/";
                      String skinRepo = getServerConfigurationService().getString("skin.repo",defaultRepo);
                      String helpHeader = getServerConfigurationService().getString("help.header",null);
                      String helpFooter = getServerConfigurationService().getString("help.footer",null);
                      String resourceName = resource.getName();
                      if (resourceName == null) {
                    	  resourceName = "";
                      }
                    	  

                      if (url == null) {
                    	  log.warn("Help document " + docId + " not found at: " + resource.getLocation());
                      } else {
                    	  BufferedReader br = null;
                    	  try {
                    		  br = new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
                    	  }
                    	  catch (ConnectException e){
                    		  log.info("ConnectException on " + url.getPath());
                    		  res.sendRedirect(resource.getLocation());
                    		  return;
                    	  }
	                      try {
	                          String sbuf;
	                          while ((sbuf = br.readLine()) != null)
	                          {
								  //Replacements because help wasn't written as a template
	                        	  if (!skinRepo.equals(defaultRepo)) {
	                        		  if (StringUtils.contains(sbuf,defaultRepo)) {
	                        			  sbuf = StringUtils.replace(sbuf, defaultRepo, skinRepo + "/");
	                        			  //Reset to only do one replacement
	                        			  skinRepo=defaultRepo;
	                        		  }
	                        	  }

	                        	  if (helpHeader != null) {
	                        		  //Hopefully nobody writes <BODY>
	                        		  if (StringUtils.contains(sbuf,"<body>")) {
	                        			  sbuf = StringUtils.replace(sbuf, "<body>", "<body>"+helpHeader);
	                        			  //Reset to only do one replacement
	                        			  //Replace special variables 
	                        			  sbuf = StringUtils.replace(sbuf, "#ResourceBean.name", resourceName);
	                        			  helpHeader = null;
	                        		  }
	                        	  }
	                        	  if (helpFooter != null) {
	                        		  if (StringUtils.contains(sbuf,"</body>")) {
	                        			  sbuf = StringUtils.replace(sbuf, "</body>", helpFooter+"</body>");
	                        			  sbuf = StringUtils.replace(sbuf, "#ResourceBean.name", resourceName);
	                        			  //Reset to only do one replacement
	                        			  helpFooter = null;
	                        		  }
	                        	  }
															
	                            writer.write( sbuf );
	                            writer.write( System.getProperty("line.separator") );
	                          }
	                      } finally {
	                          br.close();
	                      }
                      }
                  }
                  else
                  {
                      res.sendRedirect(resource.getLocation());
                  }
              }
          } 
      } finally {
          try {
              writer.flush();
          } catch (IOException e) {
              // ignore
          }
          writer.close();
      }
  }

  /**
   * get the component manager through cover
   * @return help manager
   */
  public HelpManager getHelpManager()
  {
    if (helpManager == null)
    {
    	helpManager = (HelpManager) ComponentManager.get(HelpManager.class.getName()); 
    	return helpManager;
    }
    return helpManager;
  }

  /**
   * get the component manager through cover
   * @return serverconfigurationservicer
   */
  public ServerConfigurationService getServerConfigurationService()
  {
    if (serverConfigurationService == null)
    {
    	serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class.getName()); 
    	return serverConfigurationService;
    }
    return serverConfigurationService;
  }
}

