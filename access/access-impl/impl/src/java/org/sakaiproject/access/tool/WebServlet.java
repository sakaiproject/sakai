/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.access.tool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.tool.api.Session;

/**
 * <p>
 * Web extends access: all references are assumed to be under "/content", and POST to add a file is supported.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
@Slf4j
public class WebServlet extends AccessServlet
{
	protected ContentHostingService contentHostingService;
	protected UserDirectoryService userDirectoryService;
	protected TimeService timeService;

	/* (non-Javadoc)
	 * @see org.sakaiproject.access.tool.AccessServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		
		contentHostingService = ComponentManager.get(ContentHostingService.class);
		userDirectoryService = ComponentManager.get(UserDirectoryService.class);
		timeService = ComponentManager.get(TimeService.class);
	}
	/**
	 * Set active session according to sessionId parameter
	 */
	private void setSession( HttpServletRequest req )
	{
		String sessionId = req.getParameter("session");
		if ( sessionId != null)
		{
			Session session = sessionManager.getSession(sessionId);
			
			if (session != null)
			{
				session.setActive();
				sessionManager.setCurrentSession(session);
			}
		}
	}

	/**
	 * respond to an HTTP GET request
	 * 
	 * @param req
	 *			 HttpServletRequest object with the client request
	 * @param res
	 *			 HttpServletResponse object back to the client
	 * @exception ServletException
	 *				  in case of difficulties
	 * @exception IOException
	 *				  in case of difficulties
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		setSession(req);
		super.dispatch(req, res);
	}
	
	/**
	 * respond to an HTTP POST request; only to handle the login process
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// catch the login helper posts
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		if ((parts.length == 2) && ((parts[1].equals("login"))))
		{
			doLogin(req, res, null);
		}

		else if (FileUpload.isMultipartContent(req))
		{
			setSession(req);
			postUpload(req, res);
		}

		else
		{
			sendError(res, HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * Make any changes needed to the path before final "ref" processing.
	 * 
	 * @param path
	 *        The path from the request.
	 * @param req
	 *        The request object.
	 * @return The path to use to make the Reference for further processing.
	 */
	protected String preProcessPath(String path, HttpServletRequest req)
	{
		// everything we work with is down the "content" part of the Sakai access URL space

		// if path is just "/", we don't really know if the request was to .../SERVLET or .../SERVLET/ - we want to preserve the trailing slash
		// the request URI will tell us
		if ("/".equals(path) && !(req.getRequestURI().endsWith("/"))) return "/content";

		return "/content" + path;
	}

	/**
	 * Handle file upload requests.
	 * 
	 * @param req
	 * @param res
	 */
	protected void postUpload(HttpServletRequest req, HttpServletResponse res)
	{
		String path = req.getPathInfo();
		log.debug("path {}", path);
		if (path == null) path = "";
		// assume caller has verified that it is a request for content and that it's multipart
		// loop over attributes in request, picking out the ones
		// that are file uploads and doing them
		for (Enumeration e = req.getAttributeNames(); e.hasMoreElements();)
		{
			String iname = (String) e.nextElement();
			log.debug("Item {}", iname);
			Object o = req.getAttribute(iname);
			// NOTE: Fileitem is from
			// org.apache.commons.fileupload.FileItem, not
			// sakai's parameterparser version
			if (o != null && o instanceof FileItem)
			{
				FileItem fi = (FileItem) o;
				try (InputStream inputStream = fi.getInputStream())
				{
					if (!writeFile(fi.getName(), fi.getContentType(), inputStream, path, req, res, true)) return;
				} catch (IOException ioe) {
					log.warn("Problem getting InputStream", ioe);
				}
			}
		}
	}

	protected boolean writeFile(String name, String type, InputStream inputStream, String dir, HttpServletRequest req,
								HttpServletResponse resp, boolean mkdir)
	{
		try
		{
			// validate filename. Need to be fairly careful.
			int i = name.lastIndexOf(Entity.SEPARATOR);
			if (i >= 0) name = name.substring(i + 1);
			if (name.length() < 1)
			{
				log.debug("no name left / removal");
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return false;
			}

			// do our web thing with the path
			dir = preProcessPath(dir, req);

			// make sure there's a trailing separator
			if (!dir.endsWith(Entity.SEPARATOR)) dir = dir + Entity.SEPARATOR;

			// get a reference to the content collection - this lets us use alias and short refs
			Reference ref = entityManager.newReference(dir);

			// the reference id replaces the dir - as a fully qualified path (no alias, no short ref)
			dir = ref.getId();

			String path = dir + name;

			ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();

			// Try to delete the resource
			try
			{
				log.debug("Trying Del {}", path);
				// The existing document may be a collection or a file.
				boolean isCollection = contentHostingService.getProperties(path).getBooleanProperty(
						ResourceProperties.PROP_IS_COLLECTION);

				if (isCollection)
				{
					log.debug("Can't del, iscoll");
					resp.sendError(HttpServletResponse.SC_FORBIDDEN);
					return false;
				}
				else
				{
					// not sure why removeesource(path) didn't
					// work for my workspace
					ContentResourceEdit edit = contentHostingService.editResource(path);
					// if (edit != null)
					log.debug("Got edit");
					contentHostingService.removeResource(edit);
				}
			}
			catch (IdUnusedException e)
			{
				// Normal situation - nothing to do
			}
			catch (Exception e)
			{
				log.debug("Can't del, exception {}: {}", e.getClass(), e.getMessage());
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return false;
			}

			// Add the resource

			try
			{
				User user = userDirectoryService.getCurrentUser();

				TimeBreakdown timeBreakdown = timeService.newTime().breakdownLocal();
				String mycopyright = "copyright (c)" + " " + timeBreakdown.getYear() + ", " + user.getDisplayName()
						+ ". All Rights Reserved. ";

				resourceProperties.addProperty(ResourceProperties.PROP_COPYRIGHT, mycopyright);

				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

				log.debug("Trying Add {}", path);
				ContentResource resource = contentHostingService.addResource(path, type, inputStream, resourceProperties,
						NotificationService.NOTI_NONE);

			}
			catch (InconsistentException e)
			{
				// get this error if containing dir doesn't exist
				if (mkdir)
				{
					try
					{
						ContentCollection collection = contentHostingService.addCollection(dir, resourceProperties);
						return writeFile(name, type, inputStream, dir, req, resp, false);
					}
					catch (Throwable ee)
					{
					}
				}
				log.debug("Add fail, inconsistent");
				resp.sendError(HttpServletResponse.SC_CONFLICT);
				return false;
			}
			catch (IdUsedException e)
			{
				// Should not happen because we deleted above (unless tawo requests at same time)
				log.debug("Add fail, in use");
				log.warn("access post IdUsedException:" + e.getMessage());

				resp.sendError(HttpServletResponse.SC_CONFLICT);
				return false;
			}
			catch (Exception e)
			{
				log.debug("Add failed, exception {}: {}", e.getClass(), e.getMessage());
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return false;
			}
		}
		catch (IOException e)
		{
			log.debug("overall fail IOException {}", e);
		}
		return true;
	}
}
