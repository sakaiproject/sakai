package org.sakaiproject.portal.charon.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.authz.cover.SecurityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateBugReportHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "generatebugreport";

	public GenerateBugReportHandler()
	{
		setUrlFragment(GenerateBugReportHandler.URL_FRAGMENT);
	}

	@Override
	public int doPost(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session)
		throws PortalHandlerException
	{
		return NEXT;
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res, Session session)
		throws PortalHandlerException
	{
		if ((parts.length == 2) && (parts[1].equals(GenerateBugReportHandler.URL_FRAGMENT))) {
			if (!SecurityService.isSuperUser()) {
				log.debug("No bug report generated because user isn't a superuser");
			} else {
				throw new RuntimeException(URL_FRAGMENT);
			}
		}

		return NEXT;
	}
}
