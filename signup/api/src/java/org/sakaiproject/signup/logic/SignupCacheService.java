/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/api/src/java/org/sakaiproject/signup/logic/SignupCacheService.java $
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

import java.util.Date;
import java.util.List;

import org.sakaiproject.signup.model.SignupMeeting;

/**
 * <p>
 * SignupMeetingService is an interface, which provides methods to cache the search results of 
 * a list of the SignupMeeting objects from the DB. 
 * </p>
 * 
 * @author Peter Liu
 * 
 */
public interface SignupCacheService {

	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * the sites
	 * 
	 * @param siteIds
	 *            a collection of unique ids which represents the multiple sites
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param timeFrameInDays
	 *            search time frame as int value.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getAllSignupMeetingsInSites(List<String> siteIds, Date startDate, int timeFrameInDays);
	
	/**
	 * This returns a subset list of SignupMeeting from startDate to endDate for
	 * a specific site
	 * 
	 * @param siteIds
	 *            a collection of unique ids which represents the multiple sites
	 * @param startDate
	 *            date,which constraints the search starting date.
	 * @param timeFrameInDays
	 *            search time frame as int value.
	 * @return a list of SignupMeeting objects
	 */
	List<SignupMeeting> getAllSignupMeetingsInSite(String siteId, Date startDate,  int timeFrameInDays);
}
