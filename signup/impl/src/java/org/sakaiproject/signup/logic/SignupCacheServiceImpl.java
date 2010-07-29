/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/logic/SignupCacheServiceImpl.java $
 * $Id: SignupMeetingService.java 59241 2009-03-24 15:52:18Z guangzheng.liu@yale.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/

package org.sakaiproject.signup.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.signup.dao.SignupMeetingDao;
import org.sakaiproject.signup.model.SignupMeeting;

/**
 * <p>
 * SignupCacheServiceImpl implements the methods to cache the search results of a
 * list of the SignupMeeting objects from the DB.
 * </p>
 * 
 * @author Peter Liu
 * 
 */

public class SignupCacheServiceImpl implements SignupCacheService,
		CacheRefresher {

	private MemoryService memoryService;

	protected SignupMeetingDao signupMeetingDao;

	private static Log M_log = LogFactory.getLog(SignupCacheServiceImpl.class);

	private Cache m_signupSiteCache = null;

	/** The one and only one client. */
	public static SignupCacheServiceImpl m_instance = null;

	public void init() {
		
		/* Here we use sakai default cache expiration: 5 minutes */
		try {
			// The other parameters are not needed for this cache
			m_signupSiteCache = memoryService
					.newCache("org.sakaiproject.signup.logic.siteCache");
			if (m_instance == null) {
				m_instance = this;
			}

			if (M_log.isDebugEnabled()) {
				M_log.debug(this + ".init()");
			}
		} catch (Throwable t) {
			M_log.warn(this + "init(): ", t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getAllSignupMeetingsInSite(String siteId,
			Date startDate, int timeLengthInDays) {
		List<SignupMeeting> sEvents = null;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.HOUR, 24 * timeLengthInDays);
		Date endDate = calendar.getTime();

		String searchKey = siteId + "_" + timeLengthInDays;

		Object cached = getCachedValue(searchKey);

		if (cached != null) {
			sEvents = (List<SignupMeeting>) cached;
			return sEvents;
		}

		/* Case: if not cached */

		//get data from db then cache and return it
		sEvents = getSignupMeetingDao().getSignupMeetingsInSite(siteId,
				startDate, endDate);

		if (sEvents == null || sEvents.isEmpty()) {
			return null;
		}

		// cache the results for awhile Sakai default: 5 minutes.
		if (m_signupSiteCache != null)
			m_signupSiteCache.put(searchKey, sEvents);
		
		//if the expiration time 5 minutes is too long, then we should use the below one.
		//m_signupSiteCache.put(searchKey, sEvents, 3*60); //3 minutes

		return sEvents;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SignupMeeting> getAllSignupMeetingsInSites(
			List<String> siteIds, Date startDate, int searchTimeFrame) {
		List<SignupMeeting> allSearchSitesEvents = new ArrayList<SignupMeeting>();
		if (siteIds != null) {
			for (String siteId : siteIds) {
				List<SignupMeeting> oneSiteEvents = getAllSignupMeetingsInSite(
						siteId, startDate, searchTimeFrame);
				//add in
				if (oneSiteEvents != null && !oneSiteEvents.isEmpty()) {
					allSearchSitesEvents.addAll(oneSiteEvents);
				}
			}
		}

		return allSearchSitesEvents;
	}
	
	/*
	 * Encapsulate the null-check on the cache itself and return the object
	 */
	private Object getCachedValue(String searchKey) {
		return (m_signupSiteCache != null) ? m_signupSiteCache.get(searchKey): null;
	}


	/**
	 * Returns to uninitialized state.
	 */
	public void destroy() {
		if (m_signupSiteCache != null) {
			m_signupSiteCache.destroy();

		}

		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Object refresh(Object key, Object oldValue, Event event) {
		/*
		 * instead of refreshing when an entry expires, let it go and we'll get
		 * it again if needed
		 */
		return null;
	}

	public MemoryService getMemoryService() {
		return memoryService;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

	public SignupMeetingDao getSignupMeetingDao() {
		return signupMeetingDao;
	}

	public void setSignupMeetingDao(SignupMeetingDao signupMeetingDao) {
		this.signupMeetingDao = signupMeetingDao;
	}

}
