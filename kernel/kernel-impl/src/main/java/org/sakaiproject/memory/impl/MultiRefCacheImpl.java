/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.memory.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import net.sf.ehcache.Ehcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.MultiRefCache;

/**
 * <p>
 * MultiRefCacheImpl implements the MultiRefCache.
 * </p>
 * <p>
 * The references that each cache entry are sensitive to are kept in a separate map for easy access.<br />
 * Manipulation of this map is synchronized. This map is not used for cache access, just when items are added and removed.<br />
 * The cache map itself becomes synchronized when it's manipulated (not when reads occur), so this added sync. for the refs fits the existing pattern.
 * </p>
 * @deprecated Use {@link GenericMultiRefCacheImpl} instead.
 */
public class MultiRefCacheImpl extends GenericMultiRefCacheImpl implements MultiRefCache
	{
	
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(MultiRefCacheImpl.class);
	

	/** AuthzGroupService used when putting items into the cache. */
	private AuthzGroupService m_authzGroupService;

	public MultiRefCacheImpl(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService,
			AuthzGroupService authzGroupService, Ehcache cache) {
		super(memoryService, eventTrackingService, cache);
		this.m_authzGroupService = authzGroupService;
	}

	public String getDescription()
	{
		return "MultiRefCache: " + super.getDescription();
	}

	/**
	 * @inheritDoc
	 */
	public void put(Object key, Object payload, int duration, String ref, Collection azgIds)
	{
		if(M_log.isDebugEnabled())
		{
			M_log.debug("put(Object " + key + ", Object " + payload + ", int "
					+ duration + ", String " + ref + ", Collection " + azgIds
					+ ")");
		}
		if (disabled()) return;

		// make refs for any azg ids
		Collection azgRefs = null;
		if (azgIds != null)
		{
			azgRefs = new Vector(azgIds.size());
			for (Iterator i = azgIds.iterator(); i.hasNext();)
			{
				String azgId = (String) i.next();
				azgRefs.add(m_authzGroupService.authzGroupReference(azgId));
			}
		}
		put(key, payload, ref, azgRefs);
	}
	}
