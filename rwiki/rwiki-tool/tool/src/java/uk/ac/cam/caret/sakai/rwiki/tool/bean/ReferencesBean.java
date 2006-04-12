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
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.service.legacy.entity.Entity;

import uk.ac.cam.caret.sakai.rwiki.service.api.EntityHandler;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.XmlEscaper;

/**
 * Bean that uses the object service and a current rwikiObject to find the
 * referencing pages and the pages referenced by the current rwikiObject.
 * 
 * @author andrew
 */
//FIXME: Tool

public class ReferencesBean {

    private RWikiObject rwikiObject;

    private RWikiObjectService objectService;

    private String defaultSpace;

    /**
     * Create new ReferencesBean.
     */
    public ReferencesBean(RWikiObject rwikiObject,
            RWikiObjectService objectService, String localSpace) {
        this.rwikiObject = rwikiObject;
        this.objectService = objectService;
        this.defaultSpace = localSpace;
    }

    /**
     * Get links to the pages referenced by the current RWikiObject.
     * @return list of xhtml links
     */
    public List getReferencedPageLinks() {
        String referenced = rwikiObject.getReferenced();
        String[] references = referenced.split("::");
        List referenceLinks = new ArrayList(references.length);
        ViewBean vb = new ViewBean(rwikiObject.getName(), defaultSpace);
        vb.setLocalSpace(vb.getPageSpace());
        for (int i = 0; i < references.length; i++) {
            String pageName = references[i];
            if (pageName != null && !pageName.equals("")) {
                vb.setPageName(pageName);
                String link = "<a href=\""
                        + XmlEscaper.xmlEscape(vb.getViewUrl()) + "\">"
                        + XmlEscaper.xmlEscape(vb.getLocalName()) + "</a>";
                referenceLinks.add(link);
            }
        }
        return referenceLinks;
    }
    public List getFeedsLinks() {
    		List feedsLinks = new ArrayList();
        Map m = objectService.getHandlers();
        for ( Iterator ii = m.keySet().iterator(); ii.hasNext(); ){
        		String name = (String) ii.next();
        		EntityHandler eh = (EntityHandler)m.get(name);
    			Entity e = objectService.getEntity(rwikiObject);
        		String displayLink = eh.getHTML(e);
        		if ( displayLink != null  ) {
        			feedsLinks.add(displayLink);
        		}
        }
        return feedsLinks;
    }

    /**
     * Gets links to the pages referencing the current RWikiObject.
     * @return list of xhtml links
     */
    public List getReferencingPageLinks() {
        List pages = objectService.findReferencingPages(rwikiObject.getName());
        List referencingLinks = new ArrayList(pages.size());
        ViewBean vb = new ViewBean(rwikiObject.getName(), defaultSpace);
        vb.setLocalSpace(vb.getPageSpace());
        for (Iterator it = pages.iterator(); it.hasNext();) {
            vb.setPageName((String) it.next());
            String link = "<a href=\"" + XmlEscaper.xmlEscape(vb.getViewUrl())
                    + "\">" + XmlEscaper.xmlEscape(vb.getLocalName()) + "</a>";
            referencingLinks.add(link);
        }
        return referencingLinks;
    }

}
