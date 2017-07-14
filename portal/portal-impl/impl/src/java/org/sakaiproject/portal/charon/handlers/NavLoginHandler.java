/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.tool.api.Session;

/**
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class NavLoginHandler extends SiteHandler
{
	
	private static final String URL_FRAGMENT = "nav_login";

	public NavLoginHandler()
	{
		setUrlFragment(NavLoginHandler.URL_FRAGMENT);
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if ((parts.length == 3) && (parts[1].equals(NavLoginHandler.URL_FRAGMENT)))
		{
			try
			{
				doNavLogin(req, res, session, parts[2]);
				return END;
			}
			catch (Exception ex)
			{
				throw new PortalHandlerException(ex);
			}
		}
		else
		{
			return NEXT;
		}
	}

	public void doNavLogin(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId) throws IOException
	{
		// start the response
		PortalRenderContext rcontext = portal.startPageContext("", "Login", null, req, null);

		includeLogo(rcontext, req, session, siteId);

		portal.sendResponse(rcontext, res, "login", null);
	}

}
