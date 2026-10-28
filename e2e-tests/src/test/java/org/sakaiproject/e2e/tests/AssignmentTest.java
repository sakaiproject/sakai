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
import static org.junit.jupiter.api.Assertions.fail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssignmentTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String ASSIGN_TITLE = "Playwright Assignment " + System.currentTimeMillis();

    private String ensureCourseUrl() {
        if (sakaiUrl != null && !sakaiUrl.isBlank()) {
            return sakaiUrl;
        }

        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of(
            "sakai\\.rubrics",
            "sakai\\.assignment\\.grades",
            "sakai\\.gradebookng",
            "sakai\\.lessonbuildertool"
        ));
        return sakaiUrl;
    }

    @Test
    @Order(1)
    void canCreateNewCourse() {
        ensureCourseUrl();
        assertThat(page.locator("body")).containsText("SMPL101");
    }

    @Test
    @Order(2)
    void canCreateLetterGradeAssignment() {
        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");

        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill("Letter Grades");

        Locator gradeType = page.locator("#new_assignment_grade_type").first();
        if (gradeType.count() > 0) {
            gradeType.selectOption(new SelectOption().setLabel("Letter grade"));
        }

        fillAssignmentInstructions("<p>Letter grade assignment prompt.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        assertThat(page.locator("body")).containsText("Letter Grades");
    }

    @Test
    @Order(3)
    void canCreatePointsAssignment() {
        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");

        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(ASSIGN_TITLE);

        Locator honorPledge = page.locator("#new_assignment_check_add_honor_pledge").first();
        if (honorPledge.count() > 0) {
            honorPledge.click(new Locator.ClickOptions().setForce(true));
        }

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck(new Locator.UncheckOptions().setForce(true));
        }

        fillAssignmentInstructions("<p>Points assignment prompt.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        assertThat(page.locator("body")).containsText(ASSIGN_TITLE);
    }

    @Test
    @Order(4)
    void canAssociateRubricWithAssignment() {
        String courseUrl = ensureCourseUrl();
        String rubricAssignmentTitle = "Rubric Assignment " + System.currentTimeMillis();
        String rubricTitle = "Draft restriction " + System.currentTimeMillis();

        sakai.createRubric("instructor1", courseUrl);
        page.locator("input[title=\"Rubric Title\"]:visible").fill(rubricTitle);
        page.locator(".modal-footer button:visible, div.popover button:visible, button:visible")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Save", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("sakai-rubric .rubric-name")
                .filter(new Locator.FilterOptions().setHasText(rubricTitle))).hasText(rubricTitle);

        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");
        goToAssignmentsList();
        openAddAssignmentForm();

        page.locator("#new_assignment_title").fill(rubricAssignmentTitle);

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0) {
            gradeAssignment.check(new Locator.CheckOptions().setForce(true));
        }

        Locator points = page.locator("#new_assignment_grade_points").first();
        if (points.count() > 0) {
            points.fill("55.13");
        }

        Locator associateRubric = page.locator("input[name=\"rbcs-associate\"][value=\"1\"]").first();
        assertThat(associateRubric).isVisible();
        associateRubric.check();
        Locator rubricSelector = page.locator("select[name=rbcs-rubricslist]");
        rubricSelector.selectOption(rubricSelector.locator("option")
                .filter(new Locator.FilterOptions().setHasText(rubricTitle)).getAttribute("value"));
        String rubricId = rubricSelector.inputValue();

        fillAssignmentInstructions("<p>Rubric assignment prompt.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        Locator rubricRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(rubricAssignmentTitle)).first();
        assertThat(rubricRow).isVisible();
        assertThat(rubricRow).containsText(Pattern.compile("55\\.13|55", Pattern.CASE_INSENSITIVE));

        Locator editLink = rubricRow.locator("a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Edit\\b", Pattern.CASE_INSENSITIVE))).first();
        editLink.click();
        assertThat(page.locator("#new_assignment_title")).hasValue(rubricAssignmentTitle);
        assertThat(rubricSelector).hasValue(rubricId);
        assertThat(associateRubric).isChecked();
        // Exit the edit form so returning to Assignments restores the list.
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        assertThat(page.locator("#assignments-list")).isVisible();

        sakai.gotoPath(courseUrl);
        sakai.toolClick("Rubrics");
        Locator rubric = page.locator("sakai-rubric").filter(new Locator.FilterOptions().setHasText(rubricTitle));
        Locator draftButton = rubric.locator("button.draft");
        assertThat(draftButton).hasAttribute("aria-disabled", "true");
        assertThat(draftButton).hasAttribute("title", "Unlink this rubric from all items before saving it as a draft.");

        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");
        assertThat(page.locator("#assignments-list")).isVisible();
        editAssignment(rubricAssignmentTitle);
        assertThat(rubricSelector).hasValue(rubricId);
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions()
                .setName("Do not use a rubric to grade this assignment").setExact(true)).check();
        assertThat(rubricSelector).isDisabled();
        submitAssignmentForm();
        assertThat(page.locator("#assignments-list")).isVisible();

        sakai.gotoPath(courseUrl);
        sakai.toolClick("Rubrics");
        assertThat(draftButton).hasAttribute("aria-disabled", "false");
        Response draftSave = page.waitForResponse(response -> response.url().endsWith("/rubrics/" + rubricId)
                && response.request().method().equals("PATCH"), () -> draftButton.click());
        assertTrue(draftSave.ok(), () -> "Rubric draft save failed: HTTP " + draftSave.status());
        assertThat(draftButton).hasAttribute("title", "Publish Rubric");
        page.reload();
        assertThat(draftButton).hasAttribute("title", "Publish Rubric");
    }

    @Test
    @Order(5)
    @Disabled("Parity with JS suite: this student submission flow remains intentionally skipped")
    void canSubmitAsStudentOnDesktop() {
    }

    @Test
    @Order(6)
    @Disabled("Parity with JS suite: this mobile student submission flow remains intentionally skipped")
    void canSubmitAsStudentOnIphoneViewport() {
    }

    @Test
    @Order(7)
    void canAllowStudentToResubmit() {
        String courseUrl = ensureCourseUrl();
        try (TestUser grader = sakai.createUser("resubmission-grader")) {
            sakai.addMember(courseUrl, grader.username(), "Instructor");
            String title = "Resubmission " + System.currentTimeMillis();
            sakai.login("instructor1");
            sakai.gotoPath(courseUrl);
            sakai.toolClick("Assignments");
            goToAssignmentsList();
            openAddAssignmentForm();
            page.locator("#new_assignment_title").fill(title);
            page.locator("#gradeAssignment").check();
            page.locator("#new_assignment_grade_points").fill("10");
            fillAssignmentInstructions("<p>Resubmission fixture.</p>");
            submitAssignmentForm();
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
            sakai.addMember(courseUrl, "student0012", "Student");
            sakai.login("student0012");
            sakai.gotoPath(courseUrl);
            sakai.toolClick("Assignments");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(title).setExact(true)).click();
            sakai.typeCkEditor("Assignment.view_submission_text", "<p>Work to resubmit.</p>");
            page.locator("#confirm").click();
            page.locator("#post").click();
            assertThat(page.locator("body")).containsText("Submission Confirmation");
            sakai.login(grader.username());
            sakai.setAssignmentGrader(true);
            sakai.gotoPath(courseUrl);
            sakai.toolClick("Assignments");
            Locator row = page.locator("tr").filter(new Locator.FilterOptions().setHasText(title));
            row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions()
                .setName("Grade " + title).setExact(true)).click();
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
            Locator student = page.locator("#submissionList a")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("\\bstudent0012\\b"))).first();
            assertThat(student).isVisible();
            student.click();
            assertThat(page.locator("sakai-grader #grader")).isVisible(
                new LocatorAssertions.IsVisibleOptions().setTimeout(15_000));
            Locator resubmit = page.locator(".resubmission-checkbox input").first();
            assertThat(resubmit).isVisible();
            resubmit.check();
            assertThat(resubmit).isChecked();
            page.locator("#grader-save-buttons button[name=return]").click();
        }
    }

    @Test
    @Order(8)
    void canCreateNonElectronicAssignment() {
        String nonElectronicTitle = "Non-electronic Assignment " + System.currentTimeMillis();
        String courseUrl = ensureCourseUrl();

        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");
        goToAssignmentsList();

        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(nonElectronicTitle);

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck(new Locator.UncheckOptions().setForce(true));
        }

        Locator subType = page.locator("#subType").first();
        if (subType.count() > 0) {
            subType.selectOption(new SelectOption().setLabel("Non-electronic"));
        }

        fillAssignmentInstructions("<p>Submit a printed essay to office 201.</p>");
        submitAssignmentForm();

        goToAssignmentsList();
        assertThat(page.locator("body")).containsText(nonElectronicTitle);
    }

    @Test
    @Order(9)
    void reorderShowsAndSavesAssignmentsBeyondFirstPage() {
        String courseUrl = ensureCourseUrl();
        String titlePrefix = "Reorder Assignment " + System.currentTimeMillis() + " ";
        String firstTitle = titlePrefix + "01";
        String lastTitle = titlePrefix + "21";

        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");

        for (int i = 1; i <= 21; i++) {
            sakai.createAssignment(courseUrl, titlePrefix + String.format("%02d", i));
        }

        // Reload the tool list after creating fixtures through the assignment API.
        sakai.toolClick("Assignments");
        openReorderAssignments();
        assertAssignmentReorderChrome();

        Locator reorderItems = page.locator("#reorder-list li.reorder-element.sakai-reorder-row");
        int reorderCount = reorderItems.count();
        if (reorderCount < 21) {
            fail("Expected the reorder list to include assignments beyond the first page.");
        }
        assertThat(page.locator("#reorder-list")).containsText(firstTitle);
        assertThat(page.locator("#reorder-list")).containsText(lastTitle);
        assertThat(page.locator("input[id^=\"index\"]")).hasCount(0);

        String originalTopTitle = reorderItems.first().locator(".sakai-reorder-title").innerText().trim();
        Locator lastAssignment = reorderItems.filter(new Locator.FilterOptions().setHasText(lastTitle)).first();
        boolean lastAlreadyOnTop = lastAssignment.locator("[data-action=\"move-up\"]").isDisabled();

        if (!lastAlreadyOnTop) {
            lastAssignment = moveAssignmentToTop(reorderItems, lastTitle, reorderCount);

            Locator undoAll = page.locator("#undo-all");
            assertThat(undoAll).isEnabled();
            undoAll.click();
            assertThat(reorderItems.first().locator(".sakai-reorder-title")).containsText(originalTopTitle);

            lastAssignment = moveAssignmentToTop(reorderItems, lastTitle, reorderCount);
        }

        assertAssignmentPrecedes("#reorder-list", lastTitle, firstTitle);
        assertThat(lastAssignment.locator("input[name^=\"position_\"]")).hasValue("1");
        assertTrue(lastAssignment.locator("[data-action=\"move-up\"]").isDisabled());

        page.locator("input[name=\"save\"][value=\"Save\"], input[name=\"save\"]").first().click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

        goToAssignmentsList();
        String assignmentListText = page.locator("body").innerText();
        int lastIndex = assignmentListText.indexOf(lastTitle);
        int firstIndex = assignmentListText.indexOf(firstTitle);
        if (lastIndex < 0 || firstIndex < 0 || lastIndex > firstIndex) {
            fail("Expected reordered assignments beyond the first page to keep the saved order.");
        }

        openReorderAssignments();
        assertAssignmentPrecedes("#reorder-list", lastTitle, firstTitle);
    }

    @Test
    @Order(10)
    void canCreateSelectAndClearTagFilter() {
        String courseUrl = ensureCourseUrl();
        String suffix = Long.toString(System.currentTimeMillis());
        String taggedTitle = "Tagged assignment " + suffix;
        String otherTitle = "Other assignment " + suffix;
        String tagLabel = "Assignment tag " + suffix;
        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");
        createSimpleAssignment(otherTitle);

        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(taggedTitle);
        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck();
        }
        fillAssignmentInstructions("<p>Tag selector regression.</p>");
        Locator selector = page.locator("sakai-tag-selector");
        selector.getByRole(AriaRole.COMBOBOX).fill(tagLabel);
        selector.getByRole(AriaRole.COMBOBOX).press("Enter");
        assertThat(page.locator("#tag_selector")).hasValue(tagLabel);
        submitAssignmentForm();
        goToAssignmentsList();

        selector.getByRole(AriaRole.COMBOBOX).fill(tagLabel);
        selector.getByRole(AriaRole.COMBOBOX).press("ArrowDown");
        selector.getByRole(AriaRole.COMBOBOX).press("Enter");
        String selectedIds = page.locator("#tag_selector").inputValue();
        String tagRequests = "**/api/sites/*/tools/*/tags/**";
        page.route(tagRequests, route -> route.abort());
        page.locator("#btnSearchTags1").click();
        assertThat(selector.getByRole(AriaRole.ALERT)).isVisible();
        assertThat(page.locator("#tag_selector")).hasAttribute("value", selectedIds);
        assertThat(page.locator("#tag_selector")).hasValue(selectedIds);
        page.locator("#btnSearchTags1").click();
        assertThat(selector.getByRole(AriaRole.ALERT)).isVisible();
        assertThat(page.locator("#tag_selector")).hasValue(selectedIds);
        page.unroute(tagRequests);
        assertThat(page.locator("tr").filter(new Locator.FilterOptions().setHasText(taggedTitle))).isVisible();
        assertThat(page.locator("tr").filter(new Locator.FilterOptions().setHasText(otherTitle))).hasCount(0);
        page.locator("#btnSearchTagsClear").click();
        assertThat(page.locator("tr").filter(new Locator.FilterOptions().setHasText(otherTitle))).isVisible();
        assertThat(page.locator("#tag_selector")).hasValue("");
    }

    @Test
    @Order(11)
    void preservesSavedTagsWhenLoadingFailsAndAllowsRemovingAllTags() {
        String courseUrl = ensureCourseUrl();
        String title = "Preserved tags " + System.currentTimeMillis();
        String label = "Saved assignment tag " + System.currentTimeMillis();
        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");
        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(title);
        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck();
        }
        fillAssignmentInstructions("<p>Preserve tags when the selector cannot load.</p>");
        Locator selector = page.locator("sakai-tag-selector");
        selector.getByRole(AriaRole.COMBOBOX).fill(label);
        selector.getByRole(AriaRole.COMBOBOX).press("Enter");
        submitAssignmentForm();
        goToAssignmentsList();

        String tagRequests = "**/api/sites/*/tools/*/tags/**";
        for (boolean failRequest : List.of(true, false)) {
            // Cover both a failed request and one still pending when the form is saved.
            page.route(tagRequests, route -> {
                if (failRequest) {
                    route.abort();
                }
            });
            editAssignment(title);
            assertThat(page.locator("#tag_selector")).hasAttribute("value", Pattern.compile(".+"));
            String savedIds = page.locator("#tag_selector").getAttribute("value");
            assertThat(selector.getByRole(AriaRole.COMBOBOX)).isDisabled();
            if (failRequest) {
                assertThat(selector.getByRole(AriaRole.ALERT)).isVisible();
            }
            assertThat(page.locator("#tag_selector")).hasValue(savedIds);
            fillAssignmentInstructions("<p>Save with the existing tags.</p>");
            page.unroute(tagRequests);
            submitAssignmentForm();
            assertThat(page.locator("#new_assignment_title")).hasCount(0);
            goToAssignmentsList();
            editAssignment(title);
            assertThat(selector.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Deselect: " + label).setExact(true))).isVisible();
            assertThat(page.locator("#tag_selector")).hasValue(savedIds);
            submitAssignmentForm();
            goToAssignmentsList();
        }

        editAssignment(title);
        selector.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Deselect: " + label).setExact(true)).click();
        assertThat(page.locator("#tag_selector")).hasValue("");
        submitAssignmentForm();
        goToAssignmentsList();
        editAssignment(title);
        assertThat(selector.getByRole(AriaRole.COMBOBOX)).isEnabled();
        assertThat(selector.getByRole(AriaRole.BUTTON)).hasCount(0);
        assertThat(page.locator("#tag_selector")).hasValue("");
    }

    private void editAssignment(String title) {
        page.locator("tr").filter(new Locator.FilterOptions().setHasText(title))
            .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(Pattern.compile("^Edit\\b"))).click();
        assertThat(page.locator("#new_assignment_title")).hasValue(title);
    }

    @Test
    void externalToolPickerDisplaysItsContent() {
        String courseUrl = ensureCourseUrl();
        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");
        openAddAssignmentForm();

        page.locator("#subType").selectOption(new SelectOption().setLabel("External Tool (LTI)"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Select External Tool (LTI)").setExact(true)).click();

        // The picker must render a tool list or the no-tools message, even on a site without LTI tools.
        assertThat(page.frameLocator("iframe[name='sakai-lti-admin-iframe']").locator(".portletBody")).isVisible();
        assertThat(page.frameLocator("iframe[name='sakai-lti-admin-iframe']").locator(".portletBody"))
            .not().hasText("");
    }

    private void openAddAssignmentForm() {
        goToAssignmentsList();

        Locator addLink = page.locator(".navIntraTool a, .navIntraTool button, .navIntraTool [role=\"button\"]")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^(Add|New)$", Pattern.CASE_INSENSITIVE)))
            .first();

        String href = null;
        if (addLink.count() > 0) {
            href = addLink.getAttribute("href");
            addLink.click(new Locator.ClickOptions().setForce(true));
        }

        Locator titleInput = page.locator("#new_assignment_title").first();
        if (!isVisible(titleInput, 8_000) && href != null && !href.equals("#")) {
            page.navigate(href);
        }

        if (!isVisible(titleInput, 8_000)) {
            String currentUrl = page.url();
            String direct = currentUrl.contains("?") ? currentUrl + "&sakai_action=doNew_assignment" : currentUrl + "?sakai_action=doNew_assignment";
            page.navigate(direct);
        }

        assertThat(titleInput).isVisible();
    }

    @Test
    @Order(12)
    void deletedAssignmentHasNoActiveLessonsLink() {
        String courseUrl = ensureCourseUrl();
        String title = "Deleted lesson assignment " + System.currentTimeMillis();
        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");
        createSimpleAssignment(title);

        sakai.toolClick("Lessons");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        page.locator("#addContentDiv").getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Link to an Assignment").setExact(true)).click();
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(title).setExact(true)).check();
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Use selected item").setExact(true)).click();
        Locator lessonLink = page.locator("#content").getByRole(AriaRole.LINK)
            .filter(new Locator.FilterOptions().setHasText(title));
        assertThat(lessonLink).isVisible();

        sakai.toolClick("Assignments");
        page.locator("tr").filter(new Locator.FilterOptions().setHasText(title))
            .locator("td[headers=remove] input[type=checkbox]").check();
        page.locator("#btnRemove").click();
        page.locator("input[name=eventSubmit_doDelete_assignment]").click();
        sakai.toolClick("Lessons");
        assertThat(page.locator(".fake-disabled").filter(new Locator.FilterOptions().setHasText(title))).isVisible();
        assertThat(page.locator("#content")).containsText("*Deleted*");
        assertThat(lessonLink).hasCount(0);
    }

    @Test
    void displaysSavedTagMarkupAsLiteralText() {
        String courseUrl = ensureCourseUrl();
        String title = "Literal tag " + System.currentTimeMillis();
        String label = "<img src=x onerror=alert(1)> & \"literal\"";
        sakai.login("instructor1");
        sakai.gotoPath(courseUrl);
        sakai.toolClick("Assignments");
        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(title);
        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck();
        }
        fillAssignmentInstructions("<p>Literal tag regression.</p>");
        Locator selector = page.locator("sakai-tag-selector");
        selector.getByRole(AriaRole.COMBOBOX).fill(label);
        selector.getByRole(AriaRole.COMBOBOX).press("Enter");
        submitAssignmentForm();
        goToAssignmentsList();
        Locator row = page.locator("tr").filter(new Locator.FilterOptions().setHasText(title));
        assertThat(row.locator(".tag-header")).containsText(label);
        assertThat(row.locator(".tag-header img")).hasCount(0);
        editAssignment(title);
        assertThat(selector.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Deselect: " + label).setExact(true))).isVisible();
    }

    private void openReorderAssignments() {
        goToAssignmentsList();

        Locator reorderLink = page.locator(".navIntraTool a, .navIntraTool button, .navIntraTool [role=\"button\"]")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Reorder$", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(reorderLink).isVisible();
        reorderLink.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        assertThat(page.locator("#assignments-reorderer")).isVisible();
        assertThat(page.locator("#reorder-list")).isVisible();
        page.waitForFunction(
            "() => document.querySelector('#assignments-reorderer')?.updateComplete",
            null,
            new Page.WaitForFunctionOptions().setTimeout(15_000)
        );
        assertThat(page.locator("#reorder-list li.reorder-element .sakai-reorder-drag-handle.drag-handle, #reorder-list li.reorder-element .drag-handle").first())
            .isVisible();
    }

    private void assertAssignmentReorderChrome() {
        Locator portlet = page.locator(".portletBody.sakai-reorder");
        assertThat(portlet).isVisible();
        assertThat(portlet.locator(".page-header h1")).isVisible();
        assertThat(portlet.locator(".instruction")).containsText(Pattern.compile("Drag assignments|move buttons|Save", Pattern.CASE_INSENSITIVE));
        assertThat(page.locator(".sakai-reorder-undo.noPrint")).isVisible();
        assertThat(page.locator("#undo-last")).isDisabled();
        assertThat(page.locator("#undo-all")).isDisabled();
        assertThat(page.locator("#reorder-list-sortingToolBar.sakai-reorder-list-header")).isVisible();
        assertThat(page.locator("#reorder-list-sortingToolBar .bi-sort-up, #reorder-list-sortingToolBar .bi-sort-down, #reorder-list-sortingToolBar a").first()).isVisible();
        assertThat(page.locator("#reorder-list li.reorder-element [data-action=\"move-up\"]").first()).isVisible();
        assertThat(page.locator("#reorder-list li.reorder-element [data-action=\"move-down\"]").first()).isVisible();
    }

    private Locator moveAssignmentToTop(Locator reorderItems, String title, int reorderCount) {
        Locator assignment = reorderItems.filter(new Locator.FilterOptions().setHasText(title)).first();
        // At most (n - 1) moves are needed to reach the top from the last row.
        for (int i = 0; i < reorderCount - 1; i++) {
            assignment = reorderItems.filter(new Locator.FilterOptions().setHasText(title)).first();
            Locator moveUp = assignment.locator("[data-action=\"move-up\"]");
            if (moveUp.isDisabled()) {
                break;
            }
            moveUp.click();
        }
        assignment = reorderItems.filter(new Locator.FilterOptions().setHasText(title)).first();
        assertTrue(assignment.locator("[data-action=\"move-up\"]").isDisabled(),
            "Expected move-up to be disabled after moving " + title + " to the top.");
        return assignment;
    }

    private void assertAssignmentPrecedes(String selector, String firstTitle, String secondTitle) {
        String listText = page.locator(selector).innerText();
        int firstTitlePosition = listText.indexOf(firstTitle);
        int secondTitlePosition = listText.indexOf(secondTitle);
        if (firstTitlePosition < 0 || secondTitlePosition < 0) {
            fail("Expected reorder list to contain " + firstTitle + " and " + secondTitle + ".");
        }
        if (firstTitlePosition >= secondTitlePosition) {
            fail("Expected assignment " + firstTitle + " to precede " + secondTitle + ".");
        }
    }

    private void createSimpleAssignment(String title) {
        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(title);

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck(new Locator.UncheckOptions().setForce(true));
        }

        fillAssignmentInstructions("<p>Reorder regression assignment.</p>");
        submitAssignmentForm();
        goToAssignmentsList();
    }

    private void goToAssignmentsList() {
        if (page.locator("#assignments-list").isVisible()) {
            return;
        }
        Locator assignmentsNav = page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Assignments$", Pattern.CASE_INSENSITIVE)))
            .first();
        if (assignmentsNav.count() > 0) {
            assignmentsNav.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        }
    }

    private void submitAssignmentForm() {
        Locator submit = page.locator(
            "div.act input[type=\"button\"][name=\"post\"]:visible, .act input[type=\"button\"][name=\"post\"]:visible, " +
            "div.act input[type=\"submit\"][name=\"post\"]:visible, .act input[type=\"submit\"][name=\"post\"]:visible, " +
            "div.act input[type=\"button\"][value*=\"Save and Release\"]:visible, .act input[type=\"button\"][value*=\"Save and Release\"]:visible, " +
            "div.act input[type=\"submit\"][value*=\"Save and Release\"]:visible, .act input[type=\"submit\"][value*=\"Save and Release\"]:visible, " +
            "div.act button:has-text(\"Save and Release\"):visible, .act button:has-text(\"Save and Release\"):visible, " +
            "div.act input[type=\"button\"][name=\"save\"]:visible, .act input[type=\"button\"][name=\"save\"]:visible, " +
            "div.act input[type=\"submit\"][name=\"save\"]:visible, .act input[type=\"submit\"][name=\"save\"]:visible, " +
            "div.act input[type=\"button\"][value=\"Post\"]:visible, .act input[type=\"button\"][value=\"Post\"]:visible, " +
            "div.act input[type=\"submit\"][value=\"Post\"]:visible, .act input[type=\"submit\"][value=\"Post\"]:visible, " +
            "div.act button:has-text(\"Post\"):visible, .act button:has-text(\"Post\"):visible, " +
            "div.act input[type=\"button\"][value=\"Save\"]:visible, .act input[type=\"button\"][value=\"Save\"]:visible, " +
            "div.act input[type=\"submit\"][value=\"Save\"]:visible, .act input[type=\"submit\"][value=\"Save\"]:visible, " +
            "div.act button:has-text(\"Save\"):visible, .act button:has-text(\"Save\"):visible, " +
            "input[type=\"submit\"][name=\"post\"]:visible, input[type=\"button\"][name=\"post\"]:visible, " +
            "input[type=\"submit\"][name=\"save\"]:visible, input[type=\"button\"][name=\"save\"]:visible"
        ).first();
        assertThat(submit).isVisible();
        submit.click(new Locator.ClickOptions().setForce(true));
    }

    private void fillAssignmentInstructions(String html) {
        if (!sakai.typeCkEditorIfPresent("new_assignment_instructions", html)) {
            Locator fallback = page.locator("textarea#new_assignment_instructions, textarea:visible, [contenteditable=\"true\"]:visible").first();
            if (fallback.count() > 0) {
                fallback.fill(html.replaceAll("<[^>]+>", ""));
            }
        }
    }

    private boolean isVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
