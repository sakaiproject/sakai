/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.sakaiproject.dav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Vector;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.util.DOMWriter;
import org.apache.catalina.util.XMLWriter;
import org.apache.tomcat.util.buf.UDecoder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.dav.MD5Encoder;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;

/**
 * Servlet which adds support for WebDAV level 2. All the basic HTTP requests are handled by the DefaultServlet.
 */
@Slf4j
public class DavServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.dav.bundle.Messages");
	// -------------------------------------------------------------- Constants

	private static final String METHOD_HEAD = "HEAD";

	private static final String METHOD_PROPFIND = "PROPFIND";

	private static final String METHOD_PROPPATCH = "PROPPATCH";

	private static final String METHOD_OPTIONS = "OPTIONS";

	private static final String METHOD_MKCOL = "MKCOL";

	private static final String METHOD_COPY = "COPY";

	private static final String METHOD_MOVE = "MOVE";

	private static final String METHOD_LOCK = "LOCK";

	private static final String METHOD_UNLOCK = "UNLOCK";

	private static final String METHOD_GET = "GET";

	private static final String METHOD_PUT = "PUT";

	private static final String METHOD_POST = "POST";

	private static final String METHOD_DELETE = "DELETE";

	/**
	 * Default depth is infite.
	 */
	private static final int INFINITY = 3; // To limit tree browsing a bit

	/**
	 * PROPFIND - Specify a property mask.
	 */
	private static final int FIND_BY_PROPERTY = 0;

	/**
	 * PROPFIND - Display all properties.
	 */
	private static final int FIND_ALL_PROP = 1;

	/**
	 * PROPFIND - Return property names.
	 */
	private static final int FIND_PROPERTY_NAMES = 2;

	/**
	 * Create a new lock.
	 */
	private static final int LOCK_CREATION = 0;

	/**
	 * Refresh lock.
	 */
	private static final int LOCK_REFRESH = 1;

	/**
	 * Default lock timeout value.
	 */
	private static final int DEFAULT_TIMEOUT = 3600;

	/**
	 * Maximum lock timeout.
	 */
	private static final int MAX_TIMEOUT = 604800;

	/**
	 * Read only flag. By default, it's set to true.
	 */
	protected boolean readOnly = true;

	/**
	 * Default namespace.
	 */
	protected static final String DEFAULT_NAMESPACE = "DAV:";

	/** delimiter for form multiple values */
	static final String FORM_VALUE_DELIMETER = "^";
	
	/**
	 * Size of buffer for streaming downloads 
	 */
	protected static final int STREAM_BUFFER_SIZE = 102400;
	
	/**
	 *  Max Size for xml property streams 4K 
	 */
	protected static final int MAX_XML_STREAM_LENGTH = 4096;

	/** Configuration: allow use of alias for site id in references. */
	protected boolean m_siteAlias = true;        

        // can be called on id with or withing adjustid, since
        // the prefixes we check for are not adjusted
        protected boolean prohibited(String id) {
	    if (id == null)
		return false;
	    if (id.startsWith("/attachment/") || id.equals("/attachment") ||
		(doProtected && id.toLowerCase().indexOf("/protected") >= 0 &&
		 (!contentHostingService.allowAddCollection(adjustId(id)))))
		return true;
	    return false;
	}


	/**
	 * Adjust the id (a resource id) to map between any tricks we want to play and the real id for content hosting.
	 * @param id the id to adjust.
	 * @return the adjusted id.
	 */
	protected String adjustId(String id)
	{
		// Note: code stolen and to be kept synced wtih BaseContentService.parseEntityReference() -ggolden

		// map unknown prefix to, if "~", /user/, else /group/
		if (contentHostingService.isShortRefs())
		{
			// ignoring the first separator, get the first item separated from the rest
			String prefix[] = StringUtil.splitFirst((id.length() > 1) ? id.substring(1) : "", Entity.SEPARATOR);
			if (prefix.length > 0)
			{
				// the following are recognized as full reference prefixe; if seen, the sort ref feature is not applied
				if (!(prefix[0].equals("group") || prefix[0].equals("user") || prefix[0].equals("group-user")
						|| prefix[0].equals("public") || prefix[0].equals("private") || prefix[0].equals("attachment")))
				{
					String newPrefix = null;
	
					// a "~" starts a /user/ reference
					if (prefix[0].startsWith("~"))
					{
						newPrefix = Entity.SEPARATOR + "user" + Entity.SEPARATOR + prefix[0].substring(1);
					}
	
					// otherwise a /group/ reference
					else
					{
						newPrefix = Entity.SEPARATOR + "group" + Entity.SEPARATOR + prefix[0];
					}
	
					// reattach the tail (if any) to get the new id (if no taik, make sure we end with a separator if id started out with one)
					id = newPrefix
							+ ((prefix.length > 1) ? (Entity.SEPARATOR + prefix[1])
									: (id.endsWith(Entity.SEPARATOR) ? Entity.SEPARATOR : ""));
				}
			}
		}

		// recognize /user/EID and makeit /user/ID
		// change /group/alias_name and make it /group/site_id
		String parts[] = StringUtil.split(id, Entity.SEPARATOR);
		if (parts.length >= 3)
		{
			if (parts[1].equals("user"))
			{
				try
				{
					// if successful, the context is already a valid user id
					UserDirectoryService.getUser(parts[2]);
				}
				catch (UserNotDefinedException tryEid)
				{
					try
					{
						// try using it as an EID
						String userId = UserDirectoryService.getUserId(parts[2]);
						
						// switch to the ID
						parts[2] = userId;
						String newId = StringUtil.unsplit(parts, Entity.SEPARATOR);

						// add the trailing separator if needed
						if (id.endsWith(Entity.SEPARATOR)) newId += Entity.SEPARATOR;

						id = newId;
					}
					catch (UserNotDefinedException notEid)
					{
						// if context was not a valid EID, leave it alone
					}
				}
				
			} 
			else if (parts[1].equals("group"))
			{
				String context = parts[2];

 				// recognize alias for site id - but if a site id exists that matches the requested site id, that's what we will use
 				if (m_siteAlias && (context != null) && (context.length() > 0))
 				{
 					if (!SiteService.siteExists(context))
 					{
 						try
 						{
 							String target = aliasService.getTarget(context);
							Reference targetRef = EntityManager.newReference(target);
 							boolean changed = false;

 							// for a site reference
 							if (SiteService.APPLICATION_ID.equals(targetRef.getType()))
 							{
 								// use the ref's id, i.e. the site id
 								context = targetRef.getId();
 								changed = true;
 							}

 							// for mail archive reference
 							// TODO: taken from MailArchiveService.APPLICATION_ID to (fake) reduce a dependency -ggolden
 							else if ("sakai:mailarchive".equals(targetRef.getType()))
 							{
 								// use the ref's context as the site id
 								context = targetRef.getContext();
 								changed = true;
 							}

 							// if changed, update the id
 							if (changed)
 							{
 								parts[2] = context;
 								String newId = StringUtil.unsplit(parts, Entity.SEPARATOR);
			
								// add the trailing separator if needed
 								if (id.endsWith(Entity.SEPARATOR)) newId += Entity.SEPARATOR;
 								id = newId;
 							}
 						}
 						catch (IdUnusedException noAlias)
 						{
 						}
 					}
 				}         
			}
		}
		// recognize /group-user/SITE_ID/USER_EID and make it /group-user/SITE_ID/USER_ID 
		if (parts.length >= 4)
		{
			if (parts[1].equals("group-user"))
			{
				try
				{
					// if successful, the context is already a valid user id
					UserDirectoryService.getUser(parts[3]);
				}
				catch (UserNotDefinedException tryEid)
				{
					try
					{
						// try using it as an EID
						String userId = UserDirectoryService.getUserId(parts[3]);

						// switch to the ID
						parts[3] = userId;
						String newId = StringUtil.unsplit(parts, Entity.SEPARATOR);

						// add the trailing separator if needed
						if (id.endsWith(Entity.SEPARATOR)) newId += Entity.SEPARATOR;

						id = newId;
					}
					catch (UserNotDefinedException notEid)
					{
						// if context was not a valid EID, leave it alone
					}
				}
			}
		}

		return id;
	}

	/**
	 * Simple date format for the creation date ISO representation (partial).
	 */
	private SimpleDateFormat creationDateFormat()
	{
		final SimpleDateFormat creationDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		creationDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return creationDateFormat;
	}

	/**
	 * Simple date format for the HTTP Date
	 */
	private SimpleDateFormat httpDateFormat()
	{
		final SimpleDateFormat httpDateFormat = new SimpleDateFormat(
				"EEE, d MMM yyyy HH:mm:ss z", Locale.US);
		httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return httpDateFormat;
	}

	/**
	 * The set of SimpleDateFormat formats to use in getDateHeader().
	 */
	private SimpleDateFormat[] dateFormats()
	{
		final SimpleDateFormat formats[] = {
				new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
				new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz",
						Locale.US),
				new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };
		return formats;
	}

	// ----------------------------------------------------- Instance Variables

	/**
	 * Repository of the locks put on single resources.
	 * <p>
	 * Key : path <br>
	 * Value : LockInfo
	 */
	private Hashtable<String,LockInfo> resourceLocks = new Hashtable<String,LockInfo>();

	/**
	 * Repository of the lock-null resources.
	 * <p>
	 * Key : path of the collection containing the lock-null resource<br>
	 * Value : Vector of lock-null resource which are members of the collection. Each element of the Vector is the path associated with the lock-null resource.
	 */
	private Hashtable<String,Vector<String>> lockNullResources = new Hashtable<String,Vector<String>>();

	/**
	 * Vector of the heritable locks.
	 * <p>
	 * Key : path <br>
	 * Value : LockInfo
	 */
	private Vector<LockInfo> collectionLocks = new Vector<LockInfo>();

	/**
	 * Secret information used to generate reasonably secure lock ids.
	 */
	private String secret = "catalina";

	/**
	 * Don't show directories starting with "protected" to non-owner This defaults off because it requires corresponding changes in AccessServlet, which currently aren't present.
	 */
	private boolean doProtected = false;

	/**
	 * MD5 message digest provider.
	 */
	protected static MessageDigest md5Helper;

	/**
	 * Array of file patterns we are not supposed to accept on PUT
	 */
	private String[] ignorePatterns = null;
	
	/**
	 * Output cookies for DAV requests
	 */
	private boolean useCookies = false;

	/**
	 * Non Dav Browsers
	 */
	private String[] nonDavUserAgent = null;

	private ContentHostingService contentHostingService;

	private CitationService citationService;

	private org.sakaiproject.entity.api.EntityManager entityManager;

	private AliasService aliasService;

	// --------------------------------------------------------- Public Methods

	/**
	 * Initialize this servlet.
	 */
	public void init() throws ServletException
	{
		contentHostingService = (ContentHostingService) ComponentManager.get(ContentHostingService.class.getName());
		citationService = ComponentManager.get(CitationService.class);
		entityManager = ComponentManager.get(org.sakaiproject.entity.api.EntityManager.class);
		aliasService = ComponentManager.get(AliasService.class);

		// Set our properties from the initialization parameters
		String value = null;

		try
		{
			value = getServletConfig().getInitParameter("readonly");
			if (value != null) readOnly = (new Boolean(value)).booleanValue();
		}
		catch (Throwable t)
		{
			;
		}

		try
		{
			value = getServletConfig().getInitParameter("secret");
			if (value != null) secret = value;
		}
		catch (Throwable t)
		{
			;
		}

		try
		{
			value = getServletConfig().getInitParameter("doprotected");
			if (value != null) doProtected = (new Boolean(value)).booleanValue();
		}
		catch (Throwable t)
		{
			;
		}

		// load up the ignorePatterns from properties
		ignorePatterns = ServerConfigurationService.getStrings("webdav.ignore");
		if (ignorePatterns != null)
		{
			String outVal = "";
			for (int i = 0; i < ignorePatterns.length; i++)
			{
				if (outVal.length() > 0) outVal = outVal + " : ";
				outVal = outVal + ignorePatterns[i];
			}
			log.info("ignore patterns:" + outVal);
		}

		// Load the MD5 helper used to calculate signatures.
		try
		{
			md5Helper = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			log.error(e.getMessage(), e);
			throw new IllegalStateException();
		}
		
		// Check cookie configuration
		useCookies = ServerConfigurationService.getBoolean("webdav.cookies", false);

		nonDavUserAgent = ServerConfigurationService.getStrings("webdav.nonDavUserAgent");
		if (nonDavUserAgent == null) {
		    nonDavUserAgent = new String[] {  "Mozilla", "Opera", "BlackBerry" };
		}
	}

	/** create the info */
	public SakaidavServletInfo newInfo(HttpServletRequest req)
	{
		return new SakaidavServletInfo(req);

	} // newInfo

	public class SakaidavServletInfo
	{
		// all properties from the request
		protected Properties m_options = null;

		public Properties getOptions()
		{
			return m_options;
		}

		/** construct from the req */
		@SuppressWarnings("unchecked")
		public SakaidavServletInfo(HttpServletRequest req)
		{
			m_options = new Properties();

			Enumeration e = req.getParameterNames();
			while (e.hasMoreElements())
			{
				String key = (String) e.nextElement();
				String[] values = req.getParameterValues(key);
				if (values.length == 1)
				{
					m_options.put(key, values[0]);
				}
				else
				{
					StringBuilder buf = new StringBuilder();
					for (int i = 0; i < values.length; i++)
					{
						buf.append(values[i] + FORM_VALUE_DELIMETER);
					}
					m_options.put(key, buf.toString());
				}
			}

		} // SakaidavServletInfo

		/** return the m_options as a string */
		@SuppressWarnings("unchecked")
		public String optionsString()
		{
			StringBuilder buf = new StringBuilder(1024);
			Enumeration e = m_options.keys();
			while (e.hasMoreElements())
			{
				String key = (String) e.nextElement();
				String o = m_options.getProperty(key);
				buf.append(key);
				buf.append("=");
				buf.append(o.toString());
				buf.append("&");
			}

			return buf.toString();

		} // optionsString

	} // SakaidavServletInfo

	// From DefaultServlet

	protected String getUserPropertyDisplayName(ResourceProperties props, String name)
	{
		String id = props.getProperty(name);
		if (id != null)
		{
			try
			{
				User u = UserDirectoryService.getUser(id);
				return u.getDisplayName();
			}
			catch (UserNotDefinedException e)
			{
				return id;
			}
		}

		return "unknown";
	}

	/**
	 * Show HTTP header information.
	 */
	@SuppressWarnings("unchecked")
	protected void showRequestInfo(HttpServletRequest req)
	{

		if (log.isDebugEnabled()) log.debug("DefaultServlet Request Info");

		// Show generic info
		if (log.isDebugEnabled()) log.debug("Encoding : " + req.getCharacterEncoding());
		if (log.isDebugEnabled()) log.debug("Length : " + req.getContentLength());
		if (log.isDebugEnabled()) log.debug("Type : " + req.getContentType());

		if (log.isDebugEnabled()) log.debug("Parameters");

		Enumeration parameters = req.getParameterNames();

		while (parameters.hasMoreElements())
		{
			String paramName = (String) parameters.nextElement();
			String[] values = req.getParameterValues(paramName);
			log.debug("{}:", paramName);
			for (int i = 0; i < values.length; i++)
			{
				log.debug("{},", values[i]);
			}
		}

		if (log.isDebugEnabled()) log.debug("Protocol : " + req.getProtocol());
		if (log.isDebugEnabled()) log.debug("Address : " + req.getRemoteAddr());
		if (log.isDebugEnabled()) log.debug("Host : " + req.getRemoteHost());
		if (log.isDebugEnabled()) log.debug("Scheme : " + req.getScheme());
		if (log.isDebugEnabled()) log.debug("Server Name : " + req.getServerName());
		if (log.isDebugEnabled()) log.debug("Server Port : " + req.getServerPort());

		if (log.isDebugEnabled()) log.debug("Attributes");

		Enumeration attributes = req.getAttributeNames();

		while (attributes.hasMoreElements())
		{
			String attributeName = (String) attributes.nextElement();
			log.debug("{}: ", attributeName);
			if (log.isDebugEnabled()) log.debug(req.getAttribute(attributeName).toString());
		}

		// Show HTTP info
		if (log.isDebugEnabled()) log.debug("HTTP Header Info");

		if (log.isDebugEnabled()) log.debug("Authentication Type : " + req.getAuthType());
		if (log.isDebugEnabled()) log.debug("HTTP Method : " + req.getMethod());
		if (log.isDebugEnabled()) log.debug("Path Info : " + req.getPathInfo());
		if (log.isDebugEnabled()) log.debug("Path translated : " + req.getPathTranslated());
		if (log.isDebugEnabled()) log.debug("Query string : " + req.getQueryString());
		if (log.isDebugEnabled()) log.debug("Remote user : " + req.getRemoteUser());
		if (log.isDebugEnabled()) log.debug("Requested session id : " + req.getRequestedSessionId());
		if (log.isDebugEnabled()) log.debug("Request URI : " + req.getRequestURI());
		if (log.isDebugEnabled()) log.debug("Context path : " + req.getContextPath());
		if (log.isDebugEnabled()) log.debug("Servlet path : " + req.getServletPath());
		if (log.isDebugEnabled()) log.debug("User principal : " + req.getUserPrincipal());
		if (log.isDebugEnabled()) log.debug("Headers : ");

		Enumeration headers = req.getHeaderNames();

		while (headers.hasMoreElements())
		{
			String headerName = (String) headers.nextElement();
			log.debug("{}: ", headerName);
			if (log.isDebugEnabled()) log.debug(req.getHeader(headerName));
		}
	}

	/**
	 * Return the relative path associated with this servlet.
	 * 
	 * @param request
	 *        The servlet request we are processing
	 */
	protected String getRelativePath(HttpServletRequest request)
	{

		// Are we being processed by a RequestDispatcher.include()?
		if (request.getAttribute("javax.servlet.include.request_uri") != null)
		{
			String result = (String) request.getAttribute("javax.servlet.include.path_info");
			if (result == null) result = (String) request.getAttribute("javax.servlet.include.servlet_path");
			if ((result == null) || (result.equals(""))) result = "/";
			return (result);
		}

		// Are we being processed by a RequestDispatcher.forward()?
		if (request.getAttribute("javax.servlet.forward.request_uri") != null)
		{
			String result = (String) request.getAttribute("javax.servlet.forward.path_info");
			if (result == null) result = (String) request.getAttribute("javax.servlet.forward.servlet_path");
			if ((result == null) || (result.equals(""))) result = "/";
			return (result);
		}

		// No, extract the desired path directly from the request
		String result = getRelativePathSAKAI(request);
		
		return normalize(result);

	}

	/**
	 * Return a context-relative path, beginning with a "/", that represents the canonical version of the specified path after ".." and "." elements are resolved out. If the specified path attempts to go outside the boundaries of the current context (i.e.
	 * too many ".." path elements are present), return <code>null</code> instead.
	 * 
	 * @param path
	 *        Path to be normalized
	 */
	protected String normalize(String path)
	{
		if (path == null) return null;

		// Create a place for the normalized path
		String normalized = path;

		/*
		 * Commented out -- already URL-decoded in StandardContextMapper Decoding twice leaves the container vulnerable to %25 --> '%' attacks. if (normalized.indexOf('%') >= 0) normalized = RequestUtil.URLDecode(normalized, "UTF8");
		 */

		if (normalized.equals("/.")) return "/";

		// Normalize the slashes and add leading slash if necessary
		if (normalized.indexOf('\\') >= 0) normalized = normalized.replace('\\', '/');
		if (!normalized.startsWith("/")) normalized = "/" + normalized;

		// Resolve occurrences of "//" in the normalized path
		while (true)
		{
			int index = normalized.indexOf("//");
			if (index < 0) break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 1);
		}

		// Resolve occurrences of "/./" in the normalized path
		while (true)
		{
			int index = normalized.indexOf("/./");
			if (index < 0) break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 2);
		}

		// Resolve occurrences of "/../" in the normalized path
		while (true)
		{
			int index = normalized.indexOf("/../");
			if (index < 0) break;
			if (index == 0) return (null); // Trying to go outside our context
			int index2 = normalized.lastIndexOf('/', index - 1);
			normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
		}

		// Return the normalized path that we have completed
		return (normalized);

	}

	/**
	 * URL rewriter.
	 * 
	 * @param path
	 *        Path which has to be rewiten
	 */
	protected String rewriteUrl(String path)
	{

		/**
		 * Note: This code portion is very similar to URLEncoder.encode. Unfortunately, there is no way to specify to the URLEncoder which characters should be encoded. Here, ' ' should be encoded as "%20" and '/' shouldn't be encoded.
		 */

		int maxBytesPerChar = 10;
		StringBuilder rewrittenPath = new StringBuilder(path.length());
		ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
		OutputStreamWriter writer = null;
		try
		{
			writer = new OutputStreamWriter(buf, "UTF8");
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			writer = new OutputStreamWriter(buf);
		}

		for (int i = 0; i < path.length(); i++)
		{
			int c = (int) path.charAt(i);
			if (safeCharacters.get(c))
			{
				rewrittenPath.append((char) c);
			}
			else
			{
				// convert to external encoding before hex conversion
				try
				{
					writer.write(c);
					writer.flush();
				}
				catch (IOException e)
				{
					buf.reset();
					continue;
				}
				byte[] ba = buf.toByteArray();
				for (int j = 0; j < ba.length; j++)
				{
					// Converting each byte in the buffer
					byte toEncode = ba[j];
					rewrittenPath.append('%');
					int low = (int) (toEncode & 0x0f);
					int high = (int) ((toEncode & 0xf0) >> 4);
					rewrittenPath.append(hexadecimal[high]);
					rewrittenPath.append(hexadecimal[low]);
				}
				buf.reset();
			}
		}

		return rewrittenPath.toString();

	}

	/**
	 * Array containing the safe characters set.
	 */
	protected static BitSet safeCharacters;

	protected static final char[] hexadecimal = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	// ----------------------------------------------------- Static Initializer

	static
	{
		safeCharacters = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++)
		{
			safeCharacters.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++)
		{
			safeCharacters.set(i);
		}
		for (i = '0'; i <= '9'; i++)
		{
			safeCharacters.set(i);
		}
		safeCharacters.set('-');
		safeCharacters.set('_');
		safeCharacters.set('.');
		safeCharacters.set('*');
		safeCharacters.set('/');
	}

	// ------------------------------------------------------ Protected Methods

	/**
	 * Return JAXP document builder instance.
	 */
	protected DocumentBuilder getDocumentBuilder() throws ServletException
	{
		DocumentBuilder documentBuilder = null;
		DocumentBuilderFactory documentBuilderFactory = null;
		try
		{
			documentBuilderFactory =
				DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setExpandEntityReferences(false);
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false); 
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false); 

			documentBuilder =
				documentBuilderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			throw new ServletException("Sakaidavservlet.jaxpfailed");
		}
		return documentBuilder;
	}

	/**
	 * Setup and cleanup around this request.
	 * 
	 * @param req
	 *        HttpServletRequest object with the client request
	 * @param res
	 *        HttpServletResponse object back to the client
	 */
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException
	{
		SakaidavServletInfo info = newInfo(req);

		// try to authenticate based on a Principal (one of ours) in the req
		Principal prin = req.getUserPrincipal();

		//SAK-14776 - In order for WAS to return the Principal with getUserPrincipal()
		//security needs to be enabled. We have employed a custom JAAS module to handle
		//WAS's user security for the Sakai WebApp. SakaiWASLoginModule also acts as a wrapper
		//to fetch PrivateCredentials from WAS. The user password is stored in those
		//credentials. Once all information is obtained, the Principal is remade and 
		//DavServlet is none the wiser. 
		//The Login Module code can be found at:
		//https://source.sakaiproject.org/contrib/websphere/was-login-module/
		/* removed 2013-09-10 -AZ
		if ("websphere".equals(ServerConfigurationService.getString("servlet.container")))
		{
		    //Fetch the credentials collection from the Subject.
		    //A wrapper is used here because we need access to 
		    //com.ibm.ws.security.auth.WSLoginHelperImpl
		    Iterator credItr = null;
		    try {
		        credItr = SakaiWASLoginModule.getSubject().getPrivateCredentials().iterator();
		    } catch (Exception e) {
		        log.error("SAKAIDAV: Unabled to obtain WAS credentials.", e);
		    }

		    String pw = "";
		    while (credItr != null && credItr.hasNext())
		    {
		        //look for the Key-Value pair
		        Object cred = credItr.next();
		        if( cred instanceof SakaiWASLoginModule.SakaiWASLoginKeyValue ) 
		        {
		            SakaiWASLoginModule.SakaiWASLoginKeyValue entry = 
		                    (SakaiWASLoginModule.SakaiWASLoginKeyValue)cred;

		            //extract the password from the Key-Value pair
		            if( "sakai.dav.pw".equals(entry.getKey()) )
		            {
		                pw = (String)entry.getValue();
		                String eid = prin.getName();

		                //remake the Principal with the user eid 
		                //and the recently fetched password
		                prin = new DavPrincipal(eid,pw);
		                break;
		            }
		        }
		    }
		}
		*/

		if ((prin != null) && (prin instanceof DavPrincipal))
		{
			String eid = prin.getName();
			String pw = ((DavPrincipal) prin).getPassword();
			Evidence e = new IdPwEvidence(eid, pw, req.getRemoteAddr());

			// in older versions of this code, we didn't authenticate
			// if there was a session for this user. Unfortunately the
			// these are special non-sakai sessions, which do not
			// have real cookies attached. The cookie looks like
			// username-hostname. That means that they're easy to
			// fake. Since the DAV protocol doesn't actually
			// support sessions in the first place, most clients
			// won't use them. So it's a security hole without
			// any real benefit. Thus we check the password for
			// every transaction. The underlying sessions are still
			// a good idea, as they set the context for later
			// operations. But we can't depend upon the cookies for
			// authentication.

			// authenticate
			try
			{
				if ((eid.length() == 0) || (pw.length() == 0))
				{
					throw new AuthenticationException("missing required fields");
				}

				Authentication a = AuthenticationManager.authenticate(e);

				// No need to log in again if UsageSession is not null, active, and the eid is the 
				// same as that resulting from the DAV basic auth authentication
				
				if ((UsageSessionService.getSession() == null || UsageSessionService.getSession().isClosed()
						|| !a.getEid().equals(UsageSessionService.getSession().getUserEid()))
						&& !UsageSessionService.login(a, req, UsageSessionService.EVENT_LOGIN_DAV))
				{
					// login failed
					res.addHeader("WWW-Authenticate","Basic realm=\"DAV\"");
					res.sendError(401);
					return;
				}
			}
			catch (AuthenticationException ex)
			{
				// not authenticated
				res.addHeader("WWW-Authenticate","Basic realm=\"DAV\"");
				res.sendError(401);
				return;
			}
		}
		else
		{
			// user name missing, so can't authenticate
			res.addHeader("WWW-Authenticate","Basic realm=\"DAV\"");
			res.sendError(401);
			return;
		}

		// Set the client cookie if enabled as this is not done by the RequestFilter for dav requests.
		// This is not required by DAV clients but may be helpful in some load-balancing
		// configurations for session affinity across app servers. However, some Windows DAV clients
		// share cookies with IE7 which can lead to confusing results in the browser session.
		
		if (useCookies) {
			req.setAttribute(RequestFilter.ATTR_SET_COOKIE, true);
		}
		
		// Setup... ?

		try
		{
			doDispatch(info, req, res);
		}
		finally
		{
			log(req, info);
		}
	}

	/** log a request processed */
	public void log(HttpServletRequest req, SakaidavServletInfo info)
	{
		log.debug("from:" + req.getRemoteAddr() + " path:" + req.getPathInfo() + " options: " + info.optionsString());

	} // log

	/**
	 * Handles the special Webdav methods
	 */
	protected void doDispatch(SakaidavServletInfo info, HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException
	{

		String method = req.getMethod();

		if (log.isDebugEnabled())
		{
			String path = getRelativePath(req);
			log.debug("SAKAIDAV doDispatch [" + method + "] " + path);
		}

		String remoteUser = req.getRemoteUser();
		if (log.isDebugEnabled()) log.debug("SAKAIDAV remoteuser = " + remoteUser);
		if (remoteUser == null)
		{
			if (log.isDebugEnabled()) log.debug("SAKAIDAV Requires Authorization");
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		if (method.equals(METHOD_PROPFIND))
		{
			doPropfind(req, resp);
		}
		else if (method.equals(METHOD_PROPPATCH))
		{
			doProppatch(req, resp);
		}
		else if (method.equals(METHOD_MKCOL))
		{
			doMkcol(req, resp);
		}
		else if (method.equals(METHOD_COPY))
		{
			doCopy(req, resp);
		}
		else if (method.equals(METHOD_MOVE))
		{
			doMove(req, resp);
		}
		else if (method.equals(METHOD_LOCK))
		{
			doLock(req, resp);
		}
		else if (method.equals(METHOD_UNLOCK))
		{
			doUnlock(req, resp);
		}
		else if (method.equals(METHOD_GET))
		{
			doGet(req, resp);
		}
		else if (method.equals(METHOD_PUT))
		{
			doPut(req, resp);
		}
		else if (method.equals(METHOD_POST))
		{
			doPost(req, resp);
		}
		else if (method.equals(METHOD_HEAD))
		{
			doHead(req, resp);
		}
		else if (method.equals(METHOD_OPTIONS))
		{
			doOptions(req, resp);
		}
		else if (method.equals(METHOD_DELETE))
		{
			doDelete(req, resp);
		}
		else
		{
			log.warn("SAKAIDAV:Request not supported");
			resp.sendError(SakaidavStatus.SC_NOT_IMPLEMENTED);
			// showRequestInfo(req);
		}

	}

	/**
	 * Determine if this path is one of the prefixes that we have been requested to ignore by the properties settings
	 * 
	 * @param request
	 *        The servlet request we are processing
	 */
	protected boolean isFileNameAllowed(HttpServletRequest req)
	{
		if (ignorePatterns == null) return true;

		String sakaiPath = getRelativePathSAKAI(req);
		for (int i = 0; i < ignorePatterns.length; i++)
		{
			if (sakaiPath.lastIndexOf(ignorePatterns[i]) > 0) return false;
		}
		return true;
	}

	/**
	 * Process a HEAD request for the specified resource.
	 * 
	 * @param request
	 *        The servlet request we are processing
	 * @param response
	 *        The servlet response we are creating
	 * @exception IOException
	 *            if an input/output error occurs
	 * @exception ServletException
	 *            if a servlet-specified error occurs
	 */
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{

		// Call helper
		processHead(request, response);
	}

	/**
	 * Helper to handle set Header information
	 * 
	 * @param request
	 *        The servlet request we are processing
	 * @param response
	 *        The servlet response we are creating
	 * @exception IOException
	 *            if an input/output error occurs
	 * @exception ServletException
	 *            if a servlet-specified error occurs
	 * @return boolean false if there was an error, true if the head variable all were set
	 */
	private boolean processHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{

		String path = getRelativePathSAKAI(request);

		if ((path == null) || prohibited(path) || path.toUpperCase().startsWith("/WEB-INF") || path.toUpperCase().startsWith("/META-INF"))
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return false;
		}

		// Retrieve the resources
		DirContextSAKAI resources = getResourcesSAKAI();
		ResourceInfoSAKAI resourceInfo = new ResourceInfoSAKAI(path, resources);

		if (!resourceInfo.exists)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
			return false;
		}

		if (!resourceInfo.collection)
		{
			response.setDateHeader("Last-Modified", resourceInfo.date);
		}

		// Find content type by looking at the file suffix
		// We should probably make this something which is done within the service
		// rather than each tool

		String contentType = getServletContext().getMimeType(resourceInfo.path);

		// if (log.isDebugEnabled()) log.debug("Default serveResource contentType = " + contentType);
		if (contentType != null)
		{
			response.setContentType(contentType);
		}

		long contentLength = resourceInfo.length;
		if ((!resourceInfo.collection) && (contentLength >= 0))
		{
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4187336
			if (contentLength <= Integer.MAX_VALUE) {
				response.setContentLength((int) contentLength);
			} else {
				response.addHeader("Content-Length", Long.toString(contentLength));
			}
		}
		return true;
	}

	/**
	 * OPTIONS Method.
	 */
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		String path = getRelativePathSAKAI(req);

		resp.addHeader("DAV", "1,2");
		String methodsAllowed = null;

		// Retrieve the resources
		DirContextSAKAI resources = getResourcesSAKAI();

		if (resources == null)
		{
			log.warn("SAKAIDAV doOptions ERROR Resources is null");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		ResourceInfoSAKAI resourceInfo = new ResourceInfoSAKAI(path, resources);

		boolean exists = true;
		try
		{
			resources.lookup(path);
		}
		catch (NamingException e)
		{
			exists = false;
		}

		if (!exists)
		{
			methodsAllowed = "OPTIONS, MKCOL, PUT, LOCK, UNLOCK";
			resp.addHeader("Allow", methodsAllowed);
			return;
		}

		methodsAllowed = "OPTIONS, GET, HEAD, POST, DELETE, PROPFIND, COPY, MOVE, LOCK, UNLOCK";

		// don't know why, but this instanceof test doesn't work
		// if (!(object instanceof DirContext)) {
		if (!resourceInfo.collection)
		{
			methodsAllowed += ", PUT";
		}
		else
		{
			methodsAllowed += ", MKCOL";
		}

		resp.addHeader("Allow", methodsAllowed);

		resp.addHeader("MS-Author-Via", "DAV");

	}

	// Wrappers to make SAKAI look almost like a DirContext - This may be replaced as
	// we move this to an OKI style framework

	public class DirContextSAKAI
	{
		protected String path;

		private DirContext myDC;

		public boolean isCollection;

		private ContentCollection collection;

		public Object lookup(String id) throws NamingException
		{

			path = id;

			// resource or collection? check the properties (also finds bad id and checks permissions)
			isCollection = false;
			try
			{
				ResourceProperties props;

				path = fixDirPathSAKAI(path);

				// Do not allow access to /attachments

				if (path.startsWith("/attachments"))
				{
					log.info("DirContextSAKAI.lookup - You do not have permission to view this area " + path);
					throw new NamingException();
				}

				props = contentHostingService.getProperties(adjustId(path));

				isCollection = props.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);

				if (isCollection)
				{
					collection = contentHostingService.getCollection(adjustId(path));
				}
			}
			catch (PermissionException e)
			{
				log.debug("DirContextSAKAI.lookup - You do not have permission to view this resource " + path);
				throw new NamingException();
			}
			catch (IdUnusedException e)
			{
				log.debug("DirContextSAKAI.lookup - This resource does not exist: " + id);
				throw new NamingException();
			}
			catch (EntityPropertyNotDefinedException e)
			{
				log.warn("DirContextSAKAI.lookup - This resource is empty: " + id);
				throw new NamingException();
			}
			catch (EntityPropertyTypeException e)
			{
				log.warn("DirContextSAKAI.lookup - This resource has a EntityPropertyTypeException exception: " + id);
				throw new NamingException();
			}
			catch (TypeException e)
			{
				log.warn("DirContextSAKAI.lookup - This resource has a type exception: " + id);
				throw new NamingException();
			}

			return myDC;
		}

		@SuppressWarnings("unchecked")
		public Iterator<ContentEntity> list(String id)
		{
			try
			{
				lookup(id);
			}
			catch (Exception e)
			{
				return null;
			}
			if (log.isDebugEnabled()) log.debug("DirContextSAKAI.list getting collection members and iterator");
			List<ContentEntity> members = collection.getMemberResources();
			Iterator<ContentEntity> it = members.iterator();
			return it;
		}
	}

	public class ResourceInfoSAKAI
	{
		private String path;

		public boolean collection;

		public boolean exists;

		public long length;

		public String httpDate;

		public long creationDate;

		public String MIMEType;

		public long modificationDate;

		public long date; // From DirContext

		public String displayName;

		public String resourceName; // The "non-display" name

		public String resourceLink; // The resource link (within SAKAI)

		public String eTag; // The eTag

		public ResourceInfoSAKAI(String our_path, DirContextSAKAI parent_resources)
		{
			path = our_path;
			exists = false;

			// if (log.isDebugEnabled()) log.debug("ResourceInfoSAKAI Constructor path = " + path);

			// resource or collection? check the properties (also finds bad id and checks permissions)
			collection = false;
			try
			{
				Entity mbr;

				path = fixDirPathSAKAI(path); // Add slash as necessary

				ResourceProperties props = contentHostingService.getProperties(adjustId(path));

				collection = props.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);

				resourceName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				displayName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				exists = true;

				if (!collection)
				{
					mbr = contentHostingService.getResource(adjustId(path));
					// Props for a file is OK from above
					length = ((ContentResource) mbr).getContentLength();
					MIMEType = ((ContentResource) mbr).getContentType();
					eTag = ((ContentResource) mbr).getId();
				}
				else
				{
					mbr = contentHostingService.getCollection(adjustId(path));
					props = mbr.getProperties();
					eTag = our_path;
				}
				modificationDate = props.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE).getTime();
				eTag = modificationDate + "+" + eTag;
				// SAK-26593 if you don't clean the eTag you may send invalid XML to client
				// SAK-29338 Cyberduck started to see our md5 etag as an AWS s3-like checksum so let's add a prefix
				eTag = "sakai-" + MD5Encoder.encode(md5Helper.digest(eTag.getBytes()));
				if (log.isDebugEnabled()) log.debug("Path=" + path + " eTag=" + eTag);
				creationDate = props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime();
				resourceLink = mbr.getUrl();

			}
			catch (PermissionException e)
			{
				log.debug("ResourceInfoSAKAI - You do not have permission to view this resource " + path);
			}
			catch (IdUnusedException e)
			{
				log.debug("ResourceInfoSAKAI - This resource does not exist " + path);
			}
			catch (EntityPropertyNotDefinedException e)
			{
				log.warn("ResourceInfoSAKAI - This resource is empty" + path);
			}
			catch (EntityPropertyTypeException e)
			{
				log.warn("ResourceInfoSAKAI - EntityPropertyType Exception " + path);
			}
			catch (TypeException e)
			{
				log.warn("ResourceInfoSAKAI - Type Exception " + path);
			}

			httpDate = getHttpDate(modificationDate);
			if (creationDate == 0) creationDate = modificationDate;
			date = modificationDate;
		}
	}

	public DirContextSAKAI getResourcesSAKAI()
	{
		return new DirContextSAKAI();
	}

	public String fixDirPathSAKAI(String path)
	{

		String tmpPath = path;

		try
		{
			contentHostingService.getProperties(adjustId(tmpPath));
		}
		catch (IdUnusedException e)
		{
			if (!tmpPath.endsWith("/"))
			{ // We will add a slash and try again
				String newPath = tmpPath + "/";
				try
				{
					contentHostingService.getProperties(adjustId(newPath));
					tmpPath = newPath;
				}
				catch (Exception x)
				{
				} // Ignore everything
			}
		}
		catch (PermissionException e)
		{
			// Ignore all other exceptions
		} 
		return tmpPath;
	}

	/**
	 * POST Method.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		String path = getRelativePathSAKAI(req);

		doContent(path, req, resp);
	}

	/**
	 * GET Method.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String header = req.getHeader("User-Agent") != null ? req.getHeader("User-Agent") : "";

		String path = getRelativePathSAKAI(req);

		for (String agent: nonDavUserAgent) {
		    if (header.toUpperCase().contains(agent.toUpperCase())) {
		        if (log.isInfoEnabled()) log.info("Redirecting DAV access because this is a browser." + header);
		        resp.sendRedirect("/access/content" + adjustId(path));
		        return;
		    }
		}

		doContent(path, req, resp);
	}

	protected int countSlashes(String s)
	{
		int count = 0;
		int loc = s.indexOf('/');

		while (loc >= 0)
		{
			count++;
			loc++;
			loc = s.indexOf('/', loc);
		}

		return count;
	}

	// id is known to be a collection
	@SuppressWarnings({ "unchecked", "deprecation" })
	private void doDirectory(String id, HttpServletRequest req, HttpServletResponse res)
	{
		// OK, it's a collection and we can read it. Do a listing.

		if (prohibited(id))
			return;

		String uri = req.getRequestURI();
		PrintWriter out = null;

		// don't set the writer until we verify that
		// getallresources is going to work.

		try
		{
			ContentCollection x = contentHostingService.getCollection(adjustId(id));

			// I want to use relative paths in the listing,
			// so we need to redirect if there's no trailing /
			// for the usual reasons.
			// --this doesn't actually work. without / it doesn't get here
			if (!uri.endsWith("/"))
			{
				log.debug("need redirect");
				try
				{
					res.sendRedirect(uri + "/");
					log.debug("redirect ok");
					return;
				}
				catch (IOException ignore)
				{
					log.debug("redirect failed");
					return;
				}
			}

			List<String> xl = x.getMembers();
			Collections.sort(xl);
			Iterator<String> xi = xl.iterator();

			res.setContentType("text/html; charset=UTF-8");

			out = res.getWriter();
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
			out.println("<html><head>");
			String webappRoot = ServerConfigurationService.getServerUrl();
			out.println("<link href=\"" + webappRoot
					+ "/css/default.css\" type=\"text/css\" rel=\"stylesheet\" media=\"screen\" />");
			out.println("<STYLE type=\"text/css\">");
			out.println("<!--");
			out.println("td {padding-right: .5em}");
			out.println("-->");
			out.println("</STYLE>");
			out.println("</head><body>");
			out.println("<div style=\"padding: 16px\">");
			out.println("<h2>" + rb.getFormattedMessage("contents_of_id", id) + "</h2>");
			out.println("<table>");

			// show .. if not already there. we don't get aliases
			// so this is
			// /group/db5a4d0c-3dfd-4d10-8018-41db42ac7c8b/
			// /user/hedrick/

			int slashes = countSlashes(adjustId(id));

			if (slashes > 3)
			{
				// go up a level
				//String uplev = id.substring(0, id.length() - 1);
				//uplev = uplev.substring(0, uplev.lastIndexOf('/') + 1);
				out.println("<tr><td><a href=\"..\">" + rb.getString("up_one_level") + "</a></td><td><b>" + rb.getString("folder") + "</b>" + "</td><td>" + "</td><td>"
						+ "</td><td>" + "</td></tr>");

			}

			while (xi.hasNext())
			{
				String xs = (String) xi.next();
				String xss = xs.substring(adjustId(id).length());

				if (xss.endsWith("/"))
				{
				    // we need to show show the trailing /, but escapeUrl blows if it's there
				        xss = xss.substring(0, xss.length()-1);
					if (doProtected
							&& xs.toLowerCase().indexOf("/protected") >= 0)
					{
						if (!contentHostingService.allowAddCollection(adjustId(xs)))
						{
							continue;
						}
					}
					// note that we put back the trailing /
					out.println("<tr><td><a href=\"" + Validator.escapeUrl(xss) + "/\">" + Validator.escapeHtml(xss)
							+ "</a></td><td><b>" + rb.getString("folder") + "</b>" + "</td><td>" + "</td><td>" + "</td><td>" + "</td></tr>");
				}
				else
					try
					{
						ContentResource nextres = contentHostingService.getResource(adjustId(xs));
						ResourceProperties properties = nextres.getProperties();

						long filesize = ((nextres.getContentLength() - 1) / 1024) + 1;
						String createdBy = getUserPropertyDisplayName(properties, ResourceProperties.PROP_CREATOR);
						Time modTime = properties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
						String modifiedTime = modTime.toStringLocalShortDate() + " " + modTime.toStringLocalShort();
						String filetype = nextres.getContentType();
						out.println("<tr><td><a href=\"" + Validator.escapeUrl(xss) + "\">" + Validator.escapeHtml(xss)
								+ "</a></td><td>" + filesize + "</td><td>" + createdBy + "</td><td>" + filetype + "</td><td>"
								+ modifiedTime + "</td></tr>");
					}
					catch (Throwable ignore)
					{
						out.println("<tr><td><a href=\"" + Validator.escapeUrl(xss) + "\">" + Validator.escapeHtml(xss)
								+ "</a></td><td>" + "</td><td>" + "</td><td>" + "</td><td>" + "</td></tr>");

					}
			}
		}
		catch (Throwable ignore)
		{
		}
		if (out != null) out.println("</table></div></body></html>");
	}

	/**
	 * Handle requests for content, resources ONLY
	 * 
	 * @param id
	 *        The local resource id.
	 * @param res
	 *        The http servlet response object.
	 * @return any error message, or null if all went well.
	 */
	private String doContent(String id, HttpServletRequest req, HttpServletResponse res) throws IOException
	{
	        if (prohibited(id))
		{
	        	res.sendError(HttpServletResponse.SC_FORBIDDEN);
	        	return rb.getString("permission_to_view");
	        }

		// resource or collection? check the properties (also finds bad id and checks permissions)
		boolean isCollection = false;
		try
		{
		    ResourceProperties props = null;
		    try {
			props = contentHostingService.getProperties(adjustId(id));
		    } catch (IdUnusedException x) {
			if (!id.endsWith(Entity.SEPARATOR)) {
			    String tempid = id + Entity.SEPARATOR;
			    props = contentHostingService.getProperties(adjustId(tempid));
			    id = tempid;
			} else {
			    res.sendError(HttpServletResponse.SC_NOT_FOUND);
			    return rb.getString("resource_not_exists");
			}
		    }

		    isCollection = props.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
		}
		catch (PermissionException e)
		{
			res.sendError(HttpServletResponse.SC_FORBIDDEN);
        	return rb.getString("permission_to_view");
		}
		catch (IdUnusedException e)
		{
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
		    return rb.getString("resource_not_exists");
		}
		catch (EntityPropertyNotDefinedException e)
		{
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
		    return rb.getString("resource_not_exists");
		}
		catch (EntityPropertyTypeException e)
		{
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
		    return rb.getString("resource_not_exists");
		}

		// for resources
		if (!isCollection)
		{
			if (log.isDebugEnabled()) log.debug("SAKAIAccess doContent is resource " + id);

			InputStream contentStream = null;
			OutputStream out = null;
			
			try
			{
				ContentResource resource = contentHostingService.getResource(adjustId(id));
				long len = resource.getContentLength();
				String contentType = resource.getContentType();

				// for URL content type, encode a redirect to the body URL
				if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL))
				{
					res.sendRedirect(new String(resource.getContent()));
				}
				else
				{
					// Similar to handleAccessResource() in BaseContentService.java

					req.getSession().setAttribute("resourceType", resource.getResourceType());

					contentStream = resource.streamContent();

					if (contentStream == null || len == 0)
					{
						return rb.getString("empty_resource");
					}

					// set the buffer of the response to match what we are reading from the request
					if (len < STREAM_BUFFER_SIZE)
					{
						res.setBufferSize( (int) len);
					}
					else
					{
						res.setBufferSize(STREAM_BUFFER_SIZE);
					}

					if (!processHead(req, res)) return rb.getString("error_setting_header_values");

					out = res.getOutputStream();
					
					// chunk content stream to response
					byte[] chunk = new byte[STREAM_BUFFER_SIZE];
					int lenRead;
					while ((lenRead = contentStream.read(chunk)) != -1)
					{
						out.write(chunk, 0, lenRead);
					}
				}
			}
			catch (Throwable e)
			{
				// log.warn(this + ".doContent(): exception: id: " + id + " : " + e.toString());
				return e.toString();
			}
			finally
			{
				if (contentStream != null) {
					try {
						contentStream.close();
					} catch (IOException e) {
						// ignore
					}
				}

				if (out != null)
				{
					try
					{
						out.close();
					}
					catch (Throwable ignore)
					{
						// ignore
					}
				}
			}
		}

		// for collections
		else
		{
			doDirectory(id, req, res);
		}

		// no errors
		return null;

	} // doContent

	// Sometimes we are the root applet and other times, we are a sub-applet
	// We have to trim off the part of the path which gets to us
	// Also we have to deal with the fact that SAKAI likes collections with trailing slashes
	// while http like collections without trailing slashes
	// So, we take a quick look to see if the directory does not exist without the slash
	// and if it does not exist without the slash, we peek to see if it exists with the slash.
	// if so, we tack the slash on.

	public String getRelativePathSAKAI(HttpServletRequest req)
	{
		String path = req.getPathInfo();
		
		if (path == null) path = "/";
		if (log.isDebugEnabled()) log.debug("getRelativePathSAKAI = " + path);
		return path;

	} // getRelativePathSAKAI

	/**
	 * getResourceNameSAKAI - Needs to become a method of resource returns the internal name for a resource.
	 */

	public String getResourceNameSAKAI(Entity mbr)
	{
		String idx = mbr.getId();
		ResourceProperties props = mbr.getProperties();
		String resourceName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

		if (idx.startsWith("/") && idx.endsWith("/") && idx.length() > 3)
		{
			int lastSlash = idx.lastIndexOf("/", idx.length() - 2);
			if (lastSlash > 0 && lastSlash + 1 <= idx.length() - 2)
			{
				// if (log.isDebugEnabled()) log.debug("ls1="+(lastSlash+1)+" idl="+(idx.length()-1));
				resourceName = idx.substring(lastSlash + 1, idx.length() - 1);
			}

		}
		else if (idx.startsWith("/") && !idx.endsWith("/") && idx.length() > 2)
		{
			int lastSlash = idx.lastIndexOf("/");
			if (lastSlash > -1)
			{
				// if (log.isDebugEnabled()) log.debug("ls="+lastSlash);
				resourceName = idx.substring(lastSlash + 1);
			}
		}

		String parts[] = StringUtil.split(idx, Entity.SEPARATOR);
		if(parts.length == 4 && parts[1].equals("group-user")){
			try
			{
				// try using it as an ID
				resourceName = UserDirectoryService.getUserEid(parts[3]);
			}
			catch (UserNotDefinedException tryId)
			{
				try
				{
					// if successful, the context is already a valid user EID
					UserDirectoryService.getUserByEid(parts[3]);
				}
				catch (UserNotDefinedException notId)
				{
					// if context was not a valid ID, leave it alone
					log.warn("getResourceNameSAKAI could not find either id or eid: " + parts[3]);
				}
			}
		 }
	 
		return resourceName;
	}

	/**
	 * PROPFIND Method.
	 */
	protected void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		String path = getRelativePathSAKAI(req);

		if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

		if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF")) || prohibited(path))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		// Properties which are to be displayed.
		Vector<String> properties = null;
		// Propfind depth
		int depth = INFINITY;
		// Propfind type
		int type = FIND_ALL_PROP;

		String depthStr = req.getHeader("Depth");

		if (depthStr == null)
		{
			depth = INFINITY;
		}
		else
		{
			if (depthStr.equals("0"))
			{
				depth = 0;
			}
			else if (depthStr.equals("1"))
			{
				depth = 1;
			}
			else if (depthStr.equals("infinity"))
			{
				depth = INFINITY;
			}
		}

		Node propNode = null;

		DocumentBuilder documentBuilder = getDocumentBuilder();

		// be careful how we get content, as we've had hangs in mod_jk
		// Rather than passing the XML parser a stream on the network
		// input, we read it into a buffer and pass them a stream
		// on the buffer. This is an experiment to see if it fixes
		// the hangs.

		// Note that getContentLength can return -1. As everyone seems
		// to use the content-length header, ignore that case for now
		// It is strongly discouraged by the spec.

		int contentLength = req.getContentLength();
		
		if (contentLength > MAX_XML_STREAM_LENGTH)
		{
			resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
			return;				
		}
		else if (contentLength > 0)
		{

			byte[] byteContent = new byte[contentLength];
			InputStream inputStream = req.getInputStream();

			int lenRead = 0;

			try
			{
				while (lenRead < contentLength)
				{
					int read = inputStream.read(byteContent, lenRead, contentLength - lenRead);
					if (read <= 0) break;
					lenRead += read;
				}
			}
			catch (Exception ignore)
			{
			}
			// if anything goes wrong, we treat it as find all props

			// Parse the input XML to see what they really want
			if (lenRead > 0) try
			{
				InputStream is = new ByteArrayInputStream(byteContent, 0, lenRead);
				log.debug("have bytes");
				Document document = documentBuilder.parse(new InputSource(is));

				// Get the root element of the document
				Element rootElement = document.getDocumentElement();
				NodeList childList = rootElement.getChildNodes();
				log.debug("have nodes {}", childList.getLength());

				for (int i = 0; i < childList.getLength(); i++)
				{
					Node currentNode = childList.item(i);
					log.debug("looking at node {}", currentNode.getNodeName());
					switch (currentNode.getNodeType())
					{
						case Node.TEXT_NODE:
							break;
						case Node.ELEMENT_NODE:
							if (currentNode.getNodeName().endsWith("prop"))
							{
								type = FIND_BY_PROPERTY;
								propNode = currentNode;
							}
							if (currentNode.getNodeName().endsWith("propname"))
							{
								type = FIND_PROPERTY_NAMES;
							}
							if (currentNode.getNodeName().endsWith("allprop"))
							{
								type = FIND_ALL_PROP;
							}
							break;
					}
				}
			}
			catch (SAXParseException se) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;				
			}
			catch (Exception e)
			{
				log.warn("Exception parsing DAV request", e);
			}
			// again, in case of exception, we'll have the default
			// FIND_ALL_PROP
		}

		log.debug("Find type {}", type);

		if (type == FIND_BY_PROPERTY)
		{
			properties = new Vector<String>();
			NodeList childList = propNode.getChildNodes();

			for (int i = 0; i < childList.getLength(); i++)
			{
				Node currentNode = childList.item(i);
				switch (currentNode.getNodeType())
				{
					case Node.TEXT_NODE:
						break;
					case Node.ELEMENT_NODE:
						String nodeName = currentNode.getNodeName();
						String propertyName = null;
						if (nodeName.indexOf(':') != -1)
						{
							propertyName = nodeName.substring(nodeName.indexOf(':') + 1);
						}
						else
						{
							propertyName = nodeName;
						}
						// href is a live property which is handled differently
						properties.addElement(propertyName);
						break;
				}
			}

		}

		// Retrieve the resources
		DirContextSAKAI resources = getResourcesSAKAI();

		if (resources == null)
		{
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		// Point the resource object at a particular path and catch the error if necessary.

		boolean exists = true;
		try
		{
			resources.lookup(path);
		}
		catch (NamingException e)
		{
			exists = false;
			int slash = path.lastIndexOf('/');
			if (slash != -1)
			{
				String parentPath = path.substring(0, slash);
				Vector<String> currentLockNullResources = lockNullResources.get(parentPath);
				if (currentLockNullResources != null)
				{
					Enumeration<String> lockNullResourcesList = currentLockNullResources.elements();
					while (lockNullResourcesList.hasMoreElements())
					{
						String lockNullPath = lockNullResourcesList.nextElement();
						if (lockNullPath.equals(path))
						{
							resp.setStatus(SakaidavStatus.SC_MULTI_STATUS);
							resp.setContentType("text/xml; charset=UTF-8");
							// Create multistatus object
							XMLWriter generatedXML = new XMLWriter(resp.getWriter());
							generatedXML.writeXMLHeader();
							generatedXML.writeElement("D", "multistatus" + generateNamespaceDeclarations(), XMLWriter.OPENING);
							parseLockNullProperties(req, generatedXML, lockNullPath, type, properties);
							generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);
							generatedXML.sendData();
							return;
						}
					}
				}
			}
		}

		if (!exists)
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "/dav" + path);
			return;
		}

		resp.setStatus(SakaidavStatus.SC_MULTI_STATUS);

		resp.setContentType("text/xml; charset=UTF-8");

		// Create multistatus object
		XMLWriter generatedXML = new XMLWriter(resp.getWriter());
		generatedXML.writeXMLHeader();

		generatedXML.writeElement("D", "multistatus" + generateNamespaceDeclarations(), XMLWriter.OPENING);

		if (depth == 0)
		{
			parseProperties(req, resources, generatedXML, path, type, properties);
		}
		else
		{
			// The stack always contains the object of the current level
			Stack<String> stack = new Stack<String>();
			stack.push(path);

			// Stack of the objects one level below
			Stack<String> stackBelow = new Stack<String>();

			while ((!stack.isEmpty()) && (depth >= 0))
			{

				String currentPath = (String) stack.pop();

				try
				{
					// if (log.isDebugEnabled()) log.debug("Lookup currentPath="+currentPath);
					resources.lookup(currentPath);
				}
				catch (NamingException e)
				{
					continue;
				}

				parseProperties(req, resources, generatedXML, currentPath, type, properties);

				if ((resources.isCollection) && (depth > 0))
				{

					Iterator<ContentEntity> it = resources.list(currentPath);
					while (it.hasNext())
					{
						Entity mbr = it.next();
						String resourceName = getResourceNameSAKAI(mbr);

						String newPath = currentPath;
						if (!(newPath.endsWith("/"))) newPath += "/";
						newPath += resourceName;
						if (!(newPath.toLowerCase().indexOf("/protected") >= 0 && !contentHostingService.allowAddCollection(newPath)))
						stackBelow.push(newPath);
						// if (log.isDebugEnabled()) log.debug("SAKAI found resource " + newPath);
					}

					// Displaying the lock-null resources present in that
					// collection
					String lockPath = currentPath;
					if (lockPath.endsWith("/")) lockPath = lockPath.substring(0, lockPath.length() - 1);
					Vector<String> currentLockNullResources = lockNullResources.get(lockPath);
					if (currentLockNullResources != null)
					{
						Enumeration<String> lockNullResourcesList = currentLockNullResources.elements();
						while (lockNullResourcesList.hasMoreElements())
						{
							String lockNullPath = (String) lockNullResourcesList.nextElement();

							parseLockNullProperties(req, generatedXML, lockNullPath, type, properties);
						}
					}

				}

				if (stack.isEmpty())
				{
					depth--;
					stack = stackBelow;

					stackBelow = new Stack<String>();
				}
				// if (log.isDebugEnabled()) log.debug("SAKAIDAV.propfind() " + generatedXML.toString());
				generatedXML.sendData();
			}
		}

		generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);
		// if (log.isDebugEnabled()) log.debug("SAKAIDAV.propfind() at end:" + generatedXML.toString());
		generatedXML.sendData();

	}

	/**
	 * PROPPATCH Method.
	 */
	@SuppressWarnings("deprecation")
	protected void doProppatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		// Check that the resource is not locked
		if (isLocked(req)) {
			resp.sendError(SakaidavStatus.SC_LOCKED);
		}
		
	    // we can't actually do this, but MS requires us to. Say we did.
	    // I'm trying to be as close to valid here, so I generate an OK
	    // for all the properties they tried to set. This is really hairy because
	    // it gets into name spaces. But if we ever try to implement this for real,
	    // we'll have to do this. So might as well start now.
	    //    During testing I found by mistake that it's actually OK to send
	    // an empty multistatus return, so I don't actually  need all of this stuff.
	    //    The big problem is that the properties are typically not in the dav namespace
	    // we build a hash table of namespaces, with the prefix we're going to use
	    // since D: is used for dav, we start with E:, actually D+1

	    DocumentBuilder documentBuilder = null;
	    try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false); 
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false); 

		documentBuilder = factory.newDocumentBuilder();
	    } catch (Exception e) {
		resp.sendError(SakaidavStatus.SC_METHOD_FAILURE);
		return;
	    }

	    int contentLength = req.getContentLength();

	    // a list of the properties with the new prefix
	    List<String> props = new ArrayList<String>();
	    // hash of namespace, prefix
	    Hashtable<String,String> spaces = new Hashtable<String, String>();

	    // read the xml document
	    if (contentLength > MAX_XML_STREAM_LENGTH)
		{
			resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE );
			return;				
		}
		else if (contentLength > 0) {

		byte[] byteContent = new byte[contentLength];
		InputStream inputStream = req.getInputStream();

		int lenRead = 0;

		try {
		    while (lenRead < contentLength) {
			int read = inputStream.read(byteContent, lenRead, contentLength - lenRead);
			if (read <= 0) break;
			lenRead += read;
		    }
		}catch (Exception ignore) {}
		
		// Parse the input XML to see what they really want
		if (lenRead > 0) 
		    try {
			// if we got here, "is" is the xml document
			InputStream is = new ByteArrayInputStream(byteContent, 0, lenRead);
			Document document = documentBuilder.parse(new InputSource(is));

			// Get the root element of the document
			Element rootElement = document.getDocumentElement();
			// find all the property nodes
			NodeList childList = rootElement.getElementsByTagNameNS("DAV:", "prop");

			int nextChar = 1;

			for (int i = 0; i < childList.getLength(); i++) {

			    // this should be a prop node
			    Node currentNode = childList.item(i);
			    // this should be the actual property
			    NodeList names = currentNode.getChildNodes();
			    // this should be the name
			    for (int j = 0; j < names.getLength(); j++ ) {
				String namespace = names.item(j).getNamespaceURI();
				String prefix = spaces.get(namespace);
				// see if we know about this namespace. If not add it and
				// generate a prefix
				if (prefix == null) {
				    prefix = "" +  Character.toChars('D' + nextChar)[0];
				    spaces.put(namespace, prefix);
				}
				props.add(prefix + ":" + names.item(j).getLocalName());
			    }
			}
		    } catch (Exception ignore) {}
	    }


	    resp.setStatus(SakaidavStatus.SC_MULTI_STATUS);
	    resp.setContentType("text/xml; charset=UTF-8");

	    Writer writer = resp.getWriter();

	    writer.write("<D:multistatus xmlns:D=\"DAV:\"");
	    // dump all the name spaces and their prefix
	    for (String namespace: spaces.keySet())
		writer.write(" xmlns:" + spaces.get(namespace) + "=\"" + namespace + "\"");
	    writer.write("><D:response><D:href>" + javax.servlet.http.HttpUtils.getRequestURL(req) + "</D:href>");
	    // now output properties, claiming we did it
	    for (String pname: props) {
		writer.write("<D:propstat><D:prop><" + pname + "/></D:prop><D:status>HTTP/1.1 201 OK</D:status></D:propstat>");
	    }
	    writer.write("</D:response></D:multistatus>");
	    writer.close();

	}

	protected String justName(String str)
	{
		try
		{
			// Note: there may be a trailing separator
			int pos = str.lastIndexOf("/", str.length() - 2);
			String rv = str.substring(pos + 1);
			if (rv.endsWith("/"))
			{
				rv = rv.substring(0, rv.length() - 1);
			}
			return rv;
		}
		catch (Throwable t)
		{
			return str;
		}
	}

	/**
	 * Find the containing collection id of a given resource id. Copied from BaseContentService.
	 * 
	 * @param id
	 *        The resource id.
	 * @return the containing collection id.
	 */
	private String isolateContainingId(String id)
	{
		// take up to including the last resource path separator, not counting one at the very end if there
		return id.substring(0, id.lastIndexOf('/', id.length() - 2) + 1);

	} // isolateContainingId


	/**
	 * MKCOL Method.
	 */
	protected void doMkcol(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		// Is there a body in the response which we don't understand? If so, return error code
		// as per rfc2518 8.3.1
		
		if (req.getContentLength() > 0) {
			resp.sendError(SakaidavStatus.SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}
		
		if (readOnly)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		if (isLocked(req))
		{
			resp.sendError(SakaidavStatus.SC_LOCKED);
			return;
		}

		String path = getRelativePathSAKAI(req);
		if (prohibited(path) || (path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF")))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		String name = justName(path);

		if ((name.toUpperCase().startsWith("/WEB-INF")) || (name.toUpperCase().startsWith("/META-INF")))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		// Check to see if the parent collection exists. ContentHosting will create a parent folder if it 
		// does not exist, but the WebDAV spec requires this operation to fail (rfc2518, 8.3.1).
		
		String parentId = isolateContainingId(adjustId(path));
		
		try {
			contentHostingService.getCollection(parentId);
		} catch (IdUnusedException e1) {
			resp.sendError(SakaidavStatus.SC_CONFLICT);
			return;		
		} catch (TypeException e1) {
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;						
		} catch (PermissionException e1) {
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;			
		}
		
		String adjustedId = adjustId(path);
	
		// Check to see if collection with this name already exists
		try
		{	
			contentHostingService.getProperties(adjustedId);
			
			// return error (litmus: MKCOL on existing collection should fail, RFC2518:8.3.1 / 8.3.2)
			
			resp.sendError(SakaidavStatus.SC_METHOD_NOT_ALLOWED);
			return;

		}
		catch (IdUnusedException e)
		{
			// Resource not found (this is actually the normal case)
		}
		catch (PermissionException e)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		// Check to see if a resource with this name already exists
		if (adjustedId.endsWith("/") && adjustedId.length() > 1)
		try
		{
			String idToCheck = adjustedId.substring(0, adjustedId.length() - 1);
			
			contentHostingService.getProperties(idToCheck);
			
			// don't allow overwriting an existing resource (litmus: mkcol_over_plain)

			resp.sendError(SakaidavStatus.SC_METHOD_NOT_ALLOWED);
			return;

		}
		catch (IdUnusedException e)
		{
			// Resource not found (this is actually the normal case)
		}
		catch (PermissionException e)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}
		
		// Add the collection

		try
		{
			ContentCollectionEdit edit = contentHostingService.addCollection(adjustId(path));
			ResourcePropertiesEdit resourceProperties = edit.getPropertiesEdit();
			resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
			contentHostingService.commitCollection(edit);
		}

		catch (IdUsedException e)
		{
			// Should not happen because if this esists, we either return or delete above
		}
		catch (IdInvalidException e)
		{
			log.warn("SAKAIDavServlet.doMkcol() IdInvalid:" + e.getMessage());
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}
		catch (PermissionException e)
		{
			// This is normal
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}
		catch (InconsistentException e)
		{
			log.warn("SAKAIDavServlet.doMkcol() InconsistentException:" + e.getMessage());
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		resp.setStatus(HttpServletResponse.SC_CREATED);
		// Removing any lock-null resource which would be present
		lockNullResources.remove(path);

	}

	/**
	 * DELETE Method.
	 */
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		if (readOnly)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		if (isLocked(req))
		{
			resp.sendError(SakaidavStatus.SC_LOCKED);
			return;
		}

		deleteResource(req, resp);

	}

	/**
	 * Process a PUT request for the specified resource.
	 * 
	 * @param request
	 *        The servlet request we are processing
	 * @param response
	 *        The servlet response we are creating
	 * @exception IOException
	 *            if an input/output error occurs
	 * @exception ServletException
	 *            if a servlet-specified error occurs
	 */
	@SuppressWarnings("unchecked")
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		// Do not allow files which match patterns specified in properties
		if (!isFileNameAllowed(req))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		if (isLocked(req))
		{
			resp.sendError(SakaidavStatus.SC_LOCKED);
			return;
		}

		String path = getRelativePathSAKAI(req);

		if (prohibited(path) || (path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF")))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		// Looking for a Content-Range header
		if (req.getHeader("Content-Range") != null)
		{
			// No content range header is supported
			resp.sendError(SakaidavStatus.SC_NOT_IMPLEMENTED);
		}

		String name = justName(path);

		// Database max for id field is 255. If we allow longer, odd things happen
		if (path.length() > 254)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		if ((name.toUpperCase().startsWith("/WEB-INF")) || (name.toUpperCase().startsWith("/META-INF")))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		// Don't delete the resource and add it again

		// Update the resource

		String contentType = "";
		InputStream inputStream = req.getInputStream();
		contentType = req.getContentType();

		// For MS office, ignore the supplied content type if we can figure out one from file type
		// they send text/xml for doc and docx files
		if (contentType != null) {
		    UsageSession session = UsageSessionService.getSession();
		    String agent = null;
		    if (session != null)
			agent = session.getUserAgent();
		    if (agent != null && agent.startsWith("Microsoft Office Core Storage Infrastructure")) {
			String fileContentType = getServletContext().getMimeType(path);
			if (fileContentType != null) {
			    contentType = fileContentType;
			}
		    }
		}

		if (log.isDebugEnabled()) log.debug("  req.contentType() =" + contentType);

		if (contentType == null)
		{
			contentType = getServletContext().getMimeType(path);
			if (log.isDebugEnabled()) log.debug("Lookup contentType =" + contentType);
		}
		if (contentType == null)
		{
			if (log.isDebugEnabled()) log.debug("Unable to determine contentType");
			contentType = ""; // Still cannot figure it out
		}

		try
		{

			ContentResourceEdit edit;

			boolean newfile = false;
			String resourcePath = adjustId(path);

			// Since editResource doesn't throw IdUnusedException correctly, try first with getResource
			try
			{
				contentHostingService.getResource(resourcePath);
			}
			catch (IdUnusedException e)
			{
				newfile = true;
			}

			if (newfile)
			{
				edit = contentHostingService.addResource(resourcePath);

				String resourceType = (String) req.getSession().getAttribute("resourceType");
				if ("org.sakaiproject.citation.impl.CitationList".equalsIgnoreCase(resourceType))
				{
					edit.setResourceType(resourceType);
					edit.getProperties().addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, "/citation");
				}
				final ResourcePropertiesEdit p = edit.getPropertiesEdit();
				p.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

				User user = UserDirectoryService.getCurrentUser();
				final TimeBreakdown timeBreakdown = TimeService.newTime().breakdownLocal();
				p.addProperty(ResourceProperties.PROP_COPYRIGHT, "copyright (c)" + " " + timeBreakdown.getYear() + ", " + user.getDisplayName() + ". All Rights Reserved. ");
			}
			else
			{
				edit = contentHostingService.editResource(resourcePath);
				contentType = edit.getContentType();
			}

			if (contentHostingService.isAvailabilityEnabled()) {
				boolean hidden = ServerConfigurationService.getBoolean("content.dav.upload.hidden", false);
				edit.setAvailability(hidden, null, null);
			}
			edit.setContentType(contentType);
			edit.setContent(inputStream);

			// commit the change
			contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);

			if ("org.sakaiproject.citation.impl.CitationList".equalsIgnoreCase(edit.getResourceType()))
			{
				Reference reference = entityManager.newReference(edit.getReference());
				citationService.copyCitationCollection(reference);
			}


		}
		catch (IdUsedException e)
		{
			// Should not happen because we deleted above (unless two requests at same time)
			log.warn("SAKAIDavServlet.doPut() IdUsedException:" + e.getMessage());

			resp.sendError(HttpServletResponse.SC_CONFLICT);
			return;
		}
		catch (IdInvalidException e)
		{
			log.warn("SAKAIDavServlet.doPut() IdInvalidException:" + e.getMessage());
			resp.sendError(HttpServletResponse.SC_CONFLICT);
			return;
		}
		catch (PermissionException e)
		{
			// Normal
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}
		catch (OverQuotaException e)
		{
			// Normal %%% what's the proper response for over-quota?
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}
		catch (InconsistentException e)
		{
			log.warn("SAKAIDavServlet.doPut() InconsistentException:" + e.getMessage());
			resp.sendError(HttpServletResponse.SC_CONFLICT);
			return;
		}
		catch (ServerOverloadException e)
		{
			log.warn("SAKAIDavServlet.doPut() ServerOverloadException:" + e.getMessage());
			resp.setStatus(SakaidavStatus.SC_SERVICE_UNAVAILABLE);
			return;
		} catch (InUseException e) {
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		} catch (TypeException e) {
			log.warn("SAKAIDavServlet.doPut() TypeException:" + e.getMessage());
			resp.sendError(HttpServletResponse.SC_CONFLICT);
			return;
		} catch (IdUnusedException inconsistent) {
			log.error("SAKAIDavServlet.doPut() Inconsistently got IdUnusedException after checking resource exists: " + inconsistent.getMessage());
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		resp.setStatus(HttpServletResponse.SC_CREATED);

		// Removing any lock-null resource which would be present
		lockNullResources.remove(path);

	}

	/**
	 * COPY Method.
	 */
	protected void doCopy(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		log.debug("doCopy called");

		if (readOnly)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		copyResource(req, resp, false);

	}

	/**
	 * MOVE Method.
	 */
	protected void doMove(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		if (readOnly)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		if (isLocked(req))
		{
			resp.sendError(SakaidavStatus.SC_LOCKED);
			return;
		}

		String path = getRelativePath(req);

		boolean success = copyResource(req, resp, true);
		// https://jira.sakaiproject.org/browse/SAK-23639
		// Remove this call to deleteResource if rename() is fixed
		if (success) {
		    deleteResource(path, req, resp);
		}

	}

	/**
	 * LOCK Method.
	 */
	protected void doLock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		if (readOnly)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		if (isLocked(req))
		{
			resp.sendError(SakaidavStatus.SC_LOCKED);
			return;
		}

		LockInfo lock = new LockInfo();

		// Parsing lock request

		// Parsing depth header

		String depthStr = req.getHeader("Depth");

		if (depthStr == null)
		{
			lock.depth = INFINITY;
		}
		else
		{
			if (depthStr.equals("0"))
			{
				lock.depth = 0;
			}
			else
			{
				lock.depth = INFINITY;
			}
		}

		// Parsing timeout header

		int lockDuration = DEFAULT_TIMEOUT;
		String lockDurationStr = req.getHeader("Timeout");
		if (lockDurationStr == null)
		{
			lockDuration = DEFAULT_TIMEOUT;
		}
		else
		{
			if (lockDurationStr.startsWith("Second-"))
			{
				lockDuration = (new Integer(lockDurationStr.substring(7))).intValue();
			}
			else
			{
				if (lockDurationStr.equalsIgnoreCase("infinity"))
				{
					lockDuration = MAX_TIMEOUT;
				}
				else
				{
					try
					{
						lockDuration = (new Integer(lockDurationStr)).intValue();
					}
					catch (NumberFormatException e)
					{
						lockDuration = MAX_TIMEOUT;
					}
				}
			}
			if (lockDuration == 0)
			{
				lockDuration = DEFAULT_TIMEOUT;
			}
			if (lockDuration > MAX_TIMEOUT)
			{
				lockDuration = MAX_TIMEOUT;
			}
		}
		lock.expiresAt = System.currentTimeMillis() + (lockDuration * 1000);

		int lockRequestType = LOCK_CREATION;

		Node lockInfoNode = null;

		DocumentBuilder documentBuilder = getDocumentBuilder();

		try
		{
			Document document = documentBuilder.parse(new InputSource(req.getInputStream()));

			// Get the root element of the document
			Element rootElement = document.getDocumentElement();
			lockInfoNode = rootElement;
		}
		catch (Exception e)
		{
			lockRequestType = LOCK_REFRESH;
		}

		if (lockInfoNode != null)
		{

			// Reading lock information

			NodeList childList = lockInfoNode.getChildNodes();
			StringWriter strWriter = null;
			DOMWriter domWriter = null;

			Node lockScopeNode = null;
			Node lockTypeNode = null;
			Node lockOwnerNode = null;

			for (int i = 0; i < childList.getLength(); i++)
			{
				Node currentNode = childList.item(i);
				switch (currentNode.getNodeType())
				{
					case Node.TEXT_NODE:
						break;
					case Node.ELEMENT_NODE:
						String nodeName = currentNode.getNodeName();
						if (nodeName.endsWith("lockscope"))
						{
							lockScopeNode = currentNode;
						}
						if (nodeName.endsWith("locktype"))
						{
							lockTypeNode = currentNode;
						}
						if (nodeName.endsWith("owner"))
						{
							lockOwnerNode = currentNode;
						}
						break;
				}
			}

			if (lockScopeNode != null)
			{

				childList = lockScopeNode.getChildNodes();
				for (int i = 0; i < childList.getLength(); i++)
				{
					Node currentNode = childList.item(i);
					switch (currentNode.getNodeType())
					{
						case Node.TEXT_NODE:
							break;
						case Node.ELEMENT_NODE:
							String tempScope = currentNode.getNodeName();
							if (tempScope.indexOf(':') != -1)
							{
								lock.scope = tempScope.substring(tempScope.indexOf(':') + 1);
							}
							else
							{
								lock.scope = tempScope;
							}
							break;
					}
				}

				if (lock.scope == null)
				{
					// Bad request
					resp.setStatus(SakaidavStatus.SC_BAD_REQUEST);
				}

			}
			else
			{
				// Bad request
				resp.setStatus(SakaidavStatus.SC_BAD_REQUEST);
			}

			if (lockTypeNode != null)
			{

				childList = lockTypeNode.getChildNodes();
				for (int i = 0; i < childList.getLength(); i++)
				{
					Node currentNode = childList.item(i);
					switch (currentNode.getNodeType())
					{
						case Node.TEXT_NODE:
							break;
						case Node.ELEMENT_NODE:
							String tempType = currentNode.getNodeName();
							if (tempType.indexOf(':') != -1)
							{
								lock.type = tempType.substring(tempType.indexOf(':') + 1);
							}
							else
							{
								lock.type = tempType;
							}
							break;
					}
				}

				if (lock.type == null)
				{
					// Bad request
					resp.setStatus(SakaidavStatus.SC_BAD_REQUEST);
				}

			}
			else
			{
				// Bad request
				resp.setStatus(SakaidavStatus.SC_BAD_REQUEST);
			}

			if (lockOwnerNode != null)
			{

				childList = lockOwnerNode.getChildNodes();
				for (int i = 0; i < childList.getLength(); i++)
				{
					Node currentNode = childList.item(i);
					switch (currentNode.getNodeType())
					{
						case Node.TEXT_NODE:
							lock.owner += currentNode.getNodeValue();
							break;
						case Node.ELEMENT_NODE:
							strWriter = new StringWriter();
							domWriter = new DOMWriter(strWriter, true);
							domWriter.print(currentNode);
							lock.owner += strWriter.toString();
							break;
					}
				}

				if (lock.owner == null)
				{
					// Bad request
					resp.setStatus(SakaidavStatus.SC_BAD_REQUEST);
				}

				// contribute feeds us an owner that looks
				// like <A:href>...</A:href>. Since we'll put it
				// back with a different namespace prefix, we
				// don't want to save it that way.

				lock.owner = lock.owner.replaceAll("<(/?)[^>]+:([hH][rR][eE][fF])>", "<$1$2>");

			}
			else
			{
				lock.owner = new String();
			}

		}

		String path = getRelativePath(req);
		String lockToken = null;

		lock.path = path;

		// Retrieve the resources
		// DirContext resources = getResources();
		DirContextSAKAI resources = getResourcesSAKAI();

		if (resources == null)
		{
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		boolean exists = true;
		Object object = null;
		try
		{
			object = resources.lookup(path);
		}
		catch (NamingException e)
		{
			exists = false;
		}

		// We don't want to allow just anyone to lock a resource.
		// It seems reasonable to allow it only for someone who
		// is allowed to modify it.
		if (prohibited(path) || 
		    !(exists ? contentHostingService.allowUpdateResource(adjustId(path)) :
		      contentHostingService.allowAddResource(adjustId(path))))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN, path);
			return;
		}


		Enumeration<LockInfo> locksList = null;

		if (lockRequestType == LOCK_CREATION)
		{

			// Generating lock id
			String lockTokenStr = req.getServletPath() + "-" + lock.type + "-" + lock.scope + "-" + req.getUserPrincipal() + "-"
					+ lock.depth + "-" + lock.owner + "-" + lock.tokens + "-" + lock.expiresAt + "-" + System.currentTimeMillis()
					+ "-" + secret;
			lockToken = MD5Encoder.encode(md5Helper.digest(lockTokenStr.getBytes()));

			if ((exists) && (object instanceof DirContext) && (lock.depth == INFINITY))
			{

				// Locking a collection (and all its member resources)

				// Checking if a child resource of this collection is
				// already locked
				Vector<String> lockPaths = new Vector<String>();
				locksList = collectionLocks.elements();
				while (locksList.hasMoreElements())
				{
					LockInfo currentLock = (LockInfo) locksList.nextElement();
					if (currentLock.hasExpired())
					{
						resourceLocks.remove(currentLock.path);
						continue;
					}
					if ((currentLock.path.startsWith(lock.path)) && ((currentLock.isExclusive()) || (lock.isExclusive())))
					{
						// A child collection of this collection is locked
						lockPaths.addElement(currentLock.path);
					}
				}
				locksList = resourceLocks.elements();
				while (locksList.hasMoreElements())
				{
					LockInfo currentLock = (LockInfo) locksList.nextElement();
					if (currentLock.hasExpired())
					{
						resourceLocks.remove(currentLock.path);
						continue;
					}
					if ((currentLock.path.startsWith(lock.path)) && ((currentLock.isExclusive()) || (lock.isExclusive())))
					{
						// A child resource of this collection is locked
						lockPaths.addElement(currentLock.path);
					}
				}

				if (!lockPaths.isEmpty())
				{

					// One of the child paths was locked
					// We generate a multistatus error report

					Enumeration<String> lockPathsList = lockPaths.elements();

					resp.setStatus(SakaidavStatus.SC_CONFLICT);

					XMLWriter generatedXML = new XMLWriter();
					generatedXML.writeXMLHeader();

					generatedXML.writeElement("D", "multistatus" + generateNamespaceDeclarations(), XMLWriter.OPENING);

					while (lockPathsList.hasMoreElements())
					{
						generatedXML.writeElement("D", "response", XMLWriter.OPENING);
						generatedXML.writeElement("D", "href", XMLWriter.OPENING);
						generatedXML.writeText((String) lockPathsList.nextElement());
						generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
						generatedXML.writeElement("D", "status", XMLWriter.OPENING);
						generatedXML.writeText("HTTP/1.1 " + SakaidavStatus.SC_LOCKED + " "
								+ SakaidavStatus.getStatusText(SakaidavStatus.SC_LOCKED));
						generatedXML.writeElement("D", "status", XMLWriter.CLOSING);

						generatedXML.writeElement("D", "response", XMLWriter.CLOSING);
					}

					generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

					Writer writer = resp.getWriter();
					writer.write(generatedXML.toString());
					writer.close();

					return;

				}

				boolean addLock = true;

				// Checking if there is already a shared lock on this path
				locksList = collectionLocks.elements();
				while (locksList.hasMoreElements())
				{

					LockInfo currentLock = (LockInfo) locksList.nextElement();
					if (currentLock.path.equals(lock.path))
					{

						if (currentLock.isExclusive())
						{
							resp.sendError(SakaidavStatus.SC_LOCKED);
							return;
						}
						else
						{
							if (lock.isExclusive())
							{
								resp.sendError(SakaidavStatus.SC_LOCKED);
								return;
							}
						}

						currentLock.tokens.addElement(lockToken);
						lock = currentLock;
						addLock = false;

					}

				}

				if (addLock)
				{
					lock.tokens.addElement(lockToken);
					collectionLocks.addElement(lock);
				}

			}
			else
			{

				// Locking a single resource

				// Retrieving an already existing lock on that resource
				LockInfo presentLock = (LockInfo) resourceLocks.get(lock.path);
				if (presentLock != null)
				{

					if ((presentLock.isExclusive()) || (lock.isExclusive()))
					{
						// If either lock is exclusive, the lock can't be
						// granted
						resp.sendError(SakaidavStatus.SC_PRECONDITION_FAILED);
						return;
					}
					else
					{
						presentLock.tokens.addElement(lockToken);
						lock = presentLock;
					}

				}
				else
				{

					lock.tokens.addElement(lockToken);
					resourceLocks.put(lock.path, lock);

					// Checking if a resource exists at this path
					exists = true;
					try
					{
						object = resources.lookup(path);
					}
					catch (NamingException e)
					{
						exists = false;
					}
					if (!exists)
					{

						// "Creating" a lock-null resource
						int slash = lock.path.lastIndexOf('/');
						String parentPath = lock.path.substring(0, slash);

						Vector<String> lockNulls = lockNullResources.get(parentPath);
						if (lockNulls == null)
						{
							lockNulls = new Vector<String>();
							lockNullResources.put(parentPath, lockNulls);
						}

						lockNulls.addElement(lock.path);

					}

				}

			}

		}

		if (lockRequestType == LOCK_REFRESH)
		{

			String ifHeader = req.getHeader("If");
			if (ifHeader == null) ifHeader = "";

			// Checking resource locks

			LockInfo toRenew = (LockInfo) resourceLocks.get(path);
			Enumeration<String> tokenList = null;
			if ((lock != null) && (toRenew != null) && (toRenew.tokens != null))
			{
				// At least one of the tokens of the locks must have been given

				tokenList = toRenew.tokens.elements();
				while (tokenList.hasMoreElements())
				{
					String token = (String) tokenList.nextElement();
					if (ifHeader.indexOf(token) != -1)
					{
						toRenew.expiresAt = lock.expiresAt;
						lock = toRenew;
					}
				}
			}

			// Checking inheritable collection locks

			Enumeration<LockInfo> collectionLocksList = collectionLocks.elements();
			while (collectionLocksList.hasMoreElements())
			{
				toRenew = collectionLocksList.nextElement();
				if (path.equals(toRenew.path))
				{

					tokenList = toRenew.tokens.elements();
					while (tokenList.hasMoreElements())
					{
						String token = (String) tokenList.nextElement();
						if (ifHeader.indexOf(token) != -1)
						{
							toRenew.expiresAt = lock.expiresAt;
							lock = toRenew;
						}
					}

				}
			}

		}

		// Set the status, then generate the XML response containing
		// the lock information
		XMLWriter generatedXML = new XMLWriter();
		generatedXML.writeXMLHeader();
		generatedXML.writeElement("D", "prop" + generateNamespaceDeclarations(), XMLWriter.OPENING);

		generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);

		lock.toXML(generatedXML, true);

		generatedXML.writeElement("D", "lockdiscovery", XMLWriter.CLOSING);

		generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);

		/* the RFC requires this header in response to lock creation */

		if (lockRequestType == LOCK_CREATION) resp.addHeader("Lock-Token", "opaquelocktoken:" + lockToken);

		resp.setStatus(exists ? SakaidavStatus.SC_OK : SakaidavStatus.SC_CREATED);
		resp.setContentType("text/xml; charset=UTF-8");
		Writer writer = resp.getWriter();
		writer.write(generatedXML.toString());
		writer.close();

	}

	/**
	 * UNLOCK Method.
	 */
	protected void doUnlock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		if (readOnly)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		if (isLocked(req))
		{
			resp.sendError(SakaidavStatus.SC_LOCKED);
			return;
		}

		String path = getRelativePath(req);

		String lockTokenHeader = req.getHeader("Lock-Token");
		if (lockTokenHeader == null) lockTokenHeader = "";

		// Only allow lock/unlock for someone who can do update
		// NB: we don't check that the person unlocking is the same as
		// the one locking. Experience with Contribute says that
		// significant size groups have a big problem with people
		// leaving resources inadvertently locked. No one will use
		// the system if they continually have to find a privileged
		// user to unscramble things. Contribute does check who owns
		// the lock, so to bypass the lock you have to run a copy of
		// DAVExplorer and unlock it manually. That seems like a
		// good compromise. At any rate, there needs to be some
		// check here, which there wasn't originally.

		// when creating, we know whether the resource exists, so we
		// can check add or update appropriately. Here we don't, and
		// I think it's faster just to do both checks.  In fact
		// I believe allowAddResource doesn't currently check whether
		// the resource exists, so it would be safe to use alone, but
		// that seems like a bad assumption to make.
		if (prohibited(path) ||
		    !(contentHostingService.allowAddResource(adjustId(path)) ||
		      contentHostingService.allowUpdateResource(adjustId(path))))

		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return;
		}

		// Checking resource locks

		LockInfo lock = (LockInfo) resourceLocks.get(path);
		Enumeration<String> tokenList = null;
		if (lock != null)
		{

			// At least one of the tokens of the locks must have been given

			tokenList = lock.tokens.elements();
			while (tokenList.hasMoreElements())
			{
				String token = (String) tokenList.nextElement();
				if (lockTokenHeader.indexOf(token) != -1)
				{
					lock.tokens.removeElement(token);
				}
			}

			if (lock.tokens.isEmpty())
			{
				resourceLocks.remove(path);
				// Removing any lock-null resource which would be present
				lockNullResources.remove(path);
			}

		}

		// Checking inheritable collection locks

		Enumeration<LockInfo> collectionLocksList = collectionLocks.elements();
		while (collectionLocksList.hasMoreElements())
		{
			lock = (LockInfo) collectionLocksList.nextElement();
			if (path.equals(lock.path))
			{

				tokenList = lock.tokens.elements();
				while (tokenList.hasMoreElements())
				{
					String token = (String) tokenList.nextElement();
					if (lockTokenHeader.indexOf(token) != -1)
					{
						lock.tokens.removeElement(token);
						break;
					}
				}

				if (lock.tokens.isEmpty())
				{
					collectionLocks.removeElement(lock);
					// Removing any lock-null resource which would be present
					lockNullResources.remove(path);
				}

			}
		}

		resp.setStatus(SakaidavStatus.SC_NO_CONTENT);

	}

	// -------------------------------------------------------- Private Methods

	/**
	 * Generate the namespace declarations.
	 */
	private String generateNamespaceDeclarations()
	{
		return " xmlns:D=\"" + DEFAULT_NAMESPACE + "\"";
	}

	/**
	 * Check to see if a resource is currently write locked. The method will look at the "If" header to make sure the client has give the appropriate lock tokens.
	 * 
	 * @param req
	 *        Servlet request
	 * @return boolean true if the resource is locked (and no appropriate lock token has been found for at least one of the non-shared locks which are present on the resource).
	 */
	private boolean isLocked(HttpServletRequest req)
	{

		String path = getRelativePath(req);

		String ifHeader = req.getHeader("If");
		if (ifHeader == null) ifHeader = "";

		String lockTokenHeader = req.getHeader("Lock-Token");
		if (lockTokenHeader == null) lockTokenHeader = "";

		return isLocked(path, ifHeader + lockTokenHeader);

	}

	/**
	 * Check to see if a resource is currently write locked.
	 * 
	 * @param path
	 *        Path of the resource
	 * @param ifHeader
	 *        "If" HTTP header which was included in the request
	 * @return boolean true if the resource is locked (and no appropriate lock token has been found for at least one of the non-shared locks which are present on the resource).
	 */
	private boolean isLocked(String path, String ifHeader)
	{

		// Checking resource locks

		LockInfo lock = (LockInfo) resourceLocks.get(path);
		Enumeration<String> tokenList = null;
		if ((lock != null) && (lock.hasExpired()))
		{
			resourceLocks.remove(path);
		}
		else if (lock != null)
		{

			// At least one of the tokens of the locks must have been given

			tokenList = lock.tokens.elements();
			boolean tokenMatch = false;
			while (tokenList.hasMoreElements())
			{
				String token = (String) tokenList.nextElement();
				if (ifHeader.indexOf(token) != -1) tokenMatch = true;
			}
			if (!tokenMatch) return true;

		}

		// Checking inheritable collection locks

		Enumeration<LockInfo> collectionLocksList = collectionLocks.elements();
		while (collectionLocksList.hasMoreElements())
		{
			lock = collectionLocksList.nextElement();
			if (lock.hasExpired())
			{
				collectionLocks.removeElement(lock);
			}
			else if (path.startsWith(lock.path))
			{

				tokenList = lock.tokens.elements();
				boolean tokenMatch = false;
				while (tokenList.hasMoreElements())
				{
					String token = (String) tokenList.nextElement();
					if (ifHeader.indexOf(token) != -1) tokenMatch = true;
				}
				if (!tokenMatch) return true;

			}
		}

		return false;

	}

	/**
	 * Get the destination path from the header
	 */

	private String getDestinationPath(HttpServletRequest req)
	{
		// Parsing destination header

		String destinationPath = req.getHeader("Destination");

		if (destinationPath == null)
		{
			return null;
		}

		int protocolIndex = destinationPath.indexOf("://");
		if (protocolIndex >= 0)
		{
			// if the Destination URL contains the protocol, we can safely
			// trim everything upto the first "/" character after "://"
			int firstSeparator = destinationPath.indexOf("/", protocolIndex + 4);
			if (firstSeparator < 0)
			{
				destinationPath = "/";
			}
			else
			{
				destinationPath = destinationPath.substring(firstSeparator);
			}
		}
		else
		{
			String hostName = req.getServerName();
			if ((hostName != null) && (destinationPath.startsWith(hostName)))
			{
				destinationPath = destinationPath.substring(hostName.length());
			}

			int portIndex = destinationPath.indexOf(":");
			if (portIndex >= 0)
			{
				destinationPath = destinationPath.substring(portIndex);
			}

			if (destinationPath.startsWith(":"))
			{
				int firstSeparator = destinationPath.indexOf("/");
				if (firstSeparator < 0)
				{
					destinationPath = "/";
				}
				else
				{
					destinationPath = destinationPath.substring(firstSeparator);
				}
			}
		}

		String contextPath = req.getContextPath();
		if ((contextPath != null) && (destinationPath.startsWith(contextPath)))
		{
			destinationPath = destinationPath.substring(contextPath.length());
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo != null)
		{
			String servletPath = req.getServletPath();
			if ((servletPath != null) && (destinationPath.startsWith(servletPath)))
			{
				destinationPath = destinationPath.substring(servletPath.length());
			}
		}

		destinationPath = UDecoder.URLDecode(normalize(destinationPath), "UTF8");

		return destinationPath;

	} // getDestinationPath

    @SuppressWarnings("unchecked")
	private ResourcePropertiesEdit duplicateResourceProperties(ResourceProperties properties, String id) {

	ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
	try {

	    if (properties == null) return resourceProperties;

	    // loop throuh the properties
	    Iterator<String> propertyNames = properties.getPropertyNames();
	    while (propertyNames.hasNext()) {
		String propertyName = propertyNames.next();
		if (!propertyName.equals(ResourceProperties.PROP_DISPLAY_NAME))
		    resourceProperties.addProperty(propertyName, properties.getProperty(propertyName));
	    }

	} catch (Exception e) {
	    return resourceProperties;
	}

	return resourceProperties;


    } // duplicateResourceProperties


    // better than before, but rather than copyIntoFolder we really need to write our own recursive
    // code. There are two problems; (1) copyIntoFolder will add .bin when there is no extension (2) it
    // doesn't check for "/protected"  The current code is the minimum necessary to support OS X.

    @SuppressWarnings({ "deprecation", "unchecked" })
	private void copyCollection(String id, String new_id) 
	throws IdUnusedException, PermissionException, TypeException, IdUnusedException, IdLengthException, IdUsedException, IdUniquenessException, IdInvalidException, InUseException, InconsistentException, OverQuotaException, ServerOverloadException {

	if (!id.endsWith("/"))
	  id = id + "/";

	if (!new_id.endsWith("/"))
	  new_id = new_id + "/";


	ContentCollection thisCollection = contentHostingService.getCollection(id);

	List<String> members = thisCollection.getMembers();
	 
	ResourceProperties properties = thisCollection.getProperties();
	ResourcePropertiesEdit newProps = duplicateResourceProperties(properties, thisCollection.getId());

	String name = new_id;
	if (name.endsWith("/"))
	    name = name.substring(0, name.length()-1);
	int i = name.lastIndexOf("/");
	if (i >= 0)
	    name = name.substring(i+1);
	newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

	contentHostingService.addCollection(new_id, newProps);

	Iterator<String> memberIt = members.iterator();
	while (memberIt.hasNext()) {
	    String member_id = (String) memberIt.next();

	    // this isn't perfect. It only protects the top two levels of directory
	    if (!(doProtected && member_id.toLowerCase().indexOf("/protected") >= 0 &&
		  (!contentHostingService.allowAddCollection(adjustId(member_id)))))
		contentHostingService.copyIntoFolder(member_id, new_id);
	}

    }


	/**
	 * Copy a resource.
	 * 
	 * @param req
	 *        Servlet request
	 * @param resp
	 *        Servlet response
	 * @param move
	 *	  This is actually a move operation
	 * @return boolean true if the copy is successful
	 */
	 private boolean copyResource(HttpServletRequest req, HttpServletResponse resp, boolean move) throws ServletException, IOException
	{

		String destinationPath = getDestinationPath(req);

		if (destinationPath == null)
		{
			resp.sendError(SakaidavStatus.SC_BAD_REQUEST);
			return false;
		}

		if (log.isDebugEnabled()) log.debug("Dest path :" + destinationPath);

		if ((destinationPath.toUpperCase().startsWith("/WEB-INF")) || (destinationPath.toUpperCase().startsWith("/META-INF")))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return false;
		}

		String path = getRelativePath(req);

		if (prohibited(path) || (path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF")))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return false;
		}

		if (prohibited(destinationPath) || destinationPath.equals(path))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return false;
		}

		// Parsing overwrite header

		boolean overwrite = true;
		String overwriteHeader = req.getHeader("Overwrite");

		if (overwriteHeader != null)
		{
			if (overwriteHeader.equalsIgnoreCase("T"))
			{
				overwrite = true;
			}
			else
			{
				overwrite = false;
			}
		}

		// Overwriting the destination

		// Retrieve the resources
		// DirContext resources = getResources();
		DirContextSAKAI resources = getResourcesSAKAI();

		if (resources == null)
		{
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return false;
		}

		boolean exists = true;
		try
		{
			resources.lookup(destinationPath);
		}
		catch (NamingException e)
		{
			exists = false;
		}

		if (overwrite)
		{

			// Delete destination resource, if it exists
			if (exists)
			{
				if (!deleteResource(destinationPath, req, resp))
				{
					return false;
				}
				else
				{
					resp.setStatus(SakaidavStatus.SC_NO_CONTENT);
				}
			}
			else
			{
				resp.setStatus(SakaidavStatus.SC_CREATED);
			}

		}
		else
		{

			// If the destination exists, then it's a conflict
			if (exists)
			{
				resp.sendError(SakaidavStatus.SC_PRECONDITION_FAILED);
				return false;
			}

		}

		// Check to see if the parent collection of the destination exists. ContentHosting 
		// will create a parent folder if it does not exist, but the WebDAV spec requires 
		// this operation to fail (rfc2518, 8.3.1).
		
		String destParentId = isolateContainingId(adjustId(destinationPath));
		
		try {
			contentHostingService.getCollection(destParentId);
		} catch (IdUnusedException e1) {
			resp.sendError(SakaidavStatus.SC_CONFLICT);
			return false;		
		} catch (TypeException e1) {
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return false;						
		} catch (PermissionException e1) {
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return false;			
		}
		
		// Copying source to destination

		Hashtable<String,Integer> errorList = new Hashtable<String,Integer>();

		boolean result = copyResource(resources, errorList, path, destinationPath, move);

		if ((!result) || (!errorList.isEmpty()))
		{

			sendReport(req, resp, errorList);
			return false;

		}

		// kernel's copyResource copies the resource name. That's inappropriate.
		// pick a new one based on filename.
		try {
		    String filename = null;

		    if (destinationPath.indexOf('/') >= 0)
			filename = destinationPath.substring(destinationPath.lastIndexOf('/') + 1);
		    else 
			filename = destinationPath;

		    // hopefully this can't happen.
		    if (filename.length() == 0)
			filename = "null";

		    ContentResourceEdit edit = contentHostingService.editResource(adjustId(fixDirPathSAKAI(destinationPath)));
		    ResourcePropertiesEdit newProps = edit.getPropertiesEdit();
		    newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, Validator.escapeResourceName(filename));
		    contentHostingService.commitResource(edit,NotificationService.NOTI_NONE);
		} catch (Exception e) {log.info("copyResource unable to set new displayname " + e);};

		// Removing any lock-null resource which would be present at
		// the destination path
		lockNullResources.remove(destinationPath);

		if (overwrite)
			resp.setStatus(SakaidavStatus.SC_NO_CONTENT);
		else
			resp.setStatus(SakaidavStatus.SC_CREATED);
				
		return true;
	}

	/**
	 * Copy or name a resource or collection.
	 * 
	 * @param resources
	 *        Resources implementation to be used
	 * @param errorList
	 *        Hashtable containing the list of errors which occurred during the copy operation
	 * @param source
	 *        Path of the resource to be copied
	 * @param dest
	 *        Destination path
	 */
	private boolean copyResource(DirContextSAKAI resources, Hashtable<String,Integer> errorList, String source, String dest, boolean move)
	{

		if (log.isDebugEnabled()) log.debug("Copy: " + source + " To: " + dest);

		source = fixDirPathSAKAI(source);
		dest = fixDirPathSAKAI(dest);

		if (prohibited(source) || prohibited(dest)) {
		    errorList.put(source, new Integer(SakaidavStatus.SC_FORBIDDEN));
		    return false;
		}
		
		source = adjustId(source);
		dest = adjustId(dest);

		// Copy
		
		try
		{
		    boolean isCollection = contentHostingService.getProperties(source).getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);

		    /* https://jira.sakaiproject.org/browse/SAK-23639
		    if (move) {
		    	contentHostingService.rename(source, dest);
		    }
		    else */
		    // NOTE: moves cause a copy as below and a delete around line 2960, if rename() is fixed the remove that code also
		    if (isCollection) {
		        copyCollection(source, dest);
		    } else {
		        contentHostingService.copy(source, dest);
		    }
		}
		catch (EntityPropertyNotDefinedException e)
		{
		    log.error("propnotdef " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_INTERNAL_SERVER_ERROR));
		    return false;
		}
		catch (EntityPropertyTypeException e)
		{
		    log.error("propntype " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_INTERNAL_SERVER_ERROR));
		    return false;
		}
		catch (IdUsedException e)
		    // internal error because caller checked for this
		{
		    log.error("idunused " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_INTERNAL_SERVER_ERROR));
		    return false;
		}
		catch (IdUniquenessException e)
		{
		    log.error("iduniqu " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_INTERNAL_SERVER_ERROR));
		    return false;
		}
		catch (IdLengthException e)
		{
		    log.error("idlen " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_FORBIDDEN));
		    return false;
		}
		catch (InconsistentException e)
		{
		    log.error("inconsis " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_CONFLICT));
		    return false;
		}
		catch (PermissionException e)
		{
		    log.error("perm " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_FORBIDDEN));
		    return false;
		}
		catch (InUseException e)
		{
		    log.error("in use " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_CONFLICT));
		    return false;
		}
		catch (IdUnusedException e)
		{
		    log.error("unused " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_NOT_FOUND));
		    return false;
		}
		catch (OverQuotaException e)
		{
		    log.error("quota " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_FORBIDDEN));
		    return false;
		}
		catch (IdInvalidException e)
		{
		    log.error("id invalid " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_FORBIDDEN));
		    return false;
		}
		catch (TypeException e)
		{
		    log.error("type " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_FORBIDDEN));
		    return false;
		}
		catch (ServerOverloadException e)
		{
		    log.error("overload " + e);
		    errorList.put(source, new Integer(SakaidavStatus.SC_INTERNAL_SERVER_ERROR));
		    return false;
		}

		// We did not have an error
		errorList.clear();
		return true;

	}

	/**
	 * Delete a resource.
	 * 
	 * @param req
	 *        Servlet request
	 * @param resp
	 *        Servlet response
	 * @return boolean true if the copy is successful
	 */
	private boolean deleteResource(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{

		String path = getRelativePathSAKAI(req);

		return deleteResource(path, req, resp);

	}

	/**
	 * Delete a resource.
	 * 
	 * @param path
	 *        Path of the resource which is to be deleted
	 * @param req
	 *        Servlet request
	 * @param resp
	 *        Servlet response
	 */
	private boolean deleteResource(String path, HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException
	{

		if (prohibited(path) || (path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF")))
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return false;
		}

		String ifHeader = req.getHeader("If");
		if (ifHeader == null) ifHeader = "";

		String lockTokenHeader = req.getHeader("Lock-Token");
		if (lockTokenHeader == null) lockTokenHeader = "";

		if (isLocked(path, ifHeader + lockTokenHeader))
		{
			resp.sendError(SakaidavStatus.SC_LOCKED);
			return false;
		}

		path = fixDirPathSAKAI(path); // In case we are a directory

		boolean isCollection = false;
		try
		{
			isCollection = contentHostingService.getProperties(adjustId(path)).getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);

			if (isCollection)
			{
				contentHostingService.removeCollection(adjustId(path));
			}
			else
			{
				contentHostingService.removeResource(adjustId(path));
			}
		}
		catch (PermissionException e)
		{
			resp.sendError(SakaidavStatus.SC_FORBIDDEN);
			return false;
		}
		catch (InUseException e)
		{
			return false;
		}
		catch (IdUnusedException e)
		{
			// Resource not found
			resp.sendError(SakaidavStatus.SC_NOT_FOUND);
			return false;
		}
		catch (EntityPropertyNotDefinedException e)
		{
			log.warn("SAKAIDavServlet.deleteResource() - EntityPropertyNotDefinedException " + path);
			return false;
		}
		catch (EntityPropertyTypeException e)
		{
			log.warn("SAKAIDavServlet.deleteResource() - EntityPropertyTypeException " + path);
			return false;
		}
		catch (TypeException e)
		{
			log.warn("SAKAIDavServlet.deleteResource() - TypeException " + path);
			return false;
		}
		catch (ServerOverloadException e)
		{
			log.warn("SAKAIDavServlet.deleteResource() - ServerOverloadException " + path);
			return false;
		}
		return true;

	}

	/**
	 * Send a multistatus element containing a complete error report to the client.
	 * 
	 * @param req
	 *        Servlet request
	 * @param resp
	 *        Servlet response
	 * @param errorList
	 *        List of error to be displayed
	 */
	private void sendReport(HttpServletRequest req, HttpServletResponse resp, Hashtable<String,Integer> errorList) throws ServletException,
			IOException
	{

		resp.setStatus(SakaidavStatus.SC_MULTI_STATUS);

		String absoluteUri = req.getRequestURI();
		String relativePath = getRelativePath(req);
		
		XMLWriter generatedXML = new XMLWriter();
		generatedXML.writeXMLHeader();

		generatedXML.writeElement("D", "multistatus" + generateNamespaceDeclarations(), XMLWriter.OPENING);

		Enumeration<String> pathList = errorList.keys();
		while (pathList.hasMoreElements())
		{

			String errorPath = pathList.nextElement();
			int errorCode = ((Integer) errorList.get(errorPath)).intValue();

			generatedXML.writeElement("D", "response", XMLWriter.OPENING);

			generatedXML.writeElement("D", "href", XMLWriter.OPENING);
			String toAppend = errorPath.substring(relativePath.length());
			if (!toAppend.startsWith("/")) toAppend = "/" + toAppend;
			generatedXML.writeText(absoluteUri + toAppend);
			generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
			generatedXML.writeElement("D", "status", XMLWriter.OPENING);
			generatedXML.writeText("HTTP/1.1 " + errorCode + " " + SakaidavStatus.getStatusText(errorCode));
			generatedXML.writeElement("D", "status", XMLWriter.CLOSING);

			generatedXML.writeElement("D", "response", XMLWriter.CLOSING);

		}

		generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

		Writer writer = resp.getWriter();
		writer.write(generatedXML.toString());
		writer.close();

	}

	/**
	 * Propfind helper method.
	 * 
	 * @param resources
	 *        Resources object associated with this context
	 * @param generatedXML
	 *        XML response to the Propfind request
	 * @param path
	 *        Path of the current resource
	 * @param type
	 *        Propfind type
	 * @param propertiesVector
	 *        If the propfind type is find properties by name, then this Vector contains those properties
	 */
	private void parseProperties(HttpServletRequest req, DirContextSAKAI resources, XMLWriter generatedXML, String path, int type,
			Vector<String> propertiesVector)
	{
		// Exclude any resource in the /WEB-INF and /META-INF subdirectories
		// (the "toUpperCase()" avoids problems on Windows systems)
		if (path.toUpperCase().startsWith("/WEB-INF") || path.toUpperCase().startsWith("/META-INF")) return;

		ResourceInfoSAKAI resourceInfo = new ResourceInfoSAKAI(path, resources);

		generatedXML.writeElement("D", "response", XMLWriter.OPENING);
		String status = new String("HTTP/1.1 " + SakaidavStatus.SC_OK + " " + SakaidavStatus.getStatusText(SakaidavStatus.SC_OK));

		// Generating href element
		generatedXML.writeElement("D", "href", XMLWriter.OPENING);

		String href = (String) req.getAttribute("javax.servlet.forward.servlet_path");
		if (href == null)
		{
			href = (String) req.getAttribute("javax.servlet.include.servlet_path");
		}
		if (href == null)
		{
			href = req.getContextPath();
		}

		if ((href.endsWith("/")) && (path.startsWith("/")))
			href += path.substring(1);
		else
			href += path;
		if ((resourceInfo.collection) && (!href.endsWith("/"))) href += "/";

		if (log.isDebugEnabled()) log.debug("parserProperties href=" + href);

		generatedXML.writeText(rewriteUrl(href));

		generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

		String resourceName = justName(path);

		switch (type)
		{

			case FIND_ALL_PROP:

				generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
				generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

				generatedXML.writeProperty("D", "creationdate", getISOCreationDate(resourceInfo.creationDate));
				generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
				generatedXML.writeData(resourceName);
				generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);
				generatedXML.writeProperty("D", "getcontentlanguage", Locale.getDefault().toString());
				if (!resourceInfo.collection)
				{
					generatedXML.writeProperty("D", "getlastmodified", resourceInfo.httpDate);
					generatedXML.writeProperty("D", "getcontentlength", String.valueOf(resourceInfo.length));
					generatedXML.writeProperty("D", "getcontenttype", resourceInfo.MIMEType);
					// getServletContext().getMimeType(resourceInfo.path));
					generatedXML.writeProperty("D", "getetag", resourceInfo.eTag);
					// getETagValue(resourceInfo, true));
					generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);
				}
				else
				{
					generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
					generatedXML.writeElement("D", "collection", XMLWriter.NO_CONTENT);
					generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);
				}

				generatedXML.writeProperty("D", "source", "");

				String supportedLocks = "<D:lockentry>" + "<D:lockscope><D:exclusive/></D:lockscope>" + "<D:locktype><D:write/></D:locktype>"
						+ "</D:lockentry>" + "<D:lockentry>" + "<D:lockscope><D:shared/></D:lockscope>" + "<D:locktype><D:write/></D:locktype>"
						+ "</D:lockentry>";
				generatedXML.writeElement("D", "supportedlock", XMLWriter.OPENING);
				generatedXML.writeText(supportedLocks);
				generatedXML.writeElement("D", "supportedlock", XMLWriter.CLOSING);

				generateLockDiscovery(path, generatedXML);

				generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "status", XMLWriter.OPENING);
				generatedXML.writeText(status);
				generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

				break;

			case FIND_PROPERTY_NAMES:

				generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
				generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

				generatedXML.writeElement("D", "creationdate", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "displayname", XMLWriter.NO_CONTENT);
				if (!resourceInfo.collection)
				{
					generatedXML.writeElement("D", "getcontentlanguage", XMLWriter.NO_CONTENT);
					generatedXML.writeElement("D", "getcontentlength", XMLWriter.NO_CONTENT);
					generatedXML.writeElement("D", "getcontenttype", XMLWriter.NO_CONTENT);
					generatedXML.writeElement("D", "getetag", XMLWriter.NO_CONTENT);
					generatedXML.writeElement("D", "getlastmodified", XMLWriter.NO_CONTENT);
				}
				generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "source", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "lockdiscovery", XMLWriter.NO_CONTENT);

				generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "status", XMLWriter.OPENING);
				generatedXML.writeText(status);
				generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

				break;

			case FIND_BY_PROPERTY:

				Vector<String> propertiesNotFound = new Vector<String>();

				// Parse the list of properties

				generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
				generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

				Enumeration<String> properties = propertiesVector.elements();

				while (properties.hasMoreElements())
				{

					String property = (String) properties.nextElement();

					if (property.equals("creationdate"))
					{
						generatedXML.writeProperty("D", "creationdate", getISOCreationDate(resourceInfo.creationDate));
					}
					else if (property.equals("displayname"))
					{
						generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);

						generatedXML.writeData(resourceInfo.displayName);
						generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);
					}
					else if (property.equals("getcontentlanguage"))
					{
						if (resourceInfo.collection)
						{
							propertiesNotFound.addElement(property);
						}
						else
						{
							generatedXML.writeProperty("D", "getcontentlanguage", Locale.getDefault().toString());
						}
					}
					else if (property.equals("getcontentlength"))
					{
						if (resourceInfo.collection)
						{
							propertiesNotFound.addElement(property);
						}
						else
						{
							generatedXML.writeProperty("D", "getcontentlength", (String.valueOf(resourceInfo.length)));
						}
					}
					else if (property.equals("getcontenttype"))
					{
						if (resourceInfo.collection)
						{
							propertiesNotFound.addElement(property);
						}
						else
						{
							generatedXML.writeProperty("D", "getcontenttype", getServletContext().getMimeType(resourceInfo.path));
						}
					}
					else if (property.equals("getetag"))
					{
						if (resourceInfo.collection)
						{
							propertiesNotFound.addElement(property);
						}
						else
						{
							generatedXML.writeProperty("D", "getetag", resourceInfo.eTag);
							// getETagValue(resourceInfo, true));
						}
					}
					else if (property.equals("getlastmodified"))
					{
						if (resourceInfo.collection)
						{
							propertiesNotFound.addElement(property);
						}
						else
						{
							generatedXML.writeProperty("D", "getlastmodified", resourceInfo.httpDate);
						}
					}
					else if (property.equals("resourcetype"))
					{
						if (resourceInfo.collection)
						{
							generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
							generatedXML.writeElement("D", "collection", XMLWriter.NO_CONTENT);
							generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);
						}
						else
						{
							generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);
						}
						// iscollection is an MS property. Contribute uses it but seems to be
						// able to get along without it
						// } else if (property.equals("iscollection")) {
						// generatedXML.writeProperty
						// (null, "iscollection",
						// resourceInfo.collection ? "1" : "0");
					}
					else if (property.equals("source"))
					{
						generatedXML.writeProperty("D", "source", "");
					}
					else if (property.equals("supportedlock"))
					{
						supportedLocks = "<D:lockentry>" + "<D:lockscope><D:exclusive/></D:lockscope>" + "<D:locktype><D:write/></D:locktype>"
								+ "</D:lockentry>" + "<D:lockentry>" + "<D:lockscope><D:shared/></D:lockscope>"
								+ "<D:locktype><D:write/></D:locktype>" + "</D:lockentry>";
						generatedXML.writeElement("D", "supportedlock", XMLWriter.OPENING);
						generatedXML.writeText(supportedLocks);
						generatedXML.writeElement("D", "supportedlock", XMLWriter.CLOSING);
					}
					else if (property.equals("lockdiscovery"))
					{
						if (!generateLockDiscovery(path, generatedXML)) propertiesNotFound.addElement(property);
					}
					else
					{
						propertiesNotFound.addElement(property);
					}
				}

				generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "status", XMLWriter.OPENING);
				generatedXML.writeText(status);
				generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

				Enumeration<String> propertiesNotFoundList = propertiesNotFound.elements();

				if (propertiesNotFoundList.hasMoreElements())
				{
					status = new String("HTTP/1.1 " + SakaidavStatus.SC_NOT_FOUND + " "
							+ SakaidavStatus.getStatusText(SakaidavStatus.SC_NOT_FOUND));

					generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
					generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

					while (propertiesNotFoundList.hasMoreElements())
					{
						generatedXML.writeElement("D", (String) propertiesNotFoundList.nextElement(), XMLWriter.NO_CONTENT);
					}

					generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
					generatedXML.writeElement("D", "status", XMLWriter.OPENING);
					generatedXML.writeText(status);
					generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
					generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

				}
				break;

		}

		generatedXML.writeElement("D", "response", XMLWriter.CLOSING);

	}

	/**
	 * Propfind helper method. Dispays the properties of a lock-null resource.
	 * 
	 * @param resources
	 *        Resources object associated with this context
	 * @param generatedXML
	 *        XML response to the Propfind request
	 * @param path
	 *        Path of the current resource
	 * @param type
	 *        Propfind type
	 * @param propertiesVector
	 *        If the propfind type is find properties by name, then this Vector contains those properties
	 */
	private void parseLockNullProperties(HttpServletRequest req, XMLWriter generatedXML, String path, int type,
			Vector<String> propertiesVector)
	{

		// Exclude any resource in the /WEB-INF and /META-INF subdirectories
		// (the "toUpperCase()" avoids problems on Windows systems)
		if (path.toUpperCase().startsWith("/WEB-INF") || path.toUpperCase().startsWith("/META-INF")) return;

		// Retrieving the lock associated with the lock-null resource
		LockInfo lock = (LockInfo) resourceLocks.get(path);

		if (lock == null) return;

		generatedXML.writeElement("D", "response", XMLWriter.OPENING);
		String status = new String("HTTP/1.1 " + SakaidavStatus.SC_OK + " " + SakaidavStatus.getStatusText(SakaidavStatus.SC_OK));

		// Generating href element
		generatedXML.writeElement("D", "href", XMLWriter.OPENING);

		String absoluteUri = req.getRequestURI();
		String relativePath = getRelativePath(req);
		String toAppend = path.substring(relativePath.length());
		if (!toAppend.startsWith("/")) toAppend = "/" + toAppend;

		generatedXML.writeText(rewriteUrl(normalize(absoluteUri + toAppend)));

		generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

		String resourceName = justName(path);

		switch (type)
		{

			case FIND_ALL_PROP:

				generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
				generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

				generatedXML.writeProperty("D", "creationdate", getISOCreationDate(lock.creationDate.getTime()));
				generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
				generatedXML.writeData(resourceName);
				generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);
				generatedXML.writeProperty("D", "getcontentlanguage", Locale.getDefault().toString());
				generatedXML.writeProperty("D", "getlastmodified", dateFormats()[0].format(lock.creationDate));
				generatedXML.writeProperty("D", "getcontentlength", String.valueOf(0));
				generatedXML.writeProperty("D", "getcontenttype", "");
				generatedXML.writeProperty("D", "getetag", "");
				generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
				generatedXML.writeElement("D", "lock-null", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);

				generatedXML.writeProperty("D", "source", "");

				String supportedLocks = "<D:lockentry>" + "<D:lockscope><D:exclusive/></D:lockscope>" + "<D:locktype><D:write/></D:locktype>"
						+ "</D:lockentry>" + "<D:lockentry>" + "<D:lockscope><D:shared/></D:lockscope>" + "<D:locktype><D:write/></D:locktype>"
						+ "</D:lockentry>";
				generatedXML.writeElement("D", "supportedlock", XMLWriter.OPENING);
				generatedXML.writeText(supportedLocks);
				generatedXML.writeElement("D", "supportedlock", XMLWriter.CLOSING);

				generateLockDiscovery(path, generatedXML);

				generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "status", XMLWriter.OPENING);
				generatedXML.writeText(status);
				generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

				break;

			case FIND_PROPERTY_NAMES:

				generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
				generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

				generatedXML.writeElement("D", "creationdate", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "displayname", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "getcontentlanguage", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "getcontentlength", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "getcontenttype", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "getetag", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "getlastmodified", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "source", XMLWriter.NO_CONTENT);
				generatedXML.writeElement("D", "lockdiscovery", XMLWriter.NO_CONTENT);

				generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "status", XMLWriter.OPENING);
				generatedXML.writeText(status);
				generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

				break;

			case FIND_BY_PROPERTY:

				Vector<String> propertiesNotFound = new Vector<String>();

				// Parse the list of properties

				generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
				generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

				Enumeration<String> properties = propertiesVector.elements();

				while (properties.hasMoreElements())
				{

					String property = properties.nextElement();

					if (property.equals("creationdate"))
					{
						generatedXML.writeProperty("D", "creationdate", getISOCreationDate(lock.creationDate.getTime()));
					}
					else if (property.equals("displayname"))
					{
						generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
						generatedXML.writeData(resourceName);
						generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);
					}
					else if (property.equals("getcontentlanguage"))
					{
						generatedXML.writeProperty("D", "getcontentlanguage", Locale.getDefault().toString());
					}
					else if (property.equals("getcontentlength"))
					{
						generatedXML.writeProperty("D", "getcontentlength", (String.valueOf(0)));
					}
					else if (property.equals("getcontenttype"))
					{
						generatedXML.writeProperty("D", "getcontenttype", "");
					}
					else if (property.equals("getetag"))
					{
						generatedXML.writeProperty("D", "getetag", "");
					}
					else if (property.equals("getlastmodified"))
					{
						generatedXML.writeProperty("D", "getlastmodified", dateFormats()[0].format(lock.creationDate));
					}
					else if (property.equals("resourcetype"))
					{
						generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
						generatedXML.writeElement("D", "lock-null", XMLWriter.NO_CONTENT);
						generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);
					}
					else if (property.equals("source"))
					{
						generatedXML.writeProperty("D", "source", "");
					}
					else if (property.equals("supportedlock"))
					{
						supportedLocks = "<D:lockentry>" + "<D:lockscope><D:exclusive/></D:lockscope>" + "<D:locktype><D:write/></D:locktype>"
								+ "</D:lockentry>" + "<D:lockentry>" + "<D:lockscope><D:shared/></D:lockscope>"
								+ "<D:locktype><D:write/></D:locktype>" + "</D:lockentry>";
						generatedXML.writeElement("D", "supportedlock", XMLWriter.OPENING);
						generatedXML.writeText(supportedLocks);
						generatedXML.writeElement("D", "supportedlock", XMLWriter.CLOSING);
					}
					else if (property.equals("lockdiscovery"))
					{
						if (!generateLockDiscovery(path, generatedXML)) propertiesNotFound.addElement(property);
					}
					else
					{
						propertiesNotFound.addElement(property);
					}

				}

				generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "status", XMLWriter.OPENING);
				generatedXML.writeText(status);
				generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
				generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

				Enumeration<String> propertiesNotFoundList = propertiesNotFound.elements();

				if (propertiesNotFoundList.hasMoreElements())
				{

					status = new String("HTTP/1.1 " + SakaidavStatus.SC_NOT_FOUND + " "
							+ SakaidavStatus.getStatusText(SakaidavStatus.SC_NOT_FOUND));

					generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
					generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

					while (propertiesNotFoundList.hasMoreElements())
					{
						generatedXML.writeElement("D", (String) propertiesNotFoundList.nextElement(), XMLWriter.NO_CONTENT);
					}

					generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
					generatedXML.writeElement("D", "status", XMLWriter.OPENING);
					generatedXML.writeText(status);
					generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
					generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

				}

				break;

		}

		generatedXML.writeElement("D", "response", XMLWriter.CLOSING);

	}

	/**
	 * Print the lock discovery information associated with a path.
	 * 
	 * @param path
	 *        Path
	 * @param generatedXML
	 *        XML data to which the locks info will be appended
	 * @return true if at least one lock was displayed
	 */
	private boolean generateLockDiscovery(String path, XMLWriter generatedXML)
	{

		LockInfo resourceLock = (LockInfo) resourceLocks.get(path);
		Enumeration<LockInfo> collectionLocksList = collectionLocks.elements();

		boolean wroteStart = false;

		if (resourceLock != null)
		{
			wroteStart = true;
			generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);
			resourceLock.toXML(generatedXML);
		}

		while (collectionLocksList.hasMoreElements())
		{
			LockInfo currentLock = (LockInfo) collectionLocksList.nextElement();
			if (path.startsWith(currentLock.path))
			{
				if (!wroteStart)
				{
					wroteStart = true;
					generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);
				}
				currentLock.toXML(generatedXML);
			}
		}

		if (wroteStart)
		{
			generatedXML.writeElement("D", "lockdiscovery", XMLWriter.CLOSING);
		}
		else
		{
			return false;
		}

		return true;

	}

	/**
	 * Get creation date in ISO format.
	 */
	private String getISOCreationDate(long creationDate)
	{
		StringBuilder creationDateValue = new StringBuilder(creationDateFormat().format(new Date(creationDate)));
		/*
		 * int offset = Calendar.getInstance().getTimeZone().getRawOffset() / 3600000; // FIXME ? if (offset < 0) { creationDateValue.append("-"); offset = -offset; } else if (offset > 0) { creationDateValue.append("+"); } if (offset != 0) { if (offset <
		 * 10) creationDateValue.append("0"); creationDateValue.append(offset + ":00"); } else { creationDateValue.append("Z"); }
		 */
		return creationDateValue.toString();
	}

	/**
	 * Get a date in HTTP format.
	 */
	private String getHttpDate(long dateMS)
	{
		return httpDateFormat().format(new Date(dateMS));
	}

	// -------------------------------------------------- LockInfo Inner Class

	/**
	 * Holds a lock information.
	 */
	private class LockInfo
	{

		// -------------------------------------------------------- Constructor

		/**
		 * Constructor.
		 * 
		 * @param pathname
		 *        Path name of the file
		 */
		public LockInfo()
		{

		}

		// ------------------------------------------------- Instance Variables

		String path = "/";

		String type = "write";

		String scope = "exclusive";

		int depth = 0;

		String owner = "";

		Vector<String> tokens = new Vector<String>();

		long expiresAt = 0;

		Date creationDate = new Date();

		// ----------------------------------------------------- Public Methods

		/**
		 * Get a String representation of this lock token.
		 */
		public String toString()
		{

			String result = "Type:" + type + "\n";
			result += "Scope:" + scope + "\n";
			result += "Depth:" + depth + "\n";
			result += "Owner:" + owner + "\n";
			result += "Expiration:" + dateFormats()[0].format(new Date(expiresAt)) + "\n";
			Enumeration<String> tokensList = tokens.elements();
			while (tokensList.hasMoreElements())
			{
				result += "Token:" + tokensList.nextElement() + "\n";
			}
			return result;

		}

		/**
		 * Return true if the lock has expired.
		 */
		public boolean hasExpired()
		{
			return (System.currentTimeMillis() > expiresAt);
		}

		/**
		 * Return true if the lock is exclusive.
		 */
		public boolean isExclusive()
		{

			return (scope.equals("exclusive"));

		}

		/**
		 * Get an XML representation of this lock token. This method will append an XML fragment to the given XML writer.
		 */
		// originally this called toXML( ... false). That causes
		// the system to show a dummy lock name. That breaks
		// Contribute. It also violates the RFC. Contribute uses
		// PROPFIND to find the lock name. Furthermore, the RFC
		// specifically prohibits hiding the lock name this way.
		// Rather than treating the lock name as secret, it's
		// better to check permissions, as I now do.
		public void toXML(XMLWriter generatedXML)
		{
			toXML(generatedXML, true);
		}

		/**
		 * Get an XML representation of this lock token. This method will append an XML fragment to the given XML writer.
		 */
		public void toXML(XMLWriter generatedXML, boolean showToken)
		{

			generatedXML.writeElement("D", "activelock", XMLWriter.OPENING);

			generatedXML.writeElement("D", "locktype", XMLWriter.OPENING);
			generatedXML.writeElement("D", type, XMLWriter.NO_CONTENT);
			generatedXML.writeElement("D", "locktype", XMLWriter.CLOSING);

			generatedXML.writeElement("D", "lockscope", XMLWriter.OPENING);
			generatedXML.writeElement("D", scope, XMLWriter.NO_CONTENT);
			generatedXML.writeElement("D", "lockscope", XMLWriter.CLOSING);

			generatedXML.writeElement("D", "depth", XMLWriter.OPENING);
			if (depth == INFINITY)
			{
				generatedXML.writeText("Infinity");
			}
			else
			{
				generatedXML.writeText("0");
			}
			generatedXML.writeElement("D", "depth", XMLWriter.CLOSING);

			generatedXML.writeElement("D", "owner", XMLWriter.OPENING);
			generatedXML.writeText(owner);
			generatedXML.writeElement("D", "owner", XMLWriter.CLOSING);

			generatedXML.writeElement("D", "timeout", XMLWriter.OPENING);
			long timeout = (expiresAt - System.currentTimeMillis()) / 1000;
			generatedXML.writeText("Second-" + timeout);
			generatedXML.writeElement("D", "timeout", XMLWriter.CLOSING);

			generatedXML.writeElement("D", "locktoken", XMLWriter.OPENING);
			if (showToken)
			{
				Enumeration<String> tokensList = tokens.elements();
				while (tokensList.hasMoreElements())
				{
					generatedXML.writeElement("D", "href", XMLWriter.OPENING);
					generatedXML.writeText("opaquelocktoken:" + tokensList.nextElement());
					generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
				}
			}
			else
			{
				generatedXML.writeElement("D", "href", XMLWriter.OPENING);
				generatedXML.writeText("opaquelocktoken:dummytoken");
				generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
			}
			generatedXML.writeElement("D", "locktoken", XMLWriter.CLOSING);

			generatedXML.writeElement("D", "activelock", XMLWriter.CLOSING);

		}

	}

};

