/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_INFO;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_SUCCESS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.CHART_COLOR_WARNING;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.GROUP_ALL;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.commons.api.CommonsConstants;
import org.sakaiproject.commons.api.CommonsManager;
import org.sakaiproject.commons.api.QueryBean;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.beans.CommentTransferBean;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsChartDataset;
import org.sakaiproject.sitestats.api.view.SiteStatsChartPoint;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;

@Slf4j
public class SiteStatsCommunicationAnalytics {

	static final String FORUMS_TOOL_ID = "sakai.forums";
	static final String MESSAGES_TOOL_ID = "sakai.messages";

	private static final String COL_USER = "user";
	private static final String COL_TOOL = "tool";
	private static final String COL_AUTHORED = "authored";
	private static final String COL_REPLIED = "replied";
	private static final String COL_UNANSWERED = "unanswered";
	private static final String COL_TOTAL = "total";
	private static final int CONVERSATIONS_PAGE_SIZE = 10;

	@Setter private MessageForumsMessageManager messageForumsMessageManager;
	@Setter private PrivateMessageManager privateMessageManager;
	@Setter private MessageForumsTypeManager messageForumsTypeManager;
	@Setter private ConversationsService conversationsService;
	@Setter private CommonsManager commonsManager;
	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetMetricSupport metricSupport;

