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

        // SAK-52774: adding a participant after the maintainer's first visit.
        sakai.addMemberToSite(siteUrl, "student0011", "access");
        page.reload();
        Locator folder = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Albertson, Albert (student0011)").setExact(true));
        assertThat(folder).isVisible();

        try (BrowserContext studentContext = context.browser().newContext(
                new Browser.NewContextOptions().setIgnoreHTTPSErrors(true))) {
            Page studentPage = studentContext.newPage();
            SakaiHelper student = new SakaiHelper(studentPage, SakaiEnvironment.baseUrl(), studentPage.request());
            student.login("student0011");
            studentPage.navigate(dropboxUrl);
            Locator studentFolder = studentPage.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Albertson, Albert (student0011)").setExact(true));
            assertThat(studentFolder).isVisible();

            sakai.toolClick("Site Info");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Participants").setExact(true)).click();
            page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Remove student0011").setExact(true)).check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update Participants").setExact(true)).click();
            page.navigate(dropboxUrl);
            assertThat(folder).hasCount(0);

            // SAK-52786: the returning participant must recreate a named folder.
            sakai.addMemberToSite(siteUrl, "student0011", "access");
            studentPage.reload();
            assertThat(studentFolder).isVisible();
            page.reload();
            assertThat(folder).isVisible();
        }
    }
}
