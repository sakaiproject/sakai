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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiEnvironment;
import org.sakaiproject.e2e.support.SakaiHelper;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Creates one course, three quizzes and real submissions before exercising ordered cancellation flows. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SamigoCancellationCategoryTest extends SakaiUiTestBase {
    private static String siteUrl;
    private static final String FIRST_QUIZ = "SAK-52130 Cancellation Q1";
    private static final String SECOND_QUIZ = "SAK-52130 Cancellation Q2";
    private static final String THIRD_QUIZ = "SAK-52130 Cancellation Q3";
    private static final String RESTRICTION = "This Gradebook category uses Keep/Drop and requires equal point totals.";

    @BeforeEach
    void prepareCourse() {
        loginInstructor();
        if (siteUrl == null) {
            createFixtures();
        }
    }

    private void loginInstructor() {
        sakai.login("instructor1");
        assertThat(page.locator("#sakai-account-panel")).hasCount(1);
    }

    @Test
    @Order(1)
    void bothRestrictedDialogsExplainReductionAndDismissalPreservesGrades() {
        openGradebook(siteUrl);
        changeCategorySettings(page, true, false);
        openGradebook(siteUrl);
        assertThat(studentRow().locator("[role='gridcell'][aria-label*='Course Grade']")).containsText("83.33");
        assertGradebookPoints(FIRST_QUIZ, "3");
        openEvaluation(siteUrl, FIRST_QUIZ);
        openCancellation();
        assertRestrictedDialog();
        modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Do not cancel question")).press("Enter");
        assertThat(modal()).isHidden();
        assertThat(page.locator("[data-item-cancellable]")).hasCount(1);
        openPublishedAuthoring(siteUrl, FIRST_QUIZ);
        assertAuthoringPoints("3");
        page.locator("[data-item-cancellable]").first().press("Enter");
        assertRestrictedDialog();
        modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Do not cancel question")).click();
        assertAuthoringPoints("3");
        openGradebook(siteUrl);
        assertThat(studentRow().locator("[role='gridcell'][aria-label*='Course Grade']")).containsText("83.33");
        assertGradebookPoints(FIRST_QUIZ, "3");
        assertStudentAssignmentScore(FIRST_QUIZ, "2");
    }

    @Test
    @Order(3)
    void redistributionTargetsSelectedQuestionAndSynchronizesThreePossiblePoints() {
        openGradebook(siteUrl);
        changeCategorySettings(page, true, false);
        openEvaluation(siteUrl, FIRST_QUIZ);
        openCancellation();
        assertRestrictedDialog();
        modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Equally distribute points")).click();
        assertThat(page.getByText("This question has been cancelled and will not affect the total score.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        openPublishedAuthoring(siteUrl, FIRST_QUIZ);
        assertAuthoringPoints("3");
        assertThat(page.locator("input[id$='answerptr']")).hasCount(3);
        assertThat(page.locator("input[id$='answerptr']").nth(0)).hasValue(Pattern.compile("0(?:\\.0+)?"));
        assertThat(page.locator("input[id$='answerptr']").nth(1)).hasValue("1.5");
        assertThat(page.locator("input[id$='answerptr']").nth(2)).hasValue("1.5");
        openGradebook(siteUrl);
        assertGradebookPoints(FIRST_QUIZ, "3");
        assertStudentAssignmentScore(FIRST_QUIZ, "1.5");
    }

    @Test
    @Order(4)
    void equalWeightCategoryAllowsReductionAndSynchronizesTwoPossiblePoints() {
        openGradebook(siteUrl);
        changeCategorySettings(page, true, true);
        reduceAndAssertTwoPoints(SECOND_QUIZ, "1");
    }

    @Test
    @Order(5)
    void ordinaryCategoryAllowsReductionAndSynchronizesTwoPossiblePoints() {
        openGradebook(siteUrl);
        changeCategorySettings(page, false, false);
        reduceAndAssertTwoPoints(THIRD_QUIZ, "2");
    }

    @Test
    @Order(2)
    void staleReductionFormCannotChangePointsAfterCategoryBecomesRestricted() {
        openGradebook(siteUrl);
        changeCategorySettings(page, false, false);
        openEvaluation(siteUrl, FIRST_QUIZ);
        openCancellation();
        Locator reduction = modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reduce total points"));
        assertThat(reduction).isEnabled();
        Page settingsPage = context.newPage();
        try {
            settingsPage.navigate(siteUrl);
            new SakaiHelper(settingsPage, SakaiEnvironment.baseUrl(), settingsPage.request()).toolClick("Gradebook");
            changeCategorySettings(settingsPage, true, false);
            reduction.click();
            assertThat(page.locator("[data-item-cancellable]")).hasCount(1);
            openCancellation();
            assertRestrictedDialog();
            modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Do not cancel question")).click();
        } finally {
            settingsPage.close();
        }
        openPublishedAuthoring(siteUrl, FIRST_QUIZ);
        assertAuthoringPoints("3");
        assertThat(page.locator("input[id$='answerptr']").first()).hasValue(Pattern.compile("1(?:\\.0+)?"));
        openGradebook(siteUrl);
        assertGradebookPoints(FIRST_QUIZ, "3");
        assertStudentAssignmentScore(FIRST_QUIZ, "2");
    }

    private void createFixtures() {
        siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo", "sakai\\.gradebookng"));
        System.out.println("SAK-52130 cancellation course: " + siteUrl);
        openGradebook(siteUrl);
        openCategorySettings(page);
        page.getByLabel("Categories only", new Page.GetByLabelOptions().setExact(true)).check();
        page.locator(".gb-category-row input[name$='name']").first().fill("Quizzes");
        saveCategorySettings(page);
        for (String title : List.of(FIRST_QUIZ, SECOND_QUIZ, THIRD_QUIZ)) {
            createQuiz(title);
        }
        openGradebook(siteUrl);
        changeCategorySettings(page, true, false);
        sakai.login("student0011");
        assertThat(page.locator("#sakai-account-panel")).hasCount(1);
        submitQuiz(FIRST_QUIZ, List.of(0, 0, 1));
        submitQuiz(SECOND_QUIZ, List.of(1, 0, 1));
        submitQuiz(THIRD_QUIZ, List.of(0, 0, 0));
        loginInstructor();
        openGradebook(siteUrl);
        assertStudentAssignmentScore(FIRST_QUIZ, "2");
        assertStudentAssignmentScore(SECOND_QUIZ, "1");
        assertStudentAssignmentScore(THIRD_QUIZ, "3");
    }

    private void createQuiz(String title) {
        openAssessments(siteUrl);
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).click();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click();
        for (int question = 1; question <= 3; question++) {
            Locator type = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
            String value = type.locator("option").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("multiple choice", Pattern.CASE_INSENSITIVE))).first().getAttribute("value");
            type.selectOption(value);
            page.locator("#itemForm\\:answerptr").fill("1");
            page.locator("#itemForm textarea").first().fill("Question " + question + ": choose Correct.");
            page.locator("#itemForm\\:mcchoices textarea").nth(0).fill("Correct");
            page.locator("#itemForm\\:mcchoices textarea").nth(1).fill("Incorrect");
            page.locator("#itemForm\\:mcchoices input[type='radio']").nth(0).check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first().click();
            assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(question);
        }
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Settings").setExact(true)).first().click();
        page.locator("#expandLink").click();
        DateTimeFormatter dateTime12h = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US);
        sakai.selectDate("#assessmentSettingsAction\\:startDate",
            LocalDateTime.now().minusDays(1).format(dateTime12h).toLowerCase(Locale.US));
        sakai.selectDate("#assessmentSettingsAction\\:endDate",
            LocalDateTime.now().plusYears(2).format(dateTime12h).toLowerCase(Locale.US));
        page.locator("[id='assessmentSettingsAction:honor_pledge']").check();
        page.locator("#assessmentSettingsAction\\:toDefaultGradebook input[value='1']").check();
        page.locator("#assessmentSettingsAction\\:selectCategory").selectOption(
            new com.microsoft.playwright.options.SelectOption().setLabel("Quizzes"));
        page.getByLabel("The complete assessment is displayed on one web page", new Page.GetByLabelOptions().setExact(true)).check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Settings and Publish").setExact(true)).click();
        page.locator("#publishAssessmentForm\\:publish").click();
        assertThat(publishedRow(title)).hasCount(1);
        assertThat(publishedRow(title).locator(".status_published_2")).isVisible();
    }

    private void submitQuiz(String title, List<Integer> answers) {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.locator("[id='selectIndexForm:selectTable'] a[id$=':takeAssessment']")
            .filter(new Locator.FilterOptions().setHasText(title)).click();
        page.locator("[id='takeAssessmentForm:honor_pledge']").check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Begin Assessment").setExact(true)).click();
        Locator questions = page.locator(".samigo-question-callout");
        assertThat(questions).hasCount(3);
        for (int question = 0; question < answers.size(); question++) {
            questions.nth(question).getByRole(AriaRole.RADIO).nth(answers.get(question)).check();
        }
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit for Grading").setExact(true)).first().click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Assessment Submission Warning").setExact(true))).isVisible();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit for Grading").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Submission " + title).setExact(true))).isVisible();
        assertThat(page.getByText("Confirmation Number", new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    private void openGradebook(String siteUrl) {
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        assertThat(page.locator("#gradeTable")).isVisible();
    }

    private Locator publishedRow(String title) {
        return page.locator("#authorIndexForm\\:coreAssessments tbody tr")
                .filter(new Locator.FilterOptions().setHasText(title))
                .filter(new Locator.FilterOptions().setHas(page.locator("span[class^='status_published_']")));
    }

    private void openAssessments(String siteUrl) {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        assertThat(page.locator("#authorIndexForm")).isVisible();
    }

    private void openEvaluation(String siteUrl, String title) {
        openAssessments(siteUrl);
        Locator row = publishedRow(title);
        assertThat(row).hasCount(1);
        row.locator("td.submitted a").first().click();
        assertThat(page.locator("#editTotalResults")).isVisible();
        page.locator("[id$='questionScoresMenuLink'] a").click();
        assertThat(page.locator("[data-item-cancellable]")).hasCount(1);
    }

    private void openPublishedAuthoring(String siteUrl, String title) {
        openAssessments(siteUrl);
        Locator row = publishedRow(title);
        assertThat(row).hasCount(1);
        row.locator("button.dropdown-toggle").first().click();
        row.locator("a.hiddenBtn_edit_published").first().click();
        Locator confirmation = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Edit").setExact(true));
        assertThat(confirmation.or(page.locator("input[id$='answerptr']").first()).first()).isVisible();
        if (confirmation.count() > 0) {
            confirmation.first().click();
        }
        assertThat(page.locator("input[id$='answerptr']")).hasCount(3);
    }

    private void openCancellation() {
        page.locator("[data-item-cancellable]").first().click();
        assertThat(modal()).isVisible();
    }

    private Locator modal() {
        return page.locator("#cancelQuestionModal");
    }

    private void assertRestrictedDialog() {
        assertThat(modal()).isVisible();
        // Bootstrap moves focus to the dialog after its opening transition completes.
        assertThat(modal()).isFocused();
        assertThat(modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reduce total points"))).isDisabled();
        assertThat(modal().getByText(RESTRICTION)).isVisible();
        assertThat(modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Equally distribute points"))).isEnabled();
    }

    private void reduceAndAssertTwoPoints(String title, String earnedScore) {
        openEvaluation(siteUrl, title);
        openCancellation();
        Locator reduction = modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reduce total points"));
        assertThat(reduction).isEnabled();
        reduction.click();
        assertThat(page.getByText("This question has been cancelled and will not affect the total score.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        openPublishedAuthoring(siteUrl, title);
        assertAuthoringPoints("2");
        assertThat(page.locator("input[id$='answerptr']").first()).hasValue(Pattern.compile("0(?:\\.0+)?"));
        openGradebook(siteUrl);
        assertGradebookPoints(title, "2");
        assertStudentAssignmentScore(title, earnedScore);
    }

    private void assertAuthoringPoints(String points) {
        assertThat(page.locator(".navList").filter(new Locator.FilterOptions().setHasText(Pattern.compile("existing questions", Pattern.CASE_INSENSITIVE))))
                .containsText(points + " total points");
    }

    private Locator studentRow() {
        Locator row = page.locator("#gradeTable [role='rowheader']")
                .filter(new Locator.FilterOptions().setHasText("student0011"));
        assertThat(row).hasCount(1);
        return row;
    }

    private void assertStudentAssignmentScore(String title, String score) {
        Locator titleLink = page.locator("#gradeTable a.gb-title")
                .filter(new Locator.FilterOptions().setHasText(title));
        String field = titleLink.locator("xpath=ancestor::*[@tabulator-field][1]").getAttribute("tabulator-field");
        assertNotNull(field, "The assignment header must identify its student-grade column");
        assertThat(studentRow().locator("[tabulator-field='" + field + "'] .gb-value")).hasText(Pattern.compile("\\s*" + score.replace(".", "\\.") + "(?:\\.0+)?\\s*"));
    }

    private void assertGradebookPoints(String title, String points) {
        Locator header = page.locator("#gradeTable .relative")
                .filter(new Locator.FilterOptions().setHas(page.locator("a.gb-title").filter(new Locator.FilterOptions().setHasText(title))));
        assertThat(header.locator(".gb-total-points")).hasText(Pattern.compile("\\s*" + points.replace(".", "\\.") + "(?:\\.0+)?\\s*"));
    }

    private void openCategorySettings(Page target) {
        target.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Settings")).first().click();
        Locator accordion = target.locator(".accordion button").filter(new Locator.FilterOptions().setHasText("Categories")).first();
        if (!"true".equals(accordion.getAttribute("aria-expanded"))) {
            ElementHandle previousPanel = target.locator("#settingsCategories").elementHandle();
            accordion.click();
            target.waitForFunction("panel => !panel.isConnected", previousPanel);
            previousPanel.dispose();
        }
    }

    private void changeCategorySettings(Page target, boolean keepHighest, boolean equalWeight) {
        openCategorySettings(target);
        for (String option : List.of("keepHighest", "equalWeight")) {
            Locator checkbox = target.locator("#settingsCategories .gb-inline-checkbox input[name$='" + option + "']");
            boolean enabled = "keepHighest".equals(option) ? keepHighest : equalWeight;
            if (checkbox.isChecked() != enabled) {
                ElementHandle previousRow = target.locator(".gb-category-row").first().elementHandle();
                checkbox.setChecked(enabled);
                target.waitForFunction("row => !row.isConnected", previousRow);
                previousRow.dispose();
            }
        }
        Locator category = target.locator(".gb-category-row").filter(new Locator.FilterOptions()
            .setHas(target.locator("input[name$='name'][value='Quizzes']")));
        assertThat(category).hasCount(1);
        if (keepHighest) {
            category.locator(".gb-category-keephighest input").fill("2");
        }
        if (equalWeight) {
            category.locator(".gb-category-equalweight input").check();
        }
        saveCategorySettings(target);
    }

    private void saveCategorySettings(Page target) {
        target.locator(".act button.active").first().click();
        assertThat(target.getByText("The settings were successfully updated", new Page.GetByTextOptions().setExact(true))).isVisible();
    }
}
