/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tinyurl/trunk/tool/src/java/org/sakaiproject/tinyurl/tool/TinyUrlServlet.java $
 * $Id: TinyUrlServlet.java 64400 2009-11-03 13:21:08Z steve.swinsburg@gmail.com $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.shortenedurl.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

/**
 * This is the resolver servlet for the ShortenedUrlService which parses the path, resolves the key to it's original URL,
 * then redirects to the that URL.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ResolverServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private ShortenedUrlService service;
  
	/**
	 * Get TinyUrlService bean from the Spring ComponentManager
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ComponentManager manager = org.sakaiproject.component.cover.ComponentManager.getInstance();
		service = (ShortenedUrlService) manager.get(ShortenedUrlService.class);
	}

	/** 
	 * Process path, get the original URL and redirect or return HTTP error response
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		String[] parts = pathInfo.split("/");
		
		/**
		 * If insufficient data or too many params
		 */
		if(parts.length < 2) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter for ShortenedUrlService");
		} else if (parts.length > 2) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL for ShortenedUrlService");
		} else {
			String id = parts[1]; //always,
			String url = service.resolve(id);
			
			if (url == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No resource found matching " + id);
			} else {
				response.sendRedirect(url);
			}
		}
	}
}
