package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AccessibilityTest extends SakaiUiTestBase {

    @BeforeEach
    void loginAndOpenPortal() {
        sakai.login("instructor1");
        page.navigate("/portal");
    }

    @Test
    void canJumpToNewContentViaKeyboardOnly() {
        Locator jumpLink = page.locator("a[href=\"#tocontent\"][title*=\"jump to content\" i], a[href=\"#tocontent\"]").first();

        boolean focusedJumpLink = false;
        for (int attempt = 0; attempt < 4; attempt++) {
            page.keyboard().press("Tab");
            Boolean focused = (Boolean) page.evaluate("() => {"
                + "const focused = document.activeElement;"
                + "if (!(focused instanceof HTMLAnchorElement)) return false;"
                + "const title = focused.getAttribute('title') || '';"
                + "return focused.getAttribute('href') === '#tocontent' || /jump to content/i.test(title);"
                + "}");
            if (Boolean.TRUE.equals(focused)) {
                focusedJumpLink = true;
                break;
            }
        }

        assertTrue(focusedJumpLink);
        assertThat(jumpLink).containsText(Pattern.compile("jump to content", Pattern.CASE_INSENSITIVE));
    }

    @Test
    void viewAllSitesPanelOpensAndCloses() {
        page.locator("#sakai-system-indicators button[title=\"View All Sites\"]").click();
        Locator sidebar = page.locator("#select-site-sidebar");
        assertThat(sidebar).containsText("All Sites");

        Locator closeButton = sidebar.locator("button.btn-close, button[aria-label=\"Close\"]").first();
        if (closeButton.count() > 0 && closeButton.isVisible()) {
            closeButton.click();
        } else {
            page.keyboard().press("Escape");
        }

        if (sidebar.isVisible()) {
            page.keyboard().press("Escape");
        }

        sidebar.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(10_000));
        assertThat(sidebar).isHidden();
    }
}
