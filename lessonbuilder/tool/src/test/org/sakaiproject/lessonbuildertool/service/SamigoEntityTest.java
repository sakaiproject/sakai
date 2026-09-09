/**
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
package org.sakaiproject.lessonbuildertool.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;

public class SamigoEntityTest {

    private PublishedAssessmentFacadeQueriesAPI originalQueries;
    private PublishedAssessmentFacadeQueriesAPI queries;
    private SamigoEntity entity;

    @Before
    public void setUp() {
        originalQueries = SamigoEntity.publishedAssessmentFacadeQueries;
        entity = new SamigoEntity(LessonEntity.TYPE_SAMIGO, 42L, 1);
        queries = mock(PublishedAssessmentFacadeQueriesAPI.class);
        entity.setPublishedAssessmentFacadeQueries(queries);
        entity.assessment = new PublishedAssessmentData();
        entity.assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
    }

    @After
    public void tearDown() {
        entity.setPublishedAssessmentFacadeQueries(originalQueries);
    }

    @Test
    public void detectsDeletionAfterTheAssessmentHasBeenLoaded() {
        when(queries.getPublishedAssessmentStatus(42L))
                .thenReturn(AssessmentBaseIfc.ACTIVE_STATUS, AssessmentBaseIfc.DEAD_STATUS);

        assertTrue(entity.objectExists());
        assertFalse(entity.objectExists());
    }

    @Test
    public void inactiveAndRetractedAssessmentsStillExist() {
        when(queries.getPublishedAssessmentStatus(42L))
                .thenReturn(AssessmentBaseIfc.INACTIVE_STATUS, AssessmentBaseIfc.RETRACT_FOR_EDIT_STATUS);

        assertTrue(entity.objectExists());
        assertTrue(entity.objectExists());
    }
}
