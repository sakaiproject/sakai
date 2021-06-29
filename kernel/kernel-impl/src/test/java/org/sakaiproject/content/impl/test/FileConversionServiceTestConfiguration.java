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
package org.sakaiproject.content.impl.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.sql.DataSource;
import java.io.IOException;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.HSQLDialect;

import org.hsqldb.jdbcDriver;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FileConversionService;
import org.sakaiproject.content.api.persistence.FileConversionQueueItem;
import org.sakaiproject.content.api.persistence.FileConversionServiceRepository;
import org.sakaiproject.content.impl.FileConversionServiceImpl;
import org.sakaiproject.content.impl.persistence.FileConversionServiceRepositoryImpl;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;

@Configuration
@PropertySource("classpath:/hibernate.properties")
public class FileConversionServiceTestConfiguration {

    @Autowired
    private Environment environment;

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.fileconversionservice")
    public AdditionalHibernateMappings hibernateMappings() {

        Class[] annotatedClasses = new Class[] {FileConversionQueueItem.class};
        AdditionalHibernateMappings mappings = new AdditionalHibernateMappingsImpl();
        mappings.setAnnotatedClasses(annotatedClasses);
        return mappings;
    }

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory() throws IOException {

        DataSource dataSource = dataSource();
        LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(dataSource);
        StandardServiceRegistryBuilder srb = sfb.getStandardServiceRegistryBuilder();
        srb.applySetting(org.hibernate.cfg.Environment.DATASOURCE, dataSource);
        srb.applySettings(hibernateProperties());
        StandardServiceRegistry sr = srb.build();
        hibernateMappings().processAdditionalMappings(sfb);
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

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {

        ServerConfigurationService scs = mock(ServerConfigurationService.class);
        when(scs.getBoolean("fileconversion.submit.enabled", false)).thenReturn(true);
        when(scs.getStringList("fileconversion.fromtypes", FileConversionService.DEFAULT_TYPES)).thenReturn(FileConversionService.DEFAULT_TYPES);
        when(scs.getString("fileconversion.converterurl", "http://localhost:9980")).thenReturn("http://localhost:9980");
        when(scs.getInt("fileconversion.workerthreads", 5)).thenReturn(5);
        when(scs.getInt("fileconversion.queueintervalminutes", 1)).thenReturn(1);
        when(scs.getInt("fileconversion.pausemillis", 1000)).thenReturn(1000);
        return scs;
    }

    @Bean(name = "org.sakaiproject.content.api.ContentHostingService")
    public ContentHostingService contentHostingService() {

        ContentHostingService chs = mock(ContentHostingService.class);
        return chs;
    }

    @Bean(name = "org.sakaiproject.authz.api.SecurityService")
    public SecurityService securityService() {

        SecurityService ss = mock(SecurityService.class);
        return ss;
    }

    @Bean(name = "org.sakaiproject.content.api.persistence.FileConversionServiceRepository")
    public FileConversionServiceRepository fileConversionServiceRepository(SessionFactory sessionFactory) {

        FileConversionServiceRepositoryImpl repository = new FileConversionServiceRepositoryImpl();
        repository.setSessionFactory(sessionFactory);
        return repository;
    }

    @Bean
    public FileConversionService fileConversionService(ServerConfigurationService scs, FileConversionServiceRepository repository) {

        FileConversionServiceImpl fcs = new FileConversionServiceImpl();
        fcs.setServerConfigurationService(scs);
        fcs.setRepository(repository);
        fcs.init();
        return fcs;
    }
}
