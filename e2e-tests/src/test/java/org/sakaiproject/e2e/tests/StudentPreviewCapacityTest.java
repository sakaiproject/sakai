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

class StudentPreviewCapacityTest extends SakaiUiTestBase {

    @Test
    void studentPreviewLeavesTheLastSeatAvailableInASection() {
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

        enterStudentView();
        joinSection(section);
        exitStudentView();
        assertThat(section.locator("td.right")).hasText(new String[] {"0", "1"});

        sakai.login("student0011");
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Section Info");
        joinSection(section);
    }

    @Test
    void studentPreviewLeavesTheLastSeatAvailableInAJoinableGroup() {
        sakai.login("instructor1");
        String siteUrl = sakai.createFreshCourse("instructor1", List.of("sakai\\.announcements"));
        sakai.addMemberToSite(siteUrl, "student0011", "Student");
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Site Info");
        String siteInfoUrl = page.url();

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Manage Groups").setExact(true)).click();
        page.locator(".navIntraTool a[href$='/joinableset']").click();
        sakai.waitForPortalFocus();
        String setTitle = "Preview signup";
        page.locator("#groupTitle").fill(setTitle);
        page.locator("#groupNumber").fill("1");
        page.locator("#groupMaxMembers").fill("1");
        page.locator("#groupTitle").press("End");
        page.locator("#create-joinableset-submit-button").click();
        assertThat(page.locator("#group-manager-form")).containsText(setTitle);

        sakai.gotoPath(siteInfoUrl);
        enterStudentView();
        String groupTitle = setTitle + " 1";
        joinSiteInfoGroup(groupTitle);
        exitStudentView();

        sakai.login("student0011");
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Site Info");
        joinSiteInfoGroup(groupTitle);
    }

    private void enterStudentView() {
        sakai.waitForPortalFocus();
        Locator roles = page.locator("#roleSwitchSelect");
        if (roles.isVisible()) {
            roles.selectOption(new SelectOption().setLabel("Student"));
        } else {
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Enter Student View").setExact(true)).click();
        }
        assertThat(page.locator("#roleSwitchAnchor.Mrphs-roleSwitch__exit")).isVisible();
    }

    private void exitStudentView() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Exit Student View").setExact(true)).click();
        assertThat(page.locator("#roleSwitchAnchor.Mrphs-roleSwitch__exit")).hasCount(0);
    }

    private void joinSiteInfoGroup(String groupTitle) {
        Locator joinable = page.locator(".info-accordion").filter(
            new Locator.FilterOptions().setHas(page.locator("form[name='joinableForm']")));
        joinable.locator(".toggleAnchor").click();
        Locator group = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHas(
            page.getByText(groupTitle, new Page.GetByTextOptions().setExact(true))));
        assertThat(group).containsText("0 (1)");
        group.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Join").setExact(true)).click();
        assertThat(group.locator("span[id^='group']")).hasText(groupTitle);
    }

    private void joinSection(Locator section) {
        section.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Join")).click();
        assertThat(section.getByText("Member", new Locator.GetByTextOptions().setExact(true))).isVisible();
    }
}
