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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/**
 * Uses the group-access fixture documented in e2e-tests/README.md.
 */
@EnabledIfSystemProperty(named = "samigo.groupAccess.siteUrl", matches = ".+")
class SamigoGroupAccessTest extends SakaiUiTestBase {

    @Test
    void instructorSeesAllReleaseGroupsAndCounts() {
        openAssessments(System.getProperty("samigo.groupAccess.instructor", "instructor1"));
        assertCounts("SAK-52287 Site", 2, 2);
        assertCounts("SAK-52287 Both Groups", 2, 2);
        assertCounts("SAK-52287 Other Group", 1, 1);
        assertCounts("SAK-52287 Own Group", 1, 1);
        assertCounts("SAK-52287 Empty Group", 0, 0);
    }

    @Test
    void taSeesOnlyTheirReleaseGroupsAndGradingCounts() {
        openAssessments(System.getProperty("samigo.groupAccess.ta", "ta1"));
        assertCounts("SAK-52287 Site", 1, 1);
        assertCounts("SAK-52287 Both Groups", 1, 1);
        assertCounts("SAK-52287 Own Group", 1, 1);
        assertCounts("SAK-52287 Empty Group", 0, 0);
        assertThat(publishedRow("SAK-52287 Other Group")).hasCount(0);

        // Rebuilding the list must preserve the same permission boundary.
        page.reload();
        assertCounts("SAK-52287 Site", 1, 1);
        assertCounts("SAK-52287 Both Groups", 1, 1);
        assertThat(publishedRow("SAK-52287 Other Group")).hasCount(0);
    }

    private void openAssessments(String username) {
        sakai.login(username);
        page.navigate(System.getProperty("samigo.groupAccess.siteUrl"));
        sakai.toolClick("Tests");
        assertThat(page.locator("#authorIndexForm\\:coreAssessments")).isVisible();
    }

    private Locator publishedRow(String title) {
        return page.locator("#authorIndexForm\\:coreAssessments tbody tr")
            .filter(new Locator.FilterOptions().setHasText(title))
            .filter(new Locator.FilterOptions().setHas(page.locator("span[class^='status_published_']")));
    }

    private void assertCounts(String title, int submitted, int inProgress) {
        Locator row = publishedRow(title);
        assertThat(row).hasCount(1);
        assertThat(row.locator("td.submitted")).hasText(Integer.toString(submitted));
        assertThat(row.locator("td.inProgress")).hasText(Integer.toString(inProgress));
    }
}
