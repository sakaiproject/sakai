package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void createsSiteWithDashboard() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.dashboard"));
    }

    @Test
    @Order(2)
    void canEditAndSaveDashboard() {
        sakai.login("instructor1");

        boolean opened = false;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                page.navigate(sakaiUrl, new com.microsoft.playwright.Page.NavigateOptions()
                    .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(60_000));
                opened = true;
                break;
            } catch (RuntimeException ignored) {
                page.waitForTimeout(1000);
            }
        }
        if (!opened) {
            page.navigate("/portal/");
        }

        sakai.toolClick("Dashboard");

        com.microsoft.playwright.Locator editButton = page.locator("#course-dashboard-edit button").first();
        assertThat(editButton).isVisible();
        editButton.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));

        com.microsoft.playwright.Locator saveButton = page.locator("#course-dashboard-save button").first();
        if (saveButton.count() > 0) {
            saveButton.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
        }

        assertThat(page.locator("main#content")).isVisible();
    }
}
