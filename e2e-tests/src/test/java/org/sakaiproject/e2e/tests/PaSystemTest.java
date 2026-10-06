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
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class PaSystemTest extends SakaiUiTestBase {

    @Test
    void administrationWorkspacePaSystem() {
        String message = "SAK-52958 " + UUID.randomUUID();
        sakai.login("admin");

        sakai.gotoPath("/portal/site/!admin");
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Administration Workspace$", Pattern.CASE_INSENSITIVE))).first()).isVisible();

        sakai.toolClick("PA System");

        Locator createBannerButton = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Create Banner$", Pattern.CASE_INSENSITIVE))).first();
        boolean openedCreateBanner = false;
        try {
            assertThat(createBannerButton).isVisible();
            createBannerButton.click();
            openedCreateBanner = true;
        } catch (AssertionError ignored) {
            // Fall through to link selector used in alternate DOM structures.
        }

        if (!openedCreateBanner) {
            Locator createBannerLink = page.locator("a[href*=\"/banners/new\"]").first();
            assertThat(createBannerLink).isVisible();
            createBannerLink.click();
        }

        Locator messageInput = page.locator("form input#message").first();
        assertThat(messageInput).isVisible();
        messageInput.fill(message);

        page.locator("form select#type").selectOption("medium");

        Locator active = page.locator("form input#active").first();
        assertThat(active).isVisible();
        active.click();

        Locator saveBanner = page.locator("form input[name=\"save\"], form input[value*=\"Save\"], form button:has-text(\"Save\")").first();
        assertThat(saveBanner).isVisible();
        saveBanner.click();

        try {
            Locator bannerAlerts = page.locator(".pasystem-banner-alerts").first();
            assertThat(bannerAlerts).containsText(message);

            Locator banner = bannerAlerts.locator(".pasystem-banner-alert").filter(
                new Locator.FilterOptions().setHasText(message));
            Locator showAlerts = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Show System Alerts").setExact(true));
            assertThat(banner).isVisible();
            assertThat(showAlerts).isHidden();

            Response dismissed = page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/bannerAcknowledge") && response.ok(),
                () -> banner.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Dismiss Alert")).click());
            assertTrue(dismissed.text().contains("SUCCESS"));
            assertThat(banner).isHidden();
            assertThat(showAlerts).isVisible();

            page.reload();
            assertThat(banner).isHidden();
            assertThat(showAlerts).isVisible();

            Response restored = page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/clearBannerAcknowledgements") && response.ok(),
                () -> showAlerts.press("Enter"));
            assertTrue(restored.text().contains("SUCCESS"));
            assertThat(banner).isVisible();
            assertThat(showAlerts).isHidden();

            page.reload();
            assertThat(banner).isVisible();
            assertThat(showAlerts).isHidden();

            page.setViewportSize(390, 844);
            page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/bannerAcknowledge") && response.ok(),
                () -> banner.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Dismiss Alert")).click());
            assertThat(banner).isHidden();
            assertThat(showAlerts).isVisible();
            page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/clearBannerAcknowledgements") && response.ok(),
                showAlerts::click);
            assertThat(banner).isVisible();
            assertThat(showAlerts).isHidden();
            page.setViewportSize(1280, 720);

            Locator bannerRow = page.locator("tbody tr").filter(new Locator.FilterOptions().setHasText(message));
            Locator editButton = bannerRow.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Edit$", Pattern.CASE_INSENSITIVE))).first();
            assertThat(editButton).isVisible();
            editButton.click();

            Locator editedMessageInput = page.locator("form input#message").first();
            assertThat(editedMessageInput).isVisible();
            editedMessageInput.fill(message + " edited");

            Locator editedActive = page.locator("form input#active").first();
            assertThat(editedActive).isVisible();
            editedActive.click();

            Locator saveEditedBanner = page.locator("form input[name=\"save\"], form input[value*=\"Save\"], form button:has-text(\"Save\")").first();
            assertThat(saveEditedBanner).isVisible();
            saveEditedBanner.click();

            assertThat(bannerAlerts).not().containsText(message);
        } finally {
            sakai.gotoPath("/portal/site/!admin");
            sakai.toolClick("PA System");
            Locator bannerRow = page.locator("tbody tr").filter(new Locator.FilterOptions().setHasText(message));
            while (bannerRow.count() > 0) {
                int count = bannerRow.count();
                bannerRow.first().locator("a.pasystem-delete-btn").click();
                page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Delete Banner").setExact(true)).click();
                assertThat(bannerRow).hasCount(count - 1);
            }
        }
    }
}
