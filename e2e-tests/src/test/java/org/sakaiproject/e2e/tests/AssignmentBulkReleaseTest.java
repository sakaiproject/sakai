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
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AssignmentBulkReleaseTest extends SakaiUiTestBase {

    @Test
    void bulkReleaseIncludesInstructorFeedbackWithoutGrade() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));
        String title = "Bulk feedback release " + System.currentTimeMillis();
        String feedback = "Please expand the evidence in your conclusion.";
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(title);
        page.locator("#gradeAssignment").check();
        page.locator("#new_assignment_grade_points").fill("100");
        sakai.typeCkEditor("new_assignment_instructions", "<p>Submit work for instructor feedback.</p>");
        page.locator("input[name=post]").click();
        assertThat(page.locator("#assignments-list")).containsText(title);

        try (TestUser grader = sakai.createUser("bulk-feedback-grader")) {
            sakai.addMember(siteUrl, grader.username(), "Instructor");
            for (String student : List.of("student0011", "student0012", "student0013")) {
                sakai.addMember(siteUrl, student, "Student");
                openStudentAssignment(siteUrl, title, student);
                sakai.typeCkEditor("Assignment.view_submission_text", "<p>Work submitted by " + student + ".</p>");
                page.locator("#confirm").click();
                page.locator("#post").click();
                assertThat(page.locator("body")).containsText("Submission Confirmation");
            }

            sakai.login(grader.username());
            sakai.setAssignmentGrader(false);
            openSubmissionList(siteUrl, title);
            studentRow("student0011").getByRole(AriaRole.LINK).first().click();
            page.locator("#grade").fill("0");
            page.locator("#save").click();

            openSubmissionList(siteUrl, title);
            studentRow("student0012").getByRole(AriaRole.LINK).first().click();
            assertThat(page.locator("#grade")).hasValue("");
            sakai.typeCkEditor("grade_submission_feedback_comment", "<p>" + feedback + "</p>");
            page.locator("#save").click();

            openSubmissionList(siteUrl, title);
            assertThat(studentRow("student0012")).containsText("Commented");
            for (String student : List.of("student0011", "student0012", "student0013")) {
                assertThat(studentRow(student)).isVisible();
                assertThat(studentRow(student).locator("td[headers=gradereleased] .si-check")).hasCount(0);
            }

            openStudentAssignment(siteUrl, title, "student0012");
            assertThat(page.locator("body")).not().containsText(feedback);

            sakai.login(grader.username());
            openSubmissionList(siteUrl, title);
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Release Grades").setExact(true)).click();
            assertThat(studentRow("student0011").locator("td[headers=gradereleased] .si-check")).hasCount(1);
            assertThat(studentRow("student0012").locator("td[headers=gradereleased] .si-check")).hasCount(1);
            assertThat(studentRow("student0013")).isVisible();
            assertThat(studentRow("student0013").locator("td[headers=gradereleased] .si-check")).hasCount(0);

            studentRow("student0012").getByRole(AriaRole.LINK).first().click();
            assertThat(page.locator("#grade")).hasValue("");

            openStudentAssignment(siteUrl, title, "student0012");
            assertThat(page.locator("body")).containsText(feedback);
            assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                .setName(title + " - Returned").setExact(true))).isVisible();
        }
    }

    private void openSubmissionList(String siteUrl, String title) {
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Grade " + title).setExact(true)).click();
        assertThat(page.locator("#submissionList")).isVisible();
    }

    private void openStudentAssignment(String siteUrl, String title, String student) {
        sakai.login(student);
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(title).setExact(true)).click();
    }

    private Locator studentRow(String student) {
        return page.locator("#submissionList tr").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("\\b" + student + "\\b")));
    }
}
