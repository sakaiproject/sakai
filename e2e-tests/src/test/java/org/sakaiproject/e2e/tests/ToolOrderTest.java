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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class ToolOrderTest extends SakaiUiTestBase {

    @Test
    void toolOrderImmediateActionsPersist() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai.announcements", "sakai.resources"));

        page.navigate(siteUrl);
        sakai.toolClick("Site Info");

        Locator toolOrderTab = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Tool Order", Pattern.CASE_INSENSITIVE))).first();
        assertThat(toolOrderTab).isVisible();
        toolOrderTab.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        Locator app = page.locator("#tool-order-app");
        assertThat(app).isVisible();

        Locator rows = page.locator(".tool-order-row");
        assertTrue(rows.count() > 1);
        String firstTitleBefore = rows.first().locator(".tool-order-title").innerText();
        Locator secondRow = rows.nth(1);
        BoundingBox secondRowBox = secondRow.boundingBox();
        rows.first().locator("[data-action=\"drag-handle\"]").dragTo(secondRow, new Locator.DragToOptions()
            .setTargetPosition(10, secondRowBox.height - 2)
            .setForce(true));
        assertThat(page.locator("#tool-order-alert")).containsText(Pattern.compile("saved", Pattern.CASE_INSENSITIVE));

        page.reload();
        page.waitForLoadState();
        String firstTitleAfter = page.locator(".tool-order-row").first().locator(".tool-order-title").innerText();
        assertNotEquals(firstTitleBefore, firstTitleAfter);

        Locator editableRow = page.locator(".tool-order-row:has([data-action=\"edit\"])").first();
        assertThat(editableRow).isVisible();
        String renamedTitle = "Site News";
        editableRow.locator("[data-action=\"edit\"]").click(new Locator.ClickOptions().setForce(true));
        editableRow.locator("input[name=\"title\"]").fill(renamedTitle);
        editableRow.locator("button[type=\"submit\"]").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(editableRow.locator(".tool-order-title")).containsText(renamedTitle);

        Locator visibilityButton = editableRow.locator("[data-action=\"visibility\"]").first();
        if (visibilityButton.count() > 0 && visibilityButton.isEnabled()) {
            visibilityButton.click(new Locator.ClickOptions().setForce(true));
            assertThat(editableRow.locator(".tool-order-hidden-badge")).isVisible();
            visibilityButton.click(new Locator.ClickOptions().setForce(true));
            assertThat(editableRow.locator(".tool-order-hidden-badge")).isHidden();
        }

        Locator deletableRow = page.locator(".tool-order-row:has([data-action=\"delete\"])").first();
        if (deletableRow.count() > 0) {
            String deletedTitle = deletableRow.locator(".tool-order-title").innerText();
            page.onceDialog(dialog -> dialog.accept());
            deletableRow.locator("[data-action=\"delete\"]").click(new Locator.ClickOptions().setForce(true));
            assertThat(page.locator(".tool-order-row").filter(new Locator.FilterOptions().setHasText(deletedTitle))).hasCount(0);
        }
    }
}
