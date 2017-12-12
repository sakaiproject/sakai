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


package org.sakaiproject.tool.podcasts;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import org.sakaiproject.api.app.podcasts.PodfeedService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.util.IdPwEvidence;

@Slf4j
public class RSSPodfeedServlet extends HttpServlet {
	/** Used to set the MIME type of the response back to the client **/
	private static final String RESPONSE_MIME_TYPE = "application/xml; charset=UTF-8";

	/** Used to track the event of generating a public feed **/
	private final String EVENT_PUBLIC_FEED = "podcast.read.public";
	
	/** Used to track the event of generating a private feed **/
	private final String EVENT_PRIVATE_FEED = "podcast.read.site";
	
	/** FUTURE DEVELOPMENT: set to pass in feed type as a parameter with name 'type' **/ 
	private static final String FEED_TYPE = "type";

	private PodfeedService podfeedService;

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		// get requested URL in attempt to extract siteId
		String reqURL = request.getPathInfo();
		String siteId;

		if (reqURL != null) {
			siteId = reqURL.substring(reqURL.lastIndexOf("/") + 1);
		} 
		else {
			// could not get it from request URL, try URI
			reqURL = request.getRequestURI();

			siteId = reqURL.substring(1, reqURL.lastIndexOf("/"));
		}

		log.debug("Podcast feed requested for site: " + siteId);

		// get podcast folder id to determine if public/private
		final String podcastsCollection = podfeedService.retrievePodcastFolderId(siteId);

		// if error finding podcast folder id, will return null, so return
		// Internal Server Error message back to client
		if (podcastsCollection == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			return;
		}

		// Determine if resource if public/private ("default" - public)
		final boolean pubView = ContentHostingService
				.isPubView(podcastsCollection);

		if (!pubView) {

			// if private, was username/password sent with request?
			final Evidence e = getBasicAuthEvidence(request);

			if ((e != null)) {

				// authenticate
				try {
					log.info("Authenticating " + e);
					final Authentication a = AuthenticationManager
							.authenticate(e);

					if (!UsageSessionService.login(a, request)) {
						// login failed, so ask for auth again
						sendErrorResponse(response);
							
						return;
					}
				}
				catch (final Exception exc) {
					// something went wrong, so ask for auth again
					sendErrorResponse(response);
						
					return;
				}
			}
			else {
				// user name missing, so can't authenticate, so ask for auth
				sendErrorResponse(response);

				return;
			}
			
			// Authenticated, but are they members of the site
			// accomplished by doing a check on read access to podcast folder
			if (!podfeedService.allowAccess(podcastsCollection)) {
				// check to access denied error
				response.sendError(403);
			}
		}
		
		response.setContentType(RESPONSE_MIME_TYPE);

		// generates actual feed, 2nd parameter for future to allow different
		// feed types currently only rss_2.0 is generated
		final String podcastFeed = podfeedService.generatePodcastRSS(siteId,
				request.getParameter(FEED_TYPE));

		// if problem getting feed, null will be returned so return
		// Internal Server Error to client
		if (podcastFeed == null || podcastFeed.equals("")) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} 
		else {
			response.getWriter().write(podcastFeed);

			// add entry for event tracking
			Event event = null;
			if (pubView) {
				event = EventTrackingService.newEvent(EVENT_PUBLIC_FEED, podcastsCollection, false, NotificationService.NOTI_NONE);
			}
			else {
				event = EventTrackingService.newEvent(EVENT_PRIVATE_FEED, podcastsCollection, false, NotificationService.NOTI_NONE);
			}
			EventTrackingService.post(event);


		}

	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		doGet(request, response);
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occure
	 */
	public void init() throws ServletException {
		log.debug(this + ": RSSPodfeedServlet.init()");

		podfeedService = (PodfeedService) ComponentManager
				.get("org.sakaiproject.api.app.podcasts.PodfeedService");

		if (podfeedService == null)
			throw new ServletException(new IllegalStateException(
					"podfeedService == null"));
	}

	/**
	 * @param podfeedService
	 *            The podfeedService to set.
	 */
	public void setPodfeedService(final PodfeedService podfeedService) {
		this.podfeedService = podfeedService;
	}

	/**
	 * Extracts auth information if it exists in the HTTP headers
	 * 
	 * @param request
	 * 			The HttpRequest object to be searched
	 * 
	 * @return IdPwEvidence
	 * 			Contains the auth information if found in request headers or null if not found
	 */
	private IdPwEvidence getBasicAuthEvidence(final HttpServletRequest request) {

		Base64 base64Encoder = new Base64();
		
		final String header = request.getHeader("Authorization");
		String[] elements = null;

		log.debug("Authorization: " + header);

		if (header != null)
			elements = header.split(" ");

		if (elements != null && elements.length >= 2) {

			final String type = elements[0];
			final String hash = elements[1];

			log.debug("type: " + type + " hash: " + hash);

			final String[] credential = (new String(base64Encoder.decode(hash.getBytes())))
					.split(":");

			log.debug("credential: " + credential);

			if (credential != null && credential.length >= 2) {
				final String eid = credential[0];
				final String password = credential[1];

				log.debug("eid: " + eid + " password: ********");

				if ((eid.length() == 0) || (password.length() == 0)) {
					return null;
				}

				return new IdPwEvidence(eid, password, request.getRemoteAddr());

			}

		}

		return null;

	}

	/**
	 * If missing or invalid username/password given, return HTTP 401 to request
	 * authentication.
	 * 
	 * @param response
	 * 			The Response object so we can set headers
	 * 
	 * @throws IOException
	 * 			Throw this exception back if there was a problem setting the headers
	 */
	private void sendErrorResponse(final HttpServletResponse response) throws IOException {
		response.setHeader("WWW-Authenticate",
				HttpServletRequest.BASIC_AUTH
						+ " realm=\"Podcaster\"");
		response.sendError(401);
		
	}

}
