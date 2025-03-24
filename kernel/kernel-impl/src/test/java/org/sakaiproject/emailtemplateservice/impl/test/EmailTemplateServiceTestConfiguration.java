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
package org.sakaiproject.emailtemplateservice.impl.test;

import org.hibernate.SessionFactory;

import org.sakaiproject.emailtemplateservice.api.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.api.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.api.repository.EmailTemplateRepository;
import org.sakaiproject.emailtemplateservice.impl.EmailTemplateServiceImpl;
import org.sakaiproject.emailtemplateservice.impl.repository.EmailTemplateRepositoryImpl;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.sakaiproject.test.SakaiTestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/hibernate.properties")
public class EmailTemplateServiceTestConfiguration extends SakaiTestConfiguration {

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.emailTemplateService")
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {

        Class[] annotatedClasses = new Class[] { EmailTemplate.class };
        AdditionalHibernateMappings mappings = new AdditionalHibernateMappingsImpl();
        mappings.setAnnotatedClasses(annotatedClasses);
        return mappings;
    }

    @Bean(name = "org.sakaiproject.emailtemplateservice.api.repository.EmailTemplateRepository")
    public EmailTemplateRepository repository(SessionFactory sessionFactory) {

        EmailTemplateRepositoryImpl repository = new EmailTemplateRepositoryImpl();
        repository.setSessionFactory(sessionFactory);
        return repository;
    }

    @Bean
    public EmailTemplateService emailTemplateService(EmailTemplateRepository repository) {

        EmailTemplateServiceImpl ets = new EmailTemplateServiceImpl();
        ets.setRepository(repository);
        return ets;
    }
}
