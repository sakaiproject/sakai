/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://opensource.org/licenses/ecl2
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.webapi.controllers.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.RubricsRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RubricsRestControllerTest.TestConfiguration.class)
public class RubricsRestControllerTest {

    @Configuration
    @Import({WebApiTestConfiguration.class, RubricsRestController.class})
    public static class TestConfiguration {
        @Bean
        public RubricsService rubricsService() {
            return mock(RubricsService.class);
        }

        @Bean
        public Session session() {
            return mock(Session.class);
        }
    }

    @Autowired private RubricsRestController controller;
    @Autowired private RubricsService rubricsService;
    @Autowired private SessionManager sessionManager;
    @Autowired private Session session;

    @Test
    public void pdfDownloadEncodesFilenamesWithoutLosingUnicode() throws Exception {
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getUserId()).thenReturn("instructor");
        RubricTransferBean rubric = new RubricTransferBean();
        byte[] pdf = {1, 2, 3};
        when(rubricsService.getRubric(1L)).thenReturn(Optional.of(rubric));
        when(rubricsService.createPdf("site", 1L, "tool", "item", "student")).thenReturn(pdf);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        for (String filename : new String[] {"New Rubric_Horner, Şaban", "Rubric_Horner, Sean \"Şaban\"", "Rubric_Student"}) {
            when(rubricsService.createContextualFilename(rubric, "tool", "item", "student", "site"))
                    .thenReturn(filename);
            String header = mvc.perform(get("/sites/site/rubrics/1/pdf")
                    .param("toolId", "tool").param("itemId", "item").param("evaluatedItemId", "student"))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(pdf))
                    .andReturn().getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);

            assertTrue("Header must be ASCII-safe: " + header, header.chars().allMatch(c -> c < 128));
            ContentDisposition disposition = ContentDisposition.parse(header);
            assertEquals("attachment", disposition.getType());
            assertEquals(filename + ".pdf", disposition.getFilename());
        }
    }
}
