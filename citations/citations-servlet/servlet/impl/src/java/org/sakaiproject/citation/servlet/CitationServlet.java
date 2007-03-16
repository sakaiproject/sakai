/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.VmServlet;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationHelper;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 *
 */
public class CitationServlet extends VelocityPortletPaneledAction
{
	/**
	 * 
	 */
	public static final String SUCCESS_TEMPLATE = "sakai_citation-servlet";
	public static final String ERROR_TEMPLATE = "sakai_citation-servlet_err";
	
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(CitationServlet.class);

	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("citation");

	/** set to true when init'ed. */
	protected boolean m_ready = false;

	protected BasicAuth basicAuth = null;

	/** init thread - so we don't wait in the actual init() call */
	public class CitationServletInit extends Thread
	{
		protected CitationService m_citationService;
		
		public void setCitationService(CitationService service)
		{
			this.m_citationService = service;
		}
		
		/**
		 * construct and start the init activity
		 */
		public CitationServletInit()
		{
			m_ready = false;
			start();
		}

		/**
		 * run the init
		 */
		public void run()
		{
			m_ready = true;
		}
	}

	/**
	 * initialize the AccessServlet servlet
	 * 
	 * @param config
	 *        the servlet config parameter
	 * @exception ServletException
	 *            in case of difficulties
	 */
	public void init( ServletConfig config ) throws ServletException
	{
		super.init(config);
		startInit();
		basicAuth = new BasicAuth();
		basicAuth.init();
	}

	/**
	 * Start the initialization process
	 */
	public void startInit()
	{
		new CitationServletInit();
	}

		
	/**
	 * respond to an HTTP POST request
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// process any login that might be present
		basicAuth.doLogin(req);
		// catch the login helper posts
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		if ((parts.length == 2) && ((parts[1].equals("login"))))
		{
			//doLogin(req, res, null);
		}

		else
		{
			//sendError(res, HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * respond to an HTTP GET request
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// process any login that might be present
		basicAuth.doLogin(req);
		// catch the login helper requests
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		if ((parts.length == 2) && ((parts[1].equals("login"))))
		{
			// doLogin(req, res, null);
		}	
		else
		{
			dispatch(req, res);
			
			// include your favorite template
			includeVm(SERVLET_TEMPLATE, req, res);
		}
	}

	/**
	 * handle get and post communication from the user
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	public void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		ParameterParser params = (ParameterParser) req.getAttribute(ATTR_PARAMS);
		
		// get the path info
		String path = params.getPath();
		if (path == null) path = "";

		if (!m_ready)
		{
			// sendError(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}
		
// 		SessionManager sessionManager = (SessionManager) ComponentManager.get("org.sakaiproject.tool.api.SessionManager");
//		String sessionId = sessionManager.getCurrentSession().getId();
//		M_log.info("sessionId == " + sessionId);
//
//		
//		UserDirectoryService userService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
//		User user = userService.getCurrentUser();
//		String userId = user.getId();
//		
//		// String userId = userService.
//		M_log.info("userId == " + userId);
//
//		
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		
		//ToolSession toolSession = SessionManager.getCurrentToolSession();
		//String resourceId = (String) toolSession.getAttribute(CitationHelper.RESOURCE_ID);
		
		String resourceUuid = parts[1];
		//String userId = parts[2];
		
		// pass to doAddCitation
		// doAddCitation( resourceUuid, params );
	}

*/	
	
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		System.out.println( "\nbuilding main panel context..." );
		
		ParameterParser params = rundata.getParameters();
		
		// get the path info
		String path = params.getPath();
		if (path == null) path = "";

		if (!m_ready)
		{
			// sendError(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return ERROR_TEMPLATE;
		}
		
// 		SessionManager sessionManager = (SessionManager) ComponentManager.get("org.sakaiproject.tool.api.SessionManager");
//		String sessionId = sessionManager.getCurrentSession().getId();
//		M_log.info("sessionId == " + sessionId);
//
//		
//		UserDirectoryService userService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
//		User user = userService.getCurrentUser();
//		String userId = user.getId();
//		
//		// String userId = userService.
//		M_log.info("userId == " + userId);
//
//		
		String option = rundata.getRequest().getPathInfo();
		String[] parts = option.split("/");
		
