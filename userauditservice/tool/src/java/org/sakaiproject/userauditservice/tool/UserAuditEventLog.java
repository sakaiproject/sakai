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
import org.sakaiproject.jsf2.util.LocaleUtil;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.userauditservice.api.model.UserAuditLog;
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
	private transient UserAuditService userAuditService = ComponentManager.get(UserAuditService.class);
	private transient UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	private transient UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
	private transient ToolManager toolManager = ComponentManager.get(ToolManager.class);
	private transient SessionManager sessionManager = ComponentManager.get(SessionManager.class);

	private ResourceLoader rb = new ResourceLoader("UserAuditMessages");
	private final String STATE_SITE_ID = "site.instance.id";

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
		try {
			totalItems = (int) Math.min(userAuditService.countUserAuditLogs(
					EventLogQueryBuilder.build(siteId, filter, getSortColumn(), sortAscending, 0, 0)),
					Integer.MAX_VALUE);
			if (totalItems <= 0) {
				totalItems = 0;
				return;
			}

			normalizeFirstItem();
			int fetchSize = getRowsNumber();
			if (fetchSize <= 0) {
				return;
			}

			List<UserAuditLog> rows = userAuditService.getUserAuditLogs(
					EventLogQueryBuilder.build(siteId, filter, getSortColumn(), sortAscending, firstItem, fetchSize));
			Set<String> userIds = new HashSet<String>();
			for (UserAuditLog row : rows) {
				if (row.getUserId() != null) {
					userIds.add(row.getUserId());
				}
				if (row.getActionUserId() != null) {
					userIds.add(row.getActionUserId());
				}
			}
			Map<String, String> eidsById = new HashMap<String, String>();
			if (!userIds.isEmpty()) {
				for (User user : userDirectoryService.getUsers(userIds)) {
					eidsById.put(user.getId(), user.getEid());
				}
			}
			for (UserAuditLog row : rows) {
				String userEid = eidsById.get(row.getUserId());
				String actionUserEid = eidsById.get(row.getActionUserId());
				eventLog.add(new EventLog(userEid != null ? userEid : row.getUserId(), row.getRoleName(),
						row.getActionTaken(), row.getAuditStamp(), row.getSource(),
						actionUserEid != null ? actionUserEid : row.getActionUserId()));
			}
		}
		catch (RuntimeException e) {
			log.warn("ERROR loading user audit logs for site {}", siteId, e);
			totalItems = 0;
			firstItem = 0;
			addErrorMessage("event_log_load_error");
		}
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
