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

package org.sakaiproject.portal.charon.handlers;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.tool.api.Session;

/**
 * Abstract class to hold common base methods for portal handlers.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public abstract class BasePortalHandler implements PortalHandler
{

	public BasePortalHandler()
	{
		urlFragment = "none";
	}

	protected PortalService portalService;

	protected Portal portal;

	private String urlFragment;

	protected ServletContext servletContext;

	public abstract int doGet(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session) throws PortalHandlerException;

	// TODO: Go through and make sure to remove and test the mistaken code that
	// simply
	// calls doGet in doPost()
	public int doPost(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		return NEXT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.charon.PortalHandler#deregister(org.sakaiproject.portal.charon.Portal)
	 */
	public void deregister(Portal portal)
	{
		this.portal = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.charon.PortalHandler#register(org.sakaiproject.portal.charon.Portal)
	 */
	public void register(Portal portal, PortalService portalService,
			ServletContext servletContext)
	{
		this.portal = portal;
		this.portalService = portalService;
		this.servletContext = servletContext;

	}

	/**
	 * @return the servletContext
	 */
	public ServletContext getServletContext()
	{
		return servletContext;
	}

	/**
	 * @param servletContext
	 *        the servletContext to set
	 */
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	/**
	 * @return the urlFragment
	 */
	public String getUrlFragment()
	{
		return urlFragment;
	}

	/**
	 * @param urlFragment the urlFragment to set
	 */
	public void setUrlFragment(String urlFragment)
	{
		this.urlFragment = urlFragment;
	}

}
