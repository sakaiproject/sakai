/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.logic.test.stubs;

import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.poll.logic.test.TestDataPreload;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test class for the Sakai User object<br/>
 * This has to be here since I cannot create a User object in Sakai for some 
 * reason... sure would be nice if I could though -AZ
 * @author Sakai App Builder -AZ
 */
public class FakeUser implements User {
	private String userId;
	private String userEid = "fakeEid";
	private String displayName = "Fake DisplayName";

   public FakeUser() { }

	/**
	 * Construct an empty test user with an id set
	 * @param userId a id string
	 */
	public FakeUser(String userId) {
		this.userId = userId;
	}
	
	/**
	 * Construct an empty test user with an id and eid set
	 * @param userId a id string
	 * @param userEid a username string
	 */
	public FakeUser(String userId, String userEid) {
		this.userId = userId;
		this.userEid = userEid;
	}

	/**
	 * Construct an empty test user with an id and eid set
	 * @param userId a id string
	 * @param userEid a username string
	 * @param displayName a user display name
	 */
	public FakeUser(String userId, String userEid, String displayName) {
		this.userId = userId;
		this.userEid = userEid;
		this.displayName = displayName;
	}


	public boolean checkPassword(String pw) {
		// TODO Auto-generated method stub
		return false;
	}

	public User getCreatedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDisplayId() {
		// TODO Auto-generated method stub
		return null;
	}

	//needed for UCT build please don't remove
	public String getDisplayName(String context) {
	    return null;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getEid() {
		return this.userEid;
	}

	public String getEmail() {
		if (userId.equals(TestDataPreload.USER_LOC_3_UPDATE_1)) {
			return TestDataPreload.USER_LOC_3_UPDATE_1_EMAIL;
		} else if (userId.equals(TestDataPreload.USER_LOC_3_UPDATE_2)) {
			return TestDataPreload.USER_LOC_3_UPDATE_2_EMAIL;
		} else if (userId.equals(TestDataPreload.USER_LOC_3_UPDATE_3)) {
			return TestDataPreload.USER_LOC_3_UPDATE_3_EMAIL;		
		} else if (userId.equals(TestDataPreload.USER_LOC_3_NO_UPDATE_1)) {
			return TestDataPreload.USER_LOC_3_NO_UPDATE_1_EMAIL;
		} else if (userId.equals(TestDataPreload.USER_LOC_3_NO_UPDATE_2)) {
			return TestDataPreload.USER_LOC_3_NO_UPDATE_2_EMAIL;
		} else {
			return "";
		}
	}

	public String getFirstName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLastName() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getModifiedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSortName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		return userId;
	}

	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference(String rootProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl(String rootProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	public Element toXml(Document doc, Stack stack) {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Time getCreatedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getModifiedTime() {
		// TODO Auto-generated method stub
		return null;
	}
}
