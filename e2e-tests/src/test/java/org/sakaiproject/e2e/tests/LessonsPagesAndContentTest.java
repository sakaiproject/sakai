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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for Lessons pages and their basic content. Each test works on its own page. */
class LessonsPagesAndContentTest extends SakaiUiTestBase {

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
        lessons.open(siteUrl);
    }

    private TestUser newStudent(String prefix) {
        return lessons.newStudent(siteUrl, prefix);
    }

    private String newPage(String prefix) {
        String title = prefix + " " + sakai.randomId();
        lessons.createTopLevelPage(title);
        return title;
    }

    @Test
    void instructorAddsTextFileAndWebLink() {
        newPage("Content");

        lessons.addText("<p>Welcome to the unit</p>");
        assertThat(page.locator(".item.textType .textbox")).containsText("Welcome to the unit");

        lessons.uploadTextFile("Lesson notes", "lesson-notes.txt", "Lesson notes body");
        Locator file = lessons.item("Lesson notes").locator("a.itemlink");
        assertThat(file).hasAttribute("target", "_blank");
        APIResponse download = page.request().get(file.getAttribute("href"));
        try {
            assertEquals(200, download.status());
            assertEquals("Lesson notes body", download.text());
        } finally {
            download.dispose();
        }

        String url = page.url().replaceFirst("(https?://[^/]+).*", "$1/portal/");
        lessons.addUrl("Portal link", url);
        Locator link = lessons.item("Portal link").locator("a.itemlink");
        assertThat(link).hasAttribute("target", "_blank");
        assertThat(lessons.items()).hasCount(3);
    }

    @Test
    void subpageTitleRejectsReservedCharacters() {
        newPage("Invalid subpage");

        Locator dialog = lessons.openSubpageDialog("Bad|title");
        lessons.submitSubpage(dialog);

        assertThat(dialog.locator("#subpage-error")).containsText("may not contain characters");
        assertThat(dialog).isVisible();
    }

    @Test
    void subpageShowsBreadcrumbAndReturnsToParent() {
        String parent = newPage("Parent");

        lessons.addSubpage("Child page");

        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(parent);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back").setExact(true)).first().click();
        assertThat(page.locator(".neoPortletTitleWrap")).containsText(parent);
        assertThat(lessons.item("Child page")).hasCount(1);
    }

    @Test
    void nextPageSubpageHasNoBreadcrumbLevel() {
        newPage("Sequence");

        Locator dialog = lessons.openSubpageDialog("Next step");
        dialog.locator("#subpage-next").check();
        lessons.submitSubpage(dialog);

        assertThat(page.locator(".neoPortletTitleWrap")).containsText("Next step");
        assertThat(page.locator("#subpage-breadcrumb-div")).not().containsText("Sequence");
    }

    @Test
    void subpageCanBeShownAsColoredButton() {
        String parent = newPage("Button");

        Locator dialog = lessons.openSubpageDialog("Blue button");
        dialog.locator("#subpage-button").check();
        dialog.locator("#subpage-btncolor-selection").selectOption("nblue");
        lessons.submitSubpage(dialog);
        assertThat(page.locator("#subpage-breadcrumb-div")).containsText("Blue button");

        sakai.toolClick(parent);
        assertThat(lessons.item("Blue button").locator("a.usebutton.nblue")).hasCount(1);
    }

    @Test
    void copyingItemsLeavesTheSourcePageUntouched() {
        String source = newPage("Source");
        lessons.addText("<p>Keep this text</p>");
        lessons.addText("<p>Leave this text</p>");

        String target = newPage("Target");
        lessons.startCopyFrom(source);
        lessons.reorderItem("Leave this text").locator("a.deleteAnswerTrashLink").click();
        lessons.saveReorder();

        assertThat(page.locator(".neoPortletTitleWrap")).containsText(target);
        assertThat(lessons.items()).hasCount(1);
        assertThat(lessons.items().first()).containsText("Keep this text");

        sakai.toolClick(source);
        assertThat(lessons.items()).hasCount(2);
        assertThat(lessons.item("Leave this text")).hasCount(1);
    }

    @Test
    void embedCodeIsShownInline() {
        newPage("Embed");

        lessons.addEmbed("<iframe title=\"Embedded frame\" src=\"/library/content/gateway/welcome.html\"></iframe>");

        assertThat(page.locator(".portletBody .item iframe[title='Embedded frame']")).isAttached();
    }

    @Test
    void itemsCanBeReorderedWithTheKeyboard() {
        newPage("Reorder");
        lessons.addText("<p>First entry</p>");
        lessons.addText("<p>Second entry</p>");

        lessons.openReorder();
        sakai.waitForPortalFocus();
        Locator second = lessons.reorderItem("Second entry");
        assertThat(second).hasAttribute("draggable", "true");
        second.press("Control+ArrowUp");
        assertThat(page.locator("#reorderCol1 .reorder-item").first()).containsText("Second entry");
        lessons.saveReorder();

        assertThat(lessons.items().first()).containsText("Second entry");
        assertThat(lessons.items().nth(1)).containsText("First entry");
    }

    @Test
    void renamingAPageUpdatesTheSiteNavigation() {
        String original = newPage("Original");
        String renamed = "Renamed " + sakai.randomId();

        lessons.rename(renamed);

        page.reload();
        assertThat(lessons.navigationLink(renamed)).hasCount(1);
        assertThat(lessons.navigationLink(original)).hasCount(0);
    }

    @Test
    void indexListsThePagesOfTheSite() {
        String title = newPage("Indexed");

        page.locator("#show-pages").click();

        assertThat(page.locator(".portletBody")).containsText("Index of pages");
        assertThat(page.locator(".portletBody")).containsText(title);
    }

    @Test
    void printViewOpensTheBrowserPrintDialog() {
        newPage("Print");
        page.evaluate("() => { window.printCalls = 0; window.print = () => window.printCalls++; }");

        page.locator("#print-view").click();

        assertEquals(1, ((Number) page.evaluate("window.printCalls")).intValue());
    }

    @Test
    void studentSeesContentWithoutEditingTools() {
        String title = newPage("Student view");
        lessons.addText("<p>Visible to students</p>");

        try (TestUser student = newStudent("lessons-student-view")) {
            sakai.login(student.username());
            page.navigate(siteUrl);
            sakai.toolClick(title);

            assertThat(page.locator(".item.textType .textbox")).containsText("Visible to students");
            assertThat(page.locator(".item.canEdit")).hasCount(0);
            for (String tool : List.of("#addcontent", "#reorder-button", "#edit-title", "#bulk-edit-pages-button")) {
                assertThat(page.locator(tool)).not().isVisible();
            }
            assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("More Tools"))).hasCount(0);
        }
    }

    @Test
    void studentChecklistProgressSurvivesReload() {
        String title = newPage("Checklist");
        lessons.addChecklist("Weekly checklist", "Read the notes", "Do the exercise");

        try (TestUser student = newStudent("lessons-checklist")) {
            sakai.login(student.username());
            page.navigate(siteUrl);
            sakai.toolClick(title);
            Locator boxes = lessons.item("Weekly checklist").locator("input[type=checkbox].checklist-checkbox");
            assertThat(boxes).hasCount(2);
            lessons.tickChecklistEntry(boxes.first());

            page.reload();
            assertThat(boxes.first()).isChecked();
            assertThat(boxes.nth(1)).not().isChecked();
        }
    }

    @Test
    void permissionChangeGrantsAndRevokesStudentEditing() {
        String permissionsSite = sakai.createFreshCourse(INSTRUCTOR, TOOLS);
        try (TestUser student = lessons.newStudent(permissionsSite, "lessons-permissions")) {
            lessons.open(permissionsSite);

            lessons.openPermissions();
            assertThat(lessons.studentCanUpdate()).not().isChecked();
            lessons.studentCanUpdate().check();
            lessons.savePermissions();

            sakai.login(student.username());
            lessons.open(permissionsSite);
            assertThat(page.locator("#addcontent")).isVisible();

            sakai.login(INSTRUCTOR);
            lessons.open(permissionsSite);
            lessons.openPermissions();
            assertThat(lessons.studentCanUpdate()).isChecked();
            lessons.studentCanUpdate().uncheck();
            lessons.savePermissions();

            sakai.login(student.username());
            lessons.open(permissionsSite);
            assertThat(page.locator("#addcontent")).not().isVisible();
        }
    }
}
