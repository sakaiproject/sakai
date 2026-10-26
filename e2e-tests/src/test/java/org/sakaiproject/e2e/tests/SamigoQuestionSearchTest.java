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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Searching question text from the authoring "Search questions" option, which needs samigo.question.show.showSearchQuestion. */
class SamigoQuestionSearchTest extends SakaiUiTestBase {

    private static final Pattern SEARCH_QUESTIONS = Pattern.compile("^\\s*Search questions\\s*$", Pattern.CASE_INSENSITIVE);

    private String siteUrl;

    @Test
    void aQuestionFoundByItsTextIsAddedToAnotherAssessment() {
        sakai.login("instructor1");
        siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        String word = "photosynthesis" + sakai.randomId();

        createAssessment("Source " + word);
        addMultipleChoiceQuestion("Which process is " + word + "?");

        createAssessment("Target " + word);
        Locator type = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        Locator searchOption = type.locator("option").filter(new Locator.FilterOptions().setHasText(SEARCH_QUESTIONS));
        assumeTrue(searchOption.count() > 0, "samigo.question.show.showSearchQuestion is off on this server");
        type.selectOption(searchOption.first().getAttribute("value"));

        // questions are indexed in the background, so search until it shows up
        Locator result = page.locator("[id='editform:searchquestions-questions'] > tbody > tr")
            .filter(new Locator.FilterOptions().setHasText(word));
        long deadline = System.currentTimeMillis() + 90_000;
        do {
            page.locator("[id='editform:textToSearch']").fill(word);
            page.locator("[id='editform:searchByTextOR']").click();
            assertThat(page.locator("[id='editform:text_searched']")).isVisible();
        } while (result.count() == 0 && System.currentTimeMillis() < deadline);
        assertThat(result).hasCount(1);

        result.locator("input[type=checkbox]").check();
        page.locator("[id='editform:import']").click();

        // back on the target assessment, now holding a copy of the question
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(1);
        assertThat(page.locator("#assessmentForm\\:parts")).containsText(word);
    }

    private void createAssessment(String title) {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).click();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click();
        assertThat(page.locator("#assessmentForm\\:parts\\:0\\:changeQType")).isVisible();
    }

    private void addMultipleChoiceQuestion(String text) {
        Locator type = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        type.selectOption(type.locator("option").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("multiple choice", Pattern.CASE_INSENSITIVE))).first().getAttribute("value"));
        page.locator("#itemForm\\:answerptr").fill("1");
        page.locator("#itemForm textarea").first().fill(text);
        page.locator("#itemForm\\:mcchoices textarea").nth(0).fill("Correct");
        page.locator("#itemForm\\:mcchoices textarea").nth(1).fill("Incorrect");
        page.locator("#itemForm\\:mcchoices input[type='radio']").nth(0).check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).first().click();
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(1);
    }
}
