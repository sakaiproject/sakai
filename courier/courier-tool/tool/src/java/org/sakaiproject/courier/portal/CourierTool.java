/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

// package
package org.sakaiproject.tool.portal;

// imports
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.kernel.session.Session;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.api.kernel.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.framework.courier.Delivery;
import org.sakaiproject.service.framework.courier.cover.CourierService;
import org.sakaiproject.service.legacy.presence.cover.PresenceService;
import org.sakaiproject.util.RequestFilter;

/**
 * <p>
 * CourierTool is a "tool" which handles courier requests and delivers courier deliveries.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Id$
 */
public class CourierTool extends HttpServlet
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(CourierTool.class);

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

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
		// lets see what we have ([0] will be "", [1] is the placement id, the rest is to make it unique to disable browser caching)
		String[] parts = req.getPathInfo().split("/");
		if (parts.length >= 2)
		{
			String placementId = parts[1];

			// get the Sakai session
			Session session = SessionManager.getCurrentSession();

			// if we are in a newly created session where we had an invalid (presumed timed out) session in the request,
			// send script to cause a sakai top level redirect
			if (ThreadLocalManager.get(RequestFilter.CURRENT_INVALID_SESSION) != null)
			{
				String loggedOutUrl = ServerConfigurationService.getLoggedOutUrl();
				if (M_log.isDebugEnabled()) M_log.debug("sending top redirect: " + placementId + " : " + loggedOutUrl);
				sendTopRedirect(res, loggedOutUrl);
			}

			else
			{
				// compute our courier delivery address: this placement in this session
				String deliveryId = session.getId() + placementId;

				// find all deliveries for the requested deivery address
				List deliveries = CourierService.getDeliveries(deliveryId);

				// form the reply
				sendDeliveries(res, deliveries);

				// refresh our presence at the location (placement)
				if (M_log.isDebugEnabled()) M_log.debug("setting presence: " + placementId);
				PresenceService.setPresence(placementId);
			}
		}

		// otherwise this is a bad request!
		else
		{
			M_log.warn("bad courier request: " + req.getPathInfo());
			sendDeliveries(res, new Vector());
		}
	}

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Courier Tool";
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

		M_log.info("init()");
	}

	/**
	 * Send any deliveries, or at least something javascrip eval()'able.
	 * 
	 * @param res
	 * @param deliveries
	 *        The list (possibly empty) of deliveries
	 * @throws IOException
	 */
	protected void sendDeliveries(HttpServletResponse res, List deliveries) throws IOException
	{
		res.setContentType("text/plain; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// get the writer
		PrintWriter out = res.getWriter();

		for (Iterator i = deliveries.iterator(); i.hasNext();)
		{
			Delivery d = (Delivery) i.next();
			String s = d.compose();
			if (M_log.isDebugEnabled()) M_log.debug("sending delivery: " + s);
			out.println(s);
		}

		// make sure we send something
		if (deliveries.isEmpty())
		{
			String s = "//";
			if (M_log.isDebugEnabled()) M_log.debug("sending delivery: " + s);
			out.println(s);
		}
	}

	/**
	 * Send a redirect so our "top" ends up at the url, via javascript.
	 * 
	 * @param url
	 *        The redirect url
	 */
	protected void sendTopRedirect(HttpServletResponse res, String url) throws IOException
	{
		res.setContentType("text/plain; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// get the writer
		PrintWriter out = res.getWriter();

		// we are on deep under the main portal window
		out.println("parent.location.replace('" + url + "');");
	}
}



