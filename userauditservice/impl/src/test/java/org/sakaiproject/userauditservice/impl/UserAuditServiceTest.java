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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.userauditservice.api.UserAuditLogQuery;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.userauditservice.api.model.UserAuditEntry;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;
import org.sakaiproject.userauditservice.api.repository.UserAuditLogRepository;

public class UserAuditServiceTest {

	private UserAuditLogRepository userAuditLogRepository;
	private UserAuditService userAuditService;

	@Before
	public void setUp() {
		userAuditLogRepository = mock(UserAuditLogRepository.class);
		UserAuditServiceImpl userAuditServiceImpl = new UserAuditServiceImpl();
		userAuditServiceImpl.setUserAuditLogRepository(userAuditLogRepository);
		userAuditService = userAuditServiceImpl;
	}

	@Test
	public void registerTracksItemsAndDerivesKeysOnDemand() {
		UserAuditRegistration first = mock(UserAuditRegistration.class);
		when(first.getDatabaseSourceKey()).thenReturn("M");
		UserAuditRegistration second = mock(UserAuditRegistration.class);
		when(second.getDatabaseSourceKey()).thenReturn("S");

		userAuditService.register(first);
		userAuditService.register(second);

		assertEquals(List.of(first, second), userAuditService.getRegisteredItems());
		assertEquals(List.of("M", "S"), userAuditService.getKeys());
	}

	@Test
	public void addToUserAuditingSavesAuditEntriesAsJpaEntities() {
		List<UserAuditEntry> userAuditEntries = List.of(
				UserAuditEntry.of("site-a", "user-a", "maintain", "A", "M", "admin-a"),
				UserAuditEntry.of("site-a", "user-b", "access", "R", "S", "admin-b"));

		userAuditService.addToUserAuditing(userAuditEntries);

		ArgumentCaptor<Iterable<UserAuditLog>> auditLogsCaptor = ArgumentCaptor.forClass(Iterable.class);
		verify(userAuditLogRepository).saveAll(auditLogsCaptor.capture());

		List<UserAuditLog> auditLogs = new ArrayList<UserAuditLog>();
		for (UserAuditLog auditLog : auditLogsCaptor.getValue()) {
			auditLogs.add(auditLog);
		}

		assertEquals(2, auditLogs.size());
		assertAuditLog(auditLogs.get(0), "site-a", "user-a", "maintain", "A", "M", "admin-a");
		assertAuditLog(auditLogs.get(1), "site-a", "user-b", "access", "R", "S", "admin-b");
	}

	@Test
	public void addToUserAuditingUsesNonDecreasingAuditStamps() {
		List<UserAuditEntry> userAuditEntries = List.of(
				UserAuditEntry.of("site-a", "user-a", "maintain", "A", "M", "admin-a"),
				UserAuditEntry.of("site-a", "user-b", "access", "R", "S", "admin-b"));

		userAuditService.addToUserAuditing(userAuditEntries);

		ArgumentCaptor<Iterable<UserAuditLog>> auditLogsCaptor = ArgumentCaptor.forClass(Iterable.class);
		verify(userAuditLogRepository).saveAll(auditLogsCaptor.capture());

		List<UserAuditLog> auditLogs = new ArrayList<UserAuditLog>();
		for (UserAuditLog auditLog : auditLogsCaptor.getValue()) {
			auditLogs.add(auditLog);
		}
		assertTrue(auditLogs.get(0).getAuditStamp().compareTo(auditLogs.get(1).getAuditStamp()) <= 0);
	}

	@Test
	public void addToUserAuditingSkipsEmptyInput() {
		userAuditService.addToUserAuditing(null);
		userAuditService.addToUserAuditing(List.of());

		verify(userAuditLogRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
	}

	@Test
	public void addToUserAuditingSkipsNullEntries() {
		List<UserAuditEntry> userAuditEntries = new ArrayList<UserAuditEntry>();
		userAuditEntries.add(null);
		userAuditEntries.add(UserAuditEntry.of("site-a", "user-b", "access", "R", "S", "admin-b"));

		userAuditService.addToUserAuditing(userAuditEntries);

		ArgumentCaptor<Iterable<UserAuditLog>> auditLogsCaptor = ArgumentCaptor.forClass(Iterable.class);
		verify(userAuditLogRepository).saveAll(auditLogsCaptor.capture());

		int savedRows = 0;
		for (UserAuditLog ignored : auditLogsCaptor.getValue()) {
			savedRows++;
		}
		assertEquals(1, savedRows);
	}

	@Test
	public void deleteUserAuditingFromSiteDelegatesToRepository() {
		userAuditService.deleteUserAuditingFromSite("site-a");

		verify(userAuditLogRepository).deleteBySiteId("site-a");
	}

	@Test
	public void deleteUserAuditingFromSiteSkipsNullSiteId() {
		userAuditService.deleteUserAuditingFromSite(null);

		verify(userAuditLogRepository, never()).deleteBySiteId(org.mockito.ArgumentMatchers.any());
	}

	@Test
	public void countUserAuditLogsDelegatesToRepository() {
		UserAuditLogQuery query = UserAuditLogQuery.builder().siteId("site-a").build();
		when(userAuditLogRepository.count(same(query))).thenReturn(7L);

		assertEquals(7L, userAuditService.countUserAuditLogs(query));
	}

	@Test
	public void countUserAuditLogsReturnsZeroWithoutSiteId() {
		assertEquals(0L, userAuditService.countUserAuditLogs(null));
		assertEquals(0L, userAuditService.countUserAuditLogs(UserAuditLogQuery.builder().build()));
		verify(userAuditLogRepository, never()).count(org.mockito.ArgumentMatchers.any());
	}

	@Test
	public void getUserAuditLogsDelegatesToRepository() {
		UserAuditLogQuery query = UserAuditLogQuery.builder().siteId("site-a").build();
		UserAuditLog auditLog = new UserAuditLog();
		List<UserAuditLog> auditLogs = List.of(auditLog);
		when(userAuditLogRepository.find(same(query))).thenReturn(auditLogs);

		assertSame(auditLogs, userAuditService.getUserAuditLogs(query));
	}

	@Test
	public void getUserAuditLogsReturnsEmptyWithoutSiteId() {
		assertTrue(userAuditService.getUserAuditLogs(null).isEmpty());
		assertTrue(userAuditService.getUserAuditLogs(UserAuditLogQuery.builder().build()).isEmpty());
		verify(userAuditLogRepository, never()).find(org.mockito.ArgumentMatchers.any());
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
}
