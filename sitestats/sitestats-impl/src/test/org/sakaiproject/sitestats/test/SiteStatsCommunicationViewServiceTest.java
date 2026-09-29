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

package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_GROUP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM_TYPE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_COMMUNICATION_AUTHORED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_COMMUNICATION_MOST_ACTIVE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_COMMUNICATION_REPLIED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_COMMUNICATION_UNANSWERED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_COMMUNICATION_AUTHORED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_COMMUNICATION_REPLIED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_COMMUNICATION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_COMMUNICATION;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.site;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.tool;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.commons.api.CommonsManager;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsFilter;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.sitestats.api.view.SiteStatsTableRow;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetricSnapshot;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.test.data.FakeData;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
public class SiteStatsCommunicationViewServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final String SITE_ID = FakeData.SITE_A_ID;
	private static final String SITE_REF = FakeData.SITE_A_REF;
	private static final String USER_A_ID = FakeData.USER_A_ID;
	private static final String USER_B_ID = FakeData.USER_B_ID;

	@Autowired private DB db;
	@Autowired private SiteStatsViewService service;
	@Autowired private MessageForumsMessageManager messageForumsMessageManager;
	@Autowired private PrivateMessageManager privateMessageManager;
	@Autowired private MessageForumsTypeManager messageForumsTypeManager;
	@Autowired private ConversationsService conversationsService;
	@Autowired private CommonsManager commonsManager;
	@Autowired private SecurityService securityService;
	@Autowired private SessionManager sessionManager;
	@Autowired private UserDirectoryService userDirectoryService;
	@Autowired private SiteService siteService;
	@Autowired private StatsManager statsManager;

	@Before
	public void setUp() throws Exception {
		db.deleteAll();
		reset(securityService, siteService, sessionManager, userDirectoryService, messageForumsMessageManager,
				privateMessageManager, messageForumsTypeManager, conversationsService, commonsManager);

		Session session = mock(Session.class);
		when(session.getUserId()).thenReturn(USER_A_ID);
		when(sessionManager.getCurrentSession()).thenReturn(session);

		User userA = mock(User.class);
		when(userA.getDisplayId()).thenReturn(USER_A_ID);
		when(userA.getDisplayName()).thenReturn("User A");
		when(userA.getSortName()).thenReturn("User A");
		when(userDirectoryService.getUser(USER_A_ID)).thenReturn(userA);

		User userB = mock(User.class);
		when(userB.getDisplayId()).thenReturn(USER_B_ID);
		when(userB.getDisplayName()).thenReturn("User B");
		when(userB.getSortName()).thenReturn("User B");
		when(userDirectoryService.getUser(USER_B_ID)).thenReturn(userB);

		Site site = site(SITE_ID, "Site A", "Instructor");
		when(site.getUsers()).thenReturn(new HashSet<String>(Arrays.asList(USER_A_ID, USER_B_ID)));
		when(siteService.siteReference(SITE_ID)).thenReturn(SITE_REF);
		when(siteService.getSite(SITE_ID)).thenReturn(site);
		when(siteService.isUserSite(SITE_ID)).thenReturn(false);
		when(siteService.isSpecialSite(SITE_ID)).thenReturn(false);

		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_VIEW, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(true);
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_OWN, SITE_REF)).thenReturn(true);

		when(messageForumsMessageManager.getAllMessagesInSite(anyString())).thenReturn(Collections.emptyList());
		when(messageForumsTypeManager.getSentPrivateMessageType()).thenReturn("sent-type");
		when(messageForumsTypeManager.getReceivedPrivateMessageType()).thenReturn("received-type");
		when(privateMessageManager.getMessagesByTypeByContext(anyString(), anyString()))
				.thenReturn(Collections.emptyList());
		when(conversationsService.getTopicsForSite(anyString())).thenReturn(Collections.emptyList());
		when(conversationsService.getPostsByTopicId(anyString(), anyString(), any(), any(), any()))
				.thenReturn(Collections.emptyList());
		when(commonsManager.getPosts(any())).thenReturn(Collections.emptyList());

		PrefsData prefsData = new PrefsData();
		prefsData.setShowOwnStatisticsToStudents(true);
		prefsData.setListToolEventsOnlyAvailableInSite(false);
		prefsData.setToolEventsDef(Arrays.asList(tool(FakeData.TOOL_CHAT, FakeData.EVENT_CHATNEW)));
		assertTrue(statsManager.setPreferences(SITE_ID, prefsData));
	}

	@Test
	public void instructorOverviewIncludesCommunicationWidgetNotStudentCommunication() {
		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertTrue(hasWidget(overview, WIDGET_COMMUNICATION));
		assertFalse(hasWidget(overview, WIDGET_STUDENT_COMMUNICATION));
		assertTrue(widget(overview, WIDGET_COMMUNICATION).getHighlights().isEmpty());
	}

	@Test
	public void studentOverviewIncludesStudentCommunicationNotInstructor() {
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		SiteStatsOverview overview = service.getOverview(SITE_ID);

		assertTrue(hasWidget(overview, WIDGET_STUDENT_COMMUNICATION));
		assertFalse(hasWidget(overview, WIDGET_COMMUNICATION));
	}

	@Test
	public void authoredAndRepliedMetricsCountForumMessages() {
		Message thread = forumMessage(1L, USER_A_ID, null, Date.from(Instant.now()));
		Message reply = forumMessage(2L, USER_B_ID, thread, Date.from(Instant.now()));
		when(messageForumsMessageManager.getAllMessagesInSite(SITE_ID)).thenReturn(Arrays.asList(thread, reply));

		assertEquals("1", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_AUTHORED).getPrimary());
		assertEquals("1", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_REPLIED).getPrimary());
	}

	@Test
	public void draftForumMessagesAreExcluded() {
		Message draft = forumMessage(1L, USER_A_ID, null, Date.from(Instant.now()));
		when(draft.getDraft()).thenReturn(Boolean.TRUE);
		when(messageForumsMessageManager.getAllMessagesInSite(SITE_ID)).thenReturn(Collections.singletonList(draft));

		assertEquals("0", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_AUTHORED).getPrimary());
		assertEquals("0", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_UNANSWERED).getPrimary());
	}

	@Test
	public void unansweredMetricIgnoresPrivateMessagesAndCountsForumThread() {
		Message thread = forumMessage(1L, USER_A_ID, null, Date.from(Instant.now()));
		when(messageForumsMessageManager.getAllMessagesInSite(SITE_ID)).thenReturn(Collections.singletonList(thread));

		Message privateMessage = forumMessage(9L, USER_A_ID, null, Date.from(Instant.now()));
		when(privateMessageManager.getMessagesByTypeByContext(eq("sent-type"), eq(SITE_ID)))
				.thenReturn(Collections.singletonList(privateMessage));

		assertEquals("1", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_UNANSWERED).getPrimary());
		assertEquals("2", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_AUTHORED).getPrimary());
	}

	@Test
	public void toolFilterLimitsToConversations() throws Exception {
		Message forumThread = forumMessage(1L, USER_B_ID, null, Date.from(Instant.now()));
		when(messageForumsMessageManager.getAllMessagesInSite(SITE_ID))
				.thenReturn(Collections.singletonList(forumThread));

		TopicTransferBean topic = conversationTopic("topic-1", USER_A_ID, Instant.now());
		when(conversationsService.getTopicsForSite(SITE_ID)).thenReturn(Collections.singletonList(topic));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		request.setItemType(ConversationsService.TOOL_ID);

		assertEquals("1", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_AUTHORED, request).getPrimary());
		assertEquals("0", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_REPLIED, request).getPrimary());
	}

	@Test
	public void byUserTabHasAuthoredRepliedTotalColumns() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_COMMUNICATION, TAB_BY_USER, request);

		assertTrue(hasColumn(view, "user"));
		assertTrue(hasColumn(view, "authored"));
		assertTrue(hasColumn(view, "replied"));
		assertTrue(hasColumn(view, "total"));
		assertFalse(hasColumn(view, "unanswered"));

		SiteStatsWidgetTab tab = service.getWidgetTab(SITE_ID, WIDGET_COMMUNICATION, TAB_BY_USER);
		assertNotNull(filterById(tab, FILTER_DATE));
		assertNotNull(filterById(tab, FILTER_ROLE));
		assertNotNull(filterById(tab, FILTER_GROUP));
		assertNull(filterById(tab, FILTER_ITEM));
		assertNull(filterById(tab, FILTER_ITEM_TYPE));
	}

	@Test
	public void byToolTabHasToolAuthoredRepliedUnanswered() {
		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_COMMUNICATION, TAB_BY_TOOL, request);

		assertTrue(hasColumn(view, "tool"));
		assertTrue(hasColumn(view, "authored"));
		assertTrue(hasColumn(view, "replied"));
		assertTrue(hasColumn(view, "unanswered"));
	}

	@Test
	public void studentMetricsOnlyOwnContributions() {
		Message thread = forumMessage(1L, USER_A_ID, null, Date.from(Instant.now()));
		Message reply = forumMessage(2L, USER_B_ID, thread, Date.from(Instant.now()));
		when(messageForumsMessageManager.getAllMessagesInSite(SITE_ID)).thenReturn(Arrays.asList(thread, reply));
		when(securityService.unlock(StatsAuthz.PERMISSION_SITESTATS_ALL, SITE_REF)).thenReturn(false);

		assertEquals("1", snapshot(WIDGET_STUDENT_COMMUNICATION, METRIC_STUDENT_COMMUNICATION_AUTHORED).getPrimary());
		assertEquals("0", snapshot(WIDGET_STUDENT_COMMUNICATION, METRIC_STUDENT_COMMUNICATION_REPLIED).getPrimary());

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_ALL);
		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_STUDENT_COMMUNICATION, TAB_BY_TOOL, request);
		assertTrue(hasColumn(view, "tool"));
		assertTrue(hasColumn(view, "authored"));
		assertTrue(hasColumn(view, "replied"));
		assertFalse(hasColumn(view, "unanswered"));
		assertFalse(hasColumn(view, "user"));
	}

	@Test
	public void metricsCountSinceSiteCreationWhileTabsHonorDateFilter() {
		Message oldThread = forumMessage(1L, USER_A_ID, null, Date.from(Instant.parse("2026-06-15T12:00:00Z")));
		when(messageForumsMessageManager.getAllMessagesInSite(SITE_ID)).thenReturn(Collections.singletonList(oldThread));

		SiteStatsReportRequest request = new SiteStatsReportRequest();
		request.setDate(ReportManager.WHEN_LAST7DAYS);

		assertEquals("1", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_AUTHORED, request).getPrimary());
		assertEquals("1", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_UNANSWERED, request).getPrimary());

		SiteStatsReportView view = service.getWidgetReport(SITE_ID, WIDGET_COMMUNICATION, TAB_BY_USER, request);
		int authored = 0;
		for (SiteStatsTableRow row : view.getTable().getRows()) {
			authored += ((Number) row.getCells().get("authored").getRaw()).intValue();
		}
		assertEquals(0, authored);
	}

	@Test
	public void mostActiveAuthorPicksHighestTotal() {
		Message thread = forumMessage(1L, USER_A_ID, null, Date.from(Instant.now()));
		Message replyA = forumMessage(2L, USER_A_ID, thread, Date.from(Instant.now()));
		Message replyB = forumMessage(3L, USER_B_ID, thread, Date.from(Instant.now()));
		when(messageForumsMessageManager.getAllMessagesInSite(SITE_ID))
				.thenReturn(Arrays.asList(thread, replyA, replyB));

		SiteStatsWidgetMetricSnapshot mostActive = snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_MOST_ACTIVE);
		assertEquals("User A", mostActive.getPrimary());
		assertEquals("2 contributions", mostActive.getDetail());
	}

	@Test
	public void conversationsAndCommonsContributeToAuthoredReplied() throws Exception {
		TopicTransferBean topic = conversationTopic("topic-1", USER_A_ID, Instant.now());
		topic.numberOfPosts = 1;
		PostTransferBean post = new PostTransferBean();
		post.id = "post-1";
		post.creator = USER_B_ID;
		post.created = Instant.now();
		post.draft = false;
		when(conversationsService.getTopicsForSite(SITE_ID)).thenReturn(Collections.singletonList(topic));
		when(conversationsService.getPostsByTopicId(eq(SITE_ID), eq("topic-1"), any(), any(), any()))
				.thenReturn(Collections.singletonList(post));

		Post commonsPost = new Post();
		commonsPost.setId("commons-1");
		commonsPost.setCreatorId(USER_A_ID);
		commonsPost.setCreatedDate(Instant.now().toEpochMilli());
		Comment comment = new Comment();
		comment.setCreatorId(USER_B_ID);
		comment.setCreatedDate(Instant.now().toEpochMilli());
		commonsPost.addComment(comment);
		when(commonsManager.getPosts(any())).thenReturn(Collections.singletonList(commonsPost));

		assertEquals("2", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_AUTHORED).getPrimary());
		assertEquals("2", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_REPLIED).getPrimary());
		assertEquals("0", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_UNANSWERED).getPrimary());
	}

	@Test
	public void toolServiceExceptionDoesNotFailWidget() {
		when(messageForumsMessageManager.getAllMessagesInSite(anyString()))
				.thenThrow(new RuntimeException("forums unavailable"));

		assertEquals("0", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_AUTHORED).getPrimary());
		assertEquals("0", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_REPLIED).getPrimary());
		assertEquals("-", snapshot(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_MOST_ACTIVE).getPrimary());
	}

	private Message forumMessage(Long id, String authorId, Message inReplyTo, Date created) {
		Message message = mock(Message.class);
		when(message.getId()).thenReturn(id);
		when(message.getAuthorId()).thenReturn(authorId);
		when(message.getInReplyTo()).thenReturn(inReplyTo);
		when(message.getDraft()).thenReturn(Boolean.FALSE);
		when(message.getDeleted()).thenReturn(Boolean.FALSE);
		when(message.getCreated()).thenReturn(created);
		return message;
	}

	private TopicTransferBean conversationTopic(String id, String creator, Instant created) {
		TopicTransferBean topic = new TopicTransferBean();
		topic.id = id;
		topic.creator = creator;
		topic.created = created;
		topic.draft = false;
		topic.numberOfPosts = 0;
		topic.siteId = SITE_ID;
		return topic;
	}

	private SiteStatsFilter filterById(SiteStatsWidgetTab tab, String id) {
		for (SiteStatsFilter filter : tab.getFilters()) {
			if (id.equals(filter.getId())) {
				return filter;
			}
		}
		return null;
	}

	private boolean hasColumn(SiteStatsReportView view, String key) {
		for (SiteStatsTableColumn column : view.getTable().getColumns()) {
			if (key.equals(column.getKey())) {
				return true;
			}
		}
		return false;
	}

	private SiteStatsWidgetMetricSnapshot snapshot(String widgetId, String metricId) {
		return snapshot(widgetId, metricId, null);
	}

	private SiteStatsWidgetMetricSnapshot snapshot(String widgetId, String metricId, SiteStatsReportRequest request) {
		List<SiteStatsWidgetMetric> metrics = request == null
				? service.getWidgetMetrics(SITE_ID, widgetId)
				: service.getWidgetMetrics(SITE_ID, widgetId, request);
		for (SiteStatsWidgetMetric metric : metrics) {
			if (metricId.equals(metric.getId())) {
				return metric.getSnapshot();
			}
		}
		throw new AssertionError("Missing metric value " + widgetId + "/" + metricId);
	}

	private boolean hasWidget(SiteStatsOverview overview, String widgetId) {
		return widget(overview, widgetId) != null;
	}

	private SiteStatsWidget widget(SiteStatsOverview overview, String widgetId) {
		for (SiteStatsWidget candidate : overview.getWidgets()) {
			if (widgetId.equals(candidate.getId())) {
				return candidate;
			}
		}
		return null;
	}
}
