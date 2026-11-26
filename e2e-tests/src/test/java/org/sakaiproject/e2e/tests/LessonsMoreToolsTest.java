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
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for the page tools of Lessons: adding, editing, removing and deleting pages. */
class LessonsMoreToolsTest extends SakaiUiTestBase {

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

    private String newPage(String prefix) {
        String title = prefix + " " + sakai.randomId();
        lessons.createTopLevelPage(title);
        return title;
    }

    private Locator openAddPages() {
        lessons.clickMoreTools("Add More Pages");
        Locator dialog = page.locator("#new-page-dialog");
        assertThat(dialog).isVisible();
        return dialog;
    }

    private void saveAddPages(Locator dialog) {
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save").setExact(true)).click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void removeCurrentPage() {
        lessons.clickMoreTools("Remove Page");
        Locator dialog = page.locator("#remove-page-dialog");
        assertThat(dialog).isVisible();
        assertThat(dialog).containsText("This does not delete the page.");
        page.waitForNavigation(() -> dialog.locator("#remove-page-submit").click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void markRequired(String item, String option) {
        Locator editor = lessons.openItemEditor(lessons.item(item));
        editor.locator("#prereqstuff button.accordionBtn").click();
        editor.locator(option).check();
        lessons.saveItemEditor(editor);
    }

    @Test
    void addMorePagesCreatesNumberedPages() {
        // Lessons numbers the pages from the first number in the title, so the title has no other digits.
        String prefix = "Copy Page ";
        Locator dialog = openAddPages();
        dialog.locator("#newPage").fill(prefix + "1");
        dialog.locator("#new-page-number").fill("2");
        saveAddPages(dialog);

        assertThat(lessons.navigationLink(prefix + "1")).hasCount(1);
        assertThat(lessons.navigationLink(prefix + "2")).hasCount(1);
    }

    @Test
    void copiedPagesKeepTheContentButNotTheSubpages() {
        String original = newPage("Main");
        lessons.addText("<p>Content of the main page</p>");
        String subpage = "Main subpage " + sakai.randomId();
        lessons.addSubpage(subpage);
        sakai.toolClick(original);
        assertThat(lessons.item(subpage)).isVisible();

        String copy = "Copy Main " + sakai.randomId();
        Locator dialog = openAddPages();
        dialog.locator("#newPage").fill(copy);
        dialog.locator("#new-page-copy").check();
        saveAddPages(dialog);

        sakai.toolClick(copy);
        assertThat(page.getByText("Content of the main page")).isVisible();
        assertThat(lessons.item(subpage)).hasCount(0);
    }

    @Test
    void bulkEditRetitlesTheSubpagesAndTheirDescriptions() {
        String title = newPage("Bulk edit");
        String first = "Unit A " + sakai.randomId();
        String second = "Unit B " + sakai.randomId();
        lessons.addSubpage(first);
        sakai.toolClick(title);
        lessons.addSubpage(second);
        sakai.toolClick(title);

        page.waitForNavigation(() -> page.locator("#bulk-edit-pages-button").click());
        Locator rows = page.locator("tr.subpage-item");
        assertThat(rows).hasCount(2);
        Locator firstRow = rows.filter(new Locator.FilterOptions().setHasText(first));
        firstRow.locator(".subpage-item-title").fill(first + " renamed");
        firstRow.locator(".subpage-item-description").fill("Description of the first unit");
        page.waitForNavigation(() -> page.locator("#save").click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        assertThat(lessons.item(first + " renamed")).containsText("Description of the first unit");
        assertThat(lessons.item(second)).isVisible();
    }

    @Test
    void removedPagesCanBeDeletedPermanentlyFromTheIndex() {
        String removed = newPage("Removed");
        lessons.addText("<p>Content of a removed page</p>");
        removeCurrentPage();
        assertThat(lessons.navigationLink(removed)).hasCount(0);

        lessons.open(siteUrl);
        page.waitForNavigation(() -> page.locator("#show-pages").click());
        Locator removedPage = page.locator(".removed-page").filter(new Locator.FilterOptions().setHasText(removed));
        assertThat(removedPage).isVisible();
        removedPage.locator("input.deletebox").check();
        page.locator("#delete-button").click();
        Locator confirm = page.locator("#delete-pages-dialog");
        assertThat(confirm).isVisible();
        assertThat(confirm).containsText(removed);
        page.waitForNavigation(() -> confirm.locator("#delete-confirm-submit").click());
        assertThat(page.locator(".sak-banner-success")).containsText("Deleted one page permanently.");
        assertThat(page.locator(".removed-page").filter(new Locator.FilterOptions().setHasText(removed))).hasCount(0);
    }

    @Test
    void teachingAssistantsCanSeeItemsRegardlessOfReleaseControls() {
        String title = newPage("Release controls");
        String reading = "Required reading " + sakai.randomId();
        String locked = "Locked unit " + sakai.randomId();
        lessons.addSubpage(reading);
        lessons.addText("<p>Text of the required reading</p>");
        sakai.toolClick(title);
        lessons.addSubpage(locked);
        lessons.addText("<p>Text of the locked unit</p>");
        sakai.toolClick(title);
        markRequired(reading, "#item-required");
        markRequired(locked, "#item-prerequisites");
        assertThat(lessons.item(locked)).containsText("Has prerequisites");

        lessons.openPermissions();
        Locator seeAll = page.locator("[id='Teaching Assistantlessonbuilder.seeall']");
        seeAll.check();
        lessons.savePermissions();

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-seeall-student");
                TestUser assistant = sakai.createUser("lessons-seeall-ta")) {
            sakai.addMember(siteUrl, assistant.username(), "Teaching Assistant");

            sakai.login(student.username());
            page.navigate(siteUrl);
            sakai.toolClick(title);
            assertThat(lessons.item(locked).locator("a.itemlink")).hasCount(0);

            sakai.login(assistant.username());
            page.navigate(siteUrl);
            sakai.toolClick(title);
            assertThat(lessons.item(locked)).containsText(Pattern.compile("Has prerequisites"));
            lessons.openSubpageFromParent(locked);
            assertThat(page.getByText("Text of the locked unit")).isVisible();
        }
    }
}
