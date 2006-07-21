package org.sakaiproject.tool.podcasts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodfeedService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class RSSPodfeedServlet extends HttpServlet {

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
		
		response.setContentType("text/xml");
		
		//Get the siteID from the URL passed in
		String reqURL = request.getPathInfo();
		String siteID;
		
		if (reqURL != null) {
			siteID = reqURL.substring(reqURL.lastIndexOf("/") + 1);
		}
		else {
			reqURL = request.getRequestURI();
			
			siteID = reqURL.substring(1, reqURL.lastIndexOf("/"));
		}

		// We want to generate this every time to ensure changes to the Podcast folder are put in feed "immediately"
		String podcastFeed = podfeedService.generatePodcastRSS(PodfeedService.PODFEED_CATEGORY, "FromServlet.xml", siteID);

		if (podcastFeed.equals("")) {
			response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
		}
		else {
			PrintWriter pw = response.getWriter();
			pw.write(podcastFeed);
		
		}
		
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
