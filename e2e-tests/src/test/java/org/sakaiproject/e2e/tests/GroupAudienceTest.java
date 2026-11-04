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
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class GroupAudienceTest extends SakaiUiTestBase {

    @Test
    void resourceRowsSaveIndependentGroupSelections() {
        sakai.login("instructor1");
        String site = createGroupedSite("sakai\\.resources");
        sakai.toolClick("Resources");
        page.locator("button[title='Actions']").first().click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Create Folders").setExact(true)).click();
        Locator names = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Folder Name").setExact(true));
        names.first().fill("North folder");
        setFolderGroup(0, "North seminar");
        page.locator("#addFolderInput").click();
        names.nth(1).fill("South folder");
        setFolderGroup(1, "South seminar");
        page.locator("#saveChanges").click();
        assertThat(folder("North folder")).isVisible();
        assertThat(folder("South folder")).isVisible();

        sakai.login("student0011");
        page.navigate(site);
        sakai.toolClick("Resources");
        assertThat(folder("North folder")).isVisible();
        assertThat(folder("South folder")).hasCount(0);
        sakai.login("student0012");
        page.navigate(site);
        sakai.toolClick("Resources");
        assertThat(folder("South folder")).isVisible();
        assertThat(folder("North folder")).hasCount(0);
    }

    @Test
    void wikiKeepsTheSelectedGroupWhenReopened() {
        sakai.login("instructor1");
        String site = createGroupedSite("sakai\\.rwiki");
        sakai.addGroup(site, "Wiki seminar", "instructor1", "student0011");
        sakai.toolClick("Wiki");
        openWikiEditor();
        page.locator("#wiki-textarea-content").fill("[Seminar notes]");
        page.locator("input[name=command_save]").first().click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Seminar notes")).first().click();
        openWikiEditor();
        page.locator("#wiki-textarea-content").fill("Seminar wiki content");
        page.locator("#groupSelector").check();
        selectGroup(page.locator("#groupTable sakai-group-picker"), "Wiki seminar");
        page.locator("input[name=command_save]").first().click();
        openWikiEditor();
        assertThat(page.locator("#groupTable sakai-group-picker")
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove Wiki seminar"))).isVisible();
    }

    private String createGroupedSite(String tool) {
        String site = sakai.createFreshCourse("instructor1", List.of(tool));
        sakai.addGroup(site, "North seminar", "student0011");
        sakai.addGroup(site, "South seminar", "student0012");
        page.navigate(site);
        return site;
    }

    private void setFolderGroup(int index, String name) {
        page.waitForLoadState(LoadState.LOAD);
        page.locator("a[id^=propsTrigger]").nth(index).click();
        Locator groupAccess = page.locator("input[id^=access_mode_groups]").nth(index);
        if (!groupAccess.isVisible()) {
            page.locator("a[data-bs-target='#collapseAccess']").nth(index).click();
        }
        groupAccess.check();
        selectGroup(page.locator("sakai-group-picker[bind-select]").nth(index), name);
    }

    private void selectGroup(Locator picker, String name) {
        Locator search = picker.getByRole(AriaRole.COMBOBOX);
        assertThat(search).isVisible();
        search.fill(name);
        search.click();
        picker.getByRole(AriaRole.OPTION, new Locator.GetByRoleOptions().setName(name)).click();
        search.press("Escape");
    }

    private Locator folder(String name) {
        return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    private void openWikiEditor() {
        page.locator("#editLink").click();
        page.waitForLoadState(LoadState.LOAD);
        assertThat(page.locator("#wiki-textarea-content")).isVisible();
    }
}
