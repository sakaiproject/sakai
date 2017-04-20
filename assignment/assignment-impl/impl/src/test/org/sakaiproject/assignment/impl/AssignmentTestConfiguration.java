package org.sakaiproject.assignment.impl;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.HSQLDialect;
import org.hsqldb.jdbcDriver;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.AssessorSubmissionId;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.persistence.AssignmentRepository;
import org.sakaiproject.assignment.persistence.AssignmentRepositoryImpl;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@PropertySource("classpath:/hibernate.properties")
public class AssignmentTestConfiguration {

    @Autowired
    private Environment environment;

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory() {
        LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(dataSource());
        sfb.addAnnotatedClasses(Assignment.class, AssignmentSubmission.class, AssessorSubmissionId.class, AssignmentSubmissionSubmitter.class);
        sfb.addProperties(hibernateProperties());
        return sfb.buildSessionFactory();
    }

    @Bean
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

    @Bean
    @Autowired
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager =  new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

    @Bean
    public AssignmentRepository assignmentRepository() {
        AssignmentRepositoryImpl repository = new AssignmentRepositoryImpl();
        repository.setSessionFactory(sessionFactory());
        return repository;
    }

    @Bean
    public AssignmentService assignmentService() {
        AssignmentServiceImpl assignmentService = new AssignmentServiceImpl();
        assignmentService.setAssignmentRepository(assignmentRepository());
        // Add default mocks which can be overidden in the Test
        assignmentService.setSecurityService(Mockito.mock(SecurityService.class));
        assignmentService.setEventTrackingService(Mockito.mock(EventTrackingService.class));
        assignmentService.setSessionManager(Mockito.mock(SessionManager.class));
        return assignmentService;
    }
}
