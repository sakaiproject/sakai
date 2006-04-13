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
	private String standardURLFormat = "/access/wiki{0}.html";

	/**
	 * Format string used to generate a URL with anchor
	 */
	private String anchorURLFormat = "/access/wiki{0}.html#{1}";

	/**
	 * HTML markup pattern
	 */
	private String urlFormat = "<a href=\"{0}\" >{1}</a>";

	public ComponentPageLinkRenderImpl(String localSpace)
	{
		this.localSpace = localSpace;
	}

	/**
	 * Generates a publiv navigation link
	 */
	public void appendLink(StringBuffer buffer, String name, String view)
	{
		name = NameHelper.globaliseName(name, localSpace);
		String url = MessageFormat.format(standardURLFormat,
				new Object[] { name });
		buffer.append(MessageFormat.format(urlFormat, new Object[] {
				XmlEscaper.xmlEscape(url), XmlEscaper.xmlEscape(view) }));
	}

	/**
	 * Generated a public navigation link
	 */
	public void appendLink(StringBuffer buffer, String name, String view,
			String anchor)
	{
		name = NameHelper.globaliseName(name, localSpace);
		String url = MessageFormat.format(anchorURLFormat, new Object[] { name,
				anchor });
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
		return useCache;
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
		return useCache;
	}

}
