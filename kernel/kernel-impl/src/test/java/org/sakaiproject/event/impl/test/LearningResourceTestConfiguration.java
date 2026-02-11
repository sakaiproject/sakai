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
package org.sakaiproject.event.impl.test;

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
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.ActivityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.sakaiproject.log.api.LogConfigurationManager;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.scheduling.api.SchedulingService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
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
@ImportResource("classpath:/WEB-INF/event-components.xml")
@PropertySource("classpath:/hibernate.properties")
public class LearningResourceTestConfiguration {

    @Autowired
    private Environment environment;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.tincanapiEvent")
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
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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

    @Bean(name = "org.sakaiproject.db.api.SqlService")
    public SqlService sqlService() {
        return mock(SqlService.class);
    }
    
    @Bean(name = "org.sakaiproject.authz.api.AuthzGroupService")
    public AuthzGroupService authzGroupService() {
        return mock(AuthzGroupService.class);
    }
    
    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }
    
    @Bean(name = "org.sakaiproject.memory.api.MemoryService")
    public MemoryService memoryService() {
        return mock(MemoryService.class);
    }
    
    @Bean(name = "org.sakaiproject.event.api.ActivityService")
    public ActivityService activityService() {
        return mock(ActivityService.class);
    }
    
    @Bean(name = "org.sakaiproject.event.api.NotificationService")
    public NotificationService notificationService() {
        return mock(NotificationService.class);
    }

    @Bean(name = "org.sakaiproject.log.api.LogConfigurationManager")
    public LogConfigurationManager logConfigurationManager() {
        return mock(LogConfigurationManager.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.ToolManager")
    public ToolManager toolManager() {
        return mock(ToolManager.class);
    }

    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService scs = mock(ServerConfigurationService.class);
        Mockito.when(scs.getBoolean("lrs.enabled", true)).thenReturn(true);
        Mockito.when(scs.getInt("lrs.max.threadPool", 10)).thenReturn(2);
        return scs;
    }

    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.LearningResourceStoreService")
    public LearningResourceStoreService learningResourceStoreService() {
        return mock(LearningResourceStoreService.class);
    }

    @Bean(name = "org.sakaiproject.scheduling.api.SchedulingService")
    public SchedulingService schedulingService() {
        return mock(SchedulingService.class);
    }
}
