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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

final class EventLogSqlBuilder {

	static final String COUNT_EVENTS_SQL = "select count(*) from user_audits_log where site_id=?";
	static final String GET_EVENTS_BASE_SQL = "select user_id, role_name, action_taken, audit_stamp, source, action_user_id from user_audits_log where site_id=?";

	private EventLogSqlBuilder() {
	}

	static String buildCountEventsSql(EventLogFilter filter) {
		return appendFilterPredicates(COUNT_EVENTS_SQL, filter);
	}

	static String buildEventsSql(EventLogFilter filter) {
		return appendFilterPredicates(GET_EVENTS_BASE_SQL, filter);
	}

	static String buildPagedEventsSql(EventLogFilter filter, String sortColumn, boolean sortAscending, int offset, int limit, String vendor) {
		String sql = buildEventsSql(filter) + " order by " + orderByClause(sortColumn, sortAscending);
		return appendPaging(sql, offset, limit, vendor);
	}

	static void bindParameters(PreparedStatement statement, String siteId, EventLogFilter filter) throws SQLException {
		int index = 1;
		statement.setString(index++, siteId);
		if (filter.userId != null) {
			statement.setString(index++, filter.userId);
		}
		Calendar calendar = filter.timeZone == null ? null : Calendar.getInstance(filter.timeZone);
		if (filter.fromAuditStamp != null) {
			statement.setTimestamp(index++, filter.fromAuditStamp, calendar);
		}
		if (filter.toAuditStamp != null) {
			statement.setTimestamp(index, filter.toAuditStamp, calendar);
		}
	}

	static String orderByClause(String sortColumn, boolean sortAscending) {
		String column;
		if ("userId".equals(sortColumn)) {
			column = "user_id";
		}
		else if ("roleName".equals(sortColumn)) {
			column = "role_name";
		}
		else if ("actionText".equals(sortColumn)) {
			column = "action_taken";
		}
		else if ("sourceText".equals(sortColumn)) {
			column = "source";
		}
		else {
			column = "audit_stamp";
		}
		String direction = sortAscending ? "asc" : "desc";
		return column + " " + direction + ", user_id asc";
	}

	static String appendPaging(String sql, int offset, int limit, String vendor) {
		if (limit <= 0) {
			return sql;
		}
		if ("oracle".equalsIgnoreCase(vendor)) {
			// Same rownum/rnum wrapper used in kernel storage SQL (e.g. SingleStorageSqlOracle).
			int lastRow = offset + limit;
			return "select * from ( select page_rows.*, rownum rnum from ( " + sql
					+ " ) page_rows where rownum <= " + lastRow + " ) where rnum >= " + (offset + 1);
		}
		if ("hsqldb".equalsIgnoreCase(vendor)) {
			String trimmed = sql.trim();
			int position = trimmed.toLowerCase().indexOf("select ");
			if (position != 0) {
				return sql;
			}
			return "select limit " + offset + " " + limit + " " + trimmed.substring(position + 7);
		}
		return sql + " limit " + offset + "," + limit;
	}

	private static String appendFilterPredicates(String sql, EventLogFilter filter) {
		StringBuilder filteredSql = new StringBuilder(sql);
		if (filter.userId != null) {
			filteredSql.append(" and user_id=?");
		}
		if (filter.fromAuditStamp != null) {
			filteredSql.append(" and audit_stamp>=?");
		}
		if (filter.toAuditStamp != null) {
			filteredSql.append(" and audit_stamp<?");
		}
		return filteredSql.toString();
	}
}
