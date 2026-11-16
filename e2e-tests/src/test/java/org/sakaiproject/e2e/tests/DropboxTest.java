/*
 * Copyright (c) 2026 The Apereo Foundation
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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.SelectOption;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiEnvironment;
import org.sakaiproject.e2e.support.SakaiHelper;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class DropboxTest extends SakaiUiTestBase {

    @Test
    void namedFoldersFollowMembershipChangesInExistingSessions() {
        sakai.login("instructor1");
        String siteUrl = sakai.createProject("instructor1", List.of("sakai.dropbox"));
        page.navigate(siteUrl);
        sakai.toolClick("Drop Box");
        String dropboxUrl = page.url();
        Locator highlight = page.locator("#dropboxHighlight");
        sakai.submitForm(() -> highlight.selectOption("14"));

        // SAK-52774: adding a participant after the maintainer's first visit.
        sakai.addMemberToSite(siteUrl, "student0011", "access");
        sakai.toolClick("Site Info");
        sakai.toolClick("Drop Box");
        assertThat(highlight).hasValue("14");
        Locator folder = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Albertson, Albert (student0011)").setExact(true));
        assertThat(folder).isVisible();

        try (BrowserContext studentContext = context.browser().newContext(
                new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
             BrowserContext adminContext = context.browser().newContext(
                new Browser.NewContextOptions().setIgnoreHTTPSErrors(true))) {
            Page studentPage = studentContext.newPage();
            SakaiHelper student = new SakaiHelper(studentPage, SakaiEnvironment.baseUrl(), studentPage.request());
            student.login("student0011");
            studentPage.navigate(dropboxUrl);
            Locator studentFolder = studentPage.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Albertson, Albert (student0011)").setExact(true));
            assertThat(studentFolder).isVisible();

            // Keep an administrator inside the empty folder while its owner is removed.
            Page adminPage = adminContext.newPage();
            SakaiHelper admin = new SakaiHelper(adminPage, SakaiEnvironment.baseUrl(), adminPage.request());
            admin.login("admin");
            adminPage.navigate(dropboxUrl);
            adminPage.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Albertson, Albert (student0011)").setExact(true)).click();

            sakai.toolClick("Site Info");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Participants").setExact(true)).click();
            page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Remove student0011").setExact(true)).check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update Participants").setExact(true)).click();
            adminPage.reload();
            assertThat(adminPage.locator("#dropboxHighlight")).isVisible();
            page.navigate(dropboxUrl);
            assertThat(folder).hasCount(0);

            // SAK-52786: the returning participant must recreate a named folder.
            sakai.addMemberToSite(siteUrl, "student0011", "access");
            studentPage.reload();
            assertThat(studentFolder).isVisible();
            page.reload();
            assertThat(folder).isVisible();
        }

        // Bulk upload can be entered without revisiting the list after adding a user.
        sakai.addMemberToSite(siteUrl, "student0200", "access");
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Upload files to multiple Drop Box folders").setExact(true)).click();
        page.getByLabel("File To Upload").setInputFiles(new FilePayload(
            "dropbox-regression.txt", "text/plain", "Drop Box feedback".getBytes(StandardCharsets.UTF_8)));
        page.getByLabel("Available users").selectOption(new SelectOption[] {
            new SelectOption().setLabel("Albertson, Albert"), new SelectOption().setLabel("Student, 0200") });
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(">").setExact(true)).click();
        // Remove one selected recipient in another tab while the upload form stays open.
        Page rosterPage = context.newPage();
        SakaiHelper roster = new SakaiHelper(rosterPage, SakaiEnvironment.baseUrl(), rosterPage.request());
        rosterPage.navigate(siteUrl);
        roster.toolClick("Site Info");
        rosterPage.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Remove student0011").setExact(true)).check();
        rosterPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update Participants").setExact(true)).click();
        rosterPage.close();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue").setExact(true)).click();
        assertThat(page.locator(".sak-banner-warn").filter(new Locator.FilterOptions().setHasText("Albert")))
            .containsText("the Drop Box folder no longer exists.");
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Student, 0200 (student0200)").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("dropbox-regression.txt").setExact(true))).isVisible();

        // Removing a participant must retain a populated Drop Box and its uploads.
        sakai.toolClick("Site Info");
        page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Remove student0200").setExact(true)).check();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update Participants").setExact(true)).click();
        page.navigate(dropboxUrl);
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Student, 0200 (student0200)").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("dropbox-regression.txt").setExact(true))).isVisible();
    }
}
