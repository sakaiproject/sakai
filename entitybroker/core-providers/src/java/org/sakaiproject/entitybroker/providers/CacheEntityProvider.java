/**
 * Copyright 2015 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.entitybroker.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

import org.sakaiproject.entitybroker.providers.model.EntityCache;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

/**
 * Cache
 *
 * @author Earle Nietzel
 * Created on Sep 11, 2013
 *
 */
public class CacheEntityProvider extends AbstractEntityProvider implements CoreEntityProvider,
		ActionsExecutable, Outputable, Resolvable, Describeable, CollectionResolvable {

	public final static String PREFIX = "cache";

	private CacheManager cacheManager;

	public boolean entityExists(String id) {
		Cache cache = cacheManager.getCache(id);

		if (cache != null) {
			return true;
		}

		return false;
	}

	@EntityCustomAction(action="memory",viewKey=EntityView.VIEW_LIST)
	public Object getJvmMemoryStatus(EntityReference ref) {
		TreeMap<String, String> status = new TreeMap<String, String>();

		status.put(developerHelperService.getMessage(PREFIX, "cache.memory.free"), String.valueOf(Runtime.getRuntime().freeMemory()));
		status.put(developerHelperService.getMessage(PREFIX, "cache.memory.total"), String.valueOf(Runtime.getRuntime().totalMemory()));
		status.put(developerHelperService.getMessage(PREFIX, "cache.memory.max"), String.valueOf(Runtime.getRuntime().maxMemory()));

		return new ActionReturn(status);
	}

	@EntityCustomAction(action="cacheNames",viewKey=EntityView.VIEW_LIST)
	public Object getCacheNames(EntityReference ref) {
		List<String> cacheNames = Arrays.asList(cacheManager.getCacheNames());
		Collections.sort(cacheNames);
		return new ActionReturn(cacheNames);
	}

	@EntityCustomAction(action="cacheSummary",viewKey=EntityView.VIEW_LIST)
	public Object getCacheSummary(EntityReference ref) {
		TreeMap<String, String> summary = new TreeMap<String, String>();

		for (Ehcache cache : getAllCaches()) {
			EntityCache eCache = new EntityCache(cache);
			summary.put(eCache.getId(), developerHelperService.getMessage(PREFIX, "cache.summary.size") + eCache.getCacheSize() + " " +
										developerHelperService.getMessage(PREFIX, "cache.summary.hits") + eCache.getCacheHits() + " " +
										developerHelperService.getMessage(PREFIX, "cache.summary.ratio") + eCache.getCacheHitRatio());
		}

		return new ActionReturn(summary);
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.HTML, Formats.JSON };
	}

	public Object getEntity(EntityReference ref) {
		if (ref == null || ref.getId() == null) {
			return new EntityCache();
		}

		return new EntityCache(cacheManager.getCache(ref.getId()));
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		List<EntityCache> caches = new ArrayList<EntityCache>();

		if (search.getRestrictionByProperty("id") != null) {
			String id = search.getRestrictionByProperty("id").getStringValue();

			if (entityExists(id)) {
				caches.add((EntityCache) getEntity(ref));
			}
		} else {
			for (Ehcache cache : getAllCaches()) {
				caches.add(new EntityCache(cache));
			}
		}

		return caches;
	}

	private List<Ehcache> getAllCaches()
	{
		final String[] cacheNames = cacheManager.getCacheNames();
		Arrays.sort(cacheNames);
		final List<Ehcache> caches = new ArrayList<Ehcache>(cacheNames.length);
		for (String cacheName : cacheNames) {
			caches.add(cacheManager.getEhcache(cacheName));
		}
		return caches;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public String getEntityPrefix() {
		return PREFIX;
	}
}
