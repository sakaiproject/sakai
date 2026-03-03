package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeedbackTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void createsASiteWithFeedback() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", java.util.List.of("sakai\\.feedback"));
    }

    @Test
    @Order(2)
    void opensFeedbackSmoke() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Contact Us");
        assertThat(page.locator(".portletBody").first()).isVisible();
    }
}
