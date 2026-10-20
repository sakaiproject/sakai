/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
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

import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AssignmentGroupLockTest extends SakaiUiTestBase {

    @Test
    void emptyAssignmentUnlocksGroupsAndRestoresAsDraftAfterGroupDeletion() {
        String suffix = Long.toString(System.currentTimeMillis());
        String groupTitle = "Restore group " + suffix;
        String assignmentTitle = "Group lock regression " + suffix;
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));

        openGroups(courseUrl);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create New Group").setExact(true)).click();
        page.locator("#groupTitle").fill(groupTitle);
        // Group creation enables its submit button on keyup.
        page.locator("#groupTitle").press("End");
        assertThat(page.locator("#groupTitle")).hasValue(groupTitle);
        String member = page.locator("#groupMembers option").last().getAttribute("value");
        page.locator("#groupMembers").selectOption(member);
        assertThat(page.locator("#create-group-submit-button")).isEnabled();
        page.locator("#create-group-submit-button").click();
        assertThat(groupRow(groupTitle).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(groupTitle).setExact(true))).isVisible();

        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(assignmentTitle);
        page.locator("#gradeAssignment").uncheck();
        sakai.typeCkEditorIfPresent("new_assignment_instructions", "<p>Group restore regression.</p>");
        page.locator("#groupAssignment").check();
        String groupId = page.locator("#selectedGroups option")
                .filter(new Locator.FilterOptions().setHasText(groupTitle)).getAttribute("value");
        page.locator("#selectedGroups").selectOption(groupId);
        page.locator(".act input[name=post]").click();
        assertThat(assignmentRow(assignmentTitle)).isVisible();

        // Opening the grading screen creates empty submission placeholders.
        assignmentRow(assignmentTitle).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("View Submissions " + assignmentTitle).setExact(true)).click();
        assertThat(page.locator("body")).containsText(groupTitle);
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Assignments").setExact(true)).click();
        assertThat(page.locator("#assignments-list")).isVisible();

        openGroups(courseUrl);
        assertThat(groupRow(groupTitle).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(groupTitle).setExact(true))).hasCount(0);

        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        assignmentRow(assignmentTitle).locator("input[name=selectedAssignments]").check();
        page.locator("#btnRemove").click();
        assertThat(page.locator("body")).containsText("This assignment's group locks will be released.");
        page.locator("input[name=eventSubmit_doDelete_assignment]").click();

        openGroups(courseUrl);
        assertThat(groupRow(groupTitle).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(groupTitle).setExact(true))).isVisible();
        groupRow(groupTitle).locator("input[type=checkbox]").check();
        page.locator("#delete-groups-submit-button").click();
        page.locator("#modal-btn-confirm").click();
        assertThat(groupRow(groupTitle)).hasCount(0);

        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Trash").setExact(true)).click();
        Locator deletedAssignment = assignmentRow(assignmentTitle);
        assertThat(deletedAssignment).containsText("Restores as a draft.");
        deletedAssignment.locator("input[name=selectedAssignments]").check();
        page.onceDialog(Dialog::accept);
        page.locator("#btnRestore").click();
        assertThat(assignmentRow(assignmentTitle)).containsText("Draft");
        assertThat(page.locator(".sak-banner-error:visible")).hasCount(0);
    }

    @Test
    void editingGroupsRetainsSubmittedAndGradedWorkWhileEmptyGroupCanBeRemoved() {
        String suffix = Long.toString(System.nanoTime());
        String submittedGroup = "Submitted group " + suffix;
        String gradedGroup = "Graded group " + suffix;
        String emptyGroup = "Empty group " + suffix;
        String assignmentTitle = "Preserve group work " + suffix;
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));
        addStudent(courseUrl, "student0011");
        addStudent(courseUrl, "student0012");
        addStudent(courseUrl, "student0013");
        createGroupThroughUi(courseUrl, submittedGroup, "student0011");
        createGroupThroughUi(courseUrl, gradedGroup, "student0012");
        createGroupThroughUi(courseUrl, emptyGroup, "student0013");
        createGradedGroupAssignment(courseUrl, assignmentTitle, List.of(submittedGroup, gradedGroup, emptyGroup));

        sakai.login("student0011");
        openAssignments(courseUrl);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(assignmentTitle).setExact(true)).click();
        sakai.typeCkEditor("Assignment.view_submission_text", "<p>Submitted work for group A.</p>");
        page.locator("#confirm").click();
        page.locator("#post").click();
        assertThat(page.locator("body")).containsText("Submission Confirmation");

        sakai.login("instructor1");
        openGroupGrading(courseUrl, assignmentTitle, gradedGroup);
        assertThat(page.locator("body")).containsText("There is no student submitted text.");
        page.locator("#grade").first().fill("7");
        sakai.typeCkEditor("grade_submission_feedback_comment", "<p>Keep this unreleased group feedback.</p>");
        page.locator("#save").click();
        assertThat(page.locator("#gradeForm")).isVisible();
        assertThat(page.locator(".sak-banner-error:visible")).hasCount(0);

        editAssignment(courseUrl, assignmentTitle);
        assertGroupOption(submittedGroup, true, true);
        assertGroupOption(gradedGroup, true, true);
        assertGroupOption(emptyGroup, true, false);
        openGroupChoices();
        assertThat(groupCheckbox(submittedGroup)).isChecked();
        assertThat(groupCheckbox(submittedGroup)).isDisabled();
        assertThat(groupCheckbox(gradedGroup)).isChecked();
        assertThat(groupCheckbox(gradedGroup)).isDisabled();
        assertThat(groupCheckbox(emptyGroup)).isEnabled();
        groupCheckbox(emptyGroup).uncheck();
        openGroupChoices();
        saveAssignment(assignmentTitle);

        editAssignment(courseUrl, assignmentTitle);
        assertGroupOption(submittedGroup, true, true);
        assertGroupOption(gradedGroup, true, true);
        assertGroupOption(emptyGroup, false, false);
        // Every selected option is disabled and omitted from a normal HTML form submission.
        page.locator("#new_assignment_title").fill(assignmentTitle + " edited");
        saveAssignment(assignmentTitle + " edited");
        assignmentTitle += " edited";
        openGroupGrading(courseUrl, assignmentTitle, gradedGroup);
        assertSavedGradeAndFeedback("7", "Keep this unreleased group feedback.");
        openGroupGrading(courseUrl, assignmentTitle, submittedGroup);
        assertThat(page.locator("body")).containsText("Submitted work for group A.");

        openGroups(courseUrl);
        assertGroupMembershipLocked(submittedGroup);
        assertGroupMembershipLocked(gradedGroup);
        assertThat(groupRow(emptyGroup).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(emptyGroup).setExact(true))).isVisible();

        editAssignment(courseUrl, assignmentTitle);
        openGroupChoices();
        groupCheckbox(emptyGroup).check();
        openGroupChoices();
        saveAssignment(assignmentTitle);
        editAssignment(courseUrl, assignmentTitle);
        assertGroupOption(emptyGroup, true, false);
        saveAssignment(assignmentTitle);
        openGroupGrading(courseUrl, assignmentTitle, gradedGroup);
        assertSavedGradeAndFeedback("7", "Keep this unreleased group feedback.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0"})
    void titleOnlyEditRetainsFeedbackOnlyAndZeroGradesWithoutStudentSubmission(String grade) {
        String suffix = Long.toString(System.nanoTime());
        String group = "Protected grading group " + suffix;
        String title = "Protected grading " + suffix;
        String feedback = "Unreleased feedback " + suffix;
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));
        addStudent(courseUrl, "student0012");
        createGroupThroughUi(courseUrl, group, "student0012");
        createGradedGroupAssignment(courseUrl, title, List.of(group));
        openGroupGrading(courseUrl, title, group);
        assertThat(page.locator("body")).containsText("There is no student submitted text.");
        page.locator("#grade").first().fill(grade);
        sakai.typeCkEditor("grade_submission_feedback_comment", "<p>" + feedback + "</p>");
        page.locator("#save").click();
        assertThat(page.locator("#gradeForm")).isVisible();
        assertThat(page.locator(".sak-banner-error:visible")).hasCount(0);
        editAssignment(courseUrl, title);
        assertGroupOption(group, true, true);
        openGroupChoices();
        assertThat(groupCheckbox(group)).isChecked();
        assertThat(groupCheckbox(group)).isDisabled();
        openGroupChoices();
        String editedTitle = title + " edited";
        page.locator("#new_assignment_title").fill(editedTitle);
        saveAssignment(editedTitle);
        editAssignment(courseUrl, editedTitle);
        assertGroupOption(group, true, true);
        saveAssignment(editedTitle);
        openGroupGrading(courseUrl, editedTitle, group);
        assertSavedGradeAndFeedback(grade, feedback);
        openGroups(courseUrl);
        assertGroupMembershipLocked(group);
    }

    private void addStudent(String courseUrl, String eid) {
        APIResponse user = page.request().get("/direct/user/" + eid + ".json");
        assertTrue(user.ok(), user.text());
        String userId = JsonParser.parseString(user.text()).getAsJsonObject().get("id").getAsString();
        APIResponse membership = page.request().post("/direct/membership/new", RequestOptions.create().setForm(FormData.create()
                .set("locationReference", "/site/" + sakai.siteIdFromUrl(courseUrl))
                .set("userId", userId).set("memberRole", "Student").set("active", "true")));
        assertTrue(membership.ok(), membership.text());
    }

    private void createGroupThroughUi(String courseUrl, String title, String student) {
        openGroups(courseUrl);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create New Group").setExact(true)).click();
        page.locator("#groupTitle").fill(title);
        page.locator("#groupTitle").press("End");
        Locator member = page.locator("#groupMembers option").filter(new Locator.FilterOptions().setHasText(student));
        assertThat(member).hasCount(1);
        page.locator("#groupMembers").selectOption(member.getAttribute("value"));
        assertThat(page.locator("#create-group-submit-button")).isEnabled();
        page.locator("#create-group-submit-button").click();
        assertThat(groupRow(title)).isVisible();
    }

    private void createGradedGroupAssignment(String courseUrl, String title, List<String> groups) {
        openAssignments(courseUrl);
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(title);
        page.locator("#subType").selectOption("1");
        page.locator("#gradeAssignment").check();
        page.locator("#new_assignment_grade_points").fill("10");
        page.locator("#new_assignment_check_add_honor_pledge").uncheck();
        sakai.typeCkEditor("new_assignment_instructions", "<p>Submit work as a group.</p>");
        LocalDateTime openDate = LocalDateTime.parse(page.locator("#opendate input[type=datetime-local]").inputValue());
        sakai.selectDate("#opendate", openDate.minusDays(1).toString());
        sakai.selectDate("#duedate", openDate.plusDays(7).toString());
        sakai.selectDate("#closedate", openDate.plusDays(7).toString());
        page.locator("#groupAssignment").check();
        openGroupChoices();
        for (String group : groups) {
            groupCheckbox(group).check();
        }
        openGroupChoices();
        saveAssignment(title);
    }

    private void openAssignments(String courseUrl) {
        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
    }

    private void editAssignment(String courseUrl, String title) {
        openAssignments(courseUrl);
        assignmentRow(title).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Edit " + title).setExact(true)).click();
        assertThat(page.locator("#new_assignment_title")).hasValue(title);
    }

    private void saveAssignment(String title) {
        page.locator(".act input[name=post]").click();
        assertThat(assignmentRow(title)).isVisible();
        assertThat(page.locator("#assignments-list")).isVisible();
        assertThat(page.locator(".sak-banner-error:visible")).hasCount(0);
    }

    private void openGroupGrading(String courseUrl, String title, String group) {
        openAssignments(courseUrl);
        assignmentRow(title).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Grade " + title).setExact(true)).click();
        assertThat(page.locator("#submissionList")).isVisible();
        Locator toggle = page.locator("sakai-grader-toggle input");
        assertThat(toggle).isVisible();
        if (toggle.isChecked()) {
            page.waitForResponse(response -> response.url().contains("/direct/userPrefs/updateKey/")
                    && response.request().method().equals("PUT"), () -> toggle.uncheck());
        }
        page.locator("#submissionList").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(group).setExact(true)).click();
        assertThat(page.locator("#gradeForm")).isVisible();
    }

    private void assertSavedGradeAndFeedback(String grade, String feedback) {
        assertThat(page.locator("#grade").first()).hasValue(grade);
        assertThat(page.locator("#grade_submission_feedback_comment"))
                .hasValue(Pattern.compile("(?s).*" + Pattern.quote(feedback) + ".*"));
        assertThat(page.locator("#grade-is-released")).hasValue("false");
    }

    private void assertGroupOption(String group, boolean selected, boolean disabled) {
        Locator option = page.locator("#selectedGroups option").filter(new Locator.FilterOptions().setHasText(group));
        assertThat(option).hasCount(1);
        assertThat(option).hasJSProperty("selected", selected);
        assertThat(option).hasJSProperty("disabled", disabled);
    }

    private void openGroupChoices() {
        page.locator("#groupTableContainer button.multiselect").click();
    }

    private Locator groupCheckbox(String title) {
        return page.locator("#groupTableContainer .multiselect-container").getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(title).setExact(true)).locator("input[type=checkbox]");
    }

    private void assertGroupMembershipLocked(String title) {
        assertThat(groupRow(title)).isVisible();
        assertThat(groupRow(title).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(title).setExact(true))).hasCount(0);
    }

    private void openGroups(String courseUrl) {
        page.navigate(courseUrl);
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Groups").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Group List").setExact(true))).isVisible();
    }

    private Locator groupRow(String title) {
        return page.locator("#groupTable tbody tr").filter(new Locator.FilterOptions().setHasText(title));
    }

    private Locator assignmentRow(String title) {
        return page.locator("tr").filter(new Locator.FilterOptions().setHasText(title));
    }
}
