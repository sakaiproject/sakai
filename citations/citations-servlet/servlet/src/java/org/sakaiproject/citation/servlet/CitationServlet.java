/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.citation.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.cheftool.VmServlet;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.citation.api.ConfigurationService;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.BasicAuth;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * 
 *
 */
//public class CitationServlet extends VelocityPortletPaneledAction
@Slf4j
public class CitationServlet extends VmServlet
{
	/**
	 * 
	 */
	public static final String SERVLET_TEMPLATE = "/vm/servlet.vm";
	public static final String COMPACT_TEMPLATE = "/vm/compact.vm";
//	private String collectionTitle = null;

	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("citations");

	/** set to true when init'ed. */
//	protected boolean m_ready = false;

	protected BasicAuth basicAuth = null;

	protected ContentHostingService contentService;

	protected CitationService citationService;
	
	protected ConfigurationService configurationService;

	protected enum Status
	{
		SUCCESS,
		ERROR;
	}


//	/** init thread - so we don't wait in the actual init() call */
//	public class CitationServletInit extends Thread
//	{
//		protected CitationService m_citationService;
		
//		public void setCitationService(CitationService service)
//		{
//			this.m_citationService = service;
//		}
		
//		/**
//		 * construct and start the init activity
//		 */
//		public CitationServletInit()
//		{
//			m_ready = false;
//			start();
//		}

//		/**
//		 * run the init
//		 */
//		public void run()
//		{
//			m_ready = true;
//		}
//	}


	/**
	 * initialize the AccessServlet servlet
	 * 
	 * @param config
	 *        the servlet config parameter
	 * @exception ServletException
	 *            in case of difficulties
	 */
	public void init( ServletConfig config ) throws ServletException
	{
		super.init(config);
//		startInit();
		basicAuth = new BasicAuth();
		basicAuth.init();

		// get services from ComponentManager
		contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		citationService = (CitationService) ComponentManager.get("org.sakaiproject.citation.api.CitationService");
		configurationService = (ConfigurationService) ComponentManager.get("org.sakaiproject.citation.api.ConfigurationService");
	}

//	/**
//	 * Start the initialization process
//	 */
//	public void startInit()
//	{
//		new CitationServletInit();
//	}


