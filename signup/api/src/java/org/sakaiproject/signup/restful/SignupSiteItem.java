/**********************************************************************************
 * $URL$
 * $Id$
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
 **********************************************************************************/
package org.sakaiproject.signup.restful;

import java.util.List;

/**
 * <p>
 * This class holds the information of sign-up site. It's a wrapper class for
 * RESTful case.
 * </p>
 */
public class SignupSiteItem {

	private String title;

	private String siteId;

	private List<SignupGroupItem> signupGroupItems;

	public SignupSiteItem(String title, String siteId) {
		this.title = title;
		this.siteId = siteId;
	}

	/**
	 * get a list of SignupGroup objects, which belong to the site
	 * 
	 * @return a list of SignupGroup objects
	 */
	public List<SignupGroupItem> getSignupGroupItems() {
		return signupGroupItems;
	}

	/**
	 * this is a setter
	 * 
	 * @param signupGroupItem
	 *            a list of SignupGroup objects
	 */
	public void setSignupGroupItems(List<SignupGroupItem> signupGroupItems) {
		this.signupGroupItems = signupGroupItems;
	}

	/**
	 * get the Site id, which is a sakai unique site Id
	 * 
	 * @return the unique site Id
	 */
	public String getSiteId() {
		return siteId;
	}

	/**
	 * this is a setter
	 * 
	 * @param siteId
	 *            the unique site Id
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/**
	 * get the title of the site
	 * 
	 * @return the name of the site
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * this is a setter
	 * 
	 * @param title
	 *            the name of the site
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * check if the event/meeting is a site scope-wide
	 * 
	 * @return true if the event/meeting is a site scope-wide
	 */
	public boolean isSiteScope() {
		return (signupGroupItems == null || signupGroupItems.isEmpty()) ? true : false;
	}

	/**
	 * check if the two SignupSite object are equal
	 */
	public boolean equals(Object object) {
		if (object == null || !(object instanceof SignupSiteItem))
			return false;
		SignupSiteItem other = (SignupSiteItem) object;

		return (siteId.equals(other.getSiteId()));
	}

	public int hashCode() {
		return siteId.hashCode();
	}
}