	SiteStatsReportView byUserReport(String siteId, SiteStatsReportRequest request, String userId) {
		CommunicationSnapshot snapshot = snapshot(siteId, request, userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_communication");
		if (request.isIncludeTable()) {
			view.setTable(userTable(snapshot, request));
		}
		if (request.isIncludeChart()) {
			view.setChart(totalChart(userRows(snapshot), COL_USER, view.getTitle()));
		}
		return view;
	}

	SiteStatsReportView byToolReport(String siteId, SiteStatsReportRequest request, String userId) {
		CommunicationSnapshot snapshot = snapshot(siteId, request, userId);
		SiteStatsReportView view = reportShell(siteId, request, "overview_title_communication");
		boolean studentView = StringUtils.isNotBlank(userId);
		if (request.isIncludeTable()) {
			view.setTable(toolTable(snapshot, request, studentView));
		}
		if (request.isIncludeChart()) {
			view.setChart(toolChart(toolRows(snapshot, studentView), view.getTitle(), studentView));
		}
		return view;
	}

	WidgetMetricValue authoredValue(String siteId, String userId, SiteStatsReportRequest request) {
		CommunicationSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		return WidgetMetricValue.of(String.valueOf(snapshot.authored));
	}

	WidgetMetricValue repliedValue(String siteId, String userId, SiteStatsReportRequest request) {
		CommunicationSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		return WidgetMetricValue.of(String.valueOf(snapshot.replied));
	}

	WidgetMetricValue unansweredValue(String siteId, String userId, SiteStatsReportRequest request) {
		CommunicationSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		return WidgetMetricValue.of(String.valueOf(snapshot.unanswered));
	}

	WidgetMetricValue mostActiveValue(String siteId, String userId, SiteStatsReportRequest request) {
		CommunicationSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		UserTotals best = mostActive(snapshot);
		if (best == null) {
			return WidgetMetricValue.of("-");
		}
		return WidgetMetricValue.withDetail(displayUser(best.userId),
				formattedMessage("overview_metric_communication_contributions", Integer.valueOf(best.total())));
	}

	SiteStatsChart shareChart(String siteId, String userId, SiteStatsReportRequest request) {
		CommunicationSnapshot snapshot = snapshot(siteId, allTime(request), userId);
		if (snapshot.authored == 0 && snapshot.replied == 0) {
			return null;
		}
		String titleKey = StringUtils.isNotBlank(userId)
				? "overview_title_communication_share_own"
				: "overview_title_communication_share";
		String title = message(titleKey);
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey("share");
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		chart.setItemLabelsVisible(false);
		chart.setCompact(true);
		chart.setHorizontal(true);
		SiteStatsChartDataset dataset = dataset("share", title);
		dataset.getPoints().add(point(message("overview_title_communication_authored"),
				Integer.valueOf(snapshot.authored), CHART_COLOR_INFO));
		dataset.getPoints().add(point(message("overview_title_communication_replied"),
				Integer.valueOf(snapshot.replied), CHART_COLOR_SUCCESS));
		chart.getDatasets().add(dataset);
		return chart;
	}

	private CommunicationSnapshot snapshot(String siteId, SiteStatsReportRequest request, String userId) {
		SiteStatsReportRequest safeRequest = SiteStatsReportRequest.normalized(request);
		CommunicationSnapshot snapshot = new CommunicationSnapshot();
		if (filterCatalog.isIncompleteCustomRange(safeRequest)) {
			return snapshot;
		}
		Set<String> users = roster(siteId, safeRequest, userId);
		if (filterCatalog.includesItemType(safeRequest, FORUMS_TOOL_ID)) {
			collectForums(snapshot, siteId, safeRequest, users);
		}
		if (filterCatalog.includesItemType(safeRequest, MESSAGES_TOOL_ID)) {
			collectMessages(snapshot, siteId, safeRequest, users);
		}
		if (filterCatalog.includesItemType(safeRequest, ConversationsService.TOOL_ID)) {
			collectConversations(snapshot, siteId, safeRequest, users);
		}
		if (filterCatalog.includesItemType(safeRequest, CommonsConstants.TOOL_ID)) {
			collectCommons(snapshot, siteId, safeRequest, users);
		}
		for (String rosterUser : users) {
			snapshot.user(rosterUser);
		}
		seedTools(snapshot, safeRequest);
		snapshot.summarize();
		return snapshot;
	}

	private void collectForums(CommunicationSnapshot snapshot, String siteId, SiteStatsReportRequest request,
			Set<String> users) {
		List<Message> messages;
		try {
			messages = messageForumsMessageManager.getAllMessagesInSite(siteId);
		} catch (RuntimeException e) {
			log.warn("Unable to load forum messages for site {}", siteId, e);
			return;
		}
		if (messages == null || messages.isEmpty()) {
			return;
		}
		for (Message message : messages) {
			if (skipMessage(message)) {
				continue;
			}
			String authorId = forumAuthorId(message);
			if (!includeContribution(authorId, users) || !createdInRange(message.getCreated(), request)) {
				continue;
			}
			boolean reply = isReply(message);
			String threadId = message.getId() == null ? null : FORUMS_TOOL_ID + ":" + message.getId();
			String parentId = replyParentId(message, FORUMS_TOOL_ID);
			snapshot.add(new Contribution(authorId, FORUMS_TOOL_ID, !reply, true, threadId, parentId));
		}
	}

	private void collectMessages(CommunicationSnapshot snapshot, String siteId, SiteStatsReportRequest request,
			Set<String> users) {
		String sentType;
		try {
			sentType = messageForumsTypeManager.getSentPrivateMessageType();
		} catch (RuntimeException e) {
			log.warn("Unable to resolve sent private-message type for site {}", siteId, e);
			return;
		}
		if (StringUtils.isBlank(sentType)) {
			return;
		}
		List messages;
		try {
			messages = privateMessageManager.getMessagesByTypeByContext(sentType, siteId);
		} catch (RuntimeException e) {
			log.warn("Unable to load private messages for site {}", siteId, e);
			return;
		}
		if (messages == null || messages.isEmpty()) {
			return;
		}
		for (Object raw : messages) {
			if (!(raw instanceof Message)) {
				continue;
			}
			Message message = (Message) raw;
			if (skipMessage(message)) {
				continue;
			}
			String authorId = forumAuthorId(message);
			if (!includeContribution(authorId, users) || !createdInRange(message.getCreated(), request)) {
				continue;
			}
			boolean reply = isReply(message);
			snapshot.add(new Contribution(authorId, MESSAGES_TOOL_ID, !reply, false, null, null));
		}
	}

	private void collectConversations(CommunicationSnapshot snapshot, String siteId, SiteStatsReportRequest request,
			Set<String> users) {
		List<TopicTransferBean> topics;
		try {
			topics = conversationsService.getTopicsForSite(siteId);
		} catch (ConversationsPermissionsException | RuntimeException e) {
			log.warn("Unable to load conversation topics for site {}", siteId, e);
			return;
		}
		if (topics == null || topics.isEmpty()) {
			return;
		}
		for (TopicTransferBean topic : topics) {
			if (topic == null || topic.draft) {
				continue;
			}
			String topicId = StringUtils.trimToNull(topic.id);
			String threadId = topicId == null ? null : ConversationsService.TOOL_ID + ":" + topicId;
			if (includeContribution(topic.creator, users) && createdInRange(topic.created, request)) {
				snapshot.add(new Contribution(topic.creator, ConversationsService.TOOL_ID, true, true, threadId, null));
			}
			if (topicId == null) {
				continue;
			}
			List<PostTransferBean> posts = conversationPosts(siteId, topicId);
			for (PostTransferBean post : posts) {
				collectConversationPost(snapshot, request, users, threadId, post);
			}
		}
	}

	private void collectConversationPost(CommunicationSnapshot snapshot, SiteStatsReportRequest request,
			Set<String> users, String threadId, PostTransferBean post) {
		if (post == null || post.draft) {
			return;
		}
		if (includeContribution(post.creator, users) && createdInRange(post.created, request)) {
			snapshot.add(new Contribution(post.creator, ConversationsService.TOOL_ID, false, true, null, threadId));
		}
		if (post.comments != null) {
			for (CommentTransferBean comment : post.comments) {
				if (comment == null) {
					continue;
				}
				if (includeContribution(comment.creator, users) && createdInRange(comment.created, request)) {
					snapshot.add(new Contribution(comment.creator, ConversationsService.TOOL_ID, false, true, null,
							threadId));
				}
			}
		}
		if (post.posts != null) {
			for (PostTransferBean nested : post.posts) {
				collectConversationPost(snapshot, request, users, threadId, nested);
			}
		}
	}

	private List<PostTransferBean> conversationPosts(String siteId, String topicId) {
		List<PostTransferBean> posts = new ArrayList<PostTransferBean>();
		int page = 0;
		while (true) {
			Collection<PostTransferBean> pagePosts;
			try {
				pagePosts = conversationsService.getPostsByTopicId(siteId, topicId, Integer.valueOf(page), null, null);
			} catch (ConversationsPermissionsException | RuntimeException e) {
				log.warn("Unable to load conversation posts for site {} topic {}", siteId, topicId, e);
				return posts;
			}
			if (pagePosts == null || pagePosts.isEmpty()) {
				return posts;
			}
			posts.addAll(pagePosts);
			if (pagePosts.size() < CONVERSATIONS_PAGE_SIZE) {
				return posts;
			}
			page++;
		}
	}

	private void collectCommons(CommunicationSnapshot snapshot, String siteId, SiteStatsReportRequest request,
			Set<String> users) {
		List<Post> posts;
		try {
			QueryBean query = QueryBean.builder()
					.commonsId(siteId)
					.siteId(siteId)
					.embedder(CommonsConstants.SITE)
					.build();
			posts = commonsManager.getPosts(query);
		} catch (Exception e) {
			log.warn("Unable to load commons posts for site {}", siteId, e);
			return;
		}
		if (posts == null || posts.isEmpty()) {
			return;
		}
		for (Post post : posts) {
			if (post == null) {
				continue;
			}
			String threadId = StringUtils.isBlank(post.getId()) ? null : CommonsConstants.TOOL_ID + ":" + post.getId();
			if (includeContribution(post.getCreatorId(), users)
					&& createdInRange(epochMillis(post.getCreatedDate()), request)) {
				snapshot.add(new Contribution(post.getCreatorId(), CommonsConstants.TOOL_ID, true, true, threadId, null));
			}
			List<Comment> comments = post.getComments();
			if (comments == null || comments.isEmpty()) {
				continue;
			}
			for (Comment comment : comments) {
				if (comment == null) {
					continue;
				}
				if (includeContribution(comment.getCreatorId(), users)
						&& createdInRange(epochMillis(comment.getCreatedDate()), request)) {
					snapshot.add(new Contribution(comment.getCreatorId(), CommonsConstants.TOOL_ID, false, true, null,
							threadId));
				}
			}
		}
	}

	private void seedTools(CommunicationSnapshot snapshot, SiteStatsReportRequest request) {
		addToolIfIncluded(snapshot, request, FORUMS_TOOL_ID);
		addToolIfIncluded(snapshot, request, MESSAGES_TOOL_ID);
		addToolIfIncluded(snapshot, request, ConversationsService.TOOL_ID);
		addToolIfIncluded(snapshot, request, CommonsConstants.TOOL_ID);
	}

	private void addToolIfIncluded(CommunicationSnapshot snapshot, SiteStatsReportRequest request, String toolId) {
		if (filterCatalog.includesItemType(request, toolId)) {
			snapshot.tool(toolId);
		}
	}

	private Set<String> roster(String siteId, SiteStatsReportRequest request, String userId) {
		return filterUsers(siteId, siteUsers(siteId), request, userId);
	}

	private Set<String> siteUsers(String siteId) {
		Site site = site(siteId);
		if (site == null || site.getUsers() == null) {
			return Collections.emptySet();
		}
		return new HashSet<String>(site.getUsers());
	}

	private Set<String> filterUsers(String siteId, Set<String> users, SiteStatsReportRequest request, String userId) {
		Set<String> remaining = new HashSet<String>(users);
		if (StringUtils.isNotBlank(userId)) {
			return remaining.contains(userId) ? Collections.singleton(userId) : Collections.<String>emptySet();
		}
		String role = filterCatalog.roleFilter(request);
		if (!ReportManager.WHO_ALL.equals(role) && StringUtils.isNotBlank(role)) {
			Site site = site(siteId);
			Set<String> matchingRole = new HashSet<String>();
			for (String candidate : remaining) {
				if (role.equals(memberRoleFor(site, candidate))) {
					matchingRole.add(candidate);
				}
			}
			remaining = matchingRole;
		}
		String groupId = filterCatalog.groupFilter(request);
		if (!GROUP_ALL.equals(groupId) && StringUtils.isNotBlank(groupId)) {
			remaining.retainAll(groupMembers(siteId, groupId));
		}
		return remaining;
	}

	private Set<String> groupMembers(String siteId, String groupId) {
		try {
			Site site = context.getSiteService().getSite(siteId);
			Group group = site.getGroup(groupId);
			if (group == null || group.getUsers() == null) {
				return Collections.emptySet();
			}
			return new HashSet<String>(group.getUsers());
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
			return Collections.emptySet();
		}
	}

	private Site site(String siteId) {
		try {
			return context.getSiteService().getSite(siteId);
		} catch (IdUnusedException e) {
			log.warn("Site does not exist: {}", siteId);
			return null;
		}
	}

	private String memberRoleFor(Site site, String userId) {
		if (site == null) {
			return null;
		}
		Member member = site.getMember(userId);
		if (member != null && member.getRole() != null) {
			return member.getRole().getId();
		}
		return null;
	}

	private boolean skipMessage(Message message) {
		return message == null || Boolean.TRUE.equals(message.getDeleted())
				|| Boolean.TRUE.equals(message.getDraft());
	}

	private boolean includeContribution(String authorId, Set<String> users) {
		return StringUtils.isNotBlank(authorId) && users.contains(authorId);
	}

	private String forumAuthorId(Message message) {
		String authorId = StringUtils.trimToNull(message.getAuthorId());
		if (authorId != null) {
			return authorId;
		}
		return StringUtils.trimToNull(message.getAuthor());
	}

	private boolean isReply(Message message) {
		try {
			return message.getInReplyTo() != null;
		} catch (RuntimeException e) {
			log.debug("Unable to resolve inReplyTo for message {}", message.getId(), e);
			return false;
		}
	}

	private String replyParentId(Message message, String toolId) {
		try {
			Message parent = message.getInReplyTo();
			if (parent == null || parent.getId() == null) {
				return null;
			}
			return toolId + ":" + parent.getId();
		} catch (RuntimeException e) {
			log.debug("Unable to resolve inReplyTo parent for message {}", message.getId(), e);
			return null;
		}
	}

	private boolean createdInRange(Instant created, SiteStatsReportRequest request) {
		return createdInRange(created == null ? null : Date.from(created), request);
	}

	private boolean createdInRange(Date created, SiteStatsReportRequest request) {
		if (created == null) {
			return true;
		}
		Date from = filterCatalog.periodFrom(request);
		Date to = filterCatalog.periodTo(request);
		if (from != null && created.before(from)) {
			return false;
		}
		return to == null || !created.after(to);
	}

	private Date epochMillis(long createdDate) {
		return createdDate <= 0L ? null : new Date(createdDate);
	}

	private UserTotals mostActive(CommunicationSnapshot snapshot) {
		UserTotals best = null;
		for (UserTotals totals : snapshot.byUser.values()) {
			if (totals.total() == 0) {
				continue;
			}
			if (best == null || totals.total() > best.total()) {
				best = totals;
				continue;
			}
			if (totals.total() == best.total()
					&& displayUser(totals.userId).compareToIgnoreCase(displayUser(best.userId)) < 0) {
				best = totals;
			}
		}
		return best;
	}

	private SiteStatsTable userTable(CommunicationSnapshot snapshot, SiteStatsReportRequest request) {
		return pagedTable(userColumns(), userRows(snapshot), request);
	}

	private SiteStatsTable toolTable(CommunicationSnapshot snapshot, SiteStatsReportRequest request,
			boolean studentView) {
		return pagedTable(toolColumns(studentView), toolRows(snapshot, studentView), request);
	}

	private List<SiteStatsTableColumn> userColumns() {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		columns.add(column(COL_USER, "th_user", "string", "start"));
		columns.add(column(COL_AUTHORED, "th_authored", "number", "end"));
		columns.add(column(COL_REPLIED, "th_replied", "number", "end"));
		columns.add(column(COL_TOTAL, "th_total", "number", "end"));
		return columns;
	}

	private List<SiteStatsTableColumn> toolColumns(boolean studentView) {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		columns.add(column(COL_TOOL, "th_tool", "string", "start"));
		columns.add(column(COL_AUTHORED, "th_authored", "number", "end"));
		columns.add(column(COL_REPLIED, "th_replied", "number", "end"));
		if (!studentView) {
			columns.add(column(COL_UNANSWERED, "th_unanswered", "number", "end"));
		}
		return columns;
	}

	private List<SiteStatsTableRow> userRows(CommunicationSnapshot snapshot) {
		List<UserTotals> rows = new ArrayList<UserTotals>(snapshot.byUser.values());
		Collections.sort(rows, Comparator.comparing((UserTotals row) -> displayUser(row.userId),
				String.CASE_INSENSITIVE_ORDER));
		List<SiteStatsTableRow> tableRows = new ArrayList<SiteStatsTableRow>();
		for (UserTotals row : rows) {
			SiteStatsTableRow tableRow = new SiteStatsTableRow();
			tableRow.getCells().put(COL_USER, cell(displayUser(row.userId), row.userId));
			tableRow.getCells().put(COL_AUTHORED, numberCell(row.authored));
			tableRow.getCells().put(COL_REPLIED, numberCell(row.replied));
			tableRow.getCells().put(COL_TOTAL, numberCell(row.total()));
			tableRows.add(tableRow);
		}
		return tableRows;
	}

	private List<SiteStatsTableRow> toolRows(CommunicationSnapshot snapshot, boolean studentView) {
		List<ToolTotals> rows = new ArrayList<ToolTotals>(snapshot.byTool.values());
		Collections.sort(rows, Comparator.comparing((ToolTotals row) -> toolLabel(row.toolId),
				String.CASE_INSENSITIVE_ORDER));
		List<SiteStatsTableRow> tableRows = new ArrayList<SiteStatsTableRow>();
		for (ToolTotals row : rows) {
			SiteStatsTableRow tableRow = new SiteStatsTableRow();
			tableRow.getCells().put(COL_TOOL, cell(toolLabel(row.toolId), row.toolId));
			tableRow.getCells().put(COL_AUTHORED, numberCell(row.authored));
			tableRow.getCells().put(COL_REPLIED, numberCell(row.replied));
			if (!studentView) {
				tableRow.getCells().put(COL_UNANSWERED, numberCell(row.unanswered));
			}
			tableRows.add(tableRow);
		}
		return tableRows;
	}

	private SiteStatsTable pagedTable(List<SiteStatsTableColumn> columns, List<SiteStatsTableRow> rows,
			SiteStatsReportRequest request) {
		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(message("overview_title_communication"));
		table.setColumns(columns);
		table.setPage(request.getPage());
		table.setPageSize(request.getPageSize());
		table.setTotalRows(rows.size());
		table.setRows(page(rows, request));
		return table;
	}

	private List<SiteStatsTableRow> page(List<SiteStatsTableRow> rows, SiteStatsReportRequest request) {
		int from = (request.getPage() - 1) * request.getPageSize();
		if (from >= rows.size()) {
			return Collections.emptyList();
		}
		int to = Math.min(from + request.getPageSize(), rows.size());
		return new ArrayList<SiteStatsTableRow>(rows.subList(from, to));
	}

	private SiteStatsChart totalChart(List<SiteStatsTableRow> rows, String labelKey, String title) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey(labelKey);
		chart.setYKey(COL_TOTAL);
		chart.setEmptyMessage(message("no_data"));
		if (rows.isEmpty()) {
			return chart;
		}
		SiteStatsChartDataset dataset = dataset(COL_TOTAL, message("th_total"));
		for (SiteStatsTableRow row : rows) {
			dataset.getPoints().add(point(display(row, labelKey), number(row, COL_TOTAL)));
		}
		chart.getDatasets().add(dataset);
		return chart;
	}