// -------------------------------------------------------- SakaidavStatus Class

/**
 * Wraps the HttpServletResponse class to abstract the specific protocol used. To support other protocols we would only need to modify this class and the SakaidavRetCode classes.
 * 
 * @author Marc Eaddy
 * @version 1.0, 16 Nov 1997
 */
class SakaidavStatus
{

	// ----------------------------------------------------- Instance Variables

	/**
	 * This Hashtable contains the mapping of HTTP and Sakaidav status codes to descriptive text. This is a static variable.
	 */
	private static Hashtable<Integer,String> mapStatusCodes = new Hashtable<Integer,String>();

	// ------------------------------------------------------ HTTP Status Codes

	/**
	 * Status code (200) indicating the request succeeded normally.
	 */
	public static final int SC_OK = HttpServletResponse.SC_OK;

	/**
	 * Status code (201) indicating the request succeeded and created a new resource on the server.
	 */
	public static final int SC_CREATED = HttpServletResponse.SC_CREATED;

	/**
	 * Status code (202) indicating that a request was accepted for processing, but was not completed.
	 */
	public static final int SC_ACCEPTED = HttpServletResponse.SC_ACCEPTED;

	/**
	 * Status code (204) indicating that the request succeeded but that there was no new information to return.
	 */
	public static final int SC_NO_CONTENT = HttpServletResponse.SC_NO_CONTENT;

