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

import org.apache.log4j.Logger;
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

	private static final Logger log = Logger.getLogger(ProfileKudosLogicImpl.class);

	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.profile2.cache.kudos";
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public int getKudos(String userUuid){
		
		ProfileKudos k;
		
		if(cache.containsKey(userUuid)){
			log.debug("Fetching kudos from cache for: " + userUuid);
			k = (ProfileKudos)cache.get(userUuid);
		} else {
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
	
	
	public void init() {
		cache = cacheManager.createCache(CACHE_NAME);
	}
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private CacheManager cacheManager;
	
}
