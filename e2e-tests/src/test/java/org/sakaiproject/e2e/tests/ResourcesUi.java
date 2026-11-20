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
import com.microsoft.playwright.options.FilePayload;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.sakaiproject.e2e.support.SakaiHelper;

/** Shared steps for the Resources list and its create, upload and item action pages. */
final class ResourcesUi {

    private static final DateTimeFormatter DATE_TIME_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final Page page;
    private final SakaiHelper sakai;

    ResourcesUi(Page page, SakaiHelper sakai) {
        this.page = page;
        this.sakai = sakai;
    }

    /** The site folder is always the first listed row; its icon title differs when it is empty. */
    Locator rootRow() {
        return page.locator("tr").filter(new Locator.FilterOptions().setHas(page.locator("button[title='Actions']"))).first();
    }

    Locator itemLink(String name) {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    Locator row(String name) {
        return page.locator("tr").filter(new Locator.FilterOptions().setHas(itemLink(name)));
    }

    /** Opens an item's Actions menu and picks one of its entries. */
    void itemAction(Locator itemRow, String action) {
        itemRow.locator("button[title='Actions']").click();
        itemRow.getByRole(AriaRole.MENUITEM, new Locator.GetByRoleOptions().setName(action).setExact(true)).click();
    }

    /** Opens Create Folders in the given folder with one row per name and every row's access options shown. */
    void openCreateFolders(Locator parentRow, List<String> names) {
        itemAction(parentRow, "Create Folders");
        assertThat(page.locator("#content_0")).isVisible();
        for (int i = 1; i < names.size(); i++) {
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Another Folder").setExact(true)).click();
            assertThat(page.locator("#content_" + i)).isVisible();
        }
        for (int i = 0; i < names.size(); i++) {
            page.locator("#content_" + i).fill(names.get(i));
            page.locator("#propsTrigger_" + i).click();
        }
        // Every row reuses the id collapseAccess, so one header toggles the panels of all rows.
        page.locator("#propertiesDiv_0 a[data-bs-target='#collapseAccess']").click();
        for (int i = 0; i < names.size(); i++) {
            assertThat(page.locator("#hidden_true_" + i)).isVisible();
        }
    }

    void createFolders() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create Folders Now").setExact(true)).click();
        assertThat(rootRow()).isVisible();
    }

    void restrictToGroup(int row, String groupId) {
        page.locator("#access_mode_groups_" + row).check();
        page.locator("#access_groups_" + row).selectOption(groupId);
    }

    void setStartDate(int row, LocalDateTime value) {
        page.locator("#use_start_date_" + row).check();
        sakai.selectDate("#releaseDatePicker_" + row, value.format(DATE_TIME_LOCAL));
    }

    void upload(Locator folderRow, String fileName, String contentType, byte[] content, boolean hidden) {
        itemAction(folderRow, "Upload Files");
        page.locator("input[type=file]").setInputFiles(new FilePayload(fileName, contentType, content));
        if (hidden) {
            page.locator("#hidden_true").check();
        }
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).click();
        assertThat(rootRow()).isVisible();
    }

    void uploadText(String folder, String fileName, String content) {
        upload(row(folder), fileName, "text/plain", content.getBytes(StandardCharsets.UTF_8), false);
    }
}
