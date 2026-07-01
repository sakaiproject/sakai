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

import com.microsoft.playwright.Locator;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class DateManagerTest extends SakaiUiTestBase {

    @Test
    void bulkSetterOnlyShowsFieldsForSiteTools() {
        sakai.login("instructor1");

        // The per-column date setters live in each tool section's column headers, so a setter is rendered
        // only when the site has that tool (and that tool has that date column). Each setter's id is
        // bulkcol-<toolId>-<field>. Sections are collapsed by default (progressive disclosure), so we
        // assert presence in the DOM rather than visibility.
        String gradebookResourcesSite = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng", "sakai\\.resources"));
        page.navigate(gradebookResourcesSite);
        openDateManager();

        assertColumnSetterPresent("gradebookItems", "due_date");
        assertColumnSetterPresent("resources", "open_date");
        assertColumnSetterPresent("resources", "due_date");
        assertColumnSetterAbsent("assignments", "open_date");
        assertColumnSetterAbsent("assignments", "accept_until");
        assertColumnSetterAbsent("assessments", "feedback_start");
        assertColumnSetterAbsent("signupMeetings", "signup_begins");

        String assignmentsSite = sakai.createCourse("instructor1",
            List.of("sakai\\.gradebookng", "sakai\\.resources", "sakai\\.assignment\\.grades"));
        page.navigate(assignmentsSite);
        openDateManager();

        assertColumnSetterPresent("assignments", "open_date");
        assertColumnSetterPresent("assignments", "due_date");
        assertColumnSetterPresent("assignments", "accept_until");
        assertColumnSetterAbsent("assessments", "feedback_start");
        assertColumnSetterAbsent("signupMeetings", "signup_begins");

        String gradebookOnlySite = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
        page.navigate(gradebookOnlySite);
        openDateManager();

        assertColumnSetterPresent("gradebookItems", "due_date");
        assertColumnSetterAbsent("gradebookItems", "open_date");
        assertColumnSetterAbsent("assignments", "open_date");

        String assessmentsSite = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        page.navigate(assessmentsSite);
        openDateManager();

        assertColumnSetterPresent("assessments", "open_date");
        assertColumnSetterPresent("assessments", "due_date");
        assertColumnSetterPresent("assessments", "accept_until");
        assertColumnSetterPresent("assessments", "feedback_start");
        assertColumnSetterPresent("assessments", "feedback_end");
        assertColumnSetterAbsent("signupMeetings", "signup_begins");

        String signupSite = sakai.createCourse("instructor1", List.of("sakai\\.signup"));
        page.navigate(signupSite);
        openDateManager();

        assertColumnSetterPresent("signupMeetings", "open_date");
        assertColumnSetterPresent("signupMeetings", "due_date");
        assertColumnSetterPresent("signupMeetings", "signup_begins");
        assertColumnSetterPresent("signupMeetings", "signup_deadline");
        assertColumnSetterAbsent("assignments", "accept_until");
        assertColumnSetterAbsent("assessments", "feedback_start");

        String lessonsSite = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));
        page.navigate(lessonsSite);
        openDateManager();

        assertColumnSetterPresent("lessons", "open_date");
        assertColumnSetterAbsent("lessons", "due_date");
        assertColumnSetterAbsent("assignments", "open_date");

        String dashboardOnlySite = sakai.createCourse("instructor1", List.of("sakai\\.dashboard"));
        page.navigate(dashboardOnlySite);
        openDateManager();

        // No dated tools -> no per-column setters at all.
        assertThat(page.locator(".bulk-col-setter")).hasCount(0);
    }

    private void openDateManager() {
        sakai.toolClick("Site Info");
        Locator dateManager = page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Date Manager$", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(dateManager).isVisible();
        dateManager.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        assertNoTemplateRenderingError();
    }

    private void assertColumnSetterPresent(String toolId, String field) {
        assertThat(page.locator("#bulkcol-" + toolId + "-" + field)).hasCount(1);
    }

    private void assertColumnSetterAbsent(String toolId, String field) {
        assertThat(page.locator("#bulkcol-" + toolId + "-" + field)).hasCount(0);
    }

    private void assertNoTemplateRenderingError() {
        String bodyText = page.locator("body").textContent();
        Pattern templateError = Pattern.compile(
            "TemplateProcessingException|TemplateOutputException|An error happened during template rendering",
            Pattern.CASE_INSENSITIVE
        );
        assertFalse(templateError.matcher(bodyText == null ? "" : bodyText).find(), "Date Manager rendered a Thymeleaf error");
    }
}
