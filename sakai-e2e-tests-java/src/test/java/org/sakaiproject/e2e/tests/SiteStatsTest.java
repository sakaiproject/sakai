package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SiteStatsTest extends SakaiUiTestBase {

    private static String sakaiUrl;
    private static final String REPORT_TITLE = "Playwright Report " + System.currentTimeMillis();
    private static final String REPORT_DESC = "This is a Playwright-generated SiteStats report.";

    @Test
    @Order(1)
    void createsSiteWithStatistics() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.sitestats"));
    }

    @Test
    @Order(2)
    void createsReportViaReportsFlow() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);
        sakai.toolClick("Statistics");

        page.locator(".navIntraTool a, .navIntraTool button, a, button")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Reports$", Pattern.CASE_INSENSITIVE))).first()
            .click(new Locator.ClickOptions().setForce(true));

        Locator addReportLink = page.locator("a[href*=\"lnkNewReport\"], .navIntraTool a, a")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Add$", Pattern.CASE_INSENSITIVE))).first();
        if (addReportLink.count() == 0) {
            assertThat(page.locator("body")).containsText(Pattern.compile("Reports", Pattern.CASE_INSENSITIVE));
            return;
        }
        addReportLink.click(new Locator.ClickOptions().setForce(true));

        page.locator("#content, main, .portletBody").first().locator("input[type=\"text\"]:visible").first().fill(REPORT_TITLE);

        boolean hasCkEditor = Boolean.TRUE.equals(page.evaluate("() => Boolean(window.CKEDITOR && Object.keys(window.CKEDITOR.instances || {}).length)"));
        if (hasCkEditor) {
            String editorId = (String) page.evaluate("() => Object.keys(window.CKEDITOR.instances || {})[0]");
            sakai.typeCkEditor(editorId, "<p>" + REPORT_DESC + "</p>");
        } else {
            page.locator("#content, main, .portletBody").first()
                .locator("textarea:visible, [contenteditable=\"true\"]:visible, div[role=\"textbox\"]:visible")
                .first()
                .fill(REPORT_DESC);
        }

        page.locator("button:has-text(\"Generate report\"), input[type=\"submit\"][value*=\"Generate report\"]").first().click(new Locator.ClickOptions().setForce(true));
        assertThat(page.getByText(REPORT_TITLE).first()).isVisible();
    }
}
