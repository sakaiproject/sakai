package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ForumsTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String FORUM_TITLE = "Playwright Forum " + System.currentTimeMillis();
    private static final String TOPIC_TITLE = "Playwright Topic " + System.currentTimeMillis();

    @Test
    @Order(1)
    void createsSiteWithForums() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.forums"));
    }

    @Test
    @Order(2)
    void canCreateForumAndTopic() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Discussion");

        page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText("New"))
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        page.locator("form input[type=\"text\"]:visible").first().fill(FORUM_TITLE);
        page.locator("button[type=\"submit\"]:has-text(\"Save\"), button[type=\"submit\"]:has-text(\"Create\"), input[type=\"submit\"][value*=\"Save\"], input[type=\"submit\"][value*=\"Create\"], .act button:has-text(\"Save\"), .act button:has-text(\"Create\"), .act input[value*=\"Save\"], .act input[value*=\"Create\"]")
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(FORUM_TITLE)).first().click(new Locator.ClickOptions().setForce(true));

        page.locator(".navIntraTool a, .navIntraTool button")
            .filter(new Locator.FilterOptions().setHasText("New"))
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        page.locator("form input[type=\"text\"]:visible").first().fill(TOPIC_TITLE);
        page.locator("button[type=\"submit\"]:has-text(\"Save\"), button[type=\"submit\"]:has-text(\"Create\"), input[type=\"submit\"][value*=\"Save\"], input[type=\"submit\"][value*=\"Create\"], .act button:has-text(\"Save\"), .act button:has-text(\"Create\"), .act input[value*=\"Save\"], .act input[value*=\"Create\"]")
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        Locator topic = page.getByText(TOPIC_TITLE).first();
        boolean topicVisible = false;
        try {
            topic.waitFor(new Locator.WaitForOptions().setTimeout(5_000));
            topicVisible = true;
        } catch (RuntimeException ignored) {
            topicVisible = false;
        }

        if (!topicVisible) {
            page.navigate(sakaiUrl);
            sakai.toolClick("Discussion");
            Locator forumLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(FORUM_TITLE)).first();
            if (forumLink.count() > 0) {
                forumLink.click(new Locator.ClickOptions().setForce(true));
            }
        }

        assertThat(page.getByText(TOPIC_TITLE).first()).isVisible();
    }
}
