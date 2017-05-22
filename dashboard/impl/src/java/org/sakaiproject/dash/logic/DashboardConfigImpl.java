/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.logic;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.dash.app.DashboardConfig;
import org.sakaiproject.dash.dao.DashboardDao;
import org.springframework.transaction.annotation.Transactional;

public class DashboardConfigImpl implements DashboardConfig {
	
	private static Logger logger = LoggerFactory.getLogger(DashboardConfigImpl.class);
	
	public static final String DASHBOARD_CACHE_PREFIX = "org.sakaiproject.dash.app.DashboardConfig.";

	private static final Boolean CACHE_ETERNAL = new Boolean(false);
	private static final Integer CACHE_TIME_TO_IDLE = new Integer(300);
	private static final Integer CACHE_TIME_TO_LIVE = new Integer(300);
	
	protected Map<String,String> actionIconMap = new HashMap<String,String>();

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
	
	@Transactional
	public void setConfigValue(String propertyName, Integer propertyValue) {

		String cacheKey = DASHBOARD_CACHE_PREFIX + propertyName;

		synchronized(configLock) {
	
			dao.setConfigProperty(propertyName, propertyValue);
			cache.remove(cacheKey);
			cache.put(new Element(cacheKey, propertyValue, CACHE_ETERNAL, CACHE_TIME_TO_IDLE, CACHE_TIME_TO_LIVE));
			
		}
	}
	
	public void init() {
		logger.info("init()");
		
		actionIconMap.put(ACTION_UNSTAR, "/dashboard-tool/css/img/star-act.png");
		actionIconMap.put(ACTION_STAR, "/dashboard-tool/css/img/star-inact.png");
		actionIconMap.put(ACTION_HIDE, "/dashboard-tool/css/img/cancel.png");
		actionIconMap.put(ACTION_SHOW, "/dashboard-tool/css/img/accept.png");
		
	}

	public String getActionIcon(String actionId) {
		return actionIconMap.get(actionId);
	}
	
}
