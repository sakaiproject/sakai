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

import jakarta.persistence.EntityManagerFactory;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.attendance.model.*;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.user.api.PreferencesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;

/** Loads Attendance's production beans and mappings, with mocks only at Sakai boundaries. */
@Configuration
@org.springframework.transaction.annotation.EnableTransactionManagement
@ImportResource("classpath:WEB-INF/components.xml")
@PropertySource("classpath:hibernate.properties")
public class AttendanceTestConfiguration extends SakaiTestConfiguration {
    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        AdditionalHibernateMappingsImpl mappings = new AdditionalHibernateMappingsImpl();
        mappings.setAnnotatedClasses(new Class<?>[] { AttendanceSite.class, AttendanceEvent.class,
                AttendanceRecord.class, AttendanceStatus.class, AttendanceGrade.class,
                AttendanceItemStats.class, AttendanceUserStats.class, GradingRule.class });
        return mappings;
    }

    @Bean
    public static org.springframework.beans.factory.config.BeanFactoryPostProcessor entityManagerFactoryAlias() {
        return beans -> beans.registerAlias("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory", "entityManagerFactory");
    }

    @Bean(name = "org.sakaiproject.springframework.orm.jpa.GlobalJpaTransactionManager")
    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
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