	private SiteStatsChart toolChart(List<SiteStatsTableRow> rows, String title, boolean studentView) {
		SiteStatsChart chart = new SiteStatsChart();
		chart.setTitle(title);
		chart.setType(StatsManager.CHARTTYPE_BAR);
		chart.setXKey(COL_TOOL);
		chart.setYKey("count");
		chart.setEmptyMessage(message("no_data"));
		if (rows.isEmpty()) {
			return chart;
		}
		SiteStatsChartDataset authored = dataset(COL_AUTHORED, message("th_authored"), CHART_COLOR_INFO);
		SiteStatsChartDataset replied = dataset(COL_REPLIED, message("th_replied"), CHART_COLOR_SUCCESS);
		SiteStatsChartDataset unanswered = studentView ? null
				: dataset(COL_UNANSWERED, message("th_unanswered"));
		for (SiteStatsTableRow row : rows) {
			String label = display(row, COL_TOOL);
			authored.getPoints().add(point(label, number(row, COL_AUTHORED)));
			replied.getPoints().add(point(label, number(row, COL_REPLIED)));
			if (unanswered != null) {
				unanswered.getPoints().add(point(label, number(row, COL_UNANSWERED)));
			}
		}
		chart.getDatasets().add(authored);
		chart.getDatasets().add(replied);
		if (unanswered != null) {
			chart.getDatasets().add(unanswered);
		}
		return chart;
	}

