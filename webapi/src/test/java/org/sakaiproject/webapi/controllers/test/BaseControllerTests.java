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
package org.sakaiproject.webapi.controllers.test;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.UriConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.AbstractSakaiApiController;
import org.sakaiproject.webapi.exception.GlobalExceptionHandler;
import org.sakaiproject.webapi.exception.MissingSessionException;

import static org.mockito.Mockito.*;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;

public abstract class BaseControllerTests {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    protected UriConfigurer configurer;

    protected MockMvc mockMvc;

    @Autowired
    protected SessionManager sessionManager;

    public BaseControllerTests() {

        configurer = documentationConfiguration(this.restDocumentation)
            .operationPreprocessors()
            .withRequestDefaults(prettyPrint())
            .withResponseDefaults(prettyPrint())
            .and()
            .uris()
            .withHost("your-sakai.edu")
            .withPort(80);
    }

    protected void buildMockMvc(AbstractSakaiApiController controller) {

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
          .setControllerAdvice(new GlobalExceptionHandler())
          .apply(configurer)
          .build();
    }

    protected void testMissingSession(String path) throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn(null);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        mockMvc.perform(get(path))
            .andExpect(status().is(403))
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingSessionException));
    }
}
