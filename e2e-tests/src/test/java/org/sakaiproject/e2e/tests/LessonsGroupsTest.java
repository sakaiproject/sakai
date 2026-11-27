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

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for Lessons items that are released to some groups only. */
class LessonsGroupsTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool", "sakai\\.assignment\\.grades");
    /** The first visit to a tool in a new course can take longer than the default assertion timeout. */
    private static final double TOOL_LOAD_TIMEOUT = 15_000;

    private static String siteUrl;

    private LessonsUi lessons;

    @BeforeEach
    void openCourse() {
        lessons = new LessonsUi(page, sakai);
        sakai.login(INSTRUCTOR);
        if (siteUrl == null) {
            siteUrl = sakai.createFreshCourse(INSTRUCTOR, TOOLS);
        }
    }

    private void openAs(String username, String pageTitle) {
        sakai.login(username);
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
    }

    /** Creates an assignment in the Assignments tool, released to the given group or, if null, to the site. */
    private void createAssignment(String title, String group) {
        page.navigate(siteUrl);
        sakai.toolClick("Assignments");
        page.locator(".navIntraTool").getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#new_assignment_title").fill(title);
        page.locator("#gradeAssignment").uncheck();
        sakai.typeCkEditorIfPresent("new_assignment_instructions", "<p>Instructions of " + title + "</p>");
        if (group != null) {
            page.locator("#groupAssignment").check();
            String groupId = page.locator("#selectedGroups option")
                .filter(new Locator.FilterOptions().setHasText(group)).getAttribute("value");
            page.locator("#selectedGroups").selectOption(groupId);
        }
        page.locator(".act input[name=post]").click();
        assertThat(page.locator("#assignments-list tr").filter(new Locator.FilterOptions().setHasText(title)))
            .isVisible();
    }

    private Locator groupTitles(String item) {
        return lessons.item(item).locator(".item-group-titles");
    }

    /** Submits the assignment that Lessons shows in its frame. */
    private void submitThroughLessons(String title) {
        lessons.item(title).locator("a.itemlink").click();
        FrameLocator tool = page.frameLocator("#iframe1");
        Locator proceed = tool.locator("#confirm");
        assertThat(proceed).isAttached(new LocatorAssertions.IsAttachedOptions().setTimeout(TOOL_LOAD_TIMEOUT));
        Frame frame = page.locator("#iframe1").elementHandle().contentFrame();
        frame.evaluate("text => CKEDITOR.instances['Assignment.view_submission_text'].setData(text)",
            "<p>Work for " + title + "</p>");
        proceed.click();
        tool.locator("#post").click();
        assertThat(tool.locator("body")).containsText("Submission Confirmation");
    }

    @Test
    void assignmentsReleasedToGroupsAreOnlyShownToTheirMembers() {
        String pageTitle = "Group assignments " + sakai.randomId();
        String firstAssignment = "Group 1 assignment " + sakai.randomId();
        String secondAssignment = "Group 2 assignment " + sakai.randomId();
        try (TestUser firstStudent = lessons.newStudent(siteUrl, "lessons-groups-first");
                TestUser secondStudent = lessons.newStudent(siteUrl, "lessons-groups-second")) {
            String firstGroup = "Group 1 " + sakai.randomId();
            String secondGroup = "Group 2 " + sakai.randomId();
            sakai.addGroup(siteUrl, firstGroup, firstStudent.username());
            sakai.addGroup(siteUrl, secondGroup, secondStudent.username());
            createAssignment(firstAssignment, firstGroup);
            createAssignment(secondAssignment, null);

            lessons.open(siteUrl);
            lessons.createTopLevelPage(pageTitle);
            lessons.linkExisting("Link to an Assignment", firstAssignment);
            lessons.linkExisting("Link to an Assignment", secondAssignment);
            // Lessons shows the groups that the assignment itself is released to.
            assertThat(groupTitles(firstAssignment)).containsText(firstGroup);
            assertThat(groupTitles(secondAssignment)).hasCount(0);

            Locator editor = lessons.openItemEditor(lessons.item(secondAssignment));
            Locator editGroups = editor.locator("#editgroups a");
            if (!editGroups.isVisible()) {
                editor.locator("button.accordionBtn").filter(new Locator.FilterOptions().setHasText("Visibility"))
                    .click();
            }
            editGroups.click();
            editor.locator("#grouplist div").filter(new Locator.FilterOptions().setHasText(secondGroup))
                .locator("input[type=checkbox]").check();
            lessons.saveItemEditor(editor);
            assertThat(groupTitles(secondAssignment)).containsText(secondGroup);

            openAs(firstStudent.username(), pageTitle);
            assertThat(lessons.item(firstAssignment)).isVisible();
            assertThat(lessons.item(secondAssignment)).hasCount(0);
            submitThroughLessons(firstAssignment);

            openAs(secondStudent.username(), pageTitle);
            assertThat(lessons.item(secondAssignment)).isVisible();
            assertThat(lessons.item(firstAssignment)).hasCount(0);
            submitThroughLessons(secondAssignment);
        }
    }
}