	private SiteStatsChartDataset dataset(String key, String label) {
		return dataset(key, label, null);
	}

	private SiteStatsChartDataset dataset(String key, String label, String color) {
		SiteStatsChartDataset dataset = new SiteStatsChartDataset();
		dataset.setKey(key);
		dataset.setLabel(label);
		dataset.setColor(color);
		return dataset;
	}

	private SiteStatsChartPoint point(String label, Number y) {
		return point(label, y, null);
	}

	private SiteStatsChartPoint point(String label, Number y, String color) {
		SiteStatsChartPoint point = new SiteStatsChartPoint();
		point.setX(label);
		point.setLabel(label);
		point.setY(y);
		point.setColor(color);
		return point;
	}

	private SiteStatsTableColumn column(String key, String labelKey, String type, String align) {
		SiteStatsTableColumn column = new SiteStatsTableColumn();
		column.setKey(key);
		column.setLabel(message(labelKey));
		column.setType(type);
		column.setAlign(align);
		column.setSortable(true);
		column.setSortKey(key);
		return column;
	}

	private SiteStatsTableCell cell(String display, Object raw) {
		SiteStatsTableCell cell = new SiteStatsTableCell();
		cell.setDisplay(display);
		cell.setRaw(raw);
		cell.setSort(raw);
		return cell;
	}

