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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.site.api.Site;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.DebugContentHandler;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.SchemaNames;
import uk.ac.cam.caret.sakai.rwiki.utils.UserDisplayHelper;

/**
 * Provides a XSLT Based handler, that outputs the changes on the object.
 * 
 * @author ieb
 */
@Slf4j
public class XLSTChangesHandler extends XSLTEntityHandler
{

	private static final String RFC822DATE = "EEE, dd MMM yyyy HH:mm:ss Z"; //$NON-NLS-1$

	private RWikiObjectService rwikiObjectService = null;

	/**
	 * {@inheritDoc}
	 */
	public void outputContent(Entity entity, Entity sidebar, HttpServletRequest request,
			HttpServletResponse res)
	{
		if (!isAvailable()) return;

		if (!(entity instanceof RWikiEntity)) return;

		try
		{

			Map rheaders = getResponseHeaders();
			if (rheaders != null)
			{
				for (Iterator<Entry<String, String>> i = rheaders.entrySet().iterator(); i.hasNext();)
				{
					Entry<String, String> entry = i.next();
					String name = entry.getKey() ;
					String value = entry.getValue();
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
			Decoded decodedReference = decode(entity.getReference() + getMinorType());			
			
			Attributes dummyAttributes = new AttributesImpl();

			ch.startDocument();
			ch.startElement(SchemaNames.NS_CONTAINER,
					SchemaNames.EL_ENTITYSERVICE,
					SchemaNames.EL_NSENTITYSERVICE, dummyAttributes);

			{
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

				propA.addAttribute("", SchemaNames.ATTR_SERVER_URL, //$NON-NLS-1$
						SchemaNames.ATTR_SERVER_URL, "string", //$NON-NLS-1$
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
				addPropertyElement(name, value, ch);
			}
			addPropertyElement("_handler", " XSLTEntity Handler", ch); //$NON-NLS-1$ //$NON-NLS-2$

			Site site = (Site) m_siteService.getEntity(EntityManager.newReference(decodedReference.getContext()));
			String title;
			if (site != null) {
				title = site.getTitle();
			} else {
				title = decodedReference.getContext();
			}

			addPropertyElement("_siteDisplay", title, ch); //$NON-NLS-1$
			addPropertyElement("_container", decodedReference.getContainer(), ch); //$NON-NLS-1$

			if (entity instanceof RWikiEntity)
			{
				RWikiEntity rwe = (RWikiEntity) entity;

				if (!rwe.isContainer())
				{
					RWikiObject rwo = rwe.getRWikiObject();
					title = Messages.getString("XLSTChangesHandler.19") + title + ":" + NameHelper.localizeName(rwo.getName(), decodedReference.getContext());					 //$NON-NLS-1$ //$NON-NLS-2$
				}
				else
				{
					if (decodedReference.getContainer() != null && decodedReference.getContainer().length() > 1) {
						title = Messages.getString("XLSTChangesHandler.21") + title + ":" + decodedReference.getContainer().substring(1); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						title = Messages.getString("XLSTChangesHandler.23") + title; //$NON-NLS-1$
					}
					
				}
				
				addPropertyElement("_title", title, ch); //$NON-NLS-1$

			}
			
			{
				addPropertyElement("_description", ServerConfigurationService.getString("ui.service"), ch); //$NON-NLS-1$ //$NON-NLS-2$
				
			}
			{
				// 2006-02-16T18:28:03+01:00
				SimpleDateFormat sd = new SimpleDateFormat(
						RFC822DATE);
				addPropertyElement("_datestamp", sd //$NON-NLS-1$
						.format(new Date()), ch);
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

					renderToXML(rwo, ch, true, true);
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
			log.error(ex.getMessage(), ex);
			throw new RuntimeException("Failed to serialise " //$NON-NLS-1$
					+ ex.getLocalizedMessage(), ex);
		}
	}

	private void addPropertyElement(String property, String value, ContentHandler ch) throws SAXException
	{
		AttributesImpl propA = new AttributesImpl();
		propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
				SchemaNames.ATTR_NAME, "string", property); //$NON-NLS-1$
		addElement(ch, SchemaNames.NS_CONTAINER,
				SchemaNames.EL_XMLPROPERTY,
				SchemaNames.EL_NSXMLPROPERTY, propA,
				value);
	}

	public void changeHistoryToXML(RWikiObject rwo, ContentHandler ch)
			throws Exception
	{
		if (!isAvailable()) return;
		
		String name = rwo.getName();
		String localSpace = NameHelper.localizeSpace(name);
		String aliasSpace;
		
		// use alias if available
		if (m_siteAlias) {
			aliasSpace = NameHelper.aliasSpace(localSpace);
		} else {
			aliasSpace = localSpace;
		}
		
		// output current version as well
		AttributesImpl propA = new AttributesImpl();
		propA.addAttribute("", SchemaNames.ATTR_ID, SchemaNames.ATTR_ID, //$NON-NLS-1$
				"string", rwo.getId()); //$NON-NLS-1$

		String pageName = NameHelper.localizeName(name, localSpace);
		name = aliasSpace + "/" + pageName;
		// FIXME why do we know about "@" here?!
		propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
				SchemaNames.ATTR_NAME, "string", name + "@" //$NON-NLS-1$ //$NON-NLS-2$
						+ rwo.getRevision());
		propA.addAttribute("", SchemaNames.ATTR_LOCAL_NAME, //$NON-NLS-1$
				SchemaNames.ATTR_LOCAL_NAME, "string", NameHelper //$NON-NLS-1$
						.localizeName(rwo.getName(), rwo.getRealm()));
		propA.addAttribute("", SchemaNames.ATTR_OWNER, //$NON-NLS-1$
				SchemaNames.ATTR_OWNER, "string", rwo.getOwner()); //$NON-NLS-1$
		propA.addAttribute("", SchemaNames.ATTR_OWNER, //$NON-NLS-1$
				SchemaNames.ATTR_REALM, "string", rwo.getRealm()); //$NON-NLS-1$
		propA.addAttribute("", SchemaNames.ATTR_REFERENCED, //$NON-NLS-1$
						SchemaNames.ATTR_REFERENCED, "string", rwo //$NON-NLS-1$
								.getReferenced());
		propA.addAttribute("", SchemaNames.ATTR_SHA1, //$NON-NLS-1$
				SchemaNames.ATTR_SHA1, "string", rwo.getSha1()); //$NON-NLS-1$
		propA.addAttribute("", SchemaNames.ATTR_USER, //$NON-NLS-1$
				SchemaNames.ATTR_USER, "string", rwo.getUser()); //$NON-NLS-1$
		propA.addAttribute("", SchemaNames.ATTR_DISPLAY_USER, //$NON-NLS-1$
				SchemaNames.ATTR_DISPLAY_USER, "string", UserDisplayHelper //$NON-NLS-1$
						.formatDisplayName(rwo.getUser(), rwo.getRealm()));
		propA.addAttribute("", SchemaNames.ATTR_REVISION, //$NON-NLS-1$
				SchemaNames.ATTR_REVISION, "string", String.valueOf(rwo //$NON-NLS-1$
						.getRevision()));
		SimpleDateFormat sd = new SimpleDateFormat(RFC822DATE);
		propA.addAttribute("", SchemaNames.ATTR_LAST_CHANGE, //$NON-NLS-1$
				SchemaNames.ATTR_LAST_CHANGE, "string", sd.format(rwo //$NON-NLS-1$
						.getVersion()));

		ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
				SchemaNames.EL_NSCHANGE, propA);
		renderToXML(rwo, ch,true,true);
		ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
				SchemaNames.EL_NSCHANGE);
		// end of output current version


		List changes = rwikiObjectService.findRWikiHistoryObjectsInReverse(rwo);
		if (changes == null) return;
		for (Iterator i = changes.iterator(); i.hasNext();)
		{
			RWikiHistoryObject rwco = (RWikiHistoryObject) i.next();
			propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_ID, SchemaNames.ATTR_ID, //$NON-NLS-1$
					"string", rwco.getId()); //$NON-NLS-1$

			name = rwco.getName();

			pageName = NameHelper.localizeName(name, localSpace);
			name = aliasSpace + "/" + pageName;
			// FIXME why do we know about "@" here?!
			propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
					SchemaNames.ATTR_NAME, "string", name + "@" //$NON-NLS-1$ //$NON-NLS-2$
							+ rwco.getRevision());
			propA.addAttribute("", SchemaNames.ATTR_LOCAL_NAME, //$NON-NLS-1$
					SchemaNames.ATTR_LOCAL_NAME, "string", NameHelper //$NON-NLS-1$
							.localizeName(rwo.getName(), rwo.getRealm()));
			propA.addAttribute("", SchemaNames.ATTR_OWNER, //$NON-NLS-1$
					SchemaNames.ATTR_OWNER, "string", rwco.getOwner()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_OWNER, //$NON-NLS-1$
					SchemaNames.ATTR_REALM, "string", rwco.getRealm()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_REFERENCED, //$NON-NLS-1$
							SchemaNames.ATTR_REFERENCED, "string", rwco //$NON-NLS-1$
									.getReferenced());
			propA.addAttribute("", SchemaNames.ATTR_SHA1, //$NON-NLS-1$
					SchemaNames.ATTR_SHA1, "string", rwco.getSha1()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_USER, //$NON-NLS-1$
					SchemaNames.ATTR_USER, "string", rwco.getUser()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_DISPLAY_USER, //$NON-NLS-1$
					SchemaNames.ATTR_DISPLAY_USER, "string", UserDisplayHelper //$NON-NLS-1$
							.formatDisplayName(rwco.getUser(), rwco.getRealm()));
			propA.addAttribute("", SchemaNames.ATTR_REVISION, //$NON-NLS-1$
					SchemaNames.ATTR_REVISION, "string", String.valueOf(rwco //$NON-NLS-1$
							.getRevision()));
			propA.addAttribute("", SchemaNames.ATTR_LAST_CHANGE, //$NON-NLS-1$
					SchemaNames.ATTR_LAST_CHANGE, "string", sd.format(rwco //$NON-NLS-1$
							.getVersion()));

			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
					SchemaNames.EL_NSCHANGE, propA);
			renderToXML(rwco, ch, true, true);
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
		List changes = rwikiObjectService.findAllChangedSince(g.getTime(),
				basepath);
		int nchanges = 0;
		
		
		for (Iterator i = changes.iterator(); i.hasNext() && nchanges < 20;)
		{
			nchanges++;
			RWikiCurrentObject rwco = (RWikiCurrentObject) i.next();
			AttributesImpl propA = new AttributesImpl();
			propA.addAttribute("", SchemaNames.ATTR_ID, SchemaNames.ATTR_ID, //$NON-NLS-1$
					"string", rwco.getId()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_NAME, //$NON-NLS-1$
					SchemaNames.ATTR_NAME, "string", rwco.getName()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_LOCAL_NAME, //$NON-NLS-1$
					SchemaNames.ATTR_LOCAL_NAME, "string", NameHelper //$NON-NLS-1$
							.localizeName(rwco.getName(), rwco.getRealm()));
			propA.addAttribute("", SchemaNames.ATTR_OWNER, //$NON-NLS-1$
					SchemaNames.ATTR_OWNER, "string", rwco.getOwner()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_REALM, //$NON-NLS-1$
					SchemaNames.ATTR_REALM, "string", rwco.getRealm()); //$NON-NLS-1$
			propA
					.addAttribute("", SchemaNames.ATTR_REFERENCED, //$NON-NLS-1$
							SchemaNames.ATTR_REFERENCED, "string", rwco //$NON-NLS-1$
									.getReferenced());
			propA.addAttribute("", SchemaNames.ATTR_SHA1, //$NON-NLS-1$
					SchemaNames.ATTR_SHA1, "string", rwco.getSha1()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_USER, //$NON-NLS-1$
					SchemaNames.ATTR_USER, "string", rwco.getUser()); //$NON-NLS-1$
			propA.addAttribute("", SchemaNames.ATTR_DISPLAY_USER, //$NON-NLS-1$
					SchemaNames.ATTR_DISPLAY_USER, "string", UserDisplayHelper //$NON-NLS-1$
							.formatDisplayName(rwco.getUser(), rwco.getRealm()));
			propA.addAttribute("", SchemaNames.ATTR_REVISION, //$NON-NLS-1$
					SchemaNames.ATTR_REVISION, "string", String.valueOf(rwco //$NON-NLS-1$
							.getRevision()));
			SimpleDateFormat sd = new SimpleDateFormat(RFC822DATE);
			propA.addAttribute("", SchemaNames.ATTR_LAST_CHANGE, //$NON-NLS-1$
					SchemaNames.ATTR_LAST_CHANGE, "string", sd.format(rwco //$NON-NLS-1$
							.getVersion()));
			ch.startElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
					SchemaNames.EL_NSCHANGE, propA);
			renderToXML(rwco, ch, true, true);
			ch.endElement(SchemaNames.NS_CONTAINER, SchemaNames.EL_CHANGE,
					SchemaNames.EL_NSCHANGE);
		}

	}

	/**
	 * @return Returns the rwikiObjectService.
	 */
	public RWikiObjectService getRwikiObjectService()
	{
		return rwikiObjectService;
	}

	/**
	 * @param rwikiObjectService
	 *        The rwikiObjectService to set.
	 */
	public void setRwikiObjectService(RWikiObjectService rwikiObjectService)
	{
		this.rwikiObjectService = rwikiObjectService;
	}

}
