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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GroupManagerConfirmationTest {

    @Test
    void listsSelectedTitlesAsTextAndClearsPreviousSelection() throws Exception {
        Path template = Path.of("../site-manage/site-group-manager/src/main/webapp/WEB-INF/templates/fragments/javascript.html");
        String source = Files.readString(template);
        int start = source.indexOf('>', source.indexOf("<script th:fragment=\"indexJs\"")) + 1;
        String script = source.substring(start, source.indexOf("</script>", start));

        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setTimeout(15_000))) {
            Page page = browser.newPage();
            page.setDefaultTimeout(15_000);
            page.setDefaultNavigationTimeout(15_000);
            page.setContent("""
                <input id="group-manager-index-select-all" type="checkbox">
                <form id="group-manager-form">
                  <input id="planning" class="group-manager-delete-checkbox" type="checkbox">
                  <input id="review" class="group-manager-delete-checkbox" type="checkbox" groupinfo="Review - 1 member">
                  <button id="delete-groups-submit-button" type="submit">Delete selected</button>
                </form>
                <div id="confirmation-modal" hidden>
                  <ul id="deleted-groups-list"></ul>
                  <button id="modal-btn-cancel">Cancel</button>
                </div>
                """);
            page.addScriptTag(new Page.AddScriptTagOptions().setContent("""
                window.bootstrap = { Modal: { getOrCreateInstance: element => ({
                    show: () => element.hidden = false,
                    hide: () => element.hidden = true
                }) } };
                """));
            page.addScriptTag(new Page.AddScriptTagOptions().setContent(script));

            String title = "<em>Planning</em> & Review - 2 members";
            page.locator("#planning").evaluate("(box, title) => box.setAttribute('groupinfo', title)", title);
            page.locator("#planning").check();
            page.locator("#review").check();
            page.locator("#delete-groups-submit-button").click();

            Locator items = page.locator("#deleted-groups-list li");
            assertThat(page.locator("#confirmation-modal")).isVisible();
            assertThat(items).hasText(new String[] {title, "Review - 1 member"});
            assertThat(items.locator("*")).hasCount(0);

            page.locator("#modal-btn-cancel").click();
            assertThat(page.locator("#confirmation-modal")).isHidden();
            page.locator("#planning").uncheck();
            page.locator("#delete-groups-submit-button").click();
            assertThat(items).hasText(new String[] {"Review - 1 member"});
        }
    }
}
