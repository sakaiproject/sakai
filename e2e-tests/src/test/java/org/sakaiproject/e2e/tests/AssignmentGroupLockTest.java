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
        String member = page.locator("#groupMembers option").last().getAttribute("value");
        page.locator("#groupMembers").selectOption(member);
        page.locator("#create-group-submit-button").click();
        assertThat(groupRow(groupTitle).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(groupTitle).setExact(true))).isVisible();

        page.navigate(courseUrl);
        sakai.toolClick("Assignments");
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(assignmentTitle);
        sakai.typeCkEditorIfPresent("new_assignment_instructions", "<p>Group restore regression.</p>");
        page.locator("#groupAssignment").check();
        String groupId = page.locator("#selectedGroups option")
                .filter(new Locator.FilterOptions().setHasText(groupTitle)).getAttribute("value");
        page.locator("#selectedGroups").selectOption(groupId);
        page.locator(".act input[name=post]").click();
        assertThat(assignmentRow(assignmentTitle)).isVisible();

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
        page.locator("#btnRestore").click();
        assertThat(assignmentRow(assignmentTitle)).containsText("Draft");
        assertThat(page.locator(".sak-banner-error")).hasCount(0);
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
