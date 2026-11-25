/*
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
package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for the checklist settings of Lessons. */
class LessonsChecklistTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool");

    private static String siteUrl;

    private LessonsUi lessons;

    @BeforeEach
    void openCourse() {
        lessons = new LessonsUi(page, sakai);
        sakai.login(INSTRUCTOR);
        if (siteUrl == null) {
            siteUrl = sakai.createFreshCourse(INSTRUCTOR, TOOLS);
        }
    }

    private String newPage(String prefix) {
        String title = prefix + " " + sakai.randomId();
        lessons.open(siteUrl);
        lessons.createTopLevelPage(title);
        return title;
    }

    /** The edit button of an item only appears while it is hovered. */
    private void editChecklist(String name) {
        Locator checklist = lessons.item(name);
        checklist.hover();
        checklist.locator("a[title='Edit Checklist " + name + "']").click();
        assertThat(page.locator("#name")).hasValue(name);
    }

    private void saveChecklist() {
        page.locator("#save").click();
        assertThat(page.locator("#addcontent")).isAttached();
    }

    @Test
    void hiddenChecklistNameIsOnlyShownToInstructors() {
        String title = newPage("Hidden checklist");
        String name = "Hidden name " + sakai.randomId();
        lessons.addChecklist(name, "Read the syllabus");
        editChecklist(name);
        page.locator("#isNameHidden").check();
        page.locator("#description").fill("Tasks for the first week");
        saveChecklist();

        Locator checklist = lessons.item("Tasks for the first week");
        assertThat(checklist.locator(".checklistTitle")).hasText("( " + name + " )");
        assertThat(checklist.locator(".checklistTitle")).hasClass(Pattern.compile("\\bhiddenTitle\\b"));
        assertThat(checklist.locator(".checklistDescription")).hasText("Tasks for the first week");

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-checklist-name")) {
            sakai.login(student.username());
            page.navigate(siteUrl);
            sakai.toolClick(title);
            assertThat(checklist.locator(".checklistTitle")).isHidden();
            assertThat(checklist.locator(".checklistDescription")).isVisible();
            assertThat(checklist.locator("input.checklist-checkbox + label")).hasText("Read the syllabus");
        }
    }

    @Test
    void indentAndCustomClassStyleTheChecklist() {
        newPage("Styled checklist");
        String name = "Styled checklist " + sakai.randomId();
        lessons.addChecklist(name, "Watch the video");
        editChecklist(name);
        page.locator(".checklist-settings-header").filter(new Locator.FilterOptions().setHasText("Appearance")).click();
        page.locator("#indent-level-selection").selectOption("2");
        page.locator("#customCssClass").fill("lessons-e2e-checklist");
        saveChecklist();

        Locator checklist = lessons.item(name);
        assertThat(checklist.locator(".checklistDiv")).hasClass(Pattern.compile("\\blessons-e2e-checklist\\b"));
        // Lessons turns the indent level into left padding once the page has loaded.
        assertThat(checklist.locator("[x-indent='2']")).hasAttribute("x-indent-done", "true");
    }

    /** Requires an item through its edit dialog, where the setting sits in the collapsed prerequisites section. */
    private void requireItem(String title) {
        Locator row = page.locator("#content a.itemlink").filter(new Locator.FilterOptions().setHasText(title)).first()
            .locator("xpath=ancestor::*[.//a[contains(@class,'edit-link')]][1]");
        row.hover();
        row.locator(".edit-col a.edit-link").first().click();
        Locator dialog = page.locator("#edit-item-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#prereqstuff button.accordionBtn").click();
        dialog.locator("#item-required").check();
        dialog.locator("#edit-item").click();
        assertThat(dialog).isHidden();
    }

    @Test
    void linkedChecklistEntryIsTickedWhenTheRequiredItemIsDone() {
        String title = newPage("Linked checklist");
        String subpage = "Reading " + sakai.randomId();
        lessons.addSubpage(subpage);
        sakai.toolClick(title);
        requireItem(subpage);

        String name = "Linked checklist " + sakai.randomId();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        assertThat(page.locator("#addContentDiv")).isVisible();
        page.locator("#addContentDiv .add-checklist-link").first().click();
        page.locator("#name").fill(name);
        page.locator("#addChecklistItemButton").click();
        page.locator("#checklistItemDiv1 > .checklist-item-name").fill("Do the reading");
        page.locator("#external-link-1").click();
        Locator linkDialog = page.locator("#externalLink-dialog");
        assertThat(linkDialog).isVisible();
        assertThat(linkDialog).containsText("It is only possible to link to required items.");
        linkDialog.locator(".checklistList-item").filter(new Locator.FilterOptions().setHasText(subpage))
            .locator("input[type=radio]").check();
        page.locator("#externalLink-dialog-submit").click();
        assertThat(linkDialog).isHidden();
        assertThat(page.locator("#external-link-unlink-1")).isVisible();
        saveChecklist();

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-checklist-link")) {
            sakai.login(student.username());
            page.navigate(siteUrl);
            sakai.toolClick(title);
            Locator entry = lessons.item(name).locator(".is-linked");
            Locator checkbox = entry.locator("input.checklist-checkbox");
            assertThat(entry.locator(".tooltip-content"))
                .containsText("will be automatically checked once '" + subpage + "' is completed");
            // Lessons ignores clicks on a linked entry.
            checkbox.click();
            assertThat(checkbox).not().isChecked();

            page.locator("#content a").filter(new Locator.FilterOptions().setHasText(subpage)).first().click();
            assertThat(page.locator("#subpage-breadcrumb-div")).containsText(subpage);
            sakai.toolClick(title);
            assertThat(checkbox).isChecked();
        }
    }

    @Test
    void cancelledEditKeepsTheChecklist() {
        newPage("Cancelled checklist");
        String name = "Original checklist " + sakai.randomId();
        lessons.addChecklist(name, "Submit the form");
        editChecklist(name);
        page.locator("#name").fill("Renamed checklist");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        assertThat(page.locator("#addcontent")).isAttached();

        assertThat(lessons.item(name).locator(".checklistTitle")).hasText(name);
        assertThat(lessons.item("Renamed checklist")).hasCount(0);
    }
}
