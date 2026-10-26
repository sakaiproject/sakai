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

class SectionsRosterSearchTest extends SakaiUiTestBase {

    @Test
    void clearingTheSearchRestoresStudentsWithinTheSelectedSection() {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.sections"));
        sakai.gotoPath(siteUrl);
        sakai.toolClick("Section Info");
        page.locator("a").filter(new Locator.FilterOptions()
            .setHasText(Pattern.compile("Student Memberships", Pattern.CASE_INSENSITIVE))).first().click();

        Locator filter = page.locator("[id='rosterForm:selectSectionFilter']");
        Locator search = page.locator("[id='rosterForm:search']");
        Locator students = page.locator("[id='rosterForm:sectionsTable'] > tbody > tr");

        // the course's own roster section, which the filter submits the page to show
        String section = filter.locator("option").nth(1).getAttribute("value");
        submits(() -> filter.selectOption(section));
        assertThat(filter).hasValue(section);
        assertThat(search).hasAttribute("type", "search");
        assertThat(students.nth(1)).isVisible();
        int listed = students.count();

        submits(() -> {
            search.fill("student0011");
            search.press("Enter");
        });
        assertThat(students).hasCount(1);
        assertThat(search).hasValue("student0011");

        // deleting the text, as the native clear button does, drops the search and keeps the section
        submits(() -> search.fill(""));
        assertThat(students).hasCount(listed);
        assertThat(search).hasValue("");
        assertThat(filter).hasValue(section);
    }

    /** Runs an action that posts the roster form and waits for the page it answers with. */
    private void submits(Runnable action) {
        page.waitForResponse(r -> "POST".equals(r.request().method()) && r.url().contains("/roster"), action);
    }
}
