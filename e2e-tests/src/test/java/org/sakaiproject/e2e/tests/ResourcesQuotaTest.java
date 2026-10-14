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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class ResourcesQuotaTest extends SakaiUiTestBase {

    @Test
    void adminCanSetSpecialQuotaInGigabytes() {
        sakai.login("instructor1");
        String sitePath = sakai.createCourse("instructor1", List.of("sakai\\.resources"));
        String siteUrl = java.net.URI.create(page.url()).resolve(sitePath).toString();
        context.clearCookies();
        sakai.login("admin");
        page.navigate(siteUrl);
        sakai.toolClick("Resources");

        openRootProperties();
        assertThat(page.locator("label[for^='hasQuota']")).containsText("GB");
        Locator enabled = page.locator("input[name^='hasQuota']");
        Locator quota = page.locator("input[name^='quota']");
        assertThat(enabled).not().isChecked();
        assertThat(quota).isDisabled();
        assertThat(quota).hasAttribute("type", "number");
        assertThat(quota).hasAttribute("min", "0");
        assertThat(quota).hasAttribute("step", "any");
        page.locator("label[for^='hasQuota']").click();
        assertThat(enabled).isChecked();
        assertThat(quota).isEnabled();
        quota.fill("2");
        enabled.uncheck();
        assertThat(quota).isVisible();
        assertThat(quota).isDisabled();
        enabled.check();
        assertThat(quota).isEnabled();
        assertThat(quota).hasValue("2");
        Locator allowHtml = page.getByRole(AriaRole.CHECKBOX,
                new Page.GetByRoleOptions().setName("Allow HTML files").setExact(true));
        assertThat(allowHtml).isVisible();
        assertThat(allowHtml).hasAttribute("aria-describedby", "allow-html-help_0");
        assertThat(page.locator("#allow-html-help_0")).containsText("User-uploaded HTML");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Check Quota").setExact(true)).click();
        assertThat(page.locator(".highlightPanel")).containsText("2 GB");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back").setExact(true)).click();
        openRootProperties();
        assertThat(enabled).isChecked();
        assertThat(quota).hasValue("2");

        for (String invalidQuota : List.of("", "-1", "-0.000000001", "8796093022208")) {
            quota.fill(invalidQuota);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update").setExact(true)).click();
            assertThat(page.locator("#resourceAlert")).containsText("Enter a valid quota in GB");
            assertThat(quota).hasValue(invalidQuota);
            assertThat(page.locator("#quota-injected")).hasCount(0);
            quota.fill("2");
            saveAndReopen();
            assertThat(quota).hasValue("2");
        }

        // Bypass the numeric control to verify server validation and HTML escaping.
        for (String invalidQuota : List.of("  not-a-number  ",
                "\"/><span id=\"quota-injected\">&amp;'</span>")) {
            quota.evaluate("input => input.type = 'text'");
            quota.fill(invalidQuota);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update").setExact(true)).click();
            assertThat(page.locator("#resourceAlert")).containsText("Enter a valid quota in GB");
            assertThat(quota).isEmpty();
            assertThat(quota).hasAttribute("value", invalidQuota);
            assertThat(page.locator("#quota-injected")).hasCount(0);
            quota.fill("2");
            saveAndReopen();
            assertThat(quota).hasValue("2");
        }

        quota.fill("10.555555");
        assertTrue((Boolean) quota.evaluate("input => input.checkValidity()"));
        saveAndReopen();
        assertThat(quota).hasValue("10.56");

        quota.fill("3.555");
        saveAndReopen();
        assertThat(quota).hasValue("3.56");
        saveAndReopen();
        assertThat(quota).hasValue("3.56");

        quota.fill("3.554");
        saveAndReopen();
        assertThat(quota).hasValue("3.56");

        quota.fill("1.00000001");
        saveAndReopen();
        assertThat(quota).hasValue("1.01");

        quota.fill("1e1");
        saveAndReopen();
        assertThat(quota).hasValue("10");

        // Round before saving, and keep the rounded quota stable across subsequent saves.
        quota.fill("0.000000001");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update").setExact(true)).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Check Quota").setExact(true)).click();
        assertThat(page.locator(".highlightPanel")).containsText("10.2 MB");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back").setExact(true)).click();
        openRootProperties();
        assertThat(quota).hasValue("0.01");
        saveAndReopen();
        assertThat(quota).hasValue("0.01");

        quota.fill("0.01");
        saveAndReopen();
        assertThat(quota).hasValue("0.01");

        quota.fill("1.5");
        saveAndReopen();
        assertThat(quota).hasValue("1.5");

        // Small positive entries round up to the minimum nonzero quota.
        quota.fill("0.00000095367431640625");
        saveAndReopen();
        assertThat(quota).hasValue("0.01");

        quota.fill("0");
        saveAndReopen();
        assertThat(enabled).isChecked();
        assertThat(quota).hasValue("0");

        quota.fill("");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update").setExact(true)).click();
        assertThat(page.locator("#resourceAlert")).containsText("Enter a valid quota in GB");
        enabled.uncheck();
        assertThat(quota).isDisabled();
        saveAndReopen();
        assertThat(enabled).not().isChecked();
        assertThat(quota).isEmpty();
        assertThat(quota).isDisabled();
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
