package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommonsTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void canCreateANewCourse() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.commons"));
    }

    @Test
    @Order(2)
    void canCreateCommonsPostAsStudent() {
        sakai.login("student0011");
        page.navigate(sakaiUrl);
        sakai.toolClick("Commons");

        page.locator("#commons-post-creator-editor").fill("This is a student test post");
        page.locator("#commons-editor-post-button").click();

        assertThat(page.locator("body")).containsText("This is a student test post");
    }

    @Test
    @Order(3)
    void canCreateCommonsPostAsInstructor() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Commons");

        assertThat(page.locator("body")).containsText("This is a student test post");

        page.locator("#commons-post-creator-editor").fill("This is a test post");
        page.locator("#commons-editor-post-button").click();

        assertThat(page.locator("body")).containsText("This is a test post");
    }
}
