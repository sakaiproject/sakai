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
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class PortalNavigationTest extends SakaiUiTestBase {

    @BeforeEach
    void loginAndOpenPortalInNarrowView() {
        sakai.login("instructor1");
        page.setViewportSize(767, 600);
        page.navigate("/portal");
    }

    @Test
    void mobileSiteNavigationScrollsWithMouseWheel() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
            .setName(Pattern.compile("site navigation", Pattern.CASE_INSENSITIVE))).click();

        Locator sidebar = page.locator("#portal-nav-sidebar");
        Locator navigation = sidebar.locator("#toolMenu");
        assertThat(sidebar).isVisible();

        Boolean navigationFitsAboveFooter = (Boolean) sidebar.evaluate("sidebar => {"
            + "const navigation = sidebar.querySelector('#toolMenu');"
            + "const footer = sidebar.querySelector('.sticky-footer');"
            + "return navigation.getBoundingClientRect().bottom <= footer.getBoundingClientRect().top;"
            + "}");
        assertTrue(navigationFitsAboveFooter);

        Boolean navigationOverflows = (Boolean) navigation.evaluate(
            "navigation => navigation.scrollHeight > navigation.clientHeight");
        assertTrue(navigationOverflows);

        navigation.hover();
        page.mouse().wheel(0, 1000);

        Number scrollTop = (Number) navigation.evaluate("navigation => navigation.scrollTop");
        assertTrue(scrollTop.doubleValue() > 0);
    }
}
