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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class NotificationsTest extends SakaiUiTestBase {

    @Test
    void onlyLoadsNotificationsWhenPanelIsOpened() {

        List<String> notificationRequests = new CopyOnWriteArrayList<>();
        page.onRequest(request -> {
            String url = request.url();
            if (url.contains("/api/users/me/notifications")
                    && !url.contains("/clear")
                    && !url.contains("/markViewed")
                    && !url.contains("/test")) {
                notificationRequests.add(url);
            }
        });

        sakai.login("instructor1");
        page.waitForLoadState();
        page.waitForTimeout(1000);

        assertEquals(0, notificationRequests.size());

        Locator notificationsPanel = page.locator("#sakai-notifications-panel").first();
        assumeTrue(notificationsPanel.count() > 0, "Notifications panel is disabled in this environment");

        Response response = page.waitForResponse(
            candidate -> candidate.url().contains("/api/users/me/notifications")
                && "GET".equals(candidate.request().method()),
            () -> {
                Locator notificationsButton = page.locator("button[aria-controls=\"sakai-notifications-panel\"]:visible").first();
                if (notificationsButton.count() > 0) {
                    notificationsButton.click(new Locator.ClickOptions().setForce(true));
                    return;
                }

                page.evaluate(
                    "() => bootstrap.Offcanvas.getOrCreateInstance(document.getElementById('sakai-notifications-panel')).show()"
                );
            }
        );

        assertTrue(response.ok());
        assertTrue(notificationRequests.size() >= 1);
    }
}
