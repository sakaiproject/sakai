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
import static org.junit.jupiter.api.Assertions.fail;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class ResourcesReorderTest extends SakaiUiTestBase {

    private static final String DRAG_HANDLE = ".sakai-reorder-drag-handle.drag-handle, .drag-handle";

    @Test
    void canReorderResourcesWithDragMoveButtonsAndUndo() {
        String suffix = Long.toString(System.currentTimeMillis());
        String folderA = "Reorder A " + suffix;
        String folderB = "Reorder B " + suffix;
        String folderC = "Reorder C " + suffix;

        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.resources"));
        page.navigate(siteUrl);
        sakai.toolClick("Resources");

        page.locator("button[title='Actions']").first().click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Create Folders").setExact(true)).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Folder Name").setExact(true)).fill(folderA);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Another Folder").setExact(true)).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Folder Name").setExact(true)).nth(1).fill(folderB);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Another Folder").setExact(true)).click();
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Folder Name").setExact(true)).nth(2).fill(folderC);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create Folders Now").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(folderA).setExact(true))).isVisible();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(folderB).setExact(true))).isVisible();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(folderC).setExact(true))).isVisible();
        // Refresh the request-cached member count used to make Reorder available.
        page.reload();
        assertThat(page.locator("button[title='Actions']")).not().hasCount(0);

        openResourcesReorder();
        assertReorderChrome();

        Locator items = page.locator("#reorder-list li.reorder-element.sakai-reorder-row");
        assertThat(items).hasCount(3);
        assertThat(page.locator("#reorder-list")).containsText(folderA);
        assertThat(page.locator("#reorder-list")).containsText(folderC);
        assertThat(page.locator("input[id^=\"index\"]")).hasCount(0);

        Locator folderCRow = items.filter(new Locator.FilterOptions().setHasText(folderC)).first();
        folderCRow.locator("[data-action=\"move-up\"]").click();
        folderCRow = items.filter(new Locator.FilterOptions().setHasText(folderC)).first();
        folderCRow.locator("[data-action=\"move-up\"]").click();
        assertPrecedes("#reorder-list", folderC, folderA);
        assertThat(folderCRow.locator("input[name^=\"position_\"]")).hasValue("1");

        Locator undoLast = page.locator("#undo-last");
        assertThat(undoLast).isEnabled();
        undoLast.click();
        assertPrecedes("#reorder-list", folderA, folderC);

        folderCRow = items.filter(new Locator.FilterOptions().setHasText(folderC)).first();
        folderCRow.locator("[data-action=\"move-up\"]").click();
        assertPrecedes("#reorder-list", folderC, folderA);

        Locator folderARow = items.filter(new Locator.FilterOptions().setHasText(folderA)).first();
        folderARow.locator("[data-action=\"move-down\"]").click();
        assertPrecedes("#reorder-list", folderC, folderB);
        assertPrecedes("#reorder-list", folderB, folderA);

        page.locator("#save").click();
        page.waitForLoadState();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(folderC).setExact(true))).isVisible();

        String listing = page.locator("body").innerText();
        int cPos = listing.indexOf(folderC);
        int bPos = listing.indexOf(folderB);
        int aPos = listing.indexOf(folderA);
        if (cPos < 0 || bPos < 0 || aPos < 0 || cPos > bPos || bPos > aPos) {
            fail("Expected saved Resources order C, B, A.");
        }

        openResourcesReorder();
        assertPrecedes("#reorder-list", folderC, folderB);
        assertPrecedes("#reorder-list", folderB, folderA);
        assertThat(page.locator("#reorder-list li.reorder-element " + DRAG_HANDLE).first()).isVisible();
    }

    private void openResourcesReorder() {
        Locator actionsButtons = page.locator("button[title='Actions']");
        int count = actionsButtons.count();
        for (int i = 0; i < count; i++) {
            page.keyboard().press("Escape");
            actionsButtons.nth(i).click();
            Locator reorderInOpenMenu = page.locator(".dropdown-menu.show a.dropdown-item")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Reorder", Pattern.CASE_INSENSITIVE)));
            try {
                reorderInOpenMenu.first().click(new Locator.ClickOptions().setTimeout(3_000));
                waitForResourcesReorderReady();
                return;
            } catch (RuntimeException notInThisMenu) {
                // Try the next Actions button.
            }
        }

        Locator anyReorder = page.locator("a.dropdown-item")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Reorder", Pattern.CASE_INSENSITIVE)));
        if (anyReorder.count() > 0) {
            anyReorder.first().click(new Locator.ClickOptions().setForce(true));
            waitForResourcesReorderReady();
            return;
        }
        fail("Expected an Actions menu containing Reorder for a collection with multiple members.");
    }

    private void waitForResourcesReorderReady() {
        assertThat(page.locator("#resources-reorderer")).isVisible();
        assertThat(page.locator("#reorder-list")).isVisible();
        page.waitForFunction(
            "() => document.querySelector('#resources-reorderer')?.updateComplete",
            null,
            new Page.WaitForFunctionOptions().setTimeout(15_000)
        );
        assertThat(page.locator("#reorder-list li.reorder-element " + DRAG_HANDLE).first()).isVisible();
    }

    private void assertReorderChrome() {
        Locator portlet = page.locator(".portletBody.sakai-reorder");
        assertThat(portlet).isVisible();
        assertThat(portlet.locator(".page-header h1")).isVisible();
        assertThat(portlet.locator(".instruction")).containsText(Pattern.compile("Drag items|move buttons|Save", Pattern.CASE_INSENSITIVE));
        assertThat(page.locator(".sakai-reorder-undo.noPrint")).isVisible();
        assertThat(page.locator("#undo-last")).isDisabled();
        assertThat(page.locator("#undo-all")).isDisabled();
        assertThat(page.locator("#reorder-list li.reorder-element [data-action=\"move-up\"]").first()).isVisible();
        assertThat(page.locator("#reorder-list li.reorder-element [data-action=\"move-down\"]").first()).isVisible();
        assertTrue(page.locator("#reorder-list li.reorder-element [data-action=\"move-up\"]").first().isDisabled());
    }

    private void assertPrecedes(String selector, String firstTitle, String secondTitle) {
        List<String> titles = page.locator(selector + " li.reorder-element .sakai-reorder-title").allTextContents();
        int firstTitlePosition = -1;
        int secondTitlePosition = -1;
        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            if (firstTitlePosition < 0 && title.contains(firstTitle)) {
                firstTitlePosition = i;
            }
            if (secondTitlePosition < 0 && title.contains(secondTitle)) {
                secondTitlePosition = i;
            }
        }
        if (firstTitlePosition < 0 || secondTitlePosition < 0) {
            fail("Expected reorder list to contain " + firstTitle + " and " + secondTitle + ".");
        }
        if (firstTitlePosition >= secondTitlePosition) {
            fail("Expected " + firstTitle + " to precede " + secondTitle + ".");
        }
    }
}
