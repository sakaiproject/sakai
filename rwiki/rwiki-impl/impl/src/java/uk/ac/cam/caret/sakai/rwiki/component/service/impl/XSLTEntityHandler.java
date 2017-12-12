/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.DebugContentHandler;
import uk.ac.cam.caret.sakai.rwiki.utils.DigestHtml;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.SchemaNames;
import uk.ac.cam.caret.sakai.rwiki.utils.SimpleCoverage;

/**
 * Provides a XSLT Based entity handler. It will serialise the an RWikiObject
 * into XML and then apply a XSLT to generate the Whole output.
 * 
 * @author ieb
 */
@Slf4j
public class XSLTEntityHandler extends BaseEntityHandlerImpl
{

	private static ThreadLocal currentRequest = new ThreadLocal();

	/**
	 * dependency
	 */
	private String anchorLinkFormat = null;

	/**
	 * dependency
	 */
	private String standardLinkFormat = null;

	/**
	 * dependency
	 */
	private String hrefTagFormat = null;

	/**
	 * dependency
	 */
	private String xalan270ContentHandler = null;

	/**
	 * dependency
	 */
	private RenderService renderService = null;

	/**
	 * dependency
	 */
	private String authZPrefix = ""; //$NON-NLS-1$

	/**
	 * dependency The base name of the xslt file, relative to context root.
	 */
	private String xslt = null;
	/**
	 * Control whether the xml is escaped for this handler
	 */
	private boolean escaped = true;

	/**
	 * dependency The default strack trace message to use if all else fails
	 * (pattern).
	 */
	private String defaultStackTrace;

	/**
	 * A format pattern for formatting a stack trace in the xml.@param tag for
	 * 'servletConfig'. 140 Expected
	 * 
	 * @throws tag
	 *         for 'ServletException'.
	 */
	private String errorFormat;

	/**
	 * Thread holder for the transformer
	 */
	private ThreadLocal transformerHolder = new ThreadLocal();

	/**
	 * A map containing headers to inject into the response
	 */
	private Map responseHeaders;

	/**
	 * A map containing SAX Serializer properties
	 */
	private Map outputProperties;
	
	/**
	 * A map containing transform parameters.
	 */
	private Map<String, String> transformParameters;

	private EntityManager entityManager;

	private SAXParserFactory saxParserFactory;

	private SerializerFactory serializerFactory;

	private String breadCrumbParameter = "breadcrumb";

	private String breadCrumbParameterFormat = "?breadcrumb=0";

