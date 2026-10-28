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
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.FilePayload;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class DateManagerTest extends SakaiUiTestBase {

    @Test
    void dateColumnsMatchSiteTools() {
        sakai.login("instructor1");

        // The "Bulk Term Date Matrix" term-date matrix renders one checkbox per (tool, date column) present in the
        // site - driven by tool presence (not by whether the tool has items), so it is the surface that
        // reflects "which date fields exist for this site's tools". Each checkbox carries
        // data-root='collapse-<tool>' and data-field='<column>'; we scope to one term row for a count of 1.
        // The panel is collapsed by default, so we assert presence in the DOM rather than visibility.
        String gradebookResourcesSite = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng", "sakai\\.resources"));
        page.navigate(gradebookResourcesSite);
        openDateManager();

        assertTermColumnPresent("gradebook", "due_date");
        assertTermColumnPresent("resources", "open_date");
        assertTermColumnPresent("resources", "due_date");
        assertTermColumnAbsent("assignments", "open_date");
        assertTermColumnAbsent("assignments", "accept_until");
        assertTermColumnAbsent("assessments", "feedback_start");
        assertTermColumnAbsent("signup", "signup_begins");

        String assignmentsSite = sakai.createCourse("instructor1",
            List.of("sakai\\.gradebookng", "sakai\\.resources", "sakai\\.assignment\\.grades"));
        page.navigate(assignmentsSite);
        openDateManager();

        assertTermColumnPresent("assignments", "open_date");
        assertTermColumnPresent("assignments", "due_date");
        assertTermColumnPresent("assignments", "accept_until");
        assertTermColumnAbsent("assessments", "feedback_start");
        assertTermColumnAbsent("signup", "signup_begins");

        String gradebookOnlySite = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng"));
        page.navigate(gradebookOnlySite);
        openDateManager();

        assertTermColumnPresent("gradebook", "due_date");
        assertTermColumnAbsent("gradebook", "open_date");
        assertTermColumnAbsent("assignments", "open_date");

        String assessmentsSite = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        page.navigate(assessmentsSite);
        openDateManager();

        assertTermColumnPresent("assessments", "open_date");
        assertTermColumnPresent("assessments", "due_date");
        assertTermColumnPresent("assessments", "accept_until");
        assertTermColumnPresent("assessments", "feedback_start");
        assertTermColumnPresent("assessments", "feedback_end");
        assertTermColumnAbsent("signup", "signup_begins");

        String signupSite = sakai.createCourse("instructor1", List.of("sakai\\.signup"));
        page.navigate(signupSite);
        openDateManager();

        assertTermColumnPresent("signup", "open_date");
        assertTermColumnPresent("signup", "due_date");
        assertTermColumnPresent("signup", "signup_begins");
        assertTermColumnPresent("signup", "signup_deadline");
        assertTermColumnAbsent("assignments", "accept_until");
        assertTermColumnAbsent("assessments", "feedback_start");

        String lessonsSite = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));
        page.navigate(lessonsSite);
        openDateManager();

        assertTermColumnPresent("lessons", "open_date");
        assertTermColumnAbsent("lessons", "due_date");
        assertTermColumnAbsent("assignments", "open_date");

        String dashboardOnlySite = sakai.createCourse("instructor1", List.of("sakai\\.dashboard"));
        page.navigate(dashboardOnlySite);
        openDateManager();

        // No dated tools -> the term matrix has no columns at all.
        assertThat(page.locator(".term-target")).hasCount(0);
    }

    @Test
    void visualPreviewOpensCalendar() {
        sakai.login("instructor1");

        // Any site with a dated tool renders the footer actions; the Visual Preview button and its
        // read-only calendar modal are wired regardless of whether the site has any dated items yet.
        String site = sakai.createCourse("instructor1", List.of("sakai\\.gradebookng", "sakai\\.resources"));
        page.navigate(site);
        openDateManager();

        Locator previewButton = page.locator("#datemanager-preview");
        assertThat(previewButton).isVisible();

        previewButton.click();

        // The modal opens (its title resolves via i18n) and mounts either the FullCalendar month grid
        // or the empty-state message, depending on whether the site has any dated items.
        assertThat(page.locator("#modal-calendar-preview")).isVisible();
        assertThat(page.locator("#datemanager-preview-title")).isVisible();
    }

    @Test
    void csvImportClearsAnnouncementStartDateWithoutChangingEndDate() throws Exception {
        sakai.login("instructor1");
        String site = sakai.createCourse("instructor1", List.of("sakai\\.announcements"));
        page.navigate(site);
        sakai.toolClick("Announcements");
        page.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Add")).first().click();
        String title = "CSV date clearing regression";
        page.locator("#subject").fill(title);
        if (!sakai.typeCkEditorIfPresent("body", "<p>CSV date clearing regression.</p>")) {
            page.locator("textarea#body").fill("CSV date clearing regression.");
        }
        page.locator("#hidden_specify").check();
        page.locator("#use_start_date").check();
        sakai.selectDate("#opendate", "06/01/2035 08:30 am");
        page.locator("#use_end_date").check();
        sakai.selectDate("#closedate", "06/03/2035 08:30 am");
        page.locator("#saveChanges").click();
        assertThat(page.locator("table tr").filter(new Locator.FilterOptions().setHasText(title))).hasCount(1);
        openDateManager();

        Locator row = page.locator("#collapse-announcements tbody tr")
            .filter(new Locator.FilterOptions().setHasText(title));
        assertThat(row).hasCount(1);
        String endDate = row.locator("input[data-field='due_date']").inputValue();
        assertFalse(row.locator("input[data-field='open_date']").inputValue().isBlank());
        assertFalse(endDate.isBlank());

        Download download = page.waitForDownload(() -> page.locator("#exportDates").click());
        List<String> lines = Files.readAllLines(download.path(), StandardCharsets.UTF_8);
        int changedRows = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("\"" + title + "\"")) {
                String[] columns = lines.get(i).split(",", -1);
                assertEquals(4, columns.length);
                columns[2] = "\"\"";
                lines.set(i, String.join(",", columns));
                changedRows++;
            }
        }
        assertEquals(1, changedRows);
        String csv = String.join("\n", lines) + "\n";
        page.locator("#importDates").click();
        page.locator("#file").setInputFiles(new FilePayload("dates.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)));
        page.locator("#submit-form-button").click();
        assertThat(page.locator(".table-datemanager td").filter(new Locator.FilterOptions().setHasText(title))).hasCount(1);
        page.locator("#submit-form-button").click();
        assertThat(page.locator("#exportDates")).isVisible();
        assertThat(row.locator("input[data-field='open_date']")).hasValue("");
        assertThat(row.locator("input[data-field='due_date']")).hasValue(endDate);
        page.reload();
        assertThat(row.locator("input[data-field='open_date']")).hasValue("");
        assertThat(row.locator("input[data-field='due_date']")).hasValue(endDate);
    }

    private void openDateManager() {
        sakai.toolClick("Site Info");
        Locator dateManager = page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Date Manager$", Pattern.CASE_INSENSITIVE)))
            .first();
        assertThat(dateManager).isVisible();
        dateManager.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        assertNoTemplateRenderingError();
    }

    private Locator termColumn(String toolRoot, String field) {
        // Scope to a single term row (classes_start) so a present column matches exactly one checkbox.
        return page.locator(".term-target[data-root='collapse-" + toolRoot + "']"
            + "[data-field='" + field + "'][data-term='classes_start']");
    }

    private void assertTermColumnPresent(String toolRoot, String field) {
        assertThat(termColumn(toolRoot, field)).hasCount(1);
    }

    private void assertTermColumnAbsent(String toolRoot, String field) {
        assertThat(termColumn(toolRoot, field)).hasCount(0);
    }

    private void assertNoTemplateRenderingError() {
        String bodyText = page.locator("body").textContent();
        Pattern templateError = Pattern.compile(
            "TemplateProcessingException|TemplateOutputException|An error happened during template rendering",
            Pattern.CASE_INSENSITIVE
        );
        assertFalse(templateError.matcher(bodyText == null ? "" : bodyText).find(), "Date Manager rendered a Thymeleaf error");
    }
}
