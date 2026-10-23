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
        createQuiz(title);
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

    private void assertSearchClears(String table) {
        Locator search = page.locator("[id='editTotalResults:searchString']");
        Locator students = page.locator(table + " tbody tr")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("student0011", Pattern.CASE_INSENSITIVE)));
        assertThat(search).hasAttribute("type", "search");
        assertThat(search).hasValue("");
        assertThat(students).hasCount(1);

        search.fill("nobody matches this");
        search.press("Enter");
        assertThat(search).hasValue("nobody matches this");
        assertThat(students).hasCount(0);

        // deleting the text, as the native clear button does, drops the search
        search.fill("");
        assertThat(students).hasCount(1);
        assertThat(search).hasValue("");
    }

    private void createQuiz(String title) {
        page.navigate(siteUrl);
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