	/**
	 * Status code (301) indicating that the resource has permanently moved to a new location, and that future references should use a new URI with their requests.
	 */
	public static final int SC_MOVED_PERMANENTLY = HttpServletResponse.SC_MOVED_PERMANENTLY;

	/**
	 * Status code (302) indicating that the resource has temporarily moved to another location, but that future references should still use the original URI to access the resource.
	 */
	public static final int SC_MOVED_TEMPORARILY = HttpServletResponse.SC_MOVED_TEMPORARILY;

	/**
	 * Status code (304) indicating that a conditional GET operation found that the resource was available and not modified.
	 */
	public static final int SC_NOT_MODIFIED = HttpServletResponse.SC_NOT_MODIFIED;

	/**
	 * Status code (400) indicating the request sent by the client was syntactically incorrect.
	 */
	public static final int SC_BAD_REQUEST = HttpServletResponse.SC_BAD_REQUEST;

	/**
	 * Status code (401) indicating that the request requires HTTP authentication.
	 */
	public static final int SC_UNAUTHORIZED = HttpServletResponse.SC_UNAUTHORIZED;

	/**
	 * Status code (403) indicating the server understood the request but refused to fulfill it.
	 */
	public static final int SC_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;

	/**
	 * Status code (404) indicating that the requested resource is not available.
	 */
	public static final int SC_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;

