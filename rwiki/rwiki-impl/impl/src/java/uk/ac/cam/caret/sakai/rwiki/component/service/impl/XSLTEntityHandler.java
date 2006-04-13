/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.site.cover.SiteService;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;

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
public class XSLTEntityHandler extends BaseEntityHandlerImpl
{
	private static Log log = LogFactory.getLog(XSLTEntityHandler.class);

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
	private RenderService renderService = null;

	/**
	 * dependency
	 */
	private String authZPrefix = "";

	/**
	 * dependency The base name of the xslt file, relative to context root.
	 */
	private String xslt = null;

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
	public void outputContent(final Entity entity,
			final HttpServletRequest request, final HttpServletResponse res)
	{
		if (!isAvailable()) return;
		if (!(entity instanceof RWikiEntity)) return;

		try
		{
			setCurrentRequest(request);

			if (responseHeaders != null)
			{
				for (Iterator i = responseHeaders.keySet().iterator(); i
						.hasNext();)
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
			ch.startElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_ENTITYSERVICE,
					SchemaNames.EL_NSENTITYSERVICE, dummyAttributes);

			{
				AttributesImpl propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_REQUEST_PATH_INFO,
						SchemaNames.ATTR_REQUEST_PATH_INFO, "sting", request
								.getPathInfo());
				propA.addAttribute("", SchemaNames.ATTR_REQUEST_USER,
						SchemaNames.ATTR_REQUEST_USER, "sting", request
								.getRemoteUser());
				propA.addAttribute("", SchemaNames.ATTR_REQUEST_PROTOCOL,
						SchemaNames.ATTR_REQUEST_PROTOCOL, "sting", request
								.getProtocol());
				propA.addAttribute("", SchemaNames.ATTR_REQUEST_SERVER_NAME,
						SchemaNames.ATTR_REQUEST_SERVER_NAME, "sting", request
								.getServerName());
				propA.addAttribute("", SchemaNames.ATTR_REQUEST_SERVER_PORT,
						SchemaNames.ATTR_REQUEST_SERVER_PORT, "sting", String
								.valueOf(request.getServerPort()));
				propA.addAttribute("", SchemaNames.ATTR_REQUEST_REQUEST_URL,
						SchemaNames.ATTR_REQUEST_REQUEST_URL, "sting", String
								.valueOf(request.getRequestURL()));

				ch.startElement(SchemaNames.NS_CONTAINER,
						SchemaNames.EL_REQUEST_PROPERTIES,
						SchemaNames.EL_NSREQUEST_PROPERTIES, propA);
			}
			addRequestAttributes(ch, request);
			addRequestParameters(ch, request);

			ch.endElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_REQUEST_PROPERTIES,
					SchemaNames.EL_NSREQUEST_PROPERTIES);
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ENTITY,
					SchemaNames.EL_NSENTITY, dummyAttributes);
			ch.startElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_XMLPROPERTIES,
					SchemaNames.EL_NSXMLPROPERTIES, dummyAttributes);
			ResourceProperties rp = entity.getProperties();

			for (Iterator i = rp.getPropertyNames(); i.hasNext();)
			{
				Object key = i.next();
				String name = String.valueOf(key);
				String value = String.valueOf(rp.getProperty(name));
				AttributesImpl propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_NAME,
						SchemaNames.ATTR_NAME, "string", name);
				addElement(ch, SchemaNames.NS_CONTAINER,
						SchemaNames.EL_XMLPROPERTY,
						SchemaNames.EL_NSXMLPROPERTY, propA, value);
			}
			{
				AttributesImpl propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_NAME,
						SchemaNames.ATTR_NAME, "string", "_handler");
				addElement(ch, SchemaNames.NS_CONTAINER,
						SchemaNames.EL_XMLPROPERTY,
						SchemaNames.EL_NSXMLPROPERTY, propA,
						" XSLTEntity Handler");
			}
			if (entity instanceof RWikiEntity)
			{
				RWikiEntity rwe = (RWikiEntity) entity;
				if (!rwe.isContainer())
				{
					RWikiObject rwo = rwe.getRWikiObject();
					AttributesImpl propA = new AttributesImpl();
					propA.addAttribute("", SchemaNames.ATTR_NAME,
							SchemaNames.ATTR_NAME, "string", "_title");
					addElement(ch, SchemaNames.NS_CONTAINER,
							SchemaNames.EL_XMLPROPERTY,
							SchemaNames.EL_NSXMLPROPERTY, propA,
							NameHelper.localizeName(rwo.getName(), rwo
									.getRealm()));
				}
				else
				{
					AttributesImpl propA = new AttributesImpl();
					propA.addAttribute("", SchemaNames.ATTR_NAME,
							SchemaNames.ATTR_NAME, "string", "_title");
					addElement(ch, SchemaNames.NS_CONTAINER,
							SchemaNames.EL_XMLPROPERTY,
							SchemaNames.EL_NSXMLPROPERTY, propA, entity
									.getReference());

				}
			}
			ch.endElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_XMLPROPERTIES,
					SchemaNames.EL_NSXMLPROPERTIES);

			if (entity instanceof RWikiEntity)
			{
				RWikiEntity rwe = (RWikiEntity) entity;
				if (!rwe.isContainer())
				{
					RWikiObject rwo = rwe.getRWikiObject();
					ch.startElement(SchemaNames.NS_CONTAINER,
							SchemaNames.EL_RENDEREDCONTENT,
							SchemaNames.EL_NSRENDEREDCONTENT, dummyAttributes);
					renderToXML(rwo, ch);
					ch.endElement(SchemaNames.NS_CONTAINER,
							SchemaNames.EL_RENDEREDCONTENT,
							SchemaNames.EL_NSRENDEREDCONTENT);
				}
			}
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ENTITY,
					SchemaNames.EL_NSENTITY);
			ch.endElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_ENTITYSERVICE,
					SchemaNames.EL_NSENTITYSERVICE);

			ch.endDocument();

		}
		catch (Throwable ex)
		{
			log.info("Failed to serialize " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException("Failed to serialise "
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
	public void addElement(final ContentHandler ch, final String ns,
			final String lname, final String qname, final Attributes attr,
			final Object content) throws SAXException
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
	 */
	public void renderToXML(RWikiObject rwo, final ContentHandler ch)
			throws SAXException, IOException
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

			public void startPrefixMapping(String arg0, String arg1)
					throws SAXException
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

			public void characters(char[] arg0, int arg1, int arg2)
					throws SAXException
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

		String localSpace = NameHelper.localizeSpace(rwo.getName(), rwo
				.getRealm());
		ComponentPageLinkRenderImpl plr = new ComponentPageLinkRenderImpl(
				localSpace);

		plr.setAnchorURLFormat(anchorLinkFormat);
		plr.setStandardURLFormat(standardLinkFormat);
		plr.setUrlFormat(hrefTagFormat);

		String renderedPage = null;
		if (renderService == null)
		{
			// only for testing
			renderedPage = rwo.getContent();
		}
		else
		{
			renderedPage = renderService.renderPage(rwo, localSpace, plr);
		}
		String contentDigest = DigestHtml.digest(renderedPage);
		if (contentDigest.length() > 500)
		{
			contentDigest = contentDigest.substring(0, 500);
		}
		if (renderedPage == null || renderedPage.trim().length() == 0)
		{
			renderedPage = "no content on page";
		}
		if (contentDigest == null || contentDigest.trim().length() == 0)
		{
			contentDigest = "no content on page";
		}

		renderedPage = "<content><rendered>" + renderedPage
				+ "</rendered><contentdigest>" + contentDigest
				+ "</contentdigest></content>";
		InputSource ins = new InputSource(new StringReader(renderedPage));
		XMLReader xmlReader = XMLReaderFactory
				.createXMLReader("org.apache.xerces.parsers.SAXParser");
		xmlReader.setContentHandler(proxy);
		try
		{
			xmlReader.parse(ins);
		}
		catch (Throwable ex)
		{

			SimpleCoverage.cover("Failed to parse ::\n" + renderedPage
					+ "\n:: from ::\n" + rwo.getContent());
			Attributes dummyAttributes = new AttributesImpl();
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERROR,
					SchemaNames.EL_NSERROR, dummyAttributes);
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERRORDESC,
					SchemaNames.EL_NSERRORDESC, dummyAttributes);
			String s = "The Rendered Content did not parse correctly "
					+ ex.getMessage();
			ch.characters(s.toCharArray(), 0, s.length());
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERRORDESC,
					SchemaNames.EL_NSERRORDESC);
			ch.startElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_RAWCONTENT, SchemaNames.EL_NSRAWCONTENT,
					dummyAttributes);
			ch.characters(renderedPage.toCharArray(), 0, renderedPage.length());
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_RAWCONTENT,
					SchemaNames.EL_NSRAWCONTENT);
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ERROR,
					SchemaNames.EL_NSERROR);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getAuthzGroups(Reference ref)
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

				String root = Entity.SEPARATOR + paths[1] + Entity.SEPARATOR;
				// rv.add(root);

				List al = new ArrayList();
				for (int next = 2; next < paths.length; next++)
				{
					root = root + paths[next];
					if ((next < paths.length - 1) || container)
					{
						root = root + Entity.SEPARATOR;
					}
					al.add(root);
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
			if ((parts.length > 3) && (parts[1].equals("group-user")))
			{
				rv.add(SiteService.siteReference(SiteService
						.getUserSiteId(parts[3])));
			}
			// . how do we get a section by ID, I assume that the
			if (paths.length > 4
					&& ("group".equals(paths[3]) || "section".equals(paths[3])))
			{
				// paths 2 is the site id, which will be of the same form as a
				// group id
				String[] testuuid = paths[2].split("-");
				String[] uuidparts = paths[4].split("-");
				boolean isuuid = false;
				String sectionID = Entity.SEPARATOR + paths[1]
						+ Entity.SEPARATOR + paths[2] + Entity.SEPARATOR
						+ paths[3] + Entity.SEPARATOR + paths[4];
				;
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
					Reference siteRef = EntityManager.newReference(ref
							.getContext());
					List l = SectionAwareness.getSections(siteRef.getId());
					for (Iterator is = l.iterator(); is.hasNext();)
					{
						CourseSection cs = (CourseSection) is.next();
						if (paths[4].equalsIgnoreCase(cs.getTitle()))
						{
							sectionID = cs.getUuid();
							log.debug("Found Match " + sectionID);
							break;
						}
					}
					log.debug("Converted ID " + sectionID);

				}
				else
				{
					log.debug("Raw ID " + sectionID);
				}
				rv.add(sectionID);

			}

			// TODO: At the moment we cant use ref.addSiteContextAuthzGroup
			// since ref context is
			// /site/siteid and should be siteid need to look into this
			// 
			// site
			rv.add(SiteService.siteReference(parts[2]));

			// site helper
			rv.add("!site.helper");
		}
		catch (Throwable e)
		{
			log.error(this + " Problem ", e);
		}

		return rv;
	}

	/**
	 * called by spring.
	 */
	public void init()
	{
		if (!isAvailable()) return;
		try
		{
			XSLTTransform xsltTransform = new XSLTTransform();
			xsltTransform.setXslt(new InputSource(this.getClass()
					.getResourceAsStream(xslt)));
			xsltTransform.getContentHandler();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.err
					.println("Please check that the xslt is in the classpath "
							+ xslt);
			throw new RuntimeException(
					"Failed to initialise XSLTTransformer context with xslt "
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
		if (!isAvailable()) return null;
		try
		{
			XSLTTransform xsltTransform = (XSLTTransform) transformerHolder
					.get();
			if (xsltTransform == null)
			{
				xsltTransform = new XSLTTransform();
				xsltTransform.setXslt(new InputSource(this.getClass()
						.getResourceAsStream(xslt)));
				transformerHolder.set(xsltTransform);
			}
			ContentHandler ch = xsltTransform.getOutputHandler(out,
					outputProperties);
			return ch;
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to create Content Handler", ex);
			/*
			 * String stackTrace = null; try { StringWriter exw = new
			 * StringWriter(); PrintWriter pw = new PrintWriter(exw);
			 * ex.printStackTrace(pw); stackTrace = exw.toString(); } catch
			 * (Exception ex2) { stackTrace =
			 * MessageFormat.format(defaultStackTrace, new Object[] {
			 * ex.getMessage() }); } out.write(MessageFormat.format(errorFormat,
			 * new Object[] { ex.getMessage(), stackTrace }));
			 */
		}
	}

	public ContentHandler getOutputHandler(OutputStream out) throws IOException
	{
		if (!isAvailable()) return null;

		try
		{
			XSLTTransform xsltTransform = (XSLTTransform) transformerHolder
					.get();
			if (xsltTransform == null)
			{
				xsltTransform = new XSLTTransform();
				xsltTransform.setXslt(new InputSource(this.getClass()
						.getResourceAsStream(xslt)));
				transformerHolder.set(xsltTransform);
			}
			ContentHandler ch = xsltTransform.getOutputHandler(out,
					outputProperties);
			return ch;
		}
		catch (Exception ex)
		{
			// XXX Hmm I don't like this but I'm not sure what the correct way
			// to handle this is
			throw new RuntimeException("Failed to create Content Handler", ex);
			/*
			 * String stackTrace = null; try { StringWriter exw = new
			 * StringWriter(); PrintWriter pw = new PrintWriter(exw);
			 * ex.printStackTrace(pw); stackTrace = exw.toString(); } catch
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
	 * @return Returns the renderService.
	 */
	public RenderService getRenderService()
	{
		return renderService;
	}

	/**
	 * @param renderService
	 *        The renderService to set.
	 */
	public void setRenderService(RenderService renderService)
	{
		this.renderService = renderService;
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

	public void addRequestAttributes(ContentHandler ch,
			HttpServletRequest request) throws Exception
	{
		if (!isAvailable()) return;

		// add the attributes
		AttributesImpl dummyAttributes = new AttributesImpl();
		ch.startElement(SchemaNames.NS_CONTAINER,
				SchemaNames.EL_REQUEST_ATTRIBUTES,
				SchemaNames.EL_NSREQUEST_ATTRIBUTES, dummyAttributes);
		for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();)
		{
			String name = (String) e.nextElement();
			Object attr = request.getAttribute(name);
			AttributesImpl propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_NAME,
					SchemaNames.ATTR_NAME, "string", name);
			if (attr instanceof Object[])
			{
				Object[] oattr = (Object[]) attr;
				ch.startElement(SchemaNames.NS_CONTAINER,
						SchemaNames.EL_REQUEST_ATTRIBUTE,
						SchemaNames.EL_NSREQUEST_ATTRIBUTE, propA);
				for (int i = 0; i < oattr.length; i++)
				{
					addElement(ch, SchemaNames.NS_CONTAINER,
							SchemaNames.EL_VALUE, SchemaNames.EL_NSVALUE,
							dummyAttributes, oattr[i]);
				}
				ch.endElement(SchemaNames.NS_CONTAINER,
						SchemaNames.EL_REQUEST_ATTRIBUTE,
						SchemaNames.EL_NSREQUEST_ATTRIBUTE);
			}
			else
			{
				ch.startElement(SchemaNames.NS_CONTAINER,
						SchemaNames.EL_REQUEST_ATTRIBUTE,
						SchemaNames.EL_NSREQUEST_ATTRIBUTE, propA);
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_VALUE,
						SchemaNames.EL_NSVALUE, dummyAttributes, attr);
				ch.endElement(SchemaNames.NS_CONTAINER,
						SchemaNames.EL_REQUEST_ATTRIBUTE,
						SchemaNames.EL_NSREQUEST_ATTRIBUTE);
			}
		}

		ch.endElement(SchemaNames.NS_CONTAINER,
				SchemaNames.EL_REQUEST_ATTRIBUTES,
				SchemaNames.EL_NSREQUEST_ATTRIBUTES);
	}

	public void addRequestParameters(ContentHandler ch,
			HttpServletRequest request) throws Exception
	{
		if (!isAvailable()) return;

		AttributesImpl dummyAttributes = new AttributesImpl();

		// add the request parameters
		ch.startElement(SchemaNames.NS_CONTAINER,
				SchemaNames.EL_REQUEST_PARAMS, SchemaNames.EL_NSREQUEST_PARAMS,
				dummyAttributes);
		for (Enumeration e = request.getParameterNames(); e.hasMoreElements();)
		{
			String name = (String) e.nextElement();
			String[] attr = request.getParameterValues(name);
			AttributesImpl propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_NAME,
					SchemaNames.ATTR_NAME, "string", name);
			ch.startElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_REQUEST_PARAM,
					SchemaNames.EL_NSREQUEST_PARAM, propA);
			for (int i = 0; i < attr.length; i++)
			{
				addElement(ch, SchemaNames.NS_CONTAINER, SchemaNames.EL_VALUE,
						SchemaNames.EL_NSVALUE, dummyAttributes, attr[i]);
			}
			ch.endElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_REQUEST_PARAM,
					SchemaNames.EL_NSREQUEST_PARAM);
		}

		ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_REQUEST_PARAMS,
				SchemaNames.EL_REQUEST_PARAMS);
	}
}
