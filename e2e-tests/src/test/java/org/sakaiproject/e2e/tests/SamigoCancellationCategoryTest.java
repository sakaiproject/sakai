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
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiEnvironment;
import org.sakaiproject.e2e.support.SakaiHelper;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Opt in with {@code -Dsamigo.cancellation.enabled=true} and provide all five site URLs:
 * {@code samigo.cancellation.restrictedSiteUrl}, {@code samigo.cancellation.redistributionSiteUrl},
 * {@code samigo.cancellation.equalWeightSiteUrl}, {@code samigo.cancellation.ordinarySiteUrl},
 * and {@code samigo.cancellation.staleSiteUrl}. Each run requires fresh prepared fixtures;
 * the opt-in run fails if a required URL is missing.
 */
@EnabledIfSystemProperty(named = "samigo.cancellation.enabled", matches = "true")
class SamigoCancellationCategoryTest extends SakaiUiTestBase {
    private static final String FIRST_QUIZ = "SAK-52130 TQ-1";
    private static final String RESTRICTION = "This Gradebook category uses Keep/Drop and requires equal point totals.";

    @Test
    void bothRestrictedDialogsExplainReductionAndDismissalPreservesGrades() {
        String siteUrl = fixture("restricted");
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
    void redistributionTargetsSelectedQuestionAndSynchronizesThreePossiblePoints() {
        String siteUrl = fixture("redistribution");
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
    void equalWeightCategoryAllowsReductionAndSynchronizesTwoPossiblePoints() {
        String siteUrl = fixture("equalWeight");
        openGradebook(siteUrl);
        changeCategorySettings(page, false, true);
        reduceAndAssertTwoPoints(siteUrl);
    }

    @Test
    void ordinaryCategoryAllowsReductionAndSynchronizesTwoPossiblePoints() {
        reduceAndAssertTwoPoints(fixture("ordinary"));
    }

    @Test
    void staleReductionFormCannotChangePointsAfterCategoryBecomesRestricted() {
        String siteUrl = fixture("stale");
        openEvaluation(siteUrl, FIRST_QUIZ);
        openCancellation();
        Locator reduction = modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reduce total points"));
        assertThat(reduction).isEnabled();
        Page settingsPage = context.newPage();
        try {
            settingsPage.navigate(siteUrl);
            new SakaiHelper(settingsPage, SakaiEnvironment.baseUrl()).toolClick("Gradebook");
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

    private String fixture(String kind) {
        String property = "samigo.cancellation." + kind + "SiteUrl";
        String siteUrl = System.getProperty(property);
        assertNotNull(siteUrl, "Required fresh fixture missing: -D" + property + "=<site URL>");
        assertFalse(siteUrl.isBlank(), "Required fixture URL is blank: " + property);
        sakai.login(System.getProperty("samigo.cancellation.instructor", "instructor1"));
        assertThat(page.locator("#sakai-account-panel")).hasCount(1);
        return siteUrl;
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
        assertThat(page.locator("#authorIndexForm\\:coreAssessments")).isVisible();
    }

    private void openEvaluation(String siteUrl, String title) {
        openAssessments(siteUrl);
        Locator row = publishedRow(title);
        assertThat(row).hasCount(1);
        row.locator("td.submitted a").first().click();
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
        assertThat(modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reduce total points"))).isDisabled();
        assertThat(modal().getByText(RESTRICTION)).isVisible();
        assertThat(modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Equally distribute points"))).isEnabled();
    }

    private void reduceAndAssertTwoPoints(String siteUrl) {
        openEvaluation(siteUrl, FIRST_QUIZ);
        openCancellation();
        Locator reduction = modal().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reduce total points"));
        assertThat(reduction).isEnabled();
        reduction.click();
        assertThat(page.getByText("This question has been cancelled and will not affect the total score.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        openPublishedAuthoring(siteUrl, FIRST_QUIZ);
        assertAuthoringPoints("2");
        assertThat(page.locator("input[id$='answerptr']").first()).hasValue(Pattern.compile("0(?:\\.0+)?"));
        openGradebook(siteUrl);
        assertGradebookPoints(FIRST_QUIZ, "2");
        assertStudentAssignmentScore(FIRST_QUIZ, "1");
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

    private void changeCategorySettings(Page target, boolean keepHighest, boolean equalWeight) {
        target.locator(".navIntraTool a").filter(new Locator.FilterOptions().setHasText("Settings")).first().click();
        Locator accordion = target.locator(".accordion button").filter(new Locator.FilterOptions().setHasText("Categories")).first();
        if (!"true".equals(accordion.getAttribute("aria-expanded"))) {
            ElementHandle previousPanel = target.locator("#settingsCategories").elementHandle();
            accordion.click();
            target.waitForFunction("panel => !panel.isConnected", previousPanel);
            previousPanel.dispose();
        }
        if (keepHighest) {
            target.locator("#settingsCategories input[type='checkbox'][name$='keepHighest']").check();
        }
        if (equalWeight) {
            target.locator("#settingsCategories input[type='checkbox'][name$='equalWeight']").first().check();
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
        target.locator(".act button.active").first().click();
        assertThat(target.getByText("The settings were successfully updated", new Page.GetByTextOptions().setExact(true))).isVisible();
    }
}
