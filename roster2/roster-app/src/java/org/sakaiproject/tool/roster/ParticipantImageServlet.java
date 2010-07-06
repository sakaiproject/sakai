/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-api/api/src/java/org/sakaiproject/presence/api/PresenceService.java $
 * $Id: PresenceService.java 7844 2006-04-17 13:06:02Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.roster;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.PhotoService;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

public class ParticipantImageServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3420767508601031864L;
	private ResourceLoader msgs = new ResourceLoader("org.sakaiproject.tool.roster.bundle.Messages");
	private static final Log LOG = LogFactory.getLog(ParticipantImageServlet.class);
	private static final String UNIVERSITY_ID_PHOTO = "photo";
	private static final String CONTENT_TYPE = "image/jpeg";
	private PhotoService photoService;

	public void init() throws ServletException
	{
		// Initialize photo service.
		photoService = (PhotoService)ComponentManager.get(PhotoService.class.getName());
		if (photoService == null)
		{
			photoService = (PhotoService)ComponentManager.get("org.sakaiproject.component.app.roster.ProfilePhotoService");
		}
		if (LOG.isDebugEnabled()) LOG.debug("init : photoService=" + photoService);
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 *
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		if (LOG.isDebugEnabled())
			LOG.debug("doGet(HttpServletRequest" + request + ", HttpServletResponse"
					+ response + ")");
		response.setContentType(CONTENT_TYPE);
		String userId = null;
		OutputStream stream = response.getOutputStream();
		userId = (String) request.getParameter(UNIVERSITY_ID_PHOTO);
		if (userId != null && userId.trim().length() > 0)
		{
			displayUniversityIDPhoto(userId, stream, response);
		}
		else
		{
			displayLocalImage(stream);
		}

	}

	private void displayUniversityIDPhoto(String userId, OutputStream stream,
			HttpServletResponse response)
	{
		if (LOG.isDebugEnabled())
			LOG.debug("displayUniversityIDPhoto (String " + userId
					+ "OutputStream stream, HttpServletResponse response)");
		try
		{
			
			String siteid = ToolManager.getCurrentPlacement().getContext();
		
			// Two possible permission scenarios: 
			// 1. Official photos are being displayed (user has permission to view for this site)
			// 2. User profile photos are being displayed, and the target user has elected to 
			//    use his/her official photo as user profile photo
			
			byte[] displayPhoto = photoService.getPhotoAsByteArray(userId, SecurityService.unlock(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALPHOTO, SiteService.siteReference(siteid)));
 
			if (displayPhoto != null && displayPhoto.length > 0)
			{
				LOG.debug("Display University ID photo for user:" + userId);
				response.setContentLength(displayPhoto.length);
				stream.write(displayPhoto);
				stream.flush();

			}
			else
			{
				LOG.debug("Display University ID photo for user:" + userId
						+ " is unavailable");
				displayLocalImage(stream);
			}
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage(), e);
		}

	}


	private void displayLocalImage(OutputStream stream)
	{
		String unavailable_image = msgs.getString("img_off_unavail");

		if (LOG.isDebugEnabled())
			LOG.debug("displayPhotoUnavailable(OutputStream" + stream + ", String "
					+ unavailable_image + ")");
		try
		{
			BufferedInputStream in = null;
			try
			{

				in = new BufferedInputStream(new FileInputStream(getServletContext()
						.getRealPath(unavailable_image)));
				int ch;

				while ((ch = in.read()) != -1)
				{
					stream.write((char) ch);
				}
			}

			finally
			{
				if (in != null) in.close();
			}
		}
		catch (FileNotFoundException e)
		{
			LOG.error(e.getMessage(), e);
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage(), e);
		}
	}

}
