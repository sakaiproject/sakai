/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.cheftool.VmServlet;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationHelper;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
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
public class CitationServlet extends VmServlet
{
	/**
	 * 
	 */
	public static final String SERVLET_TEMPLATE = "/vm/servlet.vm";
	private String collectionTitle = null;
	
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(CitationServlet.class);

	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("citations");

	/** set to true when init'ed. */
//	protected boolean m_ready = false;

	protected BasicAuth basicAuth = null;

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
			// try to add the Citation
			Citation citation = addCitation( ( ParameterParser )req.getAttribute( ATTR_PARAMS ),
					option, res );
			if( citation != null )
			{
				// return success
				M_log.debug( "doGet() [addCitation()] added Citation '" + citation.getDisplayName() + "'" );
				respond( Status.SUCCESS, citation, req, res );
			}
			else
			{
				// return failure
				M_log.debug( "doGet() [addCitation()] failed to add citation" );
				respond( Status.ERROR, null, req, res );
			}
		}
	}
		
	/**
	 * respond to an HTTP POST request; only to handle the login process
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

	/**
	 * handle get and post communication from the user
	 * 
	 * @param req  HttpServletRequest object with the client request
	 * @param res  HttpServletResponse object back to the client
	 */
	public Citation addCitation( ParameterParser params, String option, HttpServletResponse res )
	{
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
		
		// get services from ComponentManager
		ContentHostingService contentService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		CitationService citationService = (CitationService) ComponentManager.get("org.sakaiproject.citation.api.CitationService");
		
		CitationCollection collection = null;
		Citation citation = null;
		
		try
        {
			String resourceId = contentService.resolveUuid(resourceUuid);
			
			// check to see if user has revise permission
			if( !citationService.allowReviseCitationList( resourceId ) )
			{
				// revise permission denied
				return null;
			}
			
			// revise permission granted
			ContentResource resource = contentService.getResource(resourceId);
			
			String collectionId = new String(resource.getContent());
			collection = citationService.getCollection(collectionId);

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

			// do we have enough info for a meaningful citation?
			if( ( title == null || title.trim().equals("") ) &&
					( atitle == null || atitle.trim().equals("") ) ) {
				// both title AND atitle are null
				return null;
			}
			
			// force a generic genre if we don't know any better
			if (genre == null || genre.trim().equals("")) {
				genre = CitationService.UNKNOWN_TYPE;
			}
			
			citation = citationService.addCitation(genre);

			String info = "New citation from Google Scholar:\n\t genre:\t\t" + genre;
			
			// Generally, only books have a title that's the actual title of the piece.
			// We'll check to see if there's an atitle; if not, use the title as the 
			// work's title. Otherwise, use the title as the source.
			
			if(title != null)
			{
				if (atitle != null) 
				{
					info += "\n\t source title:\t\t" + title;
					citation.addPropertyValue(Schema.SOURCE_TITLE, title);
				} else 
				{
					info += "\n\t title:\t\t" + title;
					citation.addPropertyValue(Schema.TITLE, title);
				}
			}
			
			if(atitle != null)
			{
				info += "\n\t title:\t\t" + atitle;
				citation.addPropertyValue(Schema.TITLE, atitle);
			}			
			
			if(authors != null && authors.length > 0)
			{
				for(int i = 0; i < authors.length; i++)
				{
					info += "\n\t au:\t\t" + authors[i];
					citation.addPropertyValue(Schema.CREATOR, authors[i]);
				}
			}

			if(volume != null)
			{
				info += "\n\t volume:\t\t" + volume;
				citation.addPropertyValue(Schema.VOLUME, volume);
			}
			if(issue != null)
			{
				info += "\n\t issue:\t\t" + issue;
				citation.addPropertyValue(Schema.ISSUE, issue);
			}
			if(pages != null)
			{
				info += "\n\t pages:\t\t" + pages;
				citation.addPropertyValue(Schema.PAGES, pages);
			}
			if(publisher != null)
			{
				info += "\n\t publisher:\t\t" + publisher;
				citation.addPropertyValue(Schema.PUBLISHER, publisher);
			}
			if(date != null)
			{
				info += "\n\t date:\t\t" + date;
				citation.addPropertyValue(Schema.YEAR, date);
			}
			if(id != null)
			{
				info += "\n\t id:\t\t" + id;
				citation.addPropertyValue(Schema.ISN, id);
			}
			info += "\n";
			
			collection.add(citation);
			citationService.save(collection);
			
			//M_log.info(info);
			
			// get the citation list title
			String refStr = contentService.getReference(resourceId);
			Reference ref = EntityManager.newReference(refStr);
			this.collectionTitle = ref.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
        }
        catch (PermissionException e)
        {
	        // TODO Auto-generated catch block
	        M_log.warn("PermissionException ", e);
        	return null;
        }
        catch (IdUnusedException e)
        {
	        // TODO Auto-generated catch block
	        M_log.warn("IdUnusedException ", e);
        	return null;
        }
        catch (TypeException e)
        {
	        // TODO Auto-generated catch block
	        M_log.warn("TypeException ", e);
        	return null;
        }
        catch (ServerOverloadException e)
        {
	        // TODO Auto-generated catch block
        	M_log.warn("ServerOverloadException ", e);
        	return null;
        }

		return citation;
	}

	protected void respond( Status status, Citation citation,
			HttpServletRequest req, HttpServletResponse res ) throws ServletException
	{
		// the context wraps our real vm attribute set
		ResourceProperties props = new org.sakaiproject.util.BaseResourceProperties();
		setVmReference("props", props, req);
		
		setVmReference("validator", new Validator(), req);
		setVmReference("tlang", rb, req);
		res.setContentType("text/html; charset=UTF-8");
		
		Object success = null;
		if( status == Status.SUCCESS )
		{
			success = new Object();
			setVmReference( "citation", citation, req );
			
			// schedule a refresh of the main toolframe
//			ToolSession toolSession = SessionManager.getCurrentToolSession();
//			toolSession.setAttribute( "sakai.vppa.top.refresh", Boolean.TRUE );
			setVmReference("topRefresh", Boolean.TRUE, req );  // TODO
		}
		
		// set the success flag
		setVmReference("success", success, req);
		
		// include object arrays for formatted messages
		if( collectionTitle == null || collectionTitle.trim().equals("") )
		{
			collectionTitle = "your current citation list";
		}
		Object[] titleArgs = { collectionTitle };  // TODO temporary placeholder
		setVmReference( "titleArgs", titleArgs, req );

		// return the servlet template
		includeVm( SERVLET_TEMPLATE, req, res );
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
			//System.err.println("BASIC Auth Request Sent to the Browser ");
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
			M_log.warn("doLogin - proceeding with null HELPER_DONE_URL");
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
			M_log.warn("sendError: " + t);
		}
	}
}
