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
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class GradebookFirstKeystrokeTest extends SakaiUiTestBase {

    @ParameterizedTest
    @ValueSource(strings = {"8", "Enter"})
    void firstKeyStartsEditingNewItem(String firstKey) {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai.gradebookng"));
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        assertThat(page.locator(".gb-view-grade-summary").nth(1)).isVisible();

        String title = "First keystroke " + UUID.randomUUID();
        page.locator("button.gb-add-gradebook-item-button").click();
        Locator dialog = page.locator(".gb-modal-dialog:visible");
        dialog.locator("input[name$=\"title\"]").fill(title);
        dialog.locator("input[name$=\"points\"]").fill("100");
        dialog.locator("input[name$=\"released\"]").uncheck();
        dialog.locator("input[name$=\"counted\"]").uncheck();
        dialog.locator("button.gb-create-or-save").click();
        assertThat(dialog).isHidden();

        Locator column = page.locator(".tabulator-col[data-assignment-id]")
            .filter(new Locator.FilterOptions().setHasText(title));
        assertThat(column).isVisible();
        String assignmentId = column.getAttribute("data-assignment-id");
        Locator cell = page.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId + "\"]").first();
        // Wait for the new-item selection before interacting with the table.
        assertThat(cell).hasClass(Pattern.compile(".*tabulator-range-only-cell-selected.*"));
        cell.click();
        cell.press(firstKey);
        Locator editor = cell.locator("input.tabulator-input");
        assertThat(editor).isFocused();
        assertThat(editor).hasValue("Enter".equals(firstKey) ? "" : firstKey);
        page.keyboard().press("5");
        String expectedScore = "Enter".equals(firstKey) ? "5" : "85";
        assertThat(editor).hasValue(expectedScore);
        page.keyboard().press("Enter");
        assertThat(editor).hasCount(0);
        assertThat(cell.locator(".gb-value")).hasText(expectedScore);

        page.reload();
        assertThat(cell.locator(".gb-value")).hasText(expectedScore);
    }
}
