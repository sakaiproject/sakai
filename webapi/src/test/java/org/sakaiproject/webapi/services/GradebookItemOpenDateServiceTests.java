/**
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

package org.sakaiproject.webapi.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

public class GradebookItemOpenDateServiceTests {

    private PublishedAssessmentService publishedAssessmentService = mock(PublishedAssessmentService.class);

    private GradebookItemOpenDateService service = new GradebookItemOpenDateService();

    private Assignment quiz = new Assignment();

    @Before
    public void setup() {

        service.setPublishedAssessmentService(publishedAssessmentService);

        quiz.setExternalId("42");
        quiz.setExternalAppName("sakai.samigo");
    }

    @Test
    public void testOnlyTestsAndQuizzesItemsHaveAnOpenDate() {

        Assignment essay = new Assignment();
        essay.setExternalId("/assignment/a/site1/essay-1");
        essay.setExternalAppName("sakai.assignment.grades");

        assertNull(service.getOpenDate(essay));
        assertNull(service.getOpenDate(new Assignment()));

        verifyNoInteractions(publishedAssessmentService);
    }

    @Test
    public void testOpenDateIsTheDefaultOpenDateOfThePublishedAssessment() {

        Instant openDate = Instant.parse("2026-11-10T08:00:00Z");

        when(publishedAssessmentService.getDefaultOpenDate("42")).thenReturn(openDate);

        assertEquals(openDate, service.getOpenDate(quiz));
    }

    @Test
    public void testNoOpenDateWhenThePublishedAssessmentCannotBeRead() {

        when(publishedAssessmentService.getDefaultOpenDate("42")).thenThrow(new RuntimeException("not found"));

        assertNull(service.getOpenDate(quiz));
    }
}
