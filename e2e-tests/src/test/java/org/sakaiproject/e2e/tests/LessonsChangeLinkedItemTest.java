/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class LessonsChangeLinkedItemTest extends SakaiUiTestBase {

    @Test
    void changingLinkedAssignmentReturnsToEditItemWithUnsavedEdits() {
        String suffix = sakai.randomId();
        String original = "SAK-49295 original " + suffix;
        String replacement = "SAK-49295 replacement " + suffix;
        String description = "Unsaved description " + suffix;

        sakai.login("instructor1");
        String courseUrl = sakai.createFreshCourse("instructor1", List.of(
            "sakai\\.assignment\\.grades", "sakai\\.lessonbuildertool"));
        sakai.createAssignment(courseUrl, original);
        sakai.createAssignment(courseUrl, replacement);
        page.navigate(courseUrl);
        sakai.toolClick("Lessons");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content").setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Link to an Assignment").setExact(true)).click();
        pickAssignment(original);

        Locator item = awaitVisible(page.locator("div.item").filter(new Locator.FilterOptions().setHasText(original)));
        item.hover();
        item.locator(".edit-link").click(new Locator.ClickOptions().setForce(true));

        Locator dialog = awaitVisible(page.locator("#edit-item-dialog"));
        dialog.locator("#description").fill(description);
        dialog.locator("[data-bs-target='#prereqstuffbody']").click();
        awaitVisible(dialog.locator("#item-required")).check();
        awaitVisible(dialog.locator("#item-required2")).check();
        dialog.locator("#assignment-points").fill("1");
        dialog.locator("#change-assignment").click();
        pickAssignment(replacement);

        awaitVisible(dialog);
        assertThat(dialog.locator("#name")).hasValue(replacement);
        assertThat(dialog.locator("#description")).hasValue(description);
        assertThat(dialog.locator("#item-required")).isChecked();
        dialog.locator("[data-bs-target='#prereqstuffbody']").click();
        awaitVisible(dialog.locator("#item-required"));
        assertThat(dialog.locator("#require-label2")).isVisible();
        assertThat(dialog.locator("#item-required2")).isVisible();
        assertThat(dialog.locator("#item-required2")).isChecked();
        assertThat(dialog.locator("#item-required2")).isEnabled();
        assertThat(dialog.locator("#assignment-points")).isEnabled();
        assertThat(dialog.locator("#assignment-points")).hasValue("1");

        dialog.locator("#edit-item").click();
        assertThat(page.locator(".rowdescription").filter(new Locator.FilterOptions().setHasText(description)))
            .hasCount(1);
    }

    private void pickAssignment(String title) {
        Locator row = awaitVisible(page.locator(".hierFlexList")
            .filter(new Locator.FilterOptions().setHasText(title)));
        row.locator("input[type=radio]").check();
        page.locator("input[type=submit].bold").click();
    }

    private Locator awaitVisible(Locator locator) {
        locator.first().waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE).setTimeout(30_000));
        return locator;
    }
}
