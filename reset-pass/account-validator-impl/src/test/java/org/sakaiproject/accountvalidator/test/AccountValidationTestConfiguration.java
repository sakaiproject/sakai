/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.accountvalidator.test;

import org.mockito.Mockito;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.UUID;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class AccountValidationTestConfiguration extends SakaiTestConfiguration {


    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings.accountValidation")
    private AdditionalHibernateMappings additionalHibernateMappings;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        return additionalHibernateMappings;
    }

    @Bean(name="org.sakaiproject.entitybroker.DeveloperHelperService")
    public DeveloperHelperService developerHelperService() {
        return Mockito.mock(DeveloperHelperService.class);
    }

    @Bean(name="org.sakaiproject.emailtemplateservice.api.EmailTemplateService")
    public EmailTemplateService emailTemplateService() {
        return Mockito.mock(EmailTemplateService.class);
    }

    @Bean(name="org.sakaiproject.id.api.IdManager")
    public IdManager idManager() {
        return Mockito.mock(IdManager.class);
    }

    @Bean(name="org.sakaiproject.authz.api.GroupProvider")
    public GroupProvider groupProvider() {
        return Mockito.mock(GroupProvider.class);
    }

    @Bean(name="org.sakaiproject.api.app.scheduler.SchedulerManager")
    public SchedulerManager schedulerManager() {
        return Mockito.mock(SchedulerManager.class);
    }

    @Bean(name="org.sakaiproject.user.api.PreferencesService")
    public PreferencesService preferencesService() {
        return Mockito.mock(PreferencesService.class);
    }

    @Bean
    public ResourceLoader resourceLoader() {
        return Mockito.mock(ResourceLoader.class);
    }
}
