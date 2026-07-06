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

package org.sakaiproject.userauditservice.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.userauditservice.api.UserAuditLogQuery;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.userauditservice.api.UserAuditSortColumn;
import org.sakaiproject.userauditservice.api.model.UserAuditEntry;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;
import org.sakaiproject.userauditservice.impl.test.UserAuditTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { UserAuditTestConfiguration.class })
@Transactional
public class UserAuditServiceTest {

	private static final String SITE_ID = "site-service";
	private static final String OTHER_SITE_ID = "site-other";

	@Autowired
	private UserAuditService userAuditService;

	@Test
	public void registerTracksItemsAndDerivesKeysOnDemand() {
		UserAuditRegistration first = new TestUserAuditRegistration("M");
		UserAuditRegistration second = new TestUserAuditRegistration("S");

		userAuditService.register(first);
		userAuditService.register(second);

		assertEquals(List.of(first, second), userAuditService.getRegisteredItems());
		assertEquals(List.of("M", "S"), userAuditService.getKeys());
	}

	@Test
	public void addToUserAuditingPersistsAuditEntries() {
		List<UserAuditEntry> userAuditEntries = List.of(
				UserAuditEntry.of(SITE_ID, "user-a", "maintain", "A", "M", "admin-a"),
				UserAuditEntry.of(SITE_ID, "user-b", "access", "D", "S", "admin-b"));

		userAuditService.addToUserAuditing(userAuditEntries);

		List<UserAuditLog> auditLogs = userAuditService.getUserAuditLogs(UserAuditLogQuery.builder()
				.siteId(SITE_ID)
				.sortColumn(UserAuditSortColumn.USER_ID)
				.sortAscending(true)
				.build());

		assertEquals(2, auditLogs.size());
		assertAuditLog(auditLogs.get(0), SITE_ID, "user-a", "maintain", "A", "M", "admin-a");
		assertAuditLog(auditLogs.get(1), SITE_ID, "user-b", "access", "D", "S", "admin-b");
	}

	@Test
	public void addToUserAuditingUsesNonDecreasingAuditStamps() {
		List<UserAuditEntry> userAuditEntries = List.of(
				UserAuditEntry.of(SITE_ID, "user-a", "maintain", "A", "M", "admin-a"),
				UserAuditEntry.of(SITE_ID, "user-b", "access", "D", "S", "admin-b"));

		userAuditService.addToUserAuditing(userAuditEntries);

		List<UserAuditLog> auditLogs = userAuditService.getUserAuditLogs(UserAuditLogQuery.builder()
				.siteId(SITE_ID)
				.sortAscending(true)
				.build());

		assertEquals(2, auditLogs.size());
		assertTrue(auditLogs.get(0).getAuditStamp().compareTo(auditLogs.get(1).getAuditStamp()) <= 0);
	}

	@Test
	public void addToUserAuditingSkipsEmptyInput() {
		userAuditService.addToUserAuditing(null);
		userAuditService.addToUserAuditing(List.of());

		assertEquals(0L, userAuditService.countUserAuditLogs(UserAuditLogQuery.builder().siteId(SITE_ID).build()));
	}

	@Test
	public void addToUserAuditingSkipsNullEntries() {
		List<UserAuditEntry> userAuditEntries = new ArrayList<UserAuditEntry>();
		userAuditEntries.add(null);
		userAuditEntries.add(UserAuditEntry.of(SITE_ID, "user-b", "access", "D", "S", "admin-b"));

		userAuditService.addToUserAuditing(userAuditEntries);

		assertEquals(1L, userAuditService.countUserAuditLogs(UserAuditLogQuery.builder().siteId(SITE_ID).build()));
	}

	@Test
	public void deleteUserAuditingFromSiteRemovesMatchingRows() {
		userAuditService.addToUserAuditing(List.of(
				UserAuditEntry.of(SITE_ID, "user-a", "maintain", "A", "M", "admin-a"),
				UserAuditEntry.of(OTHER_SITE_ID, "user-z", "access", "A", "M", "admin-a")));

		userAuditService.deleteUserAuditingFromSite(SITE_ID);

		assertEquals(0L, userAuditService.countUserAuditLogs(UserAuditLogQuery.builder().siteId(SITE_ID).build()));
		assertEquals(1L, userAuditService.countUserAuditLogs(UserAuditLogQuery.builder().siteId(OTHER_SITE_ID).build()));
	}

	@Test
	public void deleteUserAuditingFromSiteSkipsNullSiteId() {
		userAuditService.addToUserAuditing(List.of(
				UserAuditEntry.of(SITE_ID, "user-a", "maintain", "A", "M", "admin-a")));

		userAuditService.deleteUserAuditingFromSite(null);

		assertEquals(1L, userAuditService.countUserAuditLogs(UserAuditLogQuery.builder().siteId(SITE_ID).build()));
	}

	@Test
	public void countUserAuditLogsCountsPersistedRows() {
		userAuditService.addToUserAuditing(List.of(
				UserAuditEntry.of(SITE_ID, "user-a", "maintain", "A", "M", "admin-a"),
				UserAuditEntry.of(SITE_ID, "user-b", "access", "D", "S", "admin-b")));

		assertEquals(2L, userAuditService.countUserAuditLogs(UserAuditLogQuery.builder().siteId(SITE_ID).build()));
	}

	@Test
	public void countUserAuditLogsReturnsZeroWithoutSiteId() {
		assertEquals(0L, userAuditService.countUserAuditLogs(null));
		assertEquals(0L, userAuditService.countUserAuditLogs(UserAuditLogQuery.builder().build()));
	}

	@Test
	public void getUserAuditLogsReturnsPersistedRows() {
		userAuditService.addToUserAuditing(List.of(
				UserAuditEntry.of(SITE_ID, "user-a", "maintain", "A", "M", "admin-a")));

		List<UserAuditLog> auditLogs = userAuditService.getUserAuditLogs(UserAuditLogQuery.builder().siteId(SITE_ID).build());

		assertEquals(1, auditLogs.size());
		assertAuditLog(auditLogs.get(0), SITE_ID, "user-a", "maintain", "A", "M", "admin-a");
	}

	@Test
	public void getUserAuditLogsReturnsEmptyWithoutSiteId() {
		assertTrue(userAuditService.getUserAuditLogs(null).isEmpty());
		assertTrue(userAuditService.getUserAuditLogs(UserAuditLogQuery.builder().build()).isEmpty());
	}

	private void assertAuditLog(UserAuditLog auditLog, String siteId, String userId, String roleName,
			String actionTaken, String source, String actionUserId) {

		assertEquals(siteId, auditLog.getSiteId());
		assertEquals(userId, auditLog.getUserId());
		assertEquals(roleName, auditLog.getRoleName());
		assertEquals(actionTaken, auditLog.getActionTaken());
		assertEquals(source, auditLog.getSource());
		assertEquals(actionUserId, auditLog.getActionUserId());
		assertNotNull(auditLog.getAuditStamp());
	}

	private static class TestUserAuditRegistration implements UserAuditRegistration {

		private final String databaseSourceKey;

		private TestUserAuditRegistration(String databaseSourceKey) {
			this.databaseSourceKey = databaseSourceKey;
		}

		@Override
		public String getDatabaseSourceKey() {
			return databaseSourceKey;
		}

		@Override
		public String getSourceText(String parameter) {
			return databaseSourceKey;
		}

		@Override
		public Object getResourceLoader(String location) {
			return null;
		}
	}
}
