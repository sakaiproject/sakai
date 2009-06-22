/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.tool.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.component.cover.ComponentManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * ActiveToolComponent is the standard implementation of the Sakai ActiveTool API.
 * </p>
 */
public abstract class ActiveToolComponent extends ToolComponent implements ActiveToolManager
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(ActiveToolComponent.class);
	
	/** localized tool properties **/
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.ToolProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.tool.tools";
	private static final String RESOURCECLASS = "resource.class.tool";
	private static final String RESOURCEBUNDLE = "resource.bundle.tool";
	private String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private ResourceLoader loader = new Resource().getLoader(resourceClass, resourceBundle);
	// private ResourceLoader toolProps = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected abstract FunctionManager functionManager();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: ActiveToolManager - ToolManager is covered by our base class ToolComponent
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public void register(Tool tool, ServletContext context)
	{
		MyActiveTool at = null;

		// make it an active tool
		if (tool instanceof MyActiveTool)
		{
			at = (MyActiveTool) tool;
		}
		else
		{
			at = new MyActiveTool(tool);
		}

		at.setServletContext(context);

		// try getting the RequestDispatcher, just to test - but DON'T SAVE IT!
		// Tomcat's RequestDispatcher is NOT thread safe and must be gotten from the context
		// every time its needed!
		RequestDispatcher dispatcher = context.getNamedDispatcher(at.getId());
		if (dispatcher == null)
		{
			M_log.warn("missing dispatcher for tool: " + at.getId());
		}

		m_tools.put(at.getId(), at);
	}

	/**
	 * @inheritDoc
	 */
	public void register(Document toolXml, ServletContext context)
	{
		Element root = toolXml.getDocumentElement();
		if (!root.getTagName().equals("registration"))
		{
			M_log.info("register: invalid root element (expecting \"registration\"): " + root.getTagName());
			return;
		}

		// read the children nodes (tools)
		NodeList rootNodes = root.getChildNodes();
		final int rootNodesLength = rootNodes.getLength();
		for (int i = 0; i < rootNodesLength; i++)
		{
			Node rootNode = rootNodes.item(i);
			if (rootNode.getNodeType() != Node.ELEMENT_NODE) continue;
			Element rootElement = (Element) rootNode;

			// for tool
			if (rootElement.getTagName().equals("tool"))
			{
				org.sakaiproject.util.Tool tool = parseToolRegistration(rootElement);
				register(tool, context);
			}	
			// for function
			else if (rootElement.getTagName().equals("function"))
			{
				String function = rootElement.getAttribute("name").trim();
				functionManager().registerFunction(function);
			}
		}
	}
	
	/**
	 * Get optional Localized Tool Properties (i.e. tool title, description)
	 **/
	public String getLocalizedToolProperty(String toolId, String key) {
		if (loader == null) {
			return null;
		}
			
		final String toolProp = loader.getString(toolId + "." + key, "");
		
		if (toolProp.length() < 1 || toolProp.equals("")) {
			return null;
		}
		else {
			return toolProp;
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public List<Tool> parseTools(File toolXmlFile)
	{
		if ( ! toolXmlFile.canRead() ) return null;

                String path = toolXmlFile.getAbsolutePath();
                if (!path.endsWith(".xml"))
                {
                        M_log.info("register: skiping non .xml file: " + path);
                        return null;
                }

                M_log.info("parse-file: " + path);

                Document doc = Xml.readDocument(path);
		if ( doc == null ) return null;
		return parseTools(doc);
	}

	/**
	 * @inheritDoc
	 */
	public List<Tool> parseTools(InputStream toolXmlStream)
	{
		Document doc = Xml.readDocumentFromStream(toolXmlStream);
		try
		{
			toolXmlStream.close();
		}
		catch (Exception e)
		{
		}

		if ( doc == null ) return null;
		return parseTools(doc);
	}

	/**
	 * @inheritDoc
	 */
	public List<Tool> parseTools(Document toolXml)
	{

		List<Tool> retval = new ArrayList<Tool> ();
		Element root = toolXml.getDocumentElement();
		if (!root.getTagName().equals("registration"))
		{
			M_log.info("register: invalid root element (expecting \"registration\"): " + root.getTagName());
			return null;
		}

		// read the children nodes (tools)
		NodeList rootNodes = root.getChildNodes();
		final int rootNodesLength = rootNodes.getLength();
		for (int i = 0; i < rootNodesLength; i++)
		{
			Node rootNode = rootNodes.item(i);
			if (rootNode.getNodeType() != Node.ELEMENT_NODE) continue;
			Element rootElement = (Element) rootNode;

			// for tool
			if (rootElement.getTagName().equals("tool"))
			{
				org.sakaiproject.util.Tool tool = parseToolRegistration(rootElement);
				retval.add(tool);
			}

		}
		if ( retval.size() < 1 ) return null;
		return retval;
	}

	private org.sakaiproject.util.Tool parseToolRegistration(Element rootElement)
	{
		org.sakaiproject.util.Tool tool = new org.sakaiproject.util.Tool();
		
		final String toolId = rootElement.getAttribute("id").trim();
		tool.setId(toolId);
		
		final String toolTitle = getLocalizedToolProperty(toolId, "title");
		final String toolDescription =  getLocalizedToolProperty(toolId, "description");
			
		// if the key is empty or absent, the length is 0. if so, use the string from the XML file
		if(toolTitle != null && toolTitle.length()>0)
		{
			tool.setTitle(toolTitle);
		}
		else
		{
			tool.setTitle(rootElement.getAttribute("title").trim());
		}
		
		// if the key is empty or absent, the length is 0. if so, use the string from the XML file
		if(toolDescription != null && toolDescription.length()>0)
		{
			tool.setDescription(toolDescription);
		}
		else
		{
			tool.setDescription(rootElement.getAttribute("description").trim());
		}
		
		tool.setHome(StringUtil.trimToNull(rootElement.getAttribute("home")));

		if ("tool".equals(rootElement.getAttribute("accessSecurity")))
		{
			tool.setAccessSecurity(Tool.AccessSecurity.TOOL);
		}
		else
		{
			tool.setAccessSecurity(Tool.AccessSecurity.PORTAL);
		}

		// collect values for these collections
		Properties finalConfig = new Properties();
		Properties mutableConfig = new Properties();
		Set categories = new HashSet();
		Set keywords = new HashSet();
		NodeList kids = rootElement.getChildNodes();
		final int kidsLength = kids.getLength();
		for (int k = 0; k < kidsLength; k++)
		{
			Node kidNode = kids.item(k);
			if (kidNode.getNodeType() != Node.ELEMENT_NODE) continue;
			Element kidElement = (Element) kidNode;

			// for configuration
			if (kidElement.getTagName().equals("configuration"))
			{
				String name = kidElement.getAttribute("name").trim();
				String value = kidElement.getAttribute("value").trim();
				String type = kidElement.getAttribute("type").trim();
				if (name.length() > 0)
				{
					if ("final".equals(type))
					{
						finalConfig.put(name, value);
					}
					else
					{
						mutableConfig.put(name, value);
					}
				}
			}

			// for category
			if (kidElement.getTagName().equals("category"))
			{
				String name = kidElement.getAttribute("name").trim();
				if (name.length() > 0)
				{
					categories.add(name);
				}
			}

			// for keyword
			if (kidElement.getTagName().equals("keyword"))
			{
				String name = kidElement.getAttribute("name").trim();
				if (name.length() > 0)
				{
					keywords.add(name);
				}
			}
		}

		// set the tool's collected values
		tool.setRegisteredConfig(finalConfig, mutableConfig);
		tool.setCategories(categories);
		tool.setKeywords(keywords);

		return tool;
	}

	/**
	 * @inheritDoc
	 */
	public void register(File toolXmlFile, ServletContext context)
	{
		String path = toolXmlFile.getAbsolutePath();
		if (!path.endsWith(".xml"))
		{
			M_log.info("register: skiping non .xml file: " + path);
			return;
		}

		M_log.info("register: file: " + path);

		Document doc = Xml.readDocument(path);
		register(doc, context);
	}

	/**
	 * @inheritDoc
	 */
	public void register(InputStream toolXmlStream, ServletContext context)
	{
		Document doc = Xml.readDocumentFromStream(toolXmlStream);
		try
		{
			toolXmlStream.close();
		}
		catch (Exception e)
		{
		}

		register(doc, context);
	}

	/**
	 * @inheritDoc
	 */
	public ActiveTool getActiveTool(String id)
	{
		if (id == null) return null;
		return (ActiveTool) m_tools.get(id);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Entity: ActiveTool
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class MyActiveTool extends org.sakaiproject.util.Tool implements ActiveTool
	{

		protected ServletContext m_servletContext = null;

		/**
		 * Construct
		 */
		public MyActiveTool()
		{
			super();
		}

		public void setServletContext(ServletContext context)
		{
			m_servletContext = context;
		}

		/**
		 * Construct from a Tool
		 */
		public MyActiveTool(org.sakaiproject.tool.api.Tool t)
		{
			super();
			this.m_categories.addAll(t.getCategories());
			this.m_mutableConfig.putAll(t.getMutableConfig());
			this.m_finalConfig.putAll(t.getFinalConfig());
			this.m_keywords.addAll(t.getKeywords());
			this.m_id = t.getId();
			this.m_title = t.getTitle();
			this.m_description = t.getDescription();
			this.m_accessSecurity = t.getAccessSecurity();
			this.m_home = t.getHome();
		}

		/**
		 * Return a RequestDispatcher that can be used to dispatch to this tool - RequestDispatcher is NOT THREAD-SAFE FOR REUSE, so this method must be called each and every time a request is forwarded.
		 */
		protected RequestDispatcher getDispatcher()
		{
			return m_servletContext.getNamedDispatcher(getId());
		}

		/**
		 * @inheritDoc
		 */
		public void forward(HttpServletRequest req, HttpServletResponse res, Placement placement, String toolContext,
				String toolPath) throws ToolException
		{

			WrappedRequest wreq = null;

			try
			{
				wreq = new WrappedRequest(req, toolContext, toolPath, placement, false, this);
				WrappedResponse wres = new WrappedResponse(wreq, res);
				RequestDispatcher dispatcher = getDispatcher();
				if (dispatcher == null) {
					throw new IllegalArgumentException("Unable to find registered context for tool with ID " + getId());
				}

				dispatcher.forward(wreq, wres);
			}
			catch (IOException e)
			{
				throw new ToolException(e);
			}
			catch (ToolException e)
			{
				throw e;
			}
			catch (ServletException e)
			{
				throw new ToolException(e);
			}
			finally
			{
				if (wreq != null) wreq.cleanup();
			}
		}

		/**
		 * @inheritDoc
		 */
		public void include(HttpServletRequest req, HttpServletResponse res, Placement placement, String toolContext,
				String toolPath) throws ToolException
		{
			WrappedRequest wreq = null;

			try
			{
				wreq = new WrappedRequest(req, toolContext, toolPath, placement, true, this);
				getDispatcher().include(wreq, res);
			}
			catch (IOException e)
			{
				throw new ToolException(e);
			}
			catch (ToolException e)
			{
				throw e;
			}
			catch (ServletException e)
			{
				throw new ToolException(e);
			}
			finally
			{
				if (wreq != null) wreq.cleanup();
			}
		}

		/**
		 * @inheritDoc
		 */
		public void help(HttpServletRequest req, HttpServletResponse res, String toolContext, String toolPath) throws ToolException
		{
			// fragment?
			boolean fragment = Boolean.TRUE.toString().equals(req.getAttribute(FRAGMENT));

			WrappedRequest wreq = null;

			try
			{
				wreq = new WrappedRequest(req, toolContext, toolPath, null, fragment, this);
				if (fragment)
				{
					getDispatcher().include(wreq, res);
				}
				else
				{
					getDispatcher().forward(wreq, res);
				}
			}
			catch (IOException e)
			{
				throw new ToolException(e);
			}
			catch (ToolException e)
			{
				throw e;
			}
			catch (ServletException e)
			{
				throw new ToolException(e);
			}
			finally
			{
				if (wreq != null) wreq.cleanup();
			}
		}

		/**
		 * Wraps a request object so we can override some standard behavior.
		 */
		public class WrappedRequest extends HttpServletRequestWrapper
		{
			/** The context to report. */
			protected String m_context = null;

			/** The pathInfo to report. */
			protected String m_path = null;

			/** attributes to override the wrapped req's attributes. */
			protected Map m_attributes = new HashMap();

			/** The prior current tool session, to restore after we are done. */
			protected ToolSession m_priorToolSession = null;

			/** The prior current tool, to restore after we are done. */
			protected Tool m_priorTool = null;

			/** The prior current placementl, to restore after we are done. */
			protected Placement m_priorPlacement = null;

			/** The request object we wrap. */
			protected HttpServletRequest m_wrappedReq = null;

			public WrappedRequest(HttpServletRequest req, String context, String path, Placement placement, boolean fragment,
					Tool tool)
			{
				super(req);
				m_wrappedReq = req;

				m_context = context;

				// default if needed
				if (m_context == null)
				{
					m_context = req.getContextPath() + req.getServletPath() + req.getPathInfo();
				}

				m_path = path;

				if (placement != null)
				{
					ToolSession ts = sessionManager().getCurrentSession().getToolSession(placement.getId());

					// put the session in the request attribute
					setAttribute(TOOL_SESSION, ts);

					// set as the current tool session, and setup for undoing this later
					m_priorToolSession = sessionManager().getCurrentToolSession();
					sessionManager().setCurrentToolSession(ts);

					// set this tool placement as current, in the request and for the service's "current" tool placement
					setAttribute(PLACEMENT, placement);
					setAttribute(PLACEMENT_ID, placement.getId());
					m_priorPlacement = getCurrentPlacement();
					setCurrentPlacement(placement);
				}

				setAttribute(FRAGMENT, Boolean.toString(fragment));

				// set this tool as current, in the request and for the service's "current" tool
				setAttribute(TOOL, tool);
				m_priorTool = getCurrentTool();
				setCurrentTool(tool);
			}

	      private String requestURI;
	      /**
	       * Allows us to set the request URI to the correct value if desired
	       * @param requestURI this should be the complete URL for this request (not counting the get params, everything after the ?)
	       */
	      public void setRequestURI(String requestURI) {
	         this.requestURI = requestURI;
	      }

	      @Override
	      public StringBuffer getRequestURL() {
	         StringBuffer sb = null;
	         if (requestURI == null) {
	            sb = super.getRequestURL();
	         } else {
	            sb = new StringBuffer(requestURI);
	         }
	         // now attempt to autofix the URL
	         sb.append( getURLSuffix(sb.toString()) );
	         return sb;
	      };

	      @Override
	      public String getRequestURI() {
	         String uri = null;
	         if (requestURI == null) {
	            uri = super.getRequestURI();
	         } else {
	            uri = requestURI;
	         }
            // now attempt to autofix the URL
            uri += getURLSuffix(uri);
	         return uri;
	      }

	      /**
	       * This is here to fix up the directtool stuff,
	       * http://jira.sakaiproject.org/jira/browse/SAK-8946,
	       * it is mostly here to make the requestURL and pathinfo conform to the servlet spec<br/>
	       * Used like so:<br/>
          *  uri += getURLSuffix(uri);
	       * 
	       * @param requestURL the current requestURL (probably invalid)
	       * @return the suffix to append to the existing requestURL
	       */
	      private String getURLSuffix(String requestURL) {
	         // now attempt to autofix the URL
	         StringBuilder sb = new StringBuilder();
	         if (requestURL != null) {
               String path = getPathInfo();
               if (path != null) {
                  if (! requestURL.contains(path)) {
                     if (! path.startsWith("/")) {
                        sb.append("/");
                     }
                     sb.append(path);
                  }
               }
	         }
            return sb.toString();
	      }

	      public String getPathInfo()
			{
				if (getAttribute(NATIVE_URL) != null) return super.getPathInfo();

				return m_path;
			}

			public String getServletPath()
			{
				if (getAttribute(NATIVE_URL) != null) return super.getServletPath();

				return "";
			}

			public String getContextPath()
			{
				if (getAttribute(NATIVE_URL) != null) return super.getContextPath();

				return m_context;
			}

			public Object getAttribute(String name)
			{
				if (m_attributes.containsKey(name))
				{
					return m_attributes.get(name);
				}

				return super.getAttribute(name);
			}

			public Enumeration getAttributeNames()
			{
				Set s = new HashSet();
				s.addAll(m_attributes.keySet());
				for (Enumeration e = super.getAttributeNames(); e.hasMoreElements();)
				{
					String name = (String) e.nextElement();
					s.add(name);
				}

				return new IteratorEnumeration(s.iterator());
			}

			public void setAttribute(String name, Object value)
			{
				m_attributes.put(name, value);

				// if this is a special attribute, set it back through all wrapped requests of this class
				if (Tool.NATIVE_URL.equals(name))
				{
					m_wrappedReq.setAttribute(name, value);
				}
			}

			public void removeAttribute(String name)
			{
				if (m_attributes.containsKey(name))
				{
					m_attributes.remove(name);
				}

				else
				{
					super.removeAttribute(name);
				}

				// if this is a special attribute, set it back through all wrapped requests of this class
				if (Tool.NATIVE_URL.equals(name))
				{
					m_wrappedReq.removeAttribute(name);
				}
			}

			public void cleanup()
			{
				// restore the tool, placement and tool session and tool placement config that was in effect before we changed it
				setCurrentTool(m_priorTool);
				sessionManager().setCurrentToolSession(m_priorToolSession);
				setCurrentPlacement(m_priorPlacement);
			}

			/**************************************************************************************************************************************************************************************************************************************************
			 * Enumeration over an iterator
			 *************************************************************************************************************************************************************************************************************************************************/

			protected class IteratorEnumeration implements Enumeration
			{
				/** The iterator over which this enumerates. */
				protected Iterator m_iterator = null;

				public IteratorEnumeration(Iterator i)
				{
					m_iterator = i;
				}

				public boolean hasMoreElements()
				{
					return m_iterator.hasNext();
				}

				public Object nextElement()
				{
					return m_iterator.next();
				}
			}
		}

		/**
		 * Wraps a response object so we can override some standard behavior.
		 */
		public class WrappedResponse extends HttpServletResponseWrapper
		{
			/** The request. */
			protected HttpServletRequest m_req = null;

			/** The wrapped response. */
			protected HttpServletResponse m_res = null;

			public WrappedResponse(HttpServletRequest req, HttpServletResponse res)
			{
				super(res);

				m_req = req;
				m_res = res;
			}

			public String encodeRedirectUrl(String url)
			{
				return rewriteURL(url);
			}

			public String encodeRedirectURL(String url)
			{
				return rewriteURL(url);
			}

			public String encodeUrl(String url)
			{
				return rewriteURL(url);
			}

			public String encodeURL(String url)
			{
				return rewriteURL(url);
			}

			public void sendRedirect(String url) throws IOException
			{
				// SAK-13408 - Relative redirections are based on the request URI. This fix addresses the problem 
				// of Websphere having a different request URI than Tomcat. Instead, the relative URL will be
				// converted to an absolute URL.
				if ("websphere".equals(ServerConfigurationService.getString("servlet.container")))
				{
			    	url = createAbsoluteURL(url);
				}
				super.sendRedirect(rewriteURL(url));
			}

			/**
			 * This method takes the given relative Sakai URL and uses the
			 * context path and path info to create the corresponding 
			 * absolute URL.
			 * 
			 * @param relativeUrl the relative URL to convert to an absolute URL
			 * @return the absolute URL
			 */
			protected String createAbsoluteURL(String relativeUrl) {
				// ensure this is a relative URL
				if (!(relativeUrl.toLowerCase().startsWith("http")) && !(relativeUrl.startsWith("/"))) 
				{
					ActiveToolComponent.MyActiveTool.WrappedRequest wr = (WrappedRequest) this.m_req;
					
					// need to obtain any extra path info from the path
					StringBuilder pathBuilder = new StringBuilder("");
					if (wr.m_path != null)
					{
						StringTokenizer pathTokenizer = new StringTokenizer(wr.m_path, "/");
						// if the path has more than one segment (eg. "/name1/name2")
						int numberOfPathElements = pathTokenizer.countTokens();
						if (numberOfPathElements > 1)
						{
							// copy over everything but the last segment
							for (int i = 0; i < numberOfPathElements - 1; i++)
							{
								pathBuilder.append("/");
								pathBuilder.append(pathTokenizer.nextToken());
							}
						}
					}
					if (!pathBuilder.toString().endsWith("/"))
					{
						pathBuilder.append("/");
					}

					relativeUrl = wr.m_context + pathBuilder.toString() + relativeUrl;
				}
				return relativeUrl;
			}

			/**
			 * Rewrites the given URL to insert the current tool placement id, if any, as the start of the path
			 * 
			 * @param url
			 *        The url to rewrite.
			 */
			protected String rewriteURL(String url)
			{
				// only if we are in native url mode - if in Sakai mode, don't include the placement id
				if (m_req.getAttribute(NATIVE_URL) != null)
				{
					// if we have a tool placement to add, add it
					Placement placement = (Placement) m_req.getAttribute(PLACEMENT);
					if (placement != null)
					{
						String placementId = placement.getId();
						// compute the URL root "back" to this servlet
						// Note: this must not be pre-computed, as it can change as things are dispatched
						StringBuilder full = new StringBuilder();
						full.append(m_req.getScheme());
						full.append("://");
						full.append(m_req.getServerName());
						if (((m_req.getServerPort() != 80) && (!m_req.isSecure()))
								|| ((m_req.getServerPort() != 443) && (m_req.isSecure())))
						{
							full.append(":");
							full.append(m_req.getServerPort());
						}

						// include just the context path - anything to this context will get the encoding
						StringBuilder rel = new StringBuilder();
						rel.append(m_req.getContextPath());

						full.append(rel.toString());

						// if we match the fullUrl, or the relUrl, assume that this is a URL back to this servlet context
						if ((url.startsWith(full.toString()) || url.startsWith(rel.toString())))
						{
							// put the placementId in as a parameter
							StringBuilder newUrl = new StringBuilder(url);
							if (url.indexOf('?') != -1)
							{
								newUrl.append('&');
							}
							else
							{
								newUrl.append('?');
							}
							newUrl.append(Tool.PLACEMENT_ID);
							newUrl.append("=");
							newUrl.append(placementId);

							// TODO: (or not) let the wrapped resp. do the work, too
							return newUrl.toString();
						}
					}
				}

				return url;
			}
		}
	}
}
