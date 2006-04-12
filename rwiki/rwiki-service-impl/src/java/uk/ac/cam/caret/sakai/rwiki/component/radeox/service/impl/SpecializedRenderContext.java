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
package uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl;

import org.radeox.engine.context.BaseRenderContext;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.CachableRenderContext;

/**
 * This class acts as a container for the Render context, making the RWiki Object,
 * the Security Service and the RWikiObject service available to the Render Engine.
 * If the operation is cachable, the RenderContext should return true
 * @author andrew
 *
 */
//FIXME: Component

public class SpecializedRenderContext extends BaseRenderContext implements CachableRenderContext {

	/**
	 * Monitors cachable status
	 */
	private boolean cachable = true;
	
    private RWikiObjectService objectService;
    
    private RWikiObject rwikiObject;
    
    private RWikiSecurityService securityService;

   
    
    public SpecializedRenderContext(RWikiObject rwikiObject,  RWikiObjectService objectService,  RWikiSecurityService securityService) {
        this.rwikiObject = rwikiObject;
   
        this.objectService = objectService;
        this.securityService = securityService;
    }
    
    public RWikiObject getRWikiObject() {
        return rwikiObject;
    }

    public void setRWikiObject(RWikiObject rwikiObject) {
        this.rwikiObject = rwikiObject;
    }

    public RWikiObjectService getObjectService() {
        return objectService;
    }

    public void setObjectService(RWikiObjectService objectService) {
        this.objectService = objectService;
    }

    public RWikiSecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(RWikiSecurityService securityService) {
        this.securityService = securityService;
    }

    /*
    public String getUser() {
    		cachable = false;
        return user;
    }
    */
    /**
     * 
     * @return true if the render operation is cachable (after rendering)
     */
    public boolean isCachable() {
    		return cachable;
    }

	public Site getSite() {

			RWikiObjectService rwobjService = getObjectService();
			RWikiObject rwobj = getRWikiObject();
			Reference ref = rwobjService.getReference(rwobj);
			
			
			
			Site s = null;
			String siteId = getSiteId();
			if (siteId != null) {
				try {
					s = SiteService.getSite(siteId);
				} catch (Exception ex) {
				}
			}
			return s;

	}

	public String getSiteId() {
		RWikiObjectService rwobjService = getObjectService();
		RWikiObject rwobj = getRWikiObject();
		Reference ref = rwobjService.getReference(rwobj);
		
		
		String siteContext = ref.getContext();
		if (siteContext.startsWith("/site/")) {
			String siteId = siteContext.substring(6);

			int slash = siteId.indexOf("/");
			if (slash != -1) {
				siteId = siteId.substring(0,slash-1);
			}
			return siteId;
		}
		return null;
	}

    
}
