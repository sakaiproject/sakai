package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        Locator notificationsButton = page.locator(".portal-notifications-button").first();
        assertThat(notificationsButton).isVisible();

        Response response = page.waitForResponse(
            candidate -> candidate.url().contains("/api/users/me/notifications")
                && "GET".equals(candidate.request().method()),
            () -> notificationsButton.click(new Locator.ClickOptions().setForce(true))
        );

        assertTrue(response.ok());
        assertTrue(notificationRequests.size() >= 1);
    }
}
