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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for Lessons layouts and item editing. Each test works on its own page. */
class LessonsLayoutTest extends SakaiUiTestBase {

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

    @Test
    void sectionLayoutShowsTitleAndColor() {
        newPage("Section");

        Locator form = lessons.openSectionLayout("Blue section");
        form.locator("#layout-color-scheme-selection").selectOption("nblue");
        lessons.submitLayout(form);

        assertThat(lessons.sectionHeader("Blue section")).containsClass("colnblue-header");
        assertThat(lessons.columns(lessons.sections().first()).first()).containsClass("colnblue");
    }

    @Test
    void columnLayoutsCreateTheExpectedColumns() {
        newPage("Columns");

        assertLayout("single-column", 1, -1);
        assertLayout("two-equal", 2, -1);
        assertLayout("left-double", 2, 0);
        assertLayout("right-double", 2, 1);
        assertLayout("three-equal", 3, -1);
    }

    private void assertLayout(String layout, int columnCount, int doubleColumn) {
        Locator form = lessons.openSectionLayout("Layout " + layout);
        form.locator("input[type=radio][value='" + layout + "']").check();
        lessons.submitLayout(form);

        Locator section = page.locator(".sectionHeader")
            .filter(new Locator.FilterOptions().setHasText("Layout " + layout))
            .locator("xpath=following-sibling::*[contains(concat(' ', normalize-space(@class), ' '), ' section ')][1]");
        Locator columns = lessons.columns(section);
        assertThat(columns).hasCount(columnCount);
        for (int i = 0; i < columnCount; i++) {
            if (i == doubleColumn) {
                assertThat(columns.nth(i)).containsClass("double");
            } else {
                assertThat(columns.nth(i)).not().containsClass("double");
            }
        }
    }

    @Test
    void collapsibleSectionCollapsesAndExpands() {
        newPage("Collapsible");
        lessons.addText("<p>Inside the section</p>");

        Locator column = lessons.columns(lessons.sections().first()).first();
        Locator settings = lessons.openColumnSettings(column);
        // Typing fires the key event that enables the collapsible option.
        lessons.typeTitle(settings.locator("#sectionTitle"), "Collapsible section");
        settings.locator("#collapsible").check();
        lessons.saveColumnSettings(settings);

        Locator header = lessons.sectionHeader("Collapsible section");
        Locator text = page.getByText("Inside the section");
        assertThat(text).isVisible();
        header.click();
        assertThat(header).hasAttribute("aria-expanded", "false");
        assertThat(text).isHidden();
        header.click();
        assertThat(header).hasAttribute("aria-expanded", "true");
        assertThat(text).isVisible();
    }

    @Test
    void sectionStartingCollapsedOpensForStudents() {
        String title = newPage("Start collapsed");
        Locator form = lessons.openSectionLayout("Closed section");
        form.locator("#layout-section-collapsible").check();
        lessons.submitLayout(form);
        Locator column = lessons.columns(lessons.sections().first()).first();
        lessons.addTextFrom(column.locator(".add-bottom"), "<p>Revealed text</p>");
        Locator settings = lessons.openColumnSettings(column);
        settings.locator("#defaultClosed").check();
        lessons.saveColumnSettings(settings);

        try (TestUser student = newStudent("lessons-collapsed")) {
            sakai.login(student.username());
            page.navigate(siteUrl);
            sakai.toolClick(title);

            Locator header = lessons.sectionHeader("Closed section");
            Locator text = page.getByText("Revealed text");
            assertThat(header).hasAttribute("aria-expanded", "false");
            assertThat(text).isHidden();
            header.click();
            assertThat(text).isVisible();
        }
    }

    @Test
    void headingColorOverridesSubpageButtonColor() {
        String title = newPage("Forced color");
        Locator form = lessons.openSectionLayout("Heading color");
        form.locator("#layout-color-scheme-selection").selectOption("nblue");
        form.locator("#layout-section-force-button-color").check();
        lessons.submitLayout(form);

        Locator column = lessons.columns(lessons.sections().first()).first();
        Locator dialog = lessons.openSubpageDialogFrom(column.locator(".add-bottom"), "Green button");
        dialog.locator("#subpage-button").check();
        dialog.locator("#subpage-btncolor-selection").selectOption("ngreen");
        lessons.submitSubpage(dialog);
        assertThat(page.locator("#subpage-breadcrumb-div")).containsText("Green button");

        sakai.toolClick(title);
        Locator item = lessons.item("Green button");
        assertThat(item.locator("a.usebutton.nblue")).hasCount(1);
        assertThat(item.locator("a.usebutton.ngreen")).hasCount(0);
    }

    @Test
    void pageLayoutCreatesASubpageListing() {
        newPage("Listing");

        lessons.addSubpageListing("Week", 3, "nteal");

        assertThat(lessons.items()).hasCount(3);
        for (int week = 1; week <= 3; week++) {
            assertThat(lessons.item("Week " + week).locator("a.usebutton.nteal")).hasCount(1);
        }
    }

    @Test
    void itemsAreAddedToTheChosenColumn() {
        newPage("Column items");
        Locator form = lessons.openSectionLayout("Two columns");
        form.locator("input[type=radio][value='two-equal']").check();
        lessons.submitLayout(form);

        Locator second = lessons.columns(lessons.sections().first()).nth(1);
        lessons.addTextFrom(second.locator(".add-bottom"), "<p>Second column text</p>");

        assertThat(second).containsText("Second column text");
        assertThat(lessons.columns(lessons.sections().first()).first()).not().containsText("Second column text");
    }

    @Test
    void itemsCanBeInsertedAboveAndDeleted() {
        newPage("Item editing");
        lessons.addText("<p>First text</p>");
        lessons.addText("<p>Last text</p>");

        lessons.addTextFrom(lessons.addAboveButton(lessons.item("Last text")), "<p>Middle text</p>");

        assertThat(lessons.items()).hasCount(3);
        assertThat(lessons.items().nth(1)).containsText("Middle text");
        assertThat(lessons.items().nth(2)).containsText("Last text");

        lessons.deleteItem(lessons.item("First text"));

        page.reload();
        assertThat(lessons.items()).hasCount(2);
        assertThat(lessons.items().first()).containsText("Middle text");
    }

    @Test
    void columnSettingsPersistAfterReload() {
        newPage("Column settings");
        Locator form = lessons.openSectionLayout("Original title");
        lessons.submitLayout(form);

        Locator column = lessons.columns(lessons.sections().first()).first();
        Locator settings = lessons.openColumnSettings(column);
        settings.locator("#sectionTitle").fill("Renamed title");
        settings.locator("#columndouble").check();
        settings.locator("#columnsplit").check();
        settings.locator("#columnbackground").selectOption("norange");
        settings.locator("#show-borders").uncheck();
        lessons.saveColumnSettings(settings);

        page.reload();
        assertThat(lessons.sectionHeader("Renamed title")).containsClass("colnorange-trans-header");
        assertThat(column).containsClass("double");
        assertThat(column).containsClass("split");
        assertThat(column).containsClass("colnorange-trans");
    }

    private TestUser newStudent(String prefix) {
        return lessons.newStudent(siteUrl, prefix);
    }
}
