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
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/** Manage Participants uses the shared #searchFilterPanel, as Assignments, Announcements and Membership do. */
class ParticipantSearchTest extends SakaiUiTestBase {

    @Test
    void clearingTheSearchRestoresTheParticipants() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of());
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Site Info");
        page.locator(".navIntraTool a").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("^\\s*Manage Participants\\s*$"))).first().click();

        Locator search = page.locator("#searchFilter1");
        Locator participants = page.locator("#siteMembers > tbody > tr");
        assertThat(search).hasAttribute("type", "search");
        assertThat(participants.nth(1)).isVisible();
        int listed = participants.count();

        search.fill("student0011");
        search.press("Enter");
        assertThat(participants).hasCount(1);
        assertThat(search).hasValue("student0011");

        // deleting the text, as the native clear button does, drops the search
        search.fill("");
        assertThat(participants).hasCount(listed);

        search.fill("student0011");
        search.press("Enter");
        assertThat(participants).hasCount(1);

        // Enter on an empty field drops it too; the value is emptied without an input event so only Enter can act
        search.evaluate("field => field.value = ''");
        search.press("Enter");
        assertThat(participants).hasCount(listed);
    }
}
