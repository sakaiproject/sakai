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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for required items and prerequisites in Lessons. */
class LessonsPrerequisitesTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool");
    private static final String QUESTION_EDITOR = "question-text-area-evolved::input";
    private static final String LOCKED_PAGE = "Before viewing page, you must complete the following pages:";

    private static final String ITEM = "xpath=ancestor::div[contains(concat(' ', @class, ' '), ' item ')][1]";

    private String siteUrl;

    private LessonsUi lessons;

    @BeforeEach
    void openCourse() {
        lessons = new LessonsUi(page, sakai);
        sakai.login(INSTRUCTOR);
        // Page prerequisites take every required page above them in the tool order, so the
        // pages of one test must not share a site with those of another.
        siteUrl = sakai.createFreshCourse(INSTRUCTOR, TOOLS);
        lessons.open(siteUrl);
    }

    private String newPage(String prefix) {
        String title = prefix + " " + sakai.randomId();
        lessons.createTopLevelPage(title);
        return title;
    }

    private void openAs(String username, String pageTitle) {
        sakai.login(username);
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
    }

    private void setPageOption(String option) {
        Locator settings = lessons.openPageSettings();
        settings.locator(option).check();
        lessons.savePageSettings(settings);
    }

    private Locator commentsSection() {
        return page.locator(".commentsCell").first();
    }

    /** Adds a comments section that students must post to. */
    private void addRequiredComments() {
        lessons.addComments();
        Locator section = commentsSection();
        section.hover();
        section.locator("a.edit-comments").click();
        Locator dialog = page.locator("#comments-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#comments-required").check();
        page.waitForNavigation(() -> dialog.locator("#update-comments").click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private void postComment(String text) {
        Locator section = commentsSection().locator(".commentsDiv");
        section.locator("button.switchLink").click();
        String editorId = (String) section.locator("textarea").first().evaluate(
            "area => Object.values(CKEDITOR.instances).find(editor => editor.element.$ === area)?.name");
        sakai.typeCkEditor(editorId, "<p>" + text + "</p>");
        page.waitForNavigation(() -> section.locator("input.submitButton").click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(commentsSection().locator(".commentDiv").filter(new Locator.FilterOptions().setHasText(text)))
            .isVisible();
    }

    /** Moves a page one place up in Site Info > Tool Order, which saves the order at once. */
    private void movePageUp(String title) {
        sakai.login(INSTRUCTOR);
        page.navigate(siteUrl);
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
            .setName(Pattern.compile("Tool Order", Pattern.CASE_INSENSITIVE))).first().click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        Locator row = page.locator(".tool-order-row").filter(new Locator.FilterOptions().setHas(
            page.locator(".tool-order-title").filter(new Locator.FilterOptions().setHasText(title))));
        row.locator("[data-action=\"move-up\"]").click();
        assertThat(page.locator("#tool-order-alert")).containsText(Pattern.compile("order.*saved", Pattern.CASE_INSENSITIVE));
    }

    private Locator status(Locator item, String title) {
        return item.locator("[title='" + title + "']");
    }

    private void markSubpageRequired(String subpage) {
        Locator editor = lessons.openItemEditor(lessons.item(subpage));
        editor.locator("#prereqstuff button.accordionBtn").click();
        editor.locator("#item-required").check();
        lessons.saveItemEditor(editor);
    }

    /** Adds a required subpage whose own required item is a comments section. */
    private void addRequiredSubpage(String parent, String subpage) {
        lessons.addSubpage(subpage);
        addRequiredComments();
        sakai.toolClick(parent);
        markSubpageRequired(subpage);
    }

    private void openQuestionDialog() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        page.locator(".question-link:visible").click();
        assertThat(page.locator("#question-dialog")).isVisible();
        page.locator("#multipleChoiceSelect").check();
    }

    /** Adds a required multiple choice question whose first answer is the correct one. */
    private void addRequiredQuestion(String text, boolean prerequisite) {
        openQuestionDialog();
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + text + "</p>");
        Locator answers = page.locator(".question-multiplechoice-answer:visible");
        answers.nth(0).fill("Right");
        page.locator("#multipleChoiceDialogDiv").getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add New Answer").setExact(true)).click();
        answers.nth(1).fill("Wrong");
        page.locator(".question-multiplechoice-answer-correct:visible").first().check();
        page.locator("#question-required").check();
        if (prerequisite) {
            page.locator("#question-prerequisite").check();
        }
        page.locator("#update-question").click();
        assertThat(question(text).locator(".questionText")).isVisible();
    }

    private Locator question(String text) {
        return page.locator(".questionDiv").filter(new Locator.FilterOptions().setHasText(text));
    }

    private void answer(String text, String choice) {
        Locator question = question(text);
        // Answers are labelled with their letter, as in "A : Right".
        question.locator(".multipleChoiceAnswer").filter(new Locator.FilterOptions().setHasText(choice))
            .locator("input[type=radio]").check();
        page.waitForResponse(response -> "POST".equals(response.request().method())
                && response.url().contains("/ShowPage"),
            () -> question.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Submit Answer").setExact(true)).click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    /** Embeds a frame that is only released once the required items above it are done. */
    private void addEmbedWithPrerequisites(String embedCode) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        page.locator("#addContentDiv a.add-multimedia").first().click();
        Locator dialog = page.locator("#add-multimedia-dialog");
        assertThat(dialog).isVisible();
        dialog.locator("#new-url-section-collapse").click();
        dialog.locator("#mm-url").fill(embedCode);
        dialog.locator("#mm-prerequisite").check();
        dialog.locator("#mm-add-item").click();
        assertThat(dialog.locator("#mm-test-embed-results")).isVisible();
        dialog.locator("#mm-add-item").click();
        assertThat(dialog).isHidden();
    }

    @Test
    void pagePrerequisitesFollowTheToolOrder() {
        String first = newPage("First page");
        lessons.addText("<p>Content of the first page</p>");
        String required = newPage("Required page");
        lessons.addText("<p>Content of the required page</p>");
        addRequiredComments();
        setPageOption("#page-required");
        String last = newPage("Last page");
        lessons.addText("<p>Content of the last page</p>");
        setPageOption("#page-prerequisites");
        sakai.toolClick(first);
        setPageOption("#page-prerequisites");

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-page-order")) {
            openAs(student.username(), first);
            assertThat(page.getByText("Content of the first page")).isVisible();
            sakai.toolClick(required);
            assertThat(page.getByText("Content of the required page")).isVisible();
            sakai.toolClick(last);
            assertThat(page.getByText(LOCKED_PAGE)).isVisible();
            assertThat(page.locator(".portletBody")).containsText(required);

            movePageUp(required);
            openAs(student.username(), first);
            assertThat(page.getByText(LOCKED_PAGE)).isVisible();
            assertThat(page.getByText("Content of the first page")).hasCount(0);

            sakai.toolClick(required);
            postComment("Done with the required page " + sakai.randomId());
            sakai.toolClick(first);
            assertThat(page.getByText("Content of the first page")).isVisible();
            sakai.toolClick(last);
            assertThat(page.getByText("Content of the last page")).isVisible();
        }
    }

    @Test
    void requiredSubpagesUnlockTheNextModule() {
        String moduleA = newPage("Module A");
        setPageOption("#page-required");
        lessons.addText("<p>Introduction to module A</p>");
        String firstSubpage = "Unit 1 " + sakai.randomId();
        String secondSubpage = "Unit 2 " + sakai.randomId();
        addRequiredSubpage(moduleA, firstSubpage);
        addRequiredSubpage(moduleA, secondSubpage);
        assertThat(status(lessons.item(firstSubpage), "Required")).hasCount(1);

        String moduleB = newPage("Module B");
        lessons.addText("<p>Content of module B</p>");
        setPageOption("#page-prerequisites");

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-modules")) {
            openAs(student.username(), moduleB);
            assertThat(page.getByText(LOCKED_PAGE)).isVisible();
            assertThat(page.locator(".portletBody")).containsText(moduleA);

            sakai.toolClick(moduleA);
            lessons.openSubpageFromParent(firstSubpage);
            postComment("Finished the first unit " + sakai.randomId());
            sakai.toolClick(moduleA);
            assertThat(status(lessons.item(firstSubpage), "Completed")).hasCount(1);
            sakai.toolClick(moduleB);
            assertThat(page.getByText(LOCKED_PAGE)).isVisible();

            sakai.toolClick(moduleA);
            lessons.openSubpageFromParent(secondSubpage);
            postComment("Finished the second unit " + sakai.randomId());
            sakai.toolClick(moduleA);
            assertThat(status(lessons.item(secondSubpage), "Completed")).hasCount(1);
            sakai.toolClick(moduleB);
            assertThat(page.getByText("Content of module B")).isVisible();
        }
    }

    @Test
    void requiredQuestionsReleaseTheItemsBelowThem() {
        String title = newPage("Week 1");
        String firstQuestion = "First required question " + sakai.randomId();
        String secondQuestion = "Second required question " + sakai.randomId();
        addRequiredQuestion(firstQuestion, false);
        addRequiredQuestion(secondQuestion, true);
        assertThat(question(secondQuestion).locator(ITEM))
            .containsText("Has prerequisites");
        addEmbedWithPrerequisites("<iframe title=\"Reward frame\" src=\"/library/content/gateway/welcome.html\"></iframe>");
        assertThat(page.getByText("If you were a student, you wouldn't be able to view or access this item").first())
            .isVisible();

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-required-questions")) {
            openAs(student.username(), title);
            assertThat(page.getByText("This multimedia item will show when all pre-requisites")).isVisible();
            assertThat(page.locator("iframe[title='Reward frame']")).hasCount(0);

            answer(firstQuestion, "Right");
            assertThat(status(question(firstQuestion).locator(ITEM),
                "Completed")).hasCount(1);
            assertThat(page.locator("iframe[title='Reward frame']")).hasCount(0);

            // A wrong answer still completes the required question.
            answer(secondQuestion, "Wrong");
            assertThat(page.locator("iframe[title='Reward frame']")).isVisible();
        }
    }
}
