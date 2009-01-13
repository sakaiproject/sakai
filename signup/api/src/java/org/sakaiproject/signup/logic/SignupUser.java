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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.logic;

import org.sakaiproject.authz.api.Role;

/**
 * <P>
 * This class is a place holder, which contains all the user necessary
 * informaion for Signup Tool
 * </P>
 */
public class SignupUser implements Comparable {

	private String eid;

	private String internalUserId;

	private String firstName;

	private String lastName;

	private Role userRole;
	
	private String mainSiteId;
	
	private boolean publishedSite;

	/**
	 * Construtor
	 * 
	 * @param eid
	 *            an unique Sakai enterprise Id(UserId for UI)
	 * @param internalUserId
	 *            an unique Sakai internal used Id(not UserId)
	 * @param firstName
	 *            First Name of the user
	 * @param lastName
	 *            Last Name of the user
	 * @param role
	 *            a Sakai Role object
	 */
	public SignupUser(String eid, String internalUserId, String firstName, String lastName, Role role,String siteId,boolean publishedSite) {
		this.eid = eid;
		this.internalUserId = internalUserId;
		this.firstName = firstName;
		this.lastName = firstCharToUppercase(lastName);
		this.userRole = role;
		this.mainSiteId = siteId;
		this.publishedSite = publishedSite;

	}

	/**
	 * get Sakai Enterprise Id
	 * 
	 * @return a unique eid
	 */
	public String getEid() {
		return eid;
	}

	/**
	 * get a unique Sakai internal used Id
	 * 
	 * @return a unique Sakai internal used Id
	 */
	public String getInternalUserId() {
		return internalUserId;
	}

	/**
	 * check to see if the objects are equal
	 */
	public boolean equals(Object other) {
		if (other == null || !(other instanceof SignupUser))
			return false;
		SignupUser user = (SignupUser) other;

		return eid.equals(user.getEid());
	}

	public int hashCode() {
		return eid.hashCode();
	}

	/**
	 * for sorting purpose. It's according to string alphabetic order. Last name
	 * comes first
	 */
	public int compareTo(Object o) {
		if (o == null)
			return -1;
		if (!(o instanceof SignupUser))
			throw new ClassCastException("Not type of SignupUser");

		SignupUser other = (SignupUser) o;

		if (lastName == null)
			return -1;

		int value = lastName.compareTo(other.getLastName());
		if (value != 0)
			return value;

		if (firstName == null)
			return value;// value=0

		return firstName.compareTo(other.getFirstName());

	}

	/**
	 * get first name of the user
	 * 
	 * @return the first name of the user
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * get the last name of the user
	 * 
	 * @return the last name of the user
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * get the display name: order of 'last name, first name'
	 * 
	 * @return the display name
	 */
	public String getDisplayName() {
		String name = lastName + ", " + ((firstName != null) ? firstName : "");
		if (name.trim().length()==1)//just ',' character
			name = eid;
		return name;
	}

	/**
	 * get the user Sakai Role object
	 * 
	 * @return the Sakai user Role object
	 */
	public Role getUserRole() {
		return userRole;
	}

	/**
	 * set the user Role object
	 * 
	 * @param userRole
	 *            the user Sakai Role object
	 */
	public void setUserRole(Role userRole) {
		this.userRole = userRole;
	}		

	public String getMainSiteId() {
		return mainSiteId;
	}

	public void setMainSiteId(String mainSiteId) {
		this.mainSiteId = mainSiteId;
	}

	public boolean isPublishedSite() {
		return publishedSite;
	}

	public void setPublishedSite(boolean publishedSite) {
		this.publishedSite = publishedSite;
	}

	private String firstCharToUppercase(String name) {
		if (name != null && name.length() > 0)
			return name.substring(0, 1).toUpperCase() + name.substring(1);

		return name;
	}
}
