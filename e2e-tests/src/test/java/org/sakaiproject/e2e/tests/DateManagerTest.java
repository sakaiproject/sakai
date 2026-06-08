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

        String gradebookResourcesSite = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng", "sakai\\.resources"));
        page.navigate(gradebookResourcesSite);
        openDateManager();

        assertBulkDateFieldVisible("bulk-open-date");
        assertBulkDateFieldVisible("bulk-due-date");
        assertBulkDateFieldHidden("bulk-accept-until");
        assertBulkDateFieldHidden("bulk-feedback-start");
        assertBulkDateFieldHidden("bulk-feedback-end");
        assertBulkDateFieldHidden("bulk-signup-begins");
        assertBulkDateFieldHidden("bulk-signup-deadline");

        String assignmentsSite = sakai.createCourse("instructor1",
            List.of("sakai\\.gradebookng", "sakai\\.resources", "sakai\\.assignment\\.grades"));
        page.navigate(assignmentsSite);
        openDateManager();

        assertBulkDateFieldVisible("bulk-open-date");
        assertBulkDateFieldVisible("bulk-due-date");
        assertBulkDateFieldVisible("bulk-accept-until");
        assertBulkDateFieldHidden("bulk-feedback-start");
        assertBulkDateFieldHidden("bulk-feedback-end");
        assertBulkDateFieldHidden("bulk-signup-begins");
        assertBulkDateFieldHidden("bulk-signup-deadline");

        String gradebookOnlySite = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
        page.navigate(gradebookOnlySite);
        openDateManager();

        assertBulkDateFieldVisible("bulk-due-date");
        assertBulkDateFieldHidden("bulk-open-date");
        assertBulkDateFieldHidden("bulk-accept-until");

        String assessmentsSite = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        page.navigate(assessmentsSite);
        openDateManager();

        assertBulkDateFieldVisible("bulk-open-date");
        assertBulkDateFieldVisible("bulk-due-date");
        assertBulkDateFieldVisible("bulk-accept-until");
        assertBulkDateFieldVisible("bulk-feedback-start");
        assertBulkDateFieldVisible("bulk-feedback-end");
        assertBulkDateFieldHidden("bulk-signup-begins");
        assertBulkDateFieldHidden("bulk-signup-deadline");

        String signupSite = sakai.createCourse("instructor1", List.of("sakai\\.signup"));
        page.navigate(signupSite);
        openDateManager();

        assertBulkDateFieldVisible("bulk-open-date");
        assertBulkDateFieldVisible("bulk-due-date");
        assertBulkDateFieldVisible("bulk-signup-begins");
        assertBulkDateFieldVisible("bulk-signup-deadline");
        assertBulkDateFieldHidden("bulk-accept-until");
        assertBulkDateFieldHidden("bulk-feedback-start");
        assertBulkDateFieldHidden("bulk-feedback-end");

        String lessonsSite = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));
        page.navigate(lessonsSite);
        openDateManager();

        assertBulkDateFieldVisible("bulk-open-date");
        assertBulkDateFieldHidden("bulk-due-date");
        assertBulkDateFieldHidden("bulk-accept-until");

        String dashboardOnlySite = sakai.createCourse("instructor1", List.of("sakai\\.dashboard"));
        page.navigate(dashboardOnlySite);
        openDateManager();

        assertThat(page.locator(".date-manager-setter")).hasCount(0);
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

    private void assertBulkDateFieldVisible(String id) {
        assertThat(page.locator("#" + id)).isVisible();
    }

    private void assertBulkDateFieldHidden(String id) {
        assertThat(page.locator("#" + id)).hasCount(0);
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
