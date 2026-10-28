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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.CheckedNode;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GradebookTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng", "sakai\\.rubrics"));
    }

    @Test
    @Order(2)
    void canCreateGradebookCategories() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Settings")).first().click();
        ElementHandle categoriesPanel = page.locator("#settingsCategories").elementHandle();
        page.locator(".accordion button").filter(new Locator.FilterOptions().setHasText("Categories")).first().click();
        // Bootstrap reveals the old panel before Wicket replaces it via AJAX.
        page.waitForFunction("panel => !panel.isConnected", categoriesPanel);
        categoriesPanel.dispose();

        assertThat(page.locator("#settingsCategories input[type=\"radio\"]:visible")).hasCount(3);

        Locator weightedCategoryOption = page.getByLabel("Categories & weighting", new Page.GetByLabelOptions().setExact(true));
        assertThat(weightedCategoryOption).isVisible();
        weightedCategoryOption.check();
        assertThat(weightedCategoryOption).isChecked();

        page.locator(".gb-category-row input[name$=\"name\"]").first().fill("A");
        page.locator(".gb-category-weight input[name$=\"weight\"]").first().fill("100");

        page.locator(".act button.active").first().click();
        assertThat(page.getByText("The settings were successfully updated", new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    @Test
    @Order(3)
    void canCreateGradebookItems() throws Exception {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        Locator dialog = page.locator(".gb-modal-dialog:visible, dialog:visible, div[role=\"dialog\"]:visible, .wicket-modal:visible, .modal:visible").first();
        Locator addButton = page.locator("button.gb-add-gradebook-item-button").first();

        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        page.waitForFunction("() => Boolean(window.Wicket && window.Wicket.Ajax)");
        addButton.click();
        assertThat(dialog).isVisible();

        Locator closeLink = dialog.locator(".gb-modal-close");
        String originalTheme = page.locator("html").getAttribute("class");
        try (AutoCloseable cleanup = () -> {
            page.locator("html").evaluate("(element, originalTheme) => { if (originalTheme === null) element.removeAttribute('class'); else element.setAttribute('class', originalTheme); }", originalTheme);
        }) {
            for (String theme : List.of("light", "dark")) {
                page.locator("html").evaluate("(element, theme) => { element.classList.remove('sakaiUserTheme-light', 'sakaiUserTheme-dark'); element.classList.add('sakaiUserTheme-' + theme); }", theme);
                String headerColor = (String) dialog.locator(".gb-modal-titlebar")
                    .evaluate("element => getComputedStyle(element).color");
                assertThat(closeLink).hasCSS("color", headerColor);
                assertThat(closeLink).hasCSS("opacity", "1");
                closeLink.hover();
                assertThat(closeLink).hasCSS("color", headerColor);
                closeLink.focus();
                assertThat(closeLink).isFocused();
                assertThat(closeLink).hasCSS("color", headerColor);
            }
        }

        Locator cancelButton = dialog.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName(Pattern.compile("Cancel|Close", Pattern.CASE_INSENSITIVE))).first();
        if (cancelButton.count() > 0) {
            cancelButton.click(new Locator.ClickOptions().setForce(true));
        }
    }

    @Test
    @Order(4)
    void canNavigateToNextStudent() throws Exception {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        Locator students = page.locator(".gb-view-grade-summary");
        assertThat(students.nth(1)).isVisible();
        String nextStudentId = students.nth(1).locator("xpath=ancestor::*[@data-student-id][1]")
            .getAttribute("data-student-id");
        students.first().click();
        Locator dialog = page.locator(".gb-modal-dialog:visible");
        assertThat(dialog).hasAttribute("role", "dialog");
        dialog.locator(".gb-summary-next-student").click();
        assertThat(dialog.locator("[data-studentid]").first()).hasAttribute("data-studentid", nextStudentId);
        int originalWidth = page.viewportSize().width;
        int originalHeight = page.viewportSize().height;
        try (AutoCloseable cleanup = () -> {
            page.setViewportSize(originalWidth, originalHeight);
        }) {
            for (int width : List.of(1280, 640)) {
                page.setViewportSize(width, originalHeight);
                assertTrue((Boolean) dialog.evaluate("element => { "
                    + "const navigation = element.querySelector('.gb-summary-previous-student').getBoundingClientRect(); "
                    + "const done = element.querySelector('.gb-summary-close').getBoundingClientRect(); "
                    + "const print = element.querySelector('.gb-summary-print').getBoundingClientRect(); "
                    + "return done.top - navigation.bottom >= 8 "
                    + "&& (print.left - done.right >= 6 || print.top - done.bottom >= 6); }"),
                    "Student navigation and footer actions should have space between them at width " + width);
            }
        }
        dialog.locator(".gb-summary-close").click();
        assertThat(dialog).isHidden();
    }

    @Test
    @Order(5)
    void courseGradePreviewHasAccessibleContrast() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        page.locator(".navIntraTool a")
            .filter(new Locator.FilterOptions().setHasText("Settings"))
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        Locator gradeReleaseButton = page.locator(".accordion button")
            .filter(new Locator.FilterOptions().setHasText("Grade Release Rules"))
            .first();
        assertThat(gradeReleaseButton).isVisible();
        if (!"true".equals(gradeReleaseButton.getAttribute("aria-expanded"))) {
            gradeReleaseButton.click(new Locator.ClickOptions().setForce(true));
        }

        Locator displayCourseGrade = page.locator("label")
            .filter(new Locator.FilterOptions().setHasText("Display final course grade to students"))
            .locator("input[type=\"checkbox\"]")
            .first();
        assertThat(displayCourseGrade).isVisible();
        if (!displayCourseGrade.isChecked()) {
            displayCourseGrade.check(new Locator.CheckOptions().setForce(true));
        }

        Locator preview = page.locator("#settingsGradeRelease .form-group")
            .filter(new Locator.FilterOptions().setHasText("Preview"))
            .locator("span")
            .nth(1);
        assertThat(preview).isVisible();

        page.evaluate("() => document.documentElement.classList.add('sakaiUserTheme-dark')");
        assertThat(preview).isVisible();
        assertNoAxeContrastViolations("dark mode");

        page.evaluate("() => document.documentElement.classList.remove('sakaiUserTheme-dark')");
        assertNoAxeContrastViolations("light mode");
    }

    @Test
    @Order(6)
    void rubricPreviewOpensAboveStudentSummary() {
        String rubricTitle = "Summary rubric " + System.currentTimeMillis();
        String itemTitle = "Rubric item " + System.currentTimeMillis();
        sakai.createRubric("instructor1", sakaiUrl);
        page.locator("input[title=\"Rubric Title\"]:visible").fill(rubricTitle);
        page.locator("sakai-rubric-edit .modal.show .modal-footer .btn-primary").click();
        assertThat(page.locator("sakai-rubric .rubric-name")
            .filter(new Locator.FilterOptions().setHasText(rubricTitle))).hasText(rubricTitle);

        sakai.toolClick("Gradebook");
        page.locator("button.gb-add-gradebook-item-button").click();
        Locator dialog = page.locator(".gb-modal-dialog:visible");
        dialog.locator("input[name$=\"title\"]").fill(itemTitle);
        dialog.locator("input[name$=\"points\"]").fill("100");
        dialog.locator("input[name=rbcs-associate][value=\"1\"]").check();
        Locator rubricSelector = dialog.locator("select[name=rbcs-rubricslist]");
        rubricSelector.selectOption(rubricSelector.locator("option")
            .filter(new Locator.FilterOptions().setHasText(rubricTitle)).getAttribute("value"));
        dialog.locator("button.gb-create-or-save").click();
        assertThat(dialog).isHidden();

        // Keyboard activation avoids the table moving the link during pointer selection.
        page.locator(".gb-view-grade-summary").first().press("Enter");
        Locator summary = page.locator(".gb-modal-dialog:visible");
        assertThat(summary).isVisible();
        // Let the modal finish its initial focus move before focusing the rubric opener.
        assertThat(summary.getByRole(com.microsoft.playwright.options.AriaRole.TAB,
            new Locator.GetByRoleOptions().setName("Grade Summary").setExact(true))).isFocused();
        Locator rubricLink = summary.locator("tr").filter(new Locator.FilterOptions().setHasText(itemTitle))
            .locator("sakai-rubric-student-button a");
        // Establish the opener's focus before checking that closing the preview restores it.
        rubricLink.focus();
        assertThat(rubricLink).isFocused();
        rubricLink.press("Enter");
        Locator preview = page.locator("#rubric-preview");
        assertThat(preview).isVisible();
        assertThat(preview.locator("sakai-rubric-student h3")).hasText(rubricTitle);
        assertThat(summary.locator("xpath=..")).hasAttribute("inert", "");
        // A normal click verifies that the summary overlay does not intercept the rubric.
        preview.locator(".btn-close").click();
        assertThat(preview).isHidden();
        assertThat(summary).isVisible();
        assertThat(rubricLink).isFocused();
        summary.locator(".gb-summary-close").click();
        assertThat(summary).isHidden();
    }

    @Test
    @Order(7)
    void canCancelAndConfirmBulkItemDeletion() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");

        String itemTitle = "Bulk deletion " + System.currentTimeMillis();
        page.locator("button.gb-add-gradebook-item-button").click();
        Locator dialog = page.locator(".gb-modal-dialog:visible");
        dialog.locator("input[name$=\"title\"]").fill(itemTitle);
        dialog.locator("input[name$=\"points\"]").fill("100");
        dialog.locator("button.gb-create-or-save").click();
        assertThat(dialog).isHidden();

        page.locator("#bulkEditItemsToolbarItem").click();
        Locator itemRow = dialog.locator("tr").filter(new Locator.FilterOptions().setHasText(itemTitle));
        itemRow.locator(".deleteBox").check();
        dialog.locator("#gb-bulk-edit-fake-submit").click();
        Locator confirmation = page.locator(".gb-bulk-edit-confirmation");
        assertThat(confirmation).isVisible();
        confirmation.locator(".gb-bulk-edit-cancel").click();
        assertThat(confirmation).hasCount(0);
        assertThat(itemRow.locator(".deleteBox")).isChecked();
        assertThat(page.locator(".modal-backdrop")).hasCount(0);

        dialog.locator("#gb-bulk-edit-fake-submit").click();
        assertThat(confirmation).isVisible();
        confirmation.locator(".gb-bulk-edit-continue").click();
        assertThat(dialog).isHidden();
        assertThat(page.locator(".tabulator-col")
            .filter(new Locator.FilterOptions().setHasText(itemTitle))).hasCount(0);
        assertThat(page.locator(".modal-backdrop")).hasCount(0);
    }

    @Test
    @Order(8)
    void clearingTheStudentFilterRestoresAllStudents() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Gradebook");
        Locator filter = page.locator("#studentFilterInput");
        Locator students = page.locator(".gb-view-grade-summary");
        assertThat(students.nth(1)).isVisible();
        int all = students.count();

        filter.fill("student0011");
        filter.press("Enter");
        assertThat(students).hasCount(1);
        assertThat(filter).hasValue("student0011");

        // deleting the text, as the native clear button does, shows every student again
        filter.fill("");
        assertThat(students).hasCount(all);
    }

    private void assertNoAxeContrastViolations(String theme) {
        AxeResults results = new AxeBuilder(page)
            .include("#settingsGradeRelease")
            .withRules(List.of("color-contrast"))
            .analyze();

        assertTrue(results.violationFree(),
            "Grade Release Rules should have no axe color-contrast violations in " + theme
                + System.lineSeparator() + formatAxeViolations(results.getViolations()));
    }

    private String formatAxeViolations(List<Rule> violations) {
        return violations.stream()
            .map(rule -> rule.getId() + ": " + rule.getHelp() + System.lineSeparator()
                + rule.getNodes().stream()
                    .map(this::formatAxeNode)
                    .collect(Collectors.joining(System.lineSeparator())))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private String formatAxeNode(CheckedNode node) {
        return "  " + node.getTarget() + " - " + node.getFailureSummary();
    }
}
