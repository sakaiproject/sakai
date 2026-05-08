/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.entitybroker.test;

import org.mockito.Mockito;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.LearningResourceStoreService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.mockito.Mockito.mock;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class EntityBrokerTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings.entitybroker")
    private AdditionalHibernateMappings additionalHibernateMappings;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return additionalHibernateMappings;
    }

    @Bean(name = "org.sakaiproject.email.api.EmailService")
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }

    @Bean(name = "org.sakaiproject.event.api.LearningResourceStoreService")
    public LearningResourceStoreService learningResourceStoreService() {
        return Mockito.mock(LearningResourceStoreService.class);

    }
    @Bean(name = "org.sakaiproject.thread_local.api.ThreadLocalManager")
    public ThreadLocalManager threadLocalManager() {
        return mock(ThreadLocalManager.class);
    }

    @Bean(name = "org.sakaiproject.event.api.UsageSessionService")
    public UsageSessionService usageSessionService() {
        return Mockito.mock(UsageSessionService.class);
    }

}
