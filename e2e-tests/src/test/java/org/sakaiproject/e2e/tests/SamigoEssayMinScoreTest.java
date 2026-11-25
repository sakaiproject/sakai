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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * SAK-49397: an essay's minimum points are only awarded when the answer meets the
 * word count and keywords set on the question. Needs samigo.allowMinScore=true.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SamigoEssayMinScoreTest extends SakaiUiTestBase {

    private static final String TITLE = "SAK-49397 Essay Minimum " + System.currentTimeMillis();
    private static final String MEETS_CRITERIA = "student0031";
    private static final String TOO_SHORT = "student0032";
    private static final String MISSING_KEYWORD = "student0033";
    private static String siteUrl;

    @Test
    @Order(1)
    void instructorSetsEssayMinimumCriteria() {
        sakai.login("instructor1");
        siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        openTests();
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).click();
        page.locator("#authorIndexForm\\:title").fill(TITLE);
        page.locator("#authorIndexForm\\:createnew").click();

        Locator type = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        type.selectOption(type.locator("option").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("short answer|essay", Pattern.CASE_INSENSITIVE))).first().getAttribute("value"));
        page.locator("#itemForm\\:answerptr").fill("10");
        page.locator("#itemForm textarea").first().fill("Describe what protects a plant cell.");
        page.getByLabel("Minimum Point Value (optional)").fill("2");
        page.getByLabel("Word Count (optional)").fill("5");
        page.getByLabel("Keywords (optional)").fill("cell, wall");
        saveQuestion();

        // The criteria load back into the question editor
        page.locator("#assessmentForm\\:parts\\:0\\:parts\\:0\\:modify").click();
        assertThat(page.getByLabel("Word Count (optional)")).hasValue("5");
        assertThat(page.getByLabel("Keywords (optional)")).hasValue("cell, wall");
        saveQuestion();

        publish();
    }

    @Test
    @Order(2)
    void studentsSubmitEssays() {
        submitEssay(MEETS_CRITERIA, "The cell wall protects the plant.");
        submitEssay(TOO_SHORT, "cell wall");
        submitEssay(MISSING_KEYWORD, "The cell membrane protects the plant.");
    }

    @Test
    @Order(3)
    void onlyTheEssayMeetingCriteriaGetsMinimumPoints() {
        sakai.login("instructor1");
        openTests();
        Locator row = page.locator("#authorIndexForm\\:coreAssessments tbody tr")
            .filter(new Locator.FilterOptions().setHasText(TITLE))
            .filter(new Locator.FilterOptions().setHas(page.locator("span[class^='status_published_']")));
        row.locator("td.submitted a").first().click();
        assertThat(page.locator("#editTotalResults")).isVisible();

        assertTotalScore(MEETS_CRITERIA, "2");
        assertTotalScore(TOO_SHORT, "0");
        assertTotalScore(MISSING_KEYWORD, "0");
    }

    private void openTests() {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
    }

    private void saveQuestion() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first().click();
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(1);
    }

    private void publish() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Settings").setExact(true)).first().click();
        sakai.waitForPortalFocus();
        assertThat(page.locator("#jqueryui-accordion.ui-accordion")).isVisible();
        page.locator("#expandLink").click();
        DateTimeFormatter dateTime12h = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US);
        sakai.selectDate("#assessmentSettingsAction\\:startDate",
            LocalDateTime.now().minusDays(1).format(dateTime12h).toLowerCase(Locale.US));
        sakai.selectDate("#assessmentSettingsAction\\:endDate",
            LocalDateTime.now().plusYears(2).format(dateTime12h).toLowerCase(Locale.US));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Settings and Publish").setExact(true)).click();
        page.locator("#publishAssessmentForm\\:publish").click();
        assertThat(page.locator("#authorIndexForm\\:coreAssessments tbody tr")
            .filter(new Locator.FilterOptions().setHasText(TITLE))
            .locator("span[class^='status_published_']")).isVisible();
    }

    private void submitEssay(String student, String answer) {
        sakai.login(student);
        openTests();
        page.locator("[id='selectIndexForm:selectTable'] a[id$=':takeAssessment']")
            .filter(new Locator.FilterOptions().setHasText(TITLE)).click();
        sakai.dismissTutorial();
        Locator begin = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Begin Assessment").setExact(true));
        assertThat(begin).isVisible();
        Locator honorPledge = page.locator("[id='takeAssessmentForm:honor_pledge']");
        if (honorPledge.isVisible()) {
            honorPledge.check();
        }
        begin.click();
        assertThat(page.locator(".samigo-question-callout")).hasCount(1);
        page.locator(".samigo-question-callout textarea").first().fill(answer);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit for Grading").setExact(true)).first().click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit for Grading").setExact(true)).click();
        assertThat(page.getByText("Confirmation Number", new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    private void assertTotalScore(String student, String score) {
        Locator table = page.locator("[id='editTotalResults:totalScoreTable']");
        List<String> headers = table.locator("thead th").allTextContents().stream().map(String::trim).toList();
        int finalScore = headers.indexOf("Final Score");
        Locator row = table.locator("tbody tr").filter(new Locator.FilterOptions().setHasText(student));
        assertThat(row).hasCount(1);
        assertThat(row.locator("td").nth(finalScore)).hasText(Pattern.compile("\\s*" + score + "(?:\\.0+)?\\s*"));
    }
}
