package org.sakaiproject.dash.logic;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.sakaiproject.dash.dao.DashboardDao;

public class DashboardConfigImpl implements DashboardConfig {
	
	public static final String DASHBOARD_CACHE_PREFIX = "org.sakaiproject.dash.logic.DashboardConfig.";

	private static final Boolean CACHE_ETERNAL = new Boolean(false);
	private static final Integer CACHE_TIME_TO_IDLE = new Integer(300);
	private static final Integer CACHE_TIME_TO_LIVE = new Integer(300);

	private Object configLock = new Object();
	
	
	/************************************************************************
	 * Spring-injected classes
	 ************************************************************************/
	
	protected DashboardDao dao;
	public void setDao(DashboardDao dao) {
		this.dao = dao;
	}
	
	protected Cache cache;
	public void setCache(Cache cache) {
		this.cache = cache;
	}
		
	/************************************************************************
	 * DashboardConfig methods
	 ************************************************************************/
		
	public Integer getConfigValue(String propertyName, Integer defaultValue) {
		
		String cacheKey = DASHBOARD_CACHE_PREFIX + propertyName;
		Integer value = null;
		synchronized(configLock) {
			Element element = cache.get(cacheKey);
			if(element != null) {
				value = (Integer) element.getObjectValue();
			}
			if(value == null) {
				value = dao.getConfigProperty(propertyName);
				element = null;
			}
			if(value == null) {
				value = defaultValue;
			}
			if(element == null) {
				cache.put(new Element(cacheKey, value, CACHE_ETERNAL, CACHE_TIME_TO_IDLE, CACHE_TIME_TO_LIVE));
			}
		}
		return value;
	}
	public void setConfigValue(String propertyName, Integer propertyValue) {

		String cacheKey = DASHBOARD_CACHE_PREFIX + propertyName;

		synchronized(configLock) {
	
			dao.setConfigProperty(propertyName, propertyValue);
			cache.remove(cacheKey);
			cache.put(new Element(cacheKey, propertyValue, CACHE_ETERNAL, CACHE_TIME_TO_IDLE, CACHE_TIME_TO_LIVE));
			
		}
	}
	
}
