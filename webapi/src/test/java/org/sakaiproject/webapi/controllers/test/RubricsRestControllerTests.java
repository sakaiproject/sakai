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
package org.sakaiproject.webapi.controllers.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.rubrics.api.RubricsConflictException;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.EvaluationTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.controllers.RubricsRestController;
import org.sakaiproject.webapi.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RubricsRestControllerTests.TestConfiguration.class)
public class RubricsRestControllerTests {

    @Configuration
    @Import({WebApiTestConfiguration.class, RubricsRestController.class})
    public static class TestConfiguration {
        @Bean
        public RubricsService rubricsService() {
            return mock(RubricsService.class);
        }
    }

    @Autowired private RubricsRestController controller;
    @Autowired private RubricsService rubricsService;
    @Autowired private SessionManager sessionManager;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        reset(rubricsService, sessionManager);
        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("user1");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    public void staleRubricPatchReturnsConflictBeforeApplyingRemovedCriterion() throws Exception {
        RubricTransferBean rubric = new RubricTransferBean();
        rubric.setVersion(1L);
        when(rubricsService.getRubric(1L)).thenReturn(Optional.of(rubric));

        mockMvc.perform(patch("/sites/site1/rubrics/1")
                .header("If-Match", "\"0\"")
                .contentType("application/json-patch+json")
                .content("[{\"op\":\"remove\",\"path\":\"/criteria/0\"}]"))
                .andExpect(status().isConflict());

        verify(rubricsService, never()).saveRubric(any());
    }

    @Test
    public void staleCriterionPatchChecksParentRevisionBeforeApplyingRemovedRating() throws Exception {
        CriterionTransferBean criterion = new CriterionTransferBean();
        criterion.setVersion(0L);
        criterion.setRubricVersion(1L);
        when(rubricsService.getCriterion(1L, "site1")).thenReturn(Optional.of(criterion));

        mockMvc.perform(patch("/sites/site1/rubrics/2/criteria/1")
                .header("If-Match", "\"0\"")
                .contentType("application/json-patch+json")
                .content("[{\"op\":\"remove\",\"path\":\"/ratings/0\"}]"))
                .andExpect(status().isConflict());

        verify(rubricsService, never()).updateCriterion(any(), eq("site1"));
    }

    @Test
    public void staleEvaluationPatchReturnsConflictBeforeApplyingRemovedOutcome() throws Exception {
        EvaluationTransferBean evaluation = new EvaluationTransferBean();
        evaluation.setVersion(1L);
        when(rubricsService.getEvaluation(1L, "site1")).thenReturn(Optional.of(evaluation));

        mockMvc.perform(patch("/sites/site1/rubric-evaluations/1")
                .header("If-Match", "\"0\"")
                .contentType("application/json-patch+json")
                .content("[{\"op\":\"remove\",\"path\":\"/criterionOutcomes/0\"}]"))
                .andExpect(status().isConflict());

        verify(rubricsService, never()).saveEvaluation(any(), eq("site1"));
    }

    @Test
    public void currentRubricPatchSavesWithTheHeaderRevisionAndReturnsJson() throws Exception {
        RubricTransferBean rubric = new RubricTransferBean();
        rubric.setVersion(1L);
        when(rubricsService.getRubric(1L)).thenReturn(Optional.of(rubric));
        when(rubricsService.saveRubric(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/sites/site1/rubrics/1")
                .header("If-Match", "\"1\"")
                .contentType("application/json-patch+json")
                .content("[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Updated\"},"
                        + "{\"op\":\"replace\",\"path\":\"/version\",\"value\":99}]"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    public void aServiceConflictAfterLoadingStillReturnsConflict() throws Exception {
        RubricTransferBean rubric = new RubricTransferBean();
        rubric.setVersion(1L);
        when(rubricsService.getRubric(1L)).thenReturn(Optional.of(rubric));
        when(rubricsService.saveRubric(any())).thenThrow(new RubricsConflictException());

        mockMvc.perform(patch("/sites/site1/rubrics/1")
                .header("If-Match", "\"1\"")
                .contentType("application/json-patch+json")
                .content("[{\"op\":\"replace\",\"path\":\"/title\",\"value\":\"Updated\"}]"))
                .andExpect(status().isConflict());

        verify(rubricsService).saveRubric(any());
    }
}
