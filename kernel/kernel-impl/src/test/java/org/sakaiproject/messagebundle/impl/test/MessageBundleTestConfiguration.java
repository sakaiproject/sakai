/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.messagebundle.impl.test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.HSQLDialect;
import org.hsqldb.jdbcDriver;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.impl.MessageBundleServiceImpl;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.sakaiproject.tool.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:/hibernate.properties")
public class MessageBundleTestConfiguration {
    @Autowired
    private Environment environment;

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.messagebundle")
    public AdditionalHibernateMappings hibernateMappings() {
        Class[] annotatedClasses = new Class[] {MessageBundleProperty.class};
        AdditionalHibernateMappings mappings = new AdditionalHibernateMappingsImpl();
        mappings.setAnnotatedClasses(annotatedClasses);
        return mappings;
    }

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory(Properties hibernateProperties, AdditionalHibernateMappings mappings) {
        LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(dataSource());
        try {
            mappings.processAdditionalMappings(sfb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sfb.addProperties(hibernateProperties);
        sfb.getIdentifierGeneratorFactory().register("uuid2", AssignableUUIDGenerator.class);
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
                setProperty(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, environment.getProperty(org.hibernate.cfg.Environment.ENABLE_LAZY_LOAD_NO_TRANS, "true"));
                setProperty(org.hibernate.cfg.Environment.CACHE_REGION_FACTORY, environment.getProperty(org.hibernate.cfg.Environment.CACHE_REGION_FACTORY));
            }
        };
    }

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService scs = mock(ServerConfigurationService.class);
        when(scs.getBoolean(eq("load.bundles.from.db"), eq(true))).thenReturn(true);
        return scs;
    }

    @Bean(name = "org.sakaiproject.messagebundle.api.MessageBundleService")
    public MessageBundleService messageBundleService(ServerConfigurationService serverConfigurationService, SessionFactory sessionFactory, PlatformTransactionManager transactionManager) {
        MessageBundleServiceImpl messageBundleService = new MessageBundleServiceImpl();
        messageBundleService.setServerConfigurationService(serverConfigurationService);
        messageBundleService.setSessionFactory(sessionFactory);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        messageBundleService.setTransactionTemplate(transactionTemplate);
        messageBundleService.setScheduleSaves(false);
        messageBundleService.init();
        return messageBundleService;
    }
}
