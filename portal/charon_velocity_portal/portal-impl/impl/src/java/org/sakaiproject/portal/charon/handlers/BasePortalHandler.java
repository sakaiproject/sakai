/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
 * @author ieb
 */
public abstract class BasePortalHandler implements PortalHandler
{

	public BasePortalHandler()
	{
		urlFragment = "none";
	}

	protected PortalService portalService;

	protected Portal portal;

	protected String urlFragment;

	protected ServletContext servletContext;

	public abstract int doGet(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session) throws PortalHandlerException;

	/*
	 * Make sure to override this and call doGet() if this handler wants to
	 * support JSR-168 portlets
	 */
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

	public String getUrlFragment()
	{
		return urlFragment;
	}

	public void setUrlFragment(String urlFagment)
	{
		this.urlFragment = urlFagment;
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

}
