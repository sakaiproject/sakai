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
import com.microsoft.playwright.options.SelectOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class GroupImportTest extends SakaiUiTestBase {

    @ParameterizedTest
    @CsvSource({"sakai.assignment.grades,false", "sakai.assignment.grades,true", "sakai.samigo,false", "sakai.samigo,true"})
    void importingToolPreservesSectionAndJoinableGroupSettings(String toolId, boolean replace) {
        page.onDialog(dialog -> dialog.accept());
        sakai.login("instructor1");
        List<String> tools = List.of("sakai\\.sections", toolId.replace(".", "\\."));
        String sourceSite = sakai.createFreshCourse("instructor1", tools);
        String destinationSite = sakai.createFreshCourse("instructor1", tools);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String sectionTitle = "Import lab " + suffix;
        String retainedTitle = "Existing lab " + suffix;
        String setTitle = "Joinable " + suffix;
        String contentTitle = "Group import " + suffix;

        manageSectionsManually(sourceSite);
        addSection(sectionTitle, "Source room");
        addSection(retainedTitle, "Source settings must not overwrite destination");
        addSection("Unused lab " + suffix, "Must not be imported");
        addJoinableSet(sourceSite, setTitle);
        List<String> groups = List.of(sectionTitle, retainedTitle, setTitle + " 1");
        if (toolId.equals("sakai.assignment.grades")) {
            addAssignmentForGroups(contentTitle, groups);
        } else {
            addQuizForGroups(sourceSite, contentTitle, groups);
        }
        manageSectionsManually(destinationSite);
        addSection(retainedTitle, "Destination room");

        importTool(sourceSite, destinationSite, toolId, replace);
        awaitSection(destinationSite, sectionTitle);
        assertSectionDetails(sectionTitle, "Source room");
        assertSectionDetails(retainedTitle, "Destination room");
        assertThat(sectionRow("Unused lab " + suffix)).hasCount(0);
        assertAudience(destinationSite, toolId, contentTitle, groups);
        openGroups(destinationSite);
        assertThat(page.locator("tr").filter(new Locator.FilterOptions().setHas(
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(setTitle + " 1").setExact(true)))))
            .containsText("(7)");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(setTitle + " 1").setExact(true)).click();
        assertThat(page.locator("#groupAllowPreviewMembership")).isChecked();
        openGroups(destinationSite);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(setTitle).setExact(true)).click();
        assertThat(page.locator("#allowUnjoin")).isChecked();
        assertThat(page.locator("#joinableOpenDate")).hasValue("2020-01-01T09:00");
        assertThat(page.locator("#joinableCloseDate")).hasValue("2090-01-01T17:00");

        // A second import reuses the same groups instead of duplicating or overwriting them.
        page.navigate(sourceSite);
        sakai.toolClick("Section Info");
        String nextTitle = "Next lab " + suffix;
        addSection(nextTitle, "Second room");
        if (toolId.equals("sakai.assignment.grades")) {
            addAssignmentForGroups(contentTitle + " next", List.of(nextTitle));
        } else {
            addQuizForGroups(sourceSite, contentTitle + " next", List.of(nextTitle));
        }
        importTool(sourceSite, destinationSite, toolId, replace);
        awaitSection(destinationSite, nextTitle);
        assertSectionDetails(sectionTitle, "Source room");
        assertSectionDetails(retainedTitle, "Destination room");
        openGroups(destinationSite);
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(setTitle + " 1").setExact(true))).hasCount(1);
    }

    private void openGroups(String siteUrl) {
        page.navigate(siteUrl);
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Groups").setExact(true)).click();
    }

    private void addJoinableSet(String siteUrl, String title) {
        openGroups(siteUrl);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create New Joinable Set").setExact(true)).click();
        page.locator("#groupTitle").fill(title);
        page.locator("#groupNumber").fill("1");
        page.locator("#groupMaxMembers").fill("7");
        page.locator("#allowUnjoin").check();
        page.locator("#allowPreviewMembership").check();
        page.locator("#joinableOpenDate").fill("2020-01-01T09:00");
        page.locator("#joinableCloseDate").fill("2090-01-01T17:00");
        page.locator("#groupTitle").press("End");
        page.locator("#create-joinableset-submit-button").click();
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(title + " 1").setExact(true))).isVisible();
    }

    private void addQuizForGroups(String siteUrl, String title, List<String> groups) {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click();
        page.locator("#assessmentForm\\:parts\\:0\\:changeQType")
            .selectOption(new SelectOption().setLabel("True False"));
        page.locator("#itemForm\\:answerptr").fill("1");
        page.locator("#itemForm textarea").first().fill("Group import regression.");
        page.locator("#itemForm\\:TF input[type=radio]").first().check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first().click();
        page.locator("[id$=':editAssessmentSettings_editAssessment']").click();
        page.locator("#assessmentSettingsAction\\:releaseTo").selectOption("Selected Groups");
        selectGroups(page.locator("#assessmentSettingsAction\\:groupsForSite"), groups);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).click();
    }

    private void assertAudience(String siteUrl, String toolId, String title, List<String> groups) {
        page.navigate(siteUrl);
        if (toolId.equals("sakai.assignment.grades")) {
            sakai.toolClick("Assignments");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Edit " + title).setExact(true)).click();
            Locator selected = page.locator("#selectedGroups option:checked");
            assertThat(selected).hasCount(groups.size());
            for (String group : groups) {
                assertThat(selected.filter(new Locator.FilterOptions().setHasText(group))).hasCount(1);
            }
        } else {
            sakai.toolClick("Tests");
            Locator row = page.locator("#authorIndexForm\\:coreAssessments tbody tr")
                .filter(new Locator.FilterOptions().setHasText(title));
            row.getByRole(AriaRole.BUTTON).click();
            row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Settings").setExact(true)).click();
            assertThat(page.locator("#assessmentSettingsAction\\:releaseTo")).hasValue("Selected Groups");
            Locator selected = page.locator("#assessmentSettingsAction\\:groupsForSite option:checked");
            assertThat(selected).hasCount(groups.size());
            for (String group : groups) {
                assertThat(selected.filter(new Locator.FilterOptions().setHasText(group))).hasCount(1);
            }
        }
    }

    private void selectGroups(Locator select, List<String> groups) {
        String[] values = groups.stream().map(title -> select.locator("option")
            .filter(new Locator.FilterOptions().setHasText(title)).getAttribute("value")).toArray(String[]::new);
        select.selectOption(values, new Locator.SelectOptionOptions().setForce(true));
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

    private void addAssignmentForGroups(String title, List<String> groups) {
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
        selectGroups(page.locator("#selectedGroups"), groups);
        sakai.typeCkEditor("new_assignment_instructions", "<p>Section import regression.</p>");
        page.locator("input[name=save]:visible").click();
        assertThat(page.locator("body")).containsText(title);
    }

    private void importTool(String sourceSite, String destinationSite, String toolId, boolean replace) {
        page.navigate(destinationSite);
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Import from Site").setExact(true)).click();
        String mode = replace ? "replace my data" : "merge my data";
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
            .setName(Pattern.compile(mode, Pattern.CASE_INSENSITIVE))).click();
        String sourceSiteId = sakai.siteIdFromUrl(sourceSite);
        page.locator("input[name=importSites][value='" + sourceSiteId + "']").check();
        continueImport();
        assertThat(page.locator("input[id='toolSite-sakai.sections-" + sourceSiteId + "']")).hasCount(0);
        page.locator("input[id='toolSite-" + toolId + "-" + sourceSiteId + "']").check();
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
