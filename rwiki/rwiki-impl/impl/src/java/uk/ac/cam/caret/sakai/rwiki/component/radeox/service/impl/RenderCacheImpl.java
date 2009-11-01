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

package uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.RenderCache;

/**
 * @author ieb
 */
public class RenderCacheImpl implements RenderCache
{

	private static Log log = LogFactory.getLog(RenderCacheImpl.class);

	private Cache cache = null;
	
	public String getRenderedContent(String key)
	{
		String cacheValue = null;
		try
		{
			Element e = cache.get(key);
			if (e != null)
			{
				cacheValue = (String) e.getValue();
			}
		}
		catch (Exception ex)
		{
			log.error("RenderCache threw Exception for key: " + key, ex);
		} 

		if (cacheValue != null)
			log.debug("Cache hit for " + key + " size " + cacheValue.length());
		else
			log.debug("Cache miss for " + key);
		return cacheValue;
	}

	public void putRenderedContent(String key, String content)
	{
			Element e = new Element(key, content);
			cache.put(e);
			log.debug("Put " + key + " size " + content.length());
	}

	public void init()
	{
		log.info(cache.getName()+" cache is ");}

	/**
	 * @return the cache
	 */
	public Cache getCache()
	{
		return cache;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(Cache cache)
	{
		this.cache = cache;
	}


}
