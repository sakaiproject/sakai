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
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class AccessibilityTest extends SakaiUiTestBase {

    @BeforeEach
    void loginAndOpenPortal() {
        sakai.login("instructor2");
        page.navigate("/portal");
    }

    @Test
    void canJumpToNewContentViaKeyboardOnly() {
        sakai.waitForPortalFocus();
        Locator jumpLink = page.locator("a[href=\"#tocontent\"][title*=\"jump to content\" i], a[href=\"#tocontent\"]").first();

        Locator skipLinks = page.locator("#skipNav a");
        page.keyboard().press("Tab");
        assertThat(page.locator(":focus")).hasAttribute("href", skipLinks.first().getAttribute("href"));
        if (!"#tocontent".equals(skipLinks.first().getAttribute("href"))) {
            page.keyboard().press("Tab");
        }
        assertThat(page.locator(":focus")).hasAttribute("href", "#tocontent");
        assertThat(jumpLink).containsText(Pattern.compile("jump to content", Pattern.CASE_INSENSITIVE));
        page.keyboard().press("Enter");
        assertThat(page.locator("#tocontent")).isFocused();
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
