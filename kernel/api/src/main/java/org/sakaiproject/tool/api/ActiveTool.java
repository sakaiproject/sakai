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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Extension to tool to introduce Servlet API specific tool activity.
 * </p>
 */
public interface ActiveTool extends Tool
{
	/** A tool session attribute where the placement's current destination (URL path) is stored. */
	final static String TOOL_ATTR_CURRENT_DESTINATION = "sakai:tool:current_destination";

	/**
	 * Invoke the tool to handle the complete request
	 * 
	 * @param req
	 *        The request.
	 * @param res
	 *        The response.
	 * @param placement
	 *        The tool placement for this request.
	 * @param toolContext
	 *        The (optional) servlet context path that is given to the tool.
	 * @param toolPath
	 *        The (optional) servlet pathInfo that is given to the tool.
	 * @throws ToolException
	 *         if there's any trouble running the tool.
	 */
	void forward(HttpServletRequest req, HttpServletResponse res, Placement placement, String toolContext, String toolPath)
			throws ToolException;

	/**
	 * Invoke the tool to handle the request by producing a fragment
	 * 
	 * @param req
	 *        The request.
	 * @param res
	 *        The response.
	 * @param placement
	 *        The tool placement for this request.
	 * @param toolContext
	 *        The (optional) servlet context path that is given to the tool.
	 * @param toolPath
	 *        The (optional) servlet pathInfo that is given to the tool.
	 * @throws ToolException
	 *         if there's any trouble running the tool.
	 */
	void include(HttpServletRequest req, HttpServletResponse res, Placement placement, String toolContext, String toolPath)
			throws ToolException;

	/**
	 * Invoke the tool to handle the complete request as a helper. Note, the placement is shared between invoker and invoked.
	 * 
	 * @param req
	 *        The request.
	 * @param res
	 *        The response.
	 * @param toolContext
	 *        The (optional) servlet context path that is given to the tool.
	 * @param toolPath
	 *        The (optional) servlet pathInfo that is given to the tool.
	 * @throws ToolException
	 *         if there's any trouble running the tool.
	 */
	void help(HttpServletRequest req, HttpServletResponse res, String toolContext, String toolPath) throws ToolException;
}
