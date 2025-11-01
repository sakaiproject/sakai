package org.sakaiproject.tool.assessment.services.assessment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;

public class AssessmentEntityProducerTest {

    private AssessmentService assessmentService;
    private PublishedAssessmentService publishedAssessmentService;
    private AssessmentEntityProducer producer;

    @Before
    public void setUp() {
        assessmentService = mock(AssessmentService.class);
        publishedAssessmentService = mock(PublishedAssessmentService.class);

        producer = new AssessmentEntityProducer() {
            @Override
            protected AssessmentService createAssessmentService() {
                return assessmentService;
            }

            @Override
            protected PublishedAssessmentService createPublishedAssessmentService() {
                return publishedAssessmentService;
            }
        };
    }

    @Test
    public void hardDeleteRemovesDraftAndPublishedAssessments() {
        AssessmentData draft = mock(AssessmentData.class);
        when(draft.getAssessmentId()).thenReturn(Long.valueOf(10L));
        when(assessmentService.getAllActiveAssessmentsbyAgent("site"))
                .thenReturn(Collections.singletonList(draft));

        PublishedAssessmentData published = mock(PublishedAssessmentData.class);
        when(published.getPublishedAssessmentId()).thenReturn(Long.valueOf(20L));
        when(publishedAssessmentService.getAllPublishedAssessmentsForSite("site"))
                .thenReturn(Collections.singletonList(published));

        producer.hardDelete("site");

        verify(assessmentService).removeAssessment("10");
        verify(publishedAssessmentService).removeAssessment("20");
    }

    @Test
    public void hardDeleteSkipsWhenServicesReturnNull() {
        when(assessmentService.getAllActiveAssessmentsbyAgent("site")).thenReturn(null);
        when(publishedAssessmentService.getAllPublishedAssessmentsForSite("site")).thenReturn(null);

        producer.hardDelete("site");

        verify(assessmentService, never()).removeAssessment(anyString());
        verify(publishedAssessmentService, never()).removeAssessment(anyString());
    }
}

