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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.LoadState;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiHelper.TestUser;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Regression checks for creating, editing and answering Lessons questions. */
class LessonsQuestionEditingTest extends SakaiUiTestBase {

    private static final String INSTRUCTOR = "instructor1";
    private static final List<String> TOOLS = List.of("sakai\\.lessonbuildertool", "sakai\\.gradebookng");
    private static final String QUESTION_EDITOR = "question-text-area-evolved::input";

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

    private void openAsStudent(TestUser student, String pageTitle) {
        sakai.login(student.username());
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
    }

    private Locator dialog() {
        return page.locator("#question-dialog");
    }

    private void openNewQuestion(boolean multipleChoice) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        page.locator(".question-link:visible").click();
        assertThat(dialog()).isVisible();
        page.locator(multipleChoice ? "#multipleChoiceSelect" : "#shortanswerSelect").check();
    }

    /** Fills the visible answers, adding rows as needed. The first multiple choice answer is the correct one. */
    private void fillAnswers(boolean multipleChoice, boolean markCorrect, String... answers) {
        String section = multipleChoice ? "#multipleChoiceDialogDiv" : "#shortanswerDialogDiv";
        Locator inputs = answerInputs(multipleChoice);
        for (int i = 0; i < answers.length; i++) {
            if (i > 0) {
                page.locator(section).getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Add New Answer").setExact(true)).click();
            }
            inputs.nth(i).fill(answers[i]);
        }
        if (multipleChoice && markCorrect) {
            page.locator(".question-multiplechoice-answer-correct:visible").first().check();
        }
    }

    private Locator answerInputs(boolean multipleChoice) {
        return page.locator(multipleChoice
            ? ".question-multiplechoice-answer:visible" : ".question-shortanswer-answer:visible");
    }

    private Locator question(String text) {
        return page.locator(".questionDiv").filter(new Locator.FilterOptions().setHasText(text));
    }

    private void saveQuestion(String text) {
        page.locator("#update-question").click();
        assertThat(question(text).locator(".questionText")).isVisible();
    }

    /** The edit button of an item only appears while it is hovered. */
    private void editQuestion(String text) {
        Locator item = lessons.item(text);
        item.hover();
        item.locator("a.edit-question").click();
        assertThat(dialog()).isVisible();
    }

    private void removeAnswer(boolean multipleChoice, int index, String expectedValue) {
        Locator answer = answerInputs(multipleChoice).nth(index);
        assertThat(answer).hasValue(expectedValue);
        answer.locator("xpath=ancestor::tr[1]").locator(".deleteAnswerLink").click();
    }

    private Locator choice(Locator question, String label) {
        return question.getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions().setName(label).setExact(true));
    }

    /** The answer is posted with the page form, and the status text only exists when the question defines it. */
    private void submit(Locator question) {
        page.waitForResponse(response -> "POST".equals(response.request().method())
                && response.url().contains("/ShowPage"),
            () -> question.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Submit Answer").setExact(true)).click());
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(page.locator(".sak-banner-error").filter(new Locator.FilterOptions()
            .setHasText("Unable to save or update item"))).hasCount(0);
    }

    private Locator status(Locator question) {
        return question.locator(".statusCol span");
    }

    /** A disposable student with its own first name, so the grading pane and Gradebook rows can be told apart. */
    private TestUser student(String prefix, String firstName, boolean active) {
        TestUser student = sakai.createUser(prefix, firstName);
        try {
            if (active) {
                sakai.addMember(siteUrl, student.username(), "Student");
            } else {
                setStudentMembership(student, false);
            }
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

    /** Adds the student to the site, or updates their membership, as active or inactive. */
    private void setStudentMembership(TestUser student, boolean active) {
        APIResponse user = page.request().get("/direct/user/" + student.username() + ".json");
        assertTrue(user.ok(), user.text());
        String userId = JsonParser.parseString(user.text()).getAsJsonObject().get("id").getAsString();
        APIResponse membership = page.request().post("/direct/membership/new", RequestOptions.create()
            .setForm(FormData.create().set("locationReference", "/site/" + sakai.siteIdFromUrl(siteUrl))
                .set("userId", userId).set("memberRole", "Student").set("active", String.valueOf(active))));
        assertTrue(membership.ok(), membership.text());
    }

    private void createGradedQuestion(boolean multipleChoice, String text, String gradebookTitle, String... answers) {
        openNewQuestion(multipleChoice);
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + text + "</p>");
        if (answers.length > 0) {
            fillAnswers(multipleChoice, true, answers);
        }
        page.locator("#question-graded").check();
        page.locator("#question-gradebook-title").fill(gradebookTitle);
        page.locator("#question-max").fill("10");
        page.locator("#question-required").check();
        saveQuestion(text);
    }

    private void answer(TestUser student, String pageTitle, String text, String shortAnswer, String choice) {
        openAsStudent(student, pageTitle);
        Locator question = question(text);
        if (shortAnswer != null) {
            question.locator(".shortAnswerInput").fill(shortAnswer);
        } else {
            choice(question, choice).check();
        }
        submit(question);
    }

    /** The grading pane link of a question only appears while it is hovered. */
    private void openGradingPane(String pageTitle, String text) {
        sakai.login(INSTRUCTOR);
        page.navigate(siteUrl);
        sakai.toolClick(pageTitle);
        Locator item = lessons.item(text);
        item.hover();
        item.locator("a.gradingPaneLink").click();
        assertThat(page.locator(".gradingPane")).containsText(text);
        // The page script numbers the status icons when it binds the points boxes, so wait for it
        // before typing a grade.
        assertThat(page.locator(".gradingTable img.statusImg[id='statusImg']")).hasCount(0);
    }

    private Locator gradingRow(String firstName) {
        return page.locator(".gradingTable tr").filter(new Locator.FilterOptions().setHasText(", " + firstName));
    }

    /** The student's own Gradebook page shows the score that Lessons sent for the question. */
    private Locator gradebookScore(TestUser student, String gradebookTitle) {
        return gradebookRow(student, gradebookTitle).locator(".gb-summary-grade-score-raw");
    }

    private Locator gradebookRow(TestUser student, String gradebookTitle) {
        sakai.login(student.username());
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        return page.locator(".gb-summary-grade-row")
            .filter(new Locator.FilterOptions().setHas(page.locator(".gb-summary-grade-title")
                .filter(new Locator.FilterOptions().setHasText(gradebookTitle))));
    }

    private static Pattern points(String value) {
        return Pattern.compile("^\\s*" + value + "(?:\\.0+)?\\s*$");
    }

    @Test
    void questionTextCanStartFromATemplate() {
        newPage("Question template");
        openNewQuestion(false);
        page.locator("[id='cke_" + QUESTION_EDITOR + "'] .cke_button__templates").click();
        page.locator(".cke_dialog:visible .cke_tpl_list a")
            .filter(new Locator.FilterOptions().setHasText("Reading Assignment")).click();
        assertThat(page.locator(".cke_dialog:visible")).hasCount(0);
        fillAnswers(false, false, "chapters 1 and 2");
        saveQuestion("Opening Up Education");

        assertThat(question("Opening Up Education").locator(".questionText h3")).containsText("Reading Assignment");
    }

    @Test
    void removedMultipleChoiceAnswerRelabelsTheOthers() {
        newPage("Question answers");
        String text = "Pick the first letter " + sakai.randomId();
        openNewQuestion(true);
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + text + "</p>");
        fillAnswers(true, true, "Alpha", "Beta", "Gamma");
        saveQuestion(text);
        assertThat(question(text).getByRole(AriaRole.RADIO)).hasCount(3);

        editQuestion(text);
        removeAnswer(true, 1, "Beta");
        assertThat(answerInputs(true)).hasCount(2);
        assertThat(page.locator(".question-multiplechoice-answer-option:visible")).hasText(new String[] {"A", "B"});
        saveQuestion(text);

        Locator question = question(text);
        assertThat(question.getByRole(AriaRole.RADIO)).hasCount(2);
        assertThat(choice(question, "A : Alpha")).isVisible();
        assertThat(choice(question, "B : Gamma")).isVisible();
    }

    @Test
    void shortAnswerStopsAcceptingARemovedAnswer() {
        String title = newPage("Short answers");
        String text = "Name a primary colour " + sakai.randomId();
        openNewQuestion(false);
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + text + "</p>");
        fillAnswers(false, false, "red", "blue");
        page.locator("#question-correct-text").fill("Accepted");
        page.locator("#question-incorrect-text").fill("Not accepted");
        saveQuestion(text);

        Locator question = question(text);
        question.locator(".shortAnswerInput").fill("blue");
        submit(question);
        assertThat(question.locator(".questionStatusText")).hasText("Accepted");

        editQuestion(text);
        removeAnswer(false, 1, "blue");
        assertThat(answerInputs(false)).hasCount(1);
        saveQuestion(text);

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-short-answer")) {
            openAsStudent(student, title);
            question.locator(".shortAnswerInput").fill("blue");
            submit(question);
            assertThat(question.locator(".questionStatusText")).hasText("Not accepted");
            assertThat(status(question)).hasClass(Pattern.compile("\\bfa-times\\b"));
        }
    }

    @Test
    void gradedQuestionWaitsForTheInstructor() {
        String title = newPage("Graded question");
        String text = "Explain your choice " + sakai.randomId();
        String gradebookTitle = "Lessons question " + sakai.randomId();
        openNewQuestion(true);
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + text + "</p>");
        fillAnswers(true, false, "First option", "Second option", "Third option");
        page.locator("#question-graded").check();
        page.locator("#question-gradebook-title").fill(gradebookTitle);
        page.locator("#question-max").fill("5");
        saveQuestion(text);

        try (TestUser student = lessons.newStudent(siteUrl, "lessons-graded-question")) {
            openAsStudent(student, title);
            Locator question = question(text);
            choice(question, "B : Second option").check();
            submit(question);
            assertThat(status(question)).hasClass(Pattern.compile("\\bfa-question\\b"));
        }

        sakai.login(INSTRUCTOR);
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        Locator header = page.locator("#gradeTable .relative").filter(new Locator.FilterOptions()
            .setHas(page.locator("a.gb-title").filter(new Locator.FilterOptions().setHasText(gradebookTitle))));
        assertThat(header.locator(".gb-total-points")).hasText(Pattern.compile("\\s*5(?:\\.0+)?\\s*"));
    }

    @Test
    void requiredQuestionShowsStudentsTheirResult() {
        String title = newPage("Required question");
        String text = "Which one is a vowel " + sakai.randomId();
        openNewQuestion(true);
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + text + "</p>");
        fillAnswers(true, true, "E", "K", "T", "S");
        page.locator("#question-required").check();
        saveQuestion(text);

        Locator question = question(text);
        try (TestUser student = lessons.newStudent(siteUrl, "lessons-required-wrong")) {
            openAsStudent(student, title);
            assertThat(status(question)).hasClass(Pattern.compile("\\bfa-asterisk\\b"));
            choice(question, "C : T").check();
            submit(question);
            assertThat(status(question)).hasClass(Pattern.compile("\\bfa-times\\b"));
        }
        try (TestUser student = lessons.newStudent(siteUrl, "lessons-required-right")) {
            openAsStudent(student, title);
            choice(question, "A : E").check();
            submit(question);
            assertThat(status(question)).hasClass(Pattern.compile("\\bfa-check\\b"));
        }
    }

    @Test
    void gradedShortAnswerWithoutAnswersIsGradedByTheInstructor() {
        String title = newPage("Manual grading");
        String text = "Describe photosynthesis " + sakai.randomId();
        String gradebookTitle = "Lessons short answer " + sakai.randomId();
        createGradedQuestion(false, text, gradebookTitle);

        try (TestUser student = student("lessons-manual-grading", "Alba", true)) {
            answer(student, title, text, "Plants turn light into energy", null);
            assertThat(status(question(text))).hasClass(Pattern.compile("\\bfa-question\\b"));

            openGradingPane(title, text);
            Locator row = gradingRow("Alba");
            assertThat(row).containsText("Plants turn light into energy");
            Locator pointsBox = row.locator(".pointsBox");
            assertThat(pointsBox).hasValue("");
            pointsBox.fill("7");
            pointsBox.press("Enter");
            assertThat(row.locator("img.statusImg")).hasAttribute("src", Pattern.compile("success\\.png$"));

            assertThat(gradebookScore(student, gradebookTitle)).hasText(points("7"));
        }
    }

    @Test
    void gradingPaneListsActiveStudentsAndZeroesTheMissingOnes() {
        String title = newPage("Grading pane");
        String text = "Which city is the capital of France " + sakai.randomId();
        String gradebookTitle = "Lessons multiple choice " + sakai.randomId();
        createGradedQuestion(true, text, gradebookTitle, "Paris", "Lyon");

        try (TestUser right = student("lessons-grading-right", "Bruno", true);
                TestUser wrong = student("lessons-grading-wrong", "Clara", true);
                TestUser missing = student("lessons-grading-missing", "Dario", true);
                TestUser inactive = student("lessons-grading-inactive", "Elena", false)) {
            answer(right, title, text, null, "A : Paris");
            answer(wrong, title, text, null, "B : Lyon");

            openGradingPane(title, text);
            assertThat(gradingRow("Bruno").locator(".pointsBox")).hasValue(points("10"));
            assertThat(gradingRow("Clara").locator(".pointsBox")).hasValue(points("0"));
            Locator notSubmitted = page.locator(".gradingPane h3")
                .filter(new Locator.FilterOptions().setHasText("Students with no submission"))
                .locator("xpath=following-sibling::div[1]");
            assertThat(notSubmitted).containsText(missing.username());
            assertThat(notSubmitted).not().containsText(inactive.username());
            assertThat(notSubmitted).not().containsText(right.username());

            page.waitForNavigation(() -> page.locator("#zeroMissing").click());
            assertThat(gradebookScore(right, gradebookTitle)).hasText(points("10"));
            assertThat(gradebookScore(wrong, gradebookTitle)).hasText(points("0"));
            assertThat(gradebookScore(missing, gradebookTitle)).hasText(points("0"));

            // The inactive student was not listed, so the button must not have given them a score.
            sakai.login(INSTRUCTOR);
            setStudentMembership(inactive, true);
            Locator inactiveRow = gradebookRow(inactive, gradebookTitle);
            assertThat(inactiveRow).isVisible();
            // Gradebook shows a dash for an item without a score.
            assertThat(inactiveRow.locator(".gb-summary-grade-score-raw")).hasText(Pattern.compile("^\\s*-\\s*$"));
        }
    }

    @Test
    void cancelledQuestionIsNotCreatedAndDeletedQuestionIsRemoved() {
        newPage("Question cancel");
        String cancelled = "Cancelled question " + sakai.randomId();
        openNewQuestion(true);
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + cancelled + "</p>");
        fillAnswers(true, true, "Yes", "No");
        dialog().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        assertThat(dialog()).isHidden();
        page.reload();
        assertThat(question(cancelled)).hasCount(0);

        String deleted = "Deleted question " + sakai.randomId();
        openNewQuestion(true);
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + deleted + "</p>");
        fillAnswers(true, true, "Yes", "No");
        saveQuestion(deleted);
        editQuestion(deleted);
        // Computing accessible names inside the edited dialog fails in Playwright, so use the button class.
        dialog().locator("button.deleteButton").click();
        assertThat(page.locator("#addcontent")).isAttached();
        assertThat(question(deleted)).hasCount(0);
    }

    @Test
    @Disabled("Open bug: saving a question keeps its auto-saved text, so the next page load asks to restore it")
    void savedQuestionDoesNotOfferToRestoreItsText() {
        newPage("Question autosave");
        String text = "Auto-saved question " + sakai.randomId();
        List<String> dialogs = new ArrayList<>();
        page.onDialog(dialog -> {
            dialogs.add(dialog.message());
            dialog.dismiss();
        });
        openNewQuestion(false);
        sakai.typeCkEditor(QUESTION_EDITOR, "<p>" + text + "</p>");
        fillAnswers(false, false, "anything");
        // The editor stores its content a few seconds after a change.
        page.waitForFunction("() => Object.keys(localStorage).some(key => key.startsWith('autosave_')"
            + " && key.includes('question-text-area-evolved'))");
        saveQuestion(text);

        openNewQuestion(true);
        assertEquals(List.of(), dialogs);
    }
}
