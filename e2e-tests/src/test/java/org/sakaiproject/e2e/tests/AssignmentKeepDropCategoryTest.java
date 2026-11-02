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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class AssignmentKeepDropCategoryTest extends SakaiUiTestBase {
    private static final String FIRST = "SAK-52328 First assignment";
    private static final String SECOND = "SAK-52328 Second assignment";
    private static final String ALERT = "Points must match assignments in the selected Gradebook category (10.0)";

    @ParameterizedTest
    @CsvSource({"Categories only, keepHighest, keephighest", "Categories & weighting, dropLowest, droplowest"})
    void mismatchedPointsAreBlockedBeforeCreatingOrUpdatingAssignment(String categoryMode, String rule, String column) {
        sakai.login("instructor1");
        String siteUrl = sakai.createSiteThroughUi("instructor1", List.of("sakai\\.assignment\\.grades", "sakai\\.gradebookng"), "course");
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        configureCategory(categoryMode, rule, column);

        openAssignments(siteUrl);
        newAssignment(FIRST, "10");
        releaseAssignment();
        assertThat(page.locator("#new_assignment_title")).hasCount(0);

        newAssignment(SECOND, "20");
        releaseAssignment();
        assertThat(page.locator("body")).containsText(ALERT);
        assertThat(page.locator("#new_assignment_title")).hasValue(SECOND);
        page.locator("#new_assignment_grade_points").fill("10");
        releaseAssignment();
        assertThat(page.locator("#new_assignment_title")).hasCount(0);

        editAssignment(FIRST);
        page.locator("#new_assignment_grade_points").fill("20");
        releaseAssignment();
        assertThat(page.locator("body")).containsText(ALERT);
        assertThat(page.locator("#new_assignment_title")).hasValue(FIRST);

        // Leave the rejected edit and reload persisted assignment and Gradebook data.
        openAssignments(siteUrl);
        editAssignment(FIRST);
        assertThat(page.locator("#new_assignment_grade_points")).hasValue(Pattern.compile("10(?:\\.0+)?"));
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        for (String title : List.of(FIRST, SECOND)) {
            Locator header = page.locator("#gradeTable .relative").filter(new Locator.FilterOptions()
                    .setHas(page.locator("a.gb-title").filter(new Locator.FilterOptions().setHasText(title))));
            assertThat(header.locator(".gb-total-points")).hasText(Pattern.compile("\\s*10(?:\\.0+)?\\s*"));
        }
    }

    private void configureCategory(String categoryMode, String rule, String column) {
        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Settings")).first().click();
        Locator accordion = page.locator(".accordion button").filter(new Locator.FilterOptions().setHasText("Categories")).first();
        if (!"true".equals(accordion.getAttribute("aria-expanded"))) {
            ElementHandle panel = page.locator("#settingsCategories").elementHandle();
            accordion.click();
            page.waitForFunction("panel => !panel.isConnected", panel);
            panel.dispose();
        }
        page.getByLabel(categoryMode, new Page.GetByLabelOptions().setExact(true)).check();
        ElementHandle row = page.locator(".gb-category-row").first().elementHandle();
        page.locator("#settingsCategories .gb-inline-checkbox input[name$='" + rule + "']").check();
        page.waitForFunction("row => !row.isConnected", row);
        row.dispose();
        Locator category = page.locator(".gb-category-row").first();
        category.locator("input[name$='name']").fill("Assignments");
        category.locator(".gb-category-" + column + " input").fill("1");
        if (categoryMode.contains("weighting")) {
            category.locator(".gb-category-weight input").fill("100");
        }
        page.locator(".act button.active").first().click();
        assertThat(page.getByText("The settings were successfully updated", new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    private void openAssignments(String siteUrl) {
        page.navigate(siteUrl);
        sakai.toolClick("Assignments");
    }

    private void newAssignment(String title, String points) {
        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^(Add|New)$"))).click();
        page.locator("#new_assignment_title").fill(title);
        page.locator("#gradeAssignment").check();
        page.locator("#new_assignment_grade_points").fill(points);
        page.locator("#new_assignment_send_to_gradebook").check();
        page.locator("#category").selectOption(new SelectOption().setLabel("Assignments"));
        sakai.typeCkEditor("new_assignment_instructions", "<p>Keep/Drop category validation regression.</p>");
    }

    private void editAssignment(String title) {
        page.locator("tr").filter(new Locator.FilterOptions().setHasText(title))
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(Pattern.compile("^Edit\\b"))).click();
        assertThat(page.locator("#new_assignment_title")).hasValue(title);
    }

    private void releaseAssignment() {
        page.locator(".act input[name='post']").click();
    }
}
