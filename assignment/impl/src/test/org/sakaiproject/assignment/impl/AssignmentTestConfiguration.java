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
package org.sakaiproject.assignment.impl;

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
import org.mockito.Mockito;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.taggable.AssignmentActivityProducer;
import org.sakaiproject.assignment.impl.reminder.prefs.AssignmentUserNotificationPreferencesRegistrationImpl;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.contentreview.service.ContentReviewService;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.timesheet.api.TimeSheetService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.api.LinkMigrationHelper;
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
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Created by enietzel on 4/12/17.
 */
@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class AssignmentTestConfiguration {

    @Autowired
    private Environment environment;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.assignment")
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

    @Bean(name = "org.sakaiproject.authz.api.SecurityService")
    public SecurityService securityService() {
        return mock(SecurityService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        return mock(SessionManager.class);
    }

    @Bean(name = "org.sakaiproject.entity.api.EntityManager")
    public EntityManager entityManager() {
        return mock(EntityManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.EventTrackingService")
    public EventTrackingService eventTrackingService() {
        return mock(EventTrackingService.class);
    }

    @Bean(name = "org.sakaiproject.announcement.api.AnnouncementService")
    public AnnouncementService announcementService() {
        return mock(AnnouncementService.class);
    }

    @Bean(name = "org.sakaiproject.assignment.api.taggable.AssignmentActivityProducer")
    public AssignmentActivityProducer assignmentActivityProducer() {
        return mock(AssignmentActivityProducer.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.AuthzGroupService")
    public AuthzGroupService authzGroupService() {
        return mock(AuthzGroupService.class);
    }

    @Bean(name = "org.sakaiproject.calendar.api.CalendarService")
    public CalendarService calendarService() {
        return mock(CalendarService.class);
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {
        return mock(ContentHostingService.class);
    }

    @Bean(name = "org.sakaiproject.entitybroker.DeveloperHelperService")
    public DeveloperHelperService developerHelperService() {
        return mock(DeveloperHelperService.class);
    }

    @Bean(name = "org.sakaiproject.email.api.DigestService")
    public DigestService digestService() {
        return mock(DigestService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        return mock(FormattedText.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.FunctionManager")
    public FunctionManager functionManager() {
        return mock(FunctionManager.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradingService() {
        return mock(GradingService.class);
    }

    @Bean(name = "org.sakaiproject.assignment.impl.GradeSheetExporter")
    public GradeSheetExporter gradeSheetExporter() {
        return mock(GradeSheetExporter.class);
    }

    @Bean(name = "org.sakaiproject.site.api.SiteService")
    public SiteService siteService() {
        return mock(SiteService.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService scs = mock(ServerConfigurationService.class);
        Mockito.when(scs.getBoolean("content.cleaner.filter.utf8", true)).thenReturn(Boolean.TRUE);
        Mockito.when(scs.getString("content.cleaner.filter.utf8.replacement", "")).thenReturn("");
        Mockito.when(scs.getString("assignment.timesheet.timePattern", AssignmentServiceConstants.SAK_PROP_ASSIGNMENT_TIMESHEET_TIME_PATTERN_DEFAULT)).thenReturn(AssignmentServiceConstants.SAK_PROP_ASSIGNMENT_TIMESHEET_TIME_PATTERN_DEFAULT);
        Mockito.when(scs.getInt("assignment.grading.decimals", AssignmentConstants.DEFAULT_DECIMAL_POINT)).thenReturn(2);
        return scs;
    }

    @Bean(name = "org.sakaiproject.taggable.api.TaggingManager")
    public TaggingManager taggingManager() {
        return mock(TaggingManager.class);
    }

    @Bean(name = "org.sakaiproject.time.api.TimeService")
    public TimeService timeService() {
        return mock(TimeService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.ToolManager")
    public ToolManager toolManager() {
        return mock(ToolManager.class);
    }

    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.ScheduledInvocationManager")
    public ScheduledInvocationManager scheduledInvocationManager() {
        return mock(ScheduledInvocationManager.class);
    }

    @Bean(name = "org.sakaiproject.contentreview.service.ContentReviewService")
    public ContentReviewService contentReviewService() {
        return mock(ContentReviewService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.LearningResourceStoreService")
    public LearningResourceStoreService learningResourceStoreService() {
        return mock(LearningResourceStoreService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.LinkMigrationHelper")
    public LinkMigrationHelper linkMigrationHelper() {
        return mock(LinkMigrationHelper.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return mock(SchedulerManager.class);
    }

    @Bean(name = "org.sakaiproject.user.api.UserNotificationPreferencesRegistration")
    public UserNotificationPreferencesRegistration userNotificationPreferencesRegistration() {
        return mock(AssignmentUserNotificationPreferencesRegistrationImpl.class);
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.rubrics.api.RubricsService")
    public RubricsService rubricsService() {
        return mock(RubricsService.class);
    }

    @Bean(name = "org.sakaiproject.elfinder.SakaiFsService")
    public SakaiFsService sakaiFsService() {
        return mock(SakaiFsService.class);
    }

    @Bean(name = "org.sakaiproject.search.api.SearchService")
    public SearchService searchService() {
        return mock(SearchService.class);
    }

    @Bean(name = "org.sakaiproject.search.api.SearchIndexBuilder")
    public SearchIndexBuilder searchIndexBuilder() {
        return mock(SearchIndexBuilder.class);
    }

    @Bean(name = "org.springproject.transaction.support.TransactionTemplate")
    public TransactionTemplate transactionTemplate() {
        return mock(TransactionTemplate.class);
    }

    @Bean(name = "org.sakaiproject.tasks.api.TaskService")
    public TaskService taskService() {
        return mock(TaskService.class);
    }

    @Bean(name = "org.sakaiproject.messaging.api.UserMessagingService")
    public UserMessagingService userMessagingService() {
        return mock(UserMessagingService.class);
    }

    @Bean(name = "org.sakaiproject.timesheet.api.TimeSheetService")
    public TimeSheetService timeSheetService() {
        return mock(TimeSheetService.class);
    }

    @Bean(name = "org.sakaiproject.tags.api.TagService")
    public TagService tagService() {
        return mock(TagService.class);
    }

    @Bean(name = "org.sakaiproject.lti.api.LTIService")
    public LTIService ltiService() {
        return mock(LTIService.class);
    }

    @Bean(name = "org.sakaiproject.email.api.EmailService")
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean(name = "org.sakaiproject.emailtemplateservice.api.EmailTemplateService")
    public EmailTemplateService emailTemplateService() {
        return mock(EmailTemplateService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }
}
