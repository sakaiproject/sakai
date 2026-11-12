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

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AssignmentUploadAllTest extends SakaiUiTestBase {

    @TempDir
    Path temporaryDirectory;

    @Test
    void fullArchiveCanBeUploadedRepeatedlyBeforeReleasingFeedback() throws IOException {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));
        String title = "Full archive feedback " + sakai.randomId();
        String feedback = "Please explain your conclusion.";
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(title);
        page.locator("#gradeAssignment").check();
        page.locator("#new_assignment_grade_points").fill("100");
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions()
            .setName("Send notification email to student when the grade is released").setExact(true)).check();
        sakai.typeCkEditor("new_assignment_instructions", "<p>Submit an answer.</p>");
        page.locator("input[name=post]").click();
        assertThat(page.locator("#assignments-list")).containsText(title);

        try (TestUser grader = sakai.createUser("upload-all-grader")) {
            sakai.addMember(siteUrl, grader.username(), "Instructor");
            for (String student : List.of("student0011", "student0012")) {
                sakai.addMember(siteUrl, student, "Student");
                sakai.login(student);
                openAssignment(siteUrl, title);
                sakai.typeCkEditor("Assignment.view_submission_text", "<p>Original answer by " + student + ".</p>");
                page.locator("#confirm").click();
                page.locator("#post").click();
                assertThat(page.locator("body")).containsText("Submission Confirmation");
            }

            sakai.login(grader.username());
            sakai.setAssignmentGrader(false);
            openSubmissionList(siteUrl, title);
            studentRow("student0011").getByRole(AriaRole.LINK).first().click();
            page.locator("#grade").fill("86");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                .setName("Save and Release to Student").setExact(true)).click();
            openSubmissionList(siteUrl, title);
            assertThat(studentRow("student0011")).containsText("Returned");
            String submissionDate = studentRow("student0011").locator("td[headers=submitted]").innerText();

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Download All").setExact(true)).click();
            page.locator("#selectall").check();
            Download download = page.waitForDownload(() -> page.locator("#downloadButton").click());
            Path originalArchive = temporaryDirectory.resolve("download.zip");
            download.saveAs(originalArchive);
            Path feedbackArchive = temporaryDirectory.resolve("feedback.zip");
            addFeedback(originalArchive, feedbackArchive, sakai.resolveUsername("student0012"), feedback);

            for (int upload = 0; upload < 2; upload++) {
                openSubmissionList(siteUrl, title);
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Upload All").setExact(true)).click();
                page.locator("#file").setInputFiles(feedbackArchive);
                page.locator("#selectall").check();
                assertThat(page.locator("#release_false")).isChecked();
                page.locator("#uploadButton").click();
                assertThat(studentRow("student0011").locator("td[headers=gradereleased] .si-check")).hasCount(0);
                assertThat(studentRow("student0012").locator("td[headers=gradereleased] .si-check")).hasCount(0);
                assertThat(studentRow("student0011").locator("td[headers=submitted]")).hasText(submissionDate);
            }

            studentRow("student0011").getByRole(AriaRole.LINK).first().click();
            assertThat(page.locator("#grade")).hasValue(Pattern.compile("86(?:\\.0+)?"));
            assertThat(page.frameLocator("iframe[title='Editor, grade_submission_feedback_text']").locator("body"))
                .containsText("Original answer by student0011");

            sakai.login("student0012");
            openAssignment(siteUrl, title);
            assertThat(page.locator("body")).not().containsText(feedback);
            sakai.login(grader.username());
            openSubmissionList(siteUrl, title);
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Release Grades").setExact(true)).click();
            assertThat(studentRow("student0011")).containsText("Returned");
            assertThat(studentRow("student0012")).containsText("Returned");
            sakai.login("student0012");
            openAssignment(siteUrl, title);
            assertThat(page.locator("body")).containsText(feedback);
        }
    }

    private void addFeedback(Path original, Path updated, String student, String feedback) throws IOException {
        String studentDirectory = null;
        boolean hasSubmissionText = false;
        try (ZipInputStream input = new ZipInputStream(Files.newInputStream(original));
                ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(updated))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                String name = entry.getName();
                hasSubmissionText |= name.contains("_submissionText");
                if (name.contains("(" + student + ")/")) {
                    studentDirectory = name.substring(0, name.indexOf(")/") + 2);
                }
                if (name.contains("(" + student + ")/") && name.endsWith("/comments.txt")) {
                    continue;
                }
                output.putNextEntry(new ZipEntry(name));
                input.transferTo(output);
                output.closeEntry();
            }
            assertTrue(hasSubmissionText, "Download must retain student submission text to cover full-ZIP uploads");
            assertTrue(studentDirectory != null, "Download must include the ungraded student");
            output.putNextEntry(new ZipEntry(studentDirectory + "comments.txt"));
            output.write(feedback.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
    }

    private void openAssignment(String siteUrl, String title) {
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(title).setExact(true)).click();
    }

    private void openSubmissionList(String siteUrl, String title) {
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Grade " + title).setExact(true)).click();
        assertThat(page.locator("#submissionList")).isVisible();
    }

    private Locator studentRow(String student) {
        return page.locator("#submissionList tr").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("\\b" + student + "\\b")));
    }
}
