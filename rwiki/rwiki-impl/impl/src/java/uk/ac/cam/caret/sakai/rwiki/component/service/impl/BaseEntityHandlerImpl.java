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

import java.text.MessageFormat;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;

import uk.ac.cam.caret.sakai.rwiki.service.api.EntityHandler;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * Provides a regex base entity handler where the matching uses MessageFormats
 * and Regex patterns
 * 
 * @author ieb
 */
public abstract class BaseEntityHandlerImpl implements EntityHandler
{

	/**
	 * The start of the URL endign in a /
	 */
	private String accessURLStart = null;

	/**
	 * The sub type of this handler, but be unique in the context of the major
	 * type
	 */
	private String minorType = null;

	/**
	 * if the eh provides a link, this URL will give the link
	 */
	private String feedFormat;

	/** Configuration: allow use of alias for site id in references. */
	protected boolean m_siteAlias = true;
	
	private boolean experimental = false;

	private boolean setup = false;

	private boolean available = true;

	
	/** Dependency: AliasService. */
	protected AliasService m_aliasService = null;

	/**
	 * Dependency: AliasService.
	 * 
	 * @param service
	 *        The AliasService.
	 */
	public void setAliasService(AliasService service)
	{
		m_aliasService = service;
	}

	/** Dependency: SiteService. */
	protected SiteService m_siteService = null;

	/**
	 * Dependency: SiteService.
	 * 
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService service)
	{
		m_siteService = service;
	}
	
	/**
	 * {@inheritDoc} 
	 */
	public void setReference(String majorType, Reference ref, String reference)
	{
		if (!isAvailable()) return;
		Decoded decoded = decode(reference);
		if (decoded != null)
		{
			ref.set(majorType, minorType, decoded.getId(), decoded
					.getContainer(), decoded.getContext());
		}
		else
		{
			throw new RuntimeException(this
					+ " Failed to setReference in EntityHelper " + majorType
					+ ":" + minorType
					+ " reference not for this EntityHandler ");
		}

	}

	/**
	 * {@inheritDoc} 
	 */
	public boolean matches(String reference)
	{
		if (!isAvailable()) return false;
		return (decode(reference) != null);
	}

	/**
	 * {@inheritDoc} 
	 */
	public int getRevision(Reference reference)
	{
		if (!isAvailable()) return 0;
		Decoded decode = decode(reference.getReference());
		return Integer.parseInt(decode.getVersion());
	}

	/**
	 * @return Returns the minorType.
	 */
	public String getMinorType()
	{
		return minorType;
	}

	/**
	 * @param minorType
	 *        The minorType to set.
	 */
	public void setMinorType(String minorType)
	{
		this.minorType = minorType;
	}

