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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Moving Resources items to the Trash and restoring them, from the community Resources regression script. */
class ResourcesTrashTest extends SakaiUiTestBase {

    @Test
    void fileMovedToTrashCanBeRestored() {
        ResourcesUi resources = openResources();
        resources.openCreateFolders(resources.rootRow(), List.of("Docs"));
        resources.createFolders();
        resources.uploadText("Docs", "notes.txt", "Notes");
        // Files are listed only once their folder is open.
        resources.openFolder("Docs");

        moveToTrash(resources, "notes.txt");
        assertThat(resources.itemLink("notes.txt")).hasCount(0);
        restoreFromTrash("notes.txt");
        assertThat(resources.itemLink("notes.txt")).isVisible();
    }

    @Test
    @Disabled("SAK-44521 is open: a folder recreated by restoring its files loses its settings")
    void restoredFolderKeepsItsSettings() {
        ResourcesUi resources = openResources();
        resources.openCreateFolders(resources.rootRow(), List.of("Restore-Me"));
        page.locator("#description_0").fill("Kept description");
        page.locator("#hidden_true_0").check();
        resources.createFolders();
        assertThat(resources.row("Restore-Me").locator(".resource-access")).hasText("Entire site (Hidden)");
        resources.uploadText("Restore-Me", "inside.txt", "Inside");

        // The Trash lists the files of a deleted folder, not the folder itself.
        moveToTrash(resources, "Restore-Me");
        restoreFromTrash("inside.txt");

        assertThat(resources.row("Restore-Me").locator(".resource-access")).hasText("Entire site (Hidden)");
        resources.itemAction(resources.row("Restore-Me"), "Edit Details");
        assertThat(page.locator("#hidden_true_0")).isChecked();
        assertThat(page.locator("#description_0")).hasValue("Kept description");
    }

    private ResourcesUi openResources() {
        ResourcesUi resources = new ResourcesUi(page, sakai);
        sakai.login("instructor1");
        page.navigate(sakai.createFreshCourse("instructor1", List.of("sakai\\.resources")));
        sakai.toolClick("Resources");
        return resources;
    }

    private void moveToTrash(ResourcesUi resources, String name) {
        resources.row(name).locator("input[name='selectedMembers']").check();
        page.locator("#delete-button").click();
        assertThat(page.locator("#content")).containsText(name);
        page.locator("[name='eventSubmit_doFinalizeDelete']").click();
        assertThat(resources.rootRow()).isVisible();
    }

    private void restoreFromTrash(String fileName) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Trash").setExact(true)).click();
        Locator trashed = page.locator("tr").filter(new Locator.FilterOptions().setHasText(fileName))
            .locator("input[name='selectedMembers']");
        assertThat(trashed).hasCount(1);
        trashed.check();
        page.locator("#restore-button").click();
        assertThat(page.locator(".sak-banner-success")).containsText("successfully restored");
        page.locator("#cancel-button").click();
    }
}
