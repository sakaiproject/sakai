package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class BecomeUserTest extends SakaiUiTestBase {

    @Test
    void administrationWorkspaceBecomeUser() {
        sakai.login("admin");
        gotoAdminSite();

        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("Administration Workspace", Pattern.CASE_INSENSITIVE))).first()).isVisible();

        sakai.toolClick("Become User");

        Locator becomeUserForm = page.locator("#su").first();
        if (!isVisible(becomeUserForm, 10_000)) {
            Locator becomeUserLink = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Become User$", Pattern.CASE_INSENSITIVE))).first();
            String href = becomeUserLink.getAttribute("href");
            if (href != null && !href.isBlank()) {
                page.navigate(href);
            }
        }

        assertThat(becomeUserForm).isVisible();
        Locator becomeUserInput = becomeUserForm.locator("input[type=\"text\"]").first();
        assertThat(becomeUserInput).isVisible();
        becomeUserInput.fill("instructor1");
        page.locator("#su\\:become").click();

        try {
            page.waitForURL(Pattern.compile("/portal(?:/|$)"), new Page.WaitForURLOptions().setTimeout(30_000));
        } catch (RuntimeException ignored) {
        }

        gotoAdminSite();
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Site Unavailable$", Pattern.CASE_INSENSITIVE))).first()).isVisible();

        page.locator(".sak-sysInd-account").click();
        page.locator("a#loginLink1").filter(new Locator.FilterOptions().setHasText(Pattern.compile("Return to", Pattern.CASE_INSENSITIVE))).click();

        gotoAdminSite();
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(Pattern.compile("^Administration Workspace$", Pattern.CASE_INSENSITIVE))).first()).isVisible();
    }

    private void gotoAdminSite() {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                page.navigate("/portal/site/!admin", new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
            } catch (RuntimeException ignored) {
            }
            if (!page.url().startsWith("chrome-error://")) {
                return;
            }
            try {
                page.navigate("/portal", new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
            } catch (RuntimeException ignored) {
            }
            page.waitForTimeout(250);
        }
        throw new IllegalStateException("Unable to load /portal/site/!admin without chrome-error navigation");
    }

    private boolean isVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
