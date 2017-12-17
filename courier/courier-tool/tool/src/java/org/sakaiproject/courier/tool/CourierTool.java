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

package org.sakaiproject.courier.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.courier.api.Delivery;
import org.sakaiproject.courier.api.DeliveryProvider;
import org.sakaiproject.courier.cover.CourierService;
import org.sakaiproject.courier.cover.PresenceUpdater;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>
 * CourierTool is a "tool" which handles courier requests and delivers courier deliveries.
 * </p>
 */
@Slf4j
public class CourierTool extends HttpServlet
{
	private SessionManager sessionManager = (SessionManager)
			ComponentManager.get(SessionManager.class);
	
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
		// support two "/" separated parameters [1] and [2]) - both get presence updated, the first is the address for delivery (second optional)
		String[] parts = req.getPathInfo().split("/");
		
		if ((parts.length == 2) || (parts.length == 3))
		{
			String placementId = parts[1];
			
			// if we are in a newly created session where we had an invalid (presumed timed out) session in the request,
			// send script to cause a sakai top level redirect
			if (ThreadLocalManager.get(SessionManager.CURRENT_INVALID_SESSION) != null)
			{
				String loggedOutUrl = ServerConfigurationService.getLoggedOutUrl();
				if (log.isDebugEnabled()) log.debug("sending top redirect: {} : {}", placementId, loggedOutUrl);
				sendTopRedirect(res, loggedOutUrl);
			}

			else
			{
				String requestUserId = req.getParameter("userId");
				Session session = sessionManager.getCurrentSession();
				
				if (requestUserId == null || requestUserId.equals(session.getUserId())) {
					// compute our courier delivery address: this placement in this session
					String deliveryId = session.getId() + placementId;
	
					// find all deliveries for the requested deivery address
					List deliveries = CourierService.getDeliveries(deliveryId);
	
					// see if any DeliveryProviders have deliveries
					List<DeliveryProvider> providers = CourierService.getDeliveryProviders();
					if(providers != null) {
						List<Delivery> moreDeliveries = new ArrayList<Delivery>();
						for(DeliveryProvider provider : providers) {
							List<Delivery> d = provider.getDeliveries(session.getId(), placementId);
							if(d != null && ! d.isEmpty()) {
								moreDeliveries.addAll(d);
							}
						}
						if(moreDeliveries.isEmpty()) {
							// use deliveries
						} else if (deliveries.isEmpty()) {
							deliveries = moreDeliveries;
						} else {
							// both lists have deliveries, so add moreDeliveries to deliveries
							deliveries.addAll(moreDeliveries);
						}
					}
					
					// form the reply
					sendDeliveries(res, deliveries);
	
					// refresh our presence at the location (placement)
					if (log.isDebugEnabled()) log.debug("setting presence: {}", placementId);
					PresenceUpdater.setPresence(placementId);
	
					// register another presence if present
					if (parts.length == 3)
					{
						String secondPlacementId = parts[2];
						if (log.isDebugEnabled()) log.debug("setting second presence: {}", secondPlacementId);
						PresenceUpdater.setPresence(secondPlacementId);
					}
				} else {
					//This courier request was not meant for this user (i.e., session), so we won't honour it
					log.debug("out-of-session courier request: requestUserId={} session user={}", requestUserId, session.getUserId());
				}
			}
		}

		// otherwise this is a bad request!
		else
		{
			log.warn("bad courier request: {}", req.getPathInfo());
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

		log.info("init()");
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
			if (log.isDebugEnabled()) log.debug("sending delivery: {}", s);
			out.println(s);
		}

		// make sure we send something
		if (deliveries.isEmpty())
		{
			String s = "//";
			if (log.isDebugEnabled()) log.debug("sending delivery: {}", s);
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
