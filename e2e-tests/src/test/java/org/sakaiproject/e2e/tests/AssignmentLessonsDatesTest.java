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

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AssignmentLessonsDatesTest extends SakaiUiTestBase {

    @Test
    void editingDueDateThroughLessonsPersistsTheNewDate() {
        String title = "SAK-52972 assignment " + System.currentTimeMillis();
        sakai.login("instructor1");
        String courseUrl = sakai.createCourse("instructor1", List.of(
            "sakai\\.assignment\\.grades", "sakai\\.lessonbuildertool"));
        page.navigate(courseUrl);
        sakai.toolClick("Lessons");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content").setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Link to an Assignment").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
            .setName("Create new assignment using Assignments").setExact(true)).click();

        FrameLocator assignments = page.frameLocator("#iframe1");
        assignments.getByRole(AriaRole.LINK, new FrameLocator.GetByRoleOptions().setName("Add").setExact(true)).click();
        assignments.locator("#new_assignment_title").fill(title);
        assignments.locator("#gradeAssignment").uncheck();
        assignments.getByRole(AriaRole.CHECKBOX, new FrameLocator.GetByRoleOptions()
            .setName("Send a reminder email 24 hours before the due date").setExact(true)).uncheck();
        assignments.frameLocator("iframe[title='Editor, new_assignment_instructions']")
            .getByRole(AriaRole.TEXTBOX, new FrameLocator.GetByRoleOptions()
                .setName("Editor, new_assignment_instructions").setExact(true))
            .fill("Edit the assignment due date through Lessons.");
        Locator dueDate = assignments.locator("#duedate input[type='datetime-local']");
        String originalDate = dueDate.inputValue();
        String revisedDate = LocalDateTime.parse(originalDate).minusDays(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        assignments.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Post").setExact(true)).click();

        Locator row = assignments.locator("#assignments-list tr")
            .filter(new Locator.FilterOptions().setHasText(title));
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit " + title).setExact(true)).click();
        assertThat(dueDate).hasValue(originalDate);
        dueDate.fill(revisedDate);
        dueDate.press("Tab");
        assignments.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Post").setExact(true)).click();
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit " + title).setExact(true)).click();
        assertThat(dueDate).hasValue(revisedDate);
        assignments.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Cancel").setExact(true)).click();

        // Verify through the regular Assignments tool too, after leaving the Lessons iframe.
        sakai.toolClick("Assignments");
        page.locator("#assignments-list tr").filter(new Locator.FilterOptions().setHasText(title))
            .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit " + title).setExact(true)).click();
        assertThat(page.locator("#duedate input[type='datetime-local']")).hasValue(revisedDate);
    }
}
