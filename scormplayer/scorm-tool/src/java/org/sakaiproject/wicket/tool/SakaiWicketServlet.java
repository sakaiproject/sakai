/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.tool;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.WicketServlet;

import org.sakaiproject.tool.api.Tool;

public class SakaiWicketServlet extends WicketServlet
{
	public static final String FIRST_PAGE = "first-page";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		final String contextPath = req.getContextPath();

		if ("GET".equals(req.getMethod()))
		{
			String myFirstPage = getInitParameter( FIRST_PAGE );
			String myPathInfo = req.getPathInfo();

			if ( (myPathInfo == null) && (myFirstPage != null) && (!myFirstPage.equals("/")) )
			{
				if (!myFirstPage.startsWith("/"))
				{
					myFirstPage = "/" + myFirstPage;
				}

				resp.sendRedirect(contextPath + myFirstPage);
			}
			else if (myPathInfo != null && (myPathInfo.startsWith("/WEB-INF/") || myPathInfo.equals("/WEB-INF")))
			{
				resp.sendRedirect(contextPath + "/");
			}

			req.removeAttribute(Tool.NATIVE_URL);
		}

		super.service(req, resp);
	}
}
