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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AssignmentGroupSubmissionTest extends SakaiUiTestBase {

    @Test
    void submissionsListShowsOnlyGroupNameForGroupAssignment() {
        String suffix = Long.toString(System.currentTimeMillis());
        String groupTitle = "Submission group " + suffix;
        String assignmentTitle = "Group submission names " + suffix;
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of("sakai\\.assignment\\.grades"));

        page.navigate(courseUrl);
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Groups").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create New Group").setExact(true)).click();
        page.locator("#groupTitle").fill(groupTitle);
        // Group creation enables its submit button on keyup.
        page.locator("#groupTitle").press("End");
        Locator members = page.locator("#groupMembers option");
        String firstMember = members.filter(new Locator.FilterOptions().setHasText("(student0011)")).getAttribute("value");
        String secondMember = members.filter(new Locator.FilterOptions().setHasText("(student0012)")).getAttribute("value");
        page.locator("#groupMembers").selectOption(new String[] {firstMember, secondMember});
        assertThat(page.locator("#create-group-submit-button")).isEnabled();
        page.locator("#create-group-submit-button").click();
        assertThat(page.locator("#groupTable tbody tr").filter(new Locator.FilterOptions().setHasText(groupTitle)))
                .isVisible();

        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(assignmentTitle);
        page.locator("#gradeAssignment").check();
        page.locator("#new_assignment_grade_type").selectOption("3");
        page.locator("#new_assignment_grade_points").fill("100");
        sakai.typeCkEditorIfPresent("new_assignment_instructions", "<p>Submit once for the entire group.</p>");
        page.locator("#groupAssignment").check();
        String groupId = page.locator("#selectedGroups option")
                .filter(new Locator.FilterOptions().setHasText(groupTitle)).getAttribute("value");
        page.locator("#selectedGroups").selectOption(groupId);
        page.locator(".act input[name=post]").click();
        Locator assignmentRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(assignmentTitle));
        assertThat(assignmentRow).isVisible();
        assignmentRow.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Grade " + assignmentTitle).setExact(true)).click();

        Locator groupName = page.locator("#submissionList td[headers=studentname]")
                .filter(new Locator.FilterOptions().setHasText(groupTitle));
        assertThat(groupName).hasCount(1);
        assertThat(groupName).hasText(groupTitle);
        assertThat(groupName.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(groupTitle).setExact(true))).isVisible();
        page.reload();
        assertThat(groupName).hasText(groupTitle);
    }
}
