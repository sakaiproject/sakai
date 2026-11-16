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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class TutorialTest extends SakaiUiTestBase {

    @Test
    void resumesRequestedTutorialOnToolUrl() {
        sakai.login("instructor1");
        sakai.toolClick("Calendar");
        assertTrue(page.url().contains("/portal/site/"));
        List<String> tutorialRequests = new ArrayList<>();
        page.onRequest(request -> {
            if (request.url().contains("/direct/tutorial/")) {
                tutorialRequests.add(request.url());
            }
        });

        // The Tutorial link sets this flag before navigating home. The portal may
        // redirect /portal to a canonical site/tool URL (for example, Dashboard).
        page.evaluate("sessionStorage.setItem('triggerTutorial', 'true')");
        Response tutorial = page.waitForResponse(
            response -> response.url().endsWith("/direct/tutorial/introToSakai_p1.json"),
            () -> page.reload());
        assertTrue(tutorial.ok());
        assertEquals("false", page.evaluate("sessionStorage.getItem('triggerTutorial')"));

        Locator popup = page.locator(".sakai-tutorial:visible");
        assertThat(popup).isVisible();
        assertThat(popup.locator(".qtip-title")).containsText("Welcome to");
        assertEquals(1, tutorialRequests.size());

        // The pending request is consumed even before the user dismisses the tour.
        page.reload();
        assertEquals("false", page.evaluate("sessionStorage.getItem('triggerTutorial')"));
        assertEquals(1, tutorialRequests.size());
        assertThat(popup).hasCount(0);

        // Explicitly request it again, then verify dismissal clears the request.
        page.evaluate("sessionStorage.setItem('triggerTutorial', 'true')");
        page.waitForResponse(
            response -> response.url().endsWith("/direct/tutorial/introToSakai_p1.json"),
            () -> page.reload());
        assertThat(popup).isVisible();
        popup.locator(".tut-close").click();
        assertThat(popup).hasCount(0);
        assertNull(page.evaluate("sessionStorage.getItem('triggerTutorial')"));
        assertEquals(2, tutorialRequests.size());

        page.reload();
        assertEquals(2, tutorialRequests.size());
        assertThat(popup).hasCount(0);

        sakai.toolClick("Overview");
        assertEquals(2, tutorialRequests.size());
        assertThat(popup).hasCount(0);
    }
}
