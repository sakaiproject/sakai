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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.sakaiproject.service.framework.log.Logger;

import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderCache;
/**
 * 
 * @author ieb
 *
 */
// FIXME: Component
public class RenderCacheImpl implements RenderCache {

	private Logger log;
	private Cache cache = null;
    private String cacheName = null;
	
	public String getRenderedContent(String key) {
		String cacheValue = null;
		try {
		    Element e = cache.get(key);
		    if ( e != null ) {
		    		cacheValue = (String)e.getValue();
		    }
		} catch ( Exception ex ) {
		}
		if ( cacheValue != null ) log.debug("Cache hit for "+key+" size "+cacheValue.length());
		else log.debug("Cache miss for "+key);
		return cacheValue;
	}

	public void putRenderedContent(String key, String content) {
		try {
		    Element e = new Element(key,content);
		    cache.put(e);
		    log.debug("Put "+key+" size "+content.length());
		} catch ( Exception ex ) {
			log.warn(" RWiki Cache PUT Failure, restarting cache ",ex);
			init();
		}
	}

    public void init() {
        try {
            CacheManager cacheManager = CacheManager.create();
            if ( cacheManager.cacheExists(cacheName) )
                cacheManager.removeCache(cacheName);
            cacheManager.addCache(cacheName);
            cache = cacheManager.getCache(cacheName);
        } catch ( Exception ex ) {
            log.warn("Failed to start RWiki cache ");
        }
    }

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}


    /**
     * @return Returns the cacheName.
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * @param cacheName The cacheName to set.
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

}
