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

package org.sakaiproject.portal.render.portlet.servlet;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

// This utility class is so that the different servlet wrappers can share code

/**
 * @author ddwolf
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public class SakaiServletUtil
{

	public static boolean isUserInRole(String string, PortletState state)
	{
		if (string == null) return false;
		if (string.equalsIgnoreCase("admin") && SecurityService.isSuperUser())
			return true;
		// Gridsphere convention
		if (string.equalsIgnoreCase("super") && SecurityService.isSuperUser())
			return true;

		String placementId = state.getId();

		// find the tool from some site
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		if (siteTool == null) return false;

		String siteId = siteTool.getSiteId();

		String siteReference = SiteService.siteReference(siteId);

		if (SecurityService.unlock(string, siteReference)) return true;

		Session session = SessionManager.getCurrentSession();

		if (session == null) return false;

		String userId = session.getUserId();

		// Fall through to roles
		try
		{
			Site site = SiteService.getSite(siteId);
			Role role = site.getUserRole(userId);
			if (role == null) return false;
			if ( string.equalsIgnoreCase(role.getId()) ) return true;
		}
		catch (IdUnusedException e)
		{
			return false;
		}

		// One last mapping for IMS Enterprise Role compatibility

		// "Admin" is handled above

		// The ideal way to handle Student and Instructor is to 
		// Make functions or roles in the site - this allows the 
		// support of any IMS Enterprise role such as Observer
		// or Mentor.  However this will be uncommon and project
		// sites will never have these defined - so if we encounter
		// The IMS Standard roles "Student" or "Instructor" and
		// we have fallen down to here, we fall back to the venerable
		// "site.upd" and "site.visit"

		if (string.equalsIgnoreCase("student") && 
		    SecurityService.unlock(SiteService.SITE_VISIT, siteReference) ) return true;

		if (string.equalsIgnoreCase("instructor") && 
		    SecurityService.unlock(SiteService.SECURE_UPDATE_SITE, siteReference) ) return true;

		// So sorry - no matter how hard we tried - you are not in this role
		return false;
	}
}
