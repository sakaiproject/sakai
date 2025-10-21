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
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentService;
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
import java.text.NumberFormat;

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
}