	/**
	 * Status code (500) indicating an error inside the HTTP service which prevented it from fulfilling the request.
	 */
	public static final int SC_INTERNAL_SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

	/**
	 * Status code (501) indicating the HTTP service does not support the functionality needed to fulfill the request.
	 */
	public static final int SC_NOT_IMPLEMENTED = HttpServletResponse.SC_NOT_IMPLEMENTED;

	/**
	 * Status code (502) indicating that the HTTP server received an invalid response from a server it consulted when acting as a proxy or gateway.
	 */
	public static final int SC_BAD_GATEWAY = HttpServletResponse.SC_BAD_GATEWAY;

	/**
	 * Status code (503) indicating that the HTTP service is temporarily overloaded, and unable to handle the request.
	 */
	public static final int SC_SERVICE_UNAVAILABLE = HttpServletResponse.SC_SERVICE_UNAVAILABLE;

	/**
	 * Status code (100) indicating the client may continue with its request. This interim response is used to inform the client that the initial part of the request has been received and has not yet been rejected by the server.
	 */
	public static final int SC_CONTINUE = 100;

	/**
	 * Status code (405) indicating the method specified is not allowed for the resource.
	 */
	public static final int SC_METHOD_NOT_ALLOWED = 405;

	/**
	 * Status code (409) indicating that the request could not be completed due to a conflict with the current state of the resource.
	 */
	public static final int SC_CONFLICT = 409;

