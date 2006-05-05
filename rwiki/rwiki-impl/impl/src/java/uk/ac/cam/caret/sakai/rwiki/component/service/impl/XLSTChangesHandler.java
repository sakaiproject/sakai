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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.DebugContentHandler;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.SchemaNames;

/**
 * Provides a XSLT Based handler, that outputs the changes on the object.
 * 
 * @author ieb
 */
public class XLSTChangesHandler extends XSLTEntityHandler
{
	private RWikiObjectService rwikObjectService = null;

	/**
	 * {@inheritDoc}
	 */
	public void outputContent(Entity entity, HttpServletRequest request,
			HttpServletResponse res)
	{
		if (!isAvailable()) return;

		if (!(entity instanceof RWikiEntity)) return;

		try
		{

			Map rheaders = getResponseHeaders();
			if (rheaders != null)
			{
				for (Iterator i = rheaders.keySet().iterator(); i.hasNext();)
				{
					String name = (String) i.next();
					String value = (String) rheaders.get(name);
					res.setHeader(name, value);

				}

			}
			ContentHandler opch = getOutputHandler(res.getOutputStream());
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

				propA.addAttribute("", SchemaNames.ATTR_SERVER_URL,
						SchemaNames.ATTR_SERVER_URL, "sting",
						ServerConfigurationService.getServerUrl());

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
			{
				AttributesImpl propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_NAME,
						SchemaNames.ATTR_NAME, "string", "_description");
				addElement(ch, SchemaNames.NS_CONTAINER,
						SchemaNames.EL_XMLPROPERTY,
						SchemaNames.EL_NSXMLPROPERTY, propA,
						ServerConfigurationService.getString("ui.service"));

			}
			{
				AttributesImpl propA = new AttributesImpl();
				propA.addAttribute("", SchemaNames.ATTR_NAME,
						SchemaNames.ATTR_NAME, "string", "_datestamp");
				// 2006-02-16T18:28:03+01:00
				SimpleDateFormat sd = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ssZ");

				addElement(ch, SchemaNames.NS_CONTAINER,
						SchemaNames.EL_XMLPROPERTY,
						SchemaNames.EL_NSXMLPROPERTY, propA, sd
								.format(new Date()));

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
					ch.startElement(SchemaNames.NS_CONTAINER,
							SchemaNames.EL_CHANGES, SchemaNames.EL_NSCHANGES,
							dummyAttributes);
					changeHistoryToXML(rwo, ch);
					ch.endElement(SchemaNames.NS_CONTAINER,
							SchemaNames.EL_CHANGES, SchemaNames.EL_NSCHANGES);

				}
				else
				{
					ch.startElement(SchemaNames.NS_CONTAINER,
							SchemaNames.EL_CHANGES, SchemaNames.EL_NSCHANGES,
							dummyAttributes);
					recentChangesToXML(rwe, ch);
					ch.endElement(SchemaNames.NS_CONTAINER,
							SchemaNames.EL_CHANGES, SchemaNames.EL_NSCHANGES);

				}

			}

			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_ENTITY,
					SchemaNames.EL_NSXMLSERVICE);
			ch.endElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_ENTITYSERVICE, SchemaNames.EL_NSXMLSERVICE);

			ch.endDocument();

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException("Failed to serialise "
					+ ex.getLocalizedMessage(), ex);
		}
	}

	public void changeHistoryToXML(RWikiObject rwo, ContentHandler ch)
			throws Exception
	{
		if (!isAvailable()) return;

		List changes = rwikObjectService.findRWikiHistoryObjectsInReverse(rwo);
		if (changes == null) return;
		for (Iterator i = changes.iterator(); i.hasNext();)
		{
			RWikiHistoryObject rwco = (RWikiHistoryObject) i.next();
			AttributesImpl propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_ID, SchemaNames.ATTR_ID,
					"sting", rwco.getId());
			propA.addAttribute("", SchemaNames.ATTR_NAME,
					SchemaNames.ATTR_NAME, "sting", rwco.getName());
			propA.addAttribute("", SchemaNames.ATTR_LOCAL_NAME,
					SchemaNames.ATTR_LOCAL_NAME, "sting", NameHelper
							.localizeName(rwo.getName(), rwo.getRealm()));
			propA.addAttribute("", SchemaNames.ATTR_OWNER,
					SchemaNames.ATTR_OWNER, "sting", rwco.getOwner());
			propA.addAttribute("", SchemaNames.ATTR_OWNER,
					SchemaNames.ATTR_REALM, "sting", rwco.getRealm());
			propA.addAttribute("", SchemaNames.ATTR_REFERENCED,
					SchemaNames.ATTR_REFERENCED, "sting", rwco.getReferenced());
			propA.addAttribute("", SchemaNames.ATTR_SHA1,
					SchemaNames.ATTR_SHA1, "sting", rwco.getSha1());
			propA.addAttribute("", SchemaNames.ATTR_USER,
					SchemaNames.ATTR_USER, "sting", rwco.getUser());
			propA.addAttribute("", SchemaNames.ATTR_REVISION,
					SchemaNames.ATTR_REVISION, "sting", String.valueOf(rwco
							.getRevision()));
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			propA.addAttribute("", SchemaNames.ATTR_LAST_CHANGE,
					SchemaNames.ATTR_LAST_CHANGE, "sting", sd.format(rwco
							.getVersion()));

			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
					SchemaNames.EL_NSCHANGE, propA);
			renderToXML(rwco, ch);
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
					SchemaNames.EL_NSCHANGE);
		}

	}

	public void recentChangesToXML(RWikiEntity rwe, ContentHandler ch)
			throws Exception
	{
		if (!isAvailable()) return;

		GregorianCalendar g = new GregorianCalendar();
		g.setTime(new Date());
		g.add(GregorianCalendar.YEAR, -1);

		Decoded d = decode(rwe.getReference() + getMinorType());
		String basepath = d.getContext() + d.getContainer();
		List changes = rwikObjectService.findAllChangedSince(g.getTime(),
				basepath);
		int nchanges = 0;
		for (Iterator i = changes.iterator(); i.hasNext() && nchanges < 20;)
		{
			nchanges++;
			RWikiCurrentObject rwco = (RWikiCurrentObject) i.next();
			AttributesImpl propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_ID, SchemaNames.ATTR_ID,
					"sting", rwco.getId());
			propA.addAttribute("", SchemaNames.ATTR_NAME,
					SchemaNames.ATTR_NAME, "sting", rwco.getName());
			propA.addAttribute("", SchemaNames.ATTR_LOCAL_NAME,
					SchemaNames.ATTR_LOCAL_NAME, "sting", NameHelper
							.localizeName(rwco.getName(), rwco.getRealm()));
			propA.addAttribute("", SchemaNames.ATTR_OWNER,
					SchemaNames.ATTR_OWNER, "sting", rwco.getOwner());
			propA.addAttribute("", SchemaNames.ATTR_REALM,
					SchemaNames.ATTR_REALM, "sting", rwco.getRealm());
			propA.addAttribute("", SchemaNames.ATTR_REFERENCED,
					SchemaNames.ATTR_REFERENCED, "sting", rwco.getReferenced());
			propA.addAttribute("", SchemaNames.ATTR_SHA1,
					SchemaNames.ATTR_SHA1, "sting", rwco.getSha1());
			propA.addAttribute("", SchemaNames.ATTR_USER,
					SchemaNames.ATTR_USER, "sting", rwco.getUser());
			propA.addAttribute("", SchemaNames.ATTR_REVISION,
					SchemaNames.ATTR_REVISION, "sting", String.valueOf(rwco
							.getRevision()));
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			propA.addAttribute("", SchemaNames.ATTR_LAST_CHANGE,
					SchemaNames.ATTR_LAST_CHANGE, "sting", sd.format(rwco
							.getVersion()));
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
					SchemaNames.EL_NSCHANGE, propA);
			renderToXML(rwco, ch);
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
					SchemaNames.EL_NSCHANGE);
		}

	}

	/**
	 * @return Returns the rwikObjectService.
	 */
	public RWikiObjectService getRwikObjectService()
	{
		return rwikObjectService;
	}

	/**
	 * @param rwikObjectService
	 *        The rwikObjectService to set.
	 */
	public void setRwikObjectService(RWikiObjectService rwikObjectService)
	{
		this.rwikObjectService = rwikObjectService;
	}

}
