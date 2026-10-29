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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Searching question text from the authoring "Search questions" option, which needs samigo.question.show.showSearchQuestion. */
class SamigoQuestionSearchTest extends SakaiUiTestBase {

    private static final Pattern SEARCH_QUESTIONS = Pattern.compile("^\\s*Search questions\\s*$", Pattern.CASE_INSENSITIVE);

    @Test
    void aQuestionFoundByItsTextIsAddedToAnotherAssessment() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        String word = "photosynthesis" + sakai.randomId();

        createAssessment(siteUrl, "Source " + word);
        addMultipleChoiceQuestion("Which process is " + word + "?");

        createAssessment(siteUrl, "Target " + word);
        Locator result = search(word);
        result.locator("input[type=checkbox]").check();
        page.locator("[id='editform:import']").click();

        // back on the target assessment, now holding a copy of the question
        assertThat(page.locator("#assessmentForm\\:parts .samigo-question-callout")).hasCount(1);
        assertThat(page.locator("#assessmentForm\\:parts")).containsText(word);
    }

    @Test
    void onlyQuestionsTheAuthorCanReachArePreviewed() {
        String theirWord = "osmosis" + sakai.randomId();
        sakai.login("instructor2");
        // a project, as a course would share its roster's instructors with instructor1
        String theirSite = sakai.createProject("instructor2", List.of("sakai\\.samigo"));
        createAssessment(theirSite, "Theirs " + theirWord);
        addMultipleChoiceQuestion("Which process is " + theirWord + "?");
        String theirPreview = previewLink(search(theirWord));

        String ownWord = "diffusion" + sakai.randomId();
        sakai.login("instructor1");
        String ownSite = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        sakai.gotoPath(theirSite);
        assertThat(page.locator("body")).not().containsText("Theirs " + theirWord);
        createAssessment(ownSite, "Mine " + ownWord);
        addMultipleChoiceQuestion("Which process is " + ownWord + "?");
        String ownPreview = previewLink(search(ownWord));

        // searches only cover the sites and pools the author can reach
        page.locator("[id='editform:textToSearch']").fill(theirWord);
        page.locator("[id='editform:searchByTextOR']").click();
        assertThat(page.locator("[id='editform:text_searched']")).containsText(theirWord);
        assertThat(page.locator("[id='editform:searchquestions-questions'] > tbody > tr")).hasCount(0);

        sakai.gotoPath(ownPreview);
        assertThat(page.locator("body")).containsText(ownWord);

        // the same preview page with the other instructor's question id
        sakai.gotoPath(ownPreview.replaceFirst("idString=[^&]*", "idString=" + idString(theirPreview)));
        assertThat(page.locator("body")).not().containsText(theirWord);
    }

    private void createAssessment(String siteUrl, String title) {
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.locator("#authorIndexForm a").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$"))).click();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.locator("#authorIndexForm\\:createnew").click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertThat(page.locator("#assessmentForm\\:parts\\:0\\:changeQType")).isVisible();
        assumeTrue(page.locator("#assessmentForm\\:parts\\:0\\:changeQType option")
            .filter(new Locator.FilterOptions().setHasText(SEARCH_QUESTIONS)).count() > 0,
            "samigo.question.show.showSearchQuestion is off on this server");
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

    /** Opens "Search questions" from the assessment being edited and searches its text until the question is indexed. */
    private Locator search(String word) {
        Locator type = page.locator("#assessmentForm\\:parts\\:0\\:changeQType");
        Locator searchOption = type.locator("option").filter(new Locator.FilterOptions().setHasText(SEARCH_QUESTIONS));
        assertThat(searchOption).hasCount(1);
        type.selectOption(searchOption.first().getAttribute("value"));

        Locator result = page.locator("[id='editform:searchquestions-questions'] > tbody > tr")
            .filter(new Locator.FilterOptions().setHasText(word));
        long deadline = System.currentTimeMillis() + 15_000;
        do {
            page.locator("[id='editform:textToSearch']").fill(word);
            page.locator("[id='editform:searchByTextOR']").click();
            assertThat(page.locator("[id='editform:text_searched']")).isVisible();
        } while (result.count() == 0 && System.currentTimeMillis() < deadline);
        assertThat(result).hasCount(1);
        return result;
    }

    private static String previewLink(Locator result) {
        String href = result.locator("a.uimodal").getAttribute("href");
        assertTrue(href != null && href.contains("searchPreview"), "no preview link in " + result.innerHTML());
        return href.replaceFirst("^https?://[^/]+", "");
    }

    private static String idString(String previewLink) {
        return previewLink.replaceFirst(".*idString=([^&]*).*", "$1");
    }
}
