package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private static final String LIMITS_POLL_TITLE = "Playwright Poll Limits " + System.currentTimeMillis();
    private static final String DEFAULT_DATES_POLL_TITLE = "Playwright Default Dates Poll " + System.currentTimeMillis();
    private static boolean pollWithTwoOptionsCreated;

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
        ensurePollWithTwoOptionsExists();
    }

    private void ensurePollWithTwoOptionsExists() {
        if (pollWithTwoOptionsCreated) {
            return;
        }

        createsSiteWithPolls();
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Poll");

        page.locator(".navIntraTool a, .navIntraTool button, ul.nav a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Add|New", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

        page.locator("form:visible input[type=\"text\"]").first().fill(POLL_TITLE);

        LocalDateTime now = LocalDateTime.now();
        // Keep poll open regardless of browser/server timezone differences in CI.
        String openDateTime = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
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
        pollWithTwoOptionsCreated = true;
    }

    @Test
    @Order(3)
    void cannotSavePollWhenLimitsExceedAvailableOptions() {
        createsSiteWithPolls();
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Poll");

        page.locator(".navIntraTool a, .navIntraTool button, ul.nav a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Add|New", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

        page.locator("form:visible input[type=\"text\"]").first().fill(LIMITS_POLL_TITLE);
        page.locator("#poll-details").fill("Poll limits regression test");

        LocalDateTime now = LocalDateTime.now();
        String openDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String closeDateTime = now.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        page.locator("input[name=\"openDate\"]").fill(openDateTime);
        page.locator("input[name=\"closeDate\"]").fill(closeDateTime);

        page.locator("input[name=\"minOptions\"]").fill("6");
        page.locator("input[name=\"maxOptions\"]").fill("6");

        page.locator("form:visible").first().locator("button[type=\"submit\"]:has-text(\"Save\"), button[type=\"submit\"]:has-text(\"Add\"), button[type=\"submit\"]:has-text(\"Create\"), button[type=\"submit\"]:has-text(\"Continue\"), input[type=\"submit\"][value*=\"Save\"], input[type=\"submit\"][value*=\"Add\"], input[type=\"submit\"][value*=\"Create\"], input[type=\"submit\"][value*=\"Continue\"], .act button:has-text(\"Save\"), .act button:has-text(\"Add\"), .act button:has-text(\"Create\"), .act button:has-text(\"Continue\"), .act input[value*=\"Save\"], .act input[value*=\"Add\"], .act input[value*=\"Create\"], .act input[value*=\"Continue\"]")
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("textarea")).isVisible();

        page.locator("textarea").fill("Option one");
        page.locator("button:has-text(\"Save\"), input[type=\"submit\"][value*=\"Save\"]").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator(".sak-banner-success")).isVisible();

        Locator addOptionButton = addOptionControl();
        if (!isVisible(addOptionButton, 5_000)) {
            Locator pollLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(Pattern.compile("^" + Pattern.quote(LIMITS_POLL_TITLE) + "$"))).first();
            if (isVisible(pollLink, 5_000)) {
                pollLink.click(new Locator.ClickOptions().setForce(true));
            }
            addOptionButton = addOptionControl();
        }

        assertThat(addOptionButton).isVisible();
        addOptionButton.click(new Locator.ClickOptions().setForce(true));

        page.locator("textarea").fill("Option two");
        page.locator("button:has-text(\"Save\"), input[type=\"submit\"][value*=\"Save\"]").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.locator(".sak-banner-success")).isVisible();

        page.locator("button:has-text(\"Save\"), input[type=\"submit\"][value*=\"Save\"]").first().click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#max-options")).hasClass(Pattern.compile(".*is-invalid.*"));
        assertThat(page.locator("body")).containsText(Pattern.compile("add more answer options", Pattern.CASE_INSENSITIVE));
    }

    @Test
    @Order(4)
    void newPollFormPrefillsDatesAndAllowsSave() {
        createsSiteWithPolls();
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Poll");

        page.locator(".navIntraTool a, .navIntraTool button, ul.nav a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Add|New", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

        page.locator("form:visible input[type=\"text\"]").first().fill(DEFAULT_DATES_POLL_TITLE);

        assertThat(page.locator("label[for=\"poll-text\"]")).hasClass(Pattern.compile(".*required.*"));
        assertThat(page.locator("label[for=\"open-date\"]")).hasClass(Pattern.compile(".*required.*"));
        assertThat(page.locator("label[for=\"close-date\"]")).hasClass(Pattern.compile(".*required.*"));
        assertThat(page.locator("label[for=\"min-options\"]")).hasClass(Pattern.compile(".*required.*"));
        assertThat(page.locator("label[for=\"max-options\"]")).hasClass(Pattern.compile(".*required.*"));

        assertThat(page.locator("input[name=\"openDate\"]")).not().hasValue("");
        assertThat(page.locator("input[name=\"closeDate\"]")).not().hasValue("");

        page.locator("form:visible").first().locator("button[type=\"submit\"]:has-text(\"Save\"), button[type=\"submit\"]:has-text(\"Add\"), button[type=\"submit\"]:has-text(\"Create\"), button[type=\"submit\"]:has-text(\"Continue\"), input[type=\"submit\"][value*=\"Save\"], input[type=\"submit\"][value*=\"Add\"], input[type=\"submit\"][value*=\"Create\"], input[type=\"submit\"][value*=\"Continue\"], .act button:has-text(\"Save\"), .act button:has-text(\"Add\"), .act button:has-text(\"Create\"), .act button:has-text(\"Continue\"), .act input[value*=\"Save\"], .act input[value*=\"Add\"], .act input[value*=\"Create\"], .act input[value*=\"Continue\"]")
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("textarea")).isVisible();
        assertThat(page.locator("body")).containsText(DEFAULT_DATES_POLL_TITLE);
    }

    @Test
    @Order(5)
    void canVoteAndViewResultsChart() {
        ensurePollWithTwoOptionsExists();
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Poll");

        Locator pollRow = page.locator("tr").filter(new Locator.FilterOptions().setHasText(POLL_TITLE)).first();
        assertThat(pollRow).isVisible();

        pollRow.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(POLL_TITLE))
            .first()
            .click(new Locator.ClickOptions().setForce(true));

        page.locator("input[name=\"selectedOptionIds\"]").first().check();
        page.locator("form:visible input[type=\"submit\"], form:visible button[type=\"submit\"]").first()
            .click(new Locator.ClickOptions().setForce(true));

        page.waitForURL(Pattern.compile(".*/voteThanks\\?voteRef=.*"));
        assertThat(page.locator(".portletBody .fw-bold")).containsText(Pattern.compile(".+"));

        page.locator("input[type=\"button\"][onclick*=\"/votePolls\"], button[onclick*=\"/votePolls\"], .act input[type=\"button\"], .act button").first()
            .click(new Locator.ClickOptions().setForce(true));

        assertThat(pollRow).isVisible();
        pollRow.locator("a[href*='/voteResults']").first()
            .click(new Locator.ClickOptions().setForce(true));

        assertThat(page.locator("#poll-results-chart")).isVisible();
        page.waitForFunction(
            "() => { const canvas = document.querySelector('#poll-results-chart');"
                + " if (!canvas) { return false; }"
                + " const chart = window.Chart && typeof window.Chart.getChart === 'function'"
                + " ? window.Chart.getChart(canvas) : null;"
                + " return Boolean(chart || window.pollResultsChartData); }"
        );
        Boolean chartInitialized = (Boolean) page.evaluate(
            "() => { const canvas = document.querySelector('#poll-results-chart');"
                + " if (!canvas) { return false; }"
                + " const chart = window.Chart && typeof window.Chart.getChart === 'function'"
                + " ? window.Chart.getChart(canvas) : null;"
                + " return Boolean(chart || window.pollResultsChartData); }"
        );
        assertTrue(chartInitialized);
        Locator resultsTable = page.locator(".table-responsive table").first();
        assertThat(resultsTable).containsText("Yes");
        assertThat(resultsTable).containsText(Pattern.compile("100\\s*%"));
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
