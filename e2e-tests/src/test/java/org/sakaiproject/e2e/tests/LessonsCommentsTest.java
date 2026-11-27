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
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for the comments sections of Lessons. */
class LessonsCommentsTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool", "sakai\\.gradebookng");

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

    private String newPage(String prefix) {
        String title = prefix + " " + sakai.randomId();
        lessons.open(siteUrl);
        lessons.createTopLevelPage(title);
        return title;
    }

    /** A disposable student with its own first name, so their comments can be told apart. */
    private TestUser student(String prefix, String firstName) {
        TestUser student = sakai.createUser(prefix, firstName);
        try {
            sakai.addMember(siteUrl, student.username(), "Student");
        } catch (RuntimeException | AssertionError e) {
            try {
                student.close();
            } catch (RuntimeException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
        return student;
    }

    private void openAsStudent(TestUser student, String pageTitle) {
        sakai.login(student.username());
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
    }

    private void openAsInstructor(String pageTitle) {
        sakai.login(INSTRUCTOR);
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
    }

    private Locator commentsSection() {
        return page.locator(".commentsCell").first();
    }

    /** The comments are loaded into the section after the page itself. */
    private Locator comment(String text) {
        return commentsSection().locator(".commentDiv").filter(new Locator.FilterOptions().setHasText(text));
    }

    /** The edit button of the comments section only appears while it is hovered. */
    private Locator openCommentsSettings() {
        Locator section = commentsSection();
        section.hover();
        section.locator("a.edit-comments").click();
        Locator dialog = page.locator("#comments-dialog");
        assertThat(dialog).isVisible();
        return dialog;
    }

    private void saveCommentsSettings(Locator dialog) {
        page.waitForNavigation(() -> dialog.locator("#update-comments").click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(page.locator("#addcontent")).isAttached();
    }

    private void gradeComments(Locator dialog, String maxPoints) {
        dialog.locator("#comments-graded").check();
        dialog.locator("#comments-max").fill(maxPoints);
    }

    private void postComment(String text) {
        Locator section = commentsSection().locator(".commentsDiv");
        section.locator("button.switchLink").click();
        String editorId = (String) section.locator("textarea").first().evaluate(
            "area => Object.values(CKEDITOR.instances).find(editor => editor.element.$ === area)?.name");
        sakai.typeCkEditor(editorId, "<p>" + text + "</p>");
        page.waitForNavigation(() -> section.locator("input.submitButton").click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(comment(text)).isVisible();
    }

    /** The page script numbers the status icons when it binds the points boxes, so wait for it. */
    private void waitForPointBoxes() {
        assertThat(page.locator("img.statusImg[id='statusImg']")).hasCount(0);
    }

    private void gradeWithEnter(Locator pointsBox, String points) {
        waitForPointBoxes();
        pointsBox.fill(points);
        pointsBox.press("Enter");
        assertThat(pointsBox.locator("xpath=..").locator("img.statusImg"))
            .hasAttribute("src", Pattern.compile("success\\.png$"));
    }

    /** The student's own Gradebook page shows the score that Lessons sent for their comments. */
    private Locator gradebookScore(TestUser student, String pageTitle) {
        sakai.login(student.username());
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        return page.locator(".gb-summary-grade-row")
            .filter(new Locator.FilterOptions().setHas(page.locator(".gb-summary-grade-title")
                .filter(new Locator.FilterOptions().setHasText(pageTitle + " Comments"))))
            .locator(".gb-summary-grade-score-raw");
    }

    private static Pattern points(String value) {
        return Pattern.compile("^\\s*" + value + "(?:\\.0+)?\\s*$");
    }

    @Test
    void anonymousCommentsHideTheirAuthors() {
        String title = newPage("Anonymous comments");
        lessons.addComments();
        Locator settings = openCommentsSettings();
        settings.locator("#comments-anonymous").check();
        saveCommentsSettings(settings);

        try (TestUser author = student("lessons-comments-author", "Ana");
                TestUser reader = student("lessons-comments-reader", "Blas")) {
            String text = "Anonymous comment " + sakai.randomId();
            openAsStudent(author, title);
            postComment(text);
            assertThat(comment(text).locator(".author")).hasText("Anonymous 1 (You)");

            openAsStudent(reader, title);
            assertThat(comment(text).locator(".author")).hasText("Anonymous 1");
            assertThat(comment(text)).not().containsText("Ana Test");

            openAsInstructor(title);
            assertThat(comment(text).locator(".author")).hasText("Anonymous 1");
        }
    }

    @Test
    void gradedCommentsAreGradedNextToEachComment() {
        String title = newPage("Graded comments");
        lessons.addComments();
        Locator settings = openCommentsSettings();
        gradeComments(settings, "10");
        saveCommentsSettings(settings);

        try (TestUser student = student("lessons-comments-graded", "Carmen")) {
            String text = "Graded comment " + sakai.randomId();
            openAsStudent(student, title);
            postComment(text);
            assertThat(gradebookScore(student, title)).hasText(Pattern.compile("^\\s*-\\s*$"));

            openAsInstructor(title);
            Locator pointsBox = comment(text).locator(".pointsBox");
            assertThat(comment(text)).containsText("/ 10");
            gradeWithEnter(pointsBox, "8");

            assertThat(gradebookScore(student, title)).hasText(points("8"));
        }
    }

    @Test
    void commentsWithPrerequisitesWaitForTheRequiredItems() {
        String title = newPage("Comments prerequisites");
        String subpage = "Reading " + sakai.randomId();
        lessons.addSubpage(subpage);
        sakai.toolClick(title);
        Locator subpageEditor = lessons.openItemEditor(lessons.item(subpage));
        subpageEditor.locator("#prereqstuff button.accordionBtn").click();
        subpageEditor.locator("#item-required").check();
        lessons.saveItemEditor(subpageEditor);
        lessons.addComments();
        Locator settings = openCommentsSettings();
        settings.locator("#comments-prerequisite").check();
        settings.locator("#comments-required").check();
        saveCommentsSettings(settings);

        try (TestUser student = student("lessons-comments-prereq", "Diego")) {
            openAsStudent(student, title);
            assertThat(commentsSection().locator(".sak-banner-warn"))
                .containsText("you must complete all required items above");
            assertThat(commentsSection().locator("button.switchLink")).isHidden();

            lessons.openSubpageFromParent(subpage);
            sakai.toolClick(title);
            String text = "Comment after the reading " + sakai.randomId();
            postComment(text);

            openAsInstructor(title);
            assertThat(comment(text).locator(".author")).hasText("Diego Test");
        }
    }

    @Test
    void commentsShownToAGroupAreHiddenFromOtherStudents() {
        String title = newPage("Group comments");
        try (TestUser member = student("lessons-comments-member", "Elisa");
                TestUser outsider = student("lessons-comments-outsider", "Fermin")) {
            String groupTitle = "Comments group " + sakai.randomId();
            sakai.addGroup(siteUrl, groupTitle, member.username());
            page.reload();
            lessons.addComments();
            Locator settings = openCommentsSettings();
            settings.locator("#editgroups-comments a").click();
            settings.locator("#grouplist div").filter(new Locator.FilterOptions().setHasText(groupTitle))
                .locator("input[type=checkbox]").check();
            saveCommentsSettings(settings);
            assertThat(commentsSection()).containsText(groupTitle);

            openAsStudent(outsider, title);
            assertThat(page.locator(".commentsDiv")).hasCount(0);

            openAsStudent(member, title);
            String text = "Group comment " + sakai.randomId();
            postComment(text);
        }
    }

    @Test
    void commentsGradingPaneGradesAndZeroesTheMissingStudents() {
        String title = newPage("Comments grading pane");
        lessons.addComments();
        Locator settings = openCommentsSettings();
        gradeComments(settings, "5");
        saveCommentsSettings(settings);

        try (TestUser commenter = student("lessons-comments-pane", "Gloria");
                TestUser missing = student("lessons-comments-missing", "Hugo")) {
            String text = "Comment to grade " + sakai.randomId();
            openAsStudent(commenter, title);
            postComment(text);

            openAsInstructor(title);
            Locator section = commentsSection();
            section.hover();
            section.locator("a.gradingPaneLink").click();
            Locator row = page.locator(".gradingTable tr.first-row")
                .filter(new Locator.FilterOptions().setHasText("Gloria Test"));
            assertThat(row).isVisible();
            row.locator(".toggleButton").click();
            assertThat(page.locator(".gradingTable .details-row").filter(new Locator.FilterOptions().setHasText(text)))
                .isVisible();
            gradeWithEnter(row.locator(".pointsBox"), "4");

            Locator notSubmitted = page.locator("h3").filter(new Locator.FilterOptions()
                .setHasText("Students with no submission")).locator("xpath=following-sibling::div[1]");
            assertThat(notSubmitted).containsText(missing.username());
            assertThat(notSubmitted).not().containsText(commenter.username());
            page.waitForNavigation(() -> page.locator("#zeroMissing").click());

            assertThat(gradebookScore(commenter, title)).hasText(points("4"));
            assertThat(gradebookScore(missing, title)).hasText(points("0"));
        }
    }
}
