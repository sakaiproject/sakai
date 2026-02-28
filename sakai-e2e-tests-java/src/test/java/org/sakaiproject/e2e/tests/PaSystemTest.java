package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class PaSystemTest extends SakaiUiTestBase {

    @Test
    void administrationWorkspacePaSystem() {
        sakai.login("admin");

        page.navigate("/portal/site/!admin");
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Administration Workspace$", Pattern.CASE_INSENSITIVE))).first()).isVisible();

        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^PA System$", Pattern.CASE_INSENSITIVE))).first()
            .click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Create Banner$", Pattern.CASE_INSENSITIVE))).first()
            .click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));

        page.locator("form input#message").fill("This is a test");
        page.locator("#active").click();
        page.locator("input[value*=\"Save\"], button:has-text(\"Save\")").first().click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));

        assertThat(page.locator(".pasystem-banner-alerts")).containsText("This is a test");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Edit")).click();
        page.locator("form input#message").fill("This is a test -- 2");
        page.locator("#active").click();
        page.locator("input[value*=\"Save\"], button:has-text(\"Save\")").first().click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));

        assertThat(page.locator(".pasystem-banner-alerts")).not().containsText("This is a test");

        page.locator("a.pasystem-delete-btn").click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete")).click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
    }
}
