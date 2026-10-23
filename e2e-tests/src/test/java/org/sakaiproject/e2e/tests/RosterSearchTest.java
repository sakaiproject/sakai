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
import com.microsoft.playwright.options.SelectOption;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class RosterSearchTest extends SakaiUiTestBase {

    @Test
    void clearingTheSearchRestoresResultsWithinTheSelectedGroupAndRole() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.site\\.roster2"));
        String group = "Roster search " + sakai.randomId();
        String groupId = sakai.addGroup(siteUrl, group, "student0011", "student0012");

        sakai.gotoPath(siteUrl);
        sakai.toolClick("Roster");
        Locator groups = page.locator("#roster-groups-selector");
        Locator roles = page.locator("#roster-roles-selector");
        Locator members = page.locator("#roster-members .roster-entry");
        Locator search = page.locator("#roster-search-field");

        groups.selectOption(new SelectOption().setLabel(group));
        assertThat(members).hasCount(2);
        roles.selectOption("Student");
        assertThat(members).hasCount(2);

        search.fill("student0011");
        search.press("Enter");
        assertThat(members).hasCount(1);

        // deleting the text, as the native clear button does, restores the group's students
        search.fill("");
        assertThat(members).hasCount(2);
        assertThat(groups).hasValue(groupId);
        assertThat(roles).hasValue("Student");

        search.fill("student0011");
        search.press("Enter");
        assertThat(members).hasCount(1);

        // Enter on an empty field restores them too; the value is emptied without an input event so only Enter can act
        search.evaluate("field => field.value = ''");
        search.press("Enter");
        assertThat(members).hasCount(2);
        assertThat(groups).hasValue(groupId);
        assertThat(roles).hasValue("Student");
    }
}
