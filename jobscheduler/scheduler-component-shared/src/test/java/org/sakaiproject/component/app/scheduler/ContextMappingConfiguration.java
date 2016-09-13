package org.sakaiproject.component.app.scheduler;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hsqldb.jdbcDriver;
import org.sakaiproject.scheduler.events.hibernate.ContextMapping;
import org.sakaiproject.scheduler.events.hibernate.DelayedInvocation;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for tests.
 */
@org.springframework.context.annotation.Configuration
@EnableTransactionManagement
public class ContextMappingConfiguration {

    private SessionFactory sessionFactory;

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory() {
        if (sessionFactory == null) {
            String dialectClassName = HSQLDialect.class.getName();
            Configuration config = new Configuration();
            config.addAnnotatedClass(ContextMapping.class);
            config.addAnnotatedClass(DelayedInvocation.class);

            config.setProperty(Environment.DIALECT, dialectClassName);
            config.setProperty(Environment.DRIVER, jdbcDriver.class.getName());
            config.setProperty(Environment.URL, "jdbc:hsqldb:mem:testDB");
            config.setProperty(Environment.USER, "SA");
            config.setProperty(Environment.PASS, "");
            // If we let spring create the session factory we probably wouldn't have to do this.
            config.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "org.springframework.orm.hibernate3.SpringSessionContext");

            SchemaExport export = new SchemaExport(config);
            export.create(false, true);

            sessionFactory = config.buildSessionFactory();
        }
        return sessionFactory;
    }

    @Bean
    public ContextMappingDAO contextMapingDAO() {
        ContextMappingDAO dao = new ContextMappingDAO();
        return dao;
    }

    @Bean
    public PlatformTransactionManager txManager() {
        return new HibernateTransactionManager(sessionFactory());
    }

}
