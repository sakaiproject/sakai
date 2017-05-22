package org.sakaiproject.component.app.scheduler;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.hsqldb.jdbcDriver;
import org.sakaiproject.scheduler.events.hibernate.ContextMapping;
import org.sakaiproject.scheduler.events.hibernate.DelayedInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for tests.
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:/hibernate.properties")
public class ContextMappingConfiguration {

    @Autowired
    private org.springframework.core.env.Environment env;

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory() {
        LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(dataSource());
        sfb.addAnnotatedClasses(ContextMapping.class, DelayedInvocation.class);
        sfb.addProperties(hibernateProperties());
        return sfb.buildSessionFactory();
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource db = new DriverManagerDataSource();
        db.setDriverClassName(env.getProperty(Environment.DRIVER, jdbcDriver.class.getName()));
        db.setUrl(env.getProperty(Environment.URL, "jdbc:hsqldb:mem:test"));
        db.setUsername(env.getProperty(Environment.USER, "SA"));
        db.setPassword(env.getProperty(Environment.PASS, ""));
        return db;
    }

    @Bean
    public Properties hibernateProperties() {
        return new Properties() {
            {
                setProperty(Environment.DIALECT, env.getProperty(Environment.DIALECT, HSQLDialect.class.getName()));
                setProperty(Environment.HBM2DDL_AUTO, env.getProperty(Environment.HBM2DDL_AUTO));
            }
        };
    }

    @Bean
    public ContextMappingDAO contextMapingDAO() {
        ContextMappingDAO dao = new ContextMappingDAO();
        return dao;
    }

    @Bean
    @Autowired
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager =  new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }
}
