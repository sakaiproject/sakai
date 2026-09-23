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
        sakai.createCourse("instructor1", List.of("sakai.messages", "sakai.forums"));
        page.navigate("/portal");

        FrameLocator notifications = page.frameLocator("iframe[title='Message Center Notifications ']");
        Locator table = notifications.locator(".workspaceTable");
        assertThat(table).isVisible();
        assertThat(table.locator("tbody tr").first()).isVisible();

        for (int width : new int[] {1280, 600}) {
            page.setViewportSize(width, 900);
            assertColumnsFillTable(table, 3);
            notifications.locator("#showOptions").click();
            assertThat(table.locator("thead th").first()).isVisible();
            assertColumnsFillTable(table, 4);
            notifications.locator("#cancel").click();
            assertThat(table.locator("thead th").first()).isHidden();
            assertColumnsFillTable(table, 3);
            table.locator("#siteHeader").click();
            assertColumnsFillTable(table, 3);
        }
    }

    private void assertColumnsFillTable(Locator table, int columns) {
        assertThat(table.locator("thead th:visible")).hasCount(columns);
        // A leftover col creates blank table space even when its cells are hidden.
        double unusedWidth = ((Number) table.evaluate("""
            table => table.getBoundingClientRect().width - [...table.tHead.rows[0].cells]
                .reduce((width, cell) => width + cell.getBoundingClientRect().width, 0)
            """)).doubleValue();
        assertEquals(0, unusedWidth, 2, "Visible columns should fill the table without a phantom column");
    }
}