	/**
	 * @param s
	 *        the full reference
	 * @return A Decoded object contianing the values, or null if not this
	 *         handler
	 */
	public Decoded decode(String s)
	{

		String ending = "." + minorType;
		if (isAvailable() && s.startsWith(accessURLStart) && s.endsWith(ending)
				&& s.indexOf("//") == -1 )
		{
			Decoded decoded = new Decoded();
			s = s.substring(accessURLStart.length() - 1);
			int lastslash = s.lastIndexOf(Entity.SEPARATOR);
			int firstslash = s.indexOf(Entity.SEPARATOR, 1);
			int nextslash = s.indexOf(Entity.SEPARATOR, firstslash + 1);
			if (nextslash == -1)
			{
				nextslash = firstslash;
			}
			
			String siteContext = s.substring(0, nextslash);
			String context;
			int slashIndex = siteContext.indexOf(Entity.SEPARATOR, 1);
			if (slashIndex == -1) {
				context = "";
			} else {
				context = s.substring (slashIndex + 1, siteContext.length());
			}

			// recognize alias for site id - but if a site id exists that matches the requested site id, that's what we will use
			if ((context != null) && (context.length() > 0))
			{
				if (m_siteAlias && (m_aliasService != null) && (m_siteService != null) 
						&& (!m_siteService.siteExists(context)))
				{
					try
					{
						String target = m_aliasService.getTarget(context);

						// the code is taken and adapted from 
						// org.sakaiproject.content.impl.BaseContentService
						// public boolean parseEntityReference(String reference, Reference ref)
						Reference targetRef = EntityManager.newReference(target);

						// for a site reference
						if (SiteService.APPLICATION_ID.equals(targetRef.getType()))
						{
							// use the ref's id, i.e. the site id
							context = targetRef.getId();
						}

						// for mail archive reference
						// TODO: taken from MailArchiveService.APPLICATION_ID to (fake) reduce a dependency -ggolden
						else if ("sakai:mailarchive".equals(targetRef.getType()))
						{
							// use the ref's context as the site id
							context = targetRef.getContext();
						}
					}
					catch (IdUnusedException noAlias)
					{
					}
				}
				siteContext = "/site/" + context;
			}
			
			decoded.setContext(siteContext);
			if (nextslash == lastslash)
			{
				decoded.setContainer(Entity.SEPARATOR);
			}
			else
			{
				decoded.setContainer(s.substring(nextslash, lastslash));
			}

			String filename = s.substring(lastslash + 1);
			filename = filename.substring(0, filename.length()
					- ending.length());

			int versionSeparator = filename.lastIndexOf("@");
			if (versionSeparator != -1 && versionSeparator < filename.length() - 1)
			{
				try { 
					Integer.parseInt(filename.substring(versionSeparator + 1));
					
					decoded.setPage(filename.substring(0, versionSeparator));
					decoded.setVersion(filename.substring(versionSeparator + 1));
				} catch (NumberFormatException e) {
					decoded.setPage(filename);
					decoded.setVersion("-1");
				}
			}
			else
			{
				decoded.setPage(filename);
				decoded.setVersion("-1");
			}

			return decoded;
		}

		return null;
	}

	/**
	 * @return Returns the accessURLStart.
	 */
	public String getAccessURLStart()
	{
		return accessURLStart;
	}

	/**
	 * @param accessURLStart
	 *        The accessURLStart to set.
	 */
	public void setAccessURLStart(String accessURLStart)
	{
		this.accessURLStart = accessURLStart;
	}

	public boolean isAvailable()
	{
		if (!setup)
		{
			if (!experimental)
			{
				available = true;
			}
			else if (ServerConfigurationService.getBoolean("wiki.experimental",
					false))
			{
				available = true;
			}
			else
			{
				available = false;
			}
			setup = true;
		}
		return available;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHTML(Entity e)
	{
		if (isAvailable())
		{
			if (feedFormat == null) return null;
			
			String url = e.getUrl();
			
			// remove the "/access" prefix
			if ( url.startsWith("/access") ) {
				url = url.substring("/access".length());
			}
			
			if (m_siteAlias && url.startsWith ("/wiki/site/")) {
				// the url format: /wiki/site/siteID/page
				int slashIndex = url.indexOf (Entity.SEPARATOR, "/wiki/site/".length());
				if (slashIndex != -1) {
					String siteId = url.substring ("/wiki/site/".length(), slashIndex);
					String pageURL = url.substring (slashIndex);
					String localSpace = "/site/" + siteId;
					url = "/wiki" + NameHelper.aliasSpace (localSpace) + pageURL;
				}
			}
			
			return MessageFormat
					.format(feedFormat, new Object[] { url });
		}
		return null;
	}

	/**
	 * @return Returns the feedFormat.
	 */
	public String getFeedFormat()
	{
		return feedFormat;
	}

	/**
	 * @param feedFormat
	 *        The feedFormat to set.
	 */
	public void setFeedFormat(String feedFormat)
	{
		this.feedFormat = feedFormat;
	}

	/**
	 * @return Returns the experimental.
	 */
	public boolean getExperimental()
	{
		return experimental;
	}

	/**
	 * @param experimental
	 *        The experimental to set.
	 */
	public void setExperimental(boolean experimental)
	{
		this.experimental = experimental;
	}

}
