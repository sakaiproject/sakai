/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.tool.api;

/**
 * The ToolURLManager interface allows creation of ToolURL that reference the portlet itself. Sakai tools assume the servlet APIs as the basis for generating markup and <br />
 * getting parameters, yet for presentation inside different portals, the URL <br />
 * encoding API javax.servlet.http.HttpServletResponse#encodeURL is not <br />
 * sufficient. This is because the Servlet API treats all URLs the same (hence a <br />
 * single encodeURL method), whereas portlet technologies such as JSR-168 and <br />
 * WSRP differentiate between URLs based on what they represent. There are <br />
 * primarily three different URL types as distinguished by WSRP (JSR 168 has 2, <br />
 * which is a subset of the three types distinguished by WSRP). The only <br />
 * reasonable way to allow tools to generate markup that can be presented in a <br />
 * portlet is to have the tools differentiate the URLs that are embedded in the <br />
 * markup. Some of this can be done automatically if the URLs are generated <br />
 * using macros or other APIs that allows for this differentiation to be plugged <br />
 * underneath. For instance, most of velocity based tools used different macros <br />
 * for different URL types, so it is possible to plug the appropriate URL <br />
 * encoding underneath the macros when the tool is being rendered as a portlet. <br />
 * However, tools that directly access Servlet APIs to generate markup must use <br />
 * these APIs directly. <br />
 * <br />
 * Using these APIs is simple. Instead of creating a String URL with the <br />
 * parameters, you create a ToolURL object. You must decide whether the URL type <br />
 * (render, action or resource) to create a ToolURL object. You can then set the <br />
 * request path and request parameters by using methods in ToolURL. Finally, to <br />
 * include it in the generated markup, you convert the ToolURL to a String using <br />
 * the ToolURL#toString method. <br />
 * 
 * @author <a href="mailto:vgoenka@sungardsct.com">Vishal Goenka</a>
 */
public interface ToolURLManager
{
	/**
	 * Create a URL that is a hyperlink back to this tool. HTTP GET requests initiated by simple &lt;a href&gt; construct falls in this category.
	 * 
	 * @return a URL that is a hyperlink back to this tool
	 */
	ToolURL createRenderURL();

	/**
	 * Create a URL that is an action performed on this tool. HTML Form actions that initiate an HTTP POST back to the tool falls in this category.
	 * 
	 * @return a URL that is an action performed on this tool
	 */
	ToolURL createActionURL();

	/**
	 * Create a URL for a resource related to the tool, but not necessarily pointing back to the tool. Image files, CSS files, JS files etc. are examples of Resource URLs. Paths for resource URLs may have to be relative to the server, as opposed to being
	 * relative to the tool.
	 * 
	 * @return a URL for a resource
	 */
	ToolURL createResourceURL();
}
