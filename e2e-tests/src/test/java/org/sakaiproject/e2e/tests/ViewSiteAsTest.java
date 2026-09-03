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
import com.microsoft.playwright.options.SelectOption;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class ViewSiteAsTest extends SakaiUiTestBase {

    @Test
    void superuserCanViewSiteAsAnotherRole() {
        sakai.login("instructor1");
        String siteUrl = sakai.createProject("instructor1", List.of("sakai\\.announcements"));

        sakai.login("admin");
        // The test base URL can differ from the portal URL used in generated links.
        Locator usernameInput = page.locator("input[name=\"eid\"], #eid").first();
        if (usernameInput.isVisible()) {
            usernameInput.fill("admin");
            page.locator("input[name=\"pw\"], #pw").first().fill("admin");
            page.locator("#submit, button[type=\"submit\"], input[type=\"submit\"]").first().click();
            page.waitForLoadState();
        }

        URI portalUri = URI.create(page.url());
        String configuredSiteUrl = portalUri.resolve(URI.create(siteUrl).getRawPath()).toString();
        String adminSiteUrl = portalUri.resolve("/portal/site/!admin").toString();
        page.navigate(configuredSiteUrl);

        Locator roleSwitcher = page.locator("#roleSwitch");
        assertThat(roleSwitcher).isVisible();

        Locator roleSelect = roleSwitcher.locator("#roleSwitchSelect");
        if (roleSelect.count() > 0 && roleSelect.isVisible()) {
            roleSelect.selectOption(new SelectOption().setIndex(1));
        } else {
            roleSwitcher.locator("#roleSwitchAnchor").click();
        }
        page.waitForLoadState();

        assertThat(page.locator("#roleSwitchAnchor.Mrphs-roleSwitch__exit")).isVisible();

        page.navigate(adminSiteUrl);
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions()
                .setName(Pattern.compile("^Site Unavailable$", Pattern.CASE_INSENSITIVE))).first()).isVisible();

        page.navigate(configuredSiteUrl);
        page.locator("#roleSwitchAnchor.Mrphs-roleSwitch__exit").click();
        page.waitForLoadState();

        page.navigate(adminSiteUrl);
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions()
                .setName(Pattern.compile("^Administration Workspace$", Pattern.CASE_INSENSITIVE))).first()).isVisible();
    }
}
