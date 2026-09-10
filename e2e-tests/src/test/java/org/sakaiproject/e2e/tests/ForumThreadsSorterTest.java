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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ForumThreadsSorterTest {

    @Test
    void restoresThreadHierarchyAfterAuthorAndDateSorts() {
        try (Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch()) {
            Page page = browser.newPage();
            // Match the hierarchy markers and indentation emitted by HierDataTableRender.
            page.setContent("""
                <style>body { font-size: 16px; } td { padding-left: 0; }</style>
                <table id="threads">
                  <thead><tr>
                    <th>Expand all</th>
                    <th><a href="#" data-sakai-forum-sort="thread">Thread</a></th>
                    <th><a href="#" data-sakai-forum-sort="author">Authored by</a></th>
                    <th><a href="#" data-sakai-forum-sort="date">Date</a></th>
                  </tr></thead>
                  <tbody>
                    <tr class="hierItemBlock"><td></td><td>A</td><td>Amy</td><td>2026-09-01</td></tr>
                    <tr id="_id_11__hide_division_"><td></td><td style="padding-left:1em">A reply</td><td>Yan</td><td>2026-09-02</td></tr>
                    <tr id="_id_12__hide_division_"><td></td><td style="padding-left:2em">A nested</td><td>Zoe</td><td>2026-09-04</td></tr>
                    <tr id="_id_13__hide_division_"><td></td><td style="padding-left:1em">A second reply</td><td>Cara</td><td>2026-09-03</td></tr>
                    <tr class="hierItemBlock"><td></td><td>B</td><td>Bob</td><td>2026-09-02</td></tr>
                    <tr id="_id_21__hide_division_"><td></td><td style="padding-left:1em">B reply</td><td>Dan</td><td>2026-09-05</td></tr>
                  </tbody>
                </table>
                """);
            page.addScriptTag(new Page.AddScriptTagOptions().setPath(Path.of(
                    "../msgcntr/messageforums-app/src/webapp/js/forumTopicThreadsSorter.js")));
            page.evaluate("sakaiForumThreadsSorter.init(document.getElementById('threads'), 'en_US')");

            page.locator("[data-sakai-forum-sort=author]").click();
            assertOrder(page, "A", "A reply", "A nested", "A second reply", "B", "B reply");
            page.locator("[data-sakai-forum-sort=date]").click();
            assertOrder(page, "A", "A reply", "A second reply", "A nested", "B", "B reply");
            page.locator("[data-sakai-forum-sort=date]").click();
            assertOrder(page, "B", "B reply", "A", "A nested", "A second reply", "A reply");

            page.locator("[data-sakai-forum-sort=thread]").click();
            assertOrder(page, "A", "A reply", "A nested", "A second reply", "B", "B reply");
            page.locator("[data-sakai-forum-sort=author]").click();
            assertOrder(page, "B", "B reply", "A", "A reply", "A nested", "A second reply");
            page.locator("[data-sakai-forum-sort=thread]").click();
            assertOrder(page, "B", "B reply", "A", "A second reply", "A reply", "A nested");
            page.locator("[data-sakai-forum-sort=date]").click();
            assertOrder(page, "A", "A reply", "A second reply", "A nested", "B", "B reply");
        }
    }

    private void assertOrder(Page page, String... subjects) {
        assertThat(page.locator("#threads tbody tr td:nth-child(2)")).hasText(subjects);
    }
}