		//ToolSession toolSession = SessionManager.getCurrentToolSession();
		//String resourceId = (String) toolSession.getAttribute(CitationHelper.RESOURCE_ID);
		
		String resourceUuid = parts[1];
		//String userId = parts[2];
		
		// pass to doAddCitation
		if( doAddCitation( resourceUuid, params ) != null )
		{
			return SUCCESS_TEMPLATE;
		}
		else
		{
			return ERROR_TEMPLATE;
		}
		
	}
	
	protected Citation doAddCitation( String resourceUuid, ParameterParser params ) {
		ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		CitationService citationService = (CitationService) ComponentManager.get("org.sakaiproject.citation.api.CitationService");
		
		CitationCollection collection = null;
		Citation citation = null;
		
		try
        {
			String resourceId = contentService.resolveUuid(resourceUuid);
			// edit the collection to verify "content.revise" permission
			// and to get the CitationCollection's id
			ContentResourceEdit edit = contentService.editResource(resourceId);
			
			String collectionId = new String(edit.getContent());
			collection = citationService.getCollection(collectionId);
			
			contentService.cancelResource(edit);

			String genre = params.getString("genre");
			String[] authors = params.getStrings("au");
			String title = params.getString("title");
			String atitle = params.getString("atitle");
			String volume = params.getString("volume");
			String issue = params.getString("issue");
			String pages = params.getString("pages");
			String publisher = params.getString("publisher");
			String date = params.getString("date");
			String id = params.getString("id");

			citation = citationService.addCitation(genre);

			String info = "New citation from Google Scholar:\n\t genre:\t\t" + genre;
			if(title != null)
			{
				info += "\n\t title:\t\t" + title;
				citation.addPropertyValue(Schema.TITLE, title);
			}
			if(authors != null && authors.length > 0)
			{
				for(int i = 0; i < authors.length; i++)
				{
					info += "\n\t au:\t\t" + authors[i];
					citation.addPropertyValue(Schema.CREATOR, authors[i]);
				}
			}
			if(atitle != null)
			{
				info += "\n\t atitle:\t\t" + atitle;
				citation.addPropertyValue(Schema.SOURCE_TITLE, atitle);
			}
			if(volume != null)
			{
				info += "\n\t volume:\t\t" + volume;
				citation.addPropertyValue(Schema.VOLUME, volume);
			}
			if(issue != null)
			{
				info += "\n\t issue:\t\t" + issue;
				citation.addPropertyValue(Schema.ISSUE, issue);
			}
			if(pages != null)
			{
				info += "\n\t pages:\t\t" + pages;
				citation.addPropertyValue(Schema.PAGES, pages);
			}
			if(publisher != null)
			{
				info += "\n\t publisher:\t\t" + publisher;
				citation.addPropertyValue(Schema.PUBLISHER, publisher);
			}
			if(date != null)
			{
				info += "\n\t date:\t\t" + date;
				citation.addPropertyValue(Schema.YEAR, date);
			}
			if(id != null)
			{
				info += "\n\t id:\t\t" + id;
				citation.addPropertyValue(Schema.ISN, id);
			}
			info += "\n";
			
			collection.add(citation);
			citationService.save(collection);
			
			M_log.info(info);
			
        }
        catch (PermissionException e)
        {
	        // TODO Auto-generated catch block
	        M_log.warn("PermissionException ", e);
	        
	        return null;
        }
        catch (IdUnusedException e)
        {
	        // TODO Auto-generated catch block
	        M_log.warn("IdUnusedException ", e);
	        
	        return null;
        }
        catch (TypeException e)
        {
	        // TODO Auto-generated catch block
	        M_log.warn("TypeException ", e);
	        
	        return null;
        }
        catch (InUseException e)
        {
	        // TODO Auto-generated catch block
        	M_log.warn("InUseException", e);
        	
        	return null;
        }
        catch (ServerOverloadException e)
        {
	        // TODO Auto-generated catch block
        	M_log.warn("ServerOverloadException ", e);
        	
        	return null;
        }
        
        // no exceptions, return true
        return citation;
	}
}
