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

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for the items that Lessons creates through the other tools of the site. */
class LessonsCreateWithToolsTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool", "sakai\\.assignment\\.grades",
        "sakai\\.samigo", "sakai\\.forums");
    /** The first visit to a tool in a new course can take longer than the default assertion timeout. */
    private static final double TOOL_LOAD_TIMEOUT = 15_000;

    private String siteUrl;

    private String weekTitle;

    private LessonsUi lessons;

    /** Each test starts from a site without assignments, quizzes or topics, as the script asks. */
    @BeforeEach
    void openCourse() {
        lessons = new LessonsUi(page, sakai);
        sakai.login(INSTRUCTOR);
        siteUrl = sakai.createFreshCourse(INSTRUCTOR, TOOLS);
        lessons.open(siteUrl);
        weekTitle = "Week 1 " + sakai.randomId();
        lessons.createTopLevelPage(weekTitle);
        lessons.addText("<p>Week 1 Lessons content</p>");
    }

    private void openPicker(String menuEntry, String emptyMessage) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(menuEntry).setExact(true)).click();
        assertThat(page.getByText(emptyMessage)).isVisible();
    }

    /** Opens the tool inside Lessons through the picker's create link. */
    private FrameLocator createWith(String linkText) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(linkText).setExact(true)).click();
        return page.frameLocator("#iframe1");
    }

    /** Goes back to the picker, where the new item is chosen and added to the page. */
    private void continueAndUse(String returnText, String title) {
        page.locator("a.returnmsg").filter(new Locator.FilterOptions().setHasText(returnText)).click();
        Locator radio = page.locator("[role=listitem]").filter(new Locator.FilterOptions().setHasText(title))
            .locator("input[type=radio]");
        radio.check();
        page.locator("input[type=submit].bold").click();
        assertThat(lessons.item(title)).isVisible();
    }

    @Test
    void assignmentCreatedFromLessonsIsAddedToThePage() {
        String title = "Lessons new assignment " + sakai.randomId();
        openPicker("Link to an Assignment", "There are no assignments available in this site.");
        FrameLocator assignments = createWith("Create new assignment using Assignments");
        assignments.getByRole(AriaRole.LINK, new FrameLocator.GetByRoleOptions().setName("Add").setExact(true)).click();
        assignments.locator("#new_assignment_title").fill(title);
        assignments.getByRole(AriaRole.CHECKBOX, new FrameLocator.GetByRoleOptions()
            .setName("Send a reminder email 24 hours before the due date").setExact(true)).uncheck();
        assignments.frameLocator("iframe[title='Editor, new_assignment_instructions']")
            .getByRole(AriaRole.TEXTBOX, new FrameLocator.GetByRoleOptions()
                .setName("Editor, new_assignment_instructions").setExact(true))
            .fill("Assignment created from Lessons.");
        assignments.locator("#new_assignment_grade_points").fill("100");
        assignments.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Post").setExact(true)).click();
        assertThat(assignments.locator("#assignments-list tr").filter(new Locator.FilterOptions().setHasText(title)))
            .isVisible();

        continueAndUse("Continue with Add Assignment", title);
    }

    @Test
    void forumAndTopicsCreatedFromLessonsAreOfferedInThePicker() {
        String forum = "Lessons new forum " + sakai.randomId();
        String firstTopic = "Lessons first topic " + sakai.randomId();
        String secondTopic = "Lessons second topic " + sakai.randomId();
        openPicker("Link to a Forum or Topic", "There are no forums or topics available in this site.");
        FrameLocator discussions = createWith("Create new forum or topic using Discussions");
        discussions.getByRole(AriaRole.LINK, new FrameLocator.GetByRoleOptions()
            .setName(Pattern.compile("^New Forum$", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setTimeout(TOOL_LOAD_TIMEOUT));
        discussions.locator("[id='revise:forum_title']").fill(forum);
        discussions.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions()
            .setName("Save Settings & Add Topic").setExact(true)).first().click();
        discussions.locator("[id='revise:topic_title']").fill(firstTopic);
        discussions.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Save").setExact(true))
            .first().click();
        assertThat(discussions.getByText(firstTopic).first()).isVisible();

        Locator forumBlock = discussions.locator(".forumHeader, .hierItemBlock").filter(
            new Locator.FilterOptions().setHasText(forum)).first();
        forumBlock.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions()
            .setName(Pattern.compile("^New Topic$", Pattern.CASE_INSENSITIVE))).first().click();
        discussions.locator("[id='revise:topic_title']").fill(secondTopic);
        discussions.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Save").setExact(true))
            .first().click();
        assertThat(discussions.getByText(secondTopic).first()).isVisible();

        page.locator("a.returnmsg").filter(new Locator.FilterOptions().setHasText("Continue with Add Forum or Topic"))
            .click();
        assertThat(page.locator("[role=listitem]").filter(new Locator.FilterOptions().setHasText(forum)).first())
            .isVisible();
        assertThat(page.locator("[role=listitem]").filter(new Locator.FilterOptions().setHasText(firstTopic)))
            .isVisible();
        page.locator("[role=listitem]").filter(new Locator.FilterOptions().setHasText(secondTopic))
            .locator("input[type=radio]").check();
        page.locator("input[type=submit].bold").click();
        assertThat(lessons.item(secondTopic)).isVisible();
    }

    @Test
    void quizCreatedFromLessonsIsAddedToThePage() {
        String title = "Lessons new quiz " + sakai.randomId();
        openPicker("Link to a Test or Quiz", "There are no assessments available in this site.");
        FrameLocator samigo = createWith("Create new quiz using Tests and Quizzes");
        samigo.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$")))
            .click(new Locator.ClickOptions().setTimeout(TOOL_LOAD_TIMEOUT));
        samigo.locator("#authorIndexForm\\:title").fill(title);
        samigo.locator("#authorIndexForm\\:createnew").click();
        Locator type = samigo.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        type.selectOption(type.locator("option").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("multiple choice", Pattern.CASE_INSENSITIVE))).first().getAttribute("value"));
        samigo.locator("#itemForm\\:answerptr").fill("1");
        samigo.locator("#itemForm textarea").first().fill("Choose Correct.");
        samigo.locator("#itemForm\\:mcchoices textarea").nth(0).fill("Correct");
        samigo.locator("#itemForm\\:mcchoices textarea").nth(1).fill("Incorrect");
        samigo.locator("#itemForm\\:mcchoices input[type='radio']").nth(0).check();
        samigo.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Save").setExact(true))
            .first().click();
        samigo.getByRole(AriaRole.LINK, new FrameLocator.GetByRoleOptions().setName("Publish").setExact(true))
            .first().click();
        samigo.locator("#publishAssessmentForm\\:publish").click();
        assertThat(samigo.locator("#authorIndexForm")).isVisible(
            new LocatorAssertions.IsVisibleOptions().setTimeout(TOOL_LOAD_TIMEOUT));

        continueAndUse("Continue with Add Quiz", title);
    }

    @Test
    void topicCreatedFromLessonsForAGroupIsOnlyShownToItsMembers() {
        String forum = "Lessons group forum " + sakai.randomId();
        String topic = "Lessons group topic " + sakai.randomId();
        try (TestUser member = lessons.newStudent(siteUrl, "lessons-tools-member");
                TestUser outsider = lessons.newStudent(siteUrl, "lessons-tools-outsider")) {
            String group = "Tools group " + sakai.randomId();
            sakai.addGroup(siteUrl, group, member.username());
            page.reload();

            openPicker("Link to a Forum or Topic", "There are no forums or topics available in this site.");
            FrameLocator discussions = createWith("Create new forum or topic using Discussions");
            discussions.getByRole(AriaRole.LINK, new FrameLocator.GetByRoleOptions()
                .setName(Pattern.compile("^New Forum$", Pattern.CASE_INSENSITIVE))).first()
                .click(new Locator.ClickOptions().setTimeout(TOOL_LOAD_TIMEOUT));
            discussions.locator("[id='revise:forum_title']").fill(forum);
            discussions.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions()
                .setName("Save Settings & Add Topic").setExact(true)).first().click();
            discussions.locator("[id='revise:topic_title']").fill(topic);
            // The permissions panel may start folded, depending on the Discussions settings.
            if (!permissionLevel(discussions, "Student").isVisible()) {
                discussions.locator("td.discTria h2").getByText("Permissions", new Locator.GetByTextOptions().setExact(true))
                    .click();
            }
            permissionLevel(discussions, "Student").selectOption(new SelectOption()
                .setLabel("None"));
            permissionLevel(discussions, group).selectOption(new SelectOption()
                .setLabel("Contributor"));
            discussions.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Save").setExact(true))
                .first().click();
            assertThat(discussions.getByText(topic).first()).isVisible();

            continueAndUse("Continue with Add Forum or Topic", topic);
            assertThat(lessons.item(topic)).containsText(group);

            sakai.login(outsider.username());
            page.navigate(siteUrl);
            sakai.toolClick(weekTitle);
            assertThat(page.getByText("Week 1 Lessons content")).isVisible();
            assertThat(lessons.item(topic)).hasCount(0);

            sakai.login(member.username());
            page.navigate(siteUrl);
            sakai.toolClick(weekTitle);
            assertThat(lessons.item(topic).locator("a.itemlink")).isVisible();
        }
    }

    /** The level menu of a role or group in the permissions table of a Discussions topic. */
    private Locator permissionLevel(FrameLocator discussions, String name) {
        return discussions.locator("[id='revise:perm'] tr").filter(new Locator.FilterOptions().setHas(
            discussions.locator("label").getByText(name, new Locator.GetByTextOptions().setExact(true))))
            .locator("select[id$=':level']");
    }
}
