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

package org.sakaiproject.search.tool;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.search.api.SearchService;

/**
 * Serializes a search result out to the requesting service
 * 
 * @author ieb
 */
@Slf4j
public class RestSearchServlet extends HttpServlet
{

    public static final String REQUEST_PARAM_CTX = "ctx";
    public static final String REQUEST_PARAM_SCOPE = "scope";
    public static final String REQUEST_PARAMETER_Q = "q";

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
        // /sakai-search-tool/xmlsearch/suggestion
        String result;
        String[] parts = request.getRequestURI().split("/");
        if (parts.length == 4 && "suggestion".equalsIgnoreCase(parts[3])) {
            boolean searchAllMySites = true;
            String currentSiteId = null;
            if (request.getParameter(REQUEST_PARAM_SCOPE) != null) {
                if ("SITE".equalsIgnoreCase(request.getParameter(REQUEST_PARAM_SCOPE)))  {
                    searchAllMySites = false;
                }
            }
            if (request.getParameter(REQUEST_PARAM_CTX) != null) {
                currentSiteId = request.getParameter(REQUEST_PARAM_CTX);
            }

            String[] suggestions = searchService.getSearchSuggestions(request.getParameter(REQUEST_PARAMETER_Q), currentSiteId, searchAllMySites);
            response.setContentType("application/json");
            result = stringArrayAsJson(suggestions);
        // /sakai-search-tool/xmlsearch
        } else {
			result = searchService.searchXML(request.getParameterMap());
            response.setContentType("text/xml");
        }
        Writer out = response.getWriter();
        out.write(result);

    }

    private String stringArrayAsJson(String[] strArray) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        int i=0;
        for (String item: strArray) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"" + item + "\"");
            i++;
        }
        sb.append("]");
        return sb.toString();
    }


}
