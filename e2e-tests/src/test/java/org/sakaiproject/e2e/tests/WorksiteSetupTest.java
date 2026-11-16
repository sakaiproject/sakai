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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class WorksiteSetupTest extends SakaiUiTestBase {

    @Test
    void canContinueSiteCreationAfterNavigatingAwayAndReturning() {
        returnToSiteCreation();

        page.locator("#project").check();
        page.locator("#submitBuildOwn").click();

        assertThat(page.locator("#title")).isVisible();
        assertThat(page.locator("#typeform")).not().isVisible();
    }

    @Test
    void canCancelSiteCreationAfterNavigatingAwayAndReturning() {
        returnToSiteCreation();

        page.locator("#cancelCreate").click();

        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Create New Site").setExact(true))).isVisible();
        assertThat(page.locator("#typeform")).not().isVisible();
        sakai.toolClick("Overview");
        sakai.toolClick("Worksite Setup");
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Create New Site").setExact(true))).isVisible();
    }

    private void returnToSiteCreation() {
        sakai.login("instructor1");
        sakai.gotoPath("/portal/site/~instructor1");
        sakai.toolClick("Worksite Setup");
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Create New Site").setExact(true)).click();
        assertThat(page.locator("#typeform")).isVisible();

        sakai.toolClick("Overview");
        sakai.toolClick("Worksite Setup");
        assertThat(page.locator("#typeform")).isVisible();
    }
}
