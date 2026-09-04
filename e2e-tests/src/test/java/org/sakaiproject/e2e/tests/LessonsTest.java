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
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LessonsTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void canCreateNewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of(
            "sakai\\.rubrics",
            "sakai\\.assignment\\.grades",
            "sakai\\.gradebookng",
            "sakai\\.lessonbuildertool"
        ));
    }

    @Test
    @Order(2)
    void createLessonChecklistItem() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Lessons");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click(new Locator.ClickOptions().setForce(true));
        page.locator(".add-checklist-link:visible").first().click(new Locator.ClickOptions().setForce(true));

        Locator nameInput = page.locator("#name:visible").first();
        if (nameInput.count() == 0) {
            assertThat(page.locator("body")).containsText(Pattern.compile("Checklist|Add Checklist", Pattern.CASE_INSENSITIVE));
            return;
        }

        nameInput.fill("Checklist");

        page.locator("#addChecklistItemButton > :nth-child(2)").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv1 > .checklist-item-name").fill("A");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv2 > .checklist-item-name").fill("B");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv3 > .checklist-item-name").fill("C");
        page.locator("#addChecklistItemButton").click(new Locator.ClickOptions().setForce(true));
        page.locator("#checklistItemDiv4 > .checklist-item-name").fill("D");

        page.locator("#save").click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator("#content")).isVisible();
    }

    @Test
    @Order(3)
    void canPromoteOnlySubpagesToSiteNavigation() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Lessons");

        addSubpage("Promote Me");

        sakai.toolClick("Lessons");
        openAddMorePages();
        chooseExistingPageForSiteNavigation();

        Locator lessonsRadio = page.locator("input[type=radio][title^=\"Lessons \"]").first();
        assertThat(lessonsRadio).isDisabled();
        lessonsRadio.evaluate("element => element.removeAttribute('disabled')");
        lessonsRadio.check();
        useSelectedItem();
        assertThat(page.locator("body")).containsText("The page could not be added to site navigation.");
        Locator currentSiteNavigation = page.locator("li.site-list-item.is-current-site .site-page-list a.btn-nav");
        assertThat(currentSiteNavigation.filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("^\\s*Lessons\\s*$", Pattern.CASE_INSENSITIVE)))).hasCount(1);

        openAddMorePages();
        chooseExistingPageForSiteNavigation();
        Locator subpageRadio = page.locator("input[type=radio][title^=\"Promote Me \"]").first();
        assertThat(subpageRadio).isEnabled();
        subpageRadio.check();
        useSelectedItem();

        sakai.toolClick("Promote Me");
        assertThat(page.locator(".neoPortletTitleWrap")).containsText("Promote Me");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("More Tools")).click();
        Locator moreToolsDialog = page.locator("#moreDiv");
        assertThat(moreToolsDialog).isVisible();
        assertThat(moreToolsDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add More Pages").setExact(true))).isVisible();
    }

    private void addSubpage(String title) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Content")).first().click();

        Locator addContentDialog = page.locator("#addContentDiv");
        assertThat(addContentDialog).isVisible();
        addContentDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add Subpage").setExact(true)).click();

        Locator subpageDialog = page.locator("#subpage-dialog");
        assertThat(subpageDialog).isVisible();
        subpageDialog.locator("#subpage-title").fill(title);
        subpageDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Create").setExact(true)).click();

        assertThat(page.locator("#subpage-breadcrumb-div")).containsText(title);
    }

    private void openAddMorePages() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("More Tools")).click();
        Locator moreToolsDialog = page.locator("#moreDiv");
        assertThat(moreToolsDialog).isVisible();
        moreToolsDialog.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add More Pages").setExact(true)).click();
        assertThat(page.locator("#new-page-dialog")).isVisible();
    }

    private void chooseExistingPageForSiteNavigation() {
        page.locator("#new-page-dialog").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
            .setName(Pattern.compile("Put existing page", Pattern.CASE_INSENSITIVE))).click();
    }

    private void useSelectedItem() {
        page.locator(".PagePicker").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
            .setName(Pattern.compile("Use selected item", Pattern.CASE_INSENSITIVE))).click();
    }
}
