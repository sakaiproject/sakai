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
 * Worksite page link rendered that renders links with create steps
 * @author andrew
 *
 */
// FIXME: Tool
public class PageLinkRendererImpl implements PageLinkRenderer {

	private boolean cacheable = true;
	private boolean useCache = true;
    public String localRealm;
    public String localSpace;
    
    public PageLinkRendererImpl(String localRealm) {
        this(localRealm, localRealm);
    }
    
    public PageLinkRendererImpl(String localSpace, String localRealm) {
        this.localSpace = localSpace;
        this.localRealm = localRealm;
    }
    public void appendLink(StringBuffer buffer, String name, String view) {
        name = NameHelper.globaliseName(name, localSpace);
        ViewBean vb = new ViewBean(name, localRealm);
        
        buffer.append("<a href=\"" + XmlEscaper.xmlEscape(vb.getViewUrl()) + "\">" + XmlEscaper.xmlEscape(view) + "</a>");
    }

    public void appendLink(StringBuffer buffer, String name, String view, String anchor) {
        name = NameHelper.globaliseName(name, localSpace);
        ViewBean vb = new ViewBean(name, localRealm);
        vb.setAnchor(anchor);       
        buffer.append("<a href=\"" + XmlEscaper.xmlEscape(vb.getViewUrl()) + "\">" + XmlEscaper.xmlEscape(view) + "</a>");
    }

    public void appendCreateLink(StringBuffer buffer, String name, String view) {
    	   cacheable = false;
        name = NameHelper.globaliseName(name, localSpace);
        ViewBean vb = new ViewBean(name, localRealm);
        buffer.append("<a href=\"" + XmlEscaper.xmlEscape(vb.getViewUrl()) + "\">" + XmlEscaper.xmlEscape(view) + "?</a>");
    }
    /*
     *  (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer#isCachable()
     */
	public boolean isCachable() {
		return cacheable;
	}

	public boolean canUseCache() {
		return useCache;
	}

	public void setCachable(boolean cachable) {
		this.cacheable = cachable;		
	}

	public void setUseCache(boolean b) {
		useCache  = b;		
	}


}