	/** Configuration: allow use of alias for site id in references. */
	protected boolean m_siteAlias = true;
	
	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name); //$NON-NLS-1$
		}
		return o;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription(Entity entity)
	{
		if (!isAvailable()) return null;
		if (!(entity instanceof RWikiEntity)) return null;
		return entity.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrl(Entity entity)
	{
		if (!isAvailable()) return null;
		if (!(entity instanceof RWikiEntity)) return null;
		return entity.getUrl() + getMinorType();
	}

	/**
	 * @return
	 */
	public static HttpServletRequest getCurrentRequest()
	{
		return (HttpServletRequest) currentRequest.get();
	}

	/**
	 * @param request
	 */
	public static void setCurrentRequest(HttpServletRequest request)
	{
		currentRequest.set(request);
	}

	/**
	 * {@inheritDoc}
	 */
	public void outputContent(final Entity entity, final Entity sideBar,
			final HttpServletRequest request, final HttpServletResponse res)
	{
		if (!isAvailable()) return;
		if (!(entity instanceof RWikiEntity)) return;

		try
		{
			String skin = ServerConfigurationService.getString("skin.default"); //$NON-NLS-1$
			String skinRepo = ServerConfigurationService.getString("skin.repo"); //$NON-NLS-1$
			request.setAttribute("sakai.skin.repo", skinRepo); //$NON-NLS-1$
			request.setAttribute("sakai.skin", skin); //$NON-NLS-1$

			HttpSession s = request.getSession();
			PageVisits pageVisits = (PageVisits) s.getAttribute(XSLTEntityHandler.class
					.getName()
					+ this.getMinorType() + "_visits");
			
			boolean withBreadcrumbs = !"0".equals(request.getParameter(breadCrumbParameter ) );
			if (pageVisits == null)
			{
				pageVisits = new PageVisits();
				s.setAttribute(XSLTEntityHandler.class.getName() + this.getMinorType()
						+ "_visits", pageVisits);
			}
			RWikiEntity rwe = (RWikiEntity) entity;
			if (!rwe.isContainer())
			{
				RWikiObject rwo = rwe.getRWikiObject();
				pageVisits.addPage(rwo.getName());
			}

			setCurrentRequest(request);

			if (responseHeaders != null)
			{
				for (Iterator i = responseHeaders.keySet().iterator(); i.hasNext();)
				{
					String name = (String) i.next();
					String value = (String) responseHeaders.get(name);
					res.setHeader(name, value);

				}

			}

			OutputStream out = res.getOutputStream();

			ContentHandler opch = getOutputHandler(out);
			ContentHandler ch = null;
			if (false)
			{
				ch = new DebugContentHandler(opch);
			}
			else
			{
				ch = opch;
			}

			Attributes dummyAttributes = new AttributesImpl();

			ch.startDocument();
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ENTITYSERVICE,
					SchemaNames.EL_NSENTITYSERVICE, dummyAttributes);

			AttributesImpl propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_REQUEST_PATH_INFO, //$NON-NLS-1$
					SchemaNames.ATTR_REQUEST_PATH_INFO, "string", request //$NON-NLS-1$
							.getPathInfo());
			propA.addAttribute("", SchemaNames.ATTR_REQUEST_USER, //$NON-NLS-1$
					SchemaNames.ATTR_REQUEST_USER, "string", request //$NON-NLS-1$
							.getRemoteUser());
			propA.addAttribute("", SchemaNames.ATTR_REQUEST_PROTOCOL, //$NON-NLS-1$
					SchemaNames.ATTR_REQUEST_PROTOCOL, "string", request //$NON-NLS-1$
							.getProtocol());
			propA.addAttribute("", SchemaNames.ATTR_REQUEST_SERVER_NAME, //$NON-NLS-1$
					SchemaNames.ATTR_REQUEST_SERVER_NAME, "string", request //$NON-NLS-1$
							.getServerName());
			propA.addAttribute("", SchemaNames.ATTR_REQUEST_SERVER_PORT, //$NON-NLS-1$
					SchemaNames.ATTR_REQUEST_SERVER_PORT, "string", String //$NON-NLS-1$
							.valueOf(request.getServerPort()));
			propA.addAttribute("", SchemaNames.ATTR_REQUEST_REQUEST_URL, //$NON-NLS-1$
					SchemaNames.ATTR_REQUEST_REQUEST_URL, "string", String //$NON-NLS-1$
							.valueOf(request.getRequestURL()));

			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_PROPERTIES,
					SchemaNames.EL_NSREQUEST_PROPERTIES, propA);
			addRequestAttributes(ch, request);
			addRequestParameters(ch, request);

			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_PROPERTIES,
					SchemaNames.EL_NSREQUEST_PROPERTIES);

			if ( withBreadcrumbs ) {
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_PAGEVISITS,
					SchemaNames.EL_NSPAGEVISITS, dummyAttributes);

			List<String[]> pv = pageVisits.getPageNames(this.getMinorType());

			for (Iterator<String[]> i = pv.iterator(); i.hasNext();)
			{
				String[] visit = i.next();
				propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_URL, SchemaNames.ATTR_URL,
						"string", visit[0]);
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_PAGEVISIT,
						SchemaNames.EL_NSPAGEVISIT, propA, visit[1]);
			}
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_PAGEVISITS,
					SchemaNames.EL_NSPAGEVISITS);
			}

			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ENTITY,
					SchemaNames.EL_NSENTITY, dummyAttributes);
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTIES,
					SchemaNames.EL_NSXMLPROPERTIES, dummyAttributes);
			ResourceProperties rp = entity.getProperties();

			for (Iterator i = rp.getPropertyNames(); i.hasNext();)
			{
				Object key = i.next();
				String name = String.valueOf(key);
				String value = String.valueOf(rp.getProperty(name));
				propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
						SchemaNames.ATTR_NAME, "string", name); //$NON-NLS-1$
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTY,
						SchemaNames.EL_NSXMLPROPERTY, propA, value);
			}
			propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
					SchemaNames.ATTR_NAME, "string", "_handler"); //$NON-NLS-1$ //$NON-NLS-2$
			addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTY,
					SchemaNames.EL_NSXMLPROPERTY, propA, " XSLTEntity Handler"); //$NON-NLS-1$

			if (!rwe.isContainer())
			{
				RWikiObject rwo = rwe.getRWikiObject();
				propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
						SchemaNames.ATTR_NAME, "string", "_title"); //$NON-NLS-1$ //$NON-NLS-2$
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTY,
						SchemaNames.EL_NSXMLPROPERTY, propA, NameHelper.localizeName(rwo
								.getName(), rwo.getRealm()));

			}
			else
			{
				propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
						SchemaNames.ATTR_NAME, "string", "_title"); //$NON-NLS-1$ //$NON-NLS-2$
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTY,
						SchemaNames.EL_NSXMLPROPERTY, propA, entity.getReference());

			}
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTIES,
					SchemaNames.EL_NSXMLPROPERTIES);

			/* http://jira.sakaiproject.org/browse/SAK-13281
			 * escapeXML is controlled via config settings
			 */
			
			if (!rwe.isContainer())
			{
				RWikiObject rwo = rwe.getRWikiObject();
				ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_RENDEREDCONTENT,
						SchemaNames.EL_NSRENDEREDCONTENT, dummyAttributes);
				
				renderToXML(rwo, ch, withBreadcrumbs, this.escaped);
				ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_RENDEREDCONTENT,
						SchemaNames.EL_NSRENDEREDCONTENT);
			}
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ENTITY,
					SchemaNames.EL_NSENTITY);

			if (sideBar != null && sideBar instanceof RWikiEntity)
			{

				RWikiEntity sbrwe = (RWikiEntity) sideBar;
				RWikiObject sbrwo = sbrwe.getRWikiObject();
				ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_SIDEBAR,
						SchemaNames.EL_NSSIDEBAR, dummyAttributes);

				ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTIES,
						SchemaNames.EL_NSXMLPROPERTIES, dummyAttributes);
				propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
						SchemaNames.ATTR_NAME, "string", "_title"); //$NON-NLS-1$ //$NON-NLS-2$
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTY,
						SchemaNames.EL_NSXMLPROPERTY, propA, NameHelper.localizeName(
								sbrwo.getName(), sbrwo.getRealm()));
				ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_XMLPROPERTIES,
						SchemaNames.EL_NSXMLPROPERTIES);

				ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_RENDEREDCONTENT,
						SchemaNames.EL_NSRENDEREDCONTENT, dummyAttributes);
				renderToXML(sbrwo, ch, withBreadcrumbs, this.escaped);
				ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_RENDEREDCONTENT,
						SchemaNames.EL_NSRENDEREDCONTENT);

				ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_SIDEBAR,
						SchemaNames.EL_NSSIDEBAR);

			}

			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ENTITYSERVICE,
					SchemaNames.EL_NSENTITYSERVICE);

			ch.endDocument();

		}
		catch (Throwable ex)
		{
			log.info("Failed to serialize " + ex.getMessage()); //$NON-NLS-1$
			throw new RuntimeException(Messages.getString("XSLTEntityHandler.68") //$NON-NLS-1$
					+ ex.getLocalizedMessage(), ex);
		}
		finally
		{
			setCurrentRequest(null);
		}
	}

	/**
	 * Adds an element to the content handler.
	 * 
	 * @param ch
	 *        the content handler
	 * @param ns
	 *        the name space of the element
	 * @param lname
	 *        the local name
	 * @param qname
	 *        the qname
	 * @param attr
	 *        the attribute list
	 * @param content
	 *        content of the element
	 * @throws SAXException
	 *         if the underlying sax chain has a problem
	 */
	public void addElement(final ContentHandler ch, final String ns, final String lname,
			final String qname, final Attributes attr, final Object content)
			throws SAXException
	{

		ch.startElement(ns, lname, qname, attr);
		try
		{
			if (content != null)
			{
				char[] c = String.valueOf(content).toCharArray();
				ch.characters(c, 0, c.length);
			}
		}
		finally
		{
			ch.endElement(ns, lname, qname);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getProperties(Entity entity)
	{
		return entity.getProperties();
	}

	/**
	 * Serialises the rendered content of the RWiki Object to SAX
	 * 
	 * @param rwo
	 * @param ch
	 * @param withBreadCrumb 
	 */
	public void renderToXML(RWikiObject rwo, final ContentHandler ch, boolean withBreadCrumb, boolean escapeXML)
			throws SAXException, IOException
	{

		String renderedPage;
		try
		{
			renderedPage = render(rwo,withBreadCrumb);
		}
		catch (Exception e)
		{
			renderedPage = Messages.getString("XSLTEntityHandler.32") + rwo.getName() + Messages.getString("XSLTEntityHandler.33") + e.getClass() + Messages.getString("XSLTEntityHandler.34") + e.getMessage(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			log.info(renderedPage, e);
		}
		String contentDigest = DigestHtml.digest(renderedPage);
		if (contentDigest.length() > 500)
		{
			contentDigest = contentDigest.substring(0, 500);
		}
		if (renderedPage == null || renderedPage.trim().length() == 0)
		{
			renderedPage = Messages.getString("XSLTEntityHandler.35"); //$NON-NLS-1$
		}
		if (contentDigest == null || contentDigest.trim().length() == 0)
		{
			contentDigest = Messages.getString("XSLTEntityHandler.36"); //$NON-NLS-1$
		}

		String cdataEscapedRendered = renderedPage
				.replaceAll("]]>", "]]>]]&gt;<![CDATA["); //$NON-NLS-1$ //$NON-NLS-2$
		String cdataContentDigest = contentDigest.replaceAll("]]>", "]]>]]&gt;<![CDATA["); //$NON-NLS-1$ //$NON-NLS-2$

        /* http://jira.sakaiproject.org/browse/SAK-13281
         * ensure all page content is escaped or double escaped before it goes into the parser,
         * if this is not done then the parser will unescape html entities during processing
         */		
        renderedPage = "<content><rendered>" + (escapeXML ? StringEscapeUtils.escapeXml(renderedPage) : renderedPage) //$NON-NLS-1$
				+ "</rendered><rendered-cdata><![CDATA[" + cdataEscapedRendered + "]]></rendered-cdata><contentdigest><![CDATA[" + cdataContentDigest //$NON-NLS-1$ //$NON-NLS-2$
				+ "]]></contentdigest></content>"; //$NON-NLS-1$

		try
		{
			parseToSAX(renderedPage, ch);
		}
		catch (SAXException ex)
		{
			SimpleCoverage.cover("Failed to parse renderedPage from " + rwo.getName()); //$NON-NLS-1$
			Attributes dummyAttributes = new AttributesImpl();
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERROR,
					SchemaNames.EL_NSERROR, dummyAttributes);
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERRORDESC,
					SchemaNames.EL_NSERRORDESC, dummyAttributes);
			String s = Messages.getString("XSLTEntityHandler.46") //$NON-NLS-1$
					+ ex.getMessage();
			ch.characters(s.toCharArray(), 0, s.length());
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERRORDESC,
					SchemaNames.EL_NSERRORDESC);
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_RAWCONTENT,
					SchemaNames.EL_NSRAWCONTENT, dummyAttributes);
			ch.characters(renderedPage.toCharArray(), 0, renderedPage.length());
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_RAWCONTENT,
					SchemaNames.EL_NSRAWCONTENT);
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERROR,
					SchemaNames.EL_NSERROR);

		}

		// SimpleCoverage.cover("Failed to parse ::\n" + renderedPage
		// + "\n:: from ::\n" + rwo.getContent());
		// Attributes dummyAttributes = new AttributesImpl();
		// ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERROR,
		// SchemaNames.EL_NSERROR, dummyAttributes);
		// ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERRORDESC,
		// SchemaNames.EL_NSERRORDESC, dummyAttributes);
		// String s = "The Rendered Content did not parse correctly "
		// + ex.getMessage();
		// ch.characters(s.toCharArray(), 0, s.length());
		// ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERRORDESC,
		// SchemaNames.EL_NSERRORDESC);
		// ch.startElement(SchemaNames.NS_CONTAINER,
		// SchemaNames.EL_RAWCONTENT, SchemaNames.EL_NSRAWCONTENT,
		// dummyAttributes);
		// ch.characters(renderedPage.toCharArray(), 0, renderedPage.length());
		// ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_RAWCONTENT,
		// SchemaNames.EL_NSRAWCONTENT);
		// ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERROR,
		// SchemaNames.EL_NSERROR);

	}

	public void parseToSAX(final String toRender, final ContentHandler ch)
			throws IOException, SAXException
	{
		/**
		 * create a proxy for the stream, filtering out the start element and
		 * end element events
		 */
		ContentHandler proxy = new ContentHandler()
		{
			public void setDocumentLocator(Locator arg0)
			{
				ch.setDocumentLocator(arg0);
			}

			public void startDocument() throws SAXException
			{
				// ignore
			}

			public void endDocument() throws SAXException
			{
				// ignore
			}

			public void startPrefixMapping(String arg0, String arg1) throws SAXException
			{
				ch.startPrefixMapping(arg0, arg1);
			}

			public void endPrefixMapping(String arg0) throws SAXException
			{
				ch.endPrefixMapping(arg0);
			}

			public void startElement(String arg0, String arg1, String arg2,
					Attributes arg3) throws SAXException
			{
				ch.startElement(arg0, arg1, arg2, arg3);
			}

			public void endElement(String arg0, String arg1, String arg2)
					throws SAXException
			{
				ch.endElement(arg0, arg1, arg2);
			}

			public void characters(char[] arg0, int arg1, int arg2) throws SAXException
			{
				ch.characters(arg0, arg1, arg2);
			}

			public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
					throws SAXException
			{
				ch.ignorableWhitespace(arg0, arg1, arg2);
			}

			public void processingInstruction(String arg0, String arg1)
					throws SAXException
			{
				ch.processingInstruction(arg0, arg1);
			}

			public void skippedEntity(String arg0) throws SAXException
			{
				ch.skippedEntity(arg0);
			}

		};
		InputSource ins = new InputSource(new StringReader(toRender));
		XMLReader xmlReader;
		try
		{
			SAXParser saxParser = saxParserFactory.newSAXParser();

			xmlReader = saxParser.getXMLReader();
		}

		catch (Exception e)
		{
			log.error("SAXException when creating XMLReader", e); //$NON-NLS-1$
			// rethrow!!
			throw new SAXException(e);
		}
		xmlReader.setContentHandler(proxy);
		xmlReader.parse(ins);
	}

	public String render(RWikiObject rwo, boolean withBreadCrumb)
	{
		String localSpace = NameHelper.localizeSpace(rwo.getName(), rwo.getRealm());

 		// use site alias / mail archive alias here to render links with short URL
 		// localSpace pattern: /site/siteId
 		String localAliasSpace;
 		if (m_siteAlias) {
 			localAliasSpace = NameHelper.aliasSpace(localSpace);
 		} else {
 			localAliasSpace = localSpace;			
 		}
 		
 		ComponentPageLinkRenderImpl plr = new ComponentPageLinkRenderImpl(localAliasSpace, withBreadCrumb);
 

		plr.setAnchorURLFormat(anchorLinkFormat);
		plr.setStandardURLFormat(standardLinkFormat);
		plr.setBreadcrumbSwitch(breadCrumbParameterFormat );
		plr.setUrlFormat(hrefTagFormat);

		if (renderService == null)
		{
			// only for testing
			return rwo.getContent();
		}
		else
		{
			return renderService.renderPage(rwo, localSpace, plr);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getAuthzGroups(Reference ref, String userId)
	{
		// use the resources realm, all container (folder) realms

		Collection rv = new Vector();
		if (!isAvailable()) return rv;

		try
		{
			// try the resource, all the folders above it (don't include /)
			String paths[] = ref.getId().split(Entity.SEPARATOR);
			boolean container = ref.getId().endsWith(Entity.SEPARATOR);
			if (paths.length > 1)
			{

				StringBuffer root = new StringBuffer(Entity.SEPARATOR + paths[1] + Entity.SEPARATOR);
				// rv.add(root);

				List<String> al = new ArrayList<String>();
				for (int next = 2; next < paths.length; next++)
				{
					root.append(paths[next]);
					if ((next < paths.length - 1) || container)
					{
						root.append(Entity.SEPARATOR);
					}
					al.add(root.toString());
				}
				for (int i = al.size() - 1; i >= 0; i--)
				{
					// add in the sections authzgroup
					rv.add(authZPrefix + al.get(i));
				}

			}

			// special check for group-user : the grant's in the user's My
			// Workspace site
			String parts[] = ref.getId().split(Entity.SEPARATOR);
			if ((parts.length > 3) && ("group-user".equals(parts[1]))) //$NON-NLS-1$
			{
				rv.add(SiteService.siteReference(SiteService.getUserSiteId(parts[3])));
			}
			// . how do we get a section by ID, I assume that the
			if (paths.length > 4
					&& ("group".equals(paths[3]) || "section".equals(paths[3]))) //$NON-NLS-1$ //$NON-NLS-2$
			{
				// paths 2 is the site id, which will be of the same form as a
				// group id
				String[] testuuid = paths[2].split("-"); //$NON-NLS-1$
				String[] uuidparts = paths[4].split("-"); //$NON-NLS-1$
				boolean isuuid = false;
				String groupID = Entity.SEPARATOR + paths[1] + Entity.SEPARATOR
						+ paths[2] + Entity.SEPARATOR + paths[3] + Entity.SEPARATOR
						+ paths[4];
				if (testuuid.length > 0 && testuuid.length == uuidparts.length)
				{
					isuuid = true;
					for (int i = 0; i < testuuid.length; i++)
					{
						if (testuuid[i].length() != uuidparts[i].length())
						{
							isuuid = false;
						}
					}

				}
				if (!isuuid)
				{
					// could be a section name
					Reference siteRef = entityManager.newReference(ref.getContext());
					Site s = (Site) siteRef.getEntity();
					Collection l = s.getGroups();
					for (Iterator is = l.iterator(); is.hasNext();)
					{
						Group g = (Group) is.next();
						if (paths[4].equalsIgnoreCase(g.getTitle()))
						{
							groupID = g.getId();
							log.debug("Found Match " + groupID); //$NON-NLS-1$
							break;
						}
					}
					log.debug("Converted ID " + groupID); //$NON-NLS-1$

				}
				else
				{
					log.debug("Raw ID " + groupID); //$NON-NLS-1$
				}
				rv.add(groupID);

			}

			// TODO: At the moment we cant use ref.addSiteContextAuthzGroup
			// since ref context is
			// /site/siteid and should be siteid need to look into this
			// 
			// site
			rv.add(SiteService.siteReference(parts[2]));

			// site helper
			rv.add("!site.helper"); //$NON-NLS-1$
		}
		catch (Throwable e)
		{
			log.error(this + " Problem ", e); //$NON-NLS-1$
		}

		return rv;
	}

	/**
	 * called by spring.
	 */
	public void init()
	{
		if (!isAvailable()) return;
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager.getInstance();
		entityManager = (EntityManager) load(cm, EntityManager.class.getName());
		renderService = (RenderService) load(cm, RenderService.class.getName());

		saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);
		try
		{
			XSLTTransform xsltTransform = new XSLTTransform();
			xsltTransform.setXslt(new InputSource(this.getClass().getResourceAsStream(xslt)));
			xsltTransform.getContentHandler();
		}
		catch (Exception ex)
		{
			log.error("Please check that the xslt is in the classpath " //$NON-NLS-1$
					+ xslt,ex);
			throw new RuntimeException(
					"Failed to initialise XSLTTransformer context with xslt " //$NON-NLS-1$
							+ xslt, ex);
		}
	}

	/**
	 * called by spring.
	 */
	public void destroy()
	{
	}

	/**
	 * @see uk.co.tfd.sakai.xmlserver.api.OutputContentHandler#getOutputHandler(java.io.Writer)
	 */

	public ContentHandler getOutputHandler(Writer out) throws IOException
	{
		throw new RuntimeException("Method Not In Use ");
		/*
		if (!isAvailable()) return null;
		try
		{
			XSLTTransform xsltTransform = (XSLTTransform) transformerHolder.get();
			if (xsltTransform == null)
			{
				xsltTransform = new XSLTTransform();
				xsltTransform.setXslt(new InputSource(this.getClass()
						.getResourceAsStream(xslt)));
				transformerHolder.set(xsltTransform);
			}
			SAXResult sr = new SAXResult();
			TransformerHandler th = xsltTransform.getContentHandler();
			Properties p = OutputPropertiesFactory.getDefaultMethodProperties("xml");
			p.putAll(outputProperties);
			
			Serializer s = SerializerFactory.getSerializer(p);
			s.setWriter(out);
			sr.setHandler(s.asContentHandler());
			th.setResult(sr);
			return th;
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to create Content Handler", ex); //$NON-NLS-1$
			/*
			 * String stackTrace = null; try { StringWriter exw = new
			 * StringWriter(); PrintWriter pw = new PrintWriter(exw);
			 * log.error(ex.getMessage(), ex); stackTrace = exw.toString(); } catch
			 * (Exception ex2) { stackTrace =
			 * MessageFormat.format(defaultStackTrace, new Object[] {
			 * ex.getMessage() }); } out.write(MessageFormat.format(errorFormat,
			 * new Object[] { ex.getMessage(), stackTrace }));
			 * /
		}
	    */
	}

	public ContentHandler getOutputHandler(OutputStream out) throws IOException
	{
		if (!isAvailable()) return null;

		try
		{
			XSLTTransform xsltTransform = (XSLTTransform) transformerHolder.get();
			if (xsltTransform == null)
			{
				xsltTransform = new XSLTTransform();
				xsltTransform.setXslt(new InputSource(this.getClass()
						.getResourceAsStream(xslt)));
				transformerHolder.set(xsltTransform);
			}
			SAXResult sr = new SAXResult();
			
			TransformerHandler th = xsltTransform.getContentHandler();
			
			Transformer transformer = th.getTransformer();
			if (transformParameters != null) {
				for (Map.Entry<String, String> entry: transformParameters.entrySet()) {
					transformer.setParameter(entry.getKey(), entry.getValue());
				}
			}

			Properties p = OutputPropertiesFactory.getDefaultMethodProperties("xml");
		
			// SAK-14388 - use the alternate XHTMLSerializer2 for Websphere environments
			if ("websphere".equals(ServerConfigurationService.getString("servlet.container")))
			{
				// SAK-16712: null in java.util.Properties causes NullPointerException
				if (getXalan270ContentHandler() != null )
				{
					outputProperties.put("{http://xml.apache.org/xalan}content-handler", getXalan270ContentHandler());
				}
			}
			p.putAll(outputProperties);
			
			/*
			S_KEY_CONTENT_HANDLER:{http://xml.apache.org/xalan}content-handler
				S_KEY_ENTITIES:{http://xml.apache.org/xalan}entities
				S_KEY_INDENT_AMOUNT:{http://xml.apache.org/xalan}indent-amount
				S_OMIT_META_TAG:{http://xml.apache.org/xalan}omit-meta-tag
				S_USE_URL_ESCAPING:{http://xml.apache.org/xalan}use-url-escaping
			*/
			
			Serializer s = SerializerFactory.getSerializer(p);
			s.setOutputStream(out);
			sr.setHandler(s.asContentHandler());
			th.setResult(sr);
			return th;
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to create Content Handler", ex); //$NON-NLS-1$
			/*
			 * String stackTrace = null; try { StringWriter exw = new
			 * StringWriter(); PrintWriter pw = new PrintWriter(exw);
			 * log.error(ex.getMessage(), ex); stackTrace = exw.toString(); } catch
			 * (Exception ex2) { stackTrace =
			 * MessageFormat.format(defaultStackTrace, new Object[] {
			 * ex.getMessage() }); } out.write(MessageFormat.format(errorFormat,
			 * new Object[] { ex.getMessage(), stackTrace }));
			 */
		}
	}

	// Need to configure components correctly.

	/**
	 * @return Returns the xslt.
	 */
	public String getXslt()
	{
		return xslt;
	}

	/**
	 * @param xslt
	 *        The xslt to set.
	 */
	public void setXslt(final String xslt)
	{
		this.xslt = xslt;
	}

	/**
	 * @return Returns the defaultStackTrace.
	 */
	public String getDefaultStackTrace()
	{
		return defaultStackTrace;
	}

	/**
	 * @param defaultStackTrace
	 *        The defaultStackTrace to set.
	 */
	public void setDefaultStackTrace(final String defaultStackTrace)
	{
		this.defaultStackTrace = defaultStackTrace;
	}

	/**
	 * @return Returns the errorFormat.
	 */
	public String getErrorFormat()
	{
		return errorFormat;
	}

	/**
	 * @param errorFormat
	 *        The errorFormat to set.
	 */
	public void setErrorFormat(final String errorFormat)
	{
		this.errorFormat = errorFormat;
	}

	/**
	 * @return Returns the authZPrefix.
	 */
	public String getAuthZPrefix()
	{
		return authZPrefix;
	}

	/**
	 * @param authZPrefix
	 *        The authZPrefix to set.
	 */
	public void setAuthZPrefix(String authZPrefix)
	{
		this.authZPrefix = authZPrefix;
	}

	/**
	 * @return Returns the anchorLinkFormat.
	 */
	public String getAnchorLinkFormat()
	{
		return anchorLinkFormat;
	}

	/**
	 * @param anchorLinkFormat
	 *        The anchorLinkFormat to set.
	 */
	public void setAnchorLinkFormat(String anchorLinkFormat)
	{
		this.anchorLinkFormat = anchorLinkFormat;
	}

	/**
	 * @return Returns the hrefTagFormat.
	 */
	public String getHrefTagFormat()
	{
		return hrefTagFormat;
	}

	/**
	 * @param hrefTagFormat
	 *        The hrefTagFormat to set.
	 */
	public void setHrefTagFormat(String hrefTagFormat)
	{
		this.hrefTagFormat = hrefTagFormat;
	}

	/**
	 * @return Returns the standardLinkFormat.
	 */
	public String getStandardLinkFormat()
	{
		return standardLinkFormat;
	}

	/**
	 * @param standardLinkFormat
	 *        The standardLinkFormat to set.
	 */
	public void setStandardLinkFormat(String standardLinkFormat)
	{
		this.standardLinkFormat = standardLinkFormat;
	}

	/**
	 * @return Returns the outputProperties.
	 */
	public Map getOutputProperties()
	{
		return outputProperties;
	}

	/**
	 * @param outputProperties
	 *        The outputProperties to set.
	 */
	public void setOutputProperties(Map outputProperties)
	{
		this.outputProperties = outputProperties;
	}

	/**
	 * @param transformParameters
	 *        The transform paramaters that should be passed when transforming it.
	 */
	public void setTransformParameters(Map<String,String> transformParameters)
	{
		this.transformParameters = transformParameters;
	}

	/**
	 * @return Returns the responseHeaders.
	 */
	public Map getResponseHeaders()
	{
		return responseHeaders;
	}

	/**
	 * @param responseHeaders
	 *        The responseHeaders to set.
	 */
	public void setResponseHeaders(Map responseHeaders)
	{
		this.responseHeaders = responseHeaders;
	}

	public void addRequestAttributes(ContentHandler ch, HttpServletRequest request)
			throws Exception
	{
		if (!isAvailable()) return;

		// add the attributes
		AttributesImpl dummyAttributes = new AttributesImpl();
		ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_ATTRIBUTES,
				SchemaNames.EL_NSREQUEST_ATTRIBUTES, dummyAttributes);
		for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();)
		{
			String name = (String) e.nextElement();
			Object attr = request.getAttribute(name);
			AttributesImpl propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
					SchemaNames.ATTR_NAME, "string", name); //$NON-NLS-1$
			if (attr instanceof Object[])
			{
				Object[] oattr = (Object[]) attr;
				ch.startElement(SchemaNames.NS_CONTAINER,
						SchemaNames.EL_REQUEST_ATTRIBUTE,
						SchemaNames.EL_NSREQUEST_ATTRIBUTE, propA);
				for (int i = 0; i < oattr.length; i++)
				{
					addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_VALUE,
							SchemaNames.EL_NSVALUE, dummyAttributes, oattr[i]);
				}
				ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_ATTRIBUTE,
						SchemaNames.EL_NSREQUEST_ATTRIBUTE);
			}
			else
			{
				ch.startElement(SchemaNames.NS_CONTAINER,
						SchemaNames.EL_REQUEST_ATTRIBUTE,
						SchemaNames.EL_NSREQUEST_ATTRIBUTE, propA);
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_VALUE,
						SchemaNames.EL_NSVALUE, dummyAttributes, attr);
				ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_ATTRIBUTE,
						SchemaNames.EL_NSREQUEST_ATTRIBUTE);
			}
		}

		ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_ATTRIBUTES,
				SchemaNames.EL_NSREQUEST_ATTRIBUTES);
	}

	public void addRequestParameters(ContentHandler ch, HttpServletRequest request)
			throws Exception
	{
		if (!isAvailable()) return;

		AttributesImpl dummyAttributes = new AttributesImpl();

		// add the request parameters
		ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_PARAMS,
				SchemaNames.EL_NSREQUEST_PARAMS, dummyAttributes);
		for (Enumeration e = request.getParameterNames(); e.hasMoreElements();)
		{
			String name = (String) e.nextElement();
			String[] attr = request.getParameterValues(name);
			AttributesImpl propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
					SchemaNames.ATTR_NAME, "string", name); //$NON-NLS-1$
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_PARAM,
					SchemaNames.EL_NSREQUEST_PARAM, propA);
			for (int i = 0; i < attr.length; i++)
			{
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_VALUE,
						SchemaNames.EL_NSVALUE, dummyAttributes, attr[i]);
			}
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_PARAM,
					SchemaNames.EL_NSREQUEST_PARAM);
		}

		ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_PARAMS,
				SchemaNames.EL_REQUEST_PARAMS);
	}

	/**
	 * @return Returns the entityManager.
	 */
	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	/**
	 * @param entityManager
	 *        The entityManager to set.
	 */
	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

	/**
	 * @return the breadCrumbParameterFormat
	 */
	public String getBreadCrumbParameterFormat()
	{
		return breadCrumbParameterFormat;
	}

	/**
	 * @param breadCrumbParameterFormat the breadCrumbParameterFormat to set
	 */
	public void setBreadCrumbParameterFormat(String breadCrumbParameterFormat)
	{
		this.breadCrumbParameterFormat = breadCrumbParameterFormat;
	}

	/**
	 * @return the breadCrumbParameter
	 */
	public String getBreadCrumbParameter()
	{
		return breadCrumbParameter;
	}

	/**
	 * @param breadCrumbParameter the breadCrumbParameter to set
	 */
	public void setBreadCrumbParameter(String breadCrumbParameter)
	{
		this.breadCrumbParameter = breadCrumbParameter;
	}
	
	/**
	 * @return the xalan270ContentHandler
	 */
	public String getXalan270ContentHandler()
	{
		return xalan270ContentHandler;
	}

	/**
	 * @param xalan270ContentHandler
	 *        the xalan270ContentHandler to set
	 */
	public void setXalan270ContentHandler(String xalan270ContentHandler)
	{
		this.xalan270ContentHandler = xalan270ContentHandler;
	}
    
    public boolean isEscaped() {
        return escaped;
    }
    
    public void setEscaped(boolean escaped) {
        this.escaped = escaped;
    }
	
}
