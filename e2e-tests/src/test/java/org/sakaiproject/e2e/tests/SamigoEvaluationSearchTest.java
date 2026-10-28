/**
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class SamigoEvaluationSearchTest extends SakaiUiTestBase {

    private String siteUrl;

    @Test
    void clearingTheStudentSearchRestoresEveryEvaluationList() {
        sakai.login("instructor1");
        siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        String title = "Search " + sakai.randomId();
        createQuiz(title, "1");
        sakai.login("student0011");
        submitQuiz(title);

        sakai.login("instructor1");
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.locator("#authorIndexForm\\:coreAssessments tbody tr").filter(new Locator.FilterOptions().setHasText(title))
            .locator("td.submitted a").first().click();
        assertSearchClears("#editTotalResults\\:totalScoreTable");

        page.locator("[id$='questionScoresMenuLink'] a").click();
        assertSearchClears("#editTotalResults\\:questionScoreTable");

        page.locator("[id$='submissionStatusMenuLink'] a").click();
        assertSearchClears("#editTotalResults\\:totalScoreTable");
    }

    @Test
    void decimalCommaScoresDisplayAndSaveInEveryGradingView() {
        sakai.login("instructor1");
        siteUrl = sakai.createSiteThroughUi("instructor1", List.of("sakai\\.samigo"), "course");
        String title = "Decimal scores " + sakai.randomId();
        createQuiz(title, "2.5");
        sakai.login("student0011");
        submitQuiz(title);

        sakai.login("instructor1");
        page.navigate(siteUrl);
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Edit Site Information").setExact(true)).click();
        page.locator("#locales").selectOption("es_ES");
        page.locator("input[name='continue'][type='submit']").click();
        page.locator("input[name='eventSubmit_doSave_siteInfo']").click();
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.locator("#authorIndexForm\\:coreAssessments tbody tr").filter(new Locator.FilterOptions().setHasText(title))
            .locator("td.submitted a").first().click();

        Locator table = page.locator("[id='editTotalResults:totalScoreTable']");
        Locator student = table.locator("tbody tr").filter(new Locator.FilterOptions().setHasText("student0011"));
        int scoreColumn = ((Number) table.locator("[id$=':totalAutoScore']")
            .evaluate("el => el.closest('th').cellIndex")).intValue();
        assertThat(student.locator("td").nth(scoreColumn)).hasText("2,5");

        page.locator("[id$='questionScoresMenuLink'] a").click();
        Locator score = page.locator("input[id$=':qscore']").first();
        assertThat(score).hasValue("2,5");
        page.locator("[id='editTotalResults:save']").click();
        assertThat(score).hasValue("2,5");
        score.fill("1,75");
        page.locator("[id='editTotalResults:save']").click();
        assertThat(score).hasValue("1,75");

        page.locator("[id$='totalScoresMenuLink'] a").click();
        assertThat(student.locator("td").nth(scoreColumn)).hasText("1,75");
        student.locator("td").nth(1).locator("a[href]").first().click();
        Locator adjusted = page.locator("input[id$=':adjustedScore']").first();
        assertThat(adjusted).hasValue("1,75");
        page.locator("[id='editStudentResults:save']").click();
        assertThat(adjusted).hasValue("1,75");
        adjusted.fill("2,25");
        page.locator("[id='editStudentResults:save']").click();
        assertThat(adjusted).hasValue("2,25");
        // Rubrics and the existing change handler can also submit canonical decimal points.
        adjusted.fill("2.5");
        page.locator("[id='editStudentResults:save']").click();
        assertThat(adjusted).hasValue("2,5");
    }

    private void assertSearchClears(String table) {
        Locator search = page.locator("[id='editTotalResults:searchString']");
        Locator students = page.locator(table + " > tbody > tr")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("student0011", Pattern.CASE_INSENSITIVE)));
        assertThat(search).hasAttribute("type", "search");
        assertThat(search).hasValue("");
        assertThat(students).hasCount(1);

        submits(() -> {
            search.fill("nobody matches this");
            search.press("Enter");
        });
        assertThat(students).hasCount(0);
        assertThat(search).hasValue("nobody matches this");

        // deleting the text, as the native clear button does, drops the search
        submits(() -> search.fill(""));
        assertThat(students).hasCount(1);
        assertThat(search).hasValue("");
    }

    /** Runs an action that posts the evaluation form and waits for the page it answers with. */
    private void submits(Runnable action) {
        page.waitForResponse(r -> "POST".equals(r.request().method()) && r.url().contains("/jsf/evaluation/"), action);
    }

    private void createQuiz(String title, String points) {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).click();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click();
        Locator type = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        type.selectOption(type.locator("option").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("multiple choice", Pattern.CASE_INSENSITIVE))).first().getAttribute("value"));
        page.locator("#itemForm\\:answerptr").fill(points);
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
    }

    private void submitQuiz(String title) {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.locator("[id='selectIndexForm:selectTable'] a[id$=':takeAssessment']")
            .filter(new Locator.FilterOptions().setHasText(title)).click();
        page.locator("[id='takeAssessmentForm:honor_pledge']").check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Begin Assessment").setExact(true)).click();
        page.locator(".samigo-question-callout").first().getByRole(AriaRole.RADIO).first().check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit for Grading").setExact(true)).first().click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit for Grading").setExact(true)).click();
        assertThat(page.getByText("Confirmation Number", new Page.GetByTextOptions().setExact(true))).isVisible();
    }
}
