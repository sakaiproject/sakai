/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.datemanager.test;

import org.mockito.Mockito;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.samigo.api.SamigoAvailableNotificationService;
import org.sakaiproject.signup.api.SignupMeetingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;

import java.util.Locale;

import static org.mockito.Mockito.when;

@Configuration
@ImportResource("classpath:/WEB-INF/components.xml")
public class DateManagerTestConfiguration {

    @Autowired
    private Environment environment;

    @Bean(name = "org.sakaiproject.assignment.api.AssignmentService")
    public AssignmentService assignmentService() {
        return Mockito.mock(AssignmentService.class);
    }

    @Bean(name = "org.sakaiproject.announcement.api.AnnouncementService")
    public AnnouncementService announcementService() {
        return Mockito.mock(AnnouncementService.class);
    }

    @Bean(name = "PersistenceService")
    public PersistenceService persistenceService() {
        return Mockito.mock(PersistenceService.class);
    }

    @Bean(name = "org.sakaiproject.calendar.api.CalendarService")
    public CalendarService calendarService() {
        return Mockito.mock(CalendarService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return Mockito.mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        return Mockito.mock(FormattedText.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradingService() {
        return Mockito.mock(GradingService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.messageforums.MessageForumsForumManager")
    public MessageForumsForumManager messageForumsForumManager() {
        return Mockito.mock(MessageForumsForumManager.class);
    }

    @Bean(name = "org.sakaiproject.samigo.api.SamigoAvailableNotificationService")
    public SamigoAvailableNotificationService samigoAvailableNotificationService() {
        return Mockito.mock(SamigoAvailableNotificationService.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        return Mockito.mock(ServerConfigurationService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        return Mockito.mock(SessionManager.class);
    }

    @Bean(name = "org.sakaiproject.signup.api.SignupMeetingService")
    public SignupMeetingService signupMeetingService() {
        return Mockito.mock(SignupMeetingService.class);
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.model.SimplePageToolDao")
    public SimplePageToolDao simplePageToolDao() {
        return Mockito.mock(SimplePageToolDao.class);
    }

    @Bean(name = "org.sakaiproject.site.api.SiteService")
    public SiteService siteService() {
        return Mockito.mock(SiteService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.TimeService")
    public TimeService timeService() {
        return Mockito.mock(TimeService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.ToolManager")
    public ToolManager toolManager() {
        return Mockito.mock(ToolManager.class);
    }

    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return Mockito.mock(UserDirectoryService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return Mockito.mock(UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return Mockito.mock(SchedulerManager.class);
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return Mockito.mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.util.ResourceLoader.datemanager")
    public ResourceLoader resourceLoader() {
        ResourceLoader resourceLoader = Mockito.mock(ResourceLoader.class);
        when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        return resourceLoader;
    }
}
