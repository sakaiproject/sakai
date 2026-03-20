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
