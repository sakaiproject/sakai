package org.sakaiproject.portal.render.portlet.servlet;

import org.sakaiproject.portal.render.portlet.services.state.PortletState;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.authz.api.Role;

import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Session;

import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.Placement;

import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.api.Site;

import org.sakaiproject.exception.IdUnusedException;

// import org.sakaiproject.thread_local.api.ThreadLocalManager;

// This utility class is so that the different servlet wrappers can share code

public class SakaiServletUtil 
{

	public static boolean isUserInRole(String string, PortletState state)
	{
		if ( string == null ) return false;
		if ( string.equalsIgnoreCase("admin") && SecurityService.isSuperUser() ) return true;
		// Gridsphere convention
		if ( string.equalsIgnoreCase("super") && SecurityService.isSuperUser() ) return true;

		String placementId = state.getId();
		// System.out.println("state.getId()="+placementId);

                // find the tool from some site
                ToolConfiguration siteTool = SiteService.findTool(placementId);
            	// System.out.println("siteTool="+siteTool);
		if ( siteTool == null ) return false;

                String siteId = siteTool.getSiteId();
            	// System.out.println("siteId="+siteId);

		String siteReference = SiteService.siteReference(siteId);
            	// System.out.println("Reference="+siteReference);

		if ( SecurityService.unlock(string,siteReference) ) return true;

		Session session = SessionManager.getCurrentSession();
		// System.out.println("Session = " + session);

		if ( session == null ) return false;

		String userId = session.getUserId();
		// System.out.println("userId = "+userId);

		// Fall through to roles
		try { 
		    Site site = SiteService.getSite(siteId);
		    // System.out.println("Site = "+site);
		    Role role = site.getUserRole(userId);
		    // System.out.println("Role = "+role);
		    if ( role == null ) return false;
		    // System.out.println("Role = "+role.getId());
		    return string.equalsIgnoreCase(role.getId());
		} 
		catch (IdUnusedException e) 
		{
		    return false;
		}
	}
}
