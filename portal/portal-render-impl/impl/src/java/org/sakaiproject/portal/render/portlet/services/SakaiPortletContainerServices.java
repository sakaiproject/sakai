/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.render.portlet.services;

import javax.portlet.PortalContext;

import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.spi.PortalCallbackService;

/**
 * @author ddwolf
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public class SakaiPortletContainerServices implements RequiredContainerServices
{

	private PortalContext portalContext;

	private PortalCallbackService portalCallbackService;

	public PortalContext getPortalContext()
	{
		return portalContext;
	}

	public void setPortalContext(PortalContext portalContext)
	{
		this.portalContext = portalContext;
	}

	public PortalCallbackService getPortalCallbackService()
	{
		return portalCallbackService;
	}

	public void setPortalCallbackService(PortalCallbackService portalCallbackService)
	{
		this.portalCallbackService = portalCallbackService;
	}
}
