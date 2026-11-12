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
package org.sakaiproject.tool.assessment.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.authz.AuthzQueriesFacade;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SamigoCancellationTestConfiguration.class)
public class SamigoImportTestConfiguration {
    @Bean
    public AssessmentFacadeQueriesAPI assessmentImportService(
            @Qualifier("assessmentFacadeQueries") AssessmentFacadeQueries queries,
            AuthzQueriesFacade authorizations, RubricsService rubricsService, UserDirectoryService users) {
        User instructor = mock(User.class);
        when(instructor.getId()).thenReturn("instructor");
        when(users.getCurrentUser()).thenReturn(instructor);
        queries.setAuthzQueriesFacade(authorizations);
        queries.setRubricsService(rubricsService);
        return queries;
    }
}
