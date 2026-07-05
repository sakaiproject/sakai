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
package org.sakaiproject.tool.assessment.services;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * SAK-52267 the single formula behind every finalScore write:
 * finalScore = max(0, totalAutoScore + totalOverrideScore - latePenalty),
 * with the penalty waived when ignoreLatePenalty is TRUE. Replaces the
 * inline (auto + override) &lt; 0 ? 0 : auto + override computations.
 */
public class GradingServiceFinalScoreTest {

    private static final double DELTA = 0.0001;

    private GradingService gradingService;

    @Before
    public void setUp() {
        gradingService = new GradingService();
    }

    @Test
    public void noPenaltyKeepsLegacyBehavior() {
        assertEquals(10.0, gradingService.computeFinalScore(8.0, 2.0, 0.0, null), DELTA);
        // negative adjustments still floor at zero, as before
        assertEquals(4.0, gradingService.computeFinalScore(8.0, -4.0, 0.0, null), DELTA);
        assertEquals(0.0, gradingService.computeFinalScore(2.0, -4.0, 0.0, null), DELTA);
    }

    @Test
    public void penaltyReducesFinalScore() {
        assertEquals(5.0, gradingService.computeFinalScore(8.0, 2.0, 5.0, null), DELTA);
        assertEquals(5.0, gradingService.computeFinalScore(8.0, 2.0, 5.0, Boolean.FALSE), DELTA);
    }

    @Test
    public void penaltyFloorsAtZero() {
        assertEquals(0.0, gradingService.computeFinalScore(3.0, 0.0, 50.0, null), DELTA);
    }

    @Test
    public void ignoreWaivesPenalty() {
        assertEquals(10.0, gradingService.computeFinalScore(8.0, 2.0, 5.0, Boolean.TRUE), DELTA);
    }
}
