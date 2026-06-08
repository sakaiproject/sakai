/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
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

package org.sakaiproject.userauditservice.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jsf2.util.LocaleUtil;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class UserAuditEventLog {
	private static final int DEFAULT_PAGE_SIZE = 200;

	protected List<EventLog> eventLog = new ArrayList<EventLog>();
	@Setter protected String sortColumn;
	@Getter @Setter protected boolean sortAscending;
	@Getter @Setter private int totalItems = -1;
	@Getter @Setter private int firstItem = 0;
	@Getter @Setter private int pageSize = DEFAULT_PAGE_SIZE;
	@Getter @Setter private String userIdFilter;
	@Getter @Setter private String fromDateFilter;
	@Getter @Setter private String toDateFilter;
	private Optional<EventLogFilter> activeFilter = Optional.of(EventLogFilter.empty());
	private transient SqlService sqlService = ComponentManager.get(SqlService.class);
	private transient UserAuditService userAuditService = ComponentManager.get(UserAuditService.class);
	private transient UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	private transient UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
	private transient ToolManager toolManager = ComponentManager.get(ToolManager.class);
	private transient SessionManager sessionManager = ComponentManager.get(SessionManager.class);

	private ResourceLoader rb = new ResourceLoader("UserAuditMessages");
	private final String STATE_SITE_ID = "site.instance.id";

	private static final class EventLogRow {
		private final String userId;
		private final String roleName;
		private final String actionTaken;
		private final Date auditStamp;
		private final String source;
		private final String actionUserId;

		private EventLogRow(String userId, String roleName, String actionTaken, Date auditStamp, String source, String actionUserId) {
			this.userId = userId;
			this.roleName = roleName;
			this.actionTaken = actionTaken;
			this.auditStamp = auditStamp;
			this.source = source;
			this.actionUserId = actionUserId;
		}
	}

    @Getter @Setter
	public class EventLog {
		protected String actionTaken;
		protected String actionText;
		protected String actionUserEid;
		protected Date auditStamp;
		protected String roleName;
		protected String source;
		protected String sourceText;
		protected String userEid;

		public EventLog(String userEid, String roleName, String actionTaken, Date auditStamp, String source, String actionUserEid) {
			this.userEid = userEid;
			this.roleName = roleName;
			this.actionTaken = actionTaken;
			this.auditStamp = auditStamp;
			this.source = source;
			this.actionUserEid = actionUserEid;
		}

		public String getActionTaken() {
			return actionTaken;
		}

		public String getActionText() {
			if (userAuditService.USER_AUDIT_ACTION_ADD.equals(actionTaken))
			{
				actionText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),"UserAuditMessages", "event_log_add");
			}
			else if (userAuditService.USER_AUDIT_ACTION_REMOVE.equals(actionTaken))
			{
				actionText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),"UserAuditMessages", "event_log_remove");
			}
			else if (userAuditService.USER_AUDIT_ACTION_UPDATE.equals(actionTaken))
			{
				actionText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),"UserAuditMessages", "event_log_update");
			}
			return actionText;
		}

		public String getAuditStamp() {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, rb.getLocale());
			df.setTimeZone(userTimeService.getLocalTimeZone());
			return df.format(auditStamp);
		}

		public String getSourceText() {
			sourceText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(), "UserAuditMessages", "event_log_not_available");
			for (UserAuditRegistration uar : userAuditService.getRegisteredItems()) {
				if (uar.getDatabaseSourceKey().equals(source)) {
					sourceText = uar.getSourceText(actionUserEid);
					break;
				}
			}
			return sourceText;
		}
	}

	public List<EventLog> getEventLog() {
		return eventLog;
	}

	private void loadEvents() {
		eventLog = new ArrayList<EventLog>();
		String siteId = resolveSiteId();
		Optional<EventLogFilter> activeFilter = getActiveFilter();
		if (activeFilter.isEmpty()) {
			totalItems = 0;
			firstItem = 0;
			return;
		}
		EventLogFilter filter = activeFilter.get();
		totalItems = countEvents(siteId, filter);
		if (totalItems <= 0) {
			totalItems = 0;
			return;
		}

		normalizeFirstItem();
		int fetchSize = getRowsNumber();
		if (fetchSize <= 0) {
			return;
		}

		String sql = EventLogSqlBuilder.buildPagedEventsSql(filter, getSortColumn(), sortAscending, firstItem, fetchSize, sqlService.getVendor());
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			conn = sqlService.borrowConnection();
			statement = conn.prepareStatement(sql);
			EventLogSqlBuilder.bindParameters(statement, siteId, filter);
			result = statement.executeQuery();
			List<EventLogRow> rows = new ArrayList<EventLogRow>();
			Set<String> userIds = new HashSet<String>();
			while (result.next()) {
				String userId = result.getString("user_id");
				String actionUserId = result.getString("action_user_id");
				rows.add(new EventLogRow(userId, result.getString("role_name"),
						result.getString("action_taken"), result.getTimestamp("audit_stamp"),
						result.getString("source"), actionUserId));
				if (userId != null) {
					userIds.add(userId);
				}
				if (actionUserId != null) {
					userIds.add(actionUserId);
				}
			}
			Map<String, String> eidsById = new HashMap<String, String>();
			if (!userIds.isEmpty()) {
				for (User user : userDirectoryService.getUsers(userIds)) {
					eidsById.put(user.getId(), user.getEid());
				}
			}
			for (EventLogRow row : rows) {
				String userEid = eidsById.get(row.userId);
				String actionUserEid = eidsById.get(row.actionUserId);
				eventLog.add(new EventLog(userEid != null ? userEid : row.userId, row.roleName,
						row.actionTaken, row.auditStamp, row.source,
						actionUserEid != null ? actionUserEid : row.actionUserId));
			}
		}
		catch (SQLException e) {
			log.warn("ERROR getting the user audit logs!", e);
		}
		finally {
			closeQuietly(result, statement, conn);
		}
	}

	private int countEvents(String siteId, EventLogFilter filter) {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			conn = sqlService.borrowConnection();
			statement = conn.prepareStatement(EventLogSqlBuilder.buildCountEventsSql(filter));
			EventLogSqlBuilder.bindParameters(statement, siteId, filter);
			result = statement.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			}
		}
		catch (SQLException e) {
			log.warn("ERROR counting user audit logs!", e);
		}
		finally {
			closeQuietly(result, statement, conn);
		}
		return 0;
	}

	private Optional<EventLogFilter> resolveEventFilter() {
		EventLogFilterResolver.Result result = EventLogFilterResolver.resolve(userIdFilter, fromDateFilter, toDateFilter,
				userDirectoryService, userTimeService);
		userIdFilter = result.userIdFilter;
		fromDateFilter = result.fromDateFilter;
		toDateFilter = result.toDateFilter;
		result.messageKey.ifPresent(this::addErrorMessage);
		return result.filter;
	}

	private void addErrorMessage(String messageKey) {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, rb.getString(messageKey), null));
		}
	}

	private Optional<EventLogFilter> getActiveFilter() {
		if (activeFilter == null) {
			activeFilter = Optional.of(EventLogFilter.empty());
		}
		return activeFilter;
	}

	private String resolveSiteId() {
		try {
			return sessionManager.getCurrentToolSession().getAttribute(STATE_SITE_ID).toString();
		}
		catch (Exception ex) {
			return toolManager.getCurrentPlacement().getContext();
		}
	}

	private void normalizeFirstItem() {
		if (pageSize <= 0) {
			firstItem = 0;
			return;
		}
		if (firstItem < 0) {
			firstItem = 0;
		}
		if (firstItem >= totalItems) {
			int lastPage = (totalItems - 1) / pageSize;
			firstItem = lastPage * pageSize;
		}
	}

	private void closeQuietly(ResultSet result, PreparedStatement statement, Connection conn) {
		try {
			if (result != null) {
				result.close();
			}
		}
		catch (SQLException e) {
			log.warn("Error closing result set in user audit event log", e);
		}
		try {
			if (statement != null) {
				statement.close();
			}
		}
		catch (SQLException e) {
			log.warn("Error closing statement in user audit event log", e);
		}
		if (conn != null) {
			sqlService.returnConnection(conn);
		}
	}

	public String getInitValues() {
		loadEvents();
		return "";
	}

	public String processActionSearch() {
		activeFilter = resolveEventFilter();
		firstItem = 0;
		return "";
	}

	public String processActionClearSearch() {
		userIdFilter = null;
		fromDateFilter = null;
		toDateFilter = null;
		activeFilter = Optional.of(EventLogFilter.empty());
		firstItem = 0;
		return "";
	}

	public String getPageTitle() {
	    return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				"UserAuditMessages", "title_event_log");
	}

	public String getSortColumn() {
		if (this.sortColumn == null) {
			this.sortColumn = "auditStamp";
		}
		return this.sortColumn;
	}

	public boolean isExportablePage() {
		return false;
	}

	public int getRowsNumber() {
		if (totalItems <= 0) {
			return 0;
		}
		if (pageSize <= 0) {
			return totalItems;
		}
		int remaining = totalItems - firstItem;
		if (remaining <= 0) {
			return 0;
		}
		return Math.min(pageSize, remaining);
	}

}
