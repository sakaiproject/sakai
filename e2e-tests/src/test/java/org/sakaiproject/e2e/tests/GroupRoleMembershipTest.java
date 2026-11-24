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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/**
 * A group whose membership is based on a site role must keep tracking that role when the site
 * membership changes.
 */
class GroupRoleMembershipTest extends SakaiUiTestBase {

    private static final String ROLE = "Teaching Assistant";
    private static final String STUDENT_ROLE = "Student";
    /** Site Info and the group helper are slow to render, the default assertion timeout is too short. */
    private static final double RENDER_TIMEOUT_MS = 30_000;

    @Test
    void roleGroupFollowsParticipantsGainingAndLosingTheRole() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.announcements"));
        String groupTitle = "Role group " + sakai.randomId();

        openManageGroups(siteUrl);
        createRoleGroup(groupTitle);
        int membersBefore = groupMemberCount(groupTitle);

        openManageParticipants(siteUrl);
        String participantRoleSelect = participantWithRole(STUDENT_ROLE);
        changeRole(participantRoleSelect, ROLE);
        openManageGroups(siteUrl);
        assertEquals(membersBefore + 1, groupMemberCount(groupTitle),
            "group built from the " + ROLE + " role did not pick up the new " + ROLE);

        // demoting the participant again also leaves the shared course as it was
        openManageParticipants(siteUrl);
        changeRole(participantRoleSelect, STUDENT_ROLE);
        openManageGroups(siteUrl);
        assertEquals(membersBefore, groupMemberCount(groupTitle),
            "group built from the " + ROLE + " role kept a participant who lost the role");
    }

    private void openManageGroups(String siteUrl) {
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Site Info");
        clickNav("Manage Groups");
    }

    private void createRoleGroup(String groupTitle) {
        clickNav("Create New Group");

        Locator title = awaitVisible(page.locator("#groupTitle"));
        title.fill(groupTitle);
        // the submit button stays disabled until the title input reports a value
        title.dispatchEvent("keyup");

        // the members select is wrapped by select2, so the underlying element is not clickable
        page.locator("#groupMembers").selectOption(new SelectOption().setValue(ROLE),
            new Locator.SelectOptionOptions().setForce(true));

        page.locator("#create-group-submit-button").click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        awaitVisible(groupRow(groupTitle));
    }

    /**
     * The id of the role select of a participant holding the role. Every demo account is already
     * enrolled in the course roster, so tests change a participant's role rather than add one.
     */
    private String participantWithRole(String role) {
        Locator roleSelects = page.locator("#siteMembers select[id^=\"role\"]");
        for (int index = roleSelects.count() - 1; index >= 0; index--) {
            if (role.equals(roleSelects.nth(index).inputValue())) {
                return roleSelects.nth(index).getAttribute("id");
            }
        }
        throw new AssertionError("no participant with the " + role + " role");
    }

    /** Changes a participant's site role on the Manage Participants page. */
    private void changeRole(String roleSelectId, String role) {
        page.locator("[id=\"" + roleSelectId + "\"]").selectOption(new SelectOption().setValue(role));

        page.locator("input[type=\"button\"].active").first().click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();

        Locator reloaded = awaitVisible(page.locator("[id=\"" + roleSelectId + "\"]"));
        assertEquals(role, reloaded.inputValue(), "Site Info did not apply the role change");
    }

    private void openManageParticipants(String siteUrl) {
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Site Info");
        // Site Info reopens its last view, and the groups pages have no Manage Participants link
        page.navigate(page.url().replaceFirst("/tool/([^/?#]+).*", "/tool-reset/$1"));
        clickNav("Manage Participants");
        awaitVisible(page.locator("#siteMembers"));
    }

    private void clickNav(String label) {
        awaitVisible(navLink(label)).click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    private Locator navLink(String label) {
        return page.locator(".navIntraTool a")
            .filter(new Locator.FilterOptions().setHasText(exactly(label)))
            .first();
    }

    /** Playwright hands the pattern to the browser, so it must be plain JavaScript regex syntax. */
    private Pattern exactly(String label) {
        return Pattern.compile("^" + label + "$", Pattern.CASE_INSENSITIVE);
    }

    private Locator awaitVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(RENDER_TIMEOUT_MS));
        return locator;
    }

    private Locator groupRow(String groupTitle) {
        return page.locator("#groupTable tbody tr")
            .filter(new Locator.FilterOptions().setHasText(groupTitle))
            .first();
    }

    /** Reads the comma separated Members column of the group row, the columns shown vary per site. */
    private int groupMemberCount(String groupTitle) {
        Locator headers = page.locator("#groupTable thead th");
        int membersColumn = -1;
        for (int index = 0; index < headers.count(); index++) {
            if ("Members".equalsIgnoreCase(text(headers.nth(index)).trim())) {
                membersColumn = index;
                break;
            }
        }
        assertTrue(membersColumn >= 0, "Members column not found in the group table");

        String members = text(groupRow(groupTitle).locator("td").nth(membersColumn)).trim();
        return members.isEmpty() ? 0 : members.split(",").length;
    }

    private String text(Locator locator) {
        String value = locator.textContent();
        return value == null ? "" : value;
    }
}
