/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.webapi.controllers.test;

import static org.mockito.Mockito.mock;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.scorm.service.api.ScormLaunchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebApiTestConfiguration {

    @Bean
    public AnnouncementService announcementService() {
        return mock(AnnouncementService.class);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return mock(AuthenticationManager.class);
    }

    @Bean
    public AuthzGroupService authzGroupService() {
        return mock(AuthzGroupService.class);
    }

    @Bean
    public CalendarService CalendarService() {
        return mock(CalendarService.class);
    }

    @Bean
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean
    public EntityManager entityManager() {
        return mock(EntityManager.class);
    }

    @Bean
    public EventTrackingService eventTrackingService() {
        return mock(EventTrackingService.class);
    }

    @Bean
    public FormattedText formattedText() {
        return mock(FormattedText.class);
    }

    @Bean
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }

    @Bean
    public GradingService gradingService() {
        return mock(GradingService.class);
    }

    @Bean
    public PortalService portalService() {
        return mock(PortalService.class);
    }

    @Bean
    public PreferencesService preferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean
    public SakaiPersonManager sakaiPersonManager() {
        return mock(SakaiPersonManager.class);
    }

    @Bean
    public ProfileService profileService() {
        return mock(ProfileService.class);
    }

    @Bean
    public ScormLaunchService scormLaunchService() {
        return mock(ScormLaunchService.class);
    }

    @Bean
    public SecurityService securityService() {
        return mock(SecurityService.class);
    }

    @Bean
    public ServerConfigurationService serverConfigurationService() {
        return mock(ServerConfigurationService.class);
    }

    @Bean
    @Qualifier("org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        return mock(SessionManager.class);
    }

    @Bean
    public SiteService siteService() {
        return mock(SiteService.class);
    }

    @Bean
    public TaskService taskService() {
        return mock(TaskService.class);
    }

    @Bean
    public ToolManager toolManager() {
        return mock(ToolManager.class);
    }

    @Bean
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }

    @Bean
    public UserMessagingService userMessagingService() {
        return mock(UserMessagingService.class);
    }

    @Bean
    public HttpServletRequest request() {
        return mock(HttpServletRequest.class);
    }

    @Bean
    public HttpServletResponse response() {
        return mock(HttpServletResponse.class);
    }

    @Bean
    public SakaiAccessTokenService accessTokenService() {
        return mock(SakaiAccessTokenService.class);
    }

    @Bean
    public TagService tagService() {
        return mock(TagService.class);
    }

		@Bean
		public SiteStatsViewService siteStatsViewService() {
			  return mock(SiteStatsViewService.class);
		}
}