	/**
	 * respond to an HTTP GET request
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		log.debug("doGet() {}", req.getMethod());
		// process any login that might be present
		basicAuth.doLogin(req);
		
		// catch the login helper requests
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		
		if ((parts.length == 2) && ((parts[1].equals("login"))))
		{
			doLogin( req, res, null );
		}
		else
		{
			// Setup velocity.
			setupResponse(req, res);
			ContentResource resource = null;
			try
			{
				ParameterParser paramParser = (ParameterParser) req
					.getAttribute(ATTR_PARAMS);
				resource = findResource(paramParser, option);
				String resourceUuid = this.contentService.getUuid(resource.getId());
				setVmReference("resourceId", resourceUuid, req);
       
				boolean fromGoogle = false;
				Citation citation = findOpenURLVersion01(paramParser);
				if (citation == null) {
         
					citation = findOpenUrlCitation(req);
				}
				// set the success flag
				setVmReference("success", citation != null, req);
       
				if (citation != null) {
					addCitation(resource, citation);
					setVmReference( "citation", citation, req );
					setVmReference("topRefresh", Boolean.TRUE, req ); // TODO
				} else {
					// return failure
					setVmReference("error", rb.getString("error.notfound"), req);
				}
			} catch (IdUnusedException iue) {
				setVmReference("error", rb.getString("error.noid"), req);
			} catch (ServerOverloadException e) {
				setVmReference("error", rb.getString("error.unavailable"), req);
			} catch (PermissionException e) {
				setVmReference("error", rb.getString("error.permission"), req);
			}
			
			setVmReference("openUrlLabel", configurationService.getSiteConfigOpenUrlLabel(), req);
			setVmReference("titleProperty", Schema.TITLE, req);
			// validator
			setVmReference("xilator", new Validator(), req);

			// Set near end so we always have something
			setVmReference( "titleArgs",  new String[]{ getCollectionTitle(resource) }, req );
			// return the servlet template
			includeVm( COMPACT_TEMPLATE, req, res );

		}
	}
	
	/**
	 * Looks for an OpenURL citation in the request.
	 * @param req
	 * @return
	 */
	protected Citation findOpenUrlCitation(HttpServletRequest req) {
		Citation citation = citationService.addCitation(req);
		return citation;
	}

		
	/**
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 * @exception ServletException
	 *            in case of difficulties
	 * @exception IOException
	 *            in case of difficulties
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// process any login that might be present
		basicAuth.doLogin(req);
		
		// catch the login helper posts
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		
		if ((parts.length == 2) && ((parts[1].equals("login"))))
		{
			doLogin(req, res, null);
		}

		else
		{
			// don't handle POSTs
			sendError(res, HttpServletResponse.SC_NOT_FOUND);
		}
	}

	public void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if(log.isDebugEnabled()) {
			log.debug("doDelete() {}", req.getMethod());
		}
		// process any login that might be present
		basicAuth.doLogin(req);
		
		// catch the login helper posts
		String option = req.getPathInfo();
		String[] parts = option.split("/");
		
		if ((parts.length == 2) && ((parts[1].equals("login")))) {
			doLogin(req, res, null);
		} else if(parts.length < 3) {
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, rb.getString("savesite.delete.missing_params"));
			return;
		} else {
			
			String citationId = parts[2];
			String resourceUuid = parts[1];
			
			if(log.isDebugEnabled()) {
				log.debug("doDelete() citationId == {} resourceUuid == {}", citationId, resourceUuid);
			}
			
			if(resourceUuid == null || resourceUuid.trim().equals("") || citationId == null || citationId.trim().equals("")) {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST, rb.getString("savesite.delete.missing_params"));
				return;
			}
			
			String resourceId = contentService.resolveUuid(resourceUuid);
			if(resourceId == null) {
				res.sendError(HttpServletResponse.SC_NOT_FOUND, rb.getString("savecite.delete.invalid_uuid"));
				return;
			}
			
			if(! citationService.allowReviseCitationList(resourceId)) {
				res.sendError(HttpServletResponse.SC_FORBIDDEN, "savecite.delete.not_permitted");
				return;
			}
			
			try {
				ContentResource resource = this.contentService.getResource(resourceId);
				
				String citationCollectionId = new String(resource.getContent());
				if(citationCollectionId == null || citationCollectionId.trim().equals("")) {
					res.sendError(HttpServletResponse.SC_CONFLICT, rb.getString("savecite.delete.invalid_uuid"));
					return;
				}
				
				CitationCollection collection = this.citationService.getCollection(citationCollectionId );
				
				Citation item = collection.getCitation(citationId);
				
				collection.remove(item);
				this.citationService.save(collection);
				
				log.debug("doDelete() SUCCESS");
				
			} catch (IdUnusedException e) {
				res.sendError(HttpServletResponse.SC_NOT_FOUND, rb.getString("savecite.delete.invalid_uuid"));
			} catch (TypeException e) {
				res.sendError(HttpServletResponse.SC_CONFLICT, rb.getString("savecite.delete.invalid_uuid"));
			} catch (PermissionException e) {
				res.sendError(HttpServletResponse.SC_FORBIDDEN, "savecite.delete.not_permitted");
			} catch (ServerOverloadException e) {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, rb.getString("savecite.delete.internal"));
			}
		}
	}

      public ContentResource findResource(ParameterParser params, String option) throws PermissionException, IdUnusedException {
		// get the path info
		String path = params.getPath();
		if (path == null) path = "";

//		if (!m_ready)
//		{
//			sendError( res, HttpServletResponse.SC_SERVICE_UNAVAILABLE );
//		}

		// parse the request path
		String[] parts = option.split("/");
		String resourceUuid = parts[1];
		
		String resourceId = contentService.resolveUuid(resourceUuid);

		if (resourceId == null) {
			throw new IdUnusedException(resourceUuid);
		} 
		
		// revise permission granted
		if (!citationService.allowReviseCitationList(resourceId)) {
			// revise permission denied
			throw new PermissionException(null, null, null);
		}
		
		ContentResource resource = null;
		try {
			resource = contentService.getResource(resourceId);
			
		} catch (TypeException e) {
			// Ignore.
			if(log.isDebugEnabled()) {
				log.debug("TypeException in findResource() {}", e.getMessage());
			}
		}
		return resource;
	}
		

	public void addCitation(ContentResource resource, Citation citation) throws IdUnusedException, ServerOverloadException {
		String citationCollectionId = new String(resource.getContent());
		CitationCollection collection = citationService.getUnnestedCitationCollection(citationCollectionId);

		collection.add(citation);
		citationService.save(collection);
	}
 
	public String getCollectionTitle(ContentResource resource) {
		String collectionTitle = null;
		if (resource != null) {
			//String refStr = resource.getReference();
			//Reference ref = EntityManager.newReference(refStr);
			//collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			collectionTitle = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		return collectionTitle == null || "".equals(collectionTitle)? "your current citation list": collectionTitle; //TODO i18n
	}


	public Citation findOpenURLVersion01(ParameterParser params) {
		String genre = params.getString("genre");
		String[] authors = params.getStrings("au");
		String title = params.getString("title");
		String atitle = params.getString("atitle");
		String volume = params.getString("volume");
		String issue = params.getString("issue");
		String pages = params.getString("pages");
		String publisher = params.getString("publisher");
		String date = params.getString("date");
		String id = params.getString("id");

		HashMap map = new HashMap();
		map.put("genre", genre);
		map.put("au", authors);
		map.put("title", title);
		map.put("atitle", atitle);
		map.put("volume", volume);
		map.put("issue", issue);
		map.put("pages", pages);
		map.put("publisher", publisher);
		map.put("date", date);
		map.put("id", id);
		return findOpenURLVersion01(map);
	}

	/**
	 * Try and extract a Citation from the request.
	 *
	 * @param req  HttpServletRequest object with the client request
	 * @param res  HttpServletResponse object back to the client
	 */
	public Citation findOpenURLVersion01(Map params) {

		// http://localhost:8080/savecite/71b84348-5962-4e0e-aa49-4ce5824ed84f
		// ?sakai.session.key=nada&sid=google&genre=book&au=Siever,+E.&au=Figgins,+S.&au=Love,+R.&au=Robbins,+A.
		// &title=Linux+in+a+Nutshell&date=2009&publisher=Oreilly+%26+Associates+Inc

		// Google is't passing much information with the Import into WebLearn
		// link as can be seen in the difference between the OpenURL version and
		// our version.
		// http://localhost:8080/savecite/71b84348-5962-4e0e-aa49-4ce5824ed84f?sakai.session.key=nada&sid=google&genre=article&au=Elsworth,+JD&au=Glover,+V.&au=Reynolds,+GP&au=Sandler,+M.&au=Lees,+AJ&au=Phuapradit,+P.&au=Shaw,+KM&au=Stern,+GM&au=Kumar,+P.&atitle=Deprenyl+administration+in+man:+a+selective+monoamine+oxidase+B+inhibitor+without+the+%E2%80%98cheese+effect%E2%80%99&title=Psychopharmacology&volume=57&issue=1&pages=33-38&date=1978&publisher=Springer
		// http://oxfordsfx.hosted.exlibrisgroup.com/oxford?sid=google&auinit=JD&aulast=Elsworth&atitle=Deprenyl+administration+in+man:+a+selective+monoamine+oxidase+B+inhibitor+without+the+%E2%80%98cheese+effect%E2%80%99&id=doi:10.1007/BF00426954&title=Psychopharmacology&volume=57&issue=1&date=1978&spage=33&issn=0033-3158

		String genre = (String) params.get("genre");
		String[] authors = (String[]) params.get("au");
		String title = (String) params.get("title");
		String atitle = (String) params.get("atitle");
		String volume = (String) params.get("volume");
		String issue = (String) params.get("issue");
		String pages = (String) params.get("pages");
		String publisher = (String) params.get("publisher");
		String date = (String) params.get("date");
		String id = (String) params.get("id");

		// do we have enough info for a meaningful citation?
		if ((title == null || title.trim().equals(""))
			&& (atitle == null || atitle.trim().equals(""))) {
			// both title AND atitle are null
			return null;
		}

		// force a generic genre if we don't know any better
		if (genre == null || genre.trim().equals("")) {
			genre = CitationService.UNKNOWN_TYPE;
		}

		Citation citation = citationService.addCitation(genre);

		String info = "New citation:\n\t genre:\t\t"
			+ genre;

		// Generally, only books have a title that's the actual title of the
		// piece.
		// We'll check to see if there's an atitle; if not, use the title as the
		// work's title. Otherwise, use the title as the source.

		if (title != null) {
			if (atitle != null) {
				info += "\n\t source title:\t\t" + title;
				citation.setCitationProperty(Schema.SOURCE_TITLE, title);
			} else {
				info += "\n\t title:\t\t" + title;
				citation.setCitationProperty(Schema.TITLE, title);
			}
		}

		if (atitle != null) {
			info += "\n\t title:\t\t" + atitle;
			citation.setCitationProperty(Schema.TITLE, atitle);
		}

		if (authors != null && authors.length > 0) {
			for (int i = 0; i < authors.length; i++) {
				info += "\n\t au:\t\t" + authors[i];
				citation.setCitationProperty(Schema.CREATOR, authors[i]);
			}
		}

		if (volume != null) {
			info += "\n\t volume:\t\t" + volume;
			citation.setCitationProperty(Schema.VOLUME, volume);
		}
		if (issue != null) {
			info += "\n\t issue:\t\t" + issue;
			citation.setCitationProperty(Schema.ISSUE, issue);
		}
		if (pages != null) {
			info += "\n\t pages:\t\t" + pages;
			citation.setCitationProperty(Schema.PAGES, pages);
		}
		if (publisher != null) {
			info += "\n\t publisher:\t\t" + publisher;
			citation.setCitationProperty(Schema.PUBLISHER, publisher);
		}
		if (date != null) {
			info += "\n\t date:\t\t" + date;
			citation.setCitationProperty(Schema.YEAR, date);
		}
		if (id != null) {
			info += "\n\t id:\t\t" + id;
			citation.setCitationProperty(Schema.ISN, id);
		}
		info += "\n";

		// M_log.info(info);

		return citation;

	}