	/**
	 * Status code (412) indicating the precondition given in one or more of the request-header fields evaluated to false when it was tested on the server.
	 */
	public static final int SC_PRECONDITION_FAILED = 412;

	/**
	 * Status code (413) indicating the server is refusing to process a request because the request entity is larger than the server is willing or able to process.
	 */
	public static final int SC_REQUEST_TOO_LONG = 413;

	/**
	 * Status code (415) indicating the server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method.
	 */
	public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

	// -------------------------------------------- Extended Sakaidav status code

	/**
	 * Status code (207) indicating that the response requires providing status for multiple independent operations.
	 */
	public static final int SC_MULTI_STATUS = 207;

	// This one colides with HTTP 1.1
	// "207 Parital Update OK"

	/**
	 * Status code (418) indicating the entity body submitted with the PATCH method was not understood by the resource.
	 */
	public static final int SC_UNPROCESSABLE_ENTITY = 418;

	// This one colides with HTTP 1.1
	// "418 Reauthentication Required"

	/**
	 * Status code (419) indicating that the resource does not have sufficient space to record the state of the resource after the execution of this method.
	 */
	public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;

	// This one colides with HTTP 1.1
	// "419 Proxy Reauthentication Required"

	/**
	 * Status code (420) indicating the method was not executed on a particular resource within its scope because some part of the method's execution failed causing the entire method to be aborted.
	 */
	public static final int SC_METHOD_FAILURE = 420;

