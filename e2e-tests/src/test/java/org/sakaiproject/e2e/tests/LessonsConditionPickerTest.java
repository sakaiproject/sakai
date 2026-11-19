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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class LessonsConditionPickerTest extends SakaiUiTestBase {

    @Test
    void textEditingUsesRenderedLabelsAndPersistsPrerequisitesWithoutFetchingTheLessonTree() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.lessonbuildertool"));
        page.navigate(siteUrl);
        sakai.toolClick("Lessons");
        List<String> lessonRequests = new ArrayList<>();
        page.onRequest(request -> {
            if (request.url().contains("/direct/lessons/lesson/")) {
                lessonRequests.add(request.url());
            }
        });

        addContent(".add-text-link");
        sakai.typeCkEditor(sakai.findFirstCkEditorId(), "Original lesson text");
        saveText();
        editText("Original lesson text");
        Locator picker = page.locator("#edit-condition-picker");
        assertThat(picker.locator(".alert-info")).isVisible();
        sakai.typeCkEditor(sakai.findFirstCkEditorId(), "Updated lesson text");
        saveText();
        page.reload();
        assertThat(page.locator(".textbox")).containsText("Updated lesson text");

        addContent(".question-link");
        page.locator("#shortanswerSelect").check();
        sakai.typeCkEditor("question-text-area-evolved::input", "<p>What does &quot;A &amp; B&quot; mean?</p>");
        page.locator(".question-shortanswer-answer:visible").first().fill("Together");
        page.locator("#update-question").click();
        assertThat(page.locator(".questionText")).containsText("What does \"A & B\" mean?");

        editText("Updated lesson text");
        String questionLabel = "What does \"A & B\" mean?";
        assertThat(picker.locator("optgroup")).hasAttribute("label", questionLabel);
        String conditionId = picker.locator("optgroup option").getAttribute("value");
        picker.locator(".condition-form select").first().selectOption(conditionId);
        picker.locator(".condition-form button").click();
        assertThat(picker.locator(".condition-row")).containsText(questionLabel);
        sakai.typeCkEditor(sakai.findFirstCkEditorId(), "Text with a prerequisite");
        saveText();

        editText("Text with a prerequisite");
        assertThat(picker.locator(".condition-row")).containsText(questionLabel);
        assertThat(picker.locator(".condition-form")).hasCount(0);
        picker.locator(".condition-row button").click();
        assertThat(picker.locator(".condition-row")).hasCount(0);
        saveText();
        editText("Text with a prerequisite");
        assertThat(picker.locator("optgroup option")).isEnabled();
        assertThat(picker.locator(".condition-row")).hasCount(0);
        assertTrue(lessonRequests.isEmpty(), "Unexpected lesson-tree requests: " + lessonRequests);
    }

    private void addContent(String selector) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();
        page.locator(selector + ":visible").click();
    }

    private void editText(String text) {
        Locator item = page.locator(".right-col").filter(new Locator.FilterOptions()
                .setHas(page.locator(".textbox").filter(new Locator.FilterOptions().setHasText(text))));
        item.hover();
        item.locator(".itemLink").click();
        assertThat(page.locator("#edit-condition-picker")).isVisible();
    }

    private void saveText() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true)).click();
        assertThat(page.locator("#content")).isVisible();
    }
}
