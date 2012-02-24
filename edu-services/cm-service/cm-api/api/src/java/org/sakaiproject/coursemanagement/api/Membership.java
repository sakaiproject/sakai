/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.api;

/**
 * A user-role pair associated with something.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface Membership {
	/**
	 * The user's enterprise id
	 * @return
	 */
	public String getUserId();
	
	/**
	 * The user's role
	 * @return
	 */
	public String getRole();


	/**
	 * What authority defines this object?
	 * @return 
	 */
	public String getAuthority();

	/**
	 * Gets the status of this Membership.  This might be active, inactive for example.
	 * @return
	 */
	public String getStatus();
	public void setStatus(String status);
}
