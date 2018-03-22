/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.api;

import java.util.Date;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * User models a Sakai end-user.
 * </p>
 */
public interface User extends Entity, Comparable
{
	/**
	 * @return the user who created this.
	 */
	User getCreatedBy();

	/**
	 * @return the user who last modified this.
	 */
	User getModifiedBy();


	
	/**
	 * @return the time created.
	 */
	Date getCreatedDate();


	/**
	 * @return the time last modified.
	 */
	Date getModifiedDate();

	
	/**
	 * Access the email address.
	 * 
	 * @return The email address string.
	 */
	String getEmail();

	/**
	 * Access the user's name for display purposes.
	 * 
	 * @return The user's name for display purposes.
	 */
	String getDisplayName();

	/**
	 * Access the user's name for sorting purposes.
	 * 
	 * @return The user's name for sorting purposes, never <code>null</code>
	 */
	String getSortName();

	/**
	 * Access the user's first name.
	 * 
	 * @return The user's first name.
	 */
	String getFirstName();

	/**
	 * Access the user's last name.
	 * 
	 * @return The user's last name.
	 */
	String getLastName();

	/**
	 * Check if this is the user's password.
	 * 
	 * @param pw
	 *        The clear text password to check.
	 * @return true if the password matches, false if not.
	 */
	boolean checkPassword(String pw);

	/**
	 * Access the user type.
	 * 
	 * @return The user type.
	 */
	String getType();

	/**
	 * Access the user's enterprise id; the id they and the enterprise know as belonging to them.<br />
	 * The Enterprise id, like the User id, is unique among all defined users.<br />
	 * The EID may be used by the user to login, and will be used when communicating with the user directory provider.
	 * 
	 * @return The user's enterprise id.
	 */
	String getEid();

	/**
	 * Access a string portraying the user's enterprise identity, for display purposes.<br />
	 * Use this, not getEid(), when displaying the user's id, probably along with the user's sort or display name, for disambiguating purposes.
	 * 
	 * @return The user's display id string.
	 */
	String getDisplayId();
}
