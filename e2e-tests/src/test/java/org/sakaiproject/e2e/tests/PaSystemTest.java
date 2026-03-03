package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class PaSystemTest extends SakaiUiTestBase {

    @Test
    void administrationWorkspacePaSystem() {
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
        messageInput.fill("This is a test");

        Locator active = page.locator("form input#active").first();
        assertThat(active).isVisible();
        active.click();

        Locator saveBanner = page.locator("form input[name=\"save\"], form input[value*=\"Save\"], form button:has-text(\"Save\")").first();
        assertThat(saveBanner).isVisible();
        saveBanner.click();

        Locator bannerAlerts = page.locator(".pasystem-banner-alerts").first();
        assertThat(bannerAlerts).containsText("This is a test");

        Locator editButton = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Edit$", Pattern.CASE_INSENSITIVE))).first();
        assertThat(editButton).isVisible();
        editButton.click();

        Locator editedMessageInput = page.locator("form input#message").first();
        assertThat(editedMessageInput).isVisible();
        editedMessageInput.fill("This is a test -- 2");

        Locator editedActive = page.locator("form input#active").first();
        assertThat(editedActive).isVisible();
        editedActive.click();

        Locator saveEditedBanner = page.locator("form input[name=\"save\"], form input[value*=\"Save\"], form button:has-text(\"Save\")").first();
        assertThat(saveEditedBanner).isVisible();
        saveEditedBanner.click();

        assertThat(bannerAlerts).not().containsText("This is a test");

        Locator deleteBanner = page.locator("a.pasystem-delete-btn").first();
        assertThat(deleteBanner).isVisible();
        deleteBanner.click();

        Locator confirmDelete = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Delete", Pattern.CASE_INSENSITIVE))).first();
        assertThat(confirmDelete).isVisible();
        confirmDelete.click();
    }
}
