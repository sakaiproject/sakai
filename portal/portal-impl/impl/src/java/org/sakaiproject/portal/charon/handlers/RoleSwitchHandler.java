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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoleSwitchHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "role-switch";
	public static final String EVENT_ROLESWAP_START = "roleswap.start";

	final EventTrackingService eventTrackingService;
	final SecurityService securityService;
	final SiteService siteService;

	public RoleSwitchHandler()
	{
		eventTrackingService = ComponentManager.get(EventTrackingService.class);
		securityService = ComponentManager.get(SecurityService.class);
		siteService = ComponentManager.get(SiteService.class);
		setUrlFragment(RoleSwitchHandler.URL_FRAGMENT);
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
				String siteUrl = req.getContextPath() + "/site/" + parts[2] + "/";
				String queryString = req.getQueryString();
				if (StringUtils.isNotBlank(queryString))
				{
					siteUrl = siteUrl + "?" + queryString;
				}

				activeSite.getPages().stream() // get all pages in site
					.map(page -> page.getTools()) // tools for each page
					.flatMap(tools -> tools.stream()) // combine all tool lists
					.peek(tool -> log.debug("Resetting state for site: " + activeSite.getId() + " tool: " + tool.getId()))
					.forEach(tool -> session.getToolSession(tool.getId()).clearAttributes()); // reset each tool

				portalService.setResetState("true"); // flag the portal to reset
				securityService.setUserEffectiveRole(activeSite.getReference(), parts[3]);

				// Post an event
				eventTrackingService.post(eventTrackingService.newEvent(EVENT_ROLESWAP_START, parts[3], parts[2], false, NotificationService.NOTI_NONE));

				res.sendRedirect(siteUrl);
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
}
