/**
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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class SectionPreviewCapacityTest extends SakaiUiTestBase {

    @Test
    void studentPreviewLeavesTheLastSeatAvailable() {
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.sections"));
        sakai.addMemberToSite(siteUrl, "student0011", "Student");
        sakai.setSiteProperty(siteUrl, "sections_externally_maintained", "false");
        sakai.setSiteProperty(siteUrl, "sections_student_registration_allowed", "true");
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Section Info");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Sections").setExact(true)).click();
        page.locator("select[id$=':category']").selectOption(new SelectOption().setLabel("Lab"));
        String sectionTitle = "Preview capacity";
        page.locator("input[id$=':titleInput']").fill(sectionTitle);
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Limit number of students in section to")).check();
        page.locator("input[id$=':maxEnrollmentInput']").fill("1");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Sections").setExact(true)).click();

        Locator section = page.locator("tr").filter(new Locator.FilterOptions().setHasText(sectionTitle));
        assertThat(section.locator("td.right")).hasText(new String[] {"0", "1"});

        Locator roles = page.locator("#roleSwitchSelect");
        if (roles.isVisible()) {
            roles.selectOption(new SelectOption().setLabel("Student"));
        } else {
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Enter Student View").setExact(true)).click();
        }
        joinSection(section);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Exit Student View").setExact(true)).click();
        assertThat(section.locator("td.right")).hasText(new String[] {"0", "1"});

        sakai.login("student0011");
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Section Info");
        joinSection(section);
    }

    private void joinSection(Locator section) {
        section.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Join")).click();
        assertThat(section.getByText("Member", new Locator.GetByTextOptions().setExact(true))).isVisible();
    }
}
