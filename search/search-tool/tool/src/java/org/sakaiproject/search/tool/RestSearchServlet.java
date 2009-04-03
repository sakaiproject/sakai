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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.tool;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.search.api.SearchService;

/**
 * Serializes a search result out to the requesting service
 * 
 * @author ieb
 */
public class RestSearchServlet extends HttpServlet
{

	private static final Log log = LogFactory.getLog(RestSearchServlet.class);

	private SearchService searchService;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		searchService = (SearchService) load(cm, SearchService.class.getName());

	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		execute(request, response);
	}


	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
/*
		String test = (String) request.getParameter("test");
		if ( "true".equals(test) ) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body><form method=\"post\" action=\"?\" >");
			sb.append("CheckSum <input type=\"text\" name=\"").append(SearchService.REST_CHECKSUM).append("\" /> <br />");
			sb.append("Contexts <input type=\"text\" name=\"").append(SearchService.REST_CONTEXTS).append("\" /> <br />");
			sb.append("Start <input type=\"text\" name=\"").append(SearchService.REST_START).append("\" /> <br />");
			sb.append("End <input type=\"text\" name=\"").append(SearchService.REST_END).append("\" /> <br />");
			sb.append("Terms <input type=\"text\" name=\"").append(SearchService.REST_TERMS).append("\" /> <br />");
			sb.append("UserID <input type=\"text\" name=\"").append(SearchService.REST_USERID).append("\" /> <br />");
			sb.append("<input type=\"hidden\" name=\"test\" value=\"act1\" /> <br />");
			sb.append("<input type=\"submit\" name=\"submit\" value=\" Go \" />");
			sb.append("</form></body>");
			response.setContentType("text/html");
			Writer out = response.getWriter();
			out.write(sb.toString());
		} else {
*/
			String result = searchService.searchXML(request.getParameterMap());
			response.setContentType("text/xml");
			Writer out = response.getWriter();
			out.write(result);
//		}

	}


}
