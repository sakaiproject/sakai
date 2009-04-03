/***********************************************************************************
 * TestDataPreload.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.logic.test;

import org.sakaiproject.genericdao.api.GenericDao;

public class TestDataPreload {

	/**
	 * current user, access level user in LOCATION_ID1
	 */
	public final static String USER_UPDATE = "user-12345678";
	
	public final static String USER_LOC_3_UPDATE_1 = "user-11112222";
	public final static String USER_LOC_3_UPDATE_1_EMAIL = "user-11112222@qnatest.com";
	
	public final static String USER_LOC_3_UPDATE_2 = "user-22221111";
	public final static String USER_LOC_3_UPDATE_2_EMAIL= "user-22221111@qnatest.com";
	
	public final static String USER_LOC_3_UPDATE_3 = "user-11144422";
	public final static String USER_LOC_3_UPDATE_3_EMAIL = "user-11144422@qnatest.com";	
	
	public final static String USER_LOC_3_NO_UPDATE_1 = "user-77755577";
	public final static String USER_LOC_3_NO_UPDATE_1_EMAIL = "user-77755577@qnatest.com";
	
	public final static String USER_LOC_3_NO_UPDATE_2 = "user-65000011";
	public final static String USER_LOC_3_NO_UPDATE_2_EMAIL = "user-65000011@qnatest.com";
	
	public final static String USER_NO_UPDATE = "user-87654321";
	
	public final static String USER_CUSTOM_EMAIL1 = "user1@qna.com";
	public final static String USER_CUSTOM_EMAIL2 = "user2@qna.com";
	public final static String USER_CUSTOM_EMAIL3 = "user3@qna.com";
	public final static String USER_CUSTOM_EMAIL4 = "user3qna.com";

	public final static String USER_CUSTOM_EMAIL_INVALID = USER_CUSTOM_EMAIL4
			+ "," + USER_CUSTOM_EMAIL1 + " " + USER_CUSTOM_EMAIL2 + ","
			+ USER_CUSTOM_EMAIL3;
	public final static String USER_CUSTOM_EMAIL_VALID = USER_CUSTOM_EMAIL1
			+ "," + USER_CUSTOM_EMAIL2 + " , " + USER_CUSTOM_EMAIL3;

	/**
	 * current location
	 */
	public final static String LOCATION1_ID = "/site/ref-1111111";
	public final static String LOCATION1_TITLE = "Location 1 title";
	public final static String LOCATION2_ID = "/site/ref-22222222";
	public final static String LOCATION2_TITLE = "Location 2 title";
	public final static String LOCATION3_ID = "/site/ref-33333333";
	public final static String LOCATION3_TITLE = "Location 3 title";
	public final static String LOCATION4_ID = "/site/ref-44444444";
	public final static String LOCATION4_TITLE = "Location 4 title";

	public final static String LOCATION1_CONTACT_NAME = "Site Contact Name";
	public final static String LOCATION1_CONTACT_EMAIL = "sitecontact@site.com";


	
	/**
	 * Preload a bunch of test data into the database
	 * 
	 * @param dao
	 *            a generic dao
	 */
	public void preloadTestData(GenericDao dao) {

	}

}
