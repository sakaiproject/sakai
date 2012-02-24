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
package org.sakaiproject.mailsender.model;

/**
 * EmailRole (can be a role, a section, a group)
 *
 * @author kimsooil
 * @author Carl Hall <carl.hall@et.gatech.edu>
 */
public class EmailRole
{
	public enum Type
	{
		GROUP, SECTION, ROLE
	}

	private final String realmId;
	private final String roleId;
	private final String roleSingular;
	private final String rolePlural;
	private final Type roleType;
	private boolean groupAware;

	public EmailRole(String realmid, String roleid, String rolesingular, String roleplural,
			Type roleType)
	{
		realmId = realmid;
		roleId = roleid;
		roleSingular = rolesingular;
		rolePlural = roleplural;
		this.roleType = roleType;
	}

	public EmailRole(String realmid, String roleid, String rolesingular, String roleplural,
			Type roleType, boolean groupAware)
	{
		this(realmid, roleid, rolesingular, roleplural, roleType);
		this.groupAware = groupAware;
	}

	public String getRealmId()
	{
		return realmId;
	}

	public String getRoleId()
	{
		return roleId;
	}

	public String getRoleSingular()
	{
		return roleSingular;
	}

	public String getRolePlural()
	{
		return rolePlural;
	}

	public boolean isGroupAware()
	{
		return groupAware;
	}

	public Type getType()
	{
		return roleType;
	}
}