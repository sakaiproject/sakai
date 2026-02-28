package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PollTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String POLL_TITLE = "Playwright Poll " + System.currentTimeMillis();

    @Test
    @Order(1)
    void createsSiteWithPolls() {
        if (sakaiUrl != null) {
            return;
        }
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.poll"));
    }

    @Test
    @Order(2)
    void canCreatePollWithTwoOptions() {
        createsSiteWithPolls();
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Poll");

        page.locator(".navIntraTool a, .navIntraTool button, ul.nav a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Add|New", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

        page.locator("form:visible input[type=\"text\"]").first().fill(POLL_TITLE);

        LocalDateTime now = LocalDateTime.now();
        String openDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String closeDateTime = now.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        page.locator("input[name=\"openDate\"]").fill(openDateTime);
        page.locator("input[name=\"closeDate\"]").fill(closeDateTime);

        page.locator("form:visible").first().locator("button[type=\"submit\"]:has-text(\"Save\"), button[type=\"submit\"]:has-text(\"Add\"), button[type=\"submit\"]:has-text(\"Create\"), button[type=\"submit\"]:has-text(\"Continue\"), input[type=\"submit\"][value*=\"Save\"], input[type=\"submit\"][value*=\"Add\"], input[type=\"submit\"][value*=\"Create\"], input[type=\"submit\"][value*=\"Continue\"], .act button:has-text(\"Save\"), .act button:has-text(\"Add\"), .act button:has-text(\"Create\"), .act button:has-text(\"Continue\"), .act input[value*=\"Save\"], .act input[value*=\"Add\"], .act input[value*=\"Create\"], .act input[value*=\"Continue\"]")
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("textarea")).isVisible();

        page.locator("textarea").fill("Yes");
        page.locator("button:has-text(\"Save\"), input[type=\"submit\"][value*=\"Save\"]").first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator(".sak-banner-success")).isVisible();

        Locator addOptionButton = addOptionControl();
        if (!isVisible(addOptionButton, 5_000)) {
            Locator pollLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Pattern.compile("^" + Pattern.quote(POLL_TITLE) + "$"))).first();
            if (isVisible(pollLink, 5_000)) {
                pollLink.click(new Locator.ClickOptions().setForce(true));
            }
            addOptionButton = addOptionControl();
        }

        assertThat(addOptionButton).isVisible();
        addOptionButton.click(new Locator.ClickOptions().setForce(true));

        page.locator("textarea").fill("No");
        page.locator("button:has-text(\"Save\"), input[type=\"submit\"][value*=\"Save\"]").first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator(".sak-banner-success")).isVisible();
        page.locator("button:has-text(\"Save\"), input[type=\"submit\"][value*=\"Save\"]").first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator(".sak-banner-success")).containsText("Poll saved successfully");
        assertThat(page.locator("body")).containsText(POLL_TITLE);
    }

    private Locator addOptionControl() {
        return page.locator("input[type=\"button\"][value=\"Add option\"], input[type=\"button\"][value=\"Add Option\"], input[type=\"button\"][value*=\"Add option\"], input[type=\"button\"][value*=\"Add Option\"], button:has-text(\"Add option\"), button:has-text(\"Add Option\"), a:has-text(\"Add option\"), a:has-text(\"Add Option\")").first();
    }

    private boolean isVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
