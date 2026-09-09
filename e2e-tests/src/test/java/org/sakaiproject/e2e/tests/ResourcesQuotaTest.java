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
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class ResourcesQuotaTest extends SakaiUiTestBase {

    @Test
    void adminCanSetSpecialQuotaInMegabytes() {
        sakai.login("instructor1");
        String sitePath = sakai.createCourse("instructor1", List.of("sakai\\.resources"));
        String siteUrl = java.net.URI.create(page.url()).resolve(sitePath).toString();
        context.clearCookies();
        sakai.login("admin");
        page.navigate(siteUrl);
        sakai.toolClick("Resources");

        openRootProperties();
        assertThat(page.locator("label[for^='hasQuota']")).containsText("MB");
        Locator enabled = page.locator("input[name^='hasQuota']");
        Locator quota = page.locator("input[name^='quota']");
        enabled.check();
        quota.fill("2048");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Check Quota").setExact(true)).click();
        assertThat(page.locator(".highlightPanel")).containsText("2 GB");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back").setExact(true)).click();
        openRootProperties();
        assertThat(enabled).isChecked();
        assertThat(quota).hasValue("2048");

        quota.fill("0.0009765625");
        saveAndReopen();
        assertThat(quota).hasValue("0.0009765625");

        quota.fill("0");
        saveAndReopen();
        assertThat(enabled).isChecked();
        assertThat(quota).hasValue("0");

        enabled.uncheck();
        saveAndReopen();
        assertThat(enabled).not().isChecked();
        assertThat(quota).isEmpty();
    }

    private void openRootProperties() {
        page.locator("button[title='Actions']").first().click();
        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Edit Details").setExact(true)).first().click();
        assertThat(page.locator("input[name^='quota']")).isVisible();
    }

    private void saveAndReopen() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update").setExact(true)).click();
        openRootProperties();
    }
}
