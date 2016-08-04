/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.logic;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.profile2.cache.CacheManager;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.hbm.model.ProfileKudos;

/**
 * Implementation of ProfileKudosLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileKudosLogicImpl implements ProfileKudosLogic {

	private static final Logger log = LoggerFactory.getLogger(ProfileKudosLogicImpl.class);

	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.profile2.cache.kudos";
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getKudos(String userUuid){
		
		ProfileKudos k = null;
		
		if(cache.containsKey(userUuid)){
			log.debug("Fetching kudos from cache for: " + userUuid);
			k = (ProfileKudos)cache.get(userUuid);
			if(k == null) {
				// This means that the cache has expired. evict the key from the cache
				log.debug("Kudos cache appears to have expired for " + userUuid);
				evictFromCache(userUuid);
			}
		}
		if(k == null) {
			k = dao.getKudos(userUuid);
			
			if(k != null){
				log.debug("Adding kudos to cache for: " + userUuid);
				cache.put(userUuid, k);
			}
		}
		
		if(k == null) {
			return 0;
		}
		return k.getScore();
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public BigDecimal getRawKudos(String userUuid){
		ProfileKudos k = dao.getKudos(userUuid);
		if(k == null){
			return null;
		}
		return k.getPercentage();
	}

	/**
 	 * {@inheritDoc}
 	 */
	public boolean updateKudos(String userUuid, int score, BigDecimal percentage) {
		ProfileKudos k = new ProfileKudos();
		k.setUserUuid(userUuid);
		k.setScore(score);
		k.setPercentage(percentage);
		k.setDateAdded(new Date());
		
		if(dao.updateKudos(k)){
			log.debug("Adding kudos to cache for: " + userUuid);
			cache.put(userUuid, k);
			return true;
		}
		return false;
	}
	
	/**
	 * Helper to evict an item from a cache. 
	 * @param cacheKey	the id for the data in the cache
	 */
	private void evictFromCache(String cacheKey) {
		cache.remove(cacheKey);
		log.debug("Evicted data in cache for key: " + cacheKey);
	}

	public void init() {
		cache = cacheManager.createCache(CACHE_NAME);
	}
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private CacheManager cacheManager;
	
}
