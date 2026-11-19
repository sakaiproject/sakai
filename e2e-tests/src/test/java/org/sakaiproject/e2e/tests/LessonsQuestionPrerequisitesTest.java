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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class LessonsQuestionPrerequisitesTest extends SakaiUiTestBase {

    @Test
    void unreleasedQuestionsAreHiddenFromStudents() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));
        page.navigate(siteUrl);
        sakai.toolClick("Lessons");
        addQuestion("Required question", false, true, false);
        addQuestion("Locked multiple-choice question", true, false, true);
        addQuestion("Locked short-answer question", false, false, true);
        addQuestion("Unrestricted question", false, false, false);

        assertThat(page.locator(".questionText")).hasCount(4);
        assertThat(page.locator(".sak-banner-warn").filter(new Locator.FilterOptions()
            .setHasText("If you were a student, you wouldn't be able to view or access this item"))).hasCount(2);
        Locator lockedMultipleChoice = page.locator(".questionDiv").filter(new Locator.FilterOptions()
            .setHasText("Locked multiple-choice question"));
        Locator lockedShortAnswer = page.locator(".questionDiv").filter(new Locator.FilterOptions()
            .setHasText("Locked short-answer question"));
        assertThat(lockedMultipleChoice.locator(".question-submit:not(.showPollGraph)")).isEnabled();
        assertThat(lockedShortAnswer.locator(".question-submit:not(.showPollGraph)")).isEnabled();

        sakai.login("student0011");
        page.navigate(siteUrl);
        sakai.toolClick("Lessons");
        assertThat(page.locator(".questionText")).hasCount(2);
        assertThat(lockedMultipleChoice).hasCount(0);
        assertThat(lockedShortAnswer).hasCount(0);
        assertThat(page.locator(".sak-banner-warn").filter(new Locator.FilterOptions()
            .setHasText("Please complete all of the above required items before viewing this item."))).hasCount(2);
        assertThat(page.locator(".question-submit:not(.showPollGraph)")).hasCount(2);
        assertThat(page.locator(".multipleChoiceAnswer")).hasCount(0);

        assertThat(page.locator(".questionText").filter(new Locator.FilterOptions()
            .setHasText("Unrestricted question"))).isVisible();
        assertThat(page.locator(".shortAnswerInput")).hasCount(2);
    }

    private void addQuestion(String text, boolean multipleChoice, boolean required, boolean prerequisite) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        page.locator(".question-link:visible").click();
        page.locator(multipleChoice ? "#multipleChoiceSelect" : "#shortanswerSelect").check();
        sakai.typeCkEditor("question-text-area-evolved::input", "<p>" + text + "</p>");
        if (multipleChoice) {
            page.locator(".question-multiplechoice-answer:visible").first().fill("Correct choice");
            page.locator(".question-multiplechoice-answer-correct:visible").first().check();
            page.locator("#multipleChoiceDialogDiv").getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add New Answer")).click();
            page.locator(".question-multiplechoice-answer:visible").nth(1).fill("Incorrect choice");
        } else {
            page.locator(".question-shortanswer-answer:visible").first().fill("Correct");
        }
        page.locator("#question-required").setChecked(required);
        page.locator("#question-prerequisite").setChecked(prerequisite);
        page.locator("#update-question").click();
        assertThat(page.locator(".questionText").filter(new Locator.FilterOptions().setHasText(text))).isVisible();
    }
}
