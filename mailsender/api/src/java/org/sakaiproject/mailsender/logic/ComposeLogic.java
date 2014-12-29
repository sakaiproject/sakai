/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic;

import java.util.List;

import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.model.EmailRole;
import org.sakaiproject.user.api.User;

public interface ComposeLogic
{

	/**
	 * Read the tool config and build the email roles that are specified
	 *
	 * @return return EmailRoles (called from getEmailGroups())
	 */
	List<EmailRole> getEmailRoles() throws GroupNotDefinedException;

	/**
	 * Get a list of groups available for this tool
	 *
	 * @return
	 */
	List<EmailRole> getEmailGroups() throws IdUnusedException;

	/**
	 * Get the sections as by the section info tool
	 *
	 * @return
	 */
	List<EmailRole> getEmailSections() throws IdUnusedException;

	/**
	 * Get group-aware role which is set in sakai.properties e.g.
	 * "mailsender.group.aware.role=Student,access"
	 *
	 * @return return the String of group-aware role name
	 */
	String getGroupAwareRole();

	/**
	 * // OOTB(Out of the box) Sakai defaults
	 *
	 * @return return default group-aware role by type if type=course, return Student. if
	 *         type=project, return access.
	 */
	String getGroupAwareRoleDefault();

	/**
	 * Retrieve members for the current site that are of a certain role.
	 * 
	 * @param role
	 * @return List of <code>User</code>s that are sorted by last name, first name.
	 * @throws IdUnusedException
	 */
	List<User> getUsersByRole(String role) throws IdUnusedException;

	/**
	 * Gets the numbers of users in a role.
	 * 
	 * @param role
	 * @return
	 */
	int countUsersByRole(String role);

	/**
	 * Retrieve members for the current site that are of a certain group/section
	 *
	 * @param groupId
	 * @return
	 * @throws IdUnusedException
	 */
	List<User> getUsersByGroup(String groupId) throws IdUnusedException;

	/**
	 * Get the number of users in a group.
	 * 
	 * @param groupId
	 * @return
	 */
	int countUsersByGroup(String groupId);

	List<User> getUsers() throws IdUnusedException;
}