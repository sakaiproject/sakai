/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.userauditservice.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UserAuditEntryTest {

	@Test
	public void ofMapsAllFields() {
		UserAuditEntry entry = UserAuditEntry.of("site-a", "user-a", "maintain", "A", "M", "admin-a");

		assertEquals("site-a", entry.getSiteId());
		assertEquals("user-a", entry.getUserId());
		assertEquals("maintain", entry.getRoleName());
		assertEquals("A", entry.getActionTaken());
		assertEquals("M", entry.getSource());
		assertEquals("admin-a", entry.getActionUserId());
	}

	@Test(expected = NullPointerException.class)
	public void ofRequiresSiteId() {
		UserAuditEntry.of(null, "user-a", "maintain", "A", "M", "admin-a");
	}

	@Test(expected = NullPointerException.class)
	public void ofRequiresUserId() {
		UserAuditEntry.of("site-a", null, "maintain", "A", "M", "admin-a");
	}

	@Test(expected = NullPointerException.class)
	public void ofRequiresRoleName() {
		UserAuditEntry.of("site-a", "user-a", null, "A", "M", "admin-a");
	}

	@Test(expected = NullPointerException.class)
	public void ofRequiresActionTaken() {
		UserAuditEntry.of("site-a", "user-a", "maintain", null, "M", "admin-a");
	}

	@Test
	public void ofAllowsOptionalSourceAndActionUserId() {
		UserAuditEntry entry = UserAuditEntry.of("site-a", "user-a", "maintain", "A", null, null);

		assertNull(entry.getSource());
		assertNull(entry.getActionUserId());
	}
}
