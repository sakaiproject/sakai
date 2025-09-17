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
package org.sakaiproject.conversations.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.util.Properties;
import java.util.Map;
import javax.annotation.Resource;

import org.hibernate.SessionFactory;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.TopicShowDateMessager;
import org.sakaiproject.conversations.api.repository.ConversationsCommentRepository;
import org.sakaiproject.conversations.impl.repository.ConversationsCommentRepositoryImpl;
import org.sakaiproject.conversations.api.repository.ConversationsPostRepository;
import org.sakaiproject.conversations.impl.repository.ConversationsPostRepositoryImpl;
import org.sakaiproject.conversations.api.repository.PostStatusRepository;
import org.sakaiproject.conversations.impl.repository.PostStatusRepositoryImpl;
import org.sakaiproject.conversations.api.repository.SettingsRepository;
import org.sakaiproject.conversations.impl.repository.SettingsRepositoryImpl;
import org.sakaiproject.conversations.api.repository.TagRepository;
import org.sakaiproject.conversations.impl.repository.TagRepositoryImpl;
import org.sakaiproject.conversations.api.repository.ConversationsTopicRepository;
import org.sakaiproject.conversations.impl.repository.ConversationsTopicRepositoryImpl;
import org.sakaiproject.conversations.api.repository.TopicStatusRepository;
import org.sakaiproject.conversations.impl.repository.TopicStatusRepositoryImpl;
import org.sakaiproject.conversations.impl.notificationprefs.ConversationsNotificationPreferencesRegistrationImpl;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.Getter;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class ConversationsTestConfiguration extends SakaiTestConfiguration {

    @Resource(name = "conversationsHibernateMappings")
    @Getter
    protected AdditionalHibernateMappings additionalHibernateMappings;

    @Bean(name="org.sakaiproject.conversations.api.repository.ConversationsPostRepository")
    public ConversationsPostRepository postRepository(SessionFactory sessionFactory) {

        ConversationsPostRepositoryImpl postRepository = new ConversationsPostRepositoryImpl();
        postRepository.setSessionFactory(sessionFactory);
        return postRepository;
    }

    @Bean(name="org.sakaiproject.conversations.api.repository.ConversationsCommentRepository")
    public ConversationsCommentRepository commentRepository(SessionFactory sessionFactory) {

        ConversationsCommentRepositoryImpl commentRepository = new ConversationsCommentRepositoryImpl();
        commentRepository.setSessionFactory(sessionFactory);
        return commentRepository;
    }

    @Bean(name="org.sakaiproject.conversations.api.repository.ConversationsTopicRepository")
    public ConversationsTopicRepository topicRepository(SessionFactory sessionFactory) {

        ConversationsTopicRepositoryImpl topicRepository = new ConversationsTopicRepositoryImpl();
        topicRepository.setSessionFactory(sessionFactory);
        return topicRepository;
    }

    @Bean(name="org.sakaiproject.conversations.api.repository.TopicStatusRepository")
    public TopicStatusRepository topicStatusRepository(SessionFactory sessionFactory) {

        TopicStatusRepositoryImpl topicStatusRepository = new TopicStatusRepositoryImpl();
        topicStatusRepository.setSessionFactory(sessionFactory);
        return topicStatusRepository;
    }

    @Bean(name="org.sakaiproject.conversations.api.repository.PostStatusRepository")
    public PostStatusRepository postStatusRepository(SessionFactory sessionFactory) {

        PostStatusRepositoryImpl postStatusRepository = new PostStatusRepositoryImpl();
        postStatusRepository.setSessionFactory(sessionFactory);
        return postStatusRepository;
    }

    @Bean(name="org.sakaiproject.conversations.api.repository.SettingsRepository")
    public SettingsRepository settingsRepository(SessionFactory sessionFactory) {

        SettingsRepositoryImpl settingsRepository = new SettingsRepositoryImpl();
        settingsRepository.setSessionFactory(sessionFactory);
        return settingsRepository;
    }

    @Bean(name="org.sakaiproject.conversations.api.repository.TagRepository")
    public TagRepository tagRepository(SessionFactory sessionFactory) {

        TagRepositoryImpl tagRepository = new TagRepositoryImpl();
        tagRepository.setSessionFactory(sessionFactory);
        return tagRepository;
    }

    @Bean(name = "org.sakaiproject.calendar.api.CalendarService")
    public CalendarService calendarService() {
        return mock(CalendarService.class);
    }

    @Bean(name = "org.sakaiproject.sitestats.api.StatsManager")
    public StatsManager statsManager() {
        return mock(StatsManager.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.TimeService")
    public TimeService timeService() {
        return mock(TimeService.class);
    }

    @Bean(name = "org.sakaiproject.messaging.api.UserMessagingService")
    public UserMessagingService userMessagingService() {
        return mock(UserMessagingService.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        return mock(ServerConfigurationService.class);
    }

    @Bean(name = "org.sakaiproject.search.api.SearchIndexBuilder")
    public SearchIndexBuilder searchIndexBuilder() {
        return mock(SearchIndexBuilder.class);
    }

    @Bean(name = "org.sakaiproject.search.api.SearchService")
    public SearchService searchService() {
        return mock(SearchService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.ScheduledInvocationManager")
    public ScheduledInvocationManager scheduledInvocationManager() {
        return mock(ScheduledInvocationManager.class);
    }

    @Bean(name = "org.sakaiproject.conversations.api.TopicShowDateMessager")
    public TopicShowDateMessager topicShowDateMessager() {
        return mock(TopicShowDateMessager.class);
    }

    @Bean(name = "org.sakaiproject.user.api.UserNotificationPreferencesRegistration")
    public UserNotificationPreferencesRegistration userNotificationPreferencesRegistration() {
        return mock(ConversationsNotificationPreferencesRegistrationImpl.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradingService() {
        return mock(GradingService.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.LTIService")
    public LTIService ltiService() {
        LTIService ltiService = mock(LTIService.class);
        when(ltiService.fixLtiLaunchUrls(anyString(), anyString(), anyString(), any(Map.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        return ltiService;
    }
}
