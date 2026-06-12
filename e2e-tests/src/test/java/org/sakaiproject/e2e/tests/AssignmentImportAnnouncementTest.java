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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AssignmentImportAnnouncementTest extends SakaiUiTestBase {

    private static final String ASSIGNMENT_TITLE = "SAK-51487 Assignment " + System.currentTimeMillis();

    @Test
    void replaceImportPublishingAssignmentsCreatesOpenDateAnnouncement() {
        sakai.login("instructor1");
        String sourceSite = sakai.createCourse("instructor1", List.of(
            "sakai\\.announcements",
            "sakai\\.assignment\\.grades",
            "sakai\\.schedule",
            "sakai\\.gradebookng"
        ));
        String destinationSite = sakai.createCourse("instructor1", List.of(
            "sakai\\.announcements",
            "sakai\\.assignment\\.grades",
            "sakai\\.schedule",
            "sakai\\.gradebookng",
            "sakai\\.dashboard"
        ));

        page.navigate(sourceSite);
        sakai.toolClick("Assignments");
        openAddAssignmentForm();
        page.locator("#new_assignment_title").fill(ASSIGNMENT_TITLE);

        Locator gradeAssignment = page.locator("#gradeAssignment").first();
        if (gradeAssignment.count() > 0 && gradeAssignment.isChecked()) {
            gradeAssignment.uncheck(new Locator.UncheckOptions().setForce(true));
        }

        page.locator("#new_assignment_check_auto_announce").check(new Locator.CheckOptions().setForce(true));
        Locator addDueDate = page.locator("#new_assignment_check_add_due_date").first();
        if (addDueDate.count() > 0) {
            addDueDate.check(new Locator.CheckOptions().setForce(true));
        }

        fillAssignmentInstructions("<p>Import announcement regression assignment.</p>");
        submitAssignmentForm();
        assertThat(page.locator("body")).containsText(ASSIGNMENT_TITLE);

        sakai.toolClick("Announcements");
        assertThat(page.locator("body")).containsText(ASSIGNMENT_TITLE);

        page.navigate(destinationSite);
        sakai.toolClick("Site Info");
        page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Import from Site$", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
                .setName(Pattern.compile("replace my data", Pattern.CASE_INSENSITIVE)))
            .first()
            .click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        String sourceSiteId = sakai.siteIdFromUrl(sourceSite);
        page.locator("input[name=\"importSites\"][value=\"" + sourceSiteId + "\"]").check(new Locator.CheckOptions().setForce(true));
        clickContinueOrFinish();

        page.locator("#toolSite-sakai\\.assignment\\.grades-" + sourceSiteId).check(new Locator.CheckOptions().setForce(true));
        page.locator("#toolSite-sakai\\.announcements-" + sourceSiteId).check(new Locator.CheckOptions().setForce(true));
        page.locator("#toolSite-sakai\\.schedule-" + sourceSiteId).check(new Locator.CheckOptions().setForce(true));
        page.locator("#toolSite-sakai\\.gradebookng-" + sourceSiteId).check(new Locator.CheckOptions().setForce(true));
        page.locator("input[name=\"sakai.assignment.grades$" + sourceSiteId + "-import-option-publish\"]")
            .check(new Locator.CheckOptions().setForce(true));
        page.onDialog(dialog -> dialog.accept());
        clickContinueOrFinish();
        page.waitForLoadState();

        sakai.toolClick("Assignments");
        assertThat(page.locator("body")).containsText(ASSIGNMENT_TITLE);
        Locator assignmentRow = page.locator("tr, li, .assignment").filter(new Locator.FilterOptions().setHasText(ASSIGNMENT_TITLE)).first();
        assertThat(assignmentRow).not().containsText(Pattern.compile("\\bDraft\\b", Pattern.CASE_INSENSITIVE));

        sakai.toolClick("Announcements");
        assertThat(page.locator("body")).containsText(ASSIGNMENT_TITLE);
    }

    private void openAddAssignmentForm() {
        Locator addLink = page.locator(".navIntraTool a, .navIntraTool button, .navIntraTool [role=\"button\"]")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^(Add|New)$", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(addLink).isVisible();
        addLink.click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#new_assignment_title")).isVisible();
    }

    private void fillAssignmentInstructions(String html) {
        if (!sakai.typeCkEditorIfPresent("new_assignment_instructions", html)) {
            Locator fallback = page.locator("textarea#new_assignment_instructions, textarea:visible, [contenteditable=\"true\"]:visible").first();
            if (fallback.count() > 0) {
                fallback.fill(html.replaceAll("<[^>]+>", ""));
            }
        }
    }

    private void submitAssignmentForm() {
        Locator submit = page.locator(
            "div.act input[type=\"button\"][name=\"post\"]:visible, .act input[type=\"button\"][name=\"post\"]:visible, " +
            "div.act input[type=\"submit\"][name=\"post\"]:visible, .act input[type=\"submit\"][name=\"post\"]:visible, " +
            "div.act input[type=\"button\"][value*=\"Save and Release\"]:visible, .act input[type=\"button\"][value*=\"Save and Release\"]:visible, " +
            "div.act input[type=\"submit\"][value*=\"Save and Release\"]:visible, .act input[type=\"submit\"][value*=\"Save and Release\"]:visible, " +
            "div.act button:has-text(\"Save and Release\"):visible, .act button:has-text(\"Save and Release\"):visible, " +
            "div.act input[type=\"button\"][value=\"Post\"]:visible, .act input[type=\"button\"][value=\"Post\"]:visible, " +
            "div.act input[type=\"submit\"][value=\"Post\"]:visible, .act input[type=\"submit\"][value=\"Post\"]:visible, " +
            "div.act button:has-text(\"Post\"):visible, .act button:has-text(\"Post\"):visible"
        ).first();
        assertThat(submit).isVisible();
        submit.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    private void clickContinueOrFinish() {
        Locator button = page.locator(
            "input[type=\"submit\"][value*=\"Continue\"], input[type=\"submit\"][value*=\"Finish\"], " +
            "button:has-text(\"Continue\"), button:has-text(\"Finish\"), #siteimport-finish-button"
        ).first();
        assertThat(button).isVisible();
        button.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

}
