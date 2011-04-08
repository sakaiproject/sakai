/******************************************************************************
 * TestUser.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2006 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.logic.stubs;

import java.util.Date;
import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
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
@SuppressWarnings("unchecked")
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

	public String getDisplayName() {
		return this.displayName;
	}

	public String getEid() {
		return this.userEid;
	}

	public String getEmail() {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public Date getCreatedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}
}
