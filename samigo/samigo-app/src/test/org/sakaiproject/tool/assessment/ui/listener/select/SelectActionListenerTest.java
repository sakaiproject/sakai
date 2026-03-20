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
package org.sakaiproject.tool.assessment.ui.listener.select;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBeanie;

public class SelectActionListenerTest {

    @Test
    public void getSecureDeliveryModuleIdsByAssessmentBatchesVisibleAssessmentIds() {
        SelectActionListener listener = new SelectActionListener();
        PublishedAssessmentService publishedAssessmentService = mock(PublishedAssessmentService.class);
        Map<Long, String> secureDeliveryModuleIdsByAssessment = new HashMap<>();
        secureDeliveryModuleIdsByAssessment.put(101L, "module-a");

        when(publishedAssessmentService.getAssessmentMetaDataEntriesByLabel(anyList(), eq(SecureDeliveryServiceAPI.MODULE_KEY)))
                .thenReturn(secureDeliveryModuleIdsByAssessment);

        Map<Long, String> result = listener.getSecureDeliveryModuleIdsByAssessment(
                Arrays.asList(deliveryBeanie("101"), deliveryBeanie("invalid"), deliveryBeanie("202")),
                publishedAssessmentService);

        assertEquals(secureDeliveryModuleIdsByAssessment, result);
        verify(publishedAssessmentService).getAssessmentMetaDataEntriesByLabel(
                Arrays.asList(101L, 202L), SecureDeliveryServiceAPI.MODULE_KEY);
    }

    @Test
    public void applySecureDeliveryUrlsOnlyCallsModulesPresentOnThePage() {
        SelectActionListener listener = new SelectActionListener();
        SecureDeliveryServiceAPI secureDelivery = mock(SecureDeliveryServiceAPI.class);
        List<DeliveryBeanie> takeablePublishedList = Arrays.asList(
                deliveryBeanie("101"),
                deliveryBeanie("102"),
                deliveryBeanie("103"),
                deliveryBeanie("invalid"));
        Map<Long, String> secureDeliveryModuleIdsByAssessment = new HashMap<>();
        secureDeliveryModuleIdsByAssessment.put(102L, SecureDeliveryServiceAPI.NONE_ID);
        secureDeliveryModuleIdsByAssessment.put(103L, "module-c");

        when(secureDelivery.getAlternativeDeliveryUrl("module-c", 103L, "student-1"))
                .thenReturn(Optional.of("https://example.test/launch"));

        listener.applySecureDeliveryUrls(
                takeablePublishedList, secureDeliveryModuleIdsByAssessment, secureDelivery, "student-1");

        assertEquals("", takeablePublishedList.get(0).getAlternativeDeliveryUrl());
        assertEquals("", takeablePublishedList.get(1).getAlternativeDeliveryUrl());
        assertEquals("https://example.test/launch", takeablePublishedList.get(2).getAlternativeDeliveryUrl());
        assertEquals("", takeablePublishedList.get(3).getAlternativeDeliveryUrl());
        verify(secureDelivery).getAlternativeDeliveryUrl("module-c", 103L, "student-1");
        verifyNoMoreInteractions(secureDelivery);
    }

    private DeliveryBeanie deliveryBeanie(String assessmentId) {
        DeliveryBeanie deliveryBeanie = new DeliveryBeanie();
        deliveryBeanie.setAssessmentId(assessmentId);
        return deliveryBeanie;
    }
}
