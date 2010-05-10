package org.sakaiproject.profile2.logic;

import java.math.BigDecimal;
import java.util.Date;

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

	private Cache cache;
	private final String CACHE_NAME = "org.sakaiproject.profile2.cache.kudos";
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public BigDecimal getKudos(String userUuid){
		ProfileKudos k = dao.getKudos(userUuid);
		if(k == null){
			return null;
		}
		return k.getScore();
	}

	/**
 	 * {@inheritDoc}
 	 */
	public boolean updateKudos(String userUuid, BigDecimal score) {
		ProfileKudos k = new ProfileKudos();
		k.setUserUuid(userUuid);
		k.setScore(score);
		k.setDateAdded(new Date());
		
		return dao.updateKudos(k);
	}
	
	public void init() {
		cache = cacheManager.createCache(CACHE_NAME);
	}
	
	
	private ProfileDao dao;
	public void setDao(ProfileDao dao) {
		this.dao = dao;
	}
	
	private CacheManager cacheManager;
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
}
