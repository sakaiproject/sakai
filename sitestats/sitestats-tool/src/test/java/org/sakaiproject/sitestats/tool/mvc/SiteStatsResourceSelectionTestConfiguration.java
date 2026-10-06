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
package org.sakaiproject.sitestats.tool.mvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.TimeZone;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.util.api.LocaleService;
import org.sakaiproject.sitestats.tool.config.SiteStatsWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:hibernate.properties")
@Import(SiteStatsWebMvcConfiguration.class)
@ImportResource("file:target/test-component/WEB-INF/components.xml")
public class SiteStatsResourceSelectionTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings.sitestats")
    private AdditionalHibernateMappings mappings;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return mappings;
    }

    @Bean
    public static BeanFactoryPostProcessor disableBackgroundCollector() {
        return factory -> {
            factory.getBeanDefinition("org.sakaiproject.sitestats.api.StatsUpdateManager.target")
                    .getPropertyValues().add("collectThreadEnabled", false);
            factory.getBeanDefinition("org.sakaiproject.sitestats.api.StatsAggregateJob")
                    .getPropertyValues().add("startEventId", 0L);
        };
    }

    @Bean(name = "org.sakaiproject.memory.api.MemoryService")
    @Override
    public MemoryService memoryService() {
        return new org.sakaiproject.memory.mock.MemoryService();
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    @Override
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService configuration = mock(ServerConfigurationService.class);
        when(configuration.getString("sitestats.db", "internal")).thenReturn("internal");
        when(configuration.getString("hibernate.dialect", "org.hibernate.dialect.HSQLDialect"))
                .thenReturn("org.hibernate.dialect.HSQLDialect");
        when(configuration.getBoolean("display.users.present", true)).thenReturn(true);
        when(configuration.getBoolean("presence.events.log", true)).thenReturn(true);
        return configuration;
    }

    @Bean(name = "org.sakaiproject.util.api.LocaleService")
    public LocaleService localeService() {
        LocaleService localeService = mock(LocaleService.class);
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.US);
        when(localeService.getLocaleForSiteAndUser(anyString(), anyString())).thenReturn(Locale.US);
        return localeService;
    }

    @Bean
    @Primary
    public MessageSource testMessages() {
        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("Messages");
        messages.setFallbackToSystemLocale(false);
        return messages;
    }

    @Bean(name = "org.sakaiproject.alias.api.AliasService")
    public org.sakaiproject.alias.api.AliasService aliasService() {
        return mock(org.sakaiproject.alias.api.AliasService.class);
    }

    @Bean(name = "org.sakaiproject.announcement.api.AnnouncementService")
    public org.sakaiproject.announcement.api.AnnouncementService announcementService() {
        return mock(org.sakaiproject.announcement.api.AnnouncementService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager")
    public org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager discussionForumManager() {
        return mock(org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager.class);
    }

    @Bean(name = "org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager")
    public org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager uIPermissionsManager() {
        return mock(org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager.class);
    }

    @Bean(name = "org.sakaiproject.api.app.podcasts.PodcastService")
    public org.sakaiproject.api.app.podcasts.PodcastService podcastService() {
        return mock(org.sakaiproject.api.app.podcasts.PodcastService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public org.sakaiproject.api.app.scheduler.SchedulerManager schedulerManager() {
        return mock(org.sakaiproject.api.app.scheduler.SchedulerManager.class);
    }

    @Bean(name = "org.sakaiproject.assignment.api.AssignmentService")
    public org.sakaiproject.assignment.api.AssignmentService assignmentService() {
        return mock(org.sakaiproject.assignment.api.AssignmentService.class);
    }

    @Bean(name = "org.sakaiproject.calendar.api.CalendarService")
    public org.sakaiproject.calendar.api.CalendarService calendarService() {
        return mock(org.sakaiproject.calendar.api.CalendarService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public org.sakaiproject.content.api.ContentHostingService contentHostingService() {
        return mock(org.sakaiproject.content.api.ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentTypeImageService")
    public org.sakaiproject.content.api.ContentTypeImageService contentTypeImageService() {
        return mock(org.sakaiproject.content.api.ContentTypeImageService.class);
    }

    @Bean(name = "org.sakaiproject.db.api.SqlService")
    public org.sakaiproject.db.api.SqlService sqlService() {
        return mock(org.sakaiproject.db.api.SqlService.class);
    }

    @Bean(name = "org.sakaiproject.entitybroker.DeveloperHelperService")
    public org.sakaiproject.entitybroker.DeveloperHelperService developerHelperService() {
        return mock(org.sakaiproject.entitybroker.DeveloperHelperService.class);
    }

    @Bean(name = "org.sakaiproject.entitybroker.EntityBroker")
    public org.sakaiproject.entitybroker.EntityBroker entityBroker() {
        return mock(org.sakaiproject.entitybroker.EntityBroker.class);
    }

    @Bean(name = "org.sakaiproject.entitybroker.entityprovider.EntityProviderManager")
    public org.sakaiproject.entitybroker.entityprovider.EntityProviderManager entityProviderManager() {
        return mock(org.sakaiproject.entitybroker.entityprovider.EntityProviderManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public org.sakaiproject.event.api.UsageSessionService usageSessionService() {
        return mock(org.sakaiproject.event.api.UsageSessionService.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public org.sakaiproject.grading.api.GradingService gradingService() {
        return mock(org.sakaiproject.grading.api.GradingService.class);
    }

    @Bean(name = "org.sakaiproject.lessonbuildertool.model.SimplePageToolDao")
    public org.sakaiproject.lessonbuildertool.model.SimplePageToolDao simplePageToolDao() {
        return mock(org.sakaiproject.lessonbuildertool.model.SimplePageToolDao.class);
    }

    @Bean(name = "org.sakaiproject.poll.api.service.PollsService")
    public org.sakaiproject.poll.api.service.PollsService pollsService() {
        return mock(org.sakaiproject.poll.api.service.PollsService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public org.sakaiproject.time.api.UserTimeService userTimeService() {
        org.sakaiproject.time.api.UserTimeService service = mock(org.sakaiproject.time.api.UserTimeService.class);
        when(service.getLocalTimeZone()).thenReturn(TimeZone.getTimeZone("UTC"));
        return service;
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public org.sakaiproject.user.api.PreferencesService preferencesService() {
        return mock(org.sakaiproject.user.api.PreferencesService.class);
    }

    @Bean(name = "uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService")
    public uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService rWikiSecurityService() {
        return mock(uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService.class);
    }

}
