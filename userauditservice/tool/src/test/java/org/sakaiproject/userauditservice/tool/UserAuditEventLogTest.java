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

package org.sakaiproject.userauditservice.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;

public class UserAuditEventLogTest {

	private UserAuditEventLog eventLog;

	@Before
	public void setUp() {
		ComponentManager.testingMode = true;
		eventLog = new UserAuditEventLog();
	}

	@After
	public void tearDown() {
		ComponentManager.shutdown();
		ComponentManager.testingMode = false;
	}

	@Test
	public void getRowsNumberUsesPositivePageSize() {
		eventLog.setTotalItems(500);
		eventLog.setFirstItem(0);
		eventLog.setPageSize(200);
		assertEquals(200, eventLog.getRowsNumber());
	}

	@Test
	public void getRowsNumberReturnsRemainingOnLastPage() {
		eventLog.setTotalItems(250);
		eventLog.setFirstItem(200);
		eventLog.setPageSize(200);
		assertEquals(50, eventLog.getRowsNumber());
	}

	@Test
	public void getRowsNumberReturnsTotalWhenSmallerThanPageSize() {
		eventLog.setTotalItems(50);
		eventLog.setFirstItem(0);
		eventLog.setPageSize(200);
		assertEquals(50, eventLog.getRowsNumber());
	}

	@Test
	public void getRowsNumberShowsAllWhenPageSizeIsZero() {
		eventLog.setTotalItems(500);
		eventLog.setFirstItem(0);
		eventLog.setPageSize(0);
		assertEquals(500, eventLog.getRowsNumber());
	}

	@Test
	public void getRowsNumberShowsAllWhenPageSizeIsNegative() {
		eventLog.setTotalItems(12);
		eventLog.setFirstItem(0);
		eventLog.setPageSize(-1);
		assertEquals(12, eventLog.getRowsNumber());
	}

	@Test
	public void orderByClauseDefaultsToAuditStampDescending() {
		assertEquals("audit_stamp desc, user_id asc", UserAuditEventLog.orderByClause("auditStamp", false));
	}

	@Test
	public void orderByClauseMapsKnownColumns() {
		assertEquals("user_id asc, user_id asc", UserAuditEventLog.orderByClause("userId", true));
		assertEquals("action_taken desc, user_id asc", UserAuditEventLog.orderByClause("actionText", false));
	}

	@Test
	public void appendPagingUsesMysqlLimitSyntax() {
		String sql = UserAuditEventLog.appendPaging(UserAuditEventLog.GET_EVENTS_BASE_SQL, 200, 200, "mysql");
		assertEquals(UserAuditEventLog.GET_EVENTS_BASE_SQL + " limit 200,200", sql);
	}

	@Test
	public void appendPagingUsesOracleRowNumWrapper() {
		String sql = UserAuditEventLog.appendPaging(UserAuditEventLog.GET_EVENTS_BASE_SQL, 400, 200, "oracle");
		assertTrue(sql.contains("rownum rnum"));
		assertTrue(sql.contains("rnum >= 401"));
		assertTrue(sql.contains("rownum <= 600"));
	}

	@Test
	public void appendPagingUsesHsqldbLimitSyntax() {
		String sql = UserAuditEventLog.appendPaging(UserAuditEventLog.GET_EVENTS_BASE_SQL, 20, 100, "hsqldb");
		assertEquals("select limit 20 100 user_id, role_name, action_taken, audit_stamp, source, action_user_id from user_audits_log where site_id=?", sql);
	}
}
