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
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class GroupTitleValidationTest extends SakaiUiTestBase {

    @Test
    void rejectsJoinableSetRenameCollisionWithoutMergingSets() {
        openFreshGroupManager();
        createJoinableSet("Original Set");
        createJoinableSet("Other Set");

        page.locator("#group-manager-form a").filter(new Locator.FilterOptions().setHasText(
                Pattern.compile("^Original Set$"))).click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.locator("#groupTitle").fill("<em>other set</em>");
        page.locator("#save-joinableset-submit-button").click();
        assertThat(page.locator(".portletBody:has(#createnewjoinableset-form) .sak-banner-error"))
                .hasText("The set name has been used already. Please choose a different one.");
        assertThat(page.locator("input[name=\"joinableSetId\"]")).hasValue("Original Set");
        assertThat(page.locator("#groupCollapsible li")).hasText("Original Set 1");

        // Saving the original name must remain valid when editing the same set.
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.locator("#groupTitle").fill("Original Set");
        page.locator("#save-joinableset-submit-button").click();
        assertThat(page.locator("#group-manager-form")).isVisible();
        assertThat(page.locator("#group-manager-form a").filter(new Locator.FilterOptions().setHasText(
                Pattern.compile("^Original Set$")))).hasCount(1);
        Locator otherSet = page.locator("#group-manager-form a").filter(new Locator.FilterOptions().setHasText(
                Pattern.compile("^Other Set$")));
        assertThat(otherSet).hasCount(1);
        otherSet.click();
        assertThat(page.locator("#groupCollapsible li")).hasText("Other Set 1");
    }

    @Test
    void rejectsExistingAutoGroupTitleBeforeConfirmation() {
        openFreshGroupManager();
        page.locator(".navIntraTool a[href$='/group']").click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.locator("#groupTitle").fill("Workshop-1");
        page.locator("#groupMembers").selectOption(page.locator("#groupMembers option").first().getAttribute("value"));
        page.locator("#groupTitle").press("End");
        page.locator("#create-group-submit-button").click();

        page.locator(".navIntraTool a[href$='/autogroups']").click();
        page.locator(".group-manager-role-checkbox").first().check();
        page.locator("#autogroups-continue-button").click();
        page.locator("#sectionOptionDontUseSections").check();
        page.locator("#autogroups-continue-button").click();
        page.locator("#mixtureConfiguration").check();
        page.locator("#splitByGroup").check();
        page.locator("#groupTitleByGroup").fill("<em>workshop</em>");
        page.locator("#groupNumberByGroup").fill("1");
        page.locator("#autogroups-continue-button").click();

        assertThat(page.locator("#autogroups-wizard-step3-form")).isVisible();
        assertThat(page.locator("#autogroups-wizard-step3-form .sak-banner-error"))
                .hasText("After cleanup, group titles must be non-empty, no more than 99 characters long, and distinct.");
        assertThat(page.locator("#autogroups-wizard-step4-form")).hasCount(0);

        page.locator("#groupTitleByGroup").fill("New workshop");
        page.locator("#autogroups-continue-button").click();
        assertThat(page.locator("#autogroups-wizard-step4-form")).isVisible();
        page.locator("#autogroups-creategroups-button").click();
        assertThat(page.locator("#group-manager-form")).containsText("Workshop-1");
        assertThat(page.locator("#group-manager-form")).containsText("New workshop-1");
    }

    private void openFreshGroupManager() {
        sakai.login("instructor1");
        page.navigate(sakai.createProject("instructor1", List.of("sakai\\.announcements")));
        sakai.toolClick("Site Info");
        page.locator(".navIntraTool a")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Manage Groups$", Pattern.CASE_INSENSITIVE)))
                .first().click(new Locator.ClickOptions().setForce(true));
    }

    private void createJoinableSet(String title) {
        page.locator(".navIntraTool a[href$='/joinableset']").click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.locator("#groupTitle").fill(title);
        page.locator("#groupNumber").fill("1");
        page.locator("#groupMaxMembers").fill("5");
        page.locator("#groupTitle").press("End");
        page.locator("#create-joinableset-submit-button").click();
        assertThat(page.locator("#group-manager-form")).isVisible();
    }
}
