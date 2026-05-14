/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.assignment.tool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.api.reminder.AssignmentDueReminderService;
import org.sakaiproject.assignment.api.taggable.AssignmentActivityProducer;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.FileConversionService;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentActionTest {

    private static final String PLESUSE2 = "Too many decimal places";

    private MockedStatic<ComponentManager> componentManagerMock;
    private MockedStatic<WebApplicationContextUtils> webAppContextUtilsMock;

    @Mock private AnnouncementService announcementService;
    @Mock private AssignmentActivityProducer assignmentActivityProducer;
    @Mock private AssignmentDueReminderService assignmentDueReminderService;
    @Mock private AssignmentPeerAssessmentService assignmentPeerAssessmentService;
    @Mock private AssignmentService assignmentService;
    @Mock private AssignmentSupplementItemService assignmentSupplementItemService;
    @Mock private AuthzGroupService authzGroupService;
    @Mock private CalendarService calendarService;
    @Mock private ContentHostingService contentHostingService;
    @Mock private ContentReviewService contentReviewService;
    @Mock private ContentTypeImageService contentTypeImageService;
    @Mock private EntityManager entityManager;
    @Mock private EventTrackingService eventTrackingService;
    @Mock private FileConversionService fileConversionService;
    @Mock private FormattedText formattedText;
    @Mock private GradingService gradingService;
    @Mock private LearningResourceStoreService learningResourceStoreService;
    @Mock private LTIService ltiService;
    @Mock private NotificationService notificationService;
    @Mock private PreferencesService preferencesService;
    @Mock private ResourceLoader resourceLoader;
    @Mock private RubricsService rubricsService;
    @Mock private SecurityService securityService;
    @Mock private ServerConfigurationService serverConfigurationService;
    @Mock private ServletConfig servletConfig;
    @Mock private ServletContext servletContext;
    @Mock private SessionManager sessionManager;
    @Mock private SiteService siteService;
    @Mock private TaggingManager taggingManager;
    @Mock private TagService tagService;
    @Mock private TimeService timeService;
    @Mock private ToolManager toolManager;
    @Mock private UserDirectoryService userDirectoryService;
    @Mock private UserTimeService userTimeService;
    @Mock private WebApplicationContext webApplicationContext;

    private AssignmentAction assignmentAction;
    private AssignmentToolUtils assignmentToolUtils;

    @Before
    public void setUp() throws Exception {
        componentManagerMock = Mockito.mockStatic(ComponentManager.class);

        componentManagerMock.when(() -> ComponentManager.get(AnnouncementService.class)).thenReturn(announcementService);
        componentManagerMock.when(() -> ComponentManager.get(AssignmentActivityProducer.class)).thenReturn(assignmentActivityProducer);
        componentManagerMock.when(() -> ComponentManager.get(AssignmentDueReminderService.class)).thenReturn(assignmentDueReminderService);
        componentManagerMock.when(() -> ComponentManager.get(AssignmentPeerAssessmentService.class)).thenReturn(assignmentPeerAssessmentService);
        componentManagerMock.when(() -> ComponentManager.get(AssignmentService.class)).thenReturn(assignmentService);
        componentManagerMock.when(() -> ComponentManager.get(AssignmentSupplementItemService.class)).thenReturn(assignmentSupplementItemService);
        componentManagerMock.when(() -> ComponentManager.get(AuthzGroupService.class)).thenReturn(authzGroupService);
        componentManagerMock.when(() -> ComponentManager.get(CalendarService.class)).thenReturn(calendarService);
        componentManagerMock.when(() -> ComponentManager.get(ContentHostingService.class)).thenReturn(contentHostingService);
        componentManagerMock.when(() -> ComponentManager.get(ContentReviewService.class)).thenReturn(contentReviewService);
        componentManagerMock.when(() -> ComponentManager.get(ContentTypeImageService.class)).thenReturn(contentTypeImageService);
        componentManagerMock.when(() -> ComponentManager.get(EntityManager.class)).thenReturn(entityManager);
        componentManagerMock.when(() -> ComponentManager.get(EventTrackingService.class)).thenReturn(eventTrackingService);
        componentManagerMock.when(() -> ComponentManager.get(FileConversionService.class)).thenReturn(fileConversionService);
        componentManagerMock.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(formattedText);
        componentManagerMock.when(() -> ComponentManager.get("org.sakaiproject.grading.api.GradingService")).thenReturn(gradingService);
        componentManagerMock.when(() -> ComponentManager.get(LearningResourceStoreService.class)).thenReturn(learningResourceStoreService);
        componentManagerMock.when(() -> ComponentManager.get(NotificationService.class)).thenReturn(notificationService);
        componentManagerMock.when(() -> ComponentManager.get(PreferencesService.class)).thenReturn(preferencesService);
        componentManagerMock.when(() -> ComponentManager.get(RubricsService.class)).thenReturn(rubricsService);
        componentManagerMock.when(() -> ComponentManager.get(SecurityService.class)).thenReturn(securityService);
        componentManagerMock.when(() -> ComponentManager.get(ServerConfigurationService.class)).thenReturn(serverConfigurationService);
        componentManagerMock.when(() -> ComponentManager.get(SessionManager.class)).thenReturn(sessionManager);
        componentManagerMock.when(() -> ComponentManager.get(SiteService.class)).thenReturn(siteService);
        componentManagerMock.when(() -> ComponentManager.get(TaggingManager.class)).thenReturn(taggingManager);
        componentManagerMock.when(() -> ComponentManager.get(TimeService.class)).thenReturn(timeService);
        componentManagerMock.when(() -> ComponentManager.get(ToolManager.class)).thenReturn(toolManager);
        componentManagerMock.when(() -> ComponentManager.get(UserDirectoryService.class)).thenReturn(userDirectoryService);
        componentManagerMock.when(() -> ComponentManager.get(UserTimeService.class)).thenReturn(userTimeService);
        componentManagerMock.when(() -> ComponentManager.get(LTIService.class)).thenReturn(ltiService);
        componentManagerMock.when(() -> ComponentManager.get(TagService.class)).thenReturn(tagService);

        assignmentToolUtils = new AssignmentToolUtils(resourceLoader);
        assignmentToolUtils.setAssignmentService(assignmentService);
        assignmentToolUtils.setFormattedText(formattedText);
        assignmentToolUtils.setGradingService(gradingService);
        assignmentToolUtils.setRubricsService(rubricsService);
        assignmentToolUtils.setSiteService(siteService);
        assignmentToolUtils.setTimeService(timeService);
        assignmentToolUtils.setToolManager(toolManager);
        assignmentToolUtils.setUserDirectoryService(userDirectoryService);

        webAppContextUtilsMock = Mockito.mockStatic(WebApplicationContextUtils.class);
        webAppContextUtilsMock.when(() -> WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)).thenReturn(webApplicationContext);
        Mockito.when(webApplicationContext.getBean("org.sakaiproject.assignment.tool.AssignmentToolUtils", AssignmentToolUtils.class)).thenReturn(assignmentToolUtils);
        Mockito.when(servletConfig.getServletContext()).thenReturn(servletContext);

        assignmentAction = new AssignmentAction();
        assignmentAction.init(servletConfig);
    }

    @After
    public void tearDown() {
        if (componentManagerMock != null) {
            componentManagerMock.close();
        }
        if (webAppContextUtilsMock != null) {
            webAppContextUtilsMock.close();
        }
    }

    public Integer getScaleFactor(Integer decimals) {
        return Double.valueOf(Math.pow(10.0, decimals)).intValue();
    }

    @Test
    public void testScalePointGrade() throws Exception {
        Mockito.when(formattedText.getDecimalSeparator()).thenReturn(".");
        Mockito.when(formattedText.getNumberFormat()).thenReturn(NumberFormat.getInstance());
        Mockito.when(resourceLoader.getFormattedMessage(Mockito.eq("plesuse2"), Mockito.anyString())).thenReturn(PLESUSE2);

        SessionState state = new SessionStateFake();

        Integer decimals = 2;
        String scaledGrade = assignmentAction.scalePointGrade(state, ".7", getScaleFactor(decimals));
        Assert.assertEquals("70", scaledGrade);
        Assert.assertNull(state.getAttribute(AssignmentAction.STATE_MESSAGE));
        state.clear();

        scaledGrade = assignmentAction.scalePointGrade(state, "1.23456789", getScaleFactor(decimals));
        Assert.assertEquals("123456789", scaledGrade);
        Assert.assertEquals(PLESUSE2, state.getAttribute(AssignmentAction.STATE_MESSAGE));
        state.clear();

        decimals = 4;
        scaledGrade = assignmentAction.scalePointGrade(state, ".7", getScaleFactor(decimals));
        Assert.assertEquals("7000", scaledGrade);
        Assert.assertNull(state.getAttribute(AssignmentAction.STATE_MESSAGE));
        state.clear();
    }

    @Test
    public void testIntegrateWithAnnouncementPublishesLinkedDraftImport() throws Exception {
        SessionState state = new SessionStateFake();
        AnnouncementChannel channel = Mockito.mock(AnnouncementChannel.class);
        AnnouncementMessage existingMessage = Mockito.mock(AnnouncementMessage.class);
        AnnouncementMessageHeader existingHeader = Mockito.mock(AnnouncementMessageHeader.class);
        AnnouncementMessageEdit editMessage = Mockito.mock(AnnouncementMessageEdit.class);
        AnnouncementMessageHeaderEdit editHeader = Mockito.mock(AnnouncementMessageHeaderEdit.class);
        ResourcePropertiesEdit editProperties = Mockito.mock(ResourcePropertiesEdit.class);
        Assignment assignment = Mockito.mock(Assignment.class);
        Map<String, String> properties = new HashMap<>();
        Instant openTime = Instant.parse("2026-04-14T16:00:00Z");

        properties.put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.TRUE.toString());
        properties.put(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID, "draft-announcement-id");

        state.setAttribute("announcement_channel", channel);

        Mockito.when(assignment.getProperties()).thenReturn(properties);
        Mockito.when(assignment.getTypeOfAccess()).thenReturn(Assignment.Access.SITE);
        Mockito.when(assignment.getId()).thenReturn("assignment-id");
        Mockito.when(channel.getAnnouncementMessage("draft-announcement-id")).thenReturn(existingMessage);
        Mockito.when(existingMessage.getId()).thenReturn("draft-announcement-id");
        Mockito.when(existingMessage.getAnnouncementHeader()).thenReturn(existingHeader);
        Mockito.when(existingHeader.getDraft()).thenReturn(true);
        Mockito.when(existingHeader.getSubject()).thenReturn("Open Assignment Imported Assignment");
        Mockito.when(existingHeader.getAccess()).thenReturn(org.sakaiproject.message.api.MessageHeader.MessageAccess.CHANNEL);
        Mockito.when(existingMessage.getBody()).thenReturn("<p>Open Date Body</p>");
        Mockito.when(channel.editAnnouncementMessage("draft-announcement-id")).thenReturn(editMessage);
        Mockito.when(editMessage.getAnnouncementHeaderEdit()).thenReturn(editHeader);
        Mockito.when(editMessage.getPropertiesEdit()).thenReturn(editProperties);
        Mockito.when(editMessage.getId()).thenReturn("draft-announcement-id");
        Mockito.when(entityManager.newReferenceList()).thenReturn(new java.util.ArrayList());
        Mockito.when(formattedText.convertPlaintextToFormattedText("Imported Assignment")).thenReturn("Imported Assignment");
        Mockito.when(userTimeService.dateTimeFormat(openTime, FormatStyle.MEDIUM, FormatStyle.LONG)).thenReturn("Apr 14, 2026 12:00 PM EDT");
        Mockito.when(assignmentService.getUsersLocalDateTimeString(openTime)).thenReturn("Apr 14, 2026 12:00 PM EDT");

        Method method = AssignmentAction.class.getDeclaredMethod("integrateWithAnnouncement", SessionState.class,
                String.class, Assignment.class, String.class, Instant.class, String.class, String.class, Instant.class);
        method.setAccessible(true);
        method.invoke(assignmentAction, state, "Imported Assignment", assignment, "Imported Assignment", openTime,
                Boolean.TRUE.toString(), org.sakaiproject.assignment.api.AssignmentConstants.ASSIGNMENT_OPENDATE_NOTIFICATION_NONE, openTime);

        Mockito.verify(channel).editAnnouncementMessage("draft-announcement-id");
        Mockito.verify(editHeader).setDraft(false);
        Mockito.verify(channel).commitMessage(editMessage, NotificationService.NOTI_NONE,
                "org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc");
        Assert.assertEquals(Boolean.TRUE.toString(), properties.get(org.sakaiproject.assignment.api.AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
        Assert.assertEquals("draft-announcement-id", properties.get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
        Mockito.verify(assignmentService).updateAssignment(assignment);
    }
}
