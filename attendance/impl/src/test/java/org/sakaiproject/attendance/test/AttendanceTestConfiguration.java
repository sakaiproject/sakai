/*
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.attendance.test;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.springframework.orm.hibernate.HibernateSessionFactoryBean;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/** Loads Attendance's production beans and mappings, with mocks only at Sakai boundaries. */
@Configuration
@org.springframework.transaction.annotation.EnableTransactionManagement
@ImportResource("classpath:WEB-INF/components.xml")
@PropertySource("classpath:hibernate.properties")
public class AttendanceTestConfiguration extends SakaiTestConfiguration {
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource());
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setEntityManagerFactoryInterface(EntityManagerFactory.class);
        factory.setJpaProperties(hibernateProperties());
        factory.setPackagesToScan("org.sakaiproject.attendance.model");
        return factory;
    }

    // Match production: JPA bootstrap plus the native Hibernate factory exposed to legacy callers.
    @Override
    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    public SessionFactory sessionFactory() throws IOException {
        HibernateSessionFactoryBean factory = new HibernateSessionFactoryBean();
        factory.setEntityManagerFactory(entityManagerFactory().getObject());
        return factory.getObject();
    }

    @Bean(name = "org.sakaiproject.util.api.LocaleService")
    public org.sakaiproject.util.api.LocaleService localeService() {
        return mock(org.sakaiproject.util.api.LocaleService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public org.sakaiproject.time.api.UserTimeService userTimeService() {
        return mock(org.sakaiproject.time.api.UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return mock(PreferencesService.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradingService() {
        return mock(GradingService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return mock(UsageSessionService.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return mock(SchedulerManager.class);
    }
}
