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

package org.sakaiproject.userauditservice.impl.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.userauditservice.api.UserAuditLogQuery;
import org.sakaiproject.userauditservice.api.UserAuditSortColumn;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;
import org.sakaiproject.userauditservice.api.repository.UserAuditLogRepository;
import org.sakaiproject.userauditservice.impl.test.UserAuditTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { UserAuditTestConfiguration.class })
@Transactional
public class UserAuditLogRepositoryImplTest {

	@Autowired
	private UserAuditLogRepository userAuditLogRepository;

	private static final String SITE_ID = "site-a";
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@Before
	public void setUp() {
		saveLog("user-a", "A", timestamp(2026, 6, 1, 10));
		saveLog("user-b", "R", timestamp(2026, 6, 2, 10));
		saveLog("user-c", "U", timestamp(2026, 6, 3, 10));
	}

	@Test
	public void countMatchesSiteRows() {
		UserAuditLogQuery query = UserAuditLogQuery.builder().siteId(SITE_ID).build();
		assertEquals(3L, userAuditLogRepository.count(query));
	}

	@Test
	public void countFiltersByUserId() {
		UserAuditLogQuery query = UserAuditLogQuery.builder().siteId(SITE_ID).userId("user-b").build();
		assertEquals(1L, userAuditLogRepository.count(query));
	}

	@Test
	public void countFiltersByDateRange() {
		UserAuditLogQuery query = UserAuditLogQuery.builder()
				.siteId(SITE_ID)
				.fromAuditStamp(instant(2026, 6, 2, 0))
				.toAuditStamp(instant(2026, 6, 4, 0))
				.build();
		assertEquals(2L, userAuditLogRepository.count(query));
	}

	@Test
	public void findSortsByUserIdAscending() {
		UserAuditLogQuery query = UserAuditLogQuery.builder()
				.siteId(SITE_ID)
				.sortColumn(UserAuditSortColumn.USER_ID)
				.sortAscending(true)
				.build();

		List<UserAuditLog> rows = userAuditLogRepository.find(query);
		assertEquals(3, rows.size());
		assertEquals("user-a", rows.get(0).getUserId());
		assertEquals("user-b", rows.get(1).getUserId());
		assertEquals("user-c", rows.get(2).getUserId());
	}

	@Test
	public void findAppliesPaging() {
		UserAuditLogQuery query = UserAuditLogQuery.builder()
				.siteId(SITE_ID)
				.sortColumn(UserAuditSortColumn.USER_ID)
				.sortAscending(true)
				.offset(1)
				.limit(1)
				.build();

		List<UserAuditLog> rows = userAuditLogRepository.find(query);
		assertEquals(1, rows.size());
		assertEquals("user-b", rows.get(0).getUserId());
	}

	@Test
	public void findMapsActionTextSortToActionTakenColumn() {
		UserAuditLogQuery query = UserAuditLogQuery.builder()
				.siteId(SITE_ID)
				.sortColumn(UserAuditSortColumn.ACTION_TEXT)
				.sortAscending(false)
				.build();

		List<UserAuditLog> rows = userAuditLogRepository.find(query);
		assertEquals("U", rows.get(0).getActionTaken());
		assertEquals("R", rows.get(1).getActionTaken());
		assertEquals("A", rows.get(2).getActionTaken());
	}

	@Test
	public void deleteBySiteIdRemovesSiteRows() {
		assertEquals(3, userAuditLogRepository.deleteBySiteId(SITE_ID));
		assertEquals(0L, userAuditLogRepository.count(UserAuditLogQuery.builder().siteId(SITE_ID).build()));
	}

	@Test
	public void deleteBySiteIdLeavesOtherSitesUntouched() {
		saveLog("other-site", "user-z", "A", timestamp(2026, 6, 4, 10));

		assertEquals(3, userAuditLogRepository.deleteBySiteId(SITE_ID));
		assertEquals(1L, userAuditLogRepository.count(UserAuditLogQuery.builder().siteId("other-site").build()));
	}

	@Test
	public void findCombinesUserAndDatePredicates() {
		saveLog("user-a", "A", timestamp(2026, 6, 8, 10));

		UserAuditLogQuery query = UserAuditLogQuery.builder()
				.siteId(SITE_ID)
				.userId("user-a")
				.fromAuditStamp(instant(2026, 6, 8, 0))
				.toAuditStamp(instant(2026, 6, 9, 0))
				.build();

		assertEquals(1L, userAuditLogRepository.count(query));
		List<UserAuditLog> rows = userAuditLogRepository.find(query);
		assertEquals(1, rows.size());
		assertTrue(rows.get(0).getAuditStamp().isAfter(instant(2026, 6, 7, 23)));
	}

	private UserAuditLog saveLog(String userId, String actionTaken, Instant auditStamp) {
		return saveLog(SITE_ID, userId, actionTaken, auditStamp);
	}

	private UserAuditLog saveLog(String siteId, String userId, String actionTaken, Instant auditStamp) {
		UserAuditLog auditLog = new UserAuditLog();
		auditLog.setSiteId(siteId);
		auditLog.setUserId(userId);
		auditLog.setRoleName("access");
		auditLog.setActionTaken(actionTaken);
		auditLog.setAuditStamp(auditStamp);
		auditLog.setSource("M");
		auditLog.setActionUserId("admin");
		return userAuditLogRepository.save(auditLog);
	}

	private Instant timestamp(int year, int month, int day) {
		return timestamp(year, month, day, 0);
	}

	private Instant timestamp(int year, int month, int day, int hour) {
		return instant(year, month, day, hour);
	}

	private Instant instant(int year, int month, int day) {
		return instant(year, month, day, 0);
	}

	private Instant instant(int year, int month, int day, int hour) {
		return LocalDate.of(year, month, day).atTime(hour, 0).atZone(UTC.toZoneId()).toInstant();
	}
}
