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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class LessonsQuestionResponseTest extends SakaiUiTestBase {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void answersSurviveReloadAndInstructorResubmission(boolean shortAnswer) {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));
        sakai.addMemberToSite(siteUrl, "student0011", "Student");
        page.navigate(siteUrl);
        sakai.toolClick("Lessons");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content").setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Question").setExact(true)).click();
        page.locator(shortAnswer ? "#shortanswerSelect" : "#multipleChoiceSelect").check();
        String prompt = shortAnswer ? "Name the Greek letter λ" : "Select the Greek letter λ";
        sakai.typeCkEditor("question-text-area-evolved::input", prompt);
        if (shortAnswer) {
            page.locator(".question-shortanswer-answer:visible").first().fill("lambda");
        } else {
            page.locator("input[name='question-multiplechoice-answer']").fill("λ");
            page.locator("input[name='question-multiplechoice-answer-correct']").check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add New Answer").setExact(true)).click();
            page.locator(".question-multiplechoice-answer:visible").nth(1).fill("μ");
        }
        page.locator("#question-correct-text").fill("Your answer was saved.");
        page.locator("#question-incorrect-text").fill("Try again.");
        page.locator("#update-question").click();
        assertThat(page.locator(".questionText").filter(new Locator.FilterOptions().setHasText(prompt))).isVisible();
        String lessonsUrl = page.url();
        Locator question = page.locator(".questionDiv").filter(new Locator.FilterOptions().setHasText(prompt));

        submitAnswer(question, shortAnswer, false);
        assertThat(question.locator(".questionStatusText")).hasText("Try again.");
        submitAnswer(question, shortAnswer, true);
        assertThat(question.locator(".questionStatusText")).hasText("Your answer was saved.");

        sakai.login("student0011");
        page.navigate(lessonsUrl);
        submitAnswer(question, shortAnswer, true);

        assertThat(question.locator(".questionStatusText")).hasText("Your answer was saved.");
        assertThat(question.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Submit Answer").setExact(true)))
            .isDisabled();
        if (shortAnswer) {
            assertThat(question.locator(".shortAnswerInput")).hasValue("lambda");
        } else {
            assertThat(question.getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName("A : λ").setExact(true))).isChecked();
        }
    }

    private void submitAnswer(Locator question, boolean shortAnswer, boolean correct) {
        if (shortAnswer) {
            question.locator(".shortAnswerInput").fill(correct ? "lambda" : "mu");
        } else {
            question.getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions()
                .setName(correct ? "A : λ" : "B : μ").setExact(true)).check();
        }
        question.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Submit Answer").setExact(true)).click();
        assertThat(question.locator(".questionStatusText"))
            .hasText(correct ? "Your answer was saved." : "Try again.");
        page.reload();
    }
}
