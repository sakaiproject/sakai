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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Media;
import java.util.regex.Pattern;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
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
        try {
            saveBanner.click();
            Locator bannerAlerts = page.locator(".pasystem-banner-alerts").first();
            assertThat(bannerAlerts).containsText(message);

            Locator banner = bannerAlerts.locator(".pasystem-banner-alert").filter(
                new Locator.FilterOptions().setHasText(message));
            Locator showAlerts = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Show dismissed system alerts").setExact(true));
            assertThat(banner).isVisible();
            assertThat(banner.locator(".bi-exclamation-triangle")).isVisible();
            assertThat(banner.locator(".bi-x-lg")).isVisible();
            openAccountMenu();
            assertThat(showAlerts).isHidden();
            closeAccountMenu();

            Response dismissed = page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/bannerAcknowledge") && response.ok(),
                () -> banner.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Dismiss Alert")).click());
            assertTrue(dismissed.text().contains("SUCCESS"));
            assertThat(banner).isHidden();
            openAccountMenu();
            assertThat(showAlerts).isVisible();
            assertThat(page.locator("#sakai-account-panel .pasystem-banner-alert-toggle")).isVisible();
            assertThat(page.locator("#sakai-system-indicators .pasystem-banner-alert-toggle")).hasCount(0);
            assertThat(page.locator(".portal-pasystem .pasystem-banner-alert-toggle")).hasCount(0);
            page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
            assertThat(showAlerts).isHidden();
            page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));

            page.reload();
            assertThat(banner).isHidden();
            openAccountMenu();
            assertThat(showAlerts).isVisible();

            Response restored = page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/clearBannerAcknowledgements") && response.ok(),
                () -> showAlerts.press("Enter"));
            assertTrue(restored.text().contains("SUCCESS"));
            assertThat(page.locator("#sakai-account-panel")).isHidden();
            assertThat(page.locator(".pasystem-banner-alerts .pasystem-banner-alert-close").first()).isFocused();
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
            openAccountMenu();
            assertThat(showAlerts).isVisible();
            assertThat(page.locator("#sakai-account-panel .pasystem-banner-alert-toggle")).isVisible();
            assertThat(page.locator("#sakai-system-indicators .pasystem-banner-alert-toggle")).hasCount(0);
            page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/clearBannerAcknowledgements") && response.ok(),
                showAlerts::click);
            assertThat(page.locator("#sakai-account-panel")).isHidden();
            assertThat(page.locator(".pasystem-banner-alerts .pasystem-banner-alert-close").first()).isFocused();
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

            assertThat(bannerRow).containsText(message + " edited");
            assertThat(bannerRow.locator("td").nth(2)).hasText("false");
            assertThat(bannerAlerts).not().containsText(message);
        } finally {
            deleteBanner(message);
        }
    }

    @Test
    void restoreWaitsForPendingBannerDismissal() {
        String firstMessage = "SAK-52958 first " + UUID.randomUUID();
        String secondMessage = "SAK-52958 second " + UUID.randomUUID();
        sakai.login("admin");
        sakai.gotoPath("/portal/site/!admin");
        sakai.toolClick("PA System");

        try {
            createMediumBanner(firstMessage);
            createMediumBanner(secondMessage);
            Locator firstBanner = page.locator(".pasystem-banner-alert").filter(
                new Locator.FilterOptions().setHasText(firstMessage));
            Locator secondBanner = page.locator(".pasystem-banner-alert").filter(
                new Locator.FilterOptions().setHasText(secondMessage));
            Locator showAlerts = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Show dismissed system alerts").setExact(true));

            page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/bannerAcknowledge") && response.ok(),
                () -> firstBanner.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Dismiss Alert")).click());
            assertThat(firstBanner).isHidden();
            openAccountMenu();
            assertThat(showAlerts).isVisible();
            closeAccountMenu();

            // Hold the second dismissal before forwarding it to the real server.
            List<Route> pendingDismissals = new ArrayList<>();
            List<Request> restoreRequests = new ArrayList<>();
            page.route("**/direct/pasystem/bannerAcknowledge", pendingDismissals::add);
            page.onRequest(request -> {
                if (request.url().contains("/direct/pasystem/clearBannerAcknowledgements")) {
                    restoreRequests.add(request);
                }
            });
            Locator secondClose = secondBanner.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Dismiss Alert"));
            secondClose.click();
            page.waitForCondition(() -> pendingDismissals.size() == 1);
            openAccountMenu();
            showAlerts.click();
            assertThat(showAlerts).isDisabled();
            assertEquals(0, restoreRequests.size(), "Restore must not clear an outstanding dismissal");
            // A queued banner click must not start another dismissal during restoration.
            secondClose.dispatchEvent("click");
            assertEquals(1, pendingDismissals.size(), "A restore must prevent new dismissals");

            Response restored = page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/clearBannerAcknowledgements") && response.ok(),
                () -> pendingDismissals.get(0).resume());
            assertTrue(restored.text().contains("SUCCESS"));
            assertThat(page.locator("#sakai-account-panel")).isHidden();
            assertThat(firstBanner).isVisible();
            assertThat(secondBanner).isVisible();
            assertThat(showAlerts).isHidden();

            page.reload();
            assertThat(firstBanner).isVisible();
            assertThat(secondBanner).isVisible();
        } finally {
            try {
                deleteBanner(firstMessage);
            } finally {
                deleteBanner(secondMessage);
            }
        }
    }

    @Test
    void dismissalSurvivesBannerRefresh() {
        String message = "SAK-52958 refresh " + UUID.randomUUID();
        String timezoneMessage = "SAK-52958 timezone " + UUID.randomUUID();
        sakai.login("admin");
        sakai.gotoPath("/portal/site/!admin");
        sakai.toolClick("PA System");

        try {
            createMediumBanner(message);
            Locator banner = page.locator(".pasystem-banner-alert").filter(
                new Locator.FilterOptions().setHasText(message));
            Locator showAlerts = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Show dismissed system alerts").setExact(true));
            List<Route> pendingDismissals = new ArrayList<>();
            page.route("**/direct/pasystem/bannerAcknowledge", pendingDismissals::add);
            banner.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Dismiss Alert")).click();
            page.waitForCondition(() -> pendingDismissals.size() == 1);

            // The asynchronous timezone check uses this public API to refresh the banners.
            page.evaluate("message => pasystem.banners.addBannerAlert('tz', message, true, 'timezone')", timezoneMessage);
            Response dismissed = page.waitForResponse(
                response -> response.url().contains("/direct/pasystem/bannerAcknowledge") && response.ok(),
                () -> pendingDismissals.get(0).resume());
            assertTrue(dismissed.text().contains("SUCCESS"));
            openAccountMenu();
            assertThat(showAlerts).isVisible();
            assertThat(banner).isHidden();
            Locator timezoneBanner = page.locator(".pasystem-banner-timezone").filter(
                new Locator.FilterOptions().setHasText(timezoneMessage));
            assertThat(timezoneBanner.locator(".bi-info-circle")).isVisible();

            page.reload();
            assertThat(banner).isHidden();
        } finally {
            deleteBanner(message);
        }
    }

    @Test
    void popupPreviewUsesStandardButtons() {
        sakai.login("admin");
        sakai.gotoPath("/portal/site/!admin");
        sakai.toolClick("PA System");

        // Preview uses the production popup renderer without creating a campaign.
        page.locator("#popup-container-content").evaluate(
            "element => element.textContent = '<header class=\"popup-container-header\">Preview</header><p>Preview content</p>'");
        page.evaluate("() => new PASystemPopup('preview', 'preview')");
        Locator popup = page.locator("#pasystem-popup-wrapper");
        assertThat(popup.locator(".popup-container-header")).hasText("Preview");
        Locator acknowledge = popup.locator("button#popup-acknowledged-button.btn-secondary");
        Locator remindLater = popup.locator("button#popup-later-button.btn-primary");
        assertThat(acknowledge).isVisible();
        assertThat(remindLater).isVisible();
        acknowledge.click();
        assertThat(popup).hasCount(0);

        page.evaluate("() => new PASystemPopup('preview', 'preview')");
        assertThat(remindLater).isVisible();
        remindLater.click();
        assertThat(popup).hasCount(0);
    }

    private void openAccountMenu() {
        page.locator("[data-bs-target=\"#sakai-account-panel\"]").click();
        assertThat(page.locator("#sakai-account-panel")).hasClass(Pattern.compile(".*\\bshow\\b.*"));
    }

    private void closeAccountMenu() {
        page.locator("#sakai-account-panel [data-bs-dismiss=offcanvas]").click();
        assertThat(page.locator("#sakai-account-panel")).isHidden();
    }

    private void createMediumBanner(String message) {
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Create Banner").setExact(true)).click();
        page.locator("form input#message").fill(message);
        page.locator("form select#type").selectOption("medium");
        page.locator("form input#active").check();
        page.locator("form input[name=\"save\"]").click();
    }

    private void deleteBanner(String message) {
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