	private SiteStatsTableCell numberCell(int value) {
		return cell(String.valueOf(value), Integer.valueOf(value));
	}

	private String displayUser(String userId) {
		return StringUtils.defaultIfBlank(metricSupport.userTooltip(userId), userId);
	}

	private String toolLabel(String toolId) {
		return context.toolName(toolId);
	}

	private String display(SiteStatsTableRow row, String key) {
		SiteStatsTableCell cell = row.getCells().get(key);
		return cell == null ? "" : StringUtils.defaultString(cell.getDisplay());
	}

	private Number number(SiteStatsTableRow row, String key) {
		SiteStatsTableCell cell = row.getCells().get(key);
		if (cell == null || !(cell.getRaw() instanceof Number)) {
			return Integer.valueOf(0);
		}
		return (Number) cell.getRaw();
	}

	private SiteStatsReportView reportShell(String siteId, SiteStatsReportRequest request, String titleKey) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setSiteId(siteId);
		view.setTitle(message(titleKey));
		view.setPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
		return view;
	}

	private SiteStatsReportRequest allTime(SiteStatsReportRequest request) {
		SiteStatsReportRequest allTime = SiteStatsReportRequest.normalized(request);
		allTime.setDate(ReportManager.WHEN_ALL);
		allTime.setWhenFrom(null);
		allTime.setWhenTo(null);
		return allTime;
	}

	private String message(String key) {
		return context.message(key);
	}

	private String formattedMessage(String key, Object... args) {
		return context.formattedMessage(key, args);
	}

	private static class Contribution {
		private final String userId;
		private final String toolId;
		private final boolean authored;
		private final boolean publicThread;
		private final String threadId;
		private final String parentId;

		Contribution(String userId, String toolId, boolean authored, boolean publicThread, String threadId,
				String parentId) {
			this.userId = userId;
			this.toolId = toolId;
			this.authored = authored;
			this.publicThread = publicThread;
			this.threadId = threadId;
			this.parentId = parentId;
		}
	}

	private static class UserTotals {
		private final String userId;
		private int authored;
		private int replied;

		UserTotals(String userId) {
			this.userId = userId;
		}

		void add(Contribution contribution) {
			if (contribution.authored) {
				authored++;
			} else {
				replied++;
			}
		}

		int total() {
			return authored + replied;
		}
	}

	private static class ToolTotals {
		private final String toolId;
		private int authored;
		private int replied;
		private int unanswered;

		ToolTotals(String toolId) {
			this.toolId = toolId;
		}

		void add(Contribution contribution) {
			if (contribution.authored) {
				authored++;
			} else {
				replied++;
			}
		}
	}

	private static class CommunicationSnapshot {
		private final List<Contribution> contributions = new ArrayList<Contribution>();
		private final Map<String, UserTotals> byUser = new LinkedHashMap<String, UserTotals>();
		private final Map<String, ToolTotals> byTool = new LinkedHashMap<String, ToolTotals>();
		private int authored;
		private int replied;
		private int unanswered;

		void add(Contribution contribution) {
			contributions.add(contribution);
		}

		UserTotals user(String userId) {
			UserTotals totals = byUser.get(userId);
			if (totals == null) {
				totals = new UserTotals(userId);
				byUser.put(userId, totals);
			}
			return totals;
		}

		ToolTotals tool(String toolId) {
			ToolTotals totals = byTool.get(toolId);
			if (totals == null) {
				totals = new ToolTotals(toolId);
				byTool.put(toolId, totals);
			}
			return totals;
		}

		void summarize() {
			Set<String> answeredThreads = new HashSet<String>();
			for (Contribution contribution : contributions) {
				if (StringUtils.isNotBlank(contribution.parentId)) {
					answeredThreads.add(contribution.parentId);
				}
			}
			Set<String> unansweredThreads = new HashSet<String>();
			for (Contribution contribution : contributions) {
				user(contribution.userId).add(contribution);
				tool(contribution.toolId).add(contribution);
				if (contribution.authored) {
					authored++;
				} else {
					replied++;
				}
				if (contribution.authored && contribution.publicThread && StringUtils.isNotBlank(contribution.threadId)
						&& !answeredThreads.contains(contribution.threadId)) {
					unansweredThreads.add(contribution.threadId);
					tool(contribution.toolId).unanswered++;
				}
			}
			unanswered = unansweredThreads.size();
		}
	}
}
