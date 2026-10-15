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
import com.microsoft.playwright.options.BoundingBox;
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

        openResourcesReorder();
        assertReorderChrome();

        Locator items = page.locator("#reorder-list li.reorder-element.sakai-reorder-row");
        assertThat(items).hasCount(3);
        assertThat(page.locator("#reorder-list")).containsText(folderA);
        assertThat(page.locator("#reorder-list")).containsText(folderC);
        assertThat(page.locator("input[id^=\"index\"]")).hasCount(0);

        Locator first = items.filter(new Locator.FilterOptions().setHasText(folderA)).first();
        Locator last = items.filter(new Locator.FilterOptions().setHasText(folderC)).first();
        dragBefore(last.locator(DRAG_HANDLE).first(), first);
        assertPrecedes("#reorder-list", folderC, folderA);
        assertThat(last.locator("input[name^=\"position_\"]")).hasValue("1");

        page.locator("#save").click();
        page.waitForLoadState();
        String afterDragListing = page.locator("body").innerText();
        if (afterDragListing.indexOf(folderC) < 0 || afterDragListing.indexOf(folderA) < 0
                || afterDragListing.indexOf(folderC) > afterDragListing.indexOf(folderA)) {
            fail("Expected drag-and-drop Resources reorder to persist after Save.");
        }

        openResourcesReorder();
        items = page.locator("#reorder-list li.reorder-element.sakai-reorder-row");
        assertPrecedes("#reorder-list", folderC, folderA);

        Locator undoLast = page.locator("#undo-last");
        Locator folderAAfterReload = items.filter(new Locator.FilterOptions().setHasText(folderA)).first();
        dragBefore(folderAAfterReload.locator(DRAG_HANDLE).first(),
            items.filter(new Locator.FilterOptions().setHasText(folderC)).first());
        assertPrecedes("#reorder-list", folderA, folderC);
        assertThat(undoLast).isEnabled();
        undoLast.click();
        assertPrecedes("#reorder-list", folderC, folderA);

        // Current order after undo is C, A, B (from earlier drag persist of C before A).
        Locator folderARow = items.filter(new Locator.FilterOptions().setHasText(folderA)).first();
        Locator moveDown = folderARow.locator("[data-action=\"move-down\"]");
        assertThat(moveDown).isEnabled();
        moveDown.click();
        assertPrecedes("#reorder-list", folderB, folderA);

        folderARow = items.filter(new Locator.FilterOptions().setHasText(folderA)).first();
        Locator moveUp = folderARow.locator("[data-action=\"move-up\"]");
        assertThat(moveUp).isEnabled();
        moveUp.click();
        assertPrecedes("#reorder-list", folderA, folderB);

        // Final order for save: C, B, A
        folderARow = items.filter(new Locator.FilterOptions().setHasText(folderA)).first();
        folderARow.locator("[data-action=\"move-down\"]").click();
        assertPrecedes("#reorder-list", folderC, folderB);
        assertPrecedes("#reorder-list", folderB, folderA);

        page.locator("#save").click();
        page.waitForLoadState();

        String listing = page.locator("body").innerText();
        int cPos = listing.indexOf(folderC);
        int bPos = listing.indexOf(folderB);
        int aPos = listing.indexOf(folderA);
        if (cPos < 0 || bPos < 0 || aPos < 0 || cPos > bPos || bPos > aPos) {
            fail("Expected saved Resources order C, B, A.");
        }
    }

    private void openResourcesReorder() {
        page.locator("button[title='Actions']").first().click();
        page.getByRole(AriaRole.MENUITEM,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Reorder$", Pattern.CASE_INSENSITIVE))).click();
        assertThat(page.locator("#resources-reorderer")).isVisible();
        assertThat(page.locator("#reorder-list")).isVisible();
        page.waitForFunction(
            "() => document.querySelector('#resources-reorderer')?.updateComplete",
            null,
            new Page.WaitForFunctionOptions().setTimeout(20_000)
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

    private void dragBefore(Locator source, Locator target) {
        BoundingBox sourceBox = source.boundingBox();
        BoundingBox targetBox = target.boundingBox();
        if (sourceBox == null || targetBox == null) {
            fail("Expected Resources reorder items to have bounding boxes.");
            return;
        }

        double sourceX = sourceBox.x + sourceBox.width / 2;
        double sourceY = sourceBox.y + sourceBox.height / 2;
        double targetY = targetBox.y + targetBox.height / 4;

        page.mouse().move(sourceX, sourceY);
        page.mouse().down();
        for (int step = 1; step <= 10; step++) {
            page.mouse().move(sourceX, sourceY + (targetY - sourceY) * step / 10);
        }
        page.mouse().up();
    }

    private void assertPrecedes(String selector, String firstTitle, String secondTitle) {
        String listText = page.locator(selector).innerText();
        int firstTitlePosition = listText.indexOf(firstTitle);
        int secondTitlePosition = listText.indexOf(secondTitle);
        if (firstTitlePosition < 0 || secondTitlePosition < 0) {
            fail("Expected reorder list to contain " + firstTitle + " and " + secondTitle + ".");
        }
        if (firstTitlePosition >= secondTitlePosition) {
            fail("Expected " + firstTitle + " to precede " + secondTitle + ".");
        }
    }
}
