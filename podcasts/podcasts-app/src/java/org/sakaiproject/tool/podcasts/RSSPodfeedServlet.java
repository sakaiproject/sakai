package org.sakaiproject.tool.podcasts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.api.app.podcasts.PodfeedService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

public class RSSPodfeedServlet extends HttpServlet {

	private PodfeedService podfeedService;
	
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
			siteID = reqURL.substring(reqURL.lastIndexOf("/"));
		}
		else {
			reqURL = request.getRequestURI();
			
			siteID = reqURL.substring(1, reqURL.lastIndexOf("/"));
		}

		// for testing purposes
		siteID = "d6bdb95a-a683-4bd1-0079-3eb5ba3341ee";
		
		// We want to generate this every time to ensure changes to the Podcast folder are put in feed "immediately"
		SyndFeed podcastFeed = podfeedService.generatePodcastRSS(PodfeedService.PODFEED_CATEGORY, "FromServlet.XML", siteID);

		if (podcastFeed != null) {
			final SyndFeedOutput feedWriter = new SyndFeedOutput();
		
			try {
				PrintWriter pw = response.getWriter();
				String xmlDoc = feedWriter.outputString(podcastFeed);
				pw.write(xmlDoc);
//				feedWriter.output(podcastFeed, response.getWriter());
			} catch (FeedException e) {
				// TODO Auto-generated catch block
				throw new IOException(e.getMessage());
			}
		
			
		
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

	public void init(ServletConfig servletConfig) throws ServletException {

		super.init(servletConfig);

		ServletContext sc = servletConfig.getServletContext();

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
