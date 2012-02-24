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

package org.sakaiproject.authz.api;

import java.io.Serializable;

/**
 * <p>
 * Member records membership in an AuthzGroup; user, role, and flags.
 * </p>
 */
public interface Member extends Comparable, Serializable
{
	/**
	 * Access the user id of the member.
	 * 
	 * @return The user id of the member.
	 */
	String getUserId();

	/**
	 * Access the user eid of the member, if we can find it - fall back to the user id if not.
	 * 
	 * @return The user eid of the member.
	 */
	String getUserEid();

	/**
	 * Access the user display id, if we can find it - fall back to the user id if not.
	 * 
	 * @return The user display id of the member.
	 */
	String getUserDisplayId();

	/**
	 * Access the member's Role.
	 * 
	 * @return The member's Role.
	 */
	Role getRole();

	/**
	 * Check if the membership is from the external provider.
	 * 
	 * @return true if the membership is from the external provider, false if not.
	 */
	boolean isProvided();

	/**
	 * Check if the membership is active.
	 * 
	 * @return true if the membership is active, false if not.
	 */
	boolean isActive();

	/**
	 * Set the active value.
	 * 
	 * @param active
	 *        The new active value.
	 */
	void setActive(boolean active);
}
