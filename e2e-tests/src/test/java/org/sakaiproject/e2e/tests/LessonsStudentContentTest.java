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

/** Regression checks for the student content sections of Lessons. */
class LessonsStudentContentTest extends SakaiUiTestBase {

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

    /** A disposable student with its own first name, so their pages can be told apart. */
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

    private void openAs(String username, String pageTitle) {
        sakai.login(username);
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
    }

    private Locator studentSection() {
        return page.locator(".studentDiv").first().locator("xpath=..");
    }

    private Locator studentPageLinks() {
        return studentSection().locator("a.studentLink");
    }

    private Locator missingPages() {
        return studentSection().locator(".missingStudent");
    }

    /** The edit button of the student content section only appears while it is hovered. */
    private Locator openStudentSettings() {
        Locator section = studentSection();
        section.hover();
        section.locator("a.edit-student").click();
        Locator dialog = page.locator("#student-dialog");
        assertThat(dialog).isVisible();
        return dialog;
    }

    private void saveStudentSettings(Locator dialog) {
        page.waitForNavigation(() -> dialog.locator("#update-student").click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(page.locator("#addcontent")).isAttached();
    }

    /** Creates the student's own page from the section and adds some text to it. */
    private void createOwnPage(String text) {
        page.waitForNavigation(() -> studentSection().locator("input.addStudentContent").click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        lessons.addText("<p>" + text + "</p>");
        assertThat(page.getByText(text)).isVisible();
    }

    private void openStudentPage(String linkText) {
        page.waitForNavigation(() -> studentPageLinks()
            .filter(new Locator.FilterOptions().setHasText(linkText)).first().click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    private Locator commentsSection() {
        return page.locator(".commentsCell").first();
    }

    private Locator comment(String text) {
        return commentsSection().locator(".commentDiv").filter(new Locator.FilterOptions().setHasText(text));
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

    /** The student's own Gradebook page shows the score that Lessons sent for the item. */
    private Locator gradebookScore(TestUser student, String gradebookTitle) {
        sakai.login(student.username());
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        return page.locator(".gb-summary-grade-row")
            .filter(new Locator.FilterOptions().setHas(page.locator(".gb-summary-grade-title")
                .filter(new Locator.FilterOptions().setHasText(gradebookTitle))))
            .locator(".gb-summary-grade-score-raw");
    }

    private static Pattern points(String value) {
        return Pattern.compile("^\\s*" + value + "(?:\\.0+)?\\s*$");
    }

    @Test
    void studentsCreateTheirOwnPagesAndLeaveTheMissingList() {
        String title = newPage("Student pages");
        try (TestUser author = student("lessons-student-author", "Ines");
                TestUser missing = student("lessons-student-missing", "Jorge")) {
            lessons.addStudentContent();
            assertThat(studentSection().locator(".studentContentTitle").first()).hasText("Student Pages");
            assertThat(studentSection().locator(".missingStudentTitle")).hasText("Missing Pages");
            assertThat(missingPages().filter(new Locator.FilterOptions().setHasText("Ines Test"))).hasCount(1);

            String text = "My own page " + sakai.randomId();
            openAs(author.username(), title);
            assertThat(studentSection().locator("input.addStudentContent")).hasValue("Add Your Own Page");
            createOwnPage(text);
            sakai.toolClick(title);
            assertThat(studentPageLinks()).hasText("Ines Test");
            assertThat(studentSection().locator("input.addStudentContent")).hasCount(0);

            openAs(INSTRUCTOR, title);
            assertThat(missingPages().filter(new Locator.FilterOptions().setHasText("Ines Test"))).hasCount(0);
            assertThat(missingPages().filter(new Locator.FilterOptions().setHasText("Jorge Test"))).hasCount(1);
            openStudentPage("Ines Test");
            assertThat(page.getByText(text)).isVisible();
        }
    }

    @Test
    void anonymousStudentPagesHideTheirOwnersFromOtherStudents() {
        String title = newPage("Anonymous student pages");
        lessons.addStudentContent();
        Locator settings = openStudentSettings();
        settings.locator("#student-anonymous").check();
        saveStudentSettings(settings);

        try (TestUser author = student("lessons-student-anon", "Karen");
                TestUser reader = student("lessons-student-reader", "Luis")) {
            openAs(author.username(), title);
            createOwnPage("Anonymous page " + sakai.randomId());
            sakai.toolClick(title);
            assertThat(studentPageLinks()).hasText("Anonymous 1 (You)");

            openAs(reader.username(), title);
            assertThat(studentPageLinks()).hasText("Anonymous 1");

            openAs(INSTRUCTOR, title);
            assertThat(studentPageLinks()).hasText("Anonymous 1 (Karen Test)");
        }
    }

    @Test
    void gradedStudentPagesAreGradedFromThePageItself() {
        String title = newPage("Graded student pages");
        String gradebookTitle = "Student pages " + sakai.randomId();
        lessons.addStudentContent();
        Locator settings = openStudentSettings();
        settings.locator("#student-graded").check();
        settings.locator("#gradebook-title").fill(gradebookTitle);
        settings.locator("#student-max").fill("50");
        saveStudentSettings(settings);

        try (TestUser author = student("lessons-student-graded", "Marta")) {
            openAs(author.username(), title);
            createOwnPage("Page to grade " + sakai.randomId());
            assertThat(gradebookScore(author, gradebookTitle)).hasText(Pattern.compile("^\\s*-\\s*$"));

            openAs(INSTRUCTOR, title);
            openStudentPage("Marta Test");
            Locator pointsBox = page.locator("#studentPointsBox");
            assertThat(pointsBox.locator("xpath=..")).containsText("50");
            pointsBox.fill("40");
            page.locator("#submit-grading").click();
            assertThat(page.locator(".gradingarea img.statusImg")).hasAttribute("src", Pattern.compile("success\\.png$"));

            assertThat(gradebookScore(author, gradebookTitle)).hasText(points("40"));
        }
    }

    @Test
    void commentsOnStudentPagesCanBeAnonymousAndGraded() {
        String title = newPage("Student page comments");
        lessons.addStudentContent();
        Locator settings = openStudentSettings();
        settings.locator("#student-comments").check();
        settings.locator("#student-comments-anon").check();
        settings.locator("#student-comments-graded").check();
        settings.locator("#student-comments-max").fill("5");
        saveStudentSettings(settings);

        try (TestUser owner = student("lessons-student-owner", "Nora");
                TestUser commenter = student("lessons-student-commenter", "Oscar")) {
            openAs(owner.username(), title);
            createOwnPage("Page with comments " + sakai.randomId());

            String text = "Comment on a classmate's page " + sakai.randomId();
            openAs(commenter.username(), title);
            openStudentPage("Nora Test");
            postComment(text);
            assertThat(comment(text).locator(".author")).hasText("Anonymous 1 (You)");

            openAs(owner.username(), title);
            openStudentPage("Nora Test");
            assertThat(comment(text).locator(".author")).hasText("Anonymous 1");

            // Comments on student pages are graded from the grading pane of the section.
            openAs(INSTRUCTOR, title);
            Locator section = studentSection();
            section.hover();
            section.locator("a.gradingPaneLink").click();
            Locator row = page.locator(".gradingTable tr.first-row")
                .filter(new Locator.FilterOptions().setHasText("Oscar Test"));
            assertThat(row).isVisible();
            assertThat(page.locator("img.statusImg[id='statusImg']")).hasCount(0);
            Locator pointsBox = row.locator(".pointsBox");
            pointsBox.fill("3");
            pointsBox.press("Enter");
            assertThat(row.locator("img.statusImg")).hasAttribute("src", Pattern.compile("success\\.png$"));

            assertThat(gradebookScore(commenter, title + " Student Page Comments")).hasText(points("3"));
        }
    }

    @Test
    void groupOwnedStudentPagesAreSharedByTheGroupMembers() {
        String title = newPage("Group student pages");
        try (TestUser first = student("lessons-student-group-a", "Pablo");
                TestUser second = student("lessons-student-group-b", "Quima")) {
            String groupTitle = "Student pages group " + sakai.randomId();
            sakai.addGroup(siteUrl, groupTitle, first.username(), second.username());
            page.reload();
            lessons.addStudentContent();
            Locator settings = openStudentSettings();
            settings.locator("#student-group-owned").check();
            settings.locator("#student-grouplist p").filter(new Locator.FilterOptions().setHasText(groupTitle))
                .locator("input[type=checkbox]").check();
            // Most students of the course are in no group, so Lessons warns once before saving.
            settings.locator("#update-student").click();
            assertThat(settings.locator("#student-group-errors-container")).isVisible();
            saveStudentSettings(settings);
            assertThat(missingPages().filter(new Locator.FilterOptions().setHasText(groupTitle))).hasCount(1);

            String text = "Shared group page " + sakai.randomId();
            openAs(first.username(), title);
            createOwnPage(text);

            openAs(second.username(), title);
            assertThat(studentPageLinks()).hasText(groupTitle);
            assertThat(studentSection().locator("input.addStudentContent")).hasCount(0);
            openStudentPage(groupTitle);
            assertThat(page.getByText(text)).isVisible();
        }
    }

    @Test
    void peerReviewRubricIsFilledByClassmatesAndShownInTheStatistics() {
        String title = newPage("Peer review");
        String rubricTitle = "Peer review " + sakai.randomId();
        lessons.addStudentContent();
        Locator settings = openStudentSettings();
        settings.locator("#peer-eval-check").check();
        settings.locator("input.peer-eval-input-title").first().fill(rubricTitle);
        settings.locator("#addRowBtn").click();
        settings.locator("#addRowBtn").click();
        Locator categories = settings.locator("#peer-eval-create-table tbody tr input.peer-eval-input-row");
        categories.nth(0).fill("Clarity");
        categories.nth(1).fill("Content");
        // The review is only shown between its open and due dates, so open it yesterday.
        page.evaluate("""
            () => {
                const iso = date => new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().substring(0, 19);
                const day = 24 * 60 * 60 * 1000;
                document.getElementById('peer_eval_open_dateISO8601').value = iso(new Date(Date.now() - day));
                document.getElementById('peer_eval_due_dateISO8601').value = iso(new Date(Date.now() + 7 * day));
            }
            """);
        saveStudentSettings(settings);

        try (TestUser owner = student("lessons-peer-owner", "Raquel");
                TestUser reviewer = student("lessons-peer-reviewer", "Santi")) {
            openAs(owner.username(), title);
            createOwnPage("Page to review " + sakai.randomId());

            openAs(reviewer.username(), title);
            openStudentPage("Raquel Test");
            Locator rubric = page.locator(".peer-eval-div");
            page.locator(".add-peereval-button button").click();
            assertThat(rubric.locator(".peer-eval-title-student")).hasText(rubricTitle);
            Locator clarity = rubric.locator("tr.peer-eval-row").filter(new Locator.FilterOptions().setHasText("Clarity"));
            Locator content = rubric.locator("tr.peer-eval-row").filter(new Locator.FilterOptions().setHasText("Content"));
            clarity.locator("td[class~='3']").click();
            content.locator("td[class~='4']").click();
            page.waitForNavigation(() -> rubric.locator("input.save-peereval-link").click());
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            assertThat(page.locator("tr.peer-eval-row").filter(new Locator.FilterOptions().setHasText("Clarity"))
                .locator("td.selectedPeerCell")).hasClass(Pattern.compile("(^|\\s)3(\\s|$)"));

            openAs(INSTRUCTOR, title);
            page.waitForNavigation(() -> studentSection().locator("a.peer-eval-stats").click());
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            Locator gradee = page.locator(".peer-eval-gradee-branch")
                .filter(new Locator.FilterOptions().setHas(page.locator(".user-name").filter(
                    new Locator.FilterOptions().setHasText("Raquel Test"))));
            assertThat(gradee).hasCount(1);
            assertThat(gradee.locator(".peer-eval-grader-name").first()).containsText("Santi Test");
            // The page script draws a rubric per student from that data, with the count of each rating.
            Locator rubricOfOwner = page.locator(".rubric-set").filter(new Locator.FilterOptions().setHas(
                page.locator(".rubric-name").filter(new Locator.FilterOptions().setHasText("Raquel Test"))));
            assertThat(ratingCount(rubricOfOwner, "Clarity", "3")).hasText(points("1"));
            assertThat(ratingCount(rubricOfOwner, "Content", "4")).hasText(points("1"));
        }
    }

    private Locator ratingCount(Locator rubric, String category, String rating) {
        return rubric.locator(".peer-eval-row").filter(new Locator.FilterOptions().setHasText(category))
            .locator("td[class~='" + rating + "'] .rating");
    }
}
