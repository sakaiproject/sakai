/**
 * Copyright (c) 2007-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.memory.api.Cache;
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
@Slf4j
public class SignupCacheServiceImpl implements SignupCacheService {

	private MemoryService memoryService;

	protected SignupMeetingDao signupMeetingDao;

	private Cache m_signupSiteCache = null;

	/** The one and only one client. */
	public static SignupCacheServiceImpl m_instance = null;

	public void init() {
		
		/* Here we use sakai default cache expiration: 5 minutes */
		try {
			// The other parameters are not needed for this cache
			m_signupSiteCache = memoryService
					.getCache("org.sakaiproject.signup.logic.siteCache");
			if (m_instance == null) {
				m_instance = this;
			}

			if (log.isDebugEnabled()) {
				log.debug(this + ".init()");
			}
		} catch (Exception t) {
			log.warn(this + "init(): ", t);
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
			m_signupSiteCache.close();

		}

		log.info("destroy()");
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
