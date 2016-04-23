package org.sakaiproject.portal.charon.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;

public class RoleSwitchOutHandler extends BasePortalHandler
{
	private static final Logger log = LoggerFactory.getLogger(RoleSwitchOutHandler.class);

	private static final String URL_FRAGMENT = "role-switch-out";
	public static final String EVENT_ROLESWAP_EXIT = "roleswap.exit";

	final EventTrackingService eventTrackingService;
	final SecurityService securityService;
	final SiteService siteService;

	public RoleSwitchOutHandler()
	{
		eventTrackingService = ComponentManager.get(EventTrackingService.class);
		securityService = ComponentManager.get(SecurityService.class);
		siteService = ComponentManager.get(SiteService.class);
		setUrlFragment(RoleSwitchOutHandler.URL_FRAGMENT);
	}
	
	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session)
			throws PortalHandlerException
	{
		if (parts == null || req == null || res == null || session == null)
		{
			throw new IllegalStateException("null pointers while swapping out of student view");
		}

		if ((parts.length > 2) && "role-switch-out".equals(parts[1]))
		{
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
				securityService.clearUserEffectiveRole(siteService.siteReference(parts[2]));
				
				// Post an event
				eventTrackingService.post(eventTrackingService.newEvent(EVENT_ROLESWAP_EXIT, null, parts[2], false, NotificationService.NOTI_NONE));

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
