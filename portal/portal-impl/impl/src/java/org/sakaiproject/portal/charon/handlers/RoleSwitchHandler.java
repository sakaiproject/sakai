/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.portal.charon.handlers;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.util.URLUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoleSwitchHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "role-switch";

	@Autowired @Qualifier("org.sakaiproject.event.api.EventTrackingService")
	private EventTrackingService eventTrackingService;
	@Autowired @Qualifier("org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;
	@Autowired @Qualifier("org.sakaiproject.site.api.SiteService")
	private SiteService siteService;
	@Autowired @Qualifier("org.sakaiproject.tool.api.ToolManager")
	private ToolManager toolManager;

	private String portalUrl;
	private String externalRoles;

	public RoleSwitchHandler() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		setUrlFragment(RoleSwitchHandler.URL_FRAGMENT);
		portalUrl = serverConfigurationService.getPortalUrl();
		// get the roles that can be swapped to from sakai.properties
		externalRoles = serverConfigurationService.getString("studentview.roles");
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session)
			throws PortalHandlerException
	{

		if (parts == null || req == null || res == null || session == null)
		{
			throw new IllegalStateException("null pointers while swapping into student view");
		}

		if ((parts.length > 3) && "role-switch".equals(parts[1]) && siteService.allowRoleSwap(parts[2]))
		{
			// confirms the url and the permission for the user on the site
			// Start check for making sure the role is legit in a site
			final Site activeSite;
			try
			{
				activeSite =  portal.getSiteHelper().getSiteVisit(parts[2]); // get our site
            }
			catch(IdUnusedException ie)
			{
        		log.error(ie.getMessage(), ie);
        		throw new IllegalStateException("Site doesn't exist!");
            }
			catch(PermissionException pe)
			{
            	log.error(pe.getMessage(), pe);
            	throw new IllegalStateException("No permission to visit site!");
            }

            Set<Role> roles = activeSite.getRoles(); // all the roles in our site
        	String[] svRoles = externalRoles.split(",");
        	boolean isRoleLegit = false;

			for (Role role : roles)
			{
				for (int i = 0; i < svRoles.length; i++)
				{
					if (svRoles[i].trim().equals(role.getId()) && svRoles[i].trim().equals(parts[5]))
					{
        				isRoleLegit = true; // set this to true because we verified the role passed in is in the site and is allowed to be switched from sakai.properties configuration
        				break;
        			}
        		}
				if (isRoleLegit)
				{
        			break; // no need to keep looping if we have the confirmed role
				}
        	}
			if (!isRoleLegit)
			{
        		return NEXT; // if the role is not legit, return without doing anything
			}
        	// End check for making sure the role is legit in a site

			try
			{
				String url = portalUrl + "/site/" + parts[2] + "/tool/" + parts[4] + "/";

				AtomicBoolean isToolHidden = new AtomicBoolean(false);
				activeSite.getPages().stream() // get all pages in site
					.map(SitePage::getTools) // tools for each page
					.flatMap(Collection::stream) // combine all tool lists
					.peek(tool -> log.debug("Resetting state for site: {} tool: {}", activeSite.getId(), tool.getId()))
					.forEach(tool -> {
						session.getToolSession(tool.getId()).clearAttributes(); // reset each tool
						if (tool.getId().equals(parts[4]) && toolManager.isHidden(tool)) { isToolHidden.set(true); } //check if the active tool is hidden
					});

				if (isToolHidden.get()) { url = (!homePageIsHidden(activeSite)) ? portalUrl + "/site/" + parts[2] + "/" : portalUrl; }

				portalService.setResetState("true"); // flag the portal to reset
				
				// Change to role view
				siteService.activateRoleViewOnSite(activeSite.getReference(), parts[5]);
				
				// Post an event
				eventTrackingService.post(eventTrackingService
						.newEvent(UsageSessionService.EVENT_ROLEVIEW_START, parts[5], parts[2], false, NotificationService.NOTI_NONE));

				res.sendRedirect(URLUtils.sanitisePath(url));
				return RESET_DONE;
			}
			catch(Exception ex)
			{
				throw new PortalHandlerException(ex);
			}
		}
		else
		{
			return NEXT;
		}
	}

	private boolean homePageIsHidden(Site activeSite) {
		return (!activeSite.getPages().isEmpty() &&
				activeSite.getPages().get(0).isHomePage() &&
				!activeSite.getPages().get(0).getTools().isEmpty() &&
				toolManager.isHidden(activeSite.getPages().get(0).getTools().get(0)));
	}

}
