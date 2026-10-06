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
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class SectionsImportTest extends SakaiUiTestBase {

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void importsManualSectionsWithMeetingDetailsAndPreservesExistingSections(boolean replace) {
        page.onDialog(dialog -> dialog.accept());
        sakai.login("instructor1");
        String sourceSite = sakai.createCourse("instructor1", List.of("sakai\\.sections", "sakai\\.assignment\\.grades"));
        String destinationSite = sakai.createCourse("instructor1", List.of("sakai\\.sections", "sakai\\.assignment\\.grades", "sakai\\.announcements"));
        String sectionTitle = "SAK-52946 Lab " + System.currentTimeMillis();
        String retainedTitle = "SAK-52946 Retained " + System.currentTimeMillis();

        manageSectionsManually(sourceSite);
        addSection(sectionTitle, "Room 52946");
        String assignmentTitle = "Assignment for " + sectionTitle;
        addAssignmentForSection(assignmentTitle, sectionTitle);
        manageSectionsManually(destinationSite);
        addSection(retainedTitle, "Existing room");

        importSections(sourceSite, destinationSite, replace);
        awaitSection(destinationSite, sectionTitle);
        assertSectionDetails(sectionTitle, "Room 52946");
        assertSectionDetails(retainedTitle, "Existing room");

        sakai.toolClick("Assignments");
        assertThat(page.locator("body")).containsText(assignmentTitle);

        // A new section lets us wait for the repeated import to finish before checking duplicates.
        page.navigate(sourceSite);
        sakai.toolClick("Section Info");
        String nextTitle = sectionTitle + " second import";
        addSection(nextTitle, "Second room");
        importSections(sourceSite, destinationSite, replace);
        awaitSection(destinationSite, nextTitle);
        assertThat(sectionRow(sectionTitle)).hasCount(1);
        assertThat(sectionRow(retainedTitle)).hasCount(1);
    }

    private void manageSectionsManually(String siteUrl) {
        page.navigate(siteUrl);
        sakai.toolClick("Section Info");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Options").setExact(true)).click();
        page.locator("input[type=radio][value=internal]").check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update").setExact(true)).click();
        Locator confirmation = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Confirm").setExact(true));
        if (confirmation.isVisible()) {
            confirmation.click();
        }
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Sections").setExact(true))).isVisible();
    }

    private void addSection(String title, String location) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Sections").setExact(true)).click();
        Locator category = page.locator("select[id$=':category']");
        String categoryValue = category.locator("option[value]:not([value=''])").first().getAttribute("value");
        category.selectOption(categoryValue);
        page.locator("input[id$=':titleInput']").fill(title);
        page.locator("input[type=radio][name$=':limit'][value=true]").check();
        page.locator("input[id$=':maxEnrollmentInput']").fill("25");
        page.locator("input[id$=':monday']").check();
        page.locator("input[id$=':location']").fill(location);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Sections").setExact(true)).click();
        assertSectionDetails(title, location);
    }

    private void addAssignmentForSection(String title, String sectionTitle) {
        sakai.toolClick("Assignments");
        page.locator(".navIntraTool a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^(Add|New)$")))
            .first().click();
        page.locator("#new_assignment_title").fill(title);
        Locator graded = page.locator("#gradeAssignment");
        if (graded.isChecked()) {
            graded.uncheck();
        }
        page.locator("#groups").check();
        String groupId = page.locator("#selectedGroups option")
            .filter(new Locator.FilterOptions().setHasText(sectionTitle)).first().getAttribute("value");
        page.locator("#selectedGroups").selectOption(groupId);
        sakai.typeCkEditor("new_assignment_instructions", "<p>Section import regression.</p>");
        page.locator("input[name=save]:visible").click();
        assertThat(page.locator("body")).containsText(title);
    }

    private void importSections(String sourceSite, String destinationSite, boolean replace) {
        page.navigate(destinationSite);
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Import from Site").setExact(true)).click();
        String mode = replace ? "replace my data" : "merge my data";
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
            .setName(Pattern.compile(mode, Pattern.CASE_INSENSITIVE))).click();
        String sourceSiteId = sakai.siteIdFromUrl(sourceSite);
        page.locator("input[name=importSites][value='" + sourceSiteId + "']").check();
        continueImport();
        Locator sectionsCheckbox = page.locator("input[id='toolSite-sakai.sections-" + sourceSiteId + "']");
        assertThat(sectionsCheckbox).isVisible();
        sectionsCheckbox.check();
        page.locator("input[id='toolSite-sakai.assignment.grades-" + sourceSiteId + "']").check();
        continueImport();
    }

    private void continueImport() {
        page.locator("input[type=submit][value*='Continue'], input[type=submit][value*='Finish'], #siteimport-finish-button")
            .first().click();
        page.waitForLoadState();
    }

    private void awaitSection(String siteUrl, String title) {
        page.navigate(siteUrl);
        sakai.toolClick("Section Info");
        Instant deadline = Instant.now().plusSeconds(60);
        while (sectionRow(title).count() == 0 && Instant.now().isBefore(deadline)) {
            page.waitForTimeout(500);
            page.reload();
        }
        assertThat(sectionRow(title)).hasCount(1);
    }

    private Locator sectionRow(String title) {
        return page.locator("table.sectionTable tr").filter(new Locator.FilterOptions()
            .setHas(page.getByText(title, new Page.GetByTextOptions().setExact(true))));
    }

    private void assertSectionDetails(String title, String location) {
        Locator row = sectionRow(title);
        assertThat(row).hasCount(1);
        assertThat(row).containsText(location);
        assertThat(row).containsText("25");
        assertThat(row).containsText("Mon");
    }
}
