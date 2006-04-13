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
package uk.ac.cam.caret.sakai.rwiki.tool.service.impl;

import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.XmlEscaper;

/**
 * A public page link renderer that renders links suitable for use in a public
 * view
 * 
 * @author ieb
 */

// FIXME: Tool
public class PublicPageLinkRendererImpl implements PageLinkRenderer
{

	/**
	 * Indicates that the render operation is cachable
	 */
	private boolean cachable = true;

	private boolean useCache = true;

	private boolean withBreadcrumb = true;

	public String localRealm;

	public String localSpace;

	public PublicPageLinkRendererImpl(String localRealm, boolean withBreadcrumb)
	{
		this(localRealm, localRealm, withBreadcrumb);
	}

	public PublicPageLinkRendererImpl(String localSpace, String localRealm,
			boolean withBreadcrumb)
	{
		this.localSpace = localSpace;
		this.localRealm = localRealm;
		this.withBreadcrumb = withBreadcrumb;
	}

	/**
	 * Generates a publiv navigation link
	 */
	public void appendLink(StringBuffer buffer, String name, String view)
	{
		name = NameHelper.globaliseName(name, localSpace);
		ViewBean vb = new ViewBean(name, localRealm);

		buffer.append("<a href=\""
				+ XmlEscaper.xmlEscape(vb.getPublicViewUrl(withBreadcrumb))
				+ "\">" + XmlEscaper.xmlEscape(view) + "</a>");
	}

	/**
	 * Generated a public navigation link
	 */
	public void appendLink(StringBuffer buffer, String name, String view,
			String anchor)
	{
		name = NameHelper.globaliseName(name, localSpace);
		ViewBean vb = new ViewBean(name, localRealm);
		vb.setAnchor(anchor);
		buffer.append("<a href=\""
				+ XmlEscaper.xmlEscape(vb.getPublicViewUrl(withBreadcrumb))
				+ "\">" + XmlEscaper.xmlEscape(view) + "</a>");
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

}
