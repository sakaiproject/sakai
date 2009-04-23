package org.sakaiproject.portal.charon.handlers;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Web;

public class RoleSwitchHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "role-switch";

	public static final String EVENT_ROLESWAP_START = "roleswap.start";

	private static final Log log = LogFactory.getLog(SiteHandler.class);

	public RoleSwitchHandler()
	{
		setUrlFragment(RoleSwitchHandler.URL_FRAGMENT);
	}
	
	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if (parts == null || req == null || res == null || session == null)
			throw new IllegalStateException("null pointers while swapping into student view");
		if ((parts.length > 3) && "role-switch".equals(parts[1]) && SiteService.allowRoleSwap(parts[2])) // confirms the url and the permission for the user on the site
		{
			// Start check for making sure the role is legit in a site
			Site activeSite = null;
			try
            {
				activeSite =  portal.getSiteHelper().getSiteVisit(parts[2]); // get our site
            }
        	catch(IdUnusedException ie)
            {
        		log.error(ie.getMessage(), ie);
            }
            catch(PermissionException pe)
            {
            	log.error(pe.getMessage(), pe);
            }
			
            Set<Role> roles = activeSite.getRoles(); // all the roles in our site

        	String externalRoles = ServerConfigurationService.getString("studentview.roles"); // get the roles that can be swapped to from sakai.properties
        	String[] svRoles = externalRoles.split(",");
        	boolean isRoleLegit = false;

        	for (Role role : roles)
        	{
        		for (int i = 0; i < svRoles.length; i++)
        		{
        			if (svRoles[i].trim().equals(role.getId()) && svRoles[i].trim().equals(parts[3]))
        			{
        				isRoleLegit = true; // set this to true because we verified the role passed in is in the site and is allowed to be switched from sakai.properties configuration
        				break;
        			}
        		}
        		if (isRoleLegit)
        			break; // no need to keep looping if we have the confirmed role
        	}
        	if (!isRoleLegit)
        		return NEXT; // if the role is not legit, return without doing anything
        	// End check for making sure the role is legit in a site
			try
			{
				String siteUrl = req.getContextPath() + "/site"
						+ Web.makePath(parts, 2, parts.length-1);
				// Make sure to add the parameters such as panel=Main
				String queryString = req.getQueryString();
				if (queryString != null)
				{
					siteUrl = siteUrl + "?" + queryString;
				}
				portalService.setResetState("true"); // flag the portal to reset
				
				SecurityService.setUserEffectiveRole(activeSite.getReference(), parts[3]);
						
				// Post an event
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_ROLESWAP_START, parts[3], parts[2], false, NotificationService.NOTI_NONE));
				
				res.sendRedirect(siteUrl);
				return RESET_DONE;
			}
			catch (Exception ex)
			{
				throw new PortalHandlerException(ex);
			}
		}
		else
		{
			return NEXT;
		}
	}

}