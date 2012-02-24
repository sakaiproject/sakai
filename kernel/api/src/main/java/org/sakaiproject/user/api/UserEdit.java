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

import org.sakaiproject.entity.api.Edit;

/**
 * <p>
 * UserEdit is a mutable User object.
 * </p>
 */
public interface UserEdit extends User, Edit
{
	/**
	 * Set the user's id. Note: this is a special purpose routine that is used only to establish the id field, when the id is null, and cannot be used to change a user's id, which is defined to be an un-changing value.
	 * 
	 * @param id
	 *        The user id.
	 */
	void setId(String id);

	/**
	 * Set the user's enterprise id. Must be unique among all users.
	 * 
	 * @param eid
	 *        The new eid value.
	 */
	void setEid(String eid);

	/**
	 * Set the email address.
	 * 
	 * @param email
	 *        The email address string.
	 */
	void setEmail(String email);

	/**
	 * Set the user's first name.
	 * 
	 * @param name
	 *        The user's first name.
	 */
	void setFirstName(String name);

	/**
	 * Set the user's last name.
	 * 
	 * @param name
	 *        The user's last name.
	 */
	void setLastName(String name);

	/**
	 * Set the user's password
	 * 
	 * @param pw
	 *        The user's new password.
	 */
	void setPassword(String pw);

	/**
	 * Set the user type.
	 * 
	 * @param type
	 *        The user type.
	 */
	void setType(String type);
	
	/**
	 * Make the user's first name unchangable during this edit
	 *    
	 */
	void restrictEditFirstName();
	
	/**
	 * Make the user's last name unchangable during this edit
	 *    
	 */
	void restrictEditLastName();
	
	/**
	 * Make the user's email address unchangable during this edit
	 *    
	 */
	void restrictEditEmail();
	
	/**
	 * Make the user's password unchangable during this edit
	 *    
	 */
	void restrictEditPassword();
	
	/**
	 * Make the user's type unchangable during this edit
	 *    
	 */
	void restrictEditType();
	
	/**
	 * Make the users eid unchangeable during the edit
	 */
	void restrictEditEid();
	
}