	/**
	 * Status code (423) indicating the destination resource of a method is locked, and either the request did not contain a valid Lock-Info header, or the Lock-Info header identifies a lock held by another principal.
	 */
	public static final int SC_LOCKED = 423;

	// ------------------------------------------------------------ Initializer

	static
	{
		// HTTP 1.0 tatus Code
		addStatusCodeMap(SC_OK, "OK");
		addStatusCodeMap(SC_CREATED, "Created");
		addStatusCodeMap(SC_ACCEPTED, "Accepted");
		addStatusCodeMap(SC_NO_CONTENT, "No Content");
		addStatusCodeMap(SC_MOVED_PERMANENTLY, "Moved Permanently");
		addStatusCodeMap(SC_MOVED_TEMPORARILY, "Moved Temporarily");
		addStatusCodeMap(SC_NOT_MODIFIED, "Not Modified");
		addStatusCodeMap(SC_BAD_REQUEST, "Bad Request");
		addStatusCodeMap(SC_UNAUTHORIZED, "Unauthorized");
		addStatusCodeMap(SC_FORBIDDEN, "Forbidden");
		addStatusCodeMap(SC_NOT_FOUND, "Not Found");
		addStatusCodeMap(SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
		addStatusCodeMap(SC_NOT_IMPLEMENTED, "Not Implemented");
		addStatusCodeMap(SC_BAD_GATEWAY, "Bad Gateway");
		addStatusCodeMap(SC_SERVICE_UNAVAILABLE, "Service Unavailable");
		addStatusCodeMap(SC_CONTINUE, "Continue");
		addStatusCodeMap(SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
		addStatusCodeMap(SC_CONFLICT, "Conflict");
		addStatusCodeMap(SC_PRECONDITION_FAILED, "Precondition Failed");
		addStatusCodeMap(SC_REQUEST_TOO_LONG, "Request Too Long");
		addStatusCodeMap(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
		// dav Status Codes
		addStatusCodeMap(SC_MULTI_STATUS, "Multi-Status");
		addStatusCodeMap(SC_UNPROCESSABLE_ENTITY, "Unprocessable Entity");
		addStatusCodeMap(SC_INSUFFICIENT_SPACE_ON_RESOURCE, "Insufficient Space On Resource");
		addStatusCodeMap(SC_METHOD_FAILURE, "Method Failure");
		addStatusCodeMap(SC_LOCKED, "Locked");
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Returns the HTTP status text for the HTTP or WebDav status code specified by looking it up in the static mapping. This is a static function.
	 * 
	 * @param nHttpStatusCode
	 *        [IN] HTTP or WebDAV status code
	 * @return A string with a short descriptive phrase for the HTTP status code (e.g., "OK").
	 */
	public static String getStatusText(int nHttpStatusCode)
	{
		Integer intKey = Integer.valueOf(nHttpStatusCode);

		if (!mapStatusCodes.containsKey(intKey))
		{
			return "";
		}
		else
		{
			return mapStatusCodes.get(intKey);
		}
	}

	// -------------------------------------------------------- Private Methods

	/**
	 * Adds a new status code -> status text mapping. This is a static method because the mapping is a static variable.
	 * 
	 * @param nKey
	 *        [IN] HTTP or WebDAV status code
	 * @param strVal
	 *        [IN] HTTP status text
	 */
	private static void addStatusCodeMap(int nKey, String strVal)
	{
		mapStatusCodes.put(Integer.valueOf(nKey), strVal);
	}
	
};
