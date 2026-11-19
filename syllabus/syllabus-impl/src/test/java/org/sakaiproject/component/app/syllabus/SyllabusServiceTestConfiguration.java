/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.component.app.syllabus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource({"classpath:/WEB-INF/components.xml", "classpath:/syllabus-test-boundaries.xml"})
@PropertySource("classpath:/hibernate.properties")
public class SyllabusServiceTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.syllabus")
    private AdditionalHibernateMappings additionalHibernateMappings;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return additionalHibernateMappings;
    }

    @Bean(name = "org.sakaiproject.event.api.NotificationService")
    public NotificationService notificationService() {
        NotificationService service = mock(NotificationService.class);
        when(service.addTransientNotification()).thenReturn(mock(NotificationEdit.class));
        return service;
    }

    @Bean(name = "org.sakaiproject.calendar.api.CalendarService")
    public CalendarService calendarService() {
        return mock(CalendarService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.TimeService")
    public TimeService timeService() {
        return mock(TimeService.class);
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.LTIService")
    public LTIService ltiService() {
        return mock(LTIService.class);
    }

    @Bean(name = "org.sakaiproject.search.api.SearchIndexBuilder")
    public SearchIndexBuilder searchIndexBuilder() {
        return mock(SearchIndexBuilder.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return mock(SchedulerManager.class);
    }

    @Bean(name = "org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService")
    public UserNotificationPreferencesRegistrationService userNotificationPreferencesRegistrationService() {
        return mock(UserNotificationPreferencesRegistrationService.class);
    }
}
