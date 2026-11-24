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
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
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
    public void testOpenDateIsTheStartDate() {

        Instant startDate = Instant.parse("2026-11-10T08:00:00Z");

        PublishedAssessmentFacade assessment = mock(PublishedAssessmentFacade.class);
        when(assessment.getStartDate()).thenReturn(Date.from(startDate));
        when(assessment.getCreatedDate()).thenReturn(Date.from(Instant.parse("2026-11-01T09:00:00Z")));
        when(publishedAssessmentService.getPublishedAssessmentQuick("42")).thenReturn(assessment);

        assertEquals(startDate, service.getOpenDate(quiz));
    }

    @Test
    public void testOpenDateIsTheCreationDateWithoutAStartDate() {

        Instant createdDate = Instant.parse("2026-11-01T09:00:00Z");

        PublishedAssessmentFacade assessment = mock(PublishedAssessmentFacade.class);
        when(assessment.getCreatedDate()).thenReturn(Date.from(createdDate));
        when(publishedAssessmentService.getPublishedAssessmentQuick("42")).thenReturn(assessment);

        assertEquals(createdDate, service.getOpenDate(quiz));
    }

    @Test
    public void testNoOpenDateWhenThePublishedAssessmentCannotBeRead() {

        when(publishedAssessmentService.getPublishedAssessmentQuick("42")).thenThrow(new RuntimeException("not found"));

        assertNull(service.getOpenDate(quiz));
    }
}
