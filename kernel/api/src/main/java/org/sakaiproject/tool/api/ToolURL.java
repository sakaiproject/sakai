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

import java.util.Map;

/**
 * A ToolURL is used to create a URL and encode it appropriate to the context and placement of the tool.
 * 
 * @author <a href="mailto:vgoenka@sungardsct.com">Vishal Goenka</a>
 */
public interface ToolURL
{
	/**
	 * Property name to retrieve an instance of ToolURLManager from an HttpServletRequest
	 */
	String MANAGER = "tool.url.manager";

	/**
	 * Property name to set the HttpServletRequest in a given thread context, to default to when it is not available to the caller. This allows calling ToolURLManager's create&lt;Type&gt;URL with a null HttpRequestServlet, if one has been set in the
	 * current thread context. We use the same attribute name as set in org.sakaiproject.util.RequestFilter to prevent having to depend on RequestFilter class only to get this attribute. Is this a bad idea??
	 */
	String HTTP_SERVLET_REQUEST = "org.sakaiproject.util.RequestFilter.http_request";

	/**
	 * Set path for this URL. Path can either be absolute with respect to the server or relative to the servlet context in which this tool is placed.
	 * 
	 * @param path
	 *        path relative to the tool
	 */
	void setPath(String path);

	/**
	 * Sets the given String parameter to this URL. This method replaces all parameters with the given key. An implementation of this interface may prefix the attribute names internally in order to preserve a unique namespace for the tool
	 * 
	 * @param name
	 *        the parameter name
	 * @param value
	 *        the parameter value
	 */
	void setParameter(String name, String value);

	/**
	 * Sets the given String array parameter to this URL. This method replaces all parameters with the given key. An implementation of this interface may prefix the attribute names internally in order to preserve a unique namespace for the tool
	 * 
	 * @param name
	 *        the parameter name
	 * @param values
	 *        the parameter values
	 */
	void setParameter(String name, String[] values);

	/**
	 * Sets a parameter map for this URL. All previously set parameters are cleared. An implementation of this interface may prefix the attribute names internally in order to preserve a unique namespace for the tool
	 * 
	 * @param parameters
	 *        Map containing parameter names as keys and parameter values as map values. The keys in the parameter map must be of type String. The values in the parameter map must be of type String array (String[]).
	 */
	void setParameters(Map<String, String[]> parameters);

	/**
	 * Returns the URL string representation to be embedded in the markup. Note that the returned String may not be a valid URL, as it may be rewritten by the portal/portlet-container before returning the markup to the client.
	 * 
	 * @return the encoded URL as a String
	 */
	String toString();
}
