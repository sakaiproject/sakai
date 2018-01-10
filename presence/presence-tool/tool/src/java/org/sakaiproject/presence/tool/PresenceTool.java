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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.presence.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.presence.cover.PresenceService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.PresenceObservingCourier;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

/**
 * <p>
 * Presence is an tool which presents an auto-updating user presence list.
 * </p>
 */
@Slf4j
public class PresenceTool extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/** Request parameter to generate a fragment only. */
	protected static final String OUTPUT_FRAGMENT = "output_fragment";

	/** Tool state attribute where the observer is stored. */
	protected static final String ATTR_OBSERVER = "observer";

	/** Common Tool ID for the Sakai Chat Tool */
	protected static final String CHAT_COMMON_ID = "sakai.chat";

	/** Tool state attribute where the chat observer is stored. */
	protected static final String ATTR_CHAT_OBSERVER = "chat_observer";

	/** Presence prefix for context id for chat presence in a site **/
	protected static final String CHAT_CONTEXT_PRESENCE_PREFIX = "chat_site_";

	/** Localized messages * */
	ResourceLoader rb = new ResourceLoader("presence");

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to access requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the current tool session, where we store our observer
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		// get the current tool placement
		Placement placement = ToolManager.getCurrentPlacement();

		// location is just placement
		String location = placement.getId();

		// refresh our presence at the location
		PresenceService.setPresence(location);

		// If we are a full frame, make sure we have an observing watching 
		// for presence change at location
		PresenceObservingCourier observer = (PresenceObservingCourier) toolSession.getAttribute(ATTR_OBSERVER);
		if (observer == null)
		{
			// setup an observer to notify us when presence at this location changes
			observer = new PresenceObservingCourier(location);
			toolSession.setAttribute(ATTR_OBSERVER, observer);
		}

		// get the list of users at the location
		List<User> users = PresenceService.getPresentUsers(location, placement.getContext());
		
		// get SiteId from the current placement and retrieve site
		String siteId = placement.getContext();

		Site site = null;
		ToolConfiguration toolConfig = null;
		List<User> chatUsers = null;

		if (siteId != null)
		{
			try
			{
				site = SiteService.getSiteVisit(siteId);
			}
			catch (Exception e)
			{
				// No problem - leave site null
			}
		}

		if (site != null)
		{
			toolConfig = site.getToolForCommonId(CHAT_COMMON_ID);
		}

		if (toolConfig != null)
		{
			// Check the secondary chat presence that's specific to the site (rather than channel or placement)
			String chatLocation = CHAT_CONTEXT_PRESENCE_PREFIX + siteId;
			chatUsers = PresenceService.getPresentUsers(chatLocation, siteId);

			PresenceObservingCourier chatObserver = (PresenceObservingCourier) toolSession.getAttribute(ATTR_CHAT_OBSERVER);
			if (chatObserver == null)
			{
				// Monitor presence changes at chatLocation and deliver them to this window's location with
				// no sub window (null)
				chatObserver = new PresenceObservingCourier(location, null, chatLocation);
				toolSession.setAttribute(ATTR_CHAT_OBSERVER, chatObserver);
			}
		}

		// start the response
		PrintWriter out = startResponse(req, res, "presence");

		sendAutoUpdate(out, req, placement.getId(), placement.getContext());
		sendPresence(out, users, chatUsers);

		// end the response
		if ( ! "yes".equals(req.getParameter(OUTPUT_FRAGMENT) ) ) endResponse(out);
	}

	/**
	 * End the response
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void endResponse(PrintWriter out) throws IOException
	{
		out.println("</body></html>");
	}

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Presence Tool";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		log.info("init()");
	}

	/**
	 * Send the HTML / Javascript to invoke an automatic update
	 * 
	 * @param out
	 * @param req
	 * @param placementId
	 * @param context
	 */
	protected void sendAutoUpdate(PrintWriter out, HttpServletRequest req, String placementId, String context)
	{
		// set the refresh of the courier to 1/2 the presence timeout value
		int updateTime = PresenceService.getTimeout() / 2;

		String userId = SessionManager.getCurrentSessionUserId();
		StringBuilder url = new StringBuilder(Web.serverUrl(req));
		url.append("/courier/");
		url.append(placementId);
		url.append("?userId=");
		url.append(userId);

		out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
		out.println("updateTime = " + updateTime + "000;");
		out.println("updateUrl = \"" + url.toString() + "\";");
		out.println("scheduleUpdate();");
		out.println("</script>");
	}

	/**
	 * Format the list of users
	 * 
	 * @param out
	 * @param users
	 */
	protected void sendPresence(PrintWriter out, List<User> users, List<User> chatUsers)
	{
		// is the current user running under an assumed (SU) user id?
		String asName = null;
		String myUserId = null;
		try
		{
			UsageSession usageSession = UsageSessionService.getSession();
			if (usageSession != null)
			{
				// this is the absolutely real end-user id, even if running as another user
				myUserId = usageSession.getUserId();

				// this is the user id the current user is running as
				String sessionUserId = SessionManager.getCurrentSessionUserId();

				// if different
				if (!myUserId.equals(sessionUserId))
				{
					asName = UserDirectoryService.getUser(sessionUserId).getDisplayName();
				}
			}
		}
		catch (Exception any)
		{
		}

		out.println("<ul class=\"presenceList\">");
		if (users == null)
		{
			out.println("<!-- Presence empty -->");
			out.println("</ul>");
			return;
		}

		// first pass - list Chat users (if any)
		if (chatUsers != null)
		{
			String msg = rb.getString("inchat");
			for (User u : chatUsers) {
				String displayName = u.getDisplayName();

				// adjust if this is the current user running as someone else
				if ((asName != null) && (u.getId().equals(myUserId)))
				{
					displayName += " (" + asName + ")";
				}

				out.print("<li class=\"inChat\">");
				out.print("<span title=\"" + msg + "\">");
				out.print(Web.escapeHtml(displayName));
				out.println("</span></li>");				
			}
		}

		// second pass - list remaining non-chat users
		List<User> nonChatUsers = new Vector<User>(users);
		if (chatUsers != null)
		{
			nonChatUsers.removeAll(chatUsers);
		}

		String msg = rb.getString("insite");
		for (User u : nonChatUsers)
		{
			String displayName = u.getDisplayName();

			// adjust if this is the current user running as someone else
			if ((asName != null) && (u.getId().equals(myUserId)))
			{
				displayName += " (" + asName + ")";
			}

			out.print("<li>");
			out.print("<span title=\"" + msg + "\">");
			out.print(Web.escapeHtml(displayName));
			out.println("</span></li>");
		}

		out.println("</ul>");
	}

	/**
	 * Start the response.
	 * 
	 * @param req
	 * @param res
	 * @param title
	 * @param skin
	 * @return
	 * @throws IOException
	 */
	protected PrintWriter startResponse(HttpServletRequest req, HttpServletResponse res, String title) throws IOException
	{
		// headers
		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// get the writer
		PrintWriter out = res.getWriter();

		if ( "yes".equals(req.getParameter(OUTPUT_FRAGMENT) ) ) return out;

		// form the head
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
				+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">" + "  <head>"
				+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");

		out.println("<title>" + rb.getString("insite") + "</title>");
		
		// send the portal set-up head
		String head = (String) req.getAttribute("sakai.html.head");
		if (head != null)
		{
			out.println(head);
		}

		out.println("</head>");

		// Note: we ignore the portal set-up body onload

		// start the body
		out.println("<body>");

		return out;
	}
}
