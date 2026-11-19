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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AssignmentDraftFeedbackReleaseTest extends SakaiUiTestBase {

    @TempDir
    Path temporaryDirectory;

    @ParameterizedTest
    @CsvSource({
        "false,classic,false", "true,classic,false", "false,bulk,false", "true,bulk,false",
        "false,grader,false", "true,grader,false", "false,upload,false", "true,upload,false",
        "false,classic,true", "false,bulk,true", "false,grader,true", "false,upload,true"
    })
    void returningGradeAndFeedbackPreservesStudentAccess(boolean submitted, String releasePath, boolean afterClose) throws IOException {
        boolean bulkRelease = "bulk".equals(releasePath);
        boolean useGrader = "grader".equals(releasePath);
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));
        String title = "Draft feedback release " + sakai.randomId();
        String feedback = "Please expand the evidence in your conclusion.";
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(title);
        page.locator("#gradeAssignment").check();
        page.locator("#new_assignment_grade_points").fill("100");
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions()
            .setName("Send notification email to student when the grade is released").setExact(true)).check();
        sakai.typeCkEditor("new_assignment_instructions", "<p>Submit work for instructor feedback.</p>");
        page.locator("input[name=post]").click();
        assertThat(page.locator("#assignments-list")).containsText(title);

        try (TestUser grader = sakai.createUser("draft-feedback-grader")) {
            sakai.addMember(siteUrl, grader.username(), "Instructor");
            sakai.addMember(siteUrl, "student0011", "Student");
            sakai.login("student0011");
            sakai.gotoPath(siteUrl);
            sakai.toolClick("Assignments");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(title).setExact(true)).click();
            sakai.typeCkEditor("Assignment.view_submission_text", "<p>Student answer for " + title + ".</p>");
            if (submitted) {
                page.locator("#confirm").click();
                page.locator("#post").click();
                assertThat(page.locator("body")).containsText("Submission Confirmation");
            } else {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Draft").setExact(true)).click();
                assertThat(page.locator("body")).containsText("Saved Assignment Confirmation");
            }

            sakai.login(grader.username());
            sakai.setAssignmentGrader(useGrader);
            if (afterClose) {
                closeAssignment(siteUrl, title);
            }
            openSubmissionList(siteUrl, title);
            studentRow().getByRole(AriaRole.LINK).first().click();
            if (afterClose) {
                if (useGrader) {
                    assertThat(page.locator("sakai-grader")).containsText("Student answer for " + title);
                } else {
                    assertThat(page.frameLocator("iframe[title='Editor, grade_submission_feedback_text']")
                        .locator("body")).containsText("Student answer for " + title);
                }
            }
            if (useGrader) {
                page.locator("sakai-grader #score-grade-input").fill("86");
                page.locator("sakai-grader #score-grade-input").press("Tab");
                page.locator("sakai-grader #grader-feedback-button").click();
                sakai.typeCkEditor("grader-feedback-comment", "<p>" + feedback + "</p>");
                page.locator("sakai-grader button[name=return]").click();
                assertThat(page.locator("sakai-grader .grader-returned")).isVisible();
            } else {
                page.locator("#grade").fill("86");
                sakai.typeCkEditor("grade_submission_feedback_comment", "<p>" + feedback + "</p>");
            }
            if ("upload".equals(releasePath)) {
                page.locator("#save").click();
                openSubmissionList(siteUrl, title);
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Upload All").setExact(true)).click();
                Path zipFile = temporaryDirectory.resolve("feedback.zip");
                try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                    zip.putNextEntry(new ZipEntry(title + "/Albertson, Albert(" + sakai.resolveUsername("student0011") + ")/comments.txt"));
                    zip.write(feedback.getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                }
                page.locator("#file").setInputFiles(zipFile);
                page.locator("#selectall").uncheck();
                page.locator("#feedbackComments").check();
                page.locator("#release_true").check();
                page.locator("#uploadButton").click();
                assertThat(studentRow()).containsText("Returned");
            } else if (bulkRelease) {
                page.locator("#save").click();
                openSubmissionList(siteUrl, title);
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Release Grades").setExact(true)).click();
                assertThat(studentRow()).containsText("Returned");
            } else if (!useGrader) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                    .setName("Save and Release to Student").setExact(true)).click();
                assertThat(page.getByRole(AriaRole.TABLE, new Page.GetByRoleOptions()
                    .setName("Assignment Details").setExact(true))).containsText("Returned");
            }

            if (afterClose) {
                sakai.login("student0011");
                sakai.gotoPath(siteUrl);
                sakai.toolClick("Assignments");
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(title).setExact(true)).click();
                assertThat(page.locator("body")).containsText(feedback);
            }
        }
    }

    private void closeAssignment(String siteUrl, String title) {
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Edit " + title).setExact(true)).click();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Open Date").setExact(true))
            .fill(today.minusDays(4) + "T00:00");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Due Date").setExact(true))
            .fill(today.minusDays(3) + "T00:00");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Accept Until").setExact(true))
            .fill(today.minusDays(2) + "T00:00");
        page.locator("input[name=post]").click();
        assertThat(page.getByRole(AriaRole.ALERT)).containsText("Assignment due date set to be in the past");
        page.locator("input[name=post]").click();
        assertThat(page.locator("#assignments-list")).containsText(title);
    }

    private void openSubmissionList(String siteUrl, String title) {
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Grade " + title).setExact(true)).click();
        assertThat(page.locator("#submissionList")).isVisible();
    }

    private Locator studentRow() {
        return page.locator("#submissionList tr").filter(new Locator.FilterOptions().setHasText("(student0011)"));
    }
}
