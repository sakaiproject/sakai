/**********************************************************************************
 * Copyright 2010 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.model;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.sakaiproject.mailsender.model.EmailRole.Type;

public class EmailRoleTest {
	EmailRole role;

	String realmid = "realmId";
	String roleid = "roleId";
	String rolesingular = "roleSingular";
	String roleplural = "rolePlural";
	Type roleType = Type.ROLE;
	boolean groupAware = true;

	@Test
	public void constructor() {
		role = new EmailRole(realmid, roleid, rolesingular, roleplural, roleType);
		assertEquals(realmid, role.getRealmId());
		assertEquals(roleid, role.getRoleId());
		assertEquals(rolesingular, role.getRoleSingular());
		assertEquals(roleplural, role.getRolePlural());
		assertEquals(roleType, role.getType());
	}

	@Test
	public void constructorGroupAware() {
		role = new EmailRole(realmid, roleid, rolesingular, roleplural, roleType, groupAware);
		assertEquals(realmid, role.getRealmId());
		assertEquals(roleid, role.getRoleId());
		assertEquals(rolesingular, role.getRoleSingular());
		assertEquals(roleplural, role.getRolePlural());
		assertEquals(roleType, role.getType());
		assertEquals(groupAware, role.isGroupAware());
	}

	@Test
	public void differentTypes() {
		role = new EmailRole(realmid, roleid, rolesingular, roleplural, Type.GROUP);
		assertEquals(role.getType(), Type.GROUP);

		role = new EmailRole(realmid, roleid, rolesingular, roleplural, Type.SECTION);
		assertEquals(role.getType(), Type.SECTION);
	}
}
