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
package org.sakaiproject.tool.assessment.facade;

import org.hibernate.SessionFactory;
import org.mockito.Mockito;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.PersistenceHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/** Real Samigo persistence wiring, with mocks only at external service boundaries. */
@Configuration
@ImportResource("classpath:/spring-samigo-query-integration.xml")
public class SamigoPersistenceTestConfiguration {
    @Bean(name = "org.sakaiproject.rubrics.api.RubricsService")
    public RubricsService rubricsService() { return Mockito.mock(RubricsService.class); }

    @Bean
    public SiteService siteService() { return Mockito.mock(SiteService.class); }

    @Bean
    public UserDirectoryService userDirectoryService() { return Mockito.mock(UserDirectoryService.class); }

    @Bean
    public PersistenceService persistenceService(AuthzQueriesFacadeAPI authzQueriesFacade,
            PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries,
            AutoSubmitFacadeQueriesAPI autoSubmitFacadeQueries, PersistenceHelper persistenceHelper) {
        PersistenceService service = new PersistenceService();
        service.setAuthzQueriesFacade(authzQueriesFacade);
        service.setPublishedAssessmentFacadeQueries(publishedAssessmentFacadeQueries);
        service.setAutoSubmitFacadeQueries(autoSubmitFacadeQueries);
        service.setPersistenceHelper(persistenceHelper);
        return service;
    }

    @Bean
    public AutoSubmitFacadeQueries autoSubmitFacadeQueries(SessionFactory sessionFactory) {
        AutoSubmitFacadeQueries queries = new AutoSubmitFacadeQueries();
        queries.setSessionFactory(sessionFactory);
        return queries;
    }
}
