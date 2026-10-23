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
import com.microsoft.playwright.Download;
import com.microsoft.playwright.options.FilePayload;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AttendanceTest extends SakaiUiTestBase {

    @Test
    void createsEditsAndDeletesEvent() {
        String siteUrl = openAttendance();
        String name = "Attendance & meeting " + sakai.randomId();
        createEvent(name);
        page.reload();
        Locator row = eventRow(name);
        assertThat(row).isVisible();

        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Actions")).click();
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Edit Attendance Item"))).isVisible();
        page.locator("input#name").fill(name + " updated");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Changes").setExact(true)).click();
        page.reload();
        Locator updated = eventRow(name + " updated");
        assertThat(updated).isVisible();

        updated.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Actions")).click();
        page.onceDialog(dialog -> dialog.accept());
        updated.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Delete").setExact(true)).click();
        assertThat(page.locator("#feedbackSpan")).containsText(name + " updated deleted successfully.");
        assertThat(page.locator("#overviewTable")).isVisible();
        assertThat(updated).hasCount(0);
        page.navigate(siteUrl);
        sakai.toolClick("Attendance");
        assertThat(page.locator("#overviewTable")).isVisible();
        assertThat(eventRow(name + " updated")).hasCount(0);
    }

    @Test
    void savesAttendanceAndShowsStudentsReadOnlyRecords() {
        String siteUrl = openAttendance();
        String name = "Attendance status " + sakai.randomId();
        createEvent(name);
        eventRow(name).getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(name).setExact(true)).click();
        assertThat(page.locator("#takeAttendanceTable tbody tr").first()).isVisible();
        page.locator("select[name$='set-all-status']").selectOption(new SelectOption().setLabel("Present"));
        assertThat(page.locator("#feedbackSpan")).containsText("set to PRESENT");
        page.reload();
        assertThat(presentRadio(page.locator("#takeAttendanceTable tbody tr").first())).isChecked();

        sakai.login("student0011");
        page.navigate(siteUrl);
        sakai.toolClick("Attendance");
        Locator studentRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(name));
        assertThat(studentRow).isVisible();
        assertThat(presentRadio(studentRow)).isChecked();
        assertThat(presentRadio(studentRow)).isDisabled();
        assertThat(page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Add").setExact(true))).hasCount(0);
    }

    @Test
    void validatesUploadsAndKeepsImportPagesRefreshable() throws Exception {
        openAttendance();
        String event = "Attendance import " + sakai.randomId();
        createEvent(event);
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Import/Export").setExact(true)).click();
        assertThat(page.locator("input[type=file]")).isVisible();
        page.reload();
        assertThat(page.locator("input[type=file]")).isVisible();

        // Exercise server-side validation even when the client disables the submit button.
        page.locator("input[type=file]").evaluate("input => input.form.requestSubmit()");
        assertThat(page.locator("#feedbackSpan")).containsText("No file was provided");
        page.locator("input[type=file]").setInputFiles(new FilePayload("bad.txt", "text/plain", "not excel".getBytes(StandardCharsets.UTF_8)));
        page.locator("#import-button").click();
        assertThat(page.locator("#feedbackSpan")).containsText("Please use a .xls file");
        page.locator("input[type=file]").setInputFiles(new FilePayload("empty.xls", "application/vnd.ms-excel", new byte[0]));
        page.locator("#import-button").click();
        assertThat(page.locator("#feedbackSpan")).containsText("contains no data");
        page.locator("input[type=file]").setInputFiles(new FilePayload("corrupt.xls", "application/vnd.ms-excel", "not excel".getBytes(StandardCharsets.UTF_8)));
        page.locator("#import-button").click();
        assertThat(page.locator("#feedbackSpan")).containsText("not the right format");

        Download download = page.waitForDownload(() -> page.locator("#exportSave").click());
        byte[] changedWorkbook;
        try (InputStream input = Files.newInputStream(download.path());
                HSSFWorkbook workbook = new HSSFWorkbook(input);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int eventColumn = -1;
            for (int column = 3; column < workbook.getSheetAt(0).getRow(0).getLastCellNum(); column++) {
                if (workbook.getSheetAt(0).getRow(0).getCell(column).getStringCellValue().contains(event)) {
                    eventColumn = column;
                    break;
                }
            }
            org.junit.jupiter.api.Assertions.assertTrue(eventColumn >= 3, "Export should include the new event");
            workbook.getSheetAt(0).getRow(1).getCell(eventColumn).setCellValue("P");
            workbook.write(output);
            changedWorkbook = output.toByteArray();
        }
        // Navigate through the tool again to clear prior validation feedback before a valid import.
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Attendance").setExact(true)).click();
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Import/Export").setExact(true)).click();
        page.locator("input[type=file]").setInputFiles(new FilePayload("attendance.XLS", "application/vnd.ms-excel", changedWorkbook));
        page.locator("#import-button").click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Changes identified in import file:"))).isVisible();
        assertThat(page.locator("#overviewTable tbody tr")).hasCount(1);
        page.reload();
        assertThat(page.locator("#overviewTable tbody tr")).hasCount(1);
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Complete Import").setExact(true))).isEnabled();
        // Change site settings after parsing, while the confirmation retains the older site model.
        try (Page settings = page.context().newPage()) {
            settings.navigate(page.url().split("/wicket/")[0] + "/");
            settings.locator(".navIntraTool").getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Settings").setExact(true)).click();
            settings.locator("input[name$='show-comments-to-students']").check();
            settings.locator("#settingsSave").click();
            settings.reload();
            assertThat(settings.locator("input[name$='show-comments-to-students']")).isChecked();

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Complete Import").setExact(true)).click();
            assertThat(page.locator("#feedbackSpan")).containsText("Attendance import process has been completed successfully");
            assertThat(eventRow(event)).isVisible();
            settings.locator(".navIntraTool").getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Attendance").setExact(true)).click();
            settings.locator(".navIntraTool").getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Settings").setExact(true)).click();
            assertThat(settings.locator("input[name$='show-comments-to-students']")).isChecked();
        }
        eventRow(event).getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(event).setExact(true)).click();
        assertThat(page.locator("#takeAttendanceTable .div-table-col:has(label:text-is('PRESENT')) input[type='radio']:checked")).hasCount(1);
    }

    @Test
    void keepsEventFormsOpenAfterFailedSaveAndAllowsRetry() {
        openAttendance();
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        String tooLong = "X".repeat(300);
        page.locator("input#name").evaluate("(input, value) => input.value = value", tooLong);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create").setExact(true)).click();
        assertThat(page.locator("input#name")).hasValue(tooLong);
        assertThat(page.locator("form .alert-warning")).containsText("Failed to save");
        String name = "Retried attendance " + sakai.randomId();
        page.locator("input#name").fill(name);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create").setExact(true)).click();
        Locator row = eventRow(name);
        assertThat(row).isVisible();
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Actions")).click();
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit").setExact(true)).click();
        page.locator("input#name").evaluate("(input, value) => input.value = value", tooLong);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Changes").setExact(true)).click();
        assertThat(page.locator("input#name")).hasValue(tooLong);
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Edit Attendance Item"))).isVisible();
        page.locator("input#name").fill(name + " edited");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Changes").setExact(true)).click();
        assertThat(eventRow(name + " edited")).isVisible();
    }

    @Test
    void informationPopupsFitNarrowViewport() throws Exception {
        page.setViewportSize(320, 640);
        String css = Files.readString(java.nio.file.Path.of("../attendance/tool/src/webapp/css/attendance.css"));
        page.setContent("<style>" + css + "</style><a href='#' class='statisticsInfo'>Info</a>"
                + "<div class='statisticsInfo'>Attendance statistics information</div>");
        page.locator("a.statisticsInfo").hover();
        assertThat(page.locator("div.statisticsInfo")).isVisible();
        com.microsoft.playwright.options.BoundingBox bounds = page.locator("div.statisticsInfo").boundingBox();
        org.junit.jupiter.api.Assertions.assertTrue(bounds.x >= 0 && bounds.x + bounds.width <= 320);
    }

    private String openAttendance() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.attendance"));
        page.navigate(siteUrl);
        sakai.toolClick("Attendance");
        assertThat(page.locator("#overviewTable")).isVisible();
        return siteUrl;
    }

    private void createEvent(String name) {
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("input#name").fill(name);
        page.locator("input#startDateTime").fill("2026-10-22T10:00");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create").setExact(true)).click();
        assertThat(eventRow(name)).isVisible();
    }

    private Locator eventRow(String name) {
        return page.locator("#overviewTable tbody tr").filter(new Locator.FilterOptions().setHasText(name));
    }

    private Locator presentRadio(Locator row) {
        return row.locator(".div-table-col:has(label:text-is('PRESENT')) input[type='radio']");
    }
}
