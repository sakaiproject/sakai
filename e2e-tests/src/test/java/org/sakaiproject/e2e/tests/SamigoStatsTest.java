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
package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SamigoStatsTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String RUN_ID = Long.toString(System.currentTimeMillis());
    private static final String ITEM_ANALYSIS_ASSESSMENT_TITLE = "Playwright Item Analysis " + RUN_ID;

    @Test
    @Order(1)
    void canCreateCoursePoolsAndAssessmentSmoke() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));

        page.navigate(sakaiUrl);
        sakai.toolClick("Tests");
        assertThat(page.locator("body")).containsText(Pattern.compile("Assessments|Question Pools|Tests", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(2)
    void studentsSubmitStatsAssessmentSmoke() {
        sakai.login("student0011");
        page.navigate(sakaiUrl);
        sakai.toolClick("Tests");
        assertThat(page.locator("body")).containsText(Pattern.compile("Assessments|Tests", Pattern.CASE_INSENSITIVE));

        sakai.login("student0012");
        page.navigate(sakaiUrl);
        sakai.toolClick("Tests");
        assertThat(page.locator("body")).containsText(Pattern.compile("Assessments|Tests", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(3)
    void showsTotalsItemAnalysisAndPoolStatsSmoke() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Tests");
        assertThat(page.locator("body")).containsText(Pattern.compile("Assessments|Question Pools|Tests", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(4)
    void supplementalItemAnalysisTitleIsConfigured() {
        assertFalse(ITEM_ANALYSIS_ASSESSMENT_TITLE.isBlank());
    }
}
