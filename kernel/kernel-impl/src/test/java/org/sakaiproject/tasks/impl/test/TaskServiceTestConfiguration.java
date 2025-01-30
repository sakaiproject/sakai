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
package org.sakaiproject.tasks.impl.test;

import static org.mockito.Mockito.mock;

import org.hibernate.SessionFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.repository.TaskAssignedRepository;
import org.sakaiproject.tasks.api.repository.TaskRepository;
import org.sakaiproject.tasks.api.repository.UserTaskRepository;
import org.sakaiproject.tasks.impl.TaskServiceImpl;
import org.sakaiproject.tasks.impl.repository.TaskAssignedRepositoryImpl;
import org.sakaiproject.tasks.impl.repository.TaskRepositoryImpl;
import org.sakaiproject.tasks.impl.repository.UserTaskRepositoryImpl;
import org.sakaiproject.test.SakaiTestConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.Getter;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/tasks-components.xml")
@PropertySource("classpath:/hibernate.properties")
public class TaskServiceTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.taskservice")
    @Getter
    private AdditionalHibernateMappings additionalHibernateMappings;

    @Bean
    public AuthzGroupService authzGroupService() {
        return mock(AuthzGroupService.class);
    }

    @Bean
    public EntityManager entityManager() {
        return mock(EntityManager.class);
    }

    @Bean
    public EventTrackingService eventTrackingService() {
        return mock(EventTrackingService.class);
    }

    @Bean
    public FunctionManager functionManager() {
        return mock(FunctionManager.class);
    }

    @Bean
    public TaskService taskService() {
        return new TaskServiceImpl();
    }

    @Bean
    public TaskRepository taskRepository(SessionFactory sessionFactory) {

        TaskRepositoryImpl tr = new TaskRepositoryImpl();
        tr.setSessionFactory(sessionFactory);
        return tr;
    }

    @Bean
    public UserTaskRepository userTaskRepository(SessionFactory sessionFactory) {

        UserTaskRepositoryImpl utr = new UserTaskRepositoryImpl();
        utr.setSessionFactory(sessionFactory);
        return utr;
    } 
    
    @Bean
    public TaskAssignedRepository taskAssignedRepository(SessionFactory sessionFactory) {

        TaskAssignedRepositoryImpl tar = new TaskAssignedRepositoryImpl();
        tar.setSessionFactory(sessionFactory);
        return tar;
    }
}
