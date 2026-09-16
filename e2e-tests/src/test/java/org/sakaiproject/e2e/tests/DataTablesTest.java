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
import com.microsoft.playwright.Route;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class DataTablesTest extends SakaiUiTestBase {

    @Test
    void sharedLoaderPreservesTwoDirectionSortingAndExplicitOverrides() throws Exception {
        String template = Files.readString(Path.of("../lti/lti-tool/src/webapp/vm/lti_tool_site_deploy.vm"));
        Matcher initializer = Pattern.compile("<script id=\"js\">([\\s\\S]*?)</script>").matcher(template);
        assertTrue(initializer.find(), "External Tools deployment table initializer must be present");

        // Use the source loader with the running server's real WebJar assets.
        page.route("**/library/js/headscripts.js", route -> route.fulfill(new Route.FulfillOptions()
            .setContentType("application/javascript")
            .setPath(Path.of("../library/src/webapp/js/headscripts.js"))));
        String fixture = """
            <!doctype html><html><head><meta charset="utf-8">
            <script src="/library/js/headscripts.js"></script>
            <script>includeWebjarLibrary('datatables');</script>
            </head><body>
            <table id="tool_site_table"><thead><tr>
              <th>Site ID</th><th>Deployment group</th><th>Notes</th><th class="no-sort">Actions</th>
            </tr></thead><tbody>
              <tr><td>beta</td><td>group-c</td><td>note-a</td><td>Edit</td></tr>
              <tr><td>alpha</td><td>group-b</td><td>note-c</td><td>Edit</td></tr>
              <tr><td>gamma</td><td>group-a</td><td>note-b</td><td>Edit</td></tr>
            </tbody></table>
            <table id="explicit"><thead><tr><th>Explicit three-state cycle</th></tr></thead>
            <tbody><tr><td>beta</td></tr><tr><td>alpha</td></tr><tr><td>gamma</td></tr></tbody></table>
            <script>
            %s
            document.addEventListener('DOMContentLoaded', () => {
                new DataTable('#explicit', {columns: [{orderSequence: ['asc', 'desc', '']}]});
            });
            </script></body></html>
            """.formatted(initializer.group(1));
        page.route("**/datatables-regression", route -> route.fulfill(new Route.FulfillOptions()
            .setContentType("text/html").setBody(fixture)));
        page.navigate("/datatables-regression");

        Locator table = page.locator("#tool_site_table");
        String[][] ascending = {{"alpha", "beta", "gamma"}, {"group-a", "group-b", "group-c"},
            {"note-a", "note-b", "note-c"}};
        String[][] descending = {{"gamma", "beta", "alpha"}, {"group-c", "group-b", "group-a"},
            {"note-c", "note-b", "note-a"}};
        for (int column = 0; column < 3; column++) {
            Locator header = table.locator("thead th").nth(column);
            for (int click = 0; click < 4; click++) {
                boolean reverse = "ascending".equals(header.getAttribute("aria-sort"));
                header.click();
                assertThat(header).hasAttribute("aria-sort", reverse ? "descending" : "ascending");
                assertThat(table.locator("tbody td:nth-child(" + (column + 1) + ")"))
                    .hasText(reverse ? descending[column] : ascending[column]);
            }
        }
        assertThat(table.locator("thead th").last()).hasClass(Pattern.compile(".*dt-orderable-none.*"));

        Locator explicitHeader = page.locator("#explicit thead th");
        explicitHeader.click();
        assertThat(explicitHeader).hasAttribute("aria-sort", "descending");
        explicitHeader.click();
        assertThat(explicitHeader).not().hasAttribute("aria-sort", Pattern.compile("ascending|descending"));
        assertThat(page.locator("#explicit tbody td")).hasText(new String[] {"beta", "alpha", "gamma"});
    }
}
