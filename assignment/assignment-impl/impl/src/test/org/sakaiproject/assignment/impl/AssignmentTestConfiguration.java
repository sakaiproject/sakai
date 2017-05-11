package org.sakaiproject.assignment.impl;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.HSQLDialect;
import org.hsqldb.jdbcDriver;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl.assignmentSupplementItem")
    private AdditionalHibernateMappings hibernateMappings;

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory() throws IOException {
        LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(dataSource());
        hibernateMappings.processAdditionalMappings(sfb);
//        sfb.addAnnotatedClasses(Assignment.class, AssignmentSubmission.class, AssessorSubmissionId.class, AssignmentSubmissionSubmitter.class);
        sfb.addProperties(hibernateProperties());
        return sfb.buildSessionFactory();
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
            }
        };
    }

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

//    @Bean(name = "org.sakaiproject.assignment.persistence.AssignmentRepository")
//    public AssignmentRepository assignmentRepository() throws IOException {
//        AssignmentRepositoryImpl repository = new AssignmentRepositoryImpl();
//        repository.setSessionFactory(sessionFactory());
//        return repository;
//    }

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

    @Bean(name = "org.sakaiproject.assignment.taggable.api.AssignmentActivityProducer")
    public AssignmentActivityProducer assignmentActivityProducer() {
        return mock(AssignmentActivityProducer.class);
    }

    @Bean(name = "org.sakaiproject.assignment.api.AssignmentPeerAssessmentService")
    public AssignmentPeerAssessmentService assignmentPeerAssessmentService() {
        return mock(AssignmentPeerAssessmentService.class);
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

    @Bean(name = "org.sakaiproject.email.api.EmailService")
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean(name = "org.sakaiproject.authz.api.FunctionManager")
    public FunctionManager functionManager() {
        return mock(FunctionManager.class);
    }

    @Bean(name = "org.sakaiproject.service.gradebook.GradebookExternalAssessmentService")
    public GradebookExternalAssessmentService gradebookExternalAssessmentService() {
        return mock(GradebookExternalAssessmentService.class);
    }

    @Bean(name = "org.sakaiproject.service.gradebook.GradebookService")
    public GradebookService gradebookService() {
        return mock(GradebookService.class);
    }

    @Bean(name = "org.sakaiproject.assignment.impl.GradeSheetExporter")
    public GradeSheetExporter gradeSheetExporter() {
        return mock(GradeSheetExporter.class);
    }

    @Bean(name = "org.sakaiproject.id.api.IdManager")
    public IdManager idManager() {
        return mock(IdManager.class);
    }

    @Bean(name = "org.sakaiproject.memory.api.MemoryService")
    public MemoryService memoryService() {
        return mock(MemoryService.class);
    }

    @Bean(name = "org.sakaiproject.site.api.SiteService")
    public SiteService siteService() {
        return mock(SiteService.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        return mock(ServerConfigurationService.class);
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
}
