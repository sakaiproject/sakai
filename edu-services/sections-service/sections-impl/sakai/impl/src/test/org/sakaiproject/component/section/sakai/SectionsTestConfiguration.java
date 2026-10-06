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
package org.sakaiproject.component.section.sakai;

import static org.mockito.Mockito.mock;

import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class SectionsTestConfiguration extends SakaiTestConfiguration {

    @Bean(name = "org.sakaiproject.coursemanagement.api.CourseManagementService")
    public CourseManagementService courseManagementService() {
        return mock(CourseManagementService.class);
    }

    @Bean(name = "org.sakaiproject.thread_local.api.ThreadLocalManager")
    public ThreadLocalManager threadLocalManager() {
        return mock(ThreadLocalManager.class);
    }

    @Bean(name = "org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return mock(SchedulerManager.class);
    }
}
