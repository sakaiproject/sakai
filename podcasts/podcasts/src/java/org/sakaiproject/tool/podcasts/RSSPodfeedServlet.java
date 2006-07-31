package org.sakaiproject.tool.podcasts;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.api.app.podcasts.PodfeedService;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.dav.DavPrincipal;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.util.IdPwEvidence;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class RSSPodfeedServlet extends HttpServlet {
	private static final String RESPONSE_MIME_TYPE="application/xml; charset=UTF-8";
	private static final String FEED_TYPE = "type";
	
	private PodfeedService podfeedService;
	private Log LOG = LogFactory.getLog(RSSPodfeedServlet.class);
	
	/**
	 * Constructor of the object.
	 */
	public RSSPodfeedServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String reqURL = request.getPathInfo();
		String siteID;
		
		if (reqURL != null) {
			siteID = reqURL.substring(reqURL.lastIndexOf("/") + 1);
		}
		else {
			reqURL = request.getRequestURI();
			
			siteID = reqURL.substring(1, reqURL.lastIndexOf("/"));
		}

		String siteCollection = ContentHostingService.getSiteCollection(siteID);
		String podcastsCollection = siteCollection + PodcastService.COLLECTION_PODCASTS + Entity.SEPARATOR;

		if (! ContentHostingService.isPubView(podcastsCollection)) {
			// Authentication madness:
			//  1. Determine if resource if public/private ("default" - public)
			//  2. if private, was username/password sent with request?
			//  3.     if so, authenticate
			//  4.     if successful, in you go, if not -> 403 response
			//  5. if no username/password, 403 response
			//  6. if public, on you go
		
			// try to authenticate based on a Principal (one of ours) in the req
			Principal prin = (DavPrincipal) request.getUserPrincipal();
			String username;
		
			if ((prin != null) && (prin instanceof DavPrincipal))
			{
				String eid = prin.getName();
				String pw = ((DavPrincipal) prin).getPassword();
				Evidence e = new IdPwEvidence(eid, pw);

				// authenticate
				try
				{
					if ((eid.length() == 0) || (pw.length() == 0))
					{
						throw new AuthenticationException("missing required fields");
					}

					Authentication a = AuthenticationManager.authenticate(e);

					if (!UsageSessionService.login(a, request))
					{
						// login failed
						response.setHeader("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"Podcaster\"");
						response.sendError(401);
						return;
					}
				}
				catch (AuthenticationException ex)
				{
					// not authenticated
					response.setHeader("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"Podcaster\"");
					response.sendError(401);
					return;
				}
			}
			else
			{
				// user name missing, so can't authenticate
				response.setHeader("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"Podcaster\"");
				response.sendError(401);
				return;
			}
			
		}

		response.setContentType(RESPONSE_MIME_TYPE);
		
		// We want to generate this every time to ensure changes to the Podcast folder are put in feed "immediately"
		String podcastFeed = podfeedService.generatePodcastRSS(PodfeedService.PODFEED_CATEGORY, "FromServlet.xml", siteID, request.getParameter(FEED_TYPE));

		if (podcastFeed.equals("")) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		else {
			PrintWriter pw = response.getWriter();
			pw.write(podcastFeed);

		}
		
/*		}
		catch (InconsistentException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
*/		
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occure
	 */
	public void init() throws ServletException {
		System.out.println(this+": RSSPodfeedServlet.init()");
		
		ServletContext sc = this.getServletContext();

		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(sc);

		podfeedService = (PodfeedService) wac.getBean("org.sakaiproject.api.app.podcasts.PodfeedService");
	}
	
	/**
	 * @return Returns the podfeedService.
	 */
	public PodfeedService getPodfeedService() {
		return podfeedService;
	}

	/**
	 * @param podfeedService The podfeedService to set.
	 */
	public void setPodfeedService(PodfeedService podfeedService) {
		this.podfeedService = podfeedService;
	}

}