	/**
	 * Setup the request/response ready for Velocity.
	 */
	protected void setupResponse(HttpServletRequest req, HttpServletResponse res) {
		// the context wraps our real vm attribute set
		ResourceProperties props = new org.sakaiproject.util.BaseResourceProperties();
		setVmReference("props", props, req);
		
		setVmReference("validator", new Validator(), req);
		setVmReference("tlang", rb, req);
		res.setContentType("text/html; charset=UTF-8");
		
		String client = req.getParameter("client");
		
		if(client != null && ! client.trim().equals("")) {
			Locale locale = rb.getLocale();
			List<Map<String,String>> clientMaps = configurationService.getSaveciteClientsForLocale(locale);
			for(Map<String,String> clientMap : clientMaps) {
				String clientId = clientMap.get("id");
				if(clientId == null || clientId.trim().equals("")) {
					continue;
				}
				if(client.trim().equalsIgnoreCase(clientId)) {
					setVmReference("client", clientMap,req);
					break;
				}
			}
		}
	}
	
	/**
	 * Make a redirect to the login url.
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request.
	 * @param res
	 *        HttpServletResponse object back to the client.
	 * @param path
	 *        The current request path, set ONLY if we want this to be where to redirect the user after successfull login
	 * @throws IOException 
	 */
	protected void doLogin(HttpServletRequest req, HttpServletResponse res, String path) throws ToolException, IOException
	{
		// if basic auth is valid do that
		if ( basicAuth.doAuth(req,res) ) {
			log.debug("BASIC Auth Request Sent to the Browser ");
			return;
		} 
		
		
		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// set the return path for after login if needed (Note: in session, not tool session, special for Login helper)
		if (path != null)
		{
			// where to go after
			session.setAttribute(Tool.HELPER_DONE_URL, Web.returnUrl(req, path));
		}

		// check that we have a return path set; might have been done earlier
		if (session.getAttribute(Tool.HELPER_DONE_URL) == null)
		{
			log.warn("doLogin - proceeding with null HELPER_DONE_URL");
		}

		// map the request to the helper, leaving the path after ".../options" for the helper
		ActiveTool tool = ActiveToolManager.getActiveTool("sakai.login");
		String context = req.getContextPath() + req.getServletPath() + "/login";
		tool.help(req, res, context, "/login");
	}
	
	/**
	 * Utility method to return errors as the response
	 * 
	 * @param res   response associated with this request
	 * @param code  error code
	 */
	protected void sendError(HttpServletResponse res, int code)
	{
		try
		{
			res.sendError(code);
		}
		catch (Throwable t)
		{
			log.warn("sendError: {}", t);
		}
	}
}
