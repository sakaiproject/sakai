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

import java.io.IOException;
import java.util.Properties;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.id.factory.internal.MutableIdentifierGeneratorFactoryInitiator;
import org.hsqldb.jdbcDriver;

import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
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
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class ConversationsTestConfiguration {

    @Autowired
    private Environment environment;

    @Resource(name = "conversationsHibernateMappings")
    private AdditionalHibernateMappings hibernateMappings;

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory() throws IOException {

        DataSource dataSource = dataSource();
        LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(dataSource);
        StandardServiceRegistryBuilder srb = sfb.getStandardServiceRegistryBuilder();
        srb.applySetting(org.hibernate.cfg.Environment.DATASOURCE, dataSource);
        srb.applySettings(hibernateProperties());
        StandardServiceRegistry sr = srb.build();
        sr.getService(MutableIdentifierGeneratorFactoryInitiator.INSTANCE.getServiceInitiated())
                .register("uuid2", AssignableUUIDGenerator.class);
        hibernateMappings.processAdditionalMappings(sfb);
        return sfb.buildSessionFactory(sr);
    }

    @Bean(name = "javax.sql.DataSource")
    public DataSource dataSource() {

        DriverManagerDataSource db = new DriverManagerDataSource();
        db.setDriverClassName(environment.getProperty(org.hibernate.cfg.Environment.DRIVER, jdbcDriver.class.getName()));
        db.setUrl(environment.getProperty(org.hibernate.cfg.Environment.URL, "jdbc:hsqldb:mem:test"));
        db.setUsername(environment.getProperty(org.hibernate.cfg.Environment.USER, "sa"));
        db.setPassword(environment.getProperty(org.hibernate.cfg.Environment.PASS, ""));
        return db;
    }

    @Bean
    public Properties hibernateProperties() {

        return new Properties() {
            {
                setProperty(org.hibernate.cfg.Environment.DIALECT, environment.getProperty(org.hibernate.cfg.Environment.DIALECT, HSQLDialect.class.getName()));
                setProperty(org.hibernate.cfg.Environment.HBM2DDL_AUTO, environment.getProperty(org.hibernate.cfg.Environment.HBM2DDL_AUTO));
                setProperty(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, environment.getProperty(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true"));
                setProperty(org.hibernate.cfg.Environment.USE_SECOND_LEVEL_CACHE, environment.getProperty(org.hibernate.cfg.Environment.USE_SECOND_LEVEL_CACHE));
                setProperty(org.hibernate.cfg.Environment.CURRENT_SESSION_CONTEXT_CLASS, environment.getProperty(org.hibernate.cfg.Environment.CURRENT_SESSION_CONTEXT_CLASS));
            }
        };
    }

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {

        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

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

    @Bean(name = "org.sakaiproject.authz.api.AuthzGroupService")
    public AuthzGroupService authzGroupService() {
        return mock(AuthzGroupService.class);
    }

    @Bean(name = "org.sakaiproject.entity.api.EntityManager")
    public EntityManager entityManager() {
        return mock(EntityManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.EventTrackingService")
    public EventTrackingService eventTrackingService() {
        return mock(EventTrackingService.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.FunctionManager")
    public FunctionManager functionManager() {
        return mock(FunctionManager.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.SecurityService")
    public SecurityService securityService() {
        return mock(SecurityService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        return mock(SessionManager.class);
    }

    @Bean(name = "org.sakaiproject.sitestats.api.StatsManager")
    public StatsManager statsManager() {
        return mock(StatsManager.class);
    }

    @Bean(name = "org.sakaiproject.memory.api.MemoryService")
    public MemoryService memoryService() {
        return mock(MemoryService.class);
    }

    @Bean(name = "org.sakaiproject.site.api.SiteService")
    public SiteService siteService() {
        return mock(SiteService.class);
    }

    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
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

    @Bean(name = "org.sakaiproject.user.api.UserNotificationPreferencesRegistration")
    public UserNotificationPreferencesRegistration userNotificationPreferencesRegistration() {
        return mock(ConversationsNotificationPreferencesRegistrationImpl.class);
    }
}
