/*
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
package org.sakaiproject.tool.assessment.ui.bean.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.Test;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService.TotalScoreCancellationException;

public class QuestionCancellationEligibilityTest {
    @Test
    public void allowedIsCachedOnceForRepeatedGetters() {
        assertCachedOnce(true, "");
    }

    @Test
    public void restrictedIsCachedOnceForRepeatedGetters() {
        assertCachedOnce(false, "cancel_question_reduce_total_category_restricted");
    }

    @Test
    public void unavailableIsCachedOnceForRepeatedGetters() {
        Map<String, Object> request = new HashMap<>();
        AtomicInteger lookups = new AtomicInteger();
        BooleanSupplier lookup = () -> {
            lookups.incrementAndGet();
            throw new TotalScoreCancellationException(false, "Gradebook unavailable", null);
        };
        assertEquals("cancel_question_reduce_total_unavailable", QuestionCancellationEligibility.restrictionKey(request, "site", 1L, lookup));
        assertEquals("cancel_question_reduce_total_unavailable", QuestionCancellationEligibility.restrictionKey(request, "site", 1L, lookup));
        assertEquals(1, lookups.get());
        assertEquals("cancel_question_reduce_total_unavailable",
            QuestionCancellationEligibility.restrictionKey(new HashMap<>(), "site", 1L, lookup));
        assertEquals(2, lookups.get());
    }

    @Test
    public void newRequestSeesChangedEligibility() {
        Map<String, Object> firstRequest = new HashMap<>();
        AtomicBoolean allowed = new AtomicBoolean(true);
        AtomicInteger lookups = new AtomicInteger();
        BooleanSupplier lookup = () -> {
            lookups.incrementAndGet();
            return allowed.get();
        };
        assertEquals("", QuestionCancellationEligibility.restrictionKey(firstRequest, "site", 1L, lookup));
        allowed.set(false);
        assertEquals("", QuestionCancellationEligibility.restrictionKey(firstRequest, "site", 1L, lookup));
        assertEquals("cancel_question_reduce_total_category_restricted", QuestionCancellationEligibility.restrictionKey(new HashMap<>(), "site", 1L, lookup));
        assertEquals(2, lookups.get());
    }

    @Test
    public void assessmentsAndSitesHaveSeparateEntriesInOneRequest() {
        Map<String, Object> request = new HashMap<>();
        AtomicInteger lookups = new AtomicInteger();
        BooleanSupplier allowed = () -> {
            lookups.incrementAndGet();
            return true;
        };
        BooleanSupplier restricted = () -> {
            lookups.incrementAndGet();
            return false;
        };
        assertEquals("", QuestionCancellationEligibility.restrictionKey(request, "first", 1L, allowed));
        assertEquals("cancel_question_reduce_total_category_restricted", QuestionCancellationEligibility.restrictionKey(request, "first", 2L, restricted));
        assertEquals("cancel_question_reduce_total_category_restricted", QuestionCancellationEligibility.restrictionKey(request, "second", 1L, restricted));
        assertEquals("", QuestionCancellationEligibility.restrictionKey(request, "first", 1L, restricted));
        assertEquals("cancel_question_reduce_total_category_restricted", QuestionCancellationEligibility.restrictionKey(request, "first", 2L, allowed));
        assertEquals("cancel_question_reduce_total_category_restricted", QuestionCancellationEligibility.restrictionKey(request, "second", 1L, allowed));
        assertEquals(3, lookups.get());
    }

    private void assertCachedOnce(boolean allowed, String expected) {
        Map<String, Object> request = new HashMap<>();
        AtomicInteger lookups = new AtomicInteger();
        BooleanSupplier lookup = () -> {
            lookups.incrementAndGet();
            return allowed;
        };
        assertEquals(expected, QuestionCancellationEligibility.restrictionKey(request, "site", 1L, lookup));
        assertEquals(expected, QuestionCancellationEligibility.restrictionKey(request, "site", 1L, lookup));
        assertEquals(1, lookups.get());
    }

}
