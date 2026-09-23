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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class OverviewTest extends SakaiUiTestBase {

    @Test
    void messageCenterColumnsStayAlignedWhenTogglingOptions() {
        sakai.login("instructor1");
        List<String> tools = List.of("home", "sakai.messages", "sakai.forums");
        List<String> sites = List.of(sakai.createCourse("instructor1", tools),
            sakai.createProject("instructor1", tools));
        // Loading each site's notification summary creates its synoptic record.
        for (String site : sites) {
            page.navigate(site);
            assertThat(page.frameLocator("iframe[title='Message Center Notifications ']")
                .getByText("New Messages", new FrameLocator.GetByTextOptions()
                    .setExact(true))).isVisible();
        }
        page.navigate("/portal");

        FrameLocator notifications = page.frameLocator("iframe[title='Message Center Notifications ']");
        Locator table = notifications.locator(".workspaceTable");
        assertThat(table).isVisible();
        for (String site : sites) {
            assertThat(table.locator("a[href$='/" + sakai.siteIdFromUrl(site) + "']")).isVisible();
        }

        for (int width : new int[] {1280, 600}) {
            page.setViewportSize(width, 900);
            assertColumnsFillTable(table, 3);
            notifications.locator("#showOptions").click();
            assertThat(table.locator(".hideHeader")).isVisible();
            assertColumnsFillTable(table, 4);
            Locator checkbox = table.locator("tbody tr:visible input[type='checkbox']:not(.unchangedValue)").first();
            boolean originalValue = checkbox.isChecked();
            checkbox.setChecked(!originalValue);
            notifications.locator("#cancel").click();
            assertThat(table.locator(".hideHeader")).isHidden();
            assertColumnsFillTable(table, 3);
            notifications.locator("#showOptions").click();
            assertEquals(originalValue, checkbox.isChecked(), "Cancel should restore the saved checkbox value");
            notifications.locator("#cancel").click();
            table.locator("#siteHeader").click();
            assertColumnsFillTable(table, 3);
        }

        String siteUrl = table.locator("tbody tr:visible").first().locator("td").last()
            .locator("a").getAttribute("href");
        Locator siteRow = table.locator("tbody tr").filter(new Locator.FilterOptions()
            .setHas(notifications.locator("a[href='" + siteUrl + "']")));
        notifications.locator("#showOptions").click();
        siteRow.locator("input[type='checkbox']:not(.unchangedValue)").check();
        notifications.locator("input[type='submit'][value='Update']").click();
        assertThat(table.locator(".hideHeader")).isHidden();
        assertThat(siteRow).isHidden();

        page.reload();
        assertThat(table.locator("#siteHeader")).isVisible();
        assertThat(siteRow).isHidden();
        notifications.locator("#showOptions").click();
        assertThat(siteRow).isVisible();
        assertThat(siteRow.locator("input[type='checkbox']:not(.unchangedValue)")).isChecked();
        siteRow.locator("input[type='checkbox']:not(.unchangedValue)").uncheck();
        notifications.locator("input[type='submit'][value='Update']").click();
        assertThat(table.locator(".hideHeader")).isHidden();
        assertThat(siteRow).isVisible();
        assertColumnsFillTable(table, 3);

        List<String> displayedSites = table.locator("tbody tr:visible td:last-child a").all().stream()
            .map(link -> link.getAttribute("href")).toList();
        notifications.locator("#showOptions").click();
        for (Locator checkbox : table.locator("input[type='checkbox']:not(.unchangedValue)").all()) {
            checkbox.check();
        }
        notifications.locator("input[type='submit'][value='Update']").click();
        assertThat(table).isHidden();
        assertThat(notifications.locator(".noActivity")).isVisible();
        notifications.locator("#showOptions").click();
        assertThat(table).isVisible();
        assertColumnsFillTable(table, 4);
        siteRow.locator("input[type='checkbox']:not(.unchangedValue)").uncheck();
        notifications.locator("#cancel").click();
        assertThat(table).isHidden();
        assertThat(notifications.locator(".noActivity")).isVisible();
        notifications.locator("#showOptions").click();
        assertThat(siteRow.locator("input[type='checkbox']:not(.unchangedValue)")).isChecked();
        for (String displayedSite : displayedSites) {
            table.locator("tbody tr").filter(new Locator.FilterOptions()
                .setHas(notifications.locator("a[href='" + displayedSite + "']")))
                .locator("input[type='checkbox']:not(.unchangedValue)").uncheck();
        }
        notifications.locator("input[type='submit'][value='Update']").click();
        assertThat(table.locator(".hideHeader")).isHidden();
        assertThat(table).isVisible();
        assertColumnsFillTable(table, 3);
    }

    private void assertColumnsFillTable(Locator table, int columns) {
        assertThat(table.locator("thead th:visible")).hasCount(columns);
        assertEquals(columns, ((Number) table.evaluate(
            "table => new DataTable(table).columns(':visible').count()")).intValue(),
            "DataTables visibility should match the rendered columns");
        // A leftover col creates blank table space even when its cells are hidden.
        double unusedWidth = ((Number) table.evaluate("""
            table => table.getBoundingClientRect().width - [...table.tHead.rows[0].cells]
                .reduce((width, cell) => width + cell.getBoundingClientRect().width, 0)
            """)).doubleValue();
        assertEquals(0, unusedWidth, 2, "Visible columns should fill the table without a phantom column");
    }
}
