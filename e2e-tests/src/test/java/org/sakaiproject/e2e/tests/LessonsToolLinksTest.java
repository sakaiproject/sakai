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

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for Lessons items that link to other tools of the site. */
class LessonsToolLinksTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool", "sakai\\.assignment\\.grades",
        "sakai\\.samigo", "sakai\\.forums");
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
        page.navigate(siteUrl);
    }

    private String newPage(String prefix) {
        String title = prefix + " " + sakai.randomId();
        lessons.open(siteUrl);
        lessons.createTopLevelPage(title);
        return title;
    }

    private void openAsStudent(TestUser student, String pageTitle) {
        sakai.login(student.username());
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
    }

    @Test
    void studentCancelsALinkedAssignmentInsideLessons() {
        String assignment = "Lessons assignment " + sakai.randomId();
        sakai.createAssignment(siteUrl, assignment);
        String title = newPage("Assignment link");
        lessons.linkExisting("Link to an Assignment", assignment);

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-assignment")) {
            openAsStudent(student, title);
            List<String> scriptErrors = new ArrayList<>();
            // The portal push notifications need a secure context, which a plain http host is not.
            page.onPageError(error -> {
                if (!error.contains("pushManager")) {
                    scriptErrors.add(error);
                }
            });
            lessons.item(assignment).locator("a.itemlink").click();
            FrameLocator tool = page.frameLocator("#iframe1");
            assertThat(tool.locator("body")).containsText(assignment);
            tool.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
            assertThat(tool.locator("body")).containsText("Select an assignment to view details");
            assertTrue(scriptErrors.isEmpty(), "Script errors: " + scriptErrors);
        }
    }

    @Test
    void linkedQuizIsShownToStudents() {
        String quiz = "Lessons quiz " + sakai.randomId();
        createPublishedQuiz(quiz);
        String title = newPage("Quiz link");
        lessons.linkExisting("Link to a Test or Quiz", quiz);

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-quiz")) {
            openAsStudent(student, title);
            lessons.item(quiz).locator("a.itemlink").click();
            // The Tests list also shows the quiz title, so check the page that starts the quiz.
            // Lessons loads it into the frame several seconds after showing the item page.
            Locator beginForm = page.frameLocator("#iframe1").locator("#takeAssessmentForm");
            assertThat(beginForm).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(TOOL_LOAD_TIMEOUT));
            assertThat(beginForm).containsText(quiz);
            assertThat(beginForm).containsText("Begin Assessment");
        }
    }

    @Test
    void linkedTopicTogglesItsFullDescription() {
        String topic = describeDefaultTopic("Full topic description");
        String title = newPage("Topic link");
        lessons.linkExisting("Link to a Forum or Topic", topic);

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-topic")) {
            openAsStudent(student, title);
            lessons.item(topic).locator("a.itemlink").click();
            FrameLocator tool = page.frameLocator("#iframe1");
            Locator view = tool.locator("#showMessage");
            Locator hide = tool.locator("#hideMessage");
            Locator description = tool.getByText("Full topic description");
            assertThat(view).isVisible();
            assertThat(hide).isHidden();
            assertThat(description).isHidden();

            view.click();
            assertThat(description).isVisible();
            assertThat(view).isHidden();
            assertThat(hide).isVisible();
            // Discussions ignores the hide link while the description is still sliding open.
            page.locator("#iframe1").elementHandle().contentFrame()
                .waitForFunction("() => !window.jQuery || window.jQuery(':animated').length === 0");

            hide.click();
            assertThat(description).isHidden();
            assertThat(view).isVisible();
        }
    }

    /** Discussions creates a forum with one topic the first time an instructor opens it. */
    private String describeDefaultTopic(String description) {
        sakai.toolClick("Discussions");
        Locator topicTitle = page.locator("[id='msgForum:forums:0:topics:0:topic_title']");
        assertThat(topicTitle).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(TOOL_LOAD_TIMEOUT));
        String topic = topicTitle.innerText().trim();
        page.locator("[id='msgForum:forums:0:topics:0:topic_setting']").click();
        page.locator("[id='revise:topic_shortDescription']").fill("Short topic summary");
        sakai.typeCkEditor("revise:topic_description_inputRichText", "<p>" + description + "</p>");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).click();
        assertThat(topicTitle).isVisible();
        return topic;
    }

    private void createPublishedQuiz(String title) {
        sakai.toolClick("Tests");
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).click();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click();
        Locator type = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        type.selectOption(type.locator("option").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("multiple choice", Pattern.CASE_INSENSITIVE))).first().getAttribute("value"));
        page.locator("#itemForm\\:answerptr").fill("1");
        page.locator("#itemForm textarea").first().fill("Choose Correct.");
        page.locator("#itemForm\\:mcchoices textarea").nth(0).fill("Correct");
        page.locator("#itemForm\\:mcchoices textarea").nth(1).fill("Incorrect");
        page.locator("#itemForm\\:mcchoices input[type='radio']").nth(0).check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first().click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Settings").setExact(true)).first().click();
        page.locator("#expandLink").click();
        DateTimeFormatter dateTime12h = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US);
        sakai.selectDate("#assessmentSettingsAction\\:startDate",
            LocalDateTime.now().minusDays(1).format(dateTime12h).toLowerCase(Locale.US));
        sakai.selectDate("#assessmentSettingsAction\\:endDate",
            LocalDateTime.now().plusYears(2).format(dateTime12h).toLowerCase(Locale.US));
        page.locator("[id='assessmentSettingsAction:honor_pledge']").check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Settings and Publish").setExact(true)).click();
        page.locator("#publishAssessmentForm\\:publish").click();
        assertThat(page.locator("#authorIndexForm")).isVisible();
    }
}
