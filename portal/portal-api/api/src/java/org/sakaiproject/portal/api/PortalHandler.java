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

package org.sakaiproject.portal.api;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.Session;

/**
 * Tools that want to add handlers into the portal URL space may impliment this
 * interface. The once injected into the portal the portal will invoke the
 * register and deregister methods as part of the life cycle.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public interface PortalHandler
{

	/**
	 * Return codes, stop processing immediately
	 */
	public static final int ABORT = 0;

	/**
	 * Stop processing
	 */
	public static final int END = 1;

	/**
	 * try next handler
	 */
	public static final int NEXT = 2;

	/**
	 * stop processing and mark reset as done
	 */
	public static final int RESET_DONE = 3;

	/**
	 * Perform a get, the method should inspect parts[] and other parameters to
	 * determin if it should perform the operation, returning one of the above
	 * codes
	 * 
	 * @param parts
	 * @param req
	 * @param res
	 * @param session
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 * @throws ToolHandlerException
	 */
	int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException;

	/**
	 * get the fragment of the URL that represents part[1] and is used to
	 * register the handler in the portal.
	 * 
	 * @return
	 */
	String getUrlFragment();

	/**
	 * deregister the the portal, invoked by the portal
	 * 
	 * @param portal
	 */
	void deregister(Portal portal);

	/**
	 * register this handler with the portal, invoked by the portal
	 * 
	 * @param portal
	 * @param portalService
	 * @param servletContext
	 */
	void register(Portal portal, PortalService portalService,
			ServletContext servletContext);

	/**
	 * perform a post but only accept it the handler accepts a post.
	 * 
	 * @param parts
	 * @param req
	 * @param res
	 * @param session
	 * @return
	 * @throws PortalHandlerException
	 */
	int doPost(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException;
}
