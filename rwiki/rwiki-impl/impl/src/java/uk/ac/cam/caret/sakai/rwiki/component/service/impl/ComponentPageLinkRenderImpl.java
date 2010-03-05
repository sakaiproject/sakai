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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.XmlEscaper;

/**
 * A public page link renderer that renders links suitable for use in a public
 * view
 * 
 * @author ieb
 */

// FIXME: Tool
public class ComponentPageLinkRenderImpl implements PageLinkRenderer
{

	/**
	 * Indicates that the render operation is cachable
	 */
	private boolean cachable = true;

	/**
	 * indicates that the cache should be used
	 */
	private boolean useCache = true;

	/**
	 * the local space of the page
	 */
	public String localSpace;

	/**
	 * format String used to generate a standard URL, no anchor
	 */
	private String standardURLFormat = "/wiki{0}.html{1}";

	/**
	 * Format string used to generate a URL with anchor
	 */
	private String anchorURLFormat = "/wiki{0}.html{2}#{1}";

	/**
	 * HTML markup pattern
	 */
	private String urlFormat = "<a href=\"{0}\" >{1}</a>";

	private boolean withBreadcrumbs = true;

	private String breadcrumbSwitch = "?breadcrumb=0";

	public ComponentPageLinkRenderImpl(String localSpace, boolean withBreadcrumbs)
	{
		this.localSpace = localSpace;
		this.withBreadcrumbs  = withBreadcrumbs;
	}

	/**
	 * Generates a publiv navigation link
	 */
	public void appendLink(StringBuffer buffer, String name, String view)
	{
		this.appendLink(buffer, name, view, null);
	}

	/**
	 * Generated a public navigation link
	 */
	public void appendLink(StringBuffer buffer, String name, String view,
			String anchor)
	{
		name = NameHelper.globaliseName(name, localSpace);
		String url;
		if (anchor != null && !"".equals(anchor)) {
			url = MessageFormat.format(anchorURLFormat, new Object[] { encode(name),
					encode(anchor), withBreadcrumbs?"":breadcrumbSwitch });			
		} else {
			url = MessageFormat.format(standardURLFormat,
					new Object[] { encode(name), withBreadcrumbs?"":breadcrumbSwitch });
		}

		buffer.append(MessageFormat.format(urlFormat, new Object[] {
				XmlEscaper.xmlEscape(url), XmlEscaper.xmlEscape(view) }));
	}

	/**
	 * Generates a create link
	 */
	public void appendCreateLink(StringBuffer buffer, String name, String view)
	{
		cachable = false;

		// In public view, pages that dont exist are not links
		buffer.append(XmlEscaper.xmlEscape(view));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer#isCachable()
	 */
	public boolean isCachable()
	{

		return cachable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer#canUseCache()
	 */
	public boolean canUseCache()
	{
		return useCache && withBreadcrumbs;
	}

	public void setCachable(boolean cachable)
	{
		this.cachable = cachable;

	}

	public void setUseCache(boolean b)
	{
		useCache = b;

	}

	/**
	 * @return Returns the anchor URL Format.
	 */
	public String getAnchorURLFormat()
	{
		return anchorURLFormat;
	}

	/**
	 * @param anchorURLFormat
	 *        The anchor URL Format to set, param 0 is the pagename, 1 is the
	 *        anchor
	 */
	public void setAnchorURLFormat(String anchorURLFormat)
	{
		this.anchorURLFormat = anchorURLFormat;
	}

	/**
	 * @return Returns the standard URL Format.
	 */
	public String getStandardURLFormat()
	{
		return standardURLFormat;
	}

	/**
	 * @param standardURLFormat
	 *        The standard URL Format to set, param 0 is the pagename
	 */
	public void setStandardURLFormat(String standardURLFormat)
	{
		this.standardURLFormat = standardURLFormat;
	}

	/**
	 * @return Returns the urlFormat. use to generate link html
	 */
	public String getUrlFormat()
	{
		return urlFormat;
	}

	/**
	 * @param urlFormat
	 *        The urlFormat to set use to generate link html
	 */
	public void setUrlFormat(String urlFormat)
	{
		this.urlFormat = urlFormat;
	}

	/**
	 * @return Returns the useCache.
	 */
	public boolean isUseCache()
	{
		return  useCache;
	}
	/**
	 * Takes a string to encode and encodes it as a UTF-8 URL-Encoded string.
	 * 
	 * @param toEncode
	 *        string to encode.
	 * @return url encoded string.
	 */
	public static String encode(String toEncode)
	{
		try
		{		
			String encoded = URLEncoder.encode(toEncode, "UTF-8");
			encoded = encoded.replaceAll("\\+", "%20").replaceAll("%2F", "/");
			
			return encoded; 

		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException("UTF-8 Encoding is not supported when encoding: "  + toEncode + ": " + e.getMessage());
		}
		
	}

	public void appendLink(StringBuffer buffer, String name, String view, String anchor, boolean autoGenerated)
	{
		this.appendLink(buffer, name, view, anchor);
	}

	/**
	 * @return the breadcrumbSwitch
	 */
	public String getBreadcrumbSwitch()
	{
		return breadcrumbSwitch;
	}

	/**
	 * @param breadcrumbSwitch the breadcrumbSwitch to set
	 */
	public void setBreadcrumbSwitch(String breadcrumbSwitch)
	{
		this.breadcrumbSwitch = breadcrumbSwitch;
	}
}
